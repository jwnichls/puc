using System;
using System.Drawing;
using System.Windows.Forms;

namespace PocketPCControls
{
	/// <summary>
	/// Summary description for TimeSlider.
	/// </summary>
	public class TimeSlider : Control
	{
		/*
		 * Constants
		 */

		public const int MINIMUM_HEIGHT = 15;
		public const int DEFAULT_HEIGHT = 20;
		public const int MINIMUM_WIDTH  = 30;
		public const int VERTICAL_TEXT_OFFSET = 3;
		public const int AVERAGE_LABEL_WIDTH  = 50;

		protected const int SPACER       = 6;


		/*
		 * Member Variables
		 */

		protected Brush				_textBrush = new SolidBrush( Color.Black );

		protected double			_time;
		protected double			_maxtime;
		protected HScrollBar		_scrollbar;
		protected int				_correctionFactor;

		/*
		 * Events
		 */

		public event EventHandler TimeChanged;


		/*
		 * Constructors
		 */

		public TimeSlider()
		{
			_scrollbar = new HScrollBar();
			this.Controls.Add( _scrollbar );
			_scrollbar.Location = new Point( 0, 0 );

			_scrollbar.ValueChanged +=new EventHandler(_scrollbar_ValueChanged);

			this.Resize += new EventHandler(this.Resized);

			refreshType();
		}


		/*
		 * Properties
		 */

		public double Time
		{
			get
			{
				return _time;
			}
			set
			{
				_time = value;

				refreshDisplay();
			}
		}

		public double Maximum
		{
			get
			{
				return _maxtime;
			}
			set
			{
				_maxtime = value;

				refreshType();
			}
		}


		/*
		 * Member Methods
		 */

		protected void Resized( object source, EventArgs e )
		{
			_scrollbar.Size = new Size( this.Size.Width - 1, DEFAULT_HEIGHT );
			this.Invalidate();
		}

		protected string dblTimeToString( double time, TimeUnits mostSignificantUnit )
		{
			string sep = ":";

			string ret = "";
			int inttime = (int)Math.Floor( time );

			int secs = inttime % 60;
			inttime /= 60;

			int mins = inttime % 60;
			inttime /= 60;

			int hours = inttime;

			if ( hours > 0 || mostSignificantUnit == TimeUnits.Hours )
				ret += hours.ToString() + sep;

			if ( mins > 0 || mostSignificantUnit <= TimeUnits.Minutes )
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

		protected string generateTimeString()
		{
			TimeUnits unit = TimeUnits.Minutes; // = TimeUnits.Seconds;

			int inttime = (int)Math.Floor( _maxtime );

			int secs = inttime % 60;
			inttime /= 60;

			int mins = inttime % 60;
			inttime /= 60;

			int hours = inttime;

			/*
			if ( mins > 0 )
				unit = TimeUnits.Minutes;*/
			
			if ( hours > 0 )
				unit = TimeUnits.Hours;

			return dblTimeToString( _time, unit ) + "/" + dblTimeToString( _maxtime, unit );
		}

		protected void refreshType() 
		{
			// reset the scrollbar appropriately
			if ( _maxtime <= 0 )
			{
				_scrollbar.Enabled = false;
				return;
			}
			else
				_scrollbar.Enabled = true;

			_correctionFactor = 0;
			double mtime = _maxtime;
			while( Math.Floor( mtime ) != mtime )
			{
				_correctionFactor++;
				mtime *= 10;
			}

			int intmaxtime = (int)mtime;

			_scrollbar.Minimum = 0;
			_scrollbar.LargeChange = (int)(mtime * 0.05);
			_scrollbar.Maximum = intmaxtime + _scrollbar.LargeChange - 1;

			refreshDisplay();
		}

		protected void refreshDisplay()
		{
			// set the value in the scrollbar
			double time = _time;
			for( int i = 0; i < _correctionFactor; i++ )
				time *= 10;

			_scrollbar.Value = (int)time;
			this.Invalidate();
		}

		protected override void OnPaint(PaintEventArgs e)
		{
			base.OnPaint (e);

			e.Graphics.FillRectangle( new SolidBrush( this.Parent.BackColor ), 0, 0, this.Size.Width-1, this.Size.Height-1 );

			string timeStr = generateTimeString();

			int labelWidth = (int)e.Graphics.MeasureString( timeStr, this.Font ).Width;

			e.Graphics.DrawString( timeStr, this.Font, _textBrush, this.Size.Width - labelWidth - 1, VERTICAL_TEXT_OFFSET + _scrollbar.Size.Height );
		}

		private void _scrollbar_ValueChanged(object sender, EventArgs e)
		{
			_time = ((double)_scrollbar.Value) / Math.Pow( 10, _correctionFactor );
			this.Invalidate();

			if ( TimeChanged != null )
				TimeChanged( this, new EventArgs() );
		}
	}
}
