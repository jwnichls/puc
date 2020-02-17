using System;
using System.Collections;
using System.Windows.Forms;

using PocketPCControls;
#if SMARTPHONE
using PhoneControls;
#endif

using PUC;
using PUC.CIO;
using PUC.Communication;
using PUC.Types;


namespace PUC.CIO.TimeDuration
{
	/// <summary>
	/// Summary description for TimeDurationSmartCIO.
	/// </summary>
	public class TimeDurationSmartCIO : SmartCIO
	{
		/*
		 * Constants
		 */

		public string HIGH_LEVEL_TYPE = "time-duration";

		// constants for parsing multiple states
		protected string HOURS_LABEL        = "Hours";
		protected string MINUTES_LABEL      = "Minutes";
		protected string SECONDS_LABEL      = "Seconds";
		protected string FRACTION_LABEL		= "Fraction";

		// constants for judging TimeEditor layout
		public const int TB_DEFAULT_HEIGHT = 22;
		public const int TB_MINIMUM_LEFT_PAD = 3;
		public const int TB_MINIMUM_RIGHT_PAD = 3;
		public const int TB_MINIMUM_TOP_PAD = 3;
		public const int TB_MINIMUM_BOTTOM_PAD = 5;

		// constants for judging TimeSlider layout
		public const int SB_MINIMUM_HEIGHT = 15;
		public const int SB_DEFAULT_HEIGHT = 20;
		public const int SB_MINIMUM_WIDTH  = 30;



		/*
		 * Delegates
		 */

		/// <summary>
		/// A delegate used for setting the time of the control that is
		/// representing the time duration based on the value of the 
		/// time state variables.
		/// </summary>
		protected delegate void SetControlTime();
		protected delegate string GetStringMethod();
		

		/*
		 * Member Variables
		 */

		protected Control			_timeControl;
		protected SetControlTime	_setTime;
		protected GetStringMethod   _getString;

		protected TimeFormat[]      _format;

		// for preventing loops when using TimeSlider
		protected Hashtable _sentValues;


		/*
		 * Static Methods (for Dynamic Class Loading)
		 */

		public static SmartCIO CreateTimeDurationSmartCIO( PUC.GroupNode group )
		{
			if ( group.IsObject() )
			{
				ApplianceState state = (ApplianceState)((ObjectGroupNode)group).Object;

				if ( state.ReadOnly && state.ConstraintVariable )
					return null;
				else
					return new TimeDurationSmartCIO( group );
			}
			else
				return new TimeDurationSmartCIO( group );
		}

		
		/*
		 * Constructor
		 */

		public TimeDurationSmartCIO( GroupNode specSnippet )
			: base( new Panel(), specSnippet )
		{
			if ( _specSnippet.IsObject() )
			{
				// single state translations

				/*
				 * There are three possible translations if there is only one state.
				 * If the state is read-only, then the time will be displayed in a label.
				 * The state will be displayed as a TimeSlider if the state is editable,
				 * numeric and bounded.  Otherwise it will be displayed as a TimeEditor.
				 */

				ApplianceState state = (ApplianceState)_objects[ SINGLE_STATE ];
				PUC.Types.ValueSpace space = state.Type.ValueSpace;

				if ( space is PUC.Types.IntegerSpace )
					_getString = new GetStringMethod(this.GetStringFromInt);
				else if ( space is PUC.Types.StringSpace )
					_getString = new GetStringMethod(this.GetStringFromString);
				else if ( space is PUC.Types.FixedPtSpace || space is PUC.Types.FloatingPtSpace )
					_getString = new GetStringMethod(this.GetStringFromFloat);

				if ( state.ReadOnly )
				{
					_timeControl = new Label();

					if ( space is PUC.Types.IntegerSpace )
						_setTime = new SetControlTime(this.SetLabel);
					else if ( space is PUC.Types.StringSpace )
						_setTime = new SetControlTime(this.SetLabel);
					else 
						_setTime = new SetControlTime(this.SetLabel);
				}
				else
				{
					if ( space is PUC.Types.IntegerSpace )
					{
						IntegerSpace intspc = (IntegerSpace)space;

						if ( intspc.IsRanged() )
						{
							_timeControl = new TimeSlider();
							_setTime = new SetControlTime(this.SetSliderFromInt);
							((TimeSlider)_timeControl).TimeChanged += new EventHandler(this.IntSliderTimeChanged);
							_sentValues = new Hashtable();
						}
						else
						{
							_format = new TimeFormat[ 4 ];
							_format[ (int)TimeUnits.Hours ] = new TimeFormat( true );
							_format[ (int)TimeUnits.Minutes ] = new TimeFormat( true );
							_format[ (int)TimeUnits.Seconds ] = new TimeFormat( true );
							_format[ (int)TimeUnits.Fraction ] = new TimeFormat( false );

							_timeControl = new TimeEditor( _format );
							_setTime = new SetControlTime(this.SetEditorFromInt);
							((TimeEditor)_timeControl).TimeChanged += new EventHandler(this.IntTimeEditorChanged);
						}
					}
					else if ( space is PUC.Types.FixedPtSpace )
					{
						FixedPtSpace fxdspc = (FixedPtSpace)space;

						if ( fxdspc.IsRanged() )
						{
							_timeControl = new TimeSlider();
							_setTime = new SetControlTime(this.SetSliderFromFixed);
							((TimeSlider)_timeControl).TimeChanged += new EventHandler(this.FloatSliderTimeChanged);
							_sentValues = new Hashtable();
						}
						else
						{
							_format = new TimeFormat[ 4 ];
							_format[ (int)TimeUnits.Hours ] = new TimeFormat( true );
							_format[ (int)TimeUnits.Minutes ] = new TimeFormat( true );
							_format[ (int)TimeUnits.Seconds ] = new TimeFormat( true );
							_format[ (int)TimeUnits.Fraction ] = new TimeFormat( true );
							_format[ (int)TimeUnits.Fraction ].Maximum = ((int)Math.Pow( 10, fxdspc.GetPointPosition() )) - 1;

							_timeControl = new TimeEditor( _format );
							_setTime = new SetControlTime(this.SetEditorFromFloat);
							((TimeEditor)_timeControl).TimeChanged += new EventHandler(this.FloatTimeEditorChanged);
						}
					}
					else if ( space is PUC.Types.FloatingPtSpace )
					{
						FloatingPtSpace fltspc = (FloatingPtSpace)space;

						if ( fltspc.IsRanged() )
						{
							_timeControl = new TimeSlider();
							_setTime = new SetControlTime(this.SetSliderFromFloat);
							((TimeSlider)_timeControl).TimeChanged += new EventHandler(this.FloatSliderTimeChanged);
							_sentValues = new Hashtable();
						}
						else
						{
							_format = new TimeFormat[ 4 ];
							_format[ (int)TimeUnits.Hours ] = new TimeFormat( true );
							_format[ (int)TimeUnits.Minutes ] = new TimeFormat( true );
							_format[ (int)TimeUnits.Seconds ] = new TimeFormat( true );
							_format[ (int)TimeUnits.Fraction ] = new TimeFormat( true );
							_format[ (int)TimeUnits.Fraction ].Maximum = 999;

							_timeControl = new TimeEditor( _format );
							_setTime = new SetControlTime(this.SetEditorFromFloat);
							((TimeEditor)_timeControl).TimeChanged += new EventHandler(this.FloatTimeEditorChanged);
						}
					}
					else if ( space is PUC.Types.StringSpace )
					{
						_format = new TimeFormat[ 4 ];
						_format[ (int)TimeUnits.Hours ] = new TimeFormat( true );
						_format[ (int)TimeUnits.Minutes ] = new TimeFormat( true );
						_format[ (int)TimeUnits.Seconds ] = new TimeFormat( true );
						_format[ (int)TimeUnits.Fraction ] = new TimeFormat( true );

						_timeControl = new TimeEditor();
						_setTime = new SetControlTime(this.SetEditorFromString);
						((TimeEditor)_timeControl).TimeChanged += new EventHandler(this.StringTimeEditorChanged);
					}
				}

				if ( _timeControl != null )
				{
					doNotRenderObject( state );
					state.ValueChangedEvent += new PUC.ApplianceState.ValueChangedHandler(this.ValueChanged);
					state.TypeChangedEvent += new PUC.ApplianceState.TypeChangedHandler(this.TypeChanged);
					state.EnableChangedEvent += new PUC.ApplianceObject.EnableChangedHandler(this.EnableChanged);
				}
			}
			else
			{
				// multiple state translation

				/*
					* There are two possible translations if there are multiple
					* states.  If any states are read-only, then the time will be displayed 
					* in a label.  Otherwise the time will displayed for editing in the
					* PocketPCControls.TimeEditor control.
					*/

				ApplianceState hourState = (ApplianceState)_objects[ HOURS_LABEL ];
				ApplianceState minState = (ApplianceState)_objects[ MINUTES_LABEL ];
				ApplianceState secState = (ApplianceState)_objects[ SECONDS_LABEL ];
				ApplianceState fracState = (ApplianceState)_objects[ FRACTION_LABEL ];

				_getString = new GetStringMethod(this.GetStringFromMultiple);

				if ( ( hourState != null && hourState.ReadOnly ) ||
					( minState != null && minState.ReadOnly ) ||
					( secState != null && secState.ReadOnly ) ||
					( fracState != null && fracState.ReadOnly ) )
				{
					_timeControl = new Label();
					_setTime = new SetControlTime(this.SetLabel);
				}
				else
				{
					ApplianceState[] states = new ApplianceState[ 4 ];
					_format = new TimeFormat[ 4 ];
					IntegerSpace space;

					states[ (int)TimeUnits.Hours ] = hourState;
					states[ (int)TimeUnits.Minutes ] = minState;
					states[ (int)TimeUnits.Seconds ] = secState;
					states[ (int)TimeUnits.Fraction ] = fracState;

					for( int i = (int)TimeUnits.Hours; i <= (int)TimeUnits.Fraction; i++ ) 
					{
						if ( states[ i ] == null )
							_format[ i ] = new TimeFormat( false );
						else
						{
							_format[ i ] = new TimeFormat( true );

							doNotRenderObject( states[ i ] );

							space = (IntegerSpace)states[ i ].Type.ValueSpace;
	
							if ( space.IsRanged() )
							{
								_format[ i ].Maximum = space.GetMaximum().GetIntValue();
								_format[ i ].Minimum = space.GetMinimum().GetIntValue();
							}
							else
							{
								_format[ i ].Maximum = Int32.MaxValue;
								_format[ i ].Minimum = 0;
							}

							if ( space.IsIncremented() )
								_format[ i ].Increment = space.GetIncrement().GetIntValue();
						}
					}

					_timeControl = new TimeEditor( _format );
					_setTime = new SetControlTime(this.SetEditorFromMultiple);
					((TimeEditor)_timeControl).TimeChanged += new EventHandler(this.MultipleTimeEditorChanged);
				}

				if ( _timeControl != null )
				{
					if ( hourState != null )
					{
						hourState.ValueChangedEvent += new PUC.ApplianceState.ValueChangedHandler(this.ValueChanged);
						hourState.EnableChangedEvent += new PUC.ApplianceObject.EnableChangedHandler(this.EnableChanged);
					}

					if ( minState != null )
					{
						minState.ValueChangedEvent += new PUC.ApplianceState.ValueChangedHandler(this.ValueChanged);
						minState.EnableChangedEvent += new PUC.ApplianceObject.EnableChangedHandler(this.EnableChanged);
					}

					if ( secState != null )
					{
						secState.ValueChangedEvent += new PUC.ApplianceState.ValueChangedHandler(this.ValueChanged);
						secState.EnableChangedEvent += new PUC.ApplianceObject.EnableChangedHandler(this.EnableChanged);
					}

					if ( fracState != null )
					{
						fracState.ValueChangedEvent += new PUC.ApplianceState.ValueChangedHandler(this.ValueChanged);
						fracState.EnableChangedEvent += new PUC.ApplianceObject.EnableChangedHandler(this.EnableChanged);
					}
				}
			}

			if ( _timeControl != null )
			{
				GetControl().Controls.Add( _timeControl );
				_timeControl.Location = new System.Drawing.Point( 0, 0 );
				_timeControl.Size = GetControl().Size;
				_control.Resize += new EventHandler(this.Resized);
			}
		}


		/*
		 * Properties
		 */


		/* 
		 * GetStringValue Methods
		 */

		public override string GetStringValue()
		{
			return _getString();;
		}

		
		public string GetStringFromMultiple()
		{
			string lbl = "";

			ApplianceState state = (ApplianceState)_objects[ HOURS_LABEL ];
			if ( state != null && state.Defined )
			{
				int hr = (int)state.Value;
				if ( hr <= 0 )
					lbl += "00";
				else if ( hr < 10 )
					lbl += "0" + hr;
				else
					lbl += hr;
			}

			state = (ApplianceState)_objects[ MINUTES_LABEL ];
			if ( state != null && state.Defined )
			{
				if ( lbl.Length > 0 )
					lbl += ":";

				int min = (int)state.Value;
				if ( min <= 0 )
					lbl += "00";
				else if ( min < 10 )
					lbl += "0" + min;
				else
					lbl += min;
			}

			state = (ApplianceState)_objects[ SECONDS_LABEL ];
			if ( state != null && state.Defined )
			{
				if ( lbl.Length > 0 )
					lbl += ":";

				int sec = (int)state.Value;
				if ( sec <= 0 )
					lbl += "00";
				else if ( sec < 10 )
					lbl += "0" + sec;
				else
					lbl += sec;
			}

			state = (ApplianceState)_objects[ FRACTION_LABEL ];
			if ( state != null && state.Defined )
			{
				if ( lbl.Length > 0 )
					lbl += ".";

				int frac = (int)state.Value;
				if ( frac <= 0 )
					lbl += "000";
				else if ( frac < 100 )
					lbl += "0" + frac;
				else if ( frac < 10 )
					lbl += "00" + frac;
				else
					lbl += frac;
			}

			return lbl;
		}

		public string GetStringFromString()
		{
			ApplianceState state = (ApplianceState)_objects[ SINGLE_STATE ];

			if ( state == null || !state.Defined )
				return null;

			return (string)state.Value;
		}
		
		public string GetStringFromInt()
		{
			ApplianceState state = (ApplianceState)_objects[ SINGLE_STATE ];

			if ( state == null || !state.Defined )
				return null;

			int time = (int)state.Value;

			string sep = ":";
			string ret = "";

			int secs = time % 60;
			time /= 60;

			int mins = time % 60;
			time /= 60;

			int hours = time;

			if ( hours > 0 )
				ret += hours.ToString() + sep;

			if ( mins > 0 )
			{
				if ( mins == 0 )
					ret += "00" + sep;
				else if ( mins < 10 )
					ret += "0" + mins.ToString() + sep;
				else
					ret += mins.ToString() + sep;
			}

			if ( secs == 0 )
				ret += "00";
			else if ( secs < 10 )
				ret += "0" + secs.ToString();
			else
				ret += secs.ToString();

			return ret;
		}

		public string GetStringFromFloat()
		{
			// this will work with either fixedpt or floatingpt spaces

			ApplianceState state = (ApplianceState)_objects[ SINGLE_STATE ];
			double time;

			if ( state == null || !state.Defined )
				return null;

			time = (double)state.Value;

			string sep = ":";

			string ret = "";
			int inttime = (int)Math.Floor( time );

			int secs = inttime % 60;
			inttime /= 60;

			int mins = inttime % 60;
			inttime /= 60;

			int hours = inttime;

			if ( hours > 0 )
				ret += hours.ToString() + sep;

			if ( mins > 0 )
			{
				if ( mins == 0 )
					ret += "00" + sep;
				else if ( mins < 10 )
					ret += "0" + mins.ToString() + sep;
				else
					ret += mins.ToString() + sep;
			}

			if ( secs == 0 )
				ret += "00";
			else if ( secs < 10 )
				ret += "0" + secs.ToString();
			else
				ret += secs.ToString();

			if ( Math.Floor( time ) != time )
			{
				ret += ".";

				double oldtime = Math.Floor( time );
				while( Math.Floor( time ) != time )
				{
					oldtime *= 10;
					time *= 10;
				}

				int frac = ((int)time) - ((int)oldtime);
				
				ret += ((int)frac).ToString();
			}

			return ret;
		}


		/*
		 * SetControlTime Methods
		 */

		// Label Variants

		public void SetLabel()
		{
			((Label)_timeControl).Text = this.GetStringValue();
		}

		// TimeSlider Variants

		public void SetSliderFromInt()
		{
			ApplianceState state = (ApplianceState)_objects[ SINGLE_STATE ];
			PUC.Types.IntegerSpace space = (PUC.Types.IntegerSpace)state.Type.ValueSpace;

			if ( state.Defined )
			{
				string val = state.Value.ToString();

				if ( _sentValues[ val ] != null && 
					((int)_sentValues[ val ]) > 0 )
				{
					int cnt = (int)_sentValues[ val ];
					_sentValues[ val ] = --cnt;
					return;
				}

				((TimeSlider)_timeControl).Time = (int)state.Value;
				((TimeSlider)_timeControl).Maximum = space.GetMaximum().GetDoubleValue();
			}
			else
			{
				((TimeSlider)_timeControl).Time = 0;
				((TimeSlider)_timeControl).Maximum = 0;
			}
		}

		public void SetSliderFromFixed()
		{
			ApplianceState state = (ApplianceState)_objects[ SINGLE_STATE ];
			PUC.Types.FixedPtSpace space = (PUC.Types.FixedPtSpace)state.Type.ValueSpace;

			if ( state.Defined )
			{
				string val = state.Value.ToString();

				if ( _sentValues[ val ] != null && 
					((int)_sentValues[ val ]) > 0 )
				{
					int cnt = (int)_sentValues[ val ];
					_sentValues[ val ] = --cnt;
					return;
				}

				((TimeSlider)_timeControl).Time = (double)state.Value;
				((TimeSlider)_timeControl).Maximum = space.GetMaximum().GetDoubleValue();
			}
			else
			{
				((TimeSlider)_timeControl).Time = 0;
				((TimeSlider)_timeControl).Maximum = 0;
			}
		}

		public void SetSliderFromFloat()
		{
			ApplianceState state = (ApplianceState)_objects[ SINGLE_STATE ];
			PUC.Types.FloatingPtSpace space = (PUC.Types.FloatingPtSpace)state.Type.ValueSpace;

			if ( state.Defined )
			{
				string val = state.Value.ToString();

				if ( _sentValues[ val ] != null && 
					((int)_sentValues[ val ]) > 0 )
				{
					int cnt = (int)_sentValues[ val ];
					_sentValues[ val ] = --cnt;
					return;
				}

				((TimeSlider)_timeControl).Time = (double)state.Value;
				((TimeSlider)_timeControl).Maximum = space.GetMaximum().GetDoubleValue();
			}
			else
			{
				((TimeSlider)_timeControl).Time = 0;
				((TimeSlider)_timeControl).Maximum = 0;
			}		
		}

		// TimeEditor Variants

		public void SetEditorFromMultiple()
		{
			ApplianceState state = (ApplianceState)_objects[ HOURS_LABEL ];
			if ( state != null && state.Defined )
				((TimeEditor)_timeControl).SetTimeUnit( TimeUnits.Hours, (int)state.Value );

			state = (ApplianceState)_objects[ MINUTES_LABEL ];
			if ( state != null && state.Defined )
				((TimeEditor)_timeControl).SetTimeUnit( TimeUnits.Minutes, (int)state.Value );

			state = (ApplianceState)_objects[ SECONDS_LABEL ];
			if ( state != null && state.Defined )
				((TimeEditor)_timeControl).SetTimeUnit( TimeUnits.Seconds, (int)state.Value );

			state = (ApplianceState)_objects[ FRACTION_LABEL ];
			if ( state != null && state.Defined )
				((TimeEditor)_timeControl).SetTimeUnit( TimeUnits.Fraction, (int)state.Value );
		}

		public void SetEditorFromInt()
		{
			ApplianceState state = (ApplianceState)_objects[ SINGLE_STATE ];
			int time = 0;
				
			if ( state.Defined )
				time = (int)state.Value;

			int sec = time % 60;
			time /= 60;

			int min = time % 60;
			time /= 60;

			int hrs = time;

			((TimeEditor)_timeControl).SetTime( hrs, min, sec, 0 );
		}

		public void SetEditorFromFloat()
		{
			ApplianceState state = (ApplianceState)_objects[ SINGLE_STATE ];
			double time = 0;
			
			if ( state.Defined )
				time = (double)state.Value;
			int inttime = (int)Math.Floor( time );

			int sec = inttime % 60;
			inttime /= 60;

			int min = inttime % 60;
			inttime /= 60;

			int hrs = inttime;

			int frac = 0;
			if ( Math.Floor( time ) != time )
			{
				double oldtime = Math.Floor( time );
				while( Math.Floor( time ) != time )
				{
					oldtime *= 10;
					time *= 10;
				}

				frac = ((int)time) - ((int)oldtime);
			}

			((TimeEditor)_timeControl).SetTime( hrs, min, sec, frac );
		}

		public void SetEditorFromString()
		{
			ApplianceState state = (ApplianceState)_objects[ SINGLE_STATE ];
			string time = "00:00:00";
			
			if ( state.Defined )
				time = (string)state.Value;

			char[] seps = new char[ 1 ];
			seps[ 0 ] = ':';

			string[] pieces = time.Split( seps );

			if ( pieces.Length == 2 )
			{
				// format is mm:ss
				_format[ (int)TimeUnits.Hours ].Valid = false;
				_format[ (int)TimeUnits.Minutes ].Valid = true;
				_format[ (int)TimeUnits.Minutes ].Maximum = 59;
				_format[ (int)TimeUnits.Seconds ].Valid = true;
				_format[ (int)TimeUnits.Seconds ].Maximum = 59;
				_format[ (int)TimeUnits.Fraction ].Valid = false;

				((TimeEditor)_timeControl).SetFormat( _format );
				((TimeEditor)_timeControl).SetTimeUnit( TimeUnits.Minutes, Int32.Parse( pieces[ 0 ] ) );
				((TimeEditor)_timeControl).SetTimeUnit( TimeUnits.Seconds, Int32.Parse( pieces[ 1 ] ) );
			}
			else if ( pieces.Length == 4 )
			{
				// format is hh:mm:ss:fff
				_format[ (int)TimeUnits.Hours ].Valid = true;
				_format[ (int)TimeUnits.Hours ].Maximum = 99;
				_format[ (int)TimeUnits.Minutes ].Valid = true;
				_format[ (int)TimeUnits.Minutes ].Maximum = 59;
				_format[ (int)TimeUnits.Seconds ].Valid = true;
				_format[ (int)TimeUnits.Seconds ].Maximum = 59;
				_format[ (int)TimeUnits.Fraction ].Valid = true;
				_format[ (int)TimeUnits.Fraction ].Maximum = 999;

				((TimeEditor)_timeControl).SetFormat( _format );
				((TimeEditor)_timeControl).SetTimeUnit( TimeUnits.Hours, Int32.Parse( pieces[ 0 ] ) );
				((TimeEditor)_timeControl).SetTimeUnit( TimeUnits.Minutes, Int32.Parse( pieces[ 1 ] ) );
				((TimeEditor)_timeControl).SetTimeUnit( TimeUnits.Seconds, Int32.Parse( pieces[ 2 ] ) );
				((TimeEditor)_timeControl).SetTimeUnit( TimeUnits.Fraction, Int32.Parse( pieces[ 3 ] ) );
			}
			else if ( pieces.Length == 3 )
			{
				// format is either hh:mm:ss or mm:ss:fff
				if ( pieces[ 2 ].Length == 3 )
				{
					// format is mm:ss:fff
					_format[ (int)TimeUnits.Hours ].Valid = false;
					_format[ (int)TimeUnits.Minutes ].Valid = true;
					_format[ (int)TimeUnits.Minutes ].Maximum = 59;
					_format[ (int)TimeUnits.Seconds ].Valid = true;
					_format[ (int)TimeUnits.Seconds ].Maximum = 59;
					_format[ (int)TimeUnits.Fraction ].Valid = true;
					_format[ (int)TimeUnits.Fraction ].Maximum = 999;

					((TimeEditor)_timeControl).SetFormat( _format );
					((TimeEditor)_timeControl).SetTimeUnit( TimeUnits.Minutes, Int32.Parse( pieces[ 0 ] ) );
					((TimeEditor)_timeControl).SetTimeUnit( TimeUnits.Seconds, Int32.Parse( pieces[ 1 ] ) );
					((TimeEditor)_timeControl).SetTimeUnit( TimeUnits.Fraction, Int32.Parse( pieces[ 2 ] ) );
				}
				else
				{
					// format is hh:mm:ss
					_format[ (int)TimeUnits.Hours ].Valid = true;
					_format[ (int)TimeUnits.Hours ].Maximum = 99;
					_format[ (int)TimeUnits.Minutes ].Valid = true;
					_format[ (int)TimeUnits.Minutes ].Maximum = 59;
					_format[ (int)TimeUnits.Seconds ].Valid = true;
					_format[ (int)TimeUnits.Seconds ].Maximum = 59;
					_format[ (int)TimeUnits.Fraction ].Valid = false;

					((TimeEditor)_timeControl).SetFormat( _format );
					((TimeEditor)_timeControl).SetTimeUnit( TimeUnits.Hours, Int32.Parse( pieces[ 0 ] ) );
					((TimeEditor)_timeControl).SetTimeUnit( TimeUnits.Minutes, Int32.Parse( pieces[ 1 ] ) );
					((TimeEditor)_timeControl).SetTimeUnit( TimeUnits.Seconds, Int32.Parse( pieces[ 2 ] ) );
				}
			}
		}


		/*
		 * Control Change Callbacks
		 */

		public void MultipleTimeEditorChanged( object source, EventArgs e )
		{
			int[] time = ((TimeEditor)_timeControl).GetTime();

			ApplianceState state = (ApplianceState)_objects[ HOURS_LABEL ];
			if ( state != null && 
				 ( !state.Defined || time[ (int)TimeUnits.Hours ] != (int)state.Value ) )
				state.RequestChange( time[ (int)TimeUnits.Hours ] );

			state = (ApplianceState)_objects[ MINUTES_LABEL ];
			if ( state != null && 
				( !state.Defined || time[ (int)TimeUnits.Minutes ] != (int)state.Value ) )
				state.RequestChange( time[ (int)TimeUnits.Minutes ] );

			state = (ApplianceState)_objects[ SECONDS_LABEL ];
			if ( state != null && 
				 ( !state.Defined || time[ (int)TimeUnits.Seconds ] != (int)state.Value ) )
				state.RequestChange( time[ (int)TimeUnits.Seconds ] );

			state = (ApplianceState)_objects[ FRACTION_LABEL ];
			if ( state != null && 
				 ( !state.Defined || time[ (int)TimeUnits.Fraction ] != (int)state.Value ) )
				state.RequestChange( time[ (int)TimeUnits.Fraction ] );
		}

		public void IntTimeEditorChanged( object source, EventArgs e )
		{
			int[] time = ((TimeEditor)_timeControl).GetTime();

			int inttime = time[ (int)TimeUnits.Hours ];

			inttime = ( inttime * 60 ) + time[ (int)TimeUnits.Minutes ];
			inttime = ( inttime * 60 ) + time[ (int)TimeUnits.Seconds ];

			ApplianceState state = (ApplianceState)_objects[ SINGLE_STATE ];
			if ( state != null ) 
				state.RequestChange( inttime );
		}

		public void FloatTimeEditorChanged( object source, EventArgs e )
		{
			ApplianceState state = (ApplianceState)_objects[ SINGLE_STATE ];
			
			double correctFactor = 1000;
			if ( state.Type.ValueSpace is FixedPtSpace )
			{
				correctFactor = Math.Pow( 10, ((FixedPtSpace)state.Type.ValueSpace).GetPointPosition() );
			}
			
			int[] time = ((TimeEditor)_timeControl).GetTime();

			int inttime = time[ (int)TimeUnits.Hours ];

			inttime = ( inttime * 60 ) + time[ (int)TimeUnits.Minutes ];
			inttime = ( inttime * 60 ) + time[ (int)TimeUnits.Seconds ];

			double dbltime = (double)inttime + ((double)time[ (int)TimeUnits.Fraction ]) / correctFactor;

			if ( state != null ) 
				state.RequestChange( inttime );
		}

		public void StringTimeEditorChanged( object source, EventArgs e )
		{
			ApplianceState state = (ApplianceState)_objects[ SINGLE_STATE ];
			int[] time = ((TimeEditor)_timeControl).GetTime();

			string strtime = "";

			if ( _format[ (int)TimeUnits.Hours ].Valid )
			{
				if ( time[ (int)TimeUnits.Hours ] == 0 )
					strtime += "00";
				else if ( time[ (int)TimeUnits.Hours ] < 10 )
					strtime += "0" + time[ (int)TimeUnits.Hours ];
				else
					strtime += time[ (int)TimeUnits.Hours ];

				if ( _format[ (int)TimeUnits.Minutes ].Valid )
					strtime += ":";
			}

			if ( _format[ (int)TimeUnits.Minutes ].Valid )
			{
				if ( time[ (int)TimeUnits.Minutes ] == 0 )
					strtime += "00";
				else if ( time[ (int)TimeUnits.Minutes ] < 10 )
					strtime += "0" + time[ (int)TimeUnits.Minutes ];
				else
					strtime += time[ (int)TimeUnits.Minutes ];

				if ( _format[ (int)TimeUnits.Seconds ].Valid )
					strtime += ":";
			}

			if ( _format[ (int)TimeUnits.Seconds ].Valid )
			{
				if ( time[ (int)TimeUnits.Seconds ] == 0 )
					strtime += "00";
				else if ( time[ (int)TimeUnits.Seconds ] < 10 )
					strtime += "0" + time[ (int)TimeUnits.Seconds ];
				else
					strtime += time[ (int)TimeUnits.Seconds ];

				if ( _format[ (int)TimeUnits.Fraction ].Valid )
					strtime += ".";
			}

			if ( _format[ (int)TimeUnits.Fraction ].Valid )
			{
				if ( time[ (int)TimeUnits.Fraction ] == 0 )
					strtime += "000";
				else if ( time[ (int)TimeUnits.Fraction ] < 100 )
					strtime += "0" + time[ (int)TimeUnits.Fraction ];
				else if ( time[ (int)TimeUnits.Fraction ] < 10 )
					strtime += "00" + time[ (int)TimeUnits.Fraction ];
				else
					strtime += time[ (int)TimeUnits.Fraction ];
			}

			if ( state != null ) 
				state.RequestChange( strtime );
		}

		public void IntSliderTimeChanged( object source, EventArgs e )
		{
			ApplianceState state = (ApplianceState)_objects[ SINGLE_STATE ];
			if ( state != null )
			{
				int valint = (int)((TimeSlider)_timeControl).Time;
				string val = valint.ToString();
				string stateval;

				if ( state.Defined )
					stateval = state.Value.ToString();
				else 
					stateval = "";

				// don't echo state change requests for notifications
				if ( val == stateval ) return;

				state.RequestChange( valint );

				// keep track of the value that was sent
				if ( _sentValues[ val ] != null )
				{
					int cnt = (int)_sentValues[ val ];
					_sentValues[ val ] = ++cnt;
				}
				else
					_sentValues[ val ] = 1;
			}			
		}

		public void FloatSliderTimeChanged( object source, EventArgs e )
		{
			ApplianceState state = (ApplianceState)_objects[ SINGLE_STATE ];
			if ( state != null )
			{
				double valdbl = (double)((TimeSlider)_timeControl).Time;
				string val = valdbl.ToString();
				string stateval;

				if ( state.Defined )
					stateval = state.Value.ToString();
				else 
					stateval = "";

				// don't echo state change requests for notifications
				if ( val == stateval ) return;

				state.RequestChange( valdbl );

				// keep track of the value that was sent
				if ( _sentValues[ val ] != null )
				{
					int cnt = (int)_sentValues[ val ];
					_sentValues[ val ] = ++cnt;
				}
				else
					_sentValues[ val ] = 1;
			}			
		}


		/*
		 * Member Methods
		 */

		public void ValueChanged( ApplianceState state )
		{
			_setTime();
		}

		public void TypeChanged( ApplianceState state )
		{
			_setTime();
		}

		public void EnableChanged( ApplianceObject ao )
		{
			_timeControl.Enabled = ao.Enabled;
		}

		public void Resized( object source, EventArgs e )
		{
			_timeControl.Size = GetControl().Size;
		}

		public override void FinalSizeNotify()
		{
			
		}

		public override System.Drawing.Point GetControlOffset()
		{
			if ( _timeControl is Label )
			{
				System.Drawing.SizeF s = Globals.MeasureString( "99:59:59.999", _timeControl.Font );	

				return new System.Drawing.Point( 0, (int)s.Height );
			}
			else if ( _timeControl is TimeEditor )
			{
				System.Drawing.SizeF s = Globals.MeasureString( "123456789:.", _timeControl.Font );	

				return new System.Drawing.Point( 0, TB_MINIMUM_TOP_PAD + (int)s.Height );
			}
			else if ( _timeControl is TimeSlider )
			{
				System.Drawing.SizeF s = Globals.MeasureString( "99:59:59.999", _timeControl.Font );	

				return new System.Drawing.Point( 0, TimeSlider.VERTICAL_TEXT_OFFSET + (int)s.Height );
			}

			return new System.Drawing.Point ();
		}

		public override System.Drawing.Size GetMinimumSize()
		{
			if ( _timeControl is Label )
			{
				System.Drawing.SizeF s = Globals.MeasureString( "99:59:59.999", _timeControl.Font );	

				int w = (int)s.Width;
				int h = (int)s.Height;

				return new System.Drawing.Size( w, h );
			}
			else if ( _timeControl is TimeEditor )
			{
				int w = 0;
				int h = 0;

				for( int i = (int)TimeUnits.Hours; i <= (int)TimeUnits.Fraction; i++ )
				{
					if ( _format[ i ].Valid )
					{
						System.Drawing.SizeF s = Globals.MeasureString( _format[ i ].Maximum.ToString(), _timeControl.Font );	

						w += (int)s.Width + TB_MINIMUM_RIGHT_PAD + TB_MINIMUM_LEFT_PAD + TimeEditor.SPACER;
						h = Math.Max( h, (int)s.Height + TB_MINIMUM_TOP_PAD + TB_MINIMUM_BOTTOM_PAD );
					}
				}

				return new System.Drawing.Size( w, h );
			}
			else if ( _timeControl is TimeSlider )
			{
				System.Drawing.SizeF s = Globals.MeasureString( "0" + ((TimeSlider)_timeControl).Maximum, _timeControl.Font );	

				int w = SB_MINIMUM_WIDTH + TimeSlider.AVERAGE_LABEL_WIDTH;
				int h = SB_DEFAULT_HEIGHT + 
#if POCKETPC || DESKTOP
					3
#endif
#if SMARTPHONE
					2 
#endif
					* TimeSlider.VERTICAL_TEXT_OFFSET + (int)s.Height;

				return new System.Drawing.Size( w, h );
			}

			return new System.Drawing.Size ();
		}

		public override PUC.Layout.PreferredSize GetPreferredSize()
		{
			return new PUC.Layout.PreferredSize( GetMinimumSize() );
		}
	}
}
