using System;
using System.Drawing;
using System.Collections;
using System.ComponentModel;
using System.Windows.Forms;

namespace DebugServer
{
	/// <summary>
	/// Summary description for ProgressBarDlg.
	/// </summary>
	public class ProgressBarDlg : System.Windows.Forms.Form
	{
		/*
		 * Member Variables
		 */
		
		private System.Windows.Forms.ProgressBar progressBar1;
		private System.Windows.Forms.Button okayBtn;
		private System.Windows.Forms.Button cancelBtn;
		/// <summary>
		/// Required designer variable.
		/// </summary>
		private System.ComponentModel.Container components = null;

		protected bool canceling = false;

	
		/*
		 * Events
		 */

		public event EventHandler CancelPressed;
		public event EventHandler OkayPressed;


		/*
		 * Constructor
		 */

		public ProgressBarDlg()
		{
			//
			// Required for Windows Form Designer support
			//
			InitializeComponent();
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
			this.progressBar1 = new System.Windows.Forms.ProgressBar();
			this.okayBtn = new System.Windows.Forms.Button();
			this.cancelBtn = new System.Windows.Forms.Button();
			this.SuspendLayout();
			// 
			// progressBar1
			// 
			this.progressBar1.Location = new System.Drawing.Point(8, 8);
			this.progressBar1.Name = "progressBar1";
			this.progressBar1.Size = new System.Drawing.Size(280, 24);
			this.progressBar1.TabIndex = 0;
			// 
			// okayBtn
			// 
			this.okayBtn.Enabled = false;
			this.okayBtn.Location = new System.Drawing.Point(208, 40);
			this.okayBtn.Name = "okayBtn";
			this.okayBtn.Size = new System.Drawing.Size(80, 24);
			this.okayBtn.TabIndex = 1;
			this.okayBtn.Text = "OK";
			this.okayBtn.Click += new System.EventHandler(this.okayBtn_Click);
			// 
			// cancelBtn
			// 
			this.cancelBtn.Location = new System.Drawing.Point(120, 40);
			this.cancelBtn.Name = "cancelBtn";
			this.cancelBtn.Size = new System.Drawing.Size(80, 24);
			this.cancelBtn.TabIndex = 1;
			this.cancelBtn.Text = "Cancel";
			this.cancelBtn.Click += new System.EventHandler(this.cancelBtn_Click);
			// 
			// ProgressBarDlg
			// 
			this.AutoScaleBaseSize = new System.Drawing.Size(5, 13);
			this.ClientSize = new System.Drawing.Size(296, 75);
			this.Controls.Add(this.okayBtn);
			this.Controls.Add(this.progressBar1);
			this.Controls.Add(this.cancelBtn);
			this.Name = "ProgressBarDlg";
			this.Text = "ProgressBarDlg";
			this.ResumeLayout(false);

		}
		#endregion

		/*
		 * Properties
		 */

		public bool Canceling
		{
			get
			{
				return canceling;
			}
		}

		public int Minimum
		{
			get
			{
				return progressBar1.Minimum;
			}
			set
			{
				progressBar1.Minimum = value;
			}
		}

		public int Maximum
		{
			get
			{
				return progressBar1.Maximum;
			}
			set
			{
				progressBar1.Maximum = value;
			}
		}

		public int Value
		{
			get
			{
				return progressBar1.Value;
			}
			set
			{
				progressBar1.Value = value;

				if ( value >= progressBar1.Maximum )
				{
					cancelBtn.Enabled = false;
					okayBtn.Enabled = true;
				}
				else
				{
					cancelBtn.Enabled = true;
					okayBtn.Enabled = false;
				}
			}
		}

		public int Step
		{
			get
			{
				return progressBar1.Step;
			}
			set
			{
				progressBar1.Step = value;
			}
		}

		/*
		 * Methods
		 */

		public void PerformStep()
		{
			progressBar1.PerformStep();
		}

		private void cancelBtn_Click(object sender, System.EventArgs e)
		{
			if ( CancelPressed != null )
				CancelPressed( this, e );
		}

		private void okayBtn_Click(object sender, System.EventArgs e)
		{
			if ( OkayPressed != null )
				OkayPressed( this, e );
		}
	}
}
