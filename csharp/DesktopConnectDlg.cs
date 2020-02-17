using System;
using System.Drawing;
using System.Collections;
using System.ComponentModel;
using System.Net;
using System.Windows.Forms;
using PUC;
using PUC.PersistentData;

namespace DesktopPUC
{
	/// <summary>
	/// Summary description for DesktopConnectDlg.
	/// </summary>
	public class DesktopConnectDlg : System.Windows.Forms.Form
	{
		private IPUCFrame _frame;
			
		private System.Windows.Forms.Label label1;
		private System.Windows.Forms.CheckBox openAllCheckBox;
		private System.Windows.Forms.Button connectBtn;
		private System.Windows.Forms.Button cancelBtn;
		private System.Windows.Forms.ComboBox addressBox;
		/// <summary>
		/// Required designer variable.
		/// </summary>
		private System.ComponentModel.Container components = null;

		public DesktopConnectDlg( IPUCFrame frame )
		{
			//
			// Required for Windows Form Designer support
			//
			InitializeComponent();

			_frame = frame;

			SetupRecentList();
		}

		public void SetupRecentList()
		{
			DataStore store = Globals.GetDataStore();

			int count = store.GetIntData( Globals.RECENT_COUNT_ATTR );
			int max = store.GetIntData( Globals.RECENT_SERVER_ATTR );

			if ( max > 0 )
			{
				addressBox.Items.Clear();

				for( int i = 0; i < count; i++ )
				{
					addressBox.Items.Add( store.GetStringData( Globals.RECENT_SERVER_PRFX + i ) );
				}

				if ( count > 0 )
					addressBox.Text = (string)addressBox.Items[ 0 ];
			}
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

		#region Windows Form Designer generated code
		/// <summary>
		/// Required method for Designer support - do not modify
		/// the contents of this method with the code editor.
		/// </summary>
		private void InitializeComponent()
		{
			this.label1 = new System.Windows.Forms.Label();
			this.openAllCheckBox = new System.Windows.Forms.CheckBox();
			this.connectBtn = new System.Windows.Forms.Button();
			this.cancelBtn = new System.Windows.Forms.Button();
			this.addressBox = new System.Windows.Forms.ComboBox();
			this.SuspendLayout();
			// 
			// label1
			// 
			this.label1.Location = new System.Drawing.Point(24, 16);
			this.label1.Name = "label1";
			this.label1.Size = new System.Drawing.Size(88, 16);
			this.label1.TabIndex = 0;
			this.label1.Text = "Server Address: ";
			this.label1.TextAlign = System.Drawing.ContentAlignment.MiddleRight;
			// 
			// openAllCheckBox
			// 
			this.openAllCheckBox.Location = new System.Drawing.Point(120, 40);
			this.openAllCheckBox.Name = "openAllCheckBox";
			this.openAllCheckBox.Size = new System.Drawing.Size(152, 24);
			this.openAllCheckBox.TabIndex = 2;
			this.openAllCheckBox.Text = "Open All Appliances";
			// 
			// connectBtn
			// 
			this.connectBtn.Location = new System.Drawing.Point(272, 80);
			this.connectBtn.Name = "connectBtn";
			this.connectBtn.Size = new System.Drawing.Size(72, 24);
			this.connectBtn.TabIndex = 3;
			this.connectBtn.Text = "Connect";
			this.connectBtn.Click += new System.EventHandler(this.connectBtn_Click);
			// 
			// cancelBtn
			// 
			this.cancelBtn.Location = new System.Drawing.Point(192, 80);
			this.cancelBtn.Name = "cancelBtn";
			this.cancelBtn.Size = new System.Drawing.Size(72, 24);
			this.cancelBtn.TabIndex = 4;
			this.cancelBtn.Text = "Cancel";
			this.cancelBtn.Click += new System.EventHandler(this.cancelBtn_Click);
			// 
			// addressBox
			// 
			this.addressBox.Location = new System.Drawing.Point(120, 14);
			this.addressBox.Name = "addressBox";
			this.addressBox.Size = new System.Drawing.Size(224, 21);
			this.addressBox.TabIndex = 5;
			// 
			// DesktopConnectDlg
			// 
			this.AutoScaleBaseSize = new System.Drawing.Size(5, 13);
			this.ClientSize = new System.Drawing.Size(376, 118);
			this.Controls.Add(this.addressBox);
			this.Controls.Add(this.cancelBtn);
			this.Controls.Add(this.connectBtn);
			this.Controls.Add(this.openAllCheckBox);
			this.Controls.Add(this.label1);
			this.Name = "DesktopConnectDlg";
			this.Text = "Connect...";
			this.Load += new System.EventHandler(this.DesktopConnectDlg_Load);
			this.ResumeLayout(false);

		}
		#endregion

		private void cancelBtn_Click(object sender, System.EventArgs e)
		{
			this.Close();
		}

		private void connectBtn_Click(object sender, System.EventArgs e)
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
				server.Reset( openAllCheckBox.Checked );
			else
			{
				server = new ServerInfo( ipAddr, openAllCheckBox.Checked );

				Globals.AddServer( server );
			}
		}

		private void DesktopConnectDlg_Load(object sender, System.EventArgs e)
		{
			SetupRecentList();
		}
	}
}
