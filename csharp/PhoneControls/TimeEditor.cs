using System;
using System.Drawing;
using System.Windows.Forms;
using PocketPCControls;

namespace PhoneControls
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
		protected bool				_resize;
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

			_picker = new DateTimePicker();
			_picker.Format = DateTimePickerFormat.Custom;
			_picker.CustomFormat = formatString;
			_picker.ShowUpDown = true;
			_picker.ControlChange += new KeyEventHandler(_picker_ControlChange);
			this.Controls.Add( _picker );

			_pickerHandler = new EventHandler(this.PickerChanged);
			_picker.ValueChanged += _pickerHandler;

			_time = new int[ 4 ] { 0, 0, 0, 0 };

			refreshType();
		}


		/*
		 * Private Helper Methods
		 */

		private string getPickerFormat()
		{
			string formatString = "";

			if ( _format[ (int)TimeUnits.Hours ].Valid )
				formatString += "HH";

			if ( formatString.Length > 0 &&
				 _format[ (int)TimeUnits.Minutes ].Valid )
				formatString += ":";

			if ( _format[ (int)TimeUnits.Minutes ].Valid )
				formatString += "mm";

			if ( formatString.Length > 0 &&
				 _format[ (int)TimeUnits.Seconds ].Valid )
				formatString += ":";

			if ( _format[ (int)TimeUnits.Seconds ].Valid )
				formatString += "ss";
			
			if ( _format[ (int)TimeUnits.Fraction ].Valid )
				formatString += "fff";

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

				_picker.CustomFormat = getPickerFormat();

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
			refreshDisplay();

			_resize = true;
		}

		protected void refreshDisplay()
		{
			_picker.Value = new DateTime( 2000, 1, 1, _time[ (int)TimeUnits.Hours ], _time[ (int)TimeUnits.Minutes ], _time[ (int)TimeUnits.Seconds ] );
		}

		protected override void OnGotFocus(EventArgs e)
		{
			base.OnGotFocus (e);

			_picker.Focus();

			this.Invalidate();
		}

		protected override void OnLostFocus(EventArgs e)
		{
			base.OnLostFocus (e);

			this.Invalidate();
		}

		protected override void OnKeyDown(KeyEventArgs e)
		{
			int loc = 0;

			switch( e.KeyCode )
			{
				case Keys.Up:
					loc = Parent.Controls.GetChildIndex( this ) - 1;
					for( int i = loc; i >= 0; i-- )
						if ( Parent.Controls[ i ].Enabled )
						{
							Parent.Controls[ i ].Focus();
							break;
						}
					break;

				case Keys.Down:
					loc = Parent.Controls.GetChildIndex( this ) + 1;
					for( int i = loc; i < Parent.Controls.Count; i++ )
						if ( Parent.Controls[ i ].Enabled )
						{
							Parent.Controls[ i ].Focus();
							break;
						}
					break;
			}
		}

		protected override void OnPaint(PaintEventArgs e)
		{
			base.OnPaint (e);

			// e.Graphics.DrawRectangle( new Pen( Color.Black ), 0, 0, this.Size.Width-1, this.Size.Height-1 );
			if ( _resize )
			{
				_picker.Location = new Point( 0, 0 );
				_picker.Size = new Size( this.Width, 20 );
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

		private void _picker_ControlChange(object sender, KeyEventArgs e)
		{
			int loc = 0;

			switch( e.KeyCode )
			{
				case Keys.Up:
					loc = Parent.Controls.GetChildIndex( this ) - 1;
					for( int i = loc; i >= 0; i-- )
						if ( Parent.Controls[ i ].Enabled )
						{
							Parent.Controls[ i ].Focus();
							break;
						}
					break;

				case Keys.Down:
					loc = Parent.Controls.GetChildIndex( this ) + 1;
					for( int i = loc; i < Parent.Controls.Count; i++ )
						if ( Parent.Controls[ i ].Enabled )
						{
							Parent.Controls[ i ].Focus();
							break;
						}
					break;
			}
		}
	}
}
