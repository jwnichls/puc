using System;
using System.Windows.Forms;
using PUC;
using PUC.Communication;

namespace PUC.CIO
{
	/// <summary>
	/// Summary description for LabelLinkedCIO.
	/// </summary>
	public class LabelLinkedCIO : StateLinkedCIO
	{
		/*
		 * Constants
		 */
		
		public static string CIO_NAME = "LabelLinkedCIO";


		/*
		 * Dynamic Loading Code
		 */

		public static ConcreteInteractionObject CreateLabelLinkedCIO( ApplianceObject ao )
		{
			return new LabelLinkedCIO( ao );
		}


		/*
		 * Member Variables
		 */

		/*
		 * Constructor
		 */

		public LabelLinkedCIO( ApplianceObject ao )
			: base( ao, new Label() )
		{
			ApplianceState state = (ApplianceState)ao;
			
			state.EnableChangedEvent += new ApplianceObject.EnableChangedHandler(this.EnableChanged);
			state.LabelChangedEvent += new ApplianceObject.LabelChangedHandler(this.LabelChanged);
			state.TypeChangedEvent += new ApplianceState.TypeChangedHandler(this.TypeChanged);
			state.ValueChangedEvent += new ApplianceState.ValueChangedHandler(this.ValueChanged);

			refreshDisplay();
		}


		/*
		 * Event Handlers
		 */

		public void LabelChanged( ApplianceObject ao )
		{
			refreshDisplay();
		}

		public void EnableChanged( ApplianceObject ao )
		{
			GetControl().Enabled = ao.Enabled;
		}

		public void TypeChanged( ApplianceState state )
		{
			refreshDisplay();
		}

		public void ValueChanged( ApplianceState state )
		{
			refreshDisplay();
		}


		/*
		 * Member Methods
		 */

		protected void refreshDisplay()
		{
			ApplianceState state = (ApplianceState)GetApplObj();
			
			string lbl;

			if ( !state.Defined )
			{
				lbl = "";
			}
			else if ( state.Type.ValueLabels == null )
			{
				lbl = state.Value.ToString();
			}
			else
			{
				// find the right label library
				LabelDictionary labels = (LabelDictionary)state.Type.ValueLabels[ state.Value ];

				if ( labels == null )
					lbl = state.Value.ToString();
				else
					try 
					{
						lbl = labels.GetLabelByPixelLength( GetControl().Font, GetControl().ClientSize.Width );
					}
					catch( Exception )
					{
						lbl = labels.GetShortestLabel();
					}
			}

			GetControl().Text = lbl;
		}

		public override bool HasLabel()
		{
			return true;
		}

		public override LabelCIO GetLabelCIO()
		{
			if ( GetApplObj().Labels != null )
				return new LabelCIO( GetApplObj().Labels );
			else 
				return null;
		}

		public override System.Drawing.Size GetMinimumSize()
		{
			// TODO: Should use the biggest string here, instead using a sample
			System.Drawing.SizeF s = Globals.MeasureString( "Sample", GetControl().Font );	

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
			// TODO: Should use the biggest string here, instead using a sample
			System.Drawing.SizeF s = Globals.MeasureString( "Sample", GetControl().Font );	

			return new System.Drawing.Point( 0, (int)s.Height );
		}

		public override void FinalSizeNotify()
		{
			refreshDisplay();
		}

	}
}
