using System;
using System.Drawing;
using System.Collections;
using System.ComponentModel;
using System.Windows.Forms;

using PUC;
using PUC.Parsers;
using PUC.PersistentData;
using PUC.UIGeneration;


namespace PhonePUC
{
	/// <summary>
	/// Summary description for PhoneOptionsDlg.
	/// </summary>
	public class PhoneOptionsDlg : System.Windows.Forms.Form
	{
		private System.Windows.Forms.TextBox registryBox;
		private System.Windows.Forms.Label label2;
		private System.Windows.Forms.ComboBox recentServerNum;
		private System.Windows.Forms.MainMenu mainMenu1;
		private System.Windows.Forms.MenuItem saveItem;
		private System.Windows.Forms.MenuItem cancelItem;
		private System.Windows.Forms.Label label1;
	
		public PhoneOptionsDlg()
		{
			//
			// Required for Windows Form Designer support
			//
			InitializeComponent();

			//
			// TODO: Add any constructor code after InitializeComponent call
			//
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
			this.label1 = new System.Windows.Forms.Label();
			this.registryBox = new System.Windows.Forms.TextBox();
			this.label2 = new System.Windows.Forms.Label();
			this.recentServerNum = new System.Windows.Forms.ComboBox();
			this.mainMenu1 = new System.Windows.Forms.MainMenu();
			this.saveItem = new System.Windows.Forms.MenuItem();
			this.cancelItem = new System.Windows.Forms.MenuItem();
			// 
			// label1
			// 
			this.label1.Size = new System.Drawing.Size(168, 24);
			this.label1.Text = "Registry File:";
			// 
			// registryBox
			// 
			this.registryBox.Location = new System.Drawing.Point(0, 24);
			this.registryBox.Size = new System.Drawing.Size(168, 25);
			this.registryBox.Text = "";
			// 
			// label2
			// 
			this.label2.Location = new System.Drawing.Point(0, 56);
			this.label2.Size = new System.Drawing.Size(168, 24);
			this.label2.Text = "Recent Server Number:";
			// 
			// recentServerNum
			// 
			this.recentServerNum.Items.Add("0");
			this.recentServerNum.Items.Add("1");
			this.recentServerNum.Items.Add("2");
			this.recentServerNum.Items.Add("3");
			this.recentServerNum.Items.Add("4");
			this.recentServerNum.Items.Add("5");
			this.recentServerNum.Items.Add("6");
			this.recentServerNum.Items.Add("7");
			this.recentServerNum.Items.Add("8");
			this.recentServerNum.Items.Add("9");
			this.recentServerNum.Items.Add("10");
			this.recentServerNum.Location = new System.Drawing.Point(0, 80);
			this.recentServerNum.Size = new System.Drawing.Size(168, 26);
			// 
			// mainMenu1
			// 
			this.mainMenu1.MenuItems.Add(this.saveItem);
			this.mainMenu1.MenuItems.Add(this.cancelItem);
			// 
			// saveItem
			// 
			this.saveItem.Text = "Save";
			this.saveItem.Click += new System.EventHandler(this.saveItem_Click);
			// 
			// cancelItem
			// 
			this.cancelItem.Text = "Cancel";
			this.cancelItem.Click += new System.EventHandler(this.cancelItem_Click);
			// 
			// PhoneOptionsDlg
			// 
			this.Controls.Add(this.registryBox);
			this.Controls.Add(this.recentServerNum);
			this.Controls.Add(this.label2);
			this.Controls.Add(this.label1);
			this.Menu = this.mainMenu1;
			this.Text = "PhoneOptionsDlg";
			this.Load += new System.EventHandler(this.PhoneOptionsDlg_Load);

		}
		#endregion

		private void PhoneOptionsDlg_Load(object sender, System.EventArgs e)
		{
			registryBox.Text = Globals.GetWidgetRegistryFileName();
			recentServerNum.SelectedIndex = Globals.GetRecentServerMaximum();
		}

		private void saveItem_Click(object sender, System.EventArgs e)
		{
			DataStore store = Globals.GetDataStore();

			int recentCount = store.GetIntData( Globals.RECENT_COUNT_ATTR );

			store.Set( Globals.REGISTRY_FILE_ATTR, registryBox.Text );
			store.Set( Globals.RECENT_SERVER_ATTR, recentServerNum.SelectedIndex );

			if ( recentCount > recentServerNum.SelectedIndex )
				store.Set( Globals.RECENT_COUNT_ATTR, recentServerNum.SelectedIndex );

			// FIXME:JWN: This needs to be done somewhere else.
			// UIGeneratorCore.ObjectRegistry = WidgetRegistryParser.Parse( registryBox.Text );

			this.Close();
		}

		private void cancelItem_Click(object sender, System.EventArgs e)
		{
			this.Close();
		}
	}
}
