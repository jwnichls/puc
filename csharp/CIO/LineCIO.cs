using System;
using System.Windows.Forms;
using PUC.CIO;

namespace PUC.CIO
{
	/// <summary>
	/// Summary description for LineCIO.
	/// </summary>
	public class LineCIO : IndependentCIO
	{
		public LineCIO() : base( new LineControl() )
		{
		}

		public override System.Drawing.Point GetControlOffset()
		{
			return new System.Drawing.Point( 0, 0 );
		}

		public override System.Drawing.Size GetMinimumSize()
		{
			return new System.Drawing.Size( 1, 1 );
		}

		public override PUC.Layout.PreferredSize GetPreferredSize()
		{
			return new PUC.Layout.PreferredSize( GetMinimumSize() );
		}

		public override void FinalSizeNotify()
		{
			return;
		}
	}

	public class LineControl : Control
	{
		protected override void OnPaint(PaintEventArgs e)
		{
			base.OnPaint(e);

			e.Graphics.DrawLine( new System.Drawing.Pen( System.Drawing.Color.Black ), 
								 0, 0, this.Size.Width - 1, this.Size.Height - 1 );
		}
	}
}
