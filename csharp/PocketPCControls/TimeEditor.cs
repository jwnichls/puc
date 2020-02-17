using System;
using System.Drawing;
using System.Windows.Forms;

namespace PocketPCControls
{
	/// <summary>
	/// Summary description for TimeEditor.
	/// </summary>
	public class TimeEditor : Control
	{
		/*
		 * Constants
		 */

		private const int DEFAULT_HEIGHT = 22;
		private const int MINIMUM_LEFT_PAD = 3;
		private const int MINIMUM_RIGHT_PAD = 3;
		private const int MINIMUM_TOP_PAD = 3;
		private const int MINIMUM_BOTTOM_PAD = 5;

		protected const int COLONSPACER  = 2;
		public    const int SPACER       = 3 * COLONSPACER;


		/*
		 * Member Variables
		 */

		protected Brush				_colonBrush = new SolidBrush( Color.Black );

		protected TimeFormat[]		_format;
		protected int[]				_time;
		protected TextBox[]			_boxes;
		protected EventHandler[]	_handlers;
		protected bool				_resize;
		protected bool				_usingPicker;
		protected DateTimePicker	_picker;
		protected EventHandler		_pickerHandler;


		/*
		 * Events
		 */

		public event EventHandler TimeChanged;


		/*
		 * Constructors
		 */

		public TimeEditor()
		{
			_format = new TimeFormat[ 4 ];

			_format[ (int)TimeUnits.Hours ] = new TimeFormat( true );
			_format[ (int)TimeUnits.Hours ].Maximum = 12;
			
			_format[ (int)TimeUnits.Minutes ] = new TimeFormat( true );

			_format[ (int)TimeUnits.Seconds ] = new TimeFormat( true );

			_format[ (int)TimeUnits.Fraction ] = new TimeFormat( true );
			_format[ (int)TimeUnits.Fraction ].Maximum = 24;

			commonConstructor();
		}

		public TimeEditor( TimeFormat[] format )
		{
			_format = new TimeFormat[ 4 ];

			for( int i = (int)TimeUnits.Hours; i <= (int)TimeUnits.Fraction; i++ )
				_format[ i ] = format[ i ];

			commonConstructor();
		}

		private void commonConstructor()
		{
			if ( _format[ (int)TimeUnits.Hours ].Valid && 
				 _format[ (int)TimeUnits.Seconds ].Valid &&
				!_format[ (int)TimeUnits.Minutes ].Valid )
			{
				_format[ (int)TimeUnits.Minutes ].Valid = true;
				_format[ (int)TimeUnits.Minutes ].Minimum = 0;
				_format[ (int)TimeUnits.Minutes ].Maximum = 59;
				_format[ (int)TimeUnits.Minutes ].Increment = 0;
			}

			if ( _format[ (int)TimeUnits.Minutes ].Valid && 
				 _format[ (int)TimeUnits.Fraction ].Valid &&
				!_format[ (int)TimeUnits.Seconds ].Valid )
			{
				_format[ (int)TimeUnits.Seconds ].Valid = true;
				_format[ (int)TimeUnits.Seconds ].Minimum = 0;
				_format[ (int)TimeUnits.Seconds ].Maximum = 59;
				_format[ (int)TimeUnits.Seconds ].Increment = 0;
			}

			// check to see if DateTimePicker can be used
			string formatString = getPickerFormat();
			if ( formatString != null )
			{
				_usingPicker = true;

				_picker = new DateTimePicker();
				_picker.Format = DateTimePickerFormat.Custom;
				_picker.CustomFormat = formatString;
				_picker.ShowUpDown = true;
				this.Controls.Add( _picker );

				_pickerHandler = new EventHandler(this.PickerChanged);
				_picker.ValueChanged += _pickerHandler;
			}
			else
			{
				_usingPicker = false;

				_boxes = new TextBox[ 4 ] { null, null, null, null };
				_handlers = new EventHandler[ 4 ];

				_handlers[ (int)TimeUnits.Hours ] = new EventHandler(this.HoursChanged);
				_handlers[ (int)TimeUnits.Minutes ] = new EventHandler(this.MinutesChanged);
				_handlers[ (int)TimeUnits.Seconds ] = new EventHandler(this.SecondsChanged);
				_handlers[ (int)TimeUnits.Fraction ] = new EventHandler(this.FractionChanged);
			}

			_time = new int[ 4 ] { 0, 0, 0, 0 };

			refreshType();
		}


		/*
		 * Private Helper Methods
		 */

		private string getPickerFormat()
		{
			string formatString = "";

			if ( _format[ (int)TimeUnits.Fraction ].Valid )
				return null;

			if ( _format[ (int)TimeUnits.Hours ].Valid &&
				 _format[ (int)TimeUnits.Hours ].Maximum > 23 )
				return null;
			else
				formatString = "HH";

			if ( formatString.Length > 0 &&
				 _format[ (int)TimeUnits.Minutes ].Valid )
				formatString += ":";

			if ( _format[ (int)TimeUnits.Minutes ].Valid )
				if ( _format[ (int)TimeUnits.Minutes ].Maximum > 59 )
					return null;
				else
					formatString += "mm";

			if ( formatString.Length > 0 &&
				 _format[ (int)TimeUnits.Seconds ].Valid )
				formatString += ":";

			if ( _format[ (int)TimeUnits.Seconds ].Valid )
				if ( _format[ (int)TimeUnits.Seconds ].Maximum > 59 )
					return null;
				else
					formatString += "ss";

			return formatString;
		}


		/*
		 * Properties
		 */

		public TimeFormat[] Format
		{
			get
			{
				return _format;
			}
		}


		/*
		 * Member Methods
		 */

		public void SetFormat( TimeFormat[] format )
		{
			bool diff = false;
			for( int i = (int)TimeUnits.Hours; i <= (int)TimeUnits.Fraction; i++ )
				diff |= _format[ i ].Different( format[ i ] );

			if ( diff )
			{
				_format = format;

				refreshType();
			}
		}

		public void SetTime( int hrs, int mins, int secs, int frac )
		{
			_time[ (int)TimeUnits.Hours ] = hrs;
			_time[ (int)TimeUnits.Minutes ] = mins;
			_time[ (int)TimeUnits.Seconds ] = secs;
			_time[ (int)TimeUnits.Fraction ] = frac;

			refreshDisplay();
		}

		public void SetTimeUnit( TimeUnits unit, int value )
		{
			_time[ (int)unit ] = value;

			refreshDisplay();
		}

		public int[] GetTime()
		{
			return _time;
		}

		protected void refreshType() 
		{
			if ( !_usingPicker )
			{
				int x = 0;

				if ( _format[ (int)TimeUnits.Hours ].Valid )
				{
					x += initTextBox( TimeUnits.Hours, x );
				}

				if ( _format[ (int)TimeUnits.Minutes ].Valid )
				{
					x += initTextBox( TimeUnits.Minutes, x );
				}

				if ( _format[ (int)TimeUnits.Seconds ].Valid )
				{
					x += initTextBox( TimeUnits.Seconds, x );
				}

				if ( _format[ (int)TimeUnits.Fraction ].Valid )
				{
					x += initTextBox( TimeUnits.Fraction, x );
				}
			}

			refreshDisplay();

			_resize = true;
		}

		protected void refreshDisplay()
		{
			if ( _usingPicker )
			{
				_picker.Value = new DateTime( 2000, 1, 1, _time[ (int)TimeUnits.Hours ], _time[ (int)TimeUnits.Minutes ], _time[ (int)TimeUnits.Seconds ] );
			}
			else
			{
				for( int i = (int)TimeUnits.Hours; i < (int)TimeUnits.Fraction; i++ )
				{
					if ( _format[ i ].Valid )
						_boxes[ i ].Text = _time[ i ].ToString();
				}
			}
		}

		protected void HoursChanged( object source, EventArgs e )
		{
			int val = Int32.Parse( _boxes[ (int)TimeUnits.Hours ].Text );

			if ( _format[ (int)TimeUnits.Hours ].Valid &&
				 val >= _format[ (int)TimeUnits.Hours ].Minimum &&
				val <= _format[ (int)TimeUnits.Hours ].Maximum &&
				val != _time[ (int)TimeUnits.Hours ] )
			{
				_time[ (int)TimeUnits.Hours ] = val;

				if ( TimeChanged != null )
					TimeChanged( this, new EventArgs() );
			}			
			else
				_boxes[ (int)TimeUnits.Hours ].Text = _time[ (int)TimeUnits.Hours ].ToString();
		}

		protected void MinutesChanged( object source, EventArgs e )
		{
			int val = Int32.Parse( _boxes[ (int)TimeUnits.Minutes ].Text );

			if ( _format[ (int)TimeUnits.Minutes ].Valid &&
				val >= _format[ (int)TimeUnits.Minutes ].Minimum &&
				val <= _format[ (int)TimeUnits.Minutes ].Maximum &&
				val != _time[ (int)TimeUnits.Minutes ] )
			{
				_time[ (int)TimeUnits.Minutes ] = val;

				if ( TimeChanged != null )
					TimeChanged( this, new EventArgs() );
			}			
			else
				_boxes[ (int)TimeUnits.Minutes ].Text = _time[ (int)TimeUnits.Minutes ].ToString();		
		}

		protected void SecondsChanged( object source, EventArgs e )
		{
			int val = Int32.Parse( _boxes[ (int)TimeUnits.Seconds ].Text );

			if ( _format[ (int)TimeUnits.Seconds ].Valid &&
				val >= _format[ (int)TimeUnits.Seconds ].Minimum &&
				val <= _format[ (int)TimeUnits.Seconds ].Maximum &&
				val != _time[ (int)TimeUnits.Seconds ] )
			{
				_time[ (int)TimeUnits.Seconds ] = val;

				if ( TimeChanged != null )
					TimeChanged( this, new EventArgs() );
			}						
			else
				_boxes[ (int)TimeUnits.Seconds ].Text = _time[ (int)TimeUnits.Seconds ].ToString();
		}
		
		protected void FractionChanged( object source, EventArgs e )
		{
			int val = Int32.Parse( _boxes[ (int)TimeUnits.Fraction ].Text );

			if ( _format[ (int)TimeUnits.Fraction ].Valid &&
				 val >= _format[ (int)TimeUnits.Fraction ].Minimum &&
				 val <= _format[ (int)TimeUnits.Fraction ].Maximum &&
				 val != _time[ (int)TimeUnits.Fraction ] )
			{
				_time[ (int)TimeUnits.Fraction ] = val;

				if ( TimeChanged != null )
					TimeChanged( this, new EventArgs() );
			}
			else
				_boxes[ (int)TimeUnits.Fraction ].Text = _time[ (int)TimeUnits.Fraction ].ToString();
		}
		
		protected int initTextBox( TimeUnits unit, int x )
		{
			int idx = (int)unit;

			if ( _boxes[ idx ] != null )
			{
				_boxes[ idx ].LostFocus -= _handlers[ idx ];
				this.Controls.Remove( _boxes[ idx ] );
			}

			_boxes[ idx ] = new TextBox();
			_boxes[ idx ].LostFocus += _handlers[ idx ];
			this.Controls.Add( _boxes[ idx ] );

			_boxes[ idx ].TextAlign = HorizontalAlignment.Right;
			_boxes[ idx ].Location = new System.Drawing.Point( x, 0 );
			_boxes[ idx ].MaxLength = _format[ idx ].Maximum.ToString().Length;
			_boxes[ idx ].Text = _time[ idx ].ToString();

			// This is just a temporary size until we can get string sizing 
			// information
			_boxes[ idx ].Size = new System.Drawing.Size( 25, 22 );

			return _boxes[ idx ].Size.Width + SPACER;
		}

		protected override void OnPaint(PaintEventArgs e)
		{
			base.OnPaint (e);

			// e.Graphics.DrawRectangle( new Pen( Color.Black ), 0, 0, this.Size.Width-1, this.Size.Height-1 );
			if ( _usingPicker )
			{
				if ( _resize )
				{
					_picker.Location = new Point( 0, 0 );
					_picker.Size = new Size( this.Width, 20 );
				}
			}
			else
			{
				int x;
				int y = MINIMUM_TOP_PAD;

				if ( _resize )
					properlySizeTextBoxes( e.Graphics );

				if ( _boxes[ (int)TimeUnits.Hours ] != null && _boxes[ (int)TimeUnits.Minutes ] != null )
				{
					x = _boxes[ (int)TimeUnits.Hours ].Location.X + _boxes[ (int)TimeUnits.Hours ].Size.Width + COLONSPACER;
					e.Graphics.DrawString( ":", this.Font, _colonBrush, x, y );
				}

				if ( _boxes[ (int)TimeUnits.Minutes ] != null && _boxes[ (int)TimeUnits.Seconds ] != null )
				{
					x = _boxes[ (int)TimeUnits.Minutes ].Location.X + _boxes[ (int)TimeUnits.Minutes ].Size.Width + COLONSPACER;
					e.Graphics.DrawString( ":", this.Font, _colonBrush, x, y );
				}

				if ( _boxes[ (int)TimeUnits.Seconds ] != null && _boxes[ (int)TimeUnits.Fraction ] != null )
				{
					x = _boxes[ (int)TimeUnits.Seconds ].Location.X + _boxes[ (int)TimeUnits.Seconds ].Size.Width + COLONSPACER;
					e.Graphics.DrawString( ".", this.Font, _colonBrush, x, y );
				}
			}
		}

		protected void properlySizeTextBoxes( System.Drawing.Graphics g )
		{
			_resize = false;

			int x = this.Size.Width;
			string exStr = "";
			System.Drawing.SizeF size;

			for( int i = (int)TimeUnits.Fraction; i >= (int)TimeUnits.Hours; i-- )
			{
				if ( _boxes[ i ] != null )
				{
					exStr = _format[ i ].Maximum.ToString();
					size = g.MeasureString( exStr, _boxes[ i ].Font );
					_boxes[ i ].Size = new Size( MINIMUM_LEFT_PAD + MINIMUM_RIGHT_PAD + (int)size.Width, 
												 _boxes[ i ].Size.Height );

					if ( _format[ i ].Maximum == Int32.MaxValue &&
						 ( i == (int)TimeUnits.Hours || _boxes[ i - 1 ] == null ) )
					{
						_boxes[ i ].Size = new Size( x, _boxes[ i ].Size.Height );
					}

					x -= ( _boxes[ i ].Width + SPACER );
				}
			}

			x = 0;

			for( int i = (int)TimeUnits.Hours; i <= (int)TimeUnits.Fraction; i++ )
			{
				if ( _boxes[ i ] != null )
				{
					_boxes[ i ].Location = new Point( x, 0 );

					x += _boxes[ i ].Size.Width + SPACER;
				}
			}
		}

		private void PickerChanged(object sender, EventArgs e)
		{
			bool change = false;

			change |= _time[ (int)TimeUnits.Hours ] != _picker.Value.Hour;
			_time[ (int)TimeUnits.Hours ] = _picker.Value.Hour;

			change |= _time[ (int)TimeUnits.Minutes ] != _picker.Value.Minute;
			_time[ (int)TimeUnits.Minutes ] = _picker.Value.Minute;

			change |= _time[ (int)TimeUnits.Seconds ] != _picker.Value.Second;
			_time[ (int)TimeUnits.Seconds ] = _picker.Value.Second;

			if ( change && TimeChanged != null )
				TimeChanged( this, new EventArgs() );
		}
	}
}
