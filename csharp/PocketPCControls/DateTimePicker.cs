using System;
using System.Drawing;
using System.Windows.Forms;

namespace PocketPCControls
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

		protected const bool UP			= true;

		protected const int ARROW_WIDTH	= 15;
		protected const int HALF_ARROW	= 8;

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

		protected int		_hour;
		protected int		_min;
		protected int		_sec;

		protected bool		_selected;
		protected int		_selectedUnit;
		protected Rectangle	_upArrowRect;
		protected bool		_downInUpArrow;
		protected Rectangle	_downArrowRect;
		protected bool		_downInDownArrow;
		protected bool		_focus;

		protected Rectangle _hourRect;
		protected Rectangle _minuteRect;
		protected Rectangle _secondRect;
		protected int		_textTop;

		protected Image		_imageBuffer;

		protected bool		_initialMeasuresNeeded;
		protected int		_hourWidth;
		protected int		_minSecWidth;
		protected int		_sepWidth;
		protected int		_fontHeight;

		protected Timer		_timer;
		protected bool		_timerUp;

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


		/*
		 * Constructor
		 */

		public DateTimePicker()
		{
			_selected = false;
			_selectedUnit = 0;
			_imageBuffer = new Bitmap( this.Width, this.Height );
			_initialMeasuresNeeded = true;

			Format = DateTimePickerFormat.Time;

			_timer = new Timer();
			_timer.Enabled = false;
			_timer.Tick += new EventHandler(_timer_Tick);

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

			_upArrowRect = new Rectangle( this.Width - ( 2 * ARROW_WIDTH ) - 1, 0, ARROW_WIDTH, this.Height - 1 );
			_downArrowRect = new Rectangle( this.Width - ARROW_WIDTH - 1, 0, ARROW_WIDTH, this.Height - 1 );

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
				_secondRect = new Rectangle( x, _textTop, _minSecWidth+(2*HRECT_PAD), _fontHeight + (2*VRECT_PAD) );
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
				return new DateTime( 2000, 1, 1, _hour, _min, _sec );
			}
			set
			{
				_hour = value.Hour;
				_min  = value.Minute;
				_sec  = value.Second;
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
					_useHour = _useMin = _useSec = true;

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
					formatString += "ss";

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
			}
		}


		/*
		 * Member Methods
		 */

		protected override void OnMouseDown(MouseEventArgs e)
		{
			this.Focus();

			if ( _downArrowRect.Contains( e.X, e.Y ) )
			{
				_timerUp = false;
				_selected = true;
				_downInDownArrow = true;

				_timer_Tick(this,new EventArgs());
				_timer.Interval = 1000;
				_timer.Enabled = true;

				this.Invalidate();
			}
			else if ( _upArrowRect.Contains( e.X, e.Y ) )
			{
				_selected = true;
				_downInUpArrow = true;
				_timerUp = true;
				
				_timer_Tick(this,new EventArgs());
				_timer.Interval = 1000;
				_timer.Enabled = true;

				this.Invalidate();
			}
			else if ( _useHour && _hourRect.Contains( e.X, e.Y ) )
			{
				_selected = true;
				_selectedUnit = HOUR;
			}
			else if ( _useMin && _minuteRect.Contains( e.X, e.Y ) )
			{
				_selected = true;
				_selectedUnit = MINUTE;
			}
			else if ( _useSec && _secondRect.Contains( e.X, e.Y ) )
			{
				_selected = true;
				_selectedUnit = SECOND;
			}
		}

		protected override void OnMouseUp(MouseEventArgs e)
		{
			_timer.Enabled = false;
			_downInUpArrow = false;
			_downInDownArrow = false;

			this.Invalidate();
		}

		protected override void OnKeyPress(KeyPressEventArgs e)
		{
			base.OnKeyPress(e);

			int number = (int)e.KeyChar - (int)'0';
			if ( number < 0 || number > 9 )
				return;

			_selected = true;

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
				_sepWidth = (int)Math.Ceiling( e.Graphics.MeasureString( ":", this.Font ).Width );
				_fontHeight = (int)Math.Ceiling( e.Graphics.MeasureString( "23:56:56", this.Font ).Height );

				calcRects();
			}

			// used for double-buffering
			Graphics offscreen = Graphics.FromImage( _imageBuffer );

			offscreen.FillRectangle( _whiteBrush, 0, 0, this.Width, this.Height );

			if ( this.Enabled )
				offscreen.DrawRectangle( _blackPen, 0, 0, this.Size.Width-1, this.Size.Height-1 );
			else
				offscreen.DrawRectangle( _disabledBorder, 0, 0, this.Size.Width-1, this.Size.Height-1 );

			// draw arrows
			drawArrow( offscreen, UP, _upArrowRect, _downInUpArrow );
			drawArrow( offscreen, !UP, _downArrowRect, _downInDownArrow );

			Brush textBrush = _blackBrush;

			// draw time
			if ( _useHour )
			{
				SizeF s = offscreen.MeasureString( _hour.ToString(), this.Font );
				float x = (float)_hourRect.X + ((float)_hourRect.Width - s.Width) / 2;

				if ( _selected && _selectedUnit == HOUR && _focus )
				{
					offscreen.FillRectangle( _selectedBrush, (int)x-HRECT_PAD, _hourRect.Y + VRECT_PAD, (int)s.Width+2*HRECT_PAD, (int)s.Height+2*VRECT_PAD );
					textBrush = _whiteBrush;
				}

				offscreen.DrawString( _hour.ToString(), this.Font, textBrush, x, (float)(_hourRect.Y + VRECT_PAD) );
			}

			if ( _useHour && _useMin )
				drawSeparator( offscreen, _hourRect.X + _hourRect.Width + HRECT_PAD );

			textBrush = _blackBrush;

			if ( _useMin )
			{
				string minString = formatString( _min );

				SizeF s = offscreen.MeasureString( minString, this.Font );
				float x = (float)_minuteRect.X + ((float)_minuteRect.Width - s.Width) / 2;

				if ( _selected && _selectedUnit == MINUTE && _focus )
				{
					offscreen.FillRectangle( _selectedBrush, _minuteRect );
					textBrush = _whiteBrush;
				}

				offscreen.DrawString( minString, this.Font, textBrush, x, (float)(_minuteRect.Y + VRECT_PAD) );
			}

			if ( _useSec && _useMin )
				drawSeparator( offscreen, _minuteRect.X + _minuteRect.Width + HRECT_PAD );

			textBrush = _blackBrush;

			if ( _useSec )
			{
				string secString = formatString( _sec );

				SizeF s = offscreen.MeasureString( secString, this.Font );
				float x = (float)_secondRect.X + ((float)_secondRect.Width - s.Width) / 2;

				if ( _selected && _selectedUnit == SECOND && _focus )
				{
					offscreen.FillRectangle( _selectedBrush, _secondRect );
					textBrush = _whiteBrush;
				}

				offscreen.DrawString( secString, this.Font, textBrush, x, (float)(_secondRect.Y + VRECT_PAD) );
			}

			// draw image to screen
			e.Graphics.DrawImage( _imageBuffer, 0, 0 );
		}

		protected string formatString( int num )
		{
			if ( num < 10 )
				return "0" + num.ToString();
			else
				return num.ToString();
		}

		protected void drawSeparator( Graphics g, int x )
		{
			g.DrawString( ":", this.Font, _blackBrush, x, _textTop );
		}

		protected void drawArrow( Graphics g, bool dir, Rectangle rect, bool selected )
		{
			g.DrawRectangle( _blackPen, rect );

			int startY = rect.Y + rect.Height / 2 - 1;
			int midX = rect.X + HALF_ARROW;

			Pen pen = selected ? _whitePen : _blackPen;

			if ( selected )
				g.FillRectangle( _selectedBrush, rect.X+1, rect.Y+1, rect.Width-1, rect.Height-1 );

			if ( dir == UP )
			{
				// draw up arrow
				g.DrawLine( pen, midX, startY, midX, startY+1 );
				for( int i = 1; i < 4; i++ )
				{
					g.DrawLine( pen, midX-i, startY+i, midX+i, startY+i );
				}
			}
			else
			{
				// draw down arrow
				for( int i = 3; i >= 1; i--, startY++ )
				{
					g.DrawLine( pen, midX-i, startY, midX+i, startY );
				}
				g.DrawLine( pen, midX, startY-1, midX, startY );
			}
		}

		private void _timer_Tick(object sender, EventArgs e)
		{
			_timer.Interval = 500;

			if ( _timerUp )
			{
				switch( _selectedUnit )
				{
					case HOUR:
						_hour++;
						if ( _hour > 23 ) _hour = 0;
						break;

					case MINUTE:
						_min--;
						if ( _min > 59 ) _min = 0;
						break;

					case SECOND:
						_sec--;
						if ( _sec > 59 ) _sec = 0;
						break;
				}
			}
			else
			{
				switch( _selectedUnit )
				{
					case HOUR:
						_hour--;
						if ( _hour < 0 ) _hour = 23;
						break;

					case MINUTE:
						_min--;
						if ( _min < 0 ) _min = 59;
						break;

					case SECOND:
						_sec--;
						if ( _sec < 0 ) _sec = 59;
						break;
				}
			}

			if ( ValueChanged != null )
				ValueChanged( this, new EventArgs() );

			this.Invalidate();
		}

		private void DateTimePicker_GotFocus(object sender, EventArgs e)
		{
			_focus = true;
		}

		private void DateTimePicker_LostFocus(object sender, EventArgs e)
		{
			_focus = false;
		}
	}

	public enum DateTimePickerFormat
	{
		Custom, Long, Short, Time
	}
}
