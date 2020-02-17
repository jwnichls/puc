using System;
using System.Drawing;
using System.Collections;
using System.ComponentModel;
using System.Windows.Forms;
using PUC;
using PUC.PersistentData;

namespace DesktopPUC
{
	/// <summary>
	/// Summary description for DesktopOptionsDialog.
	/// </summary>
	public class DesktopOptionsDialog : System.Windows.Forms.Form
	{
		private System.Windows.Forms.Button saveButton;
		private System.Windows.Forms.Button cancelButton;
		private System.Windows.Forms.Label label1;
		private System.Windows.Forms.Label label2;
		private System.Windows.Forms.NumericUpDown recentServerNum;
		private System.Windows.Forms.TextBox registryBox;
		private System.Windows.Forms.Button browseButton;
		/// <summary>
		/// Required designer variable.
		/// </summary>
		private System.ComponentModel.Container components = null;

		public DesktopOptionsDialog()
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
			this.saveButton = new System.Windows.Forms.Button();
			this.cancelButton = new System.Windows.Forms.Button();
			this.label1 = new System.Windows.Forms.Label();
			this.label2 = new System.Windows.Forms.Label();
			this.recentServerNum = new System.Windows.Forms.NumericUpDown();
			this.registryBox = new System.Windows.Forms.TextBox();
			this.browseButton = new System.Windows.Forms.Button();
			((System.ComponentModel.ISupportInitialize)(this.recentServerNum)).BeginInit();
			this.SuspendLayout();
			// 
			// saveButton
			// 
			this.saveButton.Location = new System.Drawing.Point(256, 88);
			this.saveButton.Name = "saveButton";
			this.saveButton.Size = new System.Drawing.Size(80, 24);
			this.saveButton.TabIndex = 0;
			this.saveButton.Text = "Save";
			this.saveButton.Click += new System.EventHandler(this.saveButton_Click);
			// 
			// cancelButton
			// 
			this.cancelButton.Location = new System.Drawing.Point(168, 88);
			this.cancelButton.Name = "cancelButton";
			this.cancelButton.Size = new System.Drawing.Size(80, 24);
			this.cancelButton.TabIndex = 1;
			this.cancelButton.Text = "Cancel";
			this.cancelButton.Click += new System.EventHandler(this.cancelButton_Click);
			// 
			// label1
			// 
			this.label1.Location = new System.Drawing.Point(8, 19);
			this.label1.Name = "label1";
			this.label1.Size = new System.Drawing.Size(88, 16);
			this.label1.TabIndex = 2;
			this.label1.Text = "Registry File:";
			this.label1.TextAlign = System.Drawing.ContentAlignment.MiddleLeft;
			// 
			// label2
			// 
			this.label2.Location = new System.Drawing.Point(8, 48);
			this.label2.Name = "label2";
			this.label2.Size = new System.Drawing.Size(156, 17);
			this.label2.TabIndex = 3;
			this.label2.Text = "Number of Recent Servers:";
			this.label2.TextAlign = System.Drawing.ContentAlignment.MiddleLeft;
			// 
			// recentServerNum
			// 
			this.recentServerNum.Location = new System.Drawing.Point(160, 48);
			this.recentServerNum.Maximum = new System.Decimal(new int[] {
																			50,
																			0,
																			0,
																			0});
			this.recentServerNum.Name = "recentServerNum";
			this.recentServerNum.Size = new System.Drawing.Size(64, 20);
			this.recentServerNum.TabIndex = 4;
			this.recentServerNum.Value = new System.Decimal(new int[] {
																		  10,
																		  0,
																		  0,
																		  0});
			// 
			// registryBox
			// 
			this.registryBox.Location = new System.Drawing.Point(88, 16);
			this.registryBox.Name = "registryBox";
			this.registryBox.Size = new System.Drawing.Size(176, 20);
			this.registryBox.TabIndex = 5;
			this.registryBox.Text = "";
			// 
			// browseButton
			// 
			this.browseButton.Location = new System.Drawing.Point(264, 15);
			this.browseButton.Name = "browseButton";
			this.browseButton.Size = new System.Drawing.Size(72, 24);
			this.browseButton.TabIndex = 6;
			this.browseButton.Text = "Browse...";
			this.browseButton.Click += new System.EventHandler(this.browseButton_Click);
			// 
			// DesktopOptionsDialog
			// 
			this.AutoScaleBaseSize = new System.Drawing.Size(5, 13);
			this.ClientSize = new System.Drawing.Size(352, 126);
			this.Controls.Add(this.browseButton);
			this.Controls.Add(this.registryBox);
			this.Controls.Add(this.recentServerNum);
			this.Controls.Add(this.label2);
			this.Controls.Add(this.label1);
			this.Controls.Add(this.cancelButton);
			this.Controls.Add(this.saveButton);
			this.Name = "DesktopOptionsDialog";
			this.Text = "PUC Options";
			this.Load += new System.EventHandler(this.DesktopOptionsDialog_Load);
			((System.ComponentModel.ISupportInitialize)(this.recentServerNum)).EndInit();
			this.ResumeLayout(false);

		}
		#endregion

		private void DesktopOptionsDialog_Load(object sender, System.EventArgs e)
		{
			registryBox.Text = PUC.Globals.GetWidgetRegistryFileName();
			recentServerNum.Value = PUC.Globals.GetRecentServerMaximum();
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

		private void browseButton_Click(object sender, System.EventArgs e)
		{
			OpenFileDialog dlg = new OpenFileDialog();
			dlg.Filter = "xml files (*.xml)|*.xml|All files (*.*)|*.*";

			if ( dlg.ShowDialog() == DialogResult.OK )
			{
				registryBox.Text = dlg.FileName;
			}
		}
	}
}
