using System;
using System.Collections;
using System.IO;
using System.Net;
using System.Net.Sockets;
using System.Threading;
using System.Xml;

namespace PUC.Communication
{
	/// <summary>
	/// Represents the communication stream between a PUC client and 
	/// appliance.
	/// </summary>
	public class Connection
	{
		/*
		 * Delegates & Events
		 */
		public delegate void MessageReceivedHandler( ConnectionEventArgs args );
		public event MessageReceivedHandler MessageReceivedEvent;

		public delegate void ConnectionLostHandler( ConnectionEventArgs args );
		public event ConnectionLostHandler ConnectionLostEvent;

		public delegate void ConnectionRegainedHandler( ConnectionEventArgs args );
		public event ConnectionRegainedHandler ConnectionRegainedEvent;


		/*
		 * Constants
		 */
		private const int LENGTH_BYTES = 4;


		/*
		 * Member Variables
		 */
		private static int _nextID = 0;
		private static ArrayList _connections = new ArrayList();
		private static Thread _monitorThread = null;
		private static Mutex _monitorMutex = new Mutex();
		private static bool _monitorStop = false;

		private int    _connID        = -1;
		private bool   _connected     = false;
		private Socket _socket        = null;
		private bool   _reconnect     = false;
		private Mutex  _socketMutex   = new Mutex();

		private int       _port       = -1;
		private IPAddress _ipAddress  = null;

		private Thread    _receiveThread = null;
		private bool      _receiveStop   = false;

		/*
		 * Constructor
		 */
		public Connection( IPAddress addr, int port )
		{
			if ( _monitorThread == null )
			{
				_monitorMutex.WaitOne();

				_monitorStop = false;
				_monitorThread = new Thread( new ThreadStart(Connection.monitorMethod) );
				_monitorThread.Priority = ThreadPriority.Lowest;
				_monitorThread.Start();

				_monitorMutex.ReleaseMutex();
			}

			_ipAddress = addr;
			_port = port;

			_connID = _nextID++;

			AddConnection( this );
		}

		public Connection( Socket s ) 
		{
			_connected = true;
			_socket = s;

			_ipAddress = null;
			_port = 0;
			
			_receiveStop = false;
			_receiveThread = new Thread( new ThreadStart( this.receiveMethod ) );
			_receiveThread.Priority = ThreadPriority.BelowNormal;
			_receiveThread.Start();

			_connID = _nextID++;

			AddConnection( this );
		}

		protected Connection()
		{
			_connected = true;
			_ipAddress = null;
			_port = 0;

			_connID = _nextID++;
		}

		/*
		 * Add/Remove Connection methods
		 */
		protected static void AddConnection( Connection c )
		{
			_monitorMutex.WaitOne();

			_connections.Add( c );

			_monitorMutex.ReleaseMutex();
		}

		protected static void RemoveConnection( Connection c )
		{
			_monitorMutex.WaitOne();

			_connections.Remove( c );

			_monitorMutex.ReleaseMutex();
		}

		
		/*
		 * MonitorThread method
		 */
		protected static void monitorMethod()
		{
			while( ! _monitorStop )
			{
				_monitorMutex.WaitOne();

				IEnumerator e = _connections.GetEnumerator();
				while( e.MoveNext() )
				{
					Connection c = (Connection)e.Current;

					if ( c.ReconnectRequested() )
					{
						c._socketMutex.WaitOne();

						c.Disconnect();
						c.Connect();
						if ( c.IsConnected() )
						{
							c._reconnect = false;
							Globals.AddEventCallback( new ConnRegainDispatcher( c ) );
						}

						c._socketMutex.ReleaseMutex();
					}
				}

				_monitorMutex.ReleaseMutex();
				Thread.Sleep( 1000 );
			}
		}


		/*
		 * Receive methods
		 */
		protected void receiveMethod()
		{
			Message msg = null;
			byte[] buf = new byte[65536];
			byte[] num = new byte[4];
			int bytesRead = 0;
			int len = 0;
			int xmllen = 0;

			byte[] oldbuf = buf;

			_socket.SetSocketOption( SocketOptionLevel.Socket, SocketOptionName.KeepAlive, 1 );

			while( ! _receiveStop )
			{
				try
				{
					if ( ReconnectRequested() )
					{
						Thread.Sleep( 1000 );
						continue;
					}

					// the receive process works in three steps
					// 1. Get the length of the message (4 bytes)
					// 2. Get the length of the xml portion (4 bytes)
					// 3. Get the message data

					// make sure that a reconnection isn't somehow in progress
					_socketMutex.WaitOne();

					// STEP #1: Read length value

					len = 0;

					while( true )
					{
						len += _socket.Receive( num, len, 4-len, SocketFlags.None );
						if ( len == 4 )
						{
							len = IPAddress.NetworkToHostOrder( BitConverter.ToInt32( num, 0 ) );
							break;
						}
					}
					
					// STEP #2: Read XML length value

					xmllen = 0;
					while( true )
					{
						xmllen += _socket.Receive( num, xmllen, 4-xmllen, SocketFlags.None );
						if ( xmllen == 4 )
						{
							xmllen = IPAddress.NetworkToHostOrder( BitConverter.ToInt32( num, 0 ) );

							if ( xmllen > len )
								throw new IOException( "XML length cannot exceed message length" );

							break;
						}
					}

					// STEP #3: Read Message Data
					
					// check to see if we need a longer buffer for this message, and create
					// one if needed
					if ( len > buf.Length )
					{
						buf = new byte[ len ];
					}

					bytesRead = 0;
					while( true ) 
					{
						bytesRead += _socket.Receive( buf, bytesRead, len - bytesRead, SocketFlags.None );	
						if ( bytesRead == len )
						{
							// get the xml data into usable form
							string result = System.Text.Encoding.ASCII.GetString( buf, 0, xmllen );
							XmlTextReader xml = new XmlTextReader( new StringReader( result ) );

							// get binary data into usable form (if necessary)
							MemoryStream binaryData = null;
							if ( len > xmllen ) 
							{
								byte[] binbuf = new byte[ len-xmllen ];
								Array.Copy( buf, xmllen, binbuf, 0, len-xmllen );
								binaryData = new MemoryStream( binbuf, 0, binbuf.Length, false, true );
							}

							msg = Message.Decode( xml, binaryData );

							if ( msg != null && msg.IsValid() )
								Globals.AddEventCallback( new MessageDispatcher( this, msg ) );

							break;
						}
					}

					buf = oldbuf;

					_socketMutex.ReleaseMutex();

				}
				catch( Exception )
				{
					if ( !_receiveStop )
					{
						RequestReconnect();
						Globals.AddEventCallback( new ConnLostDispatcher( this ) );
					}

					try 
					{
						// release if we're still holding it
						_socketMutex.ReleaseMutex();
					}
					catch( ApplicationException ) { }
				}
			}
		}

		/*
		 * Properties
		 */
		public int LocalPort
		{
			get
			{
				return ((IPEndPoint)_socket.LocalEndPoint).Port;
			}
		}


		/*
		 * Member Methods
		 */
		public void RequestReconnect()
		{
			_reconnect = true;
		}

		public bool ReconnectRequested()
		{
			return _reconnect;
		}

		public IPAddress GetConnectionAddr()
		{
			return _ipAddress;
		}

		public int GetConnectionPort()
		{
			return _port;
		}

		public bool IsConnected()
		{
			return _connected;
		}

		public virtual void Connect()
		{
			if ( _connected || _ipAddress == null ) return;

			try
			{
				_socket = new Socket( AddressFamily.InterNetwork,
									  SocketType.Stream,
									  ProtocolType.IP );
				IPEndPoint end = new IPEndPoint( _ipAddress, _port );

				_socket.Connect( end );

				_receiveStop = false;

				if ( _receiveThread == null )
				{
					_receiveThread = new Thread( new ThreadStart( this.receiveMethod ) );
					_receiveThread.Priority = ThreadPriority.BelowNormal;
					_receiveThread.Start();
				}

				_connected = true;
			}
			catch( Exception )
			{
				// PUCFrame.AddLogLine( "Exception in Connect: " + e.Message );
			}
		}

		public Socket GetNewSocket()
		{
			try
			{
				_socket = new Socket( AddressFamily.InterNetwork,
					SocketType.Stream,
					ProtocolType.IP );
				IPEndPoint end = new IPEndPoint( _ipAddress, _port );

				_socket.Connect( end );

				return _socket;
			}
			catch( Exception )
			{
				return null;
			}
		}

		public virtual void Disconnect()
		{
			if (! _connected ) return;

			_receiveStop = true;
			_socket.Close();

			_connected = false;
		}

		public void Stop()
		{
			_monitorMutex.WaitOne();

			if ( _connected ) Disconnect();

			_connections.Remove( this );

			_monitorMutex.ReleaseMutex();
		}
		
		public int GetConnectionID()
		{
			return _connID;
		}

		public virtual void Send( Message msg )
		{
			try
			{
				int len, xmllen;
				msg.PrepareMessage( this );
				string msgstr = msg.GetXML();
				byte[] msgbytes = System.Text.Encoding.ASCII.GetBytes( msgstr );
				xmllen = len = msgbytes.Length;

				// add binary data, if appropriate
				if ( msg.HasBinaryData() )
				{
					len += (int)msg.BinaryData.Length;
					byte[] buf = new byte[ len ];
					Array.Copy( msgbytes, 0, buf, 0, msgbytes.Length );
					msg.BinaryData.Read( buf, msgbytes.Length, (int)msg.BinaryData.Length );
					msgbytes = buf;				
				}

				_socket.Send( BitConverter.GetBytes( IPAddress.HostToNetworkOrder( len ) ) );
				_socket.Send( BitConverter.GetBytes( IPAddress.HostToNetworkOrder( xmllen ) ) );
				_socket.Send( msgbytes );
			}
			catch( Exception )
			{
			}
		}

		/*
		public static int LsbByteArrayToInt( byte[] array )
		{
			unchecked 
			{
				int output = -1;

				if ( array == null )
					return output;

				if ( array.Length > 0 )
					output = 0;
				output |= (array[0]) & (int)0xFF;
				if ( array.Length > 1 )
					output |= (array[1] << 8) & (int) 0xFF00;
				if ( array.Length > 2 ) 
				{
					output |= (array[2] << 16) & (int) 0xFF0000;
					output |= (array[3] << 24) & (int) 0xFF000000;
				}
				return output;
			}
		}

		public static byte[] IntToLsbByteArray( int num, int len )
		{
			unchecked
			{
				byte[] array = null;

				if ((len == 4) || (len ==2) || (len == 1))
				{
					array = new byte[len];

					array[0] = (byte) (num & (long) 0xFF);
					if (len == 1) return array;
					array[1] = (byte) ((num & (long) 0xFF00) >> 8);
					if (len == 2) return array;
					array[2] = (byte) ((num & (long) 0xFF0000) >> 16);
					array[3] = (byte) ((num & (long) 0xFF000000) >> 24);
					return array;
				}

				return array;
			}
		}
		*/
		
		public void DispatchMessage( Message msg )
		{
			if ( MessageReceivedEvent != null )
				MessageReceivedEvent( new ConnectionEventArgs( this, msg ) );
		}

		public void DispatchConnLost()
		{
			if ( ConnectionLostEvent != null )
				ConnectionLostEvent( new ConnectionEventArgs( this, null ) );
		}

		public void DispatchConnRegain()
		{
			if ( ConnectionRegainedEvent != null )
				ConnectionRegainedEvent( new ConnectionEventArgs( this, null ) );
		}
	}

	/// <summary>
	/// The object sent when a connection event occurs.
	/// </summary>
	public class ConnectionEventArgs
	{
		private Connection _conn = null;
		private Message    _msg = null;

		public ConnectionEventArgs( Connection conn, Message msg )
		{
			_conn = conn;
			_msg = msg;
		}

		public Connection GetConnection()
		{
			return _conn;
		}

		public Message GetMessage()
		{
			return _msg;
		}
	}

	public class MessageDispatcher : PUC.IEventDispatcher
	{
		private Message    _msg;
		private Connection _conn;

		public MessageDispatcher( Connection c, Message m )
		{
			_conn = c;
			_msg = m;
		}

		public void Dispatch()
		{
			_conn.DispatchMessage( _msg );
		}
	}

	public class ConnLostDispatcher : PUC.IEventDispatcher
	{
		private Connection _conn;

		public ConnLostDispatcher( Connection c )
		{
			_conn = c;
		}

		public void Dispatch()
		{
			_conn.DispatchConnLost();
		}
	}

	public class ConnRegainDispatcher : PUC.IEventDispatcher
	{
		private Connection _conn;

		public ConnRegainDispatcher( Connection c )
		{
			_conn = c;
		}

		public void Dispatch()
		{
			_conn.DispatchConnRegain();
		}
	}
}
