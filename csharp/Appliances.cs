using System;
using System.Collections;
using System.IO;
using System.Windows.Forms;

#if SMARTPHONE
	using PhonePUC;
#endif

using PUC.Communication;
using PUC.Parsers;
using PUC.UIGeneration;

namespace PUC
{
	/// <summary>
	/// The base of any class that represents appliances, loaded or
	/// not.
	/// 
	/// TODO: sub-class this with a CachedAppliance class!
	/// </summary>
	public abstract class ApplianceStub
	{
		/*
		 * Member Variables
		 */
		protected string     _serverName;
		protected string     _uniqueName;
		protected ServerInfo _server;
		protected int        _port;

		/// <summary>
		/// Used to search for deactivated appliances in 
		/// ServerInformation messages. (see ServerInfo.cs)
		/// </summary>
		protected bool               _mark;

		public    Connection         _connection;
		protected MenuItem           _serverMenuItem;
		protected ServerMenuListener _serverMenuListener;

		/*
		 * Constructor
		 */
		public ApplianceStub( ServerInfo server, int port, string servername, string uniquename )
		{
			_server = server;
			_port = port;
			_serverName = servername;
			_uniqueName = uniquename;

			_mark = false;

			_serverMenuListener = new ServerMenuListener( this );
		}

		public ApplianceStub( ApplianceStub stub )
		{
			_server             = stub._server;
			_port               = stub._port;
			_serverName         = stub._serverName;
			_uniqueName         = stub._uniqueName;
			_mark               = stub._mark;

			_connection         = stub._connection;

			_serverMenuItem     = stub._serverMenuItem;
			_serverMenuListener = stub._serverMenuListener;
			_serverMenuListener.SetAppliance( this );
		}

		/*
		 * Member Methods
		 */
		public abstract bool IsLoaded();

		public bool IsMarked()
		{
			return _mark;
		}

		public void SetMark( bool mark )
		{
			_mark = mark;
		}

		public virtual string Name
		{
			get
			{
				return _serverName;
			}
		}

		public string GetServerName()
		{
			return _serverName;
		}

		public string GetUniqueName()
		{
			return _uniqueName;
		}

		public ServerInfo GetServer()
		{
			return _server;
		}

		public Connection GetConnection()
		{
			return _connection;
		}

		public void SetServerMenuItem( MenuItem item )
		{
			_serverMenuItem = item;
		}

		public MenuItem GetServerMenuItem()
		{
			return _serverMenuItem;
		}

		public ServerMenuListener GetServerMenuListener()
		{
			return _serverMenuListener;
		}
	}


	/// <summary>
	/// Instances of this class represent appliances that are known 
	/// about, but not connected to.
	/// </summary>
	public class UnloadedAppliance : ApplianceStub
	{
		/*
		 * Member Variables
		 */

		/*
		 * Constructor
		 */
		public UnloadedAppliance( ServerInfo server, int port, string name, string uniquename )
			: base( server, port, name, uniquename )
		{
		}

		public UnloadedAppliance( ApplianceStub applStub )
			: base( applStub )
		{
			if ( _connection != null ) 
			{
				_connection.Stop();
				_connection = null;
			}
		}

		/*
		 * Member Methods
		 */
		public override bool IsLoaded()
		{
			return false;
		}

		public Appliance LoadAppliance()
		{
			try
			{
				Cursor.Current = Cursors.WaitCursor;
				_connection = new Connection( _server.GetIPAddress(), _port );
				_connection.Connect();

				if ( !_connection.IsConnected() )
				{
					Globals.GetDefaultLog().AddLogLine( "Unable to connect to appliance..." );
					_connection.Stop();
					Cursor.Current = Cursors.Default;
				}
				else
				{
					_connection.MessageReceivedEvent += new Connection.MessageReceivedHandler(this.messageReceived);
					_connection.ConnectionRegainedEvent += new Connection.ConnectionRegainedHandler(this.connectionRegained);

					PUC.Communication.Message msg = new SpecRequest();
					_connection.Send( msg );
				}
			}
			catch( Exception )
			{
				Cursor.Current = Cursors.Default;
			}

			return null;
		}

		protected void connectionRegained( ConnectionEventArgs a )
		{
			try
			{
				PUC.Communication.Message msg = new SpecRequest();
				_connection.Send( msg );
			}
			catch( Exception )
			{
			}
		}

		protected void messageReceived( ConnectionEventArgs a )
		{
			PUC.Communication.Message msg = a.GetMessage();

			if ( msg is DeviceSpec )
			{
				DeviceSpec dmsg = (DeviceSpec)msg;

				Appliance appl = new Appliance( this );

#if POCKETPC
				Globals.AddFrameMapping( appl, PUCFrame.DEFAULT_FRAME );
#endif
#if SMARTPHONE
				Globals.AddFrameMapping( appl, PhonePUC.PUCFrame.DEFAULT_FRAME );
#endif
#if DESKTOP && !DEBUGSVR
				// TODO: Make DesktopPUC use multiple windows, like Debug Server
				Globals.AddFrameMapping( appl, DesktopPUC.PUCFrame.DEFAULT_FRAME );
#endif

				try 
				{
					SpecParser.Parse( new StringReader( dmsg.GetSpec() ), appl );	

					MenuItem mi = new MenuItem();
					mi.Text = appl.Name;
					appl.SetMenuItem( mi );

					_server.ActivateAppliance( this, appl );

					Cursor.Current = Cursors.Default;

					_connection.MessageReceivedEvent -= new Connection.MessageReceivedHandler(this.messageReceived);
					_connection.ConnectionRegainedEvent -= new Connection.ConnectionRegainedHandler(this.connectionRegained);
					_connection.ConnectionLostEvent += new Connection.ConnectionLostHandler(appl.ConnectionLost);
					_connection.MessageReceivedEvent += new Connection.MessageReceivedHandler(appl.MessageReceived);
					_connection.ConnectionRegainedEvent += new Connection.ConnectionRegainedHandler(appl.ConnectionRegained);

					FullStateRequest fsrMsg = new FullStateRequest();
					_connection.Send( fsrMsg );
				}
				catch( Exception e )
				{
					Globals.GetFrame( appl ).AddLogLine( e.ToString() );
					Cursor.Current = Cursors.Default;
				}
			}
		}
	}

	/// <summary>
	/// An instance of this class represents an appliance that the
	/// PUC can control.  This includes information from the 
	/// specification, links to generated interface panels, links 
	/// to network resources, and network event handlers.
	/// </summary>
	public class Appliance : ApplianceStub
	{
		/*
		 * Member Variables
		 */

		protected GroupNode			_root;
		protected ArrayList			_dependedObjects;
		protected VariableTable		_varTable;
		protected LabelDictionary	_applianceLabel;

		protected MenuItem			_deviceMenuItem;

		protected UIGenerator		_uiGenerator;

        #if SMARTPHONE
		protected Stack				_leftMenuStack;
        #endif


		/*
		 * Constructor
		 */
		public Appliance( UnloadedAppliance appl )
			: base( appl )
		{
			#if SMARTPHONE
				_leftMenuStack = new Stack();

				_leftMenuStack.Push( new LeftMenuStackItem( "Log", new EventHandler(this.leftHandler) ) );
			#endif
		}

		public Appliance( Connection c )
			: base( null, 0, "echo", "echo:0" )
		{
			_connection = c;

			_mark = false;

			#if SMARTPHONE
				_leftMenuStack = new Stack();

				_leftMenuStack.Push( new LeftMenuStackItem( "Log", new EventHandler(this.leftHandler) ) );
			#endif
		}


		/*
		 * Member Methods
		 */
		public void AddLogText( string msg ) 
		{
			Globals.GetFrame( this ).AddLogText( msg );
		}

		public void AddLogLine( string msg )
		{
			Globals.GetFrame( this ).AddLogLine( msg );
		}

		public override bool IsLoaded()
		{
			return true;
		}

		public UIGenerator GetUIGenerator()
		{
			return _uiGenerator;
		}

		public void SetUIGenerator( UIGenerator ui )
		{
			_uiGenerator = ui;
		}

		public GroupNode GetRoot()
		{
			return _root;
		}

		public void ConnectionLost( ConnectionEventArgs a )
		{
			_uiGenerator.Enabled = false;

			Globals.GetFrame( this ).AddLogLine( "Connection lost to " + Name + "." );
			//Globals.GetFrame().ShowLogPanel();
			MessageBox.Show( "Connection lost to " + Name + ".", 
							 "Connection Lost" );
		}
			
		public void ConnectionRegained( ConnectionEventArgs a )
		{
			_uiGenerator.Enabled = true;
			Globals.GetFrame( this ).AddLogLine( "Reconnected to " + Name + "." );

			try
			{
				PUC.Communication.Message msg = new FullStateRequest();
				_connection.Send( msg );
			}
			catch( Exception )
			{
			}

			MessageBox.Show( "Regained connection to " + Name + ".", 
							 "Connection Regained" );
		}

		public void MessageReceived( ConnectionEventArgs a )
		{
			PUC.Communication.Message msg = a.GetMessage();

			if ( msg is StateChangeNotification )
			{
				_varTable.HandleStateChangeNotification( (StateChangeNotification)msg );
			}
			else if ( msg is BinaryStateChangeNotification )
			{
				_varTable.HandleBinaryStateChangeNotification( (BinaryStateChangeNotification)msg );
			}
			else if ( msg is AlertInformation )
			{
				MessageBox.Show( this.Name, ((AlertInformation)msg).GetAlertMessage() );
			}
		}

		public void SetParseVariables( GroupNode root, 
									   VariableTable varTable, ArrayList dobjs )
		{
			_root = root;
			_varTable = varTable;
			_dependedObjects = dobjs;
			_applianceLabel = _root.Labels;
		}

		public void SetMenuItem( MenuItem item )
		{
			_deviceMenuItem = item;
		}

		public MenuItem GetMenuItem()
		{
			return _deviceMenuItem;
		}

		public ArrayList GetDependedObjects()
		{
			return _dependedObjects;
		}

		public VariableTable VariableTable
		{
			get
			{
				return _varTable;
			}
		}


		/*
		 * Properties
		 */

		public override string Name
		{
			get
			{
				return _applianceLabel.GetShortestLabel();
			}
		}

#if SMARTPHONE
		public Stack LeftMenuStack
		{
			get
			{
				return _leftMenuStack;
			}
		}


		/*
		 * LeftMenuHandler
		 */

		protected void leftHandler( object source, EventArgs a )
		{
			Globals.GetFrame( this ).ShowLogPanel();
		}
#endif
	}

	/// <summary>
	/// A generic class for listening to menu events from the items 
	/// in the server menu.
	/// </summary>
	public class ServerMenuListener
	{
		ApplianceStub _appliance;

		public ServerMenuListener( ApplianceStub appl )
		{
			_appliance = appl;
		}

		public void SetAppliance( ApplianceStub appl )
		{
			_appliance = appl;
		}

		public void MenuClicked(object sender, System.EventArgs e)
		{
			if ( _appliance is UnloadedAppliance )
			{
				Globals.GetDefaultLog().AddLogLine( "Loading " + _appliance.GetServerName() + "..." );
				((UnloadedAppliance)_appliance).LoadAppliance();
			}
			else
			{
				Globals.GetFrame( (Appliance)_appliance ).SetCurrentAppliance( (Appliance)_appliance );
			}
		}
	}
}
