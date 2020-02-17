using System;
using System.Collections;
using System.Windows.Forms;

#if SMARTPHONE
using PhoneControls;
#endif

using PUC;
using PUC.Communication;

namespace PUC.CIO
{
	/// <summary>
	/// Summary description for ScrollbarLinkedCIO.
	/// </summary>
	public class ScrollbarLinkedCIO : StateLinkedCIO
	{
		/*
		 * Constants
		 */

		public const string CIO_NAME = "ScrollbarLinkedCIO";

		public const int MINIMUM_HEIGHT = 15;
		public const int DEFAULT_HEIGHT = 20;
		public const int MINIMUM_WIDTH  = 30;


		/*
		 * Dynamic Loading Code
		 */

		public static ConcreteInteractionObject CreateScrollbarLinkedCIO( ApplianceObject ao )
		{
			return new ScrollbarLinkedCIO( ao );
		}


		/*
		 * Member Variables
		 */

		/*
		 * This CIO should be changed such that the scrollbar always ranges from 0 to 
		 * the number of values it represents.  Values should be stored that can be 
		 * used to change the scrollbar value (which is effectively an index into the 
		 * possible values) into the actual value for transmitting to the server.
		 */

		protected double    _minimum;
		protected double    _increment;
		protected int       _maximum;

		protected Hashtable _sentValues;


		/*
		 * Constructor
		 */
		public ScrollbarLinkedCIO( ApplianceObject ao )
			: base( ao,
#if SMARTPHONE
			new PhoneControls.HInteractivePhoneBar() )
#else
			new System.Windows.Forms.HScrollBar() )
#endif
		{
			ApplianceState state = (ApplianceState)ao;

			_minimum = 0;
			_increment = 1;
			_maximum = 1;

			_sentValues = new Hashtable();

			state.LabelChangedEvent += new ApplianceObject.LabelChangedHandler(this.LabelChanged);
			state.EnableChangedEvent += new ApplianceObject.EnableChangedHandler(this.EnableChanged);
			state.TypeChangedEvent += new ApplianceState.TypeChangedHandler(this.TypeChanged);
			state.ValueChangedEvent += new ApplianceState.ValueChangedHandler(this.ValueChanged);

#if SMARTPHONE
			((HInteractivePhoneBar)GetControl()).ValueChanged
#else
			((ScrollBar)GetControl()).ValueChanged 
#endif
				+= new EventHandler(this.BarValueChanged);

			TypeChanged( state );
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
			if ( _maximum >= 0 )
				GetControl().Enabled = ao.Enabled;
		}

		public void TypeChanged( ApplianceState state )
		{
			if ( state.Type.ValueSpace is PUC.Types.IntegerSpace )
			{
				PUC.Types.IntegerSpace intspc = 
					(PUC.Types.IntegerSpace)state.Type.ValueSpace;

				_minimum = intspc.GetMinimum().GetDoubleValue();
				_maximum = intspc.GetMaximum().GetIntValue() - 
					intspc.GetMinimum().GetIntValue();
				
				if ( intspc.IsIncremented() )
				{
					_increment = intspc.GetIncrement().GetDoubleValue();
					_maximum = _maximum / intspc.GetIncrement().GetIntValue();
				}
			}
			else if ( state.Type.ValueSpace is PUC.Types.FixedPtSpace )
			{
				PUC.Types.FixedPtSpace fps = 
					(PUC.Types.FixedPtSpace)state.Type.ValueSpace;

				_minimum = fps.GetMinimum().GetDoubleValue();

				double max = fps.GetMaximum().GetDoubleValue() - 
					fps.GetMinimum().GetDoubleValue();
				
				if ( fps.IsIncremented() )
				{
					_increment = fps.GetIncrement().GetDoubleValue();
					_maximum = (int)( max / _increment );
				}
				else
				{
					_increment = 1d / Math.Pow( 10, fps.GetPointPosition() ); 
					_maximum = (int)( max * Math.Pow( 10, fps.GetPointPosition() ) );
				}
			}

			refreshDisplay();
		}

		public void ValueChanged( ApplianceState state )
		{
			if ( !state.Defined )
				return;

			string val = state.Value.ToString();

			if ( _sentValues[ val ] != null && 
				 ((int)_sentValues[ val ]) > 0 )
			{
				int cnt = (int)_sentValues[ val ];
				_sentValues[ val ] = --cnt;
			}
			else
				refreshDisplay();
		}

		public void BarValueChanged( object source, EventArgs a )
		{
			double val = (((double)
#if SMARTPHONE
				((HInteractivePhoneBar)GetControl()).Value)
#else
				((ScrollBar)GetControl()).Value) 
#endif
				* _increment) + _minimum;
			string valstr = val.ToString();

			/*
			if ( ((ApplianceState)GetApplObj()).Type.ValueSpace is PUC.Types.IntegerSpace )
				valstr = ((int)val).ToString();
			else
				valstr = val.ToString();
			*/
			string stateval;

			if ( ((ApplianceState)_applObj).Defined )
				stateval = ((ApplianceState)_applObj).Value.ToString();
			else 
				stateval = "";

			// don't echo state change requests for notifications
			if ( valstr == stateval ) return;
 
			// keep track of the value that was sent
			if ( _sentValues[ valstr ] != null )
			{
				int cnt = (int)_sentValues[ valstr ];
				_sentValues[ valstr ] = ++cnt;
			}
			else
				_sentValues[ valstr ] = 1;

			// send the value
			((ApplianceState)GetApplObj()).RequestChange( valstr );
		}


		/*
		 * Member Methods
		 */

		public override void ResetCache()
		{
			base.ResetCache ();

			_sentValues.Clear();
		}

		protected void refreshDisplay()
		{
			ApplianceState state = (ApplianceState)GetApplObj();

			int val = 0;
			object stateval = state.Value;

			if ( state.Type.ValueSpace is PUC.Types.IntegerSpace )
			{
				if ( state.Defined )
					val = ((int)stateval - (int)_minimum ) / (int)_increment;
			}
			else if ( state.Type.ValueSpace is PUC.Types.FixedPtSpace )
			{
				if ( state.Defined )
					val = (int)Math.Round( ( (double)stateval - _minimum )  / _increment );
			}
			else
				return;

#if SMARTPHONE
			HInteractivePhoneBar s = (HInteractivePhoneBar)GetControl();
#else
			ScrollBar s = (ScrollBar)GetControl();
#endif

			// s.Enabled = true;
			s.Minimum = 0;
			
			if ( _maximum < 0 )
			{
				s.Maximum = 100;
				s.Enabled = false;
			}
			else
			{
				s.Maximum = _maximum;
				
				if ( s.SmallChange < 1 )
					s.SmallChange = 1;
				
				int lrgChng = (int)(((double)_maximum) * 0.05);
				if ( lrgChng < 1 )
					s.LargeChange = s.SmallChange * 2;
				else
					s.LargeChange = lrgChng;

				// make sure the user can set the bar to its maximum
				s.Maximum = _maximum + s.LargeChange - 1; 
				s.Value   = val;
				s.Enabled = state.Enabled;
			}
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
			return new System.Drawing.Size( MINIMUM_WIDTH, MINIMUM_HEIGHT );
		}

		public override PUC.Layout.PreferredSize GetPreferredSize()
		{
			return new PUC.Layout.PreferredSize( MINIMUM_WIDTH, DEFAULT_HEIGHT );
		}

		public override System.Drawing.Point GetControlOffset()
		{
			// this assumes strings of height 14
			return new System.Drawing.Point( 0, DEFAULT_HEIGHT - 3 );
		}

		public override void FinalSizeNotify()
		{
			refreshDisplay();
		}

	}
}
