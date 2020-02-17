using System;
using System.Windows.Forms;

using PUC;
using PUC.CIO;
using PUC.Communication;


namespace PUC.CIO.GenericSmartCIO
{
	/// <summary>
	/// Summary description for TimeDurationSmartCIO.
	/// </summary>
	public class GenericSmartCIO : SmartCIO
	{
		/*
		 * Constants
		 */

		public string HIGH_LEVEL_TYPE = "time/duration";

		public const int DEFAULT_HEIGHT = 22;
		public const int MINIMUM_LEFT_PAD = 3;
		public const int MINIMUM_RIGHT_PAD = 3;
		public const int MINIMUM_TOP_PAD = 3;
		public const int MINIMUM_BOTTOM_PAD = 5;


		/*
		 * Static Methods (for Dynamic Class Loading)
		 */

		public static SmartCIO CreateGenericSmartCIO( PUC.GroupNode group )
		{
			return new GenericSmartCIO( group );
		}


		/*
		 * Member Variables
		 */

		protected ApplianceState _state;


		/*
		 * Constructor
		 */

		public GenericSmartCIO( GroupNode specSnippet )
			: base( new TextBox(), specSnippet )
		{
			if ( specSnippet.IsObject() )
			{
				_state = (ApplianceState)((ObjectGroupNode)specSnippet).Object;

				_state.LabelChangedEvent += new ApplianceObject.LabelChangedHandler(this.LabelChanged);
				_state.EnableChangedEvent += new ApplianceObject.EnableChangedHandler(this.EnableChanged);
				_state.TypeChangedEvent += new ApplianceState.TypeChangedHandler(this.TypeChanged);
				_state.ValueChangedEvent += new ApplianceState.ValueChangedHandler(this.ValueChanged);

				((TextBox)GetControl()).TextChanged += new EventHandler(this.TextChanged);

				refreshDisplay();
			}
		}


		/*
		 * Properties
		 */


		/*
		 * Member Methods
		 */

		public override string GetStringValue()
		{
			return null;
		}

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

		public void TextChanged( object source, EventArgs a )
		{
			string stateText = null;
			if ( _state.Defined )
				stateText = _state.Value.ToString();
			string fieldText = GetControl().Text;

			if ( stateText != null && stateText == fieldText ) return;

			_state.RequestChange( fieldText );
		}


		/*
		 * Member Methods
		 */

		protected void refreshDisplay()
		{
			string currentText = GetControl().Text;
			string stateText = "";
				
			if ( _state != null && _state.Defined )
				stateText = _state.Value.ToString();

			if ( currentText != stateText )
				GetControl().Text = stateText;
		}

		public override bool HasLabel()
		{
			return _state != null && _state.Labels != null;
		}

		public override LabelCIO GetLabelCIO()
		{
			if ( HasLabel() )
				return new LabelCIO( _state.Labels );

			return null;
		}

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
			System.Drawing.SizeF s = Globals.MeasureString( "Test String", GetControl().Font );	

			return new System.Drawing.Point( MINIMUM_LEFT_PAD, MINIMUM_TOP_PAD + (int)s.Height );
		}

		public override void FinalSizeNotify()
		{
			refreshDisplay();
		}
	}
}
