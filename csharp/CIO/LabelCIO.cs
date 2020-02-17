using System;
using System.Windows.Forms;
using PUC;

namespace PUC.CIO
{
	/// <summary>
	/// Summary description for LabelCIO.
	/// </summary>
	public class LabelCIO : IndependentCIO
	{
		/*
		 * Member Variables
		 */
		
		protected LabelDictionary _labels;


		/*
		* Constructor
		*/
		public LabelCIO( LabelDictionary labels ) : base( new Label() )
		{
			_labels = labels;

			GetControl().Text = _labels.GetShortestLabel();
		}


		/*
		 * Member Methods
		 */

		public void UseMinimumLabel()
		{
			((Label)GetControl()).Text = _labels.GetShortestLabel();
		}

		public void SetAlignment( System.Drawing.ContentAlignment orient )
		{
			((Label)GetControl()).TextAlign = orient;
		}

		public void SetLabelText()
		{
			string label = null;

			Control c = this.GetControl();

			try 
			{
				label = _labels.GetLabelByPixelLength( c.Font, 
													   c.ClientSize.Width ) + ":";
			}
			catch( Exception )
			{
				label = _labels.GetShortestLabel() + ":";
            }

			c.Text = label;
		}

		public override System.Drawing.Size GetMinimumSize()
		{
			System.Drawing.SizeF s = Globals.MeasureString( _labels.GetShortestLabel(), GetControl().Font );	

			int w = (int)s.Width;
			int h = (int)s.Height;

			return new System.Drawing.Size( w, h );
		}

		public override PUC.Layout.PreferredSize GetPreferredSize()
		{
			System.Drawing.SizeF s = Globals.MeasureString( _labels.GetFirstLabel(), GetControl().Font );	

			int w = (int)s.Width;
			int h = (int)s.Height;

			return new PUC.Layout.PreferredSize( w, h );
		}

		public override System.Drawing.Point GetControlOffset()
		{
			System.Drawing.SizeF s = Globals.MeasureString( GetControl().Text, GetControl().Font );	

			return new System.Drawing.Point( 0, (int)s.Height );
		}

		public override void FinalSizeNotify()
		{
			SetLabelText();
		}

	}
}
