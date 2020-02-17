using System;
using System.Drawing;
using System.Collections;
using System.ComponentModel;
using System.Net;
using System.Windows.Forms;
using Microsoft.WindowsCE.Forms;

namespace PUC
{
	/// <summary>
	/// Summary description for ConnectServerDlg.
	/// </summary>
	public class ConnectServerDlg : System.Windows.Forms.Form
	{
		private IPUCFrame _frame;

		private System.Windows.Forms.TextBox addressBox;
		private System.Windows.Forms.Label label1;
		private System.Windows.Forms.Button connectBtn;
		private System.Windows.Forms.Button cancelBtn;
		private System.Windows.Forms.MainMenu mainMenu1;
		private System.Windows.Forms.CheckBox openAllCheckBox;

		private Microsoft.WindowsCE.Forms.InputPanel _inputPanel;
	
		public ConnectServerDlg( IPUCFrame frame )
		{
			//
			// Required for Windows Form Designer support
			//
			InitializeComponent();

			_frame = frame;

			_inputPanel = new InputPanel();

			int count = Globals.GetDataStore().GetIntData( Globals.RECENT_COUNT_ATTR );
			if ( count > 0 )
				addressBox.Text = Globals.GetDataStore().GetStringData( Globals.RECENT_SERVER_PRFX + "0" );
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
			System.Resources.ResourceManager resources = new System.Resources.ResourceManager(typeof(ConnectServerDlg));
			this.addressBox = new System.Windows.Forms.TextBox();
			this.label1 = new System.Windows.Forms.Label();
			this.openAllCheckBox = new System.Windows.Forms.CheckBox();
			this.connectBtn = new System.Windows.Forms.Button();
			this.cancelBtn = new System.Windows.Forms.Button();
			this.mainMenu1 = new System.Windows.Forms.MainMenu();
			// 
			// addressBox
			// 
			this.addressBox.Location = new System.Drawing.Point(104, 16);
			this.addressBox.Size = new System.Drawing.Size(128, 22);
			this.addressBox.Text = "128.2.211.27";
			// 
			// label1
			// 
			this.label1.Location = new System.Drawing.Point(0, 18);
			this.label1.Size = new System.Drawing.Size(96, 24);
			this.label1.Text = "Server Address:";
			this.label1.TextAlign = System.Drawing.ContentAlignment.TopRight;
			// 
			// openAllCheckBox
			// 
			this.openAllCheckBox.Location = new System.Drawing.Point(56, 48);
			this.openAllCheckBox.Size = new System.Drawing.Size(152, 24);
			this.openAllCheckBox.Text = "Open All Appliances";
			// 
			// connectBtn
			// 
			this.connectBtn.Location = new System.Drawing.Point(144, 96);
			this.connectBtn.Size = new System.Drawing.Size(88, 24);
			this.connectBtn.Text = "Connect";
			this.connectBtn.Click += new System.EventHandler(this.button1_Click);
			// 
			// cancelBtn
			// 
			this.cancelBtn.Location = new System.Drawing.Point(48, 96);
			this.cancelBtn.Size = new System.Drawing.Size(88, 24);
			this.cancelBtn.Text = "Cancel";
			this.cancelBtn.Click += new System.EventHandler(this.cancelBtn_Click);
			// 
			// ConnectServerDlg
			// 
			this.ControlBox = false;
			this.Controls.Add(this.cancelBtn);
			this.Controls.Add(this.connectBtn);
			this.Controls.Add(this.openAllCheckBox);
			this.Controls.Add(this.label1);
			this.Controls.Add(this.addressBox);
			this.Menu = this.mainMenu1;
			this.Text = "Connect...";
			this.Closing += new System.ComponentModel.CancelEventHandler(this.ConnectServerDlg_Closing);
			this.Load += new System.EventHandler(this.ConnectServerDlg_Load);

		}
		#endregion

		private void button1_Click(object sender, System.EventArgs e)
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

		private void cancelBtn_Click(object sender, System.EventArgs e)
		{
			this.Close();
		}

		private void ConnectServerDlg_Closing(object sender, System.ComponentModel.CancelEventArgs e)
		{
			_inputPanel.Enabled = false;
		}

		private void ConnectServerDlg_Load(object sender, System.EventArgs e)
		{
			_inputPanel.Enabled = true;
		}
	}
}
