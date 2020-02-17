using System;
using System.Collections;
using System.Windows.Forms;

using PUC;
using PUC.Communication;

namespace PUC.CIO
{
	/// <summary>
	/// Summary description for TextFieldLinkedCIO.
	/// </summary>
	public class TextFieldLinkedCIO : StateLinkedCIO
	{
		/*
		 * Constants
		 */

		public const string CIO_NAME = "TextFieldLinkedCIO";

		public const int DEFAULT_HEIGHT = 22;
		public const int MINIMUM_LEFT_PAD = 3;
		public const int MINIMUM_RIGHT_PAD = 3;
		public const int MINIMUM_TOP_PAD = 3;
		public const int MINIMUM_BOTTOM_PAD = 5;
		public const int BETWEEN_LINES_PAD = 3; // TODO: determine this size


		/*
		 * Dynamic Loading Code
		 */

		public static ConcreteInteractionObject CreateTextFieldLinkedCIO( ApplianceObject ao )
		{
			return new TextFieldLinkedCIO( ao );
		}


		/*
		 * Member Variables
		 */

		protected Hashtable _sentValues;


		/*
		 * Constructor
		 */
		public TextFieldLinkedCIO( ApplianceObject ao )
			: base( ao, new System.Windows.Forms.TextBox() )
		{
			ApplianceState state = (ApplianceState)GetApplObj();

			_sentValues = new Hashtable();

			state.LabelChangedEvent += new ApplianceObject.LabelChangedHandler(this.LabelChanged);
			state.EnableChangedEvent += new ApplianceObject.EnableChangedHandler(this.EnableChanged);
			state.TypeChangedEvent += new ApplianceState.TypeChangedHandler(this.TypeChanged);
			state.ValueChangedEvent += new ApplianceState.ValueChangedHandler(this.ValueChanged);

			((TextBox)GetControl()).TextChanged += new EventHandler(this.TextChanged);

			refreshType();
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
			refreshType();
		}

		public void ValueChanged( ApplianceState state )
		{
			if ( !state.Defined )
				return;

			object objval = state.Value;

			string val;
			if ( objval != null )
				val = state.Value.ToString();
			else
				val = "";

			if ( _sentValues[ val ] != null && 
 				 ((int)_sentValues[ val ]) > 0 )
			{
				int cnt = (int)_sentValues[ val ];
				_sentValues[ val ] = --cnt;
			}
			else
				refreshDisplay();
		}

		public void TextChanged( object source, EventArgs a )
		{
			string stateText = null;
			if ( ((ApplianceState)GetApplObj()).Defined )
				stateText = ((ApplianceState)GetApplObj()).Value.ToString();
			string fieldText = GetControl().Text;

			if ( stateText == null )
				stateText = "";

			if ( stateText == fieldText ) return;

			// keep track of the value that was sent
			if ( _sentValues[ fieldText ] != null )
			{
				int cnt = (int)_sentValues[ fieldText ];
				_sentValues[ fieldText ] = ++cnt;
			}
			else
				_sentValues[ fieldText ] = 1;

			((ApplianceState)GetApplObj()).RequestChange( fieldText );
		}


		/*
		 * Member Methods
		 */

		public override void ResetCache()
		{
			base.ResetCache();

			_sentValues.Clear();
		}


		protected void refreshType()
		{
			ApplianceState state = (ApplianceState)GetApplObj();
			TextBox box = (TextBox)GetControl();

			if ( state.Type.ValueSpace is PUC.Types.StringSpace )
			{
				PUC.Types.StringSpace s = (PUC.Types.StringSpace)state.Type.ValueSpace;

				if ( s.MaximumChars != null )
					box.MaxLength = s.MaximumChars.GetIntValue();
			}

			// This method only needs to be called when the variable
			// becomes undefined, because no ValueChanged event happens
			// in those situations.
			if ( !state.Defined )
				refreshDisplay();
		}

		protected void refreshDisplay()
		{
			ApplianceState state = (ApplianceState)GetApplObj();

			string currentText = GetControl().Text;
			string stateText = "";
				
			if ( ((ApplianceState)GetApplObj()).Defined )
				stateText = ((ApplianceState)GetApplObj()).Value.ToString();

			if ( currentText != stateText )
				GetControl().Text = stateText;
		}

		public override bool HasLabel()
		{
			return GetApplObj().Labels != null;
		}

		public override LabelCIO GetLabelCIO()
		{
			if ( HasLabel() )
				return new LabelCIO( GetApplObj().Labels );

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

			if ( ((TextBox)GetControl()).Multiline )
				h *= 3; // arbitrarily assume we'll increase to three line height
			else if ( DEFAULT_HEIGHT > h )
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
