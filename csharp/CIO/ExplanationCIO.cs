using System;
using System.Windows.Forms;
using PUC;
using PUC.Communication;

namespace PUC.CIO
{
	/// <summary>
	/// Summary description for ExplanationCIO.
	/// </summary>
	public class ExplanationCIO : StateLinkedCIO
	{
		/*
		 * Constants
		 */
		
		public static string CIO_NAME = "ExplanationCIO";


		/*
		 * Dynamic Loading Code
		 */

		public static ConcreteInteractionObject CreateExplanationCIO( ApplianceObject ao )
		{
			return new ExplanationCIO( ao );
		}


		/*
		 * Member Variables
		 */

		
		/*
		 * Constructor
		 */

		public ExplanationCIO( ApplianceObject ao )
			: base( ao, new Label() )
		{
			((Label)GetControl()).TextAlign = System.Drawing.ContentAlignment.TopCenter;

			refreshDisplay();
		}


		/*
		 * Member Methods
		 */

		protected void refreshDisplay()
		{
			string lbl;

			try
			{
				lbl = GetApplObj().Labels.GetLabelByPixelLength( GetControl().Font, GetControl().ClientSize.Width );
			}
			catch( Exception )
			{
				lbl = GetApplObj().Labels.GetShortestLabel();
			}

			GetControl().Text = lbl;
		}

		public override bool PrefersFullWidth()
		{
			return true;
		}

		public override System.Drawing.Size GetMinimumSize()
		{
			System.Drawing.SizeF s = Globals.MeasureString( GetControl().Text, GetControl().Font );	

			int w = (int)s.Width;
			int h = (int)s.Height;

			return new System.Drawing.Size( w, h );
		}

		public override PUC.Layout.PreferredSize GetPreferredSize()
		{
			return new PUC.Layout.PreferredSize( GetMinimumSize() );
		}

		public override System.Drawing.Point GetControlOffset()
		{
			System.Drawing.SizeF s = Globals.MeasureString( GetControl().Text, GetControl().Font );	

			return new System.Drawing.Point( 0, (int)s.Height );
		}

		public override void FinalSizeNotify()
		{
			refreshDisplay();
		}

	}
}
