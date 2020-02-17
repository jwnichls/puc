using System;
using System.Drawing;
using System.Windows.Forms;

namespace PhoneControls
{
	/// <summary>
	/// Summary description for DateTimePicker.
	/// </summary>
	public class DateTimePicker : Control
	{
		/*
		 * Constants
		 */

		protected const int HOUR		= 0;
		protected const int MINUTE		= 1;
		protected const int SECOND		= 2;
		protected const int MILLISECOND	= 3;

		protected const int LEFT_PAD	= 4;
		protected const int VRECT_PAD	= 0;
		protected const int HRECT_PAD	= 1;


		/*
		 * Member Variables
		 */

		protected DateTimePickerFormat	_format;
	
		protected bool		_useHour;
		protected bool		_useMin;
		protected bool		_useSec;
		protected bool		_useFrac;

		protected int		_hour;
		protected int		_min;
		protected int		_sec;
		protected int		_frac;

		protected int		_selectedUnit;

		protected Rectangle _hourRect;
		protected Rectangle _minuteRect;
		protected Rectangle _secondRect;
		protected Rectangle _fractionRect;
		protected int		_textTop;

		protected Image		_imageBuffer;

		protected bool		_initialMeasuresNeeded;
		protected int		_hourWidth;
		protected int		_minSecWidth;
		protected int		_fracWidth;
		protected int		_sepWidth;
		protected int		_fontHeight;

		// brushes & pens

		private Brush _blackBrush		= new SolidBrush( Color.Black );
		private Brush _whiteBrush		= new SolidBrush( Color.White );
		private Brush _selectedBrush	= new SolidBrush( Color.DarkBlue );
		private Pen   _blackPen			= new Pen( Color.Black );
		private Pen	  _disabledBorder	= new Pen( Color.Gray );
		private Pen   _whitePen			= new Pen( Color.White );


		/*
		 * Events
		 */

		public event EventHandler ValueChanged;
		public event KeyEventHandler ControlChange;


		/*
		 * Constructor
		 */

		public DateTimePicker()
		{
			_selectedUnit = 0;
			_imageBuffer = new Bitmap( this.Width, this.Height );
			_initialMeasuresNeeded = true;

			Format = DateTimePickerFormat.Time;

			this.Font = new Font( this.Font.Name, this.Font.Size, FontStyle.Regular );

			this.Resize += new EventHandler(DateTimePicker_Resize);
			this.GotFocus += new EventHandler(DateTimePicker_GotFocus);
			this.LostFocus += new EventHandler(DateTimePicker_LostFocus);
		}


		/*
		 * Event Handlers
		 */

		private void DateTimePicker_Resize(object sender, EventArgs e)
		{
			if ( _imageBuffer != null )
				_imageBuffer.Dispose();

			if ( this.Height != 20 )
			{
				this.Height = 20;
				return;
			}

			_imageBuffer = new Bitmap( this.Width, this.Height );

			calcRects();

			this.Invalidate();
		}

		protected void calcRects()
		{
			if ( _initialMeasuresNeeded )
				return;

			int x = LEFT_PAD;
			_textTop = ( this.Height - _fontHeight ) / 2 - VRECT_PAD;
			if ( _useHour )
			{
				_hourRect = new Rectangle( x, _textTop, _hourWidth+(2*HRECT_PAD), _fontHeight + (2*VRECT_PAD) );
				x += _hourRect.Width + _sepWidth + 2*HRECT_PAD;
			}

			if ( _useMin )
			{
				_minuteRect = new Rectangle( x, _textTop, _minSecWidth+(2*HRECT_PAD), _fontHeight + (2*VRECT_PAD) );
				x += _minuteRect.Width + _sepWidth + 2*HRECT_PAD;
			}

			if ( _useSec )
			{
				_secondRect = new Rectangle( x, _textTop, _minSecWidth+(2*HRECT_PAD), _fontHeight + (2*VRECT_PAD) );
				x += _secondRect.Width + _sepWidth + 2*HRECT_PAD;
			}

			if ( _useFrac )
				_fractionRect = new Rectangle( x, _textTop, _fracWidth+(2*HRECT_PAD), _fontHeight + (2*VRECT_PAD) );
		}

		/*
		 * Properties
		 */

		public bool ShowUpDown
		{
			get
			{
				return true;
			}
			set
			{
				// just a dummy for compatibility with Desktop DateTimePicker
			}
		}

		public DateTime Value
		{
			get
			{
				return new DateTime( 2000, 1, 1, _hour, _min, _sec, _frac );
			}
			set
			{
				_hour = value.Hour;
				_min  = value.Minute;
				_sec  = value.Second;
				_frac = value.Millisecond;
			}
		}

		public DateTimePickerFormat Format
		{
			get
			{
				return _format;
			}
			set
			{
				if ( value == DateTimePickerFormat.Long || 
					 value == DateTimePickerFormat.Short )
					throw new NotSupportedException( "This DateTimePicker only accepts Custom and Time formats" );

				if ( value == DateTimePickerFormat.Time )
				{
					_useFrac = false;
					_useHour = _useMin = _useSec = true;
					_selectedUnit = HOUR;
				}

				_format = value;
			}
		}

		public string CustomFormat
		{
			get
			{	
				string formatString = _useHour ? "HH" : "";

				if ( _useHour && _useMin )
					formatString += ":";

				if ( _useMin )
				{
					formatString += "mm";
					if ( _useSec ) 
						formatString += ":";
				}

				if ( _useSec )
				{
					formatString += "ss";
					if ( _useFrac )
						formatString += ".";
				}

				if ( _useFrac )
					formatString += "fff";

				return formatString;
			}
			set
			{
				if ( _format != DateTimePickerFormat.Custom )
					return;

				// this control only supports the following formats:
				// "HH:mm:ss", "HH:mm", "mm:ss", "HH", "mm", "ss"
				// so the parsing is very simple!
				_useHour = value.IndexOf( "HH" ) > -1;
				_useMin = value.IndexOf( "mm" ) > -1;
				_useSec = value.IndexOf( "ss" ) > -1;
				_useFrac = value.IndexOf( "f" ) > -1;

				if ( _useHour ) _selectedUnit = HOUR;
				else if ( _useMin ) _selectedUnit = MINUTE;
				else if ( _useSec ) _selectedUnit = SECOND;
				else if ( _useFrac ) _selectedUnit = MILLISECOND;
			}
		}


		/*
		 * Member Methods
		 */

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
							e.Handled = true;
							break;
						}
					break;

				case Keys.Down:
					loc = Parent.Controls.GetChildIndex( this ) + 1;
					for( int i = loc; i < Parent.Controls.Count; i++ )
						if ( Parent.Controls[ i ].Enabled )
						{
							Parent.Controls[ i ].Focus();
							e.Handled = true;
							break;
						}
					break;

				case Keys.Left:
					switch ( _selectedUnit-1 )
					{
						case HOUR:
							if ( !_useHour )
								return;
							_selectedUnit--;
							break;
						case MINUTE:
							if ( !_useMin )
								return;
							_selectedUnit--;
							break;
						case SECOND:
							if ( !_useSec )
								return;
							_selectedUnit--;
							break;
						case MILLISECOND:
							if ( !_useFrac )
								return;
							_selectedUnit--;
							break;
					}
					e.Handled = true;
					this.Invalidate();
					break;

				case Keys.Right:
					switch ( _selectedUnit+1 )
					{
						case HOUR:
							if ( !_useHour )
								return;
							_selectedUnit++;
							break;
						case MINUTE:
							if ( !_useMin )
								return;
							_selectedUnit++;
							break;
						case SECOND:
							if ( !_useSec )
								return;
							_selectedUnit++;
							break;
						case MILLISECOND:
							if ( !_useFrac )
								return;
							_selectedUnit++;
							break;
					}
					e.Handled = true;
					this.Invalidate();
					break;
			}

			if ( this.ControlChange != null && !e.Handled && ( e.KeyCode == Keys.Up || e.KeyCode == Keys.Down ) )
				ControlChange( this, e );
		}
		
		protected override void OnKeyPress(KeyPressEventArgs e)
		{
			base.OnKeyPress(e);

			int number = (int)e.KeyChar - (int)'0';
			if ( number < 0 || number > 9 )
				return;

			int oldVal = 0, newVal = 0;

			switch( _selectedUnit )
			{
				case HOUR:
					oldVal = _hour;
					newVal = (oldVal * 10) + number;
					if ( newVal < 0 || newVal > 23 )
						_hour = number;
					else
						_hour = newVal;
					break;

				case MINUTE:
					oldVal = _min;
					newVal = (oldVal * 10) + number;
					if ( newVal < 0 || newVal > 59 )
						_min = number;
					else
						_min = newVal;
					break;

				case SECOND:
					oldVal = _sec;
					newVal = (oldVal * 10) + number;
					if ( newVal < 0 || newVal > 59 )
						_sec = number;
					else
						_sec = newVal;
					break;

				case MILLISECOND:
					oldVal = _frac;
					newVal = (oldVal * 10) + number;
					if ( newVal < 0 || newVal > 999 )
						_frac = number;
					else
						_frac = newVal;
					break;
			}

			if ( oldVal != newVal && ValueChanged != null )
				ValueChanged( this, new EventArgs() );

			this.Invalidate();
		}


		protected override void OnPaintBackground(PaintEventArgs e)
		{
			// base.OnPaintBackground (e);
			// for double-buffering
		}

		protected override void OnPaint(PaintEventArgs e)
		{
			if ( _initialMeasuresNeeded )
			{
				_initialMeasuresNeeded = false;

				_hourWidth = (int)Math.Ceiling( e.Graphics.MeasureString( "23", this.Font ).Width );
				_minSecWidth = (int)Math.Ceiling( e.Graphics.MeasureString( "56", this.Font ).Width );
				_fracWidth = (int)Math.Ceiling( e.Graphics.MeasureString( "565", this.Font ).Width );
				_sepWidth = (int)Math.Ceiling( e.Graphics.MeasureString( ":", this.Font ).Width );
				_fontHeight = (int)Math.Ceiling( e.Graphics.MeasureString( "23:56:56", this.Font ).Height );

				calcRects();
			}

			// used for double-buffering
			Graphics offscreen = Graphics.FromImage( _imageBuffer );

			offscreen.FillRectangle( _whiteBrush, 0, 0, this.Width, this.Height );

			if ( !this.Focused )
			{
				string time = "";
				
				if ( _useHour ) time += _hour;
				if ( _useHour && _useMin ) time += ":";
				if ( _useMin ) time += formatString( _min, false );
				if ( _useMin && _useSec ) time += ":";
				if ( _useSec ) time += formatString( _sec, false );
				if ( _useSec && _useFrac ) time += ".";
				if ( _useFrac ) time += formatString( _frac, true );

				offscreen.DrawString( time, this.Font, _blackBrush, _hourRect.X, _hourRect.Y );
			}
			else 
			{
				offscreen.DrawRectangle( _blackPen, 0, 0, this.Size.Width-1, this.Size.Height-1 );

				Brush textBrush = _blackBrush;

				// draw time
				if ( _useHour )
				{
					SizeF s = offscreen.MeasureString( _hour.ToString(), this.Font );
					float x = (float)_hourRect.X + ((float)_hourRect.Width - s.Width) / 2;

					if ( _selectedUnit == HOUR )
					{
						offscreen.FillRectangle( _selectedBrush, (int)x-HRECT_PAD, _hourRect.Y + VRECT_PAD, (int)s.Width+2*HRECT_PAD, (int)s.Height+2*VRECT_PAD );
						textBrush = _whiteBrush;
					}

					offscreen.DrawString( _hour.ToString(), this.Font, textBrush, x, (float)(_hourRect.Y + VRECT_PAD) );
				}

				if ( _useHour && _useMin )
					drawSeparator( offscreen, _hourRect.X + _hourRect.Width + HRECT_PAD, ":" );

				textBrush = _blackBrush;

				if ( _useMin )
				{
					string minString = formatString( _min, false );

					SizeF s = offscreen.MeasureString( minString, this.Font );
					float x = (float)_minuteRect.X + ((float)_minuteRect.Width - s.Width) / 2;

					if ( _selectedUnit == MINUTE )
					{
						offscreen.FillRectangle( _selectedBrush, _minuteRect );
						textBrush = _whiteBrush;
					}

					offscreen.DrawString( minString, this.Font, textBrush, x, (float)(_minuteRect.Y + VRECT_PAD) );
				}

				if ( _useSec && _useMin )
					drawSeparator( offscreen, _minuteRect.X + _minuteRect.Width + HRECT_PAD, ":" );

				textBrush = _blackBrush;

				if ( _useSec )
				{
					string secString = formatString( _sec, false );

					SizeF s = offscreen.MeasureString( secString, this.Font );
					float x = (float)_secondRect.X + ((float)_secondRect.Width - s.Width) / 2;

					if ( _selectedUnit == SECOND )
					{
						offscreen.FillRectangle( _selectedBrush, _secondRect );
						textBrush = _whiteBrush;
					}

					offscreen.DrawString( secString, this.Font, textBrush, x, (float)(_secondRect.Y + VRECT_PAD) );
				}

				if ( _useSec && _useFrac )
					drawSeparator( offscreen, _secondRect.X + _secondRect.Width + HRECT_PAD, "." );

				textBrush = _blackBrush;

				if ( _useFrac )
				{
					string fracString = formatString( _frac, true );

					SizeF s = offscreen.MeasureString( fracString, this.Font );
					float x = (float)_fractionRect.X + ((float)_fractionRect.Width - s.Width) / 2;

					if ( _selectedUnit == MILLISECOND )
					{
						offscreen.FillRectangle( _selectedBrush, _fractionRect );
						textBrush = _whiteBrush;
					}

					offscreen.DrawString( fracString, this.Font, textBrush, x, (float)(_fractionRect.Y + VRECT_PAD) );
				}
			}

			// draw image to screen
			e.Graphics.DrawImage( _imageBuffer, 0, 0 );
		}

		protected string formatString( int num, bool frac )
		{
			if ( frac && num < 10 )
				return "00" + num.ToString();
			else if ( ( frac && num < 100 ) || ( num < 10 ) )
				return "0" + num.ToString();
			else
				return num.ToString();
		}

		protected void drawSeparator( Graphics g, int x, string sep )
		{
			g.DrawString( sep, this.Font, _blackBrush, x, _textTop );
		}

		protected void DateTimePicker_GotFocus(object sender, EventArgs e)
		{
			this.Invalidate();
		}

		protected void DateTimePicker_LostFocus(object sender, EventArgs e)
		{
			this.Invalidate();
		}
	}

	public enum DateTimePickerFormat
	{
		Custom, Long, Short, Time
	}
}
