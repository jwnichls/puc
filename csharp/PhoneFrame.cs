using System;
using System.Collections;
using System.Data;
using System.Diagnostics;
using System.Drawing;
using System.Net;
using System.Threading;
using System.Windows.Forms;

using PUC;
using PUC.CIO;
using PUC.CIO.TimeDuration;
using PUC.Communication;
using PUC.Parsers;
using PUC.PersistentData;
using PUC.Registry;
using PUC.Rules;
using PUC.Rules.BuildConcreteList;
using PUC.Rules.BuildList;
using PUC.Rules.ListManipulate;
using PUC.Rules.SpecScan;
using PUC.Rules.UnitScan;
using PUC.UIGeneration;


namespace PhonePUC
{
	/// <summary>
	/// Summary description for Form1.
	/// </summary>
	public class PUCFrame : System.Windows.Forms.Form, IPUCFrame, IShutdown, ICallbackManager, IServerManager
	{
		public const string VERSION_STRING = "PUC v3.0";

		public static IPUCFrame DEFAULT_FRAME;

		private ArrayList _appliances;
		private Appliance _currentAppliance;
		private ArrayList _servers;
		private Queue     _uiEventQueue;

		private Mutex     _mutex;

		private bool      _recentMenuItems;

		protected PhoneConnectDlg _connectDialog;
		protected PhoneOptionsDlg _optionsDialog;

		protected Stack _leftMenuStack;

		protected MeasureStringControl _measureCtl;

		protected ArrayList			_rulePhases;
		protected SmartCIOManager	_smartCIOManager;
		protected WidgetRegistry	_registry;

		private System.Windows.Forms.MenuItem menuItem4;
		private System.Windows.Forms.MenuItem menuItem6;
		private System.Windows.Forms.MenuItem menuItem10;
		private System.Windows.Forms.MenuItem menuItem11;
		private System.Windows.Forms.MenuItem actionItem;
		private System.Windows.Forms.Panel logPanel;
		private System.Windows.Forms.Label label1;
		private System.Windows.Forms.TextBox logBox;
		private System.Windows.Forms.MenuItem pucMenu;
		private System.Windows.Forms.MenuItem openItem;
		private System.Windows.Forms.MenuItem logItem;
		private System.Windows.Forms.Timer eventQueueTimer;
		private System.Windows.Forms.MenuItem disconnectItem;
		private System.Windows.Forms.MenuItem recentMenu;
		private System.Windows.Forms.MenuItem serverMenu;
		private System.Windows.Forms.MenuItem deviceMenu;
		private System.Windows.Forms.MenuItem optionsItem;
		private System.Windows.Forms.MainMenu mainMenu1;

		public PUCFrame()
		{
			//
			// Required for Windows Form Designer support
			//
			InitializeComponent();

			Debug.Assert( DEFAULT_FRAME == null, "PUCFrame should only be instantiated once." );
			PUCFrame.DEFAULT_FRAME = this;

			_measureCtl = new MeasureStringControl();
			this.Controls.Add( _measureCtl );
			_measureCtl.Location = new Point( 0, 0 );
			_measureCtl.Size = new Size( 0, 0 );
			_measureCtl.Enabled = false;

			Globals.Init( VERSION_STRING, "\\Storage\\PUC\\pucdata.xml", 
					      this, this, this, this, _measureCtl, 
                          "\\Storage\\PUC\\smartphone.xml" );

			_uiEventQueue = new Queue();

			_appliances = new ArrayList();
			_servers = new ArrayList();

			this.Text = Globals.GetVersionString();
			AddLogText( Globals.GetVersionString() + " Starting..." );

			this.logPanel.Size = this.ClientSize;
			this.logBox.Size = new System.Drawing.Size( this.logPanel.Size.Width, 
				this.logPanel.Size.Height - 
				this.logBox.Location.Y );

			_connectDialog = new PhoneConnectDlg( this );
			_optionsDialog = new PhoneOptionsDlg();

			_leftMenuStack = new Stack();

			LeftMenuStackItem item = 
				new LeftMenuStackItem( "Open", new EventHandler(this.openItem_Click) );
			this.PushLeftMenuStack( item );

			_mutex = new Mutex();
			
			SetupRecentList();

			SetupRulePhases();
			
			_registry = WidgetRegistryParser.Parse( Globals.GetWidgetRegistryFileName() );

			SetupSmartCIOManager();

			AddLogLine( "Done." );
		}

		protected void SetupRecentList()
		{
			DataStore store = Globals.GetDataStore();

			int count = store.GetIntData( Globals.RECENT_COUNT_ATTR );

			MenuItem item;

			if ( count <= 0 )
			{
				if ( _recentMenuItems )
				{
					recentMenu.MenuItems.Clear();

					item = new MenuItem();
					item.Text = "None";
					item.Enabled = false;
					recentMenu.MenuItems.Add( item );
				}

				return;
			}

			_recentMenuItems = true;
			recentMenu.MenuItems.Clear();

			for( int i = 0; i < count; i++ )
			{
				string server = store.GetStringData( Globals.RECENT_SERVER_PRFX + i );

				item = new MenuItem();
				item.Text = server;

				item.Click += new EventHandler(this.recentItem_Click);

				recentMenu.MenuItems.Add( item );
			}
		}

		protected void SetupRulePhases()
		{
			_rulePhases = new ArrayList( 5 );

			/*
			 * Phase 1: Scan the specification for units and also any  
			 * information that may be helpful for determining structure.
			 */
			SpecScanPhase phaseOne = new SpecScanPhase();
		
			phaseOne.AddRule( new ListFindingRule() );
			phaseOne.AddRule( new UnitFindingRule() );
			phaseOne.AddRule( new MutualExclusionRule() );

			_rulePhases.Add( phaseOne );

			/*
			 * Phase 2: Scan the specification again and assign CIOs to
			 * each appliance object.  These will used in later phases to
			 * populate the lists and make decisions about list structure.
			 */

			UnitScanPhase phaseTwo = new UnitScanPhase();

			phaseTwo.AddRule( new ListUnitFinder() );
			phaseTwo.AddRule( new ObjectUnitFinder() );

			_rulePhases.Add( phaseTwo );

			/*
			 * Phase 3: Build an intermediate list structure that can be
			 * easily modified by later phases.  This structure is tightly
			 * bound to the appliance structure.
			 */
			BuildListPhase phaseThree = new BuildListPhase();

			phaseThree.AddRule( new AssignListNodeRule() );
			phaseThree.AddRule( new MutualExclusionSubListRule() );
			phaseThree.AddRule( new NamedGroupSubListRule() );
			phaseThree.AddRule( new ObjectsInListRule() );

			_rulePhases.Add( phaseThree );

			/*
			 * Phase 4: Build the concrete list structure from the 
			 * intermediate structure that has been determined so far.
			 */
			ListManipulatePhase phaseFour = new ListManipulatePhase();

			phaseFour.AddRule( new CombinePanelNodesRule() );
			phaseFour.AddRule( new NoSingleListNodesRule() );

			_rulePhases.Add( phaseFour );

			/*
			 * Phase 5: Build the concrete list structure from the 
			 * intermediate structure that has been determined so far.
			 */
			BuildConcreteListPhase phaseFive = new BuildConcreteListPhase();

			phaseFive.AddRule( new MakeValueSubListRule() );
			phaseFive.AddRule( new MakeSubListRule() );
			phaseFive.AddRule( new MakePanelItemRule() );
			phaseFive.AddRule( new MakeListItemRule() );
			phaseFive.AddRule( new PlacePanelItemsRule() );

			_rulePhases.Add( phaseFive );
		}

		protected void SetupSmartCIOManager() 
		{
			_smartCIOManager = new PUC.CIO.SmartCIOManager();

			// time-duration: TimeDurationSmartCIO
			_smartCIOManager.AddSmartCIO( 
				"time-duration", 
				new SmartCIO.CreateSmartCIO(PUC.CIO.TimeDuration.TimeDurationSmartCIO.CreateTimeDurationSmartCIO) 
				);

			/*
			// media-controls: MediaControlsSmartCIO
			_smartCIOManager.AddSmartCIO(
				"media-controls",
				new SmartCIO.CreateSmartCIO(PUC.CIO.MediaControls.MediaControlsSmartCIO.CreateMediaControlsSmartCIO)
				);*/
		}

		/// <summary>
		/// Clean up any resources being used.
		/// </summary>
		protected override void Dispose( bool disposing )
		{
			base.Dispose( disposing );
		}
		#region Windows Form Designer generated code
		/// <summary>
		/// Required method for Designer support - do not modify
		/// the contents of this method with the code editor.
		/// </summary>
		private void InitializeComponent()
		{
			this.mainMenu1 = new System.Windows.Forms.MainMenu();
			this.actionItem = new System.Windows.Forms.MenuItem();
			this.pucMenu = new System.Windows.Forms.MenuItem();
			this.openItem = new System.Windows.Forms.MenuItem();
			this.menuItem4 = new System.Windows.Forms.MenuItem();
			this.logItem = new System.Windows.Forms.MenuItem();
			this.optionsItem = new System.Windows.Forms.MenuItem();
			this.menuItem6 = new System.Windows.Forms.MenuItem();
			this.recentMenu = new System.Windows.Forms.MenuItem();
			this.menuItem10 = new System.Windows.Forms.MenuItem();
			this.serverMenu = new System.Windows.Forms.MenuItem();
			this.menuItem11 = new System.Windows.Forms.MenuItem();
			this.deviceMenu = new System.Windows.Forms.MenuItem();
			this.disconnectItem = new System.Windows.Forms.MenuItem();
			this.logPanel = new System.Windows.Forms.Panel();
			this.logBox = new System.Windows.Forms.TextBox();
			this.label1 = new System.Windows.Forms.Label();
			this.eventQueueTimer = new System.Windows.Forms.Timer();
			// 
			// mainMenu1
			// 
			this.mainMenu1.MenuItems.Add(this.actionItem);
			this.mainMenu1.MenuItems.Add(this.pucMenu);
			// 
			// actionItem
			// 
			this.actionItem.Text = "Open";
			this.actionItem.Click += new System.EventHandler(this.actionItem_Click);
			// 
			// pucMenu
			// 
			this.pucMenu.MenuItems.Add(this.openItem);
			this.pucMenu.MenuItems.Add(this.menuItem4);
			this.pucMenu.MenuItems.Add(this.logItem);
			this.pucMenu.MenuItems.Add(this.optionsItem);
			this.pucMenu.MenuItems.Add(this.menuItem6);
			this.pucMenu.MenuItems.Add(this.recentMenu);
			this.pucMenu.MenuItems.Add(this.serverMenu);
			this.pucMenu.MenuItems.Add(this.deviceMenu);
			this.pucMenu.Text = "Menu";
			// 
			// openItem
			// 
			this.openItem.Text = "Open";
			this.openItem.Click += new System.EventHandler(this.openItem_Click);
			// 
			// menuItem4
			// 
			this.menuItem4.Text = "-";
			// 
			// logItem
			// 
			this.logItem.Checked = true;
			this.logItem.Text = "Logging Pane";
			this.logItem.Click += new System.EventHandler(this.logItem_Click);
			// 
			// optionsItem
			// 
			this.optionsItem.Text = "Options...";
			this.optionsItem.Click += new System.EventHandler(this.optionsItem_Click);
			// 
			// menuItem6
			// 
			this.menuItem6.Text = "-";
			// 
			// recentMenu
			// 
			this.recentMenu.MenuItems.Add(this.menuItem10);
			this.recentMenu.Text = "Recent Servers";
			// 
			// menuItem10
			// 
			this.menuItem10.Enabled = false;
			this.menuItem10.Text = "None";
			// 
			// serverMenu
			// 
			this.serverMenu.MenuItems.Add(this.menuItem11);
			this.serverMenu.Text = "Current Servers";
			// 
			// menuItem11
			// 
			this.menuItem11.Enabled = false;
			this.menuItem11.Text = "None";
			// 
			// deviceMenu
			// 
			this.deviceMenu.MenuItems.Add(this.disconnectItem);
			this.deviceMenu.Text = "Devices";
			// 
			// disconnectItem
			// 
			this.disconnectItem.Enabled = false;
			this.disconnectItem.Text = "Disconnect";
			this.disconnectItem.Click += new System.EventHandler(this.disconnectItem_Click);
			// 
			// logPanel
			// 
			this.logPanel.Controls.Add(this.logBox);
			this.logPanel.Controls.Add(this.label1);
			this.logPanel.Size = new System.Drawing.Size(160, 184);
			// 
			// logBox
			// 
			this.logBox.Location = new System.Drawing.Point(0, 24);
			this.logBox.Multiline = true;
			this.logBox.Size = new System.Drawing.Size(160, 160);
			this.logBox.Text = "";
			// 
			// label1
			// 
			this.label1.Size = new System.Drawing.Size(160, 24);
			this.label1.Text = "PUC Log:";
			// 
			// eventQueueTimer
			// 
			this.eventQueueTimer.Enabled = true;
			this.eventQueueTimer.Tick += new System.EventHandler(this.eventQueueTimer_Tick);
			// 
			// PUCFrame
			// 
			this.Controls.Add(this.logPanel);
			this.Menu = this.mainMenu1;
			this.Text = "PUC v2.0";

		}
		#endregion

		/// <summary>
		/// The main entry point for the application.
		/// </summary>

		static void Main() 
		{
			Application.Run(new PUCFrame());
		}

		public void shutdown() 
		{
			Application.Exit();
		}

		public void AddLogLine( string text )
		{
			AddLogText( text  + "\r\n" );
		}

		public void AddLogText( string text )
		{
			// logBox.ReadOnly = false;
			string nstr = logBox.Text.Insert( logBox.TextLength, text );
			logBox.Text = nstr;
			logBox.SelectionStart = logBox.TextLength;
			logBox.ScrollToCaret();
			// logBox.ReadOnly = true;
		}

		private void recentItem_Click(object sender, System.EventArgs e)
		{
			IPAddress ipAddr = null;

			try
			{
				ipAddr = IPAddress.Parse( ((MenuItem)sender).Text );
			}
			catch( Exception )
			{
			}

			Globals.GetDefaultLog().AddLogLine( "Attempting to open server at " + ((MenuItem)sender).Text + "..." );

			ServerInfo server = Globals.DoesServerExist( ipAddr );

			if ( server != null )
				server.Reset( false );
			else
			{
				server = new ServerInfo( ipAddr, false );

				Globals.AddServer( server );
			}
		}
		
		public MenuItem GetServerMenu()
		{
			return serverMenu;
		}

		public void AddActiveAppliance( Appliance a )
		{
			_appliances.Add( a );

			DeviceSwitcher d = new DeviceSwitcher( a );
			a.GetMenuItem().Click += new EventHandler(d.DeviceMenuClicked);
			deviceMenu.MenuItems.Add( a.GetMenuItem() );

			a.SetUIGenerator( new PUC.UIGeneration.UIGenerator( _rulePhases, _smartCIOManager, _registry ) );

			a.GetUIGenerator().Size = this.ClientSize;
			a.GetUIGenerator().Location = new System.Drawing.Point( 0, 0 );
			// a.GetUIGenerator().Menu = this.Menu;
			// a.GetUIGenerator().Text = a.GetName();

			a.GetUIGenerator().GenerateUI( a );

			SetCurrentAppliance ( a );
		}

		public void SetCurrentAppliance( Appliance a )
		{
			if ( _currentAppliance != null )
			{
				Controls.Remove( _currentAppliance.GetUIGenerator() );
				_currentAppliance.GetMenuItem().Checked = false;
			}
			else
				disconnectItem.Enabled = true;

			HideLogPanel();
			_currentAppliance = a;
			_currentAppliance.GetMenuItem().Checked = true;
			Controls.Add( _currentAppliance.GetUIGenerator() );
			_currentAppliance.GetUIGenerator().Visible = true;
			_currentAppliance.GetUIGenerator().BringToFront();
			_currentAppliance.GetUIGenerator().Focus();
			this.Text = Globals.GetVersionString() + " - " + a.Name;

			setActionItemName();
		}
		
		public ServerInfo DoesServerExist( IPAddress ip )
		{
			IEnumerator e = _servers.GetEnumerator();
			while( e.MoveNext() )
			{
				ServerInfo s = (ServerInfo)e.Current;
				if ( s.GetIPAddress().Equals( ip ) )
					return s;
			}

			return null;
		}

		public void AddServer( ServerInfo s ) 
		{
			if ( _servers.Count == 0 )
				serverMenu.MenuItems.Clear();

			_servers.Add( s );

			string serverString = s.GetIPAddress().ToString();

			DataStore store = Globals.GetDataStore();

			int count = store.GetIntData( Globals.RECENT_COUNT_ATTR );
			int max = store.GetIntData( Globals.RECENT_SERVER_ATTR );

			store.BeginSet();

			string[] servers = new string[ count ];
			for( int i = 0; i < count; i++ )
				servers[ i ] = store.GetStringData( Globals.RECENT_SERVER_PRFX + i );

			int idx = 0;
			for( ; idx < count; idx++ )
			{
				if ( serverString == servers[ idx ] )
					break;
			}

			for( int i = idx; i > 0; i-- )
			{
				string lastServer = store.GetStringData( Globals.RECENT_SERVER_PRFX + ( i - 1 ) );
				store.Set( Globals.RECENT_SERVER_PRFX + i, lastServer );
			}

			store.Set( Globals.RECENT_SERVER_PRFX + 0, serverString );

			if ( count < max && idx == count )
				store.Set( Globals.RECENT_COUNT_ATTR, count + 1 );

			store.CommitSet();

			SetupRecentList();
		}

		public void AddEventCallback( IEventDispatcher ed )
		{
			_mutex.WaitOne();

			_uiEventQueue.Enqueue( ed );

			_mutex.ReleaseMutex();
		}

		public void HideLogPanel()
		{
			logItem.Checked = logPanel.Visible = false;
			if ( _currentAppliance != null )
			{
				this.Text = Globals.GetVersionString() + " - " + _currentAppliance.Name;
				Controls.Add( _currentAppliance.GetUIGenerator() );
				_currentAppliance.GetUIGenerator().Focus();
			}
			setActionItemName();
		}

		public void ShowLogPanel()
		{
			// Controls.Add( logPanel );
			logItem.Checked = logPanel.Visible = true;
			this.Text = Globals.GetVersionString();

			if ( _currentAppliance != null )
				Controls.Remove( _currentAppliance.GetUIGenerator() );

			setActionItemName();

			logPanel.BringToFront();
			logBox.SelectionStart = logBox.TextLength;
			logBox.ScrollToCaret();
			logPanel.Focus();
			logBox.Focus();
			this.BringToFront();
		}

		public SizeF MeasureString( string str, Font f )
		{
			return _measureCtl.MeasureString( str, f );
		}

		private void logItem_Click(object sender, System.EventArgs e)
		{
			if ( logPanel.Visible )
				HideLogPanel();
			else
				ShowLogPanel();

			logItem.Checked = logPanel.Visible;		
		}

		private void openItem_Click(object sender, System.EventArgs e)
		{
			_connectDialog.ShowDialog();
		}

		private void optionsItem_Click(object sender, System.EventArgs e)
		{
			_optionsDialog.ShowDialog();
		}

		private void eventQueueTimer_Tick(object sender, System.EventArgs e)
		{
			while( _uiEventQueue.Count > 0 ) 
				((IEventDispatcher)_uiEventQueue.Dequeue()).Dispatch();
		}

		public void RemoveAppliance( Appliance a )
		{
			_appliances.Remove( a );
			if ( _appliances.Count > 0 )
				SetCurrentAppliance( (Appliance)_appliances[ 0 ] );
			else
			{
				_currentAppliance = null;
				this.Controls.Remove( a.GetUIGenerator() );
				disconnectItem.Enabled = false;
				ShowLogPanel();
			}

			deviceMenu.MenuItems.Remove( a.GetMenuItem() );
		}

		private void disconnectItem_Click(object sender, System.EventArgs e)
		{
			Appliance a = _currentAppliance;
			RemoveAppliance( a );
			a.GetServer().Unload( a );
		}

		public void PushLeftMenuStack( LeftMenuStackItem newItem )
		{
			Stack stack = null;
			if ( logPanel.Visible == true || _currentAppliance == null )
				stack = _leftMenuStack;
			else
				stack = _currentAppliance.LeftMenuStack;

			LeftMenuStackItem oldItem = null;
			if ( stack.Count > 0 )
				 oldItem = (LeftMenuStackItem)stack.Peek();

			actionItem.Text = newItem.Name;

			stack.Push( newItem );
		}

		public void PopLeftMenuStack()
		{
			Stack stack = null;
			if ( logPanel.Visible == true || _currentAppliance == null )
				stack = _leftMenuStack;
			else
				stack = _currentAppliance.LeftMenuStack;

			LeftMenuStackItem oldItem = (LeftMenuStackItem)stack.Pop();
			LeftMenuStackItem newItem = (LeftMenuStackItem)stack.Peek();

			actionItem.Text = newItem.Name;
		}

		protected void setActionItemName()
		{
			Stack stack = null;
			if ( logPanel.Visible == true || _currentAppliance == null )
				stack = _leftMenuStack;
			else
				stack = _currentAppliance.LeftMenuStack;

			LeftMenuStackItem currentItem = (LeftMenuStackItem)stack.Peek();
			actionItem.Text = currentItem.Name;
		}

		private void actionItem_Click(object sender, System.EventArgs e)
		{
			Stack stack = null;
			if ( logPanel.Visible == true || _currentAppliance == null )
				stack = _leftMenuStack;
			else
				stack = _currentAppliance.LeftMenuStack;

			LeftMenuStackItem currentItem = (LeftMenuStackItem)stack.Peek();

			currentItem.Handler( sender, e );
		}
	}

	public class LeftMenuStackItem
	{
		/*
		 * Member Variables
		 */

		protected string _name;
		protected EventHandler _handler;


		/*
		 * Constructor
		 */

		public LeftMenuStackItem( string name, EventHandler handler )
		{
			_name = name;
			_handler = handler;
		}


		/*
		 * Properties
		 */

		public string Name
		{
			get
			{
				return _name;
			}
		}

		public EventHandler Handler
		{
			get
			{
				return _handler;
			}
		}
	}
}
