using System;
using System.Data;
using System.Diagnostics;
using System.Drawing;
using System.Collections;
using System.ComponentModel;
using System.Net;
using System.IO;
using System.Threading;
using System.Windows.Forms;
using System.Xml;

using Microsoft.WindowsCE.Forms;

using PUC.CIO;
using PUC.Communication;
using PUC.Parsers;
using PUC.PersistentData;
using PUC.Registry;
using PUC.Rules;
using PUC.Rules.BuildConcrete;
using PUC.Rules.FixLayout;
using PUC.Rules.GroupScan;
using PUC.Rules.SpecScan;
using PUC.Rules.TreeBuilding;
using PUC.Rules.TreeTraversal;
using PUC.Rules.UnitScan;
using PUC.UIGeneration;


namespace PUC
{
	/// <summary>
	/// Summary description for Form1.
	/// </summary>
	public class PUCFrame : System.Windows.Forms.Form, IPUCFrame, IShutdown, ICallbackManager, IServerManager
	{
		public const string VERSION_STRING = "PUC v3.0";

		public static IPUCFrame DEFAULT_FRAME;

		private ArrayList  _appliances;
		private Appliance  _currentAppliance;
		private ArrayList  _servers;
		private Queue      _uiEventQueue;
		private InputPanel _inputPanel;


		public  ConnectServerDlg _connectDialog;
		public  OptionsDialog	 _optionsDialog;

		private System.Windows.Forms.MainMenu mainMenu1;
		private System.Windows.Forms.MenuItem deviceMenu;
		private System.Windows.Forms.MenuItem pucMenu;
		private System.Windows.Forms.MenuItem openItem;
		private System.Windows.Forms.MenuItem serverMenu;
		private System.Windows.Forms.MenuItem disconnectItem;
		private System.Windows.Forms.MenuItem menuItem2;
		private System.Windows.Forms.Timer eventQueueTimer;
		private System.Windows.Forms.MenuItem menuItem1;
		private System.Windows.Forms.MenuItem logItem;
		private System.Windows.Forms.Panel logPanel;
		private System.Windows.Forms.TextBox logBox;
		private System.Windows.Forms.Label logLabel;

		protected MeasureStringControl _measureCtl;
		private System.Windows.Forms.MenuItem optionsItem;

		protected int _recentMenuStart = -1;

		protected ArrayList			_rulePhases;
		protected SmartCIOManager	_smartCIOManager;
		protected WidgetRegistry	_registry;

		/// <summary>
		/// Required designer variable.
		/// </summary>
		private System.ComponentModel.Container components = null;

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

			Globals.Init( VERSION_STRING, "\\Program Files\\PUC\\pucdata.xml", this, this, this, this, _measureCtl, "\\Program Files\\PUC\\pocketpc.xml" );

			_uiEventQueue = new Queue();

			_appliances = new ArrayList();
			_servers = new ArrayList();

			this.Text = Globals.GetVersionString();
			AddLogLine( Globals.GetVersionString() + " Starting..." );

			this.logPanel.Size = this.ClientSize;
			this.logBox.Size = new System.Drawing.Size( this.logPanel.Size.Width, 
													    this.logPanel.Size.Height - 
														this.logBox.Location.Y );

			_connectDialog = new ConnectServerDlg( this );
			_optionsDialog = new OptionsDialog();

			_inputPanel = new InputPanel();
			_inputPanel.EnabledChanged += new EventHandler(_inputPanel_EnabledChanged);

			SetupRecentList();

			SetupRulePhases();
			
			_registry = WidgetRegistryParser.Parse( Globals.GetWidgetRegistryFileName() );

			SetupSmartCIOManager();
		}

		protected void SetupRecentList()
		{
			DataStore store = Globals.GetDataStore();

			int count = store.GetIntData( Globals.RECENT_COUNT_ATTR );

			if ( _recentMenuStart >= 0 )
			{
				for( int i = _recentMenuStart; i < pucMenu.MenuItems.Count; )
					pucMenu.MenuItems.RemoveAt( i );

				_recentMenuStart = -1;
			}

			if ( count <= 0 ) return;

			_recentMenuStart = pucMenu.MenuItems.Count;

			MenuItem item = new MenuItem();
			item.Text = "-";

			pucMenu.MenuItems.Add( item );

			for( int i = 0; i < count; i++ )
			{
				string server = store.GetStringData( Globals.RECENT_SERVER_PRFX + i );

				item = new MenuItem();
				item.Text = server;

				item.Click += new EventHandler(this.recentItem_Click);

				pucMenu.MenuItems.Add( item );
			}
		}

		protected void SetupRulePhases()
		{
			_rulePhases = new ArrayList( 7 );

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
			 * Phase 2: Modify the specification based upon information
			 * found in the previous phase, and inferences made during this
			 * phase.
			 */

			GroupScanPhase phaseTwo = new GroupScanPhase();

			phaseTwo.AddRule( new NoPanelsRule() );
			phaseTwo.AddRule( new PowerPanelRule() );
			phaseTwo.AddRule( new TabbedPanelsRule() );
			phaseTwo.AddRule( new OverlappingPanelsRule() );

			_rulePhases.Add( phaseTwo );

			/*
			 * Phase 3: Scan throught the portions of the group tree
			 * not yet assigned to widgets.
			 */

			UnitScanPhase phaseThree = new UnitScanPhase();

			phaseThree.AddRule( new ListUnitFinder() );
			phaseThree.AddRule( new ObjectUnitFinder() );

			_rulePhases.Add( phaseThree );

			/*
			 * Phase 4: Build the interface tree from the group tree.  
			 * Assign rows to CIOs during this process.
			 */

			TreeBuildingPhase phaseFour = new TreeBuildingPhase();

			phaseFour.AddRule( new OrganizeTreeRule() );
			phaseFour.AddRule( new GetPanelRule() );
			phaseFour.AddRule( new LabeledTwoCompRowRule() );
			phaseFour.AddRule( new FullWidthRowRule() );
			phaseFour.AddRule( new OneColumnRowRule() );

			_rulePhases.Add( phaseFour );

			/*
			 * Phase 5: Build the concrete interface from the interface tree.
			 */

			BuildConcretePhase phaseFive = new BuildConcretePhase();

			_rulePhases.Add( phaseFive );

			/*
			 * Phase 6: Fix any layout problems that may have occurred.
			 */

			FixLayoutPhase phaseSix = new FixLayoutPhase();

			//phaseSix.AddRule( new FixLayoutWithTabsRule() );

			_rulePhases.Add( phaseSix );

			/*
			 * Phase 7: Enable scrolling for panel nodes
			 */

			TreeTraversalPhase phaseSeven = new TreeTraversalPhase();

			phaseSeven.AddRule( new TurnOnScrollingRule() );

			_rulePhases.Add( phaseSeven );
		}

		protected void SetupSmartCIOManager() 
		{
			_smartCIOManager = new PUC.CIO.SmartCIOManager();

			// time-duration: TimeDurationSmartCIO
			_smartCIOManager.AddSmartCIO( 
				"time-duration", 
				new SmartCIO.CreateSmartCIO(PUC.CIO.TimeDuration.TimeDurationSmartCIO.CreateTimeDurationSmartCIO) 
				);

			// media-controls: MediaControlsSmartCIO
			_smartCIOManager.AddSmartCIO(
				"media-controls",
				new SmartCIO.CreateSmartCIO(PUC.CIO.MediaControls.MediaControlsSmartCIO.CreateMediaControlsSmartCIO)
				);

			// dimmer: DimmerSmartCIO
			_smartCIOManager.AddSmartCIO(
				"dimmer",
				new SmartCIO.CreateSmartCIO(PUC.CIO.Dimmer.DimmerSmartCIO.CreateDimmerSmartCIO)
				);

			// image: ImageSmartCIO
			_smartCIOManager.AddSmartCIO(
				"image",
				new SmartCIO.CreateSmartCIO(PUC.CIO.Image.ImageSmartCIO.CreateImageSmartCIO)
				);

			// iamge-list: ImageListSmartCIO
			_smartCIOManager.AddSmartCIO(
				"image-list",
				new SmartCIO.CreateSmartCIO(PUC.CIO.ImageList.ImageListSmartCIO.CreateImageListSmartCIO)
				);
		}
		

		/// <summary>
		/// Clean up any resources being used.
		/// </summary>
		protected override void Dispose( bool disposing )
		{
			if( disposing )
			{
				if (components != null) 
				{
					components.Dispose();
				}
			}
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
			this.pucMenu = new System.Windows.Forms.MenuItem();
			this.openItem = new System.Windows.Forms.MenuItem();
			this.menuItem1 = new System.Windows.Forms.MenuItem();
			this.logItem = new System.Windows.Forms.MenuItem();
			this.optionsItem = new System.Windows.Forms.MenuItem();
			this.serverMenu = new System.Windows.Forms.MenuItem();
			this.deviceMenu = new System.Windows.Forms.MenuItem();
			this.disconnectItem = new System.Windows.Forms.MenuItem();
			this.menuItem2 = new System.Windows.Forms.MenuItem();
			this.eventQueueTimer = new System.Windows.Forms.Timer();
			this.logPanel = new System.Windows.Forms.Panel();
			this.logLabel = new System.Windows.Forms.Label();
			this.logBox = new System.Windows.Forms.TextBox();
			// 
			// mainMenu1
			// 
			this.mainMenu1.MenuItems.Add(this.pucMenu);
			this.mainMenu1.MenuItems.Add(this.serverMenu);
			this.mainMenu1.MenuItems.Add(this.deviceMenu);
			// 
			// pucMenu
			// 
			this.pucMenu.MenuItems.Add(this.openItem);
			this.pucMenu.MenuItems.Add(this.menuItem1);
			this.pucMenu.MenuItems.Add(this.logItem);
			this.pucMenu.MenuItems.Add(this.optionsItem);
			this.pucMenu.Text = "PUC";
			// 
			// openItem
			// 
			this.openItem.Text = "Open Server...";
			this.openItem.Click += new System.EventHandler(this.openItem_Click);
			// 
			// menuItem1
			// 
			this.menuItem1.Text = "-";
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
			// serverMenu
			// 
			this.serverMenu.Text = "Servers";
			// 
			// deviceMenu
			// 
			this.deviceMenu.MenuItems.Add(this.disconnectItem);
			this.deviceMenu.MenuItems.Add(this.menuItem2);
			this.deviceMenu.Text = "Devices";
			// 
			// disconnectItem
			// 
			this.disconnectItem.Enabled = false;
			this.disconnectItem.Text = "Disconnect";
			this.disconnectItem.Click += new System.EventHandler(this.disconnectItem_Click);
			// 
			// menuItem2
			// 
			this.menuItem2.Text = "-";
			// 
			// eventQueueTimer
			// 
			this.eventQueueTimer.Enabled = true;
			this.eventQueueTimer.Tick += new System.EventHandler(this.eventQueueTimer_Tick);
			// 
			// logPanel
			// 
			this.logPanel.Controls.Add(this.logLabel);
			this.logPanel.Controls.Add(this.logBox);
			this.logPanel.Size = new System.Drawing.Size(232, 264);
			// 
			// logLabel
			// 
			this.logLabel.Location = new System.Drawing.Point(6, 5);
			this.logLabel.Size = new System.Drawing.Size(168, 16);
			this.logLabel.Text = "PUC Log:";
			// 
			// logBox
			// 
			this.logBox.AcceptsReturn = true;
			this.logBox.AcceptsTab = true;
			this.logBox.Location = new System.Drawing.Point(0, 24);
			this.logBox.Multiline = true;
			this.logBox.ScrollBars = System.Windows.Forms.ScrollBars.Vertical;
			this.logBox.Size = new System.Drawing.Size(232, 240);
			this.logBox.Text = "";
			// 
			// PUCFrame
			// 
			this.ClientSize = new System.Drawing.Size(242, 272);
			this.Controls.Add(this.logPanel);
			this.Menu = this.mainMenu1;
			this.Text = "PUC v2.0";
			this.Resize += new System.EventHandler(this.PUCFrame_Resize);

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
			// do something?
			Application.Exit();
		}

		/// <summary>
		/// Opens a connection to a server and downloads a list of devices.
		/// </summary>
		/// <param name="sender">The source of this event.</param>
		/// <param name="e">The parameters of this event.</param>
		private void openItem_Click(object sender, System.EventArgs e)
		{
			_connectDialog.ShowDialog();
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

			a.SetUIGenerator( new UIGeneration.UIGenerator( _rulePhases, 
															_smartCIOManager, 
															_registry ) );

			a.GetUIGenerator().Size = this.Size;
			a.GetUIGenerator().Location = new System.Drawing.Point( 0, 0 );

			a.GetUIGenerator().GenerateUI( a );

			SetCurrentAppliance ( a );
		}

		public void SetCurrentAppliance( Appliance a )
		{
			if ( _currentAppliance != null )
			{
				this.Controls.Remove( _currentAppliance.GetUIGenerator() );
				_currentAppliance.GetMenuItem().Checked = false;
			}
			else
				disconnectItem.Enabled = true;

			_currentAppliance = a;
			_currentAppliance.GetMenuItem().Checked = true;
			this.Controls.Add( _currentAppliance.GetUIGenerator() );
			_currentAppliance.GetUIGenerator().BringToFront();
			HideLogPanel();
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
			_uiEventQueue.Enqueue( ed );
		}

		private void eventQueueTimer_Tick(object sender, System.EventArgs e)
		{
			while( _uiEventQueue.Count > 0 ) 
				((IEventDispatcher)_uiEventQueue.Dequeue()).Dispatch();
		}

		public void HideLogPanel()
		{
			logItem.Checked = logPanel.Visible = false;
		}

		public void ShowLogPanel()
		{
			logItem.Checked = logPanel.Visible = true;
			logPanel.BringToFront();
			logBox.Focus();
			logBox.SelectionStart = logBox.TextLength;
			logBox.ScrollToCaret();
		}

		private void logItem_Click(object sender, System.EventArgs e)
		{
			logItem.Checked = logPanel.Visible = !logItem.Checked;
			if ( logPanel.Visible )
				logPanel.BringToFront();
		}

		public SizeF MeasureString( string str, Font f )
		{
			return _measureCtl.MeasureString( str, f );
		}

		private void optionsItem_Click(object sender, System.EventArgs e)
		{
			_optionsDialog.ShowDialog();
		}

		private void PUCFrame_Resize(object sender, System.EventArgs e)
		{
			resizePanels();
		}

		private void _inputPanel_EnabledChanged(object sender, EventArgs e)
		{
			resizePanels();		
		}

		private void resizePanels() 
		{
			Size screenSize;

			if ( _inputPanel != null && _inputPanel.Enabled )
			{
				screenSize = new System.Drawing.Size(
					_inputPanel.VisibleDesktop.Width,
					_inputPanel.VisibleDesktop.Height );
			}
			else
			{
				screenSize = this.ClientSize;
			}

			this.logPanel.Size = screenSize;
			this.logBox.Size = new System.Drawing.Size( this.logPanel.Size.Width, 
				this.logPanel.Size.Height - 
				this.logBox.Location.Y );		

			IEnumerator en = _appliances.GetEnumerator();
			while( en.MoveNext() )
			{
				Appliance a = (Appliance)en.Current;
				UIGenerator ui = a.GetUIGenerator();
				InterfaceNode root = ui.InterfaceRoot;

				ui.Size = screenSize;

				root.SetSize( ui.Size.Width, ui.Size.Height );
				root.DoLayout( ui.LayoutVars );
			}
		}
	}
}
