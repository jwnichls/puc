using System;
using System.Drawing;
using System.Collections;
using System.ComponentModel;
using System.Windows.Forms;
using PUC;
using PUC.Communication;
using PUC.Types;

namespace DebugServer
{
	/// <summary>
	/// Summary description for UndefineDlg.
	/// </summary>
	public class UndefineDlg : System.Windows.Forms.Form
	{
		/// <summary>
		/// Required designer variable.
		/// </summary>
		private System.ComponentModel.Container components = null;

		protected DebugServerFrame _frame;
		private System.Windows.Forms.Button undefButton;
		private System.Windows.Forms.Button cancelButton;
		private System.Windows.Forms.Panel boxPanel;
		protected ArrayList _boxes;

		public UndefineDlg( DebugServerFrame frame )
		{
			//
			// Required for Windows Form Designer support
			//
			InitializeComponent();

			_frame = frame;
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
			this.undefButton = new System.Windows.Forms.Button();
			this.cancelButton = new System.Windows.Forms.Button();
			this.boxPanel = new System.Windows.Forms.Panel();
			this.SuspendLayout();
			// 
			// undefButton
			// 
			this.undefButton.Location = new System.Drawing.Point(128, 8);
			this.undefButton.Name = "undefButton";
			this.undefButton.Size = new System.Drawing.Size(112, 24);
			this.undefButton.TabIndex = 0;
			this.undefButton.Text = "Undefine";
			this.undefButton.Click += new System.EventHandler(this.undefButton_Click);
			// 
			// cancelButton
			// 
			this.cancelButton.Location = new System.Drawing.Point(8, 8);
			this.cancelButton.Name = "cancelButton";
			this.cancelButton.Size = new System.Drawing.Size(112, 24);
			this.cancelButton.TabIndex = 1;
			this.cancelButton.Text = "Cancel";
			this.cancelButton.Click += new System.EventHandler(this.cancelButton_Click);
			// 
			// boxPanel
			// 
			this.boxPanel.AutoScroll = true;
			this.boxPanel.Location = new System.Drawing.Point(0, 43);
			this.boxPanel.Name = "boxPanel";
			this.boxPanel.Size = new System.Drawing.Size(248, 200);
			this.boxPanel.TabIndex = 2;
			// 
			// UndefineDlg
			// 
			this.AutoScaleBaseSize = new System.Drawing.Size(5, 13);
			this.ClientSize = new System.Drawing.Size(248, 243);
			this.Controls.Add(this.boxPanel);
			this.Controls.Add(this.cancelButton);
			this.Controls.Add(this.undefButton);
			this.Name = "UndefineDlg";
			this.Text = "UndefineDlg";
			this.Resize += new System.EventHandler(this.UndefineDlg_Resize);
			this.Load += new System.EventHandler(this.UndefineDlg_Load);
			this.Closed += new System.EventHandler(this.UndefineDlg_Closed);
			this.ResumeLayout(false);

		}
		#endregion

		private void UndefineDlg_Load(object sender, System.EventArgs e)
		{
			int currentY = 8;

			_boxes = new ArrayList();

			Appliance a = _frame.GetAppliance();
			IEnumerator en = a.VariableTable.GetObjectEnumerator();
			while( en.MoveNext() )
			{
				if ( ((ApplianceObject)en.Current).State )
				{
					ApplianceState state = (ApplianceState)en.Current;
					CheckBox chbx = new CheckBox();
					chbx.Location = new System.Drawing.Point( 8, currentY );
					chbx.Size = new System.Drawing.Size( 208, 24 );
					chbx.Checked = false;
					chbx.Text = state.FullName;

					_boxes.Add( chbx );
					boxPanel.Controls.Add( chbx );

					currentY += 32;
				}
			}
		}

		private void cancelButton_Click(object sender, System.EventArgs e)
		{
			this.Close();
		}

		private void undefButton_Click(object sender, System.EventArgs e)
		{
			Appliance a = _frame.GetAppliance();

			IEnumerator en = _boxes.GetEnumerator();
			while( en.MoveNext() )
			{
				CheckBox chbx = (CheckBox)en.Current;
				if ( chbx.Checked )
				{
					// ((ApplianceState)objs[ chbx.Text ]).Undefine();
					StateChangeNotification msg = new StateChangeNotification( chbx.Text );
					_frame.SendAll( msg );
				}
			}

			this.Close();
		}

		private void UndefineDlg_Closed(object sender, System.EventArgs e)
		{
			boxPanel.Controls.Clear();
			_boxes.Clear();
		}

		private void UndefineDlg_Resize(object sender, System.EventArgs e)
		{
			boxPanel.Width = this.ClientSize.Width;

			IEnumerator en = _boxes.GetEnumerator();
			while( en.MoveNext() )
				((Control)en.Current).Width = boxPanel.Width - 48;
		}
	}
}
