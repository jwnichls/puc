using System;
using System.Drawing;
using System.Collections;
using System.ComponentModel;
using System.Net;
using System.Windows.Forms;

using PUC;
using PUC.PersistentData;


namespace PhonePUC
{
	/// <summary>
	/// Summary description for PhoneConnectDlg.
	/// </summary>
	public class PhoneConnectDlg : System.Windows.Forms.Form
	{
		private IPUCFrame _frame;

		private System.Windows.Forms.MainMenu mainMenu1;
		private System.Windows.Forms.MenuItem openItem;
		private System.Windows.Forms.MenuItem cancelItem;
		private System.Windows.Forms.Label label1;
		private System.Windows.Forms.ComboBox recentBox;
		private System.Windows.Forms.Label label2;
		private System.Windows.Forms.CheckBox openAllCheckBox;
		private System.Windows.Forms.TextBox addressBox;
	
		public PhoneConnectDlg( IPUCFrame frame )
		{
			//
			// Required for Windows Form Designer support
			//
			InitializeComponent();

			_frame = frame;

			SetupRecentList();

			addressBox.Text = recentBox.Text;
		}


		public void SetupRecentList()
		{
			DataStore store = Globals.GetDataStore();

			int count = store.GetIntData( Globals.RECENT_COUNT_ATTR );
			int max = store.GetIntData( Globals.RECENT_SERVER_ATTR );

			if ( max > 0 )
			{
				recentBox.Items.Clear();

				for( int i = 0; i < count; i++ )
				{
					recentBox.Items.Add( store.GetStringData( Globals.RECENT_SERVER_PRFX + i ) );
				}

				if ( count > 0 )
					recentBox.Text = (string)recentBox.Items[ 0 ];
			}
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
			this.openItem = new System.Windows.Forms.MenuItem();
			this.cancelItem = new System.Windows.Forms.MenuItem();
			this.label1 = new System.Windows.Forms.Label();
			this.recentBox = new System.Windows.Forms.ComboBox();
			this.label2 = new System.Windows.Forms.Label();
			this.addressBox = new System.Windows.Forms.TextBox();
			this.openAllCheckBox = new System.Windows.Forms.CheckBox();
			// 
			// mainMenu1
			// 
			this.mainMenu1.MenuItems.Add(this.openItem);
			this.mainMenu1.MenuItems.Add(this.cancelItem);
			// 
			// openItem
			// 
			this.openItem.Text = "Open";
			this.openItem.Click += new System.EventHandler(this.openItem_Click);
			// 
			// cancelItem
			// 
			this.cancelItem.Text = "Cancel";
			this.cancelItem.Click += new System.EventHandler(this.cancelItem_Click);
			// 
			// label1
			// 
			this.label1.Size = new System.Drawing.Size(168, 24);
			this.label1.Text = "Recent Servers:";
			// 
			// recentBox
			// 
			this.recentBox.Location = new System.Drawing.Point(0, 24);
			this.recentBox.Size = new System.Drawing.Size(168, 26);
			this.recentBox.SelectedIndexChanged += new System.EventHandler(this.recentBox_SelectedIndexChanged);
			// 
			// label2
			// 
			this.label2.Location = new System.Drawing.Point(0, 64);
			this.label2.Size = new System.Drawing.Size(168, 24);
			this.label2.Text = "IP Address:";
			// 
			// addressBox
			// 
			this.addressBox.Location = new System.Drawing.Point(0, 88);
			this.addressBox.Size = new System.Drawing.Size(168, 25);
			this.addressBox.Text = "";
			// 
			// openAllCheckBox
			// 
			this.openAllCheckBox.Location = new System.Drawing.Point(0, 128);
			this.openAllCheckBox.Size = new System.Drawing.Size(168, 24);
			this.openAllCheckBox.Text = "Open All Appliances";
			// 
			// PhoneConnectDlg
			// 
			this.Controls.Add(this.recentBox);
			this.Controls.Add(this.addressBox);
			this.Controls.Add(this.openAllCheckBox);
			this.Controls.Add(this.label2);
			this.Controls.Add(this.label1);
			this.Menu = this.mainMenu1;
			this.Text = "Connect...";

		}
		#endregion

		private void recentBox_SelectedIndexChanged(object sender, System.EventArgs e)
		{
			addressBox.Text = recentBox.Text;
		}

		private void cancelItem_Click(object sender, System.EventArgs e)
		{
			this.Close();
		}

		private void openItem_Click(object sender, System.EventArgs e)
		{
			IPAddress ipAddr = null;

			try
			{
				ipAddr = IPAddress.Parse( addressBox.Text );
			}
			catch( Exception )
			{
				MessageBox.Show( "Invalid IP Address Specified", "Parse Error" );
				return;
			}

			this.Close();

			Globals.GetDefaultLog().AddLogLine( "Attempting to open server at " + addressBox.Text + "..." );


			ServerInfo server = Globals.DoesServerExist( ipAddr );

			if ( server != null )
				server.Reset( false );
			else
			{
				server = new ServerInfo( ipAddr, openAllCheckBox.Checked );

				Globals.AddServer( server );
			}

		}
	}
}
