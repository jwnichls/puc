using System;
using System.Data;
using System.Diagnostics;
using System.Drawing;
using System.Collections;
using System.ComponentModel;
using System.IO;
using System.Threading;
using System.Windows.Forms;

using PUC;
using PUC.Communication;
using PUC.PersistentData;

using DesktopPUC;


namespace DebugServer
{
	/// <summary>
	/// Summary description for ApplianceGrid.
	/// </summary>
	public class ApplianceGrid : System.Windows.Forms.Form, ILogManager, ICallbackManager, IShutdown
	{
		/*
		 * Constants
		 */

		public const string VERSION_STRING = "PUC Debug Server v2.0";
		public const int DEBUG_PORT_START  = 5170;

		public const int DEVICE_COLUMN = 0;
		public const int STATUS_COLUMN = 1;
		public const int PORT_COLUMN = 2;
		public const int VISIBLE_COLUMN = 3;
		public const int ACTIVE_COLUMN = 4;

		
		/*
		 * Variables
		 */

		private System.Windows.Forms.MainMenu mainMenu1;
		private System.Windows.Forms.MenuItem menuItem1;
		private System.Windows.Forms.MenuItem menuItem4;
		private System.Windows.Forms.Timer eventTimerQueue;
		private System.ComponentModel.IContainer components;

		private Queue     _uiEventQueue;
		private Mutex     _mutex;

		protected bool _recentMenuItems = false;
		private System.Windows.Forms.MenuItem menuItem7;
		private System.Windows.Forms.MenuItem menuItem8;
		private System.Windows.Forms.MenuItem addItem;
		private System.Windows.Forms.MenuItem exitItem;
		private System.Windows.Forms.MenuItem optionsItem;
		private System.Windows.Forms.MenuItem recentMenu;
		private System.Windows.Forms.DataGrid applianceList;
		private System.Windows.Forms.DataGridTableStyle applianceGridStyle;
		private System.Windows.Forms.DataGridTextBoxColumn deviceColumn;
		private System.Windows.Forms.DataGridTextBoxColumn statusColumn;
		private System.Windows.Forms.DataGridBoolColumn visibleColumn;
		private System.Windows.Forms.DataGridBoolColumn activeColumn;

		protected DesktopOptionsDialog _optionsDialog;

		protected DataTable _applianceTable;

		protected MeasureStringControl _measureCtl;

		protected int _portCounter;
		private System.Windows.Forms.DataGridTextBoxColumn portColumn;
		private System.Windows.Forms.MenuItem convertItem;
		private System.Windows.Forms.MenuItem menuItem2;
		private System.Windows.Forms.MenuItem convertMultipleItem;
		private System.Windows.Forms.MenuItem menuItem3;
		private System.Windows.Forms.MenuItem menuItem5;

		private PUCServer _server;


		/*
		 * Constructor
		 */

		public ApplianceGrid()
		{
			//
			// Required for Windows Form Designer support
			//
			InitializeComponent();
			
			_measureCtl = new MeasureStringControl();
			this.Controls.Add( _measureCtl );
			_measureCtl.Location = new Point( 0, 0 );
			_measureCtl.Size = new Size( 0, 0 );

			Globals.Init( VERSION_STRING, ".\\debugdata.xml", this, this, this, null, _measureCtl, "echodevice.xml" );
			this.Text = Globals.GetVersionString();

			_uiEventQueue = new Queue();
			_mutex = new Mutex();

			_portCounter = DEBUG_PORT_START;

			_server = new PUCServer();

			_optionsDialog = new DesktopOptionsDialog();

			SetupDataTable();
			SetupRecentList();
		}

		/// <summary>
		/// Clean up any resources being used.
		/// </summary>
		protected override void Dispose( bool disposing )
		{
			if( disposing )
			{
				if(components != null)
				{
					components.Dispose();
				}
			}
			base.Dispose( disposing );
		}

		/// <summary>
		/// The main entry point for the application.
		/// </summary>
		[STAThread]
		static void Main() 
		{
			Application.Run(new ApplianceGrid());
		}

		#region Windows Form Designer generated code
		/// <summary>
		/// Required method for Designer support - do not modify
		/// the contents of this method with the code editor.
		/// </summary>
		private void InitializeComponent()
		{
			this.components = new System.ComponentModel.Container();
			System.Resources.ResourceManager resources = new System.Resources.ResourceManager(typeof(ApplianceGrid));
			this.mainMenu1 = new System.Windows.Forms.MainMenu();
			this.menuItem1 = new System.Windows.Forms.MenuItem();
			this.addItem = new System.Windows.Forms.MenuItem();
			this.optionsItem = new System.Windows.Forms.MenuItem();
			this.menuItem7 = new System.Windows.Forms.MenuItem();
			this.recentMenu = new System.Windows.Forms.MenuItem();
			this.menuItem8 = new System.Windows.Forms.MenuItem();
			this.menuItem4 = new System.Windows.Forms.MenuItem();
			this.exitItem = new System.Windows.Forms.MenuItem();
			this.menuItem2 = new System.Windows.Forms.MenuItem();
			this.convertItem = new System.Windows.Forms.MenuItem();
			this.convertMultipleItem = new System.Windows.Forms.MenuItem();
			this.eventTimerQueue = new System.Windows.Forms.Timer(this.components);
			this.applianceList = new System.Windows.Forms.DataGrid();
			this.applianceGridStyle = new System.Windows.Forms.DataGridTableStyle();
			this.deviceColumn = new System.Windows.Forms.DataGridTextBoxColumn();
			this.statusColumn = new System.Windows.Forms.DataGridTextBoxColumn();
			this.portColumn = new System.Windows.Forms.DataGridTextBoxColumn();
			this.visibleColumn = new System.Windows.Forms.DataGridBoolColumn();
			this.activeColumn = new System.Windows.Forms.DataGridBoolColumn();
			this.menuItem3 = new System.Windows.Forms.MenuItem();
			this.menuItem5 = new System.Windows.Forms.MenuItem();
			((System.ComponentModel.ISupportInitialize)(this.applianceList)).BeginInit();
			this.SuspendLayout();
			// 
			// mainMenu1
			// 
			this.mainMenu1.MenuItems.AddRange(new System.Windows.Forms.MenuItem[] {
																					  this.menuItem1,
																					  this.menuItem2});
			// 
			// menuItem1
			// 
			this.menuItem1.Index = 0;
			this.menuItem1.MenuItems.AddRange(new System.Windows.Forms.MenuItem[] {
																					  this.addItem,
																					  this.optionsItem,
																					  this.menuItem7,
																					  this.recentMenu,
																					  this.menuItem4,
																					  this.exitItem});
			this.menuItem1.Text = "File";
			// 
			// addItem
			// 
			this.addItem.Index = 0;
			this.addItem.Text = "Add Appliance...";
			this.addItem.Click += new System.EventHandler(this.addItem_Click);
			// 
			// optionsItem
			// 
			this.optionsItem.Index = 1;
			this.optionsItem.Text = "Options...";
			this.optionsItem.Click += new System.EventHandler(this.optionsItem_Click);
			// 
			// menuItem7
			// 
			this.menuItem7.Index = 2;
			this.menuItem7.Text = "-";
			// 
			// recentMenu
			// 
			this.recentMenu.Index = 3;
			this.recentMenu.MenuItems.AddRange(new System.Windows.Forms.MenuItem[] {
																					   this.menuItem8});
			this.recentMenu.Text = "Recent Appliances";
			// 
			// menuItem8
			// 
			this.menuItem8.Enabled = false;
			this.menuItem8.Index = 0;
			this.menuItem8.Text = "None";
			// 
			// menuItem4
			// 
			this.menuItem4.Index = 4;
			this.menuItem4.Text = "-";
			// 
			// exitItem
			// 
			this.exitItem.Index = 5;
			this.exitItem.Text = "Exit";
			// 
			// menuItem2
			// 
			this.menuItem2.Index = 1;
			this.menuItem2.MenuItems.AddRange(new System.Windows.Forms.MenuItem[] {
																					  this.convertItem,
																					  this.convertMultipleItem,
																					  this.menuItem3,
																					  this.menuItem5});
			this.menuItem2.Text = "Convert";
			// 
			// convertItem
			// 
			this.convertItem.Index = 0;
			this.convertItem.Text = "2.0 to 2.1 - Single File...";
			this.convertItem.Click += new System.EventHandler(this.convertItem_Click);
			// 
			// convertMultipleItem
			// 
			this.convertMultipleItem.Index = 1;
			this.convertMultipleItem.Text = "2.0 to 2.1 - Multiple Files...";
			this.convertMultipleItem.Click += new System.EventHandler(this.convertMultipleItem_Click);
			// 
			// eventTimerQueue
			// 
			this.eventTimerQueue.Enabled = true;
			this.eventTimerQueue.Tick += new System.EventHandler(this.eventTimerQueue_Tick);
			// 
			// applianceList
			// 
			this.applianceList.AccessibleName = "DataGrid";
			this.applianceList.AccessibleRole = System.Windows.Forms.AccessibleRole.Table;
			this.applianceList.AllowNavigation = false;
			this.applianceList.AllowSorting = false;
			this.applianceList.CaptionVisible = false;
			this.applianceList.DataMember = "";
			this.applianceList.HeaderForeColor = System.Drawing.SystemColors.ControlText;
			this.applianceList.Location = new System.Drawing.Point(0, 0);
			this.applianceList.Name = "applianceList";
			this.applianceList.ParentRowsVisible = false;
			this.applianceList.RowHeadersVisible = false;
			this.applianceList.Size = new System.Drawing.Size(384, 264);
			this.applianceList.TabIndex = 0;
			this.applianceList.TableStyles.AddRange(new System.Windows.Forms.DataGridTableStyle[] {
																									  this.applianceGridStyle});
			this.applianceList.Click += new System.EventHandler(this.applianceList_Click);
			// 
			// applianceGridStyle
			// 
			this.applianceGridStyle.DataGrid = this.applianceList;
			this.applianceGridStyle.GridColumnStyles.AddRange(new System.Windows.Forms.DataGridColumnStyle[] {
																												 this.deviceColumn,
																												 this.statusColumn,
																												 this.portColumn,
																												 this.visibleColumn,
																												 this.activeColumn});
			this.applianceGridStyle.HeaderForeColor = System.Drawing.SystemColors.ControlText;
			this.applianceGridStyle.MappingName = "appliances";
			this.applianceGridStyle.RowHeadersVisible = false;
			// 
			// deviceColumn
			// 
			this.deviceColumn.Format = "";
			this.deviceColumn.FormatInfo = null;
			this.deviceColumn.HeaderText = "Device";
			this.deviceColumn.MappingName = "Device";
			this.deviceColumn.ReadOnly = true;
			this.deviceColumn.Width = 200;
			// 
			// statusColumn
			// 
			this.statusColumn.Format = "";
			this.statusColumn.FormatInfo = null;
			this.statusColumn.HeaderText = "Status";
			this.statusColumn.MappingName = "Status";
			this.statusColumn.ReadOnly = true;
			this.statusColumn.Width = 75;
			// 
			// portColumn
			// 
			this.portColumn.Format = "";
			this.portColumn.FormatInfo = null;
			this.portColumn.HeaderText = "Port";
			this.portColumn.MappingName = "Port";
			this.portColumn.Width = 60;
			// 
			// visibleColumn
			// 
			this.visibleColumn.AllowNull = false;
			this.visibleColumn.FalseValue = false;
			this.visibleColumn.HeaderText = "Visible";
			this.visibleColumn.MappingName = "Visible";
			this.visibleColumn.NullValue = ((object)(resources.GetObject("visibleColumn.NullValue")));
			this.visibleColumn.TrueValue = true;
			this.visibleColumn.Width = 60;
			// 
			// activeColumn
			// 
			this.activeColumn.AllowNull = false;
			this.activeColumn.FalseValue = false;
			this.activeColumn.HeaderText = "Active";
			this.activeColumn.MappingName = "Active";
			this.activeColumn.NullValue = ((object)(resources.GetObject("activeColumn.NullValue")));
			this.activeColumn.TrueValue = true;
			this.activeColumn.Width = 60;
			// 
			// menuItem3
			// 
			this.menuItem3.Index = 2;
			this.menuItem3.Text = "2.1 to 2.2 - Single File...";
			this.menuItem3.Click += new System.EventHandler(this.menuItem3_Click);
			// 
			// menuItem5
			// 
			this.menuItem5.Index = 3;
			this.menuItem5.Text = "2.1 to 2.2 - Multiple Files...";
			this.menuItem5.Click += new System.EventHandler(this.menuItem5_Click);
			// 
			// ApplianceGrid
			// 
			this.AutoScaleBaseSize = new System.Drawing.Size(5, 13);
			this.ClientSize = new System.Drawing.Size(392, 271);
			this.Controls.Add(this.applianceList);
			this.Menu = this.mainMenu1;
			this.Name = "ApplianceGrid";
			this.Text = "Debug Server";
			this.Resize += new System.EventHandler(this.ApplianceGrid_Resize);
			this.Closing += new System.ComponentModel.CancelEventHandler(this.ApplianceGrid_Closing);
			((System.ComponentModel.ISupportInitialize)(this.applianceList)).EndInit();
			this.ResumeLayout(false);

		}
		#endregion	

		private void ApplianceGrid_Resize(object sender, System.EventArgs e)
		{
			applianceList.Height = this.ClientSize.Height;
			applianceList.Width = this.ClientSize.Width;
			
			int width = applianceList.Width - 
					( applianceList.TableStyles[0].GridColumnStyles[ ACTIVE_COLUMN ].Width + 
				      applianceList.TableStyles[0].GridColumnStyles[ VISIBLE_COLUMN ].Width +
				      applianceList.TableStyles[0].GridColumnStyles[ PORT_COLUMN ].Width ) - 5;
			
			applianceList.TableStyles[0].GridColumnStyles[ DEVICE_COLUMN ].Width = 2 * width / 3;
			applianceList.TableStyles[0].GridColumnStyles[ STATUS_COLUMN ].Width = width / 3;
		}

		public void shutdown()
		{
			Application.Exit();
			Process.GetCurrentProcess().Kill();
		}

		public void AddLogLine(string text)
		{
			// TODO:  Add ApplianceGrid.AddLogLine implementation
		}

		public void AddLogText(string text)
		{
			// TODO:  Add ApplianceGrid.AddLogText implementation
		}

		public MenuItem GetServerMenu()
		{
			return null;
		}

		public void AddActiveAppliance(Appliance a)
		{
		}

		public void SetCurrentAppliance(Appliance a)
		{
		}

		public void RemoveAppliance(Appliance a)
		{
		}

		public void AddServer(ServerInfo s)
		{
		}

		public ServerInfo DoesServerExist(System.Net.IPAddress ip)
		{
			return null;
		}

		public void AddEventCallback(IEventDispatcher e)
		{
			_mutex.WaitOne();

			_uiEventQueue.Enqueue( e );

			_mutex.ReleaseMutex();
		}

		public void HideLogPanel()
		{
		}

		public void ShowLogPanel()
		{
		}

		protected void SetupDataTable() 
		{
			_applianceTable = new DataTable("appliances");
			
			_applianceTable.Columns.Add( "Device", "".GetType() );
			_applianceTable.Columns.Add( "Status", "".GetType() );
			_applianceTable.Columns.Add( "Port", _portCounter.GetType() );
			_applianceTable.Columns.Add( "Visible", true.GetType() );
			_applianceTable.Columns.Add( "Active", true.GetType() );
			_applianceTable.Columns.Add( "DeviceObject", Type.GetType( "PUC.IDevice2" ) );

			_applianceTable.Columns[ "DeviceObject" ].Unique = true;
			_applianceTable.PrimaryKey = new DataColumn[] { _applianceTable.Columns[ "DeviceObject" ] };

			_applianceTable.ColumnChanging +=new DataColumnChangeEventHandler(_applianceTable_ColumnChanging);

			DataView view = new DataView( _applianceTable );
			
			view.AllowNew = false;
			view.AllowDelete = false;

			applianceList.SetDataBinding( view, "" );
			applianceList.TableStyles[ 0 ].MappingName = _applianceTable.TableName;
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
				item.Text = Path.GetFileName( server );

				item.Click += new EventHandler(this.recentItem_Click);

				recentMenu.MenuItems.Add( item );
			}
		}
		
		private void recentItem_Click(object sender, System.EventArgs e)
		{
			int idx = recentMenu.MenuItems.IndexOf( (MenuItem)sender );

			try
			{
				string spec = Globals.GetDataStore().GetStringData( Globals.RECENT_SERVER_PRFX + idx );

				addAppliance( spec );
			}
			catch( Exception excpt )
			{
				Globals.GetDefaultLog().AddLogLine( "Couldn't open spec." );

				MessageBox.Show( this, excpt.Message, excpt.GetType().Name );
			}
		}

		private void eventTimerQueue_Tick(object sender, System.EventArgs e)
		{
			_mutex.WaitOne();

			while( _uiEventQueue.Count > 0 ) 
				((IEventDispatcher)_uiEventQueue.Dequeue()).Dispatch();

			_mutex.ReleaseMutex();
		}

		private void optionsItem_Click(object sender, System.EventArgs e)
		{
			_optionsDialog.ShowDialog();
		}

		private void addItem_Click(object sender, System.EventArgs e)
		{
			OpenFileDialog dlg = new OpenFileDialog();
			dlg.Filter = "xml files (*.xml)|*.xml|All files (*.*)|*.*";

			if ( dlg.ShowDialog() == DialogResult.OK )
			{
				addAppliance( dlg.FileName );
			}
		}

		protected void addAppliance( string filename )
		{
			DebugServerFrame frame = 
				new DebugServerFrame( filename, _portCounter++ );
			frame.Show();

			frame.VisibleChanged += new EventHandler(DeviceVisibleChanged);

			DataRow row = _applianceTable.NewRow();

			row[ "DeviceObject" ] = frame;
			row[ "Device" ] = frame.ApplianceName;
			row[ "Status" ] = frame.Status;
			row[ "Port" ] = frame.Port;
			row[ "Active" ] = false;
			row[ "Visible" ] = frame.Visible;

			_applianceTable.Rows.Add( row );
			_applianceTable.AcceptChanges();

			_server.AddDevice( frame );

			string serverString = Path.GetFullPath( filename );

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
		}

		private void ApplianceGrid_Closing(object sender, System.ComponentModel.CancelEventArgs e)
		{
			shutdown();
		}

		private void _applianceTable_ColumnChanging(object sender, DataColumnChangeEventArgs e)
		{
			if ( e.Column.ColumnName == "DeviceObject" )
				return;

			IDevice2 dev = (IDevice2)e.Row[ "DeviceObject" ];
			
			switch ( e.Column.ColumnName )
			{
				case "Port":
					if ( dev.Port != (int)e.ProposedValue )
					{
						if ( dev.IsRunning() )
						{
							if ( _server.IsListenerOnPort( (int)e.ProposedValue ) )
								e.Row.RejectChanges();
							else
							{
								dev.Stop();
								_server.StopListener( dev );

								dev.Port = (int)e.ProposedValue;

								dev.Start();
								_server.StartListener( dev );
							}
						}
						else
							dev.Port = (int)e.ProposedValue;
					}
					break;

				case "Active":
					if ( (bool)e.ProposedValue )
					{
						try
						{
							dev.Start();

							if ( dev.IsRunning() )
								_server.StartListener( dev );
							else
								e.ProposedValue = false;
						}
						catch( Exception )
						{
							dev.Stop();
							_server.StopListener( dev );
						}
					}
					else
					{
						dev.Stop();
						_server.StopListener( dev );
					}
					break;

				case "Visible":
					((DebugServerFrame)dev).Visible = (bool)e.ProposedValue;
					break;
			}
		}

		private void applianceList_Click(object sender, System.EventArgs e)
		{
			if ( applianceList.CurrentCell.ColumnNumber >= VISIBLE_COLUMN )
			{
				DataRow row = _applianceTable.Rows[ applianceList.CurrentCell.RowNumber ];

				row.BeginEdit();
				row[ applianceList.CurrentCell.ColumnNumber ] = !(bool)row[ applianceList.CurrentCell.ColumnNumber ];
				row.EndEdit();

				_applianceTable.AcceptChanges();
			}		
		}

		private void DeviceVisibleChanged(object sender, EventArgs e)
		{
			DebugServerFrame device = (DebugServerFrame)sender;

			DataRow row = _applianceTable.Rows.Find( device );
			row.BeginEdit();
			row[ "Visible" ] = device.Visible;
			row.EndEdit();

			_applianceTable.AcceptChanges();
		}

		private void convertItem_Click(object sender, System.EventArgs e)
		{
			PUC.Parsers.Converter20to21.Convert();
		}

		private void convertMultipleItem_Click(object sender, System.EventArgs e)
		{
			PUC.Parsers.Converter20to21.ConvertFiles();
		}

		private void menuItem3_Click(object sender, System.EventArgs e)
		{
			PUC.Parsers.Converter21to22.Convert();
		}

		private void menuItem5_Click(object sender, System.EventArgs e)
		{
			PUC.Parsers.Converter21to22.ConvertFiles();
		}
	}
}
