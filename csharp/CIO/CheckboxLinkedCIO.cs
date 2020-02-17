using System;
using System.Windows.Forms;
using PUC;
using PUC.Communication;

namespace PUC.CIO
{
	/// <summary>
	/// Summary description for CheckboxLinkedCIO.
	/// </summary>
	public class CheckboxLinkedCIO : StateLinkedCIO
	{
		/*
		 * Constants
		 */
		
		public static string CIO_NAME = "CheckboxLinkedCIO";

		public const int DEFAULT_HEIGHT = 24;
		public const int MINIMUM_LEFT_PAD = 20;
		public const int MINIMUM_RIGHT_PAD = 0;
		public const int MINIMUM_TOP_PAD = 1;
		public const int MINIMUM_BOTTOM_PAD = 0;


		/*
		 * Dynamic Loading Code
		 */

		public static ConcreteInteractionObject CreateCheckboxLinkedCIO( ApplianceObject ao )
		{
			return new CheckboxLinkedCIO( ao );
		}


		/*
		 * Member Variables
		 */

		/*
		 * Constructor
		 */

		public CheckboxLinkedCIO( ApplianceObject ao )
			: base( ao, new CheckBox() )
		{
			ApplianceState state = (ApplianceState)ao;
			
			state.EnableChangedEvent += new ApplianceObject.EnableChangedHandler(this.EnableChanged);
			state.LabelChangedEvent += new ApplianceObject.LabelChangedHandler(this.LabelChanged);
			state.TypeChangedEvent += new ApplianceState.TypeChangedHandler(this.TypeChanged);
			state.ValueChangedEvent += new ApplianceState.ValueChangedHandler(this.ValueChanged);

			GetControl().Click += new EventHandler(this.StateClick);

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

		public void StateClick( object sender, EventArgs a )
		{
			ApplianceState state = (ApplianceState)GetApplObj();
			state.RequestChange( ((CheckBox)GetControl()).Checked );
		}


		/*
		 * Member Methods
		 */

		protected void refreshDisplay()
		{
			ApplianceState state = (ApplianceState)GetApplObj();
			string lbl;

			try
			{
				lbl = GetApplObj().Labels.GetLabelByPixelLength( GetControl().Font, GetControl().Size.Width - MINIMUM_LEFT_PAD - MINIMUM_RIGHT_PAD );
			}
			catch( Exception )
			{
#if DEBUGSVR
				if ( GetApplObj().Labels == null )
					lbl = GetApplObj().Name;
				else
#endif
				lbl = GetApplObj().Labels.GetShortestLabel();
			}

			GetControl().Text = lbl;

			if ( state.Defined )
				((CheckBox)GetControl()).Checked = (bool)state.Value;
			else
				((CheckBox)GetControl()).Checked = false;
		}

		public override System.Drawing.Size GetMinimumSize()
		{
			string text = GetControl().Text;

			if ( GetApplObj().Labels != null )
				text = GetApplObj().Labels.GetShortestLabel();

			System.Drawing.SizeF s = Globals.MeasureString( text, GetControl().Font );	

			int w = (int)s.Width + MINIMUM_RIGHT_PAD + MINIMUM_LEFT_PAD;
			int h = (int)s.Height + MINIMUM_TOP_PAD + MINIMUM_BOTTOM_PAD;

			return new System.Drawing.Size( w, h );
		}

		public override PUC.Layout.PreferredSize GetPreferredSize()
		{
			string text = GetControl().Text;

			if ( GetApplObj().Labels != null )
				text = GetApplObj().Labels.GetFirstLabel();

			System.Drawing.SizeF s = Globals.MeasureString( text, GetControl().Font );	

			int w = (int)s.Width + 2*MINIMUM_RIGHT_PAD + 2*MINIMUM_LEFT_PAD;
			int h = (int)s.Height + MINIMUM_TOP_PAD + MINIMUM_BOTTOM_PAD;
			if ( DEFAULT_HEIGHT > h )
				h = DEFAULT_HEIGHT;

			return new PUC.Layout.PreferredSize( w, h );
		}

		public override System.Drawing.Point GetControlOffset()
		{
			System.Drawing.SizeF s = Globals.MeasureString( GetControl().Text, GetControl().Font );	

			int xoffset = MINIMUM_LEFT_PAD;
			int yoffset = ( GetControl().Size.Height - (int)s.Height ) / 2 + (int)s.Height;

			return new System.Drawing.Point( xoffset, yoffset );
		}

		public override void FinalSizeNotify()
		{
			refreshDisplay();
		}

	}
}
