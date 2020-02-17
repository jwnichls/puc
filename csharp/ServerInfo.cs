using System;
using System.Collections;
using System.Net;
using System.Threading;
using System.Windows.Forms;
using PUC.Communication;

namespace PUC
{
	/// <summary>
	/// This object represents the client's knowledge of a PUCProxy 
	/// server.  It is created when a user connects to a PUCProxy and
	/// contains the most recent information retrieved via a 
	/// ServerInformation message, pointers to the UI elements on the 
	/// PUC that represent the server, and pointers to the Appliances
	/// that a PUC may be connected to via the server.
	/// </summary>
	public class ServerInfo
	{
		/*
		 * Constants
		 */
		public const int SERVER_INFO_PORT = 5149;


		/*
		 * Member Variables
		 */
		protected        string      _name;
		protected        Hashtable   _appliances;
		protected        Connection  _connection;
		protected        MenuItem    _menu;

		protected        bool        _openAllNext;

		/*
		 * Constructor
		 */
		public ServerInfo( IPAddress ipAddress, bool openall )
		{
			_openAllNext = openall;
			_appliances  = new Hashtable();

			connectToServer( ipAddress );
		}

		public ServerInfo( IPAddress ipAddress )
			: this( ipAddress, false )
		{
		}


		/*
		 * Event Handler Methods
		 */
		protected void messageReceived( ConnectionEventArgs a )
		{
			PUC.Communication.Message msg = a.GetMessage();

			if ( msg is ServerInformation )
			{
				// Globals.GetDefaultLog().AddLogLine( msg.GetXML() );
				initUIMenu( (ServerInformation)msg );
				Globals.GetDefaultLog().AddLogLine( "Server information updated for " + _name );
			}
		}

		public void connectionRegained( ConnectionEventArgs a )
		{
			try
			{
				PUC.Communication.Message msg = new ServerInformationRequest();
				_connection.Send( msg );
			}
			catch( Exception )
			{
			}
		}


		/*
		 * Member Methods
		 */

		private void connectToServer( IPAddress ipAddress ) 
		{
			_connection  = new Connection( ipAddress, SERVER_INFO_PORT );
			_connection.Connect();

			if (! _connection.IsConnected() )
			{
				Globals.GetDefaultLog().AddLogLine( "Unable to connect to server info port." );
				_connection.Stop();
			}
			else
			{
				Globals.GetDefaultLog().AddLogLine( "Connected to appliance server " + _connection.GetConnectionAddr().ToString() + "." );
				_connection.MessageReceivedEvent += new Connection.MessageReceivedHandler(this.messageReceived);
				_connection.ConnectionRegainedEvent += new Connection.ConnectionRegainedHandler(this.connectionRegained);

				ServerInformationRequest sirmsg = new ServerInformationRequest();
				_connection.Send( sirmsg );
			}
		}

		/// <summary>
		/// This method resets the connection with the server,
		/// disconnecting all devices and re-downloading the 
		/// server information.
		/// </summary>
		/// <param name="openAll">specifies whether all appliances on the server should be opened</param>
		public void Reset( bool openAll )
		{
			// disconnect current connection
			_connection.MessageReceivedEvent -= new Connection.MessageReceivedHandler(this.messageReceived);
			_connection.ConnectionRegainedEvent -= new Connection.ConnectionRegainedHandler(this.connectionRegained);
			_connection.Stop();

			// disconnect any currently connected appliances
			IEnumerator e = _appliances.Values.GetEnumerator();
			while( e.MoveNext() )
			{
				ApplianceStub astub = (ApplianceStub)e.Current;

				if ( astub is Appliance )
				{
					Globals.GetFrame( (Appliance)astub ).RemoveAppliance( (Appliance)astub );
					((Appliance)astub)._connection.Stop();
				}
			}

			// clear all appliances from the list
			_appliances.Clear();

			// clear menu
			if ( _menu != null )
				_menu.MenuItems.Clear();

			// should we open all when we reconnect?
			_openAllNext = openAll;

			// reconnect to server
			connectToServer( _connection.GetConnectionAddr() );

			// set menuitem for server 
			// (temporary until server information msg arrives)
			MenuItem mi = new MenuItem();

			if ( _connection.IsConnected() )
				mi.Text = "No Appliances!";
			else
				mi.Text = "Not Connected";

			mi.Enabled = false;
			_menu.MenuItems.Add( mi );
		}

		// only call this from initUIMenu
		protected void initMenu()
		{
			if ( _menu != null ) return;

			_menu = new MenuItem();
			_menu.Text = _name;

			Globals.GetServerMenu().MenuItems.Add( _menu );
		}

		protected void initUIMenu( ServerInformation msg )
		{
			_name = msg.GetServerName();

			if ( _menu == null ) initMenu();

			_menu.MenuItems.Clear();
			clearMarks();

			int count = 0;

			_menu.Text = _name;

			IEnumerator e = msg.GetDevices();
			while( e.MoveNext() )
			{
				ServerInformation.DeviceInfo device = 
					(ServerInformation.DeviceInfo)e.Current;

				ApplianceStub astub = (ApplianceStub)_appliances[device.GetDeviceUniqueString()];
				if ( astub == null )
				{
					astub = new UnloadedAppliance( this,
						device.GetDevicePort(),
						device.GetDeviceName(),
						device.GetDeviceUniqueString() );
					_appliances[ astub.GetUniqueName() ] = astub;

					if ( _openAllNext )
						((UnloadedAppliance)astub).LoadAppliance();
				}

				astub.SetMark( true );
				count++;

				MenuItem mi = new MenuItem();
				mi.Text = device.GetDeviceName();
				_menu.MenuItems.Add( mi );
				astub.SetServerMenuItem( mi );
				mi.Click += new System.EventHandler(astub.GetServerMenuListener().MenuClicked);				
			}

			_openAllNext = false;

			if ( count < _appliances.Count )
			{
				MenuItem mi = null;
				if ( count > 0 ) 
				{
					mi = new MenuItem();
					mi.Text = "-";
					_menu.MenuItems.Add( mi );
				}

				e = _appliances.Values.GetEnumerator();
				while( e.MoveNext() )
				{
					ApplianceStub astub = (ApplianceStub)e.Current;
					if ( !astub.IsMarked() )
					{
						mi = new MenuItem();
						mi.Text = astub.GetServerName();
						mi.Enabled = false;
						_menu.MenuItems.Add( mi );
					}
				}
			}

			if ( _appliances.Count == 0 &&
				_menu.MenuItems.Count == 0 ) 
			{
				MenuItem mi = new MenuItem();
				mi.Text = "No Appliances!";
				mi.Enabled = false;
				_menu.MenuItems.Add( mi );
			}
		}

		protected void clearMarks()
		{
			IEnumerator e = _appliances.Values.GetEnumerator();
			while( e.MoveNext() )
				((ApplianceStub)e.Current).SetMark( false );
		}

		public IPAddress GetIPAddress()
		{
			return _connection.GetConnectionAddr();
		}

		public void OpenAllNext()
		{
			_openAllNext = true;
		}

		public void ActivateAppliance( UnloadedAppliance oldAppl, Appliance newAppl )
		{
			Globals.GetFrame( newAppl ).AddActiveAppliance( newAppl );

			_appliances[ newAppl.GetUniqueName() ] = newAppl;
		}

		public void Unload( Appliance appl )
		{
			UnloadedAppliance uAppl = new UnloadedAppliance( appl );

			_appliances[ appl.GetUniqueName() ] = uAppl;

			MenuItem mi = appl.GetServerMenuItem();
		}
	}
}
