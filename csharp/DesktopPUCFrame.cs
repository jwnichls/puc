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

using PUC;
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


namespace DesktopPUC
{
	/// <summary>
	/// Summary description for Form1.
	/// </summary>
	public class PUCFrame : System.Windows.Forms.Form, IPUCFrame, ICallbackManager, ILogManager, IShutdown, IServerManager
	{
		public const string VERSION_STRING = "Desktop PUC v3.0";

		public static IPUCFrame DEFAULT_FRAME;

		public  DesktopConnectDlg    _connectDialog;
		public  DesktopOptionsDialog _optionsDialog;

		private System.Windows.Forms.Timer eventQueueTimer;
		private System.ComponentModel.IContainer components;

		private ArrayList _appliances;
		private Appliance _currentAppliance;
		private ArrayList _servers;
		private Queue     _uiEventQueue;

		private Mutex     _mutex;

		private System.Windows.Forms.Panel logPanel;
		private System.Windows.Forms.Label logLabel;
		private System.Windows.Forms.TextBox logBox;
		private System.Windows.Forms.MainMenu mainMenu1;
		private System.Windows.Forms.MenuItem menuItem5;
		private System.Windows.Forms.MenuItem menuItem6;
		private System.Windows.Forms.MenuItem menuItem11;
		private System.Windows.Forms.MenuItem pucMenu;
		private System.Windows.Forms.MenuItem openItem;
		private System.Windows.Forms.MenuItem logItem;
		private System.Windows.Forms.MenuItem exitItem;
		private System.Windows.Forms.MenuItem serverMenu;
		private System.Windows.Forms.MenuItem deviceMenu;
		private System.Windows.Forms.MenuItem disconnectItem;
		private System.Windows.Forms.MenuItem optionsItem;
		
		protected MeasureStringControl _measureCtl;
		private System.Windows.Forms.MenuItem menuItem1;
		private System.Windows.Forms.MenuItem recentMenu;
		private System.Windows.Forms.MenuItem menuItem3;

		protected bool _recentMenuItems = false;

		protected ArrayList			_rulePhases;
		protected SmartCIOManager	_smartCIOManager;
		protected WidgetRegistry	_registry;

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

			Globals.Init( VERSION_STRING, ".\\pucdata.xml", this, this, this, this, _measureCtl, "pocketpc.xml" );

			_uiEventQueue = new Queue();

			_appliances = new ArrayList();
			_servers = new ArrayList();

			this.Text = Globals.GetVersionString();
			AddLogText( Globals.GetVersionString() + " Starting..." );

			this.logPanel.Size = this.ClientSize;
			this.logBox.Size = new System.Drawing.Size( this.logPanel.Size.Width, 
				this.logPanel.Size.Height - 
				this.logBox.Location.Y );

			_connectDialog = new DesktopConnectDlg( this );
			_optionsDialog = new DesktopOptionsDialog();

			_mutex = new Mutex();
			
			this.Closing += new CancelEventHandler(PUCFrame_Closing);

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
			_rulePhases = new ArrayList( 2 );

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

			// TODO: Re-write this phase to use rules...  Currently the
			// phase object does all the work.

			_rulePhases.Add( phaseFive );

			/*
			 * Phase 6: Attempt to fix layout problems in the interface tree.
			 */

			FixLayoutPhase phaseSix = new FixLayoutPhase();

			phaseSix.AddRule( new FixLayoutWithTabsRule() );

			_rulePhases.Add( phaseSix );

			/*
			 * Phase 7: Enabling auto-scrolling for panels.
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

			// image-list: ImageListSmartCIO
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
			this.components = new System.ComponentModel.Container();
			this.eventQueueTimer = new System.Windows.Forms.Timer(this.components);
			this.logPanel = new System.Windows.Forms.Panel();
			this.logBox = new System.Windows.Forms.TextBox();
			this.logLabel = new System.Windows.Forms.Label();
			this.mainMenu1 = new System.Windows.Forms.MainMenu();
			this.pucMenu = new System.Windows.Forms.MenuItem();
			this.openItem = new System.Windows.Forms.MenuItem();
			this.menuItem6 = new System.Windows.Forms.MenuItem();
			this.logItem = new System.Windows.Forms.MenuItem();
			this.optionsItem = new System.Windows.Forms.MenuItem();
			this.menuItem1 = new System.Windows.Forms.MenuItem();
			this.recentMenu = new System.Windows.Forms.MenuItem();
			this.menuItem3 = new System.Windows.Forms.MenuItem();
			this.menuItem5 = new System.Windows.Forms.MenuItem();
			this.exitItem = new System.Windows.Forms.MenuItem();
			this.serverMenu = new System.Windows.Forms.MenuItem();
			this.deviceMenu = new System.Windows.Forms.MenuItem();
			this.disconnectItem = new System.Windows.Forms.MenuItem();
			this.menuItem11 = new System.Windows.Forms.MenuItem();
			this.logPanel.SuspendLayout();
			this.SuspendLayout();
			// 
			// eventQueueTimer
			// 
			this.eventQueueTimer.Enabled = true;
			this.eventQueueTimer.Tick += new System.EventHandler(this.eventQueueTimer_Tick);
			// 
			// logPanel
			// 
			this.logPanel.Controls.Add(this.logBox);
			this.logPanel.Controls.Add(this.logLabel);
			this.logPanel.Location = new System.Drawing.Point(0, 0);
			this.logPanel.Name = "logPanel";
			this.logPanel.Size = new System.Drawing.Size(384, 232);
			this.logPanel.TabIndex = 0;
			// 
			// logBox
			// 
			this.logBox.Location = new System.Drawing.Point(0, 24);
			this.logBox.Multiline = true;
			this.logBox.Name = "logBox";
			this.logBox.ScrollBars = System.Windows.Forms.ScrollBars.Vertical;
			this.logBox.Size = new System.Drawing.Size(384, 208);
			this.logBox.TabIndex = 1;
			this.logBox.Text = "";
			// 
			// logLabel
			// 
			this.logLabel.Location = new System.Drawing.Point(3, 6);
			this.logLabel.Name = "logLabel";
			this.logLabel.Size = new System.Drawing.Size(64, 16);
			this.logLabel.TabIndex = 0;
			this.logLabel.Text = "PUC Log:";
			// 
			// mainMenu1
			// 
			this.mainMenu1.MenuItems.AddRange(new System.Windows.Forms.MenuItem[] {
																					  this.pucMenu,
																					  this.serverMenu,
																					  this.deviceMenu});
			// 
			// pucMenu
			// 
			this.pucMenu.Index = 0;
			this.pucMenu.MenuItems.AddRange(new System.Windows.Forms.MenuItem[] {
																					this.openItem,
																					this.menuItem6,
																					this.logItem,
																					this.optionsItem,
																					this.menuItem1,
																					this.recentMenu,
																					this.menuItem5,
																					this.exitItem});
			this.pucMenu.Text = "PUC";
			// 
			// openItem
			// 
			this.openItem.Index = 0;
			this.openItem.Text = "Open Server...";
			this.openItem.Click += new System.EventHandler(this.openItem_Click);
			// 
			// menuItem6
			// 
			this.menuItem6.Index = 1;
			this.menuItem6.Text = "-";
			// 
			// logItem
			// 
			this.logItem.Checked = true;
			this.logItem.Index = 2;
			this.logItem.Text = "Logging Pane";
			this.logItem.Click += new System.EventHandler(this.logItem_Click);
			// 
			// optionsItem
			// 
			this.optionsItem.Index = 3;
			this.optionsItem.Text = "Options...";
			this.optionsItem.Click += new System.EventHandler(this.optionsItem_Click);
			// 
			// menuItem1
			// 
			this.menuItem1.Index = 4;
			this.menuItem1.Text = "-";
			// 
			// recentMenu
			// 
			this.recentMenu.Index = 5;
			this.recentMenu.MenuItems.AddRange(new System.Windows.Forms.MenuItem[] {
																					   this.menuItem3});
			this.recentMenu.Text = "Recent Servers";
			// 
			// menuItem3
			// 
			this.menuItem3.Enabled = false;
			this.menuItem3.Index = 0;
			this.menuItem3.Text = "None";
			// 
			// menuItem5
			// 
			this.menuItem5.Index = 6;
			this.menuItem5.Text = "-";
			// 
			// exitItem
			// 
			this.exitItem.Index = 7;
			this.exitItem.Text = "Exit";
			this.exitItem.Click += new System.EventHandler(this.exitItem_Click);
			// 
			// serverMenu
			// 
			this.serverMenu.Index = 1;
			this.serverMenu.Text = "Servers";
			// 
			// deviceMenu
			// 
			this.deviceMenu.Index = 2;
			this.deviceMenu.MenuItems.AddRange(new System.Windows.Forms.MenuItem[] {
																					   this.disconnectItem,
																					   this.menuItem11});
			this.deviceMenu.Text = "Devices";
			// 
			// disconnectItem
			// 
			this.disconnectItem.Enabled = false;
			this.disconnectItem.Index = 0;
			this.disconnectItem.Text = "Disconnect";
			this.disconnectItem.Click += new System.EventHandler(this.disconnectItem_Click);
			// 
			// menuItem11
			// 
			this.menuItem11.Index = 1;
			this.menuItem11.Text = "-";
			// 
			// PUCFrame
			// 
			this.AutoScaleBaseSize = new System.Drawing.Size(5, 13);
			this.ClientSize = new System.Drawing.Size(416, 249);
			this.Controls.Add(this.logPanel);
			this.Menu = this.mainMenu1;
			this.Name = "PUCFrame";
			this.Text = "PUC v2.0";
			this.Resize += new System.EventHandler(this.PUCFrame_Resize);
			this.Closing += new System.ComponentModel.CancelEventHandler(this.PUCFrame_Closing);
			this.logPanel.ResumeLayout(false);
			this.ResumeLayout(false);

		}
		#endregion

		/// <summary>
		/// The main entry point for the application.
		/// </summary>
		[STAThread]
		static void Main() 
		{
			Application.Run(new PUCFrame());
		}

		public void shutdown() 
		{
			Application.Exit();
			Process.GetCurrentProcess().Kill();
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

		public void AddActiveAppliance( Appliance a )
		{
			_appliances.Add( a );

			DeviceSwitcher d = new DeviceSwitcher( a );
			a.GetMenuItem().Click += new EventHandler(d.DeviceMenuClicked);
			deviceMenu.MenuItems.Add( a.GetMenuItem() );

			a.SetUIGenerator( new PUC.UIGeneration.UIGenerator( _rulePhases,
																_smartCIOManager,
																_registry ) );

			SetCurrentAppliance ( a );

			a.GetUIGenerator().Size = this.ClientSize;
			a.GetUIGenerator().Location = new System.Drawing.Point( 0, 0 );

			a.GetUIGenerator().GenerateUI( a );
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
			Globals.GetFrame( a ).AddLogLine( "Disconnected from " + a.Name );
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
			_mutex.WaitOne();

			_uiEventQueue.Enqueue( ed );

			_mutex.ReleaseMutex();
		}

		private void eventQueueTimer_Tick(object sender, System.EventArgs e)
		{
			_mutex.WaitOne();

			while( _uiEventQueue.Count > 0 ) 
				((IEventDispatcher)_uiEventQueue.Dequeue()).Dispatch();

			_mutex.ReleaseMutex();
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

		private void exitItem_Click(object sender, System.EventArgs e)
		{
			shutdown();
		}

		private void PUCFrame_Closing(object sender, CancelEventArgs e)
		{
			shutdown();
		}

		private void optionsItem_Click(object sender, System.EventArgs e)
		{
			_optionsDialog.ShowDialog();
		}

		private void PUCFrame_Resize(object sender, System.EventArgs e)
		{
			if ( this.ClientSize.Width == 0 && this.ClientSize.Height == 0 )
				// this happens when the window is minimized
				return;

			this.logPanel.Size = this.ClientSize;
			this.logBox.Size = new System.Drawing.Size( this.logPanel.Size.Width, 
				this.logPanel.Size.Height - 
				this.logBox.Location.Y );
		
			IEnumerator en = _appliances.GetEnumerator();
			while( en.MoveNext() )
			{
				Appliance a = (Appliance)en.Current;
				UIGenerator ui = a.GetUIGenerator();
				InterfaceNode root = ui.InterfaceRoot;

				ui.Size = this.ClientSize;

				root.SetSize( ui.Size.Width, ui.Size.Height );
				root.DoLayout( ui.LayoutVars );
			}
		}
	}
}
