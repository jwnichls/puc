using System;
using System.Collections;
using System.Diagnostics;
using System.Net;
using System.Net.Sockets;
using System.Threading;
using System.Windows.Forms;

using PUC;

namespace PUC.Communication
{
	/// <summary>
	/// Summary description for PUCServer.
	/// </summary>
	public class PUCServer
	{
		/*
		 * Constants
		 */

		public const int SERVER_INFO_PORT  = 5149;


		/*
		 * Member Variables
		 */

		protected ArrayList		_listeners;
		protected ArrayList		_connections;
		protected ArrayList		_devices;

		protected Hashtable		_connectionDeviceMap;

		/// <summary>
		/// Keeps track of whether or not this server is running
		/// the discovery service (only one allowed per IP address).
		/// </summary>
		protected bool			_discoveryServiceRunning;

		/// <summary>
		/// The connection to the discovery service, if it is not
		/// located on this service.
		/// </summary>
		protected Connection	_discoveryConnection;

		/// <summary>
		/// Used to store devices registered with the discovery 
		/// service.
		/// </summary>
		protected Hashtable		_registeredDevices;


		/*
		 * Constructor
		 */

		public PUCServer()
		{
			_listeners = new ArrayList();
			_connections = new ArrayList();
			_devices = new ArrayList();
			_registeredDevices = new Hashtable();
			_connectionDeviceMap = new Hashtable();

			try 
			{
				startDiscoveryService();
			}
			catch( Exception )
			{
				// starting the discovery service failed
				// this means that we will connect to the existing service
				// to register our devices

				try
				{
					_discoveryServiceRunning = false;

					_discoveryConnection = new Connection( IPAddress.Loopback, SERVER_INFO_PORT );
					_discoveryConnection.Connect();

					// set up event callbacks
					_discoveryConnection.ConnectionLostEvent += new PUC.Communication.Connection.ConnectionLostHandler(_discoveryConnection_ConnectionLostEvent);
					_discoveryConnection.ConnectionRegainedEvent += new PUC.Communication.Connection.ConnectionRegainedHandler(_discoveryConnection_ConnectionRegainedEvent);

					if ( !_discoveryConnection.IsConnected() )
						MessageBox.Show( "Cannot connect to discovery service!", "Error" );
				}
				catch( Exception e2 )
				{
					MessageBox.Show( "Cannot connect to discovery service.", e2.GetType().Name );
				}
			}
		}


		/*
		 * Service discovery methods
		 */

		protected void startDiscoveryService() 
		{
			InfoListener listen = new InfoListener( this );
			listen.Start();
			_listeners.Add( listen );

			_discoveryServiceRunning = true;
			if ( _discoveryConnection != null )
				_discoveryConnection.Stop();
			_discoveryConnection = null;

			_registeredDevices.Clear();
		}

		private void _discoveryConnection_ConnectionLostEvent(ConnectionEventArgs args)
		{
			// if the discovery service ever goes away, another
			// server can start one

			try
			{
				startDiscoveryService();
				serverStateChanged( false, null );
			}
			catch( Exception )
			{
				_discoveryServiceRunning = false;
			}
		}

		private void _discoveryConnection_ConnectionRegainedEvent(ConnectionEventArgs args)
		{
			if ( !_discoveryServiceRunning )
			{
				try
				{
					IEnumerator e = _devices.GetEnumerator();
					while( e.MoveNext() )
					{
						IDevice2 dev = (IDevice2)e.Current;
						Message  msg = new RegisterDevice( dev.Name, dev.Port );
						_discoveryConnection.Send( msg );
					}
				}
				catch( Exception )
				{
					// do nothing here...wait for another opportunity 
					// to re-connect
				}
			}
		}

		protected void serverStateChanged( bool added, IDevice2 dev )
		{
			if ( _discoveryServiceRunning )
			{
				Message msg = new ServerInformation( Name, _devices.GetEnumerator() );

				for( int i = 0; i < _connections.Count; i++ )
				{
					Connection c = (Connection)_connections[ i ];

					if ( !c.IsConnected() )
					{
						_connections.RemoveAt( i );
						i--;
						continue;
					}

					if ( c.LocalPort == SERVER_INFO_PORT )
						c.Send( msg );
				}
			}
			else
			{
				if ( dev == null )
					return;

				Message msg;
				if ( added )
					msg = new RegisterDevice( dev.Name, dev.Port );
				else
					msg = new UnregisterDevice( dev.Port );

				try
				{
					_discoveryConnection.Send( msg );
				}
				catch( Exception )
				{
					// problem sending message to discovery service
				}
			}
		}


		/*
		 * Properties
		 */

		public string Name
		{
			get
			{
				try
				{
					return Dns.GetHostName();
				}
				catch( Exception )
				{
					return "PUCProxy";
				}
			}
		}


		/*
		 * Member Methods
		 */

		public void AddDevice( IDevice2 device )
		{
			_devices.Add( device );
		}

		public void RemoveDevice( IDevice2 device )
		{
			if ( device.IsRunning() )
			{
				StopListener( device );

				IEnumerator e = _connections.GetEnumerator();
				while( e.MoveNext() )
				{
					Connection c = (Connection)e.Current;
					if ( c.LocalPort == device.Port )
						c.Stop();
				}
			}

			_devices.Remove( device );
		}

		public void StartListener( IDevice2 dev )
		{
			if ( findListener( dev.Port ) != null )
				return;

			try
			{
				Listener l = new DeviceListener( this, dev );
				l.Start();
				_listeners.Add( l );

				serverStateChanged( true, dev );
			}
			catch( Exception e )
			{
				MessageBox.Show( "Another appliance is already running on this port.", e.GetType().Name );
			}
		}

		public void StopListener( IDevice2 dev )
		{
			Listener l = findListener( dev.Port );

			if ( l == null )
				return;

			_listeners.Remove( l );
			l.Stop();

			serverStateChanged( false, dev );
		}

		public bool IsListenerOnPort( int port ) 
		{
			return findListener( port ) != null;
		}

		protected Listener findListener( int port )
		{
			IEnumerator e = _listeners.GetEnumerator();
			while( e.MoveNext() )
			{
				Listener l = (Listener)e.Current;
				if ( l.Port == port )
					return l;
			}

			return null;
		}

		public void AddDeviceConnection( Connection c, IDevice2 dev )
		{
			_connections.Add( c );
			dev.AddConnection( c );
			_connectionDeviceMap[ c ] = dev;

			c.MessageReceivedEvent += new PUC.Communication.Connection.MessageReceivedHandler(DeviceMessageHandler);
			c.ConnectionLostEvent += new PUC.Communication.Connection.ConnectionLostHandler(ConnectionLostHandler);
		}

		public void AddInfoConnection( Connection c )
		{
			_connections.Add( c );
			c.MessageReceivedEvent += new PUC.Communication.Connection.MessageReceivedHandler(InfoMessageHandler);
		}

		public void InfoMessageHandler( ConnectionEventArgs a )
		{
			Message m = a.GetMessage();

			if ( m == null )
				return;

			if ( m is ServerInformationRequest )
			{
				Connection c = a.GetConnection();
				ServerInformation smsg = new ServerInformation( Name, _devices.GetEnumerator() );

				try 
				{
					c.Send( smsg );
				}
				catch( Exception ) { }
			}
			else if ( m is RegisterDevice )
			{
				RegisteredDevice dev = new RegisteredDevice( (RegisterDevice)m );

				if ( _registeredDevices[ dev.Port ] == null )
				{
					_registeredDevices[ dev.Port ] = dev;
					_devices.Add( dev );
					serverStateChanged( true, dev );
				}
			}
			else if ( m is UnregisterDevice )
			{
				RegisteredDevice dev = (RegisteredDevice)
					_registeredDevices[ ((UnregisterDevice)m).GetDevicePort() ];

				if ( dev != null )
				{
					_registeredDevices.Remove( dev );
					_devices.Remove( dev );
					serverStateChanged( false, dev );
				}
			}
		}

		private void DeviceMessageHandler(ConnectionEventArgs args)
		{
			Message msg = args.GetMessage();

			if ( msg == null )
				return;

			Connection c = args.GetConnection();
			IDevice2 dev = (IDevice2)_connectionDeviceMap[ c ];

			Debug.Assert( dev != null );

			if ( msg is ServerInformationRequest )
			{
				c.Send( new ServerInformation( Name, _devices.GetEnumerator() ) );
			}
			else
				dev.HandleMessage( c, msg );
		}

		private void ConnectionLostHandler(ConnectionEventArgs args)
		{
			Connection c = args.GetConnection();
			IDevice2 dev = (IDevice2)_connectionDeviceMap[ c ];
	
			c.Stop();
	
			_connections.Remove( c );
			_connectionDeviceMap.Remove( c );
			if ( dev != null )
				dev.RemoveConnection( c );
		}
	}

	public abstract class Listener 
	{
		/*
		 * Member Variables
		 */

		protected PUCServer _server;
		protected int       _port;
		protected Socket	_socket;
		protected Thread	_thread;


		/*
		 * Constructor
		 */

		public Listener( PUCServer server, int port ) 
		{
			_server = server;
			_port = port;
		}


		/*
		 * Properties
		 */

		public int Port
		{
			get
			{
				return _port;
			}
		}


		/*
		 * Member Methods
		 */

		public bool IsRunning()
		{
			return _thread != null && _thread.ThreadState == System.Threading.ThreadState.Running;
		}

		public void Start() 
		{
			_socket = new Socket( AddressFamily.InterNetwork, SocketType.Stream, ProtocolType.IP );
			IPEndPoint endpt = new IPEndPoint( IPAddress.Any, _port );
			_socket.Bind( endpt );

			_thread = new Thread( new ThreadStart(this.ListenThread) );
			_thread.Priority = ThreadPriority.BelowNormal;
			_thread.Start();

			_socket.Listen( 1 );
		}

		public void ListenThread()
		{
			try 
			{
				while( true )
				{
					Thread.Sleep( 1000 );

					Socket s = _socket.Accept();

					if ( s != null )
						HandleIncoming( new Connection( s ) );
				}
			}
			catch( Exception ) { }
		}

		public void Stop()
		{
			if ( _thread != null )
				_thread.Suspend();

			if ( _socket != null )
				_socket.Close();
		}


		/*
		 * Abstract Methods
		 */

		public abstract void HandleIncoming( Connection c );
	}

	public class InfoListener : Listener
	{
		public InfoListener( PUCServer server ) 
			: base( server, PUCServer.SERVER_INFO_PORT )
		{
		}

		public override void HandleIncoming( Connection c )
		{
			_server.AddInfoConnection( c );
		}
	}

	public class DeviceListener : Listener
	{
		/*
		 * Member Variables
		 */

		protected IDevice2 _device;


		/*
		 * Constructor
		 */

		public DeviceListener( PUCServer server, IDevice2 device )
			: base( server, device.Port )
		{
			_device = device;
		}

		public override void HandleIncoming(Connection c)
		{
			_server.AddDeviceConnection( c, _device );
		}
	}

	public class RegisteredDevice : IDevice2
	{
		/*
		 * Member Variables
		 */

		protected string	_name;
		protected int		_port;


		/*
		 * Constructors
		 */

		public RegisteredDevice( string name, int port ) 
		{
			_name = name;
			_port = port;
		}

		public RegisteredDevice( RegisterDevice msg )
		{
			_name = msg.GetDeviceName();
			_port = msg.GetDevicePort();
		}


		/*
		 * IDevice2 Methods
		 */

		#region IDevice2 Members

		public string Name
		{
			get
			{
				return _name;
			}
		}

		public string Specification
		{
			get
			{
				throw new NotImplementedException( "RegisteredDevice objects are not implemented through this server." );
			}
		}

		public void HandleMessage(Connection c, Message m)
		{
			throw new NotImplementedException( "RegisteredDevice objects are not implemented through this server." );
		}

		public void AddConnection(Connection c)
		{
			throw new NotImplementedException( "RegisteredDevice objects are not implemented through this server." );
		}

		public void RemoveConnection(Connection c)
		{
			throw new NotImplementedException( "RegisteredDevice objects are not implemented through this server." );
		}

		public void RemoveAllConnections()
		{
			throw new NotImplementedException( "RegisteredDevice objects are not implemented through this server." );
		}

		public void Configure()
		{
			throw new NotImplementedException( "RegisteredDevice objects are not implemented through this server." );
		}

		public bool HasGUI()
		{
			throw new NotImplementedException( "RegisteredDevice objects are not implemented through this server." );
		}

		public void SetGUIVisiblity(bool visible)
		{
			throw new NotImplementedException( "RegisteredDevice objects are not implemented through this server." );
		}

		public bool IsGUIVisible()
		{
			throw new NotImplementedException( "RegisteredDevice objects are not implemented through this server." );
		}

		public void Start()
		{
			throw new NotImplementedException( "RegisteredDevice objects are not implemented through this server." );
		}

		public void Stop()
		{
			throw new NotImplementedException( "RegisteredDevice objects are not implemented through this server." );
		}

		public bool IsRunning()
		{
			return true;
		}

		public string Status
		{
			get
			{
				throw new NotImplementedException( "RegisteredDevice objects are not implemented through this server." );
			}
		}

		public event System.EventHandler StatusChanged;

		public int Port
		{
			get
			{
				return _port;
			}
			set
			{
				throw new NotImplementedException( "RegisteredDevice objects are not implemented through this server." );
			}
		}

		#endregion
	}
}
