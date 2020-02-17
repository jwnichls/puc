using System;
using System.Windows.Forms;
using PUC;
using PUC.Communication;

namespace PUC.CIO
{
	/// <summary>
	/// Summary description for ButtonLinkedCIO.
	/// </summary>
	public class ButtonLinkedCIO : StateLinkedCIO
	{
		/*
		 * Constants
		 */

		public const string CIO_NAME = "ButtonLinkedCIO";

		public const int DEFAULT_HEIGHT = 24;
		public const int MINIMUM_LEFT_PAD = 5;
		public const int MINIMUM_RIGHT_PAD = 5;
		public const int MINIMUM_TOP_PAD = 3;
		public const int MINIMUM_BOTTOM_PAD = 3;

		/* Ascent line at TOP_PAD.  Label is centered within button,
		 * but horizontally and vertically.
		 */


		/*
		 * Dynamic Loading Code
		 */

		public static ConcreteInteractionObject CreateButtonLinkedCIO( ApplianceObject ao )
		{
			return new ButtonLinkedCIO( ao );
		}


		/*
		 * Constructor
		 */
		public ButtonLinkedCIO( ApplianceObject ao )
			: base( ao, new System.Windows.Forms.Button() )
		{
			GetControl().Font = new System.Drawing.Font( GetControl().Font.Name, GetControl().Font.Size, System.Drawing.FontStyle.Regular );

			ao.LabelChangedEvent += new ApplianceObject.LabelChangedHandler(this.LabelChanged);
			ao.EnableChangedEvent += new ApplianceObject.EnableChangedHandler(this.EnableChanged);

			if ( ao is ApplianceState )
			{
				ApplianceState state = (ApplianceState)ao;

				state.TypeChangedEvent += new ApplianceState.TypeChangedHandler(this.TypeChanged);
				state.ValueChangedEvent += new ApplianceState.ValueChangedHandler(this.ValueChanged);

				GetControl().Click += new EventHandler(this.StateClick);
			}
			else
			{
				GetControl().Click += new EventHandler(this.CommandClick);
			}

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

		public void CommandClick( object sender, EventArgs a )
		{
			((ApplianceCommand)GetApplObj()).InvokeCommand();
		}

		public void StateClick( object sender, EventArgs a )
		{
			ApplianceState state = (ApplianceState)GetApplObj();

			if ( state.Defined )
				return;

			if ( state.Type.ValueSpace is PUC.Types.BooleanSpace )
			{
				// HACK: hopefully the variable is never undefined
				bool newValue = false;
				if ( state.Defined )
					newValue = !(bool)state.Value; 

				state.RequestChange( newValue );
			}
		}


		/*
		 * Member Methods
		 */

		public override System.Drawing.Size GetMinimumSize()
		{
			System.Drawing.SizeF s = Globals.MeasureString( GetControl().Text, GetControl().Font );	

			int w = (int)s.Width + MINIMUM_RIGHT_PAD + MINIMUM_LEFT_PAD;
			int h = (int)s.Height + MINIMUM_TOP_PAD + MINIMUM_BOTTOM_PAD;

			return new System.Drawing.Size( w, h );
		}

		public override PUC.Layout.PreferredSize GetPreferredSize()
		{
			System.Drawing.SizeF s = Globals.MeasureString( GetControl().Text, GetControl().Font );	

			int w = (int)s.Width + 2*MINIMUM_RIGHT_PAD + 2*MINIMUM_LEFT_PAD;
			int h = (int)s.Height + MINIMUM_TOP_PAD + MINIMUM_BOTTOM_PAD;
			if ( DEFAULT_HEIGHT > h )
				h = DEFAULT_HEIGHT;

			return new PUC.Layout.PreferredSize( w, h );
		}

		public override System.Drawing.Point GetControlOffset()
		{
			System.Drawing.SizeF s = Globals.MeasureString( GetControl().Text, GetControl().Font );	

			int xoffset = ( GetControl().Size.Width - (int)s.Width ) / 2;
			int yoffset = ( GetControl().Size.Height - (int)s.Height ) / 2 + (int)s.Height - 1;

			return new System.Drawing.Point( xoffset, yoffset );
		}

		protected void refreshDisplay()
		{
			string lbl;

			try
			{
				lbl = GetApplObj().Labels.GetLabelByPixelLength( GetControl().Font, GetControl().Size.Width - MINIMUM_LEFT_PAD - MINIMUM_RIGHT_PAD );
			}
			catch( Exception )
			{
				lbl = GetApplObj().Labels.GetShortestLabel();
			}

			GetControl().Text = lbl;
		}

		public override void FinalSizeNotify()
		{
			refreshDisplay();
		}


	}
}
