using System;
using System.Collections;
using System.ComponentModel;
using System.Data;
using System.Diagnostics;
using System.Drawing;
using System.IO;
using System.Net;
using System.Threading;
using System.Windows.Forms;

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

using DesktopPUC;


namespace DebugServer
{
	/// <summary>
	/// Summary description for Form1.
	/// </summary>
	public class DebugServerFrame : System.Windows.Forms.Form, IPUCFrame, IDevice2
	{
		/*
		 * Constants
		 */


		/*
		 * Member Variables
		 */

		private Appliance			_appliance;

		protected string			_filename;
		protected string			_specification;
		protected DebugConnection	_debugConnection;

		protected bool				_running;
		protected int				_port;
		protected ArrayList			_connections;

		protected UndefineDlg		_undefineDialog;
        
		private System.Windows.Forms.MainMenu mainMenu;
		private System.Windows.Forms.Panel logPanel;
		private System.Windows.Forms.Label logLabel;
		private System.Windows.Forms.TextBox logBox;
		private System.Windows.Forms.MenuItem pucMenu;
		private System.Windows.Forms.MenuItem logItem;
		private System.ComponentModel.IContainer components;
		private System.Windows.Forms.MenuItem menuItem1;

		// UIGenerator variables
		private ArrayList		_rulePhases;
		private SmartCIOManager	_smartCIOManager;
		private System.Windows.Forms.MenuItem saveItem;
		private System.Windows.Forms.MenuItem undefineItem;
		private System.Windows.Forms.MenuItem restoreItem;
		private System.Windows.Forms.MenuItem sendAlertItem;
		private WidgetRegistry	_widgetRegistry;


		/*
		 * Constructor
		 */

		public DebugServerFrame( String filename, int port )
		{
			//
			// Required for Windows Form Designer support
			//
			InitializeComponent();

			_filename = filename;
			_port = port;

			_appliance = null;
			_debugConnection = new DebugConnection( this );
			_running = false;
			_connections = new ArrayList();

			this.logPanel.Size = this.ClientSize;
			this.logBox.Size = new System.Drawing.Size( this.logPanel.Size.Width, 
				this.logPanel.Size.Height - 
				this.logBox.Location.Y );

			_undefineDialog = new UndefineDlg( this );

			SetupRulePhases();

			_widgetRegistry = WidgetRegistryParser.Parse( Globals.GetWidgetRegistryFileName() );

			SetupSmartCIOManager();

			AddLogLine( "Parsing specification from " + filename + "..." );

			OpenSpec( filename );

			AddLogLine( "Done." );
		}

		protected void SetupRulePhases()
		{
			_rulePhases = new ArrayList();
			_rulePhases.Capacity = 7;

			/*
			 * Phase 1: Scan the specification for units and also any  
			 * information that may be helpful for determining structure.
			 */
			SpecScanPhase phaseOne = new SpecScanPhase();
			
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

			// this phase is only here to convert data for the following phase

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

			//
			// TODO: As SmartCIOs are implemented, they should be
			// added to the SmartCIOManager here.
			//
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
			this.mainMenu = new System.Windows.Forms.MainMenu();
			this.pucMenu = new System.Windows.Forms.MenuItem();
			this.saveItem = new System.Windows.Forms.MenuItem();
			this.undefineItem = new System.Windows.Forms.MenuItem();
			this.restoreItem = new System.Windows.Forms.MenuItem();
			this.sendAlertItem = new System.Windows.Forms.MenuItem();
			this.menuItem1 = new System.Windows.Forms.MenuItem();
			this.logItem = new System.Windows.Forms.MenuItem();
			this.logPanel = new System.Windows.Forms.Panel();
			this.logBox = new System.Windows.Forms.TextBox();
			this.logLabel = new System.Windows.Forms.Label();
			this.logPanel.SuspendLayout();
			this.SuspendLayout();
			// 
			// mainMenu
			// 
			this.mainMenu.MenuItems.AddRange(new System.Windows.Forms.MenuItem[] {
																					 this.pucMenu});
			// 
			// pucMenu
			// 
			this.pucMenu.Index = 0;
			this.pucMenu.MenuItems.AddRange(new System.Windows.Forms.MenuItem[] {
																					this.saveItem,
																					this.undefineItem,
																					this.restoreItem,
																					this.sendAlertItem,
																					this.menuItem1,
																					this.logItem});
			this.pucMenu.Text = "Appliance";
			// 
			// saveItem
			// 
			this.saveItem.Enabled = false;
			this.saveItem.Index = 0;
			this.saveItem.Text = "Save State...";
			this.saveItem.Click += new System.EventHandler(this.saveItem_Click);
			// 
			// undefineItem
			// 
			this.undefineItem.Enabled = false;
			this.undefineItem.Index = 1;
			this.undefineItem.Text = "Undefine Variables...";
			this.undefineItem.Click += new System.EventHandler(this.undefItem_Click);
			// 
			// restoreItem
			// 
			this.restoreItem.Enabled = false;
			this.restoreItem.Index = 2;
			this.restoreItem.Text = "Restore State...";
			this.restoreItem.Click += new System.EventHandler(this.restoreItem_Click);
			// 
			// sendAlertItem
			// 
			this.sendAlertItem.Enabled = false;
			this.sendAlertItem.Index = 3;
			this.sendAlertItem.Text = "Send Alert Msg...";
			this.sendAlertItem.Click += new System.EventHandler(this.alertMsgItem_Click);
			// 
			// menuItem1
			// 
			this.menuItem1.Index = 4;
			this.menuItem1.Text = "-";
			// 
			// logItem
			// 
			this.logItem.Checked = true;
			this.logItem.Index = 5;
			this.logItem.Text = "Logging Pane";
			this.logItem.Click += new System.EventHandler(this.logItem_Click);
			// 
			// logPanel
			// 
			this.logPanel.Controls.Add(this.logBox);
			this.logPanel.Controls.Add(this.logLabel);
			this.logPanel.Location = new System.Drawing.Point(0, 0);
			this.logPanel.Name = "logPanel";
			this.logPanel.Size = new System.Drawing.Size(360, 264);
			this.logPanel.TabIndex = 0;
			// 
			// logBox
			// 
			this.logBox.Location = new System.Drawing.Point(0, 24);
			this.logBox.Multiline = true;
			this.logBox.Name = "logBox";
			this.logBox.ScrollBars = System.Windows.Forms.ScrollBars.Vertical;
			this.logBox.Size = new System.Drawing.Size(352, 232);
			this.logBox.TabIndex = 1;
			this.logBox.Text = "";
			// 
			// logLabel
			// 
			this.logLabel.Location = new System.Drawing.Point(2, 4);
			this.logLabel.Name = "logLabel";
			this.logLabel.Size = new System.Drawing.Size(104, 16);
			this.logLabel.TabIndex = 0;
			this.logLabel.Text = "Debug Server Log:";
			// 
			// DebugServerFrame
			// 
			this.AutoScaleBaseSize = new System.Drawing.Size(5, 13);
			this.ClientSize = new System.Drawing.Size(384, 270);
			this.Controls.Add(this.logPanel);
			this.Menu = this.mainMenu;
			this.Name = "DebugServerFrame";
			this.Text = "DebugServer";
			this.Resize += new System.EventHandler(this.DebugServerFrame_Resize);
			this.Closing += new System.ComponentModel.CancelEventHandler(this.DebugServerFrame_Closing);
			this.logPanel.ResumeLayout(false);
			this.ResumeLayout(false);

		}
		#endregion

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
			return null;
		}

		public void AddActiveAppliance( Appliance a )
		{
			// do nothing for now (don't think this is needed for debug server)
			Debug.Assert(true,"AddActiveAppliance was called");
		}

		public void SetCurrentAppliance( Appliance a )
		{
			// do nothing for now (don't think this is needed for debug server)
			Debug.Assert(true,"SetCurrentAppliance was called");
		}

		public void AddServer( ServerInfo s ) 
		{
			// do nothing
			Debug.Assert(true,"AddServer was called");
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

		protected void ReadSpecFromFile( string filename )
		{
			StreamReader read = new StreamReader( filename );

			_specification = read.ReadToEnd();

			read.Close();
		}

		protected void OpenSpec( string filename )
		{
			ReadSpecFromFile( filename );

			TextReader read = new StringReader( _specification );
			_appliance = new Appliance( _debugConnection );
			Globals.AddFrameMapping( _appliance, this );

			try 
			{
				PUC.Parsers.SpecParser.Parse( read, _appliance );

				_debugConnection.MessageReceivedEvent += new Connection.MessageReceivedHandler(_appliance.MessageReceived);
				_debugConnection.ConnectionRegainedEvent += new Connection.ConnectionRegainedHandler(_appliance.ConnectionRegained);
				this.AddConnection( _debugConnection );

				_appliance.SetUIGenerator( new PUC.UIGeneration.UIGenerator( _rulePhases,
																			 _smartCIOManager,
																			 _widgetRegistry ) );

				_appliance.GetUIGenerator().Size = this.ClientSize;
				_appliance.GetUIGenerator().Location = new System.Drawing.Point( 0, 0 );

				_appliance.GetUIGenerator().GenerateUI( _appliance );

				this.Controls.Add( _appliance.GetUIGenerator() );

				this.Text = _appliance.Name;
				this.Name = _appliance.Name;

				saveItem.Enabled = true;
				restoreItem.Enabled = true;
				undefineItem.Enabled = true;
				sendAlertItem.Enabled = true;

				HideLogPanel();
			}
			catch( Exception )
			{
				_appliance = null;
				ShowLogPanel();
			}
		}

		private void logItem_Click(object sender, System.EventArgs e)
		{
			logItem.Checked = logPanel.Visible = !logItem.Checked;
			if ( logPanel.Visible )
				logPanel.BringToFront();
		}

		public void RemoveAppliance( Appliance a )
		{
			// does nothing - required by IPUCFrame interface
		}

		public void SendAll( PUC.Communication.Message msg ) 
		{
			IEnumerator e = _connections.GetEnumerator();
			while( e.MoveNext() )
				try
				{
					((Connection)e.Current).Send( msg );
				}
				catch( Exception )
				{
				}
		}

		private void saveItem_Click(object sender, System.EventArgs evtargs)
		{
			SaveFileDialog dlg = new SaveFileDialog();
			dlg.Filter = "xml files (*.xml)|*.xml|All files (*.*)|*.*";

			if ( dlg.ShowDialog() == DialogResult.OK )
			{
				Globals.GetFrame( _appliance ).AddLogLine( "Saving state in " + dlg.FileName + "..." );

				try
				{
					DataStore saveFile = new DataStore( dlg.FileName );

					IEnumerator e = _appliance.VariableTable.GetObjectEnumerator();
					while( e.MoveNext() )
					{
						if ( ((ApplianceObject)e.Current).State )
						{
							ApplianceState state = (ApplianceState)e.Current;
							if ( state.Defined )
								saveFile.Set( state.FullName, state.Value.ToString() );
						}
					}

					saveFile.Close();
				}
				catch( Exception )
				{
					Globals.GetFrame( _appliance ).AddLogLine( "File not found." );
				}
			}
		}

		private void restoreItem_Click(object sender, System.EventArgs evtargs)
		{
			OpenFileDialog dlg = new OpenFileDialog();
			dlg.Filter = "xml files (*.xml)|*.xml|All files (*.*)|*.*";

			if ( dlg.ShowDialog() == DialogResult.OK )
			{
				Globals.GetFrame( _appliance ).AddLogLine( "Restoring state from " + dlg.FileName + "..." );

				try
				{
					DataStore loadFile = new DataStore( dlg.FileName );

					IEnumerator e = _appliance.VariableTable.GetObjectEnumerator();
					while( e.MoveNext() )
					{
						if ( ((ApplianceObject)e.Current).State )
						{
							ApplianceState state = (ApplianceState)e.Current;
							if ( loadFile.IsKeyValid( state.Name ) )
								_debugConnection.Send( 
									new StateChangeNotification( state.FullName, 
																 loadFile.GetStringData( state.FullName ) ) );
						}
					}

					loadFile.Close();
				}
				catch( Exception )
				{
					Globals.GetFrame( _appliance ).AddLogLine( "File not found." );
				}
			}		
		}

		private void undefItem_Click(object sender, System.EventArgs e)
		{
			_undefineDialog.ShowDialog();
		}

		public Appliance GetAppliance()
		{
			return _appliance;
		}

		private void alertMsgItem_Click(object sender, System.EventArgs e)
		{
			AlertMsgEntryDialog dlg = new AlertMsgEntryDialog();

			if ( dlg.ShowDialog() == DialogResult.OK && ( dlg.msgBox.Text.Length > 0 ) )
			{
				SendAll( new AlertInformation( dlg.msgBox.Text ) );
			}
		}

		private void DebugServerFrame_Resize(object sender, System.EventArgs e)
		{
			if ( this.ClientSize.Width == 0 && this.ClientSize.Height == 0 )
				// this happens when the window is minimized
				return;

			this.logPanel.Size = this.ClientSize;
			this.logBox.Size = new System.Drawing.Size( this.logPanel.Size.Width, 
				this.logPanel.Size.Height - 
				this.logBox.Location.Y );
		
			if ( _appliance != null )
			{
				UIGenerator ui = _appliance.GetUIGenerator();
				InterfaceNode root = ui.InterfaceRoot;

				ui.Size = this.ClientSize;

				root.SetSize( ui.Size.Width, ui.Size.Height );
				root.DoLayout( ui.LayoutVars );
			}
		}
		#region IDevice2 Members

		public string ApplianceName
		{
			get
			{
				if ( _appliance == null )
					return "echo";
				else
					return _appliance.Name;
			}
		}

		public string Specification
		{
			get
			{
				return _specification;
			}
		}

		void PUC.IDevice2.HandleMessage(Connection c, PUC.Communication.Message m)
		{
			Globals.GetFrame( _appliance ).AddLogLine( m.ToString() );
			PUC.Communication.Message newmsg;

			if ( m is SpecRequest )
			{
				newmsg = new PUC.Communication.DeviceSpec( _specification );
				c.Send( newmsg );
			}
			else if ( m is FullStateRequest )
			{
				IEnumerator states = _appliance.VariableTable.GetObjectEnumerator();;
				while( states.MoveNext() )
				{
					if (! ((ApplianceObject)states.Current).State ) continue;

					ApplianceState state = (ApplianceState)states.Current;

					if ( state.Defined )
						newmsg = new StateChangeNotification( state.FullName, state.Value.ToString() );
					else
						newmsg = new StateChangeNotification( state.FullName );

					c.Send( newmsg );
				}
			}
			else if ( m is StateChangeRequest )
			{
				StateChangeRequest smsg = (StateChangeRequest)m;

				try 
				{
					ApplianceState state = 
						(ApplianceState)_appliance.VariableTable[ smsg.GetState() ];

					if ( state != null ) 
					{
						string val = 
							state.Type.ValueSpace.Validate( smsg.Value ).ToString();
						newmsg = new StateChangeNotification( smsg.GetState(), val );

						SendAll( newmsg );
					}
				}
				catch( Exception )
				{
				}
			}
			else if ( m is CommandInvokeRequest )
			{
				this.AddLogLine( "Command " + ((CommandInvokeRequest)m).GetCommand() + " requested to be invoked." );
			}
		}

		public void AddConnection( Connection c )
		{
			_connections.Add( c );
		}

		public void RemoveConnection(Connection c)
		{
			_connections.Remove( c );
		}

		public void RemoveAllConnections()
		{
			_connections.Clear();
		}

		public void Configure()
		{
			throw new NotImplementedException( "Not implemented for debug server." );
		}

		public bool HasGUI()
		{
			return true;
		}

		public void SetGUIVisiblity(bool visible)
		{
			this.Visible = visible;
		}

		public bool IsGUIVisible()
		{
			return this.Visible;
		}

		public void Start()
		{
			if ( _appliance != null )
			{
				_running = true;

				if ( StatusChanged != null )
					StatusChanged( this, new EventArgs() );
			}
		}

		public void Stop()
		{
			_running = false;

			IEnumerator e = _connections.GetEnumerator();
			while( e.MoveNext() )
			{
				if ( e.Current == _debugConnection )
					continue;

				((Connection)e.Current).Stop();
			}

			_connections.Clear();
			_connections.Add( _debugConnection );

			if ( _appliance != null && StatusChanged != null )
				StatusChanged( this, new EventArgs() );
		}

		public bool IsRunning()
		{
			return _running;
		}

		private void DebugServerFrame_Closing(object sender, System.ComponentModel.CancelEventArgs e)
		{
			e.Cancel = true;
			this.Visible = false;
		}

		public string Status
		{
			get
			{
				if ( _appliance == null )
					return "Error";
				else if ( _running )
					return "Active";
				else
					return "Inactive";
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
				_port = value;
			}
		}

		#endregion
	}

	public class DebugConnection : PUC.Communication.Connection
	{
		/*
		 * Member Variables
		 */

		protected DebugServerFrame _frame;


		/*
		 * Constructor
		 */

		public DebugConnection( DebugServerFrame frame ) 
		{
			_frame = frame;
		}


		/*
		 * Member Methods
		 */

		public override void Connect()
		{
		}

		public override void Disconnect()
		{
		}

		public override void Send(PUC.Communication.Message msg)
		{
			((IDevice2)_frame).HandleMessage( this, msg );
			Globals.AddEventCallback( new MessageDispatcher( this, msg ) );
		}
	}
}
