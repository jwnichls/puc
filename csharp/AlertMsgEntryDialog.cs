using System;
using System.Drawing;
using System.Collections;
using System.ComponentModel;
using System.Windows.Forms;

namespace DebugServer
{
	/// <summary>
	/// Summary description for AlertMsgEntryDialog.
	/// </summary>
	public class AlertMsgEntryDialog : System.Windows.Forms.Form
	{
		private System.Windows.Forms.Label label1;
		public System.Windows.Forms.TextBox msgBox;
		private System.Windows.Forms.Button sendButton;
		private System.Windows.Forms.Button cancelButton;
		/// <summary>
		/// Required designer variable.
		/// </summary>
		private System.ComponentModel.Container components = null;

		public AlertMsgEntryDialog()
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
			this.label1 = new System.Windows.Forms.Label();
			this.msgBox = new System.Windows.Forms.TextBox();
			this.sendButton = new System.Windows.Forms.Button();
			this.cancelButton = new System.Windows.Forms.Button();
			this.SuspendLayout();
			// 
			// label1
			// 
			this.label1.Location = new System.Drawing.Point(8, 18);
			this.label1.Name = "label1";
			this.label1.Size = new System.Drawing.Size(88, 24);
			this.label1.TabIndex = 0;
			this.label1.Text = "Enter Message:";
			this.label1.TextAlign = System.Drawing.ContentAlignment.TopRight;
			// 
			// msgBox
			// 
			this.msgBox.Location = new System.Drawing.Point(104, 15);
			this.msgBox.Name = "msgBox";
			this.msgBox.Size = new System.Drawing.Size(320, 20);
			this.msgBox.TabIndex = 0;
			this.msgBox.Text = "";
			// 
			// sendButton
			// 
			this.sendButton.DialogResult = System.Windows.Forms.DialogResult.OK;
			this.sendButton.Location = new System.Drawing.Point(328, 48);
			this.sendButton.Name = "sendButton";
			this.sendButton.Size = new System.Drawing.Size(88, 24);
			this.sendButton.TabIndex = 1;
			this.sendButton.Text = "Send";
			// 
			// cancelButton
			// 
			this.cancelButton.DialogResult = System.Windows.Forms.DialogResult.Cancel;
			this.cancelButton.Location = new System.Drawing.Point(232, 48);
			this.cancelButton.Name = "cancelButton";
			this.cancelButton.Size = new System.Drawing.Size(88, 24);
			this.cancelButton.TabIndex = 2;
			this.cancelButton.Text = "Cancel";
			// 
			// AlertMsgEntryDialog
			// 
			this.AutoScaleBaseSize = new System.Drawing.Size(5, 13);
			this.ClientSize = new System.Drawing.Size(440, 83);
			this.Controls.Add(this.cancelButton);
			this.Controls.Add(this.sendButton);
			this.Controls.Add(this.msgBox);
			this.Controls.Add(this.label1);
			this.Name = "AlertMsgEntryDialog";
			this.Text = "Send an Alert Message";
			this.ResumeLayout(false);

		}
		#endregion
	}
}
