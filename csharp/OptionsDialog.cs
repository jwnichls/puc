using System;
using System.Drawing;
using System.Collections;
using System.ComponentModel;
using System.Windows.Forms;
using PUC.PersistentData;

namespace PUC
{
	/// <summary>
	/// Summary description for OptionsDialog.
	/// </summary>
	public class OptionsDialog : System.Windows.Forms.Form
	{
		/*
		 * Member Variables
		 */

		private System.Windows.Forms.MainMenu mainMenu1;
		private System.Windows.Forms.Label label1;
		private System.Windows.Forms.TextBox registryBox;
		private System.Windows.Forms.Label label2;
		private System.Windows.Forms.NumericUpDown recentServerNum;
		private System.Windows.Forms.Button saveButton;
		private System.Windows.Forms.Button cancelButton;
	
		public OptionsDialog()
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
			this.mainMenu1 = new System.Windows.Forms.MainMenu();
			this.label1 = new System.Windows.Forms.Label();
			this.registryBox = new System.Windows.Forms.TextBox();
			this.label2 = new System.Windows.Forms.Label();
			this.recentServerNum = new System.Windows.Forms.NumericUpDown();
			this.saveButton = new System.Windows.Forms.Button();
			this.cancelButton = new System.Windows.Forms.Button();
			// 
			// label1
			// 
			this.label1.Location = new System.Drawing.Point(8, 8);
			this.label1.Size = new System.Drawing.Size(96, 16);
			this.label1.Text = "Registry File:";
			// 
			// registryBox
			// 
			this.registryBox.Location = new System.Drawing.Point(8, 27);
			this.registryBox.Size = new System.Drawing.Size(224, 22);
			this.registryBox.Text = "";
			// 
			// label2
			// 
			this.label2.Font = new System.Drawing.Font("Tahoma", 9F, System.Drawing.FontStyle.Regular);
			this.label2.Location = new System.Drawing.Point(8, 67);
			this.label2.Size = new System.Drawing.Size(160, 16);
			this.label2.Text = "Number of Recent Servers:";
			// 
			// recentServerNum
			// 
			this.recentServerNum.Location = new System.Drawing.Point(168, 64);
			this.recentServerNum.Maximum = new System.Decimal(new int[] {
																			50,
																			0,
																			0,
																			0});
			this.recentServerNum.Size = new System.Drawing.Size(64, 20);
			this.recentServerNum.Value = new System.Decimal(new int[] {
																		  10,
																		  0,
																		  0,
																		  0});
			// 
			// saveButton
			// 
			this.saveButton.Location = new System.Drawing.Point(144, 232);
			this.saveButton.Size = new System.Drawing.Size(80, 24);
			this.saveButton.Text = "Save";
			this.saveButton.Click += new System.EventHandler(this.saveButton_Click);
			// 
			// cancelButton
			// 
			this.cancelButton.Font = new System.Drawing.Font("Tahoma", 9F, System.Drawing.FontStyle.Regular);
			this.cancelButton.Location = new System.Drawing.Point(56, 232);
			this.cancelButton.Size = new System.Drawing.Size(80, 24);
			this.cancelButton.Text = "Cancel";
			this.cancelButton.Click += new System.EventHandler(this.cancelButton_Click);
			// 
			// OptionsDialog
			// 
			this.ControlBox = false;
			this.Controls.Add(this.cancelButton);
			this.Controls.Add(this.saveButton);
			this.Controls.Add(this.recentServerNum);
			this.Controls.Add(this.label2);
			this.Controls.Add(this.registryBox);
			this.Controls.Add(this.label1);
			this.Menu = this.mainMenu1;
			this.Text = "PUC Options";
			this.Load += new System.EventHandler(this.OptionsDialog_Load);

		}
		#endregion

		private void OptionsDialog_Load(object sender, System.EventArgs e)
		{
			registryBox.Text = Globals.GetWidgetRegistryFileName();
			recentServerNum.Value = Globals.GetRecentServerMaximum();
		}

		private void cancelButton_Click(object sender, System.EventArgs e)
		{
			this.Close();
		}

		private void saveButton_Click(object sender, System.EventArgs e)
		{
			DataStore store = Globals.GetDataStore();

			int recentCount = store.GetIntData( Globals.RECENT_COUNT_ATTR );

			store.Set( Globals.REGISTRY_FILE_ATTR, registryBox.Text );
			store.Set( Globals.RECENT_SERVER_ATTR, (int)recentServerNum.Value );

			if ( recentCount > recentServerNum.Value )
				store.Set( Globals.RECENT_COUNT_ATTR, (int)recentServerNum.Value );

			this.Close();
		}
	}
}
