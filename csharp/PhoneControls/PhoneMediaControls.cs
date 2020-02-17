using System;
using System.Drawing;
using System.Windows.Forms;

namespace PhoneControls
{
	/// <summary>
	/// This is a media controls widget for the Smartphone.
	/// It is based on screenshots of Windows Media Player
	/// for Smartphone.  Unfortunately, WMP for Smartphone
	/// does not have a stop function.
	/// </summary>
	public class PhoneMediaControls : Control
	{
		/*
		 * Constants
		 */

		protected const string PREFIX			= "PhonePUC.PhoneControls.";

		protected const string PLAY				= "play.bmp";
		protected const string STOP				= "stop.bmp";
		protected const string PAUSE			= "pause.bmp";
		protected const string NEXT_TRACK_UP	= "next-track-up.bmp";
		protected const string NEXT_TRACK_DOWN	= "next-track-down.bmp";
		protected const string NEXT_TRACK_DBLD  = "next-track-disabled.bmp";
		protected const string PREV_TRACK_UP	= "prev-track-up.bmp";
		protected const string PREV_TRACK_DOWN	= "prev-track-down.bmp";
		protected const string PREV_TRACK_DBLD  = "prev-track-disabled.bmp";

		protected const bool   PLAYING			= true;


		/*
		 * Member Variables
		 */

		protected static Image _playImg;
		protected static Image _pauseImg;
		protected static Image _stopImg;
		protected static Image _nextTrackImgUp;
		protected static Image _nextTrackImgDown;
		protected static Image _prevTrackImgUp;
		protected static Image _prevTrackImgDown;
		protected static Image _nextTrackImgDisabled;
		protected static Image _prevTrackImgDisabled;

		protected static int   _startLeft;
		protected static int   _startMiddle;
		protected static int   _startRight;

		protected bool _nextTrackDown;
		protected bool _nextTrackEnabled;
		protected bool _prevTrackDown;
		protected bool _prevTrackEnabled;
		protected bool _playMode;
		protected bool _showStop;
		protected bool _processStop;

		protected Image _imageBuffer;

		protected System.Threading.Timer _stopTimer;
		protected long  _stopTime;


		/*
		 * Static Constructor
		 */

		static PhoneMediaControls()
		{
			System.Reflection.Assembly thisExe = 
				System.Reflection.Assembly.GetExecutingAssembly();

			_playImg = new Bitmap( thisExe.GetManifestResourceStream( PREFIX + PLAY ) );
			_stopImg = new Bitmap( thisExe.GetManifestResourceStream( PREFIX + STOP ) );
			_pauseImg = new Bitmap( thisExe.GetManifestResourceStream( PREFIX + PAUSE ) );
			_nextTrackImgUp = new Bitmap( thisExe.GetManifestResourceStream( PREFIX + NEXT_TRACK_UP ) );
			_nextTrackImgDown = new Bitmap( thisExe.GetManifestResourceStream( PREFIX + NEXT_TRACK_DOWN ) );
			_prevTrackImgUp = new Bitmap( thisExe.GetManifestResourceStream( PREFIX + PREV_TRACK_UP ) );
			_prevTrackImgDown = new Bitmap( thisExe.GetManifestResourceStream( PREFIX + PREV_TRACK_DOWN ) );
			_nextTrackImgDisabled = new Bitmap( thisExe.GetManifestResourceStream( PREFIX + NEXT_TRACK_DBLD ) );
			_prevTrackImgDisabled = new Bitmap( thisExe.GetManifestResourceStream( PREFIX + PREV_TRACK_DBLD ) );
		}

		
		/*
		 * Events
		 */

		public event EventHandler NextTrack;
		public event EventHandler PrevTrack;
		public event EventHandler Play;
		public event EventHandler Stop;
		public event EventHandler Pause;


		/*
		 * Constructors
		 */

		public PhoneMediaControls()
		{
			_nextTrackDown = false;
			_nextTrackEnabled = false;
			_prevTrackDown = false;
			_prevTrackEnabled = false;
			_playMode = false;
			_showStop = false;	
			_processStop = false;

			_imageBuffer = new Bitmap( this.Width, this.Height );

			// _stopTimer = new System.Threading.Timer( new System.Threading.TimerCallback(this._stopTimer_Tick), null, System.Threading.Timeout.Infinite, System.Threading.Timeout.Infinite );
		}


		/*
		 * Properties
		 */
		
		public bool NextTrackEnabled
		{
			get
			{
				return _nextTrackEnabled;
			}
			set
			{
				_nextTrackEnabled = value;
				this.Invalidate();
			}
		}

		public bool PrevTrackEnabled
		{
			get
			{
				return _prevTrackEnabled;
			}
			set
			{
				_prevTrackEnabled = value;
				this.Invalidate();
			}
		}

		public bool Playing
		{
			get
			{
				return _playMode;
			}
			set
			{
				_playMode = value;
				this.Invalidate();
			}
		}


		/*
		 * Member Methods
		 */

		protected override void OnResize(EventArgs e)
		{
			base.OnResize (e);

			_imageBuffer = new Bitmap( this.Width, this.Height );

			int totalWidth = _prevTrackImgUp.Width + _playImg.Width + _nextTrackImgUp.Width;
			int spacer = ( this.Width - totalWidth ) / 2;
			
			_startLeft = Math.Max( 1, spacer );
			_startMiddle = _startLeft + _prevTrackImgUp.Width;
			_startRight = _startMiddle + _playImg.Width;

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

				case Keys.Enter:
					if ( _playMode == PLAYING )
					{
						// start stop timer
						_processStop = true;
						//_stopTimer.Change( 2000, System.Threading.Timeout.Infinite );
						if ( Pause != null )
							Pause( this, new EventArgs() );
					}
					else
					{
						_playMode = PLAYING;
						if ( Play != null )
							Play( this, new EventArgs() );
					}
					this.Invalidate();
					break;

				case Keys.Left:
					_prevTrackDown = true;
					this.Invalidate();
					break;

				case Keys.Right:
					_nextTrackDown = true;
					this.Invalidate();
					break;
			}
		}

		protected override void OnKeyUp(KeyEventArgs e)
		{
			base.OnKeyUp (e);

			switch( e.KeyCode )
			{
				case Keys.Left:
					_prevTrackDown = false;
					if ( PrevTrack != null )
						PrevTrack( this, new EventArgs() );
					this.Invalidate();
					break;

				case Keys.Right:
					_nextTrackDown = false;
					if ( NextTrack != null )
						NextTrack( this, new EventArgs() );
					this.Invalidate();
					break;

				case Keys.Enter:
					if ( _processStop )
					{
						_processStop = false;
						_playMode = !PLAYING;
						if ( _showStop && ( _playMode != PLAYING ) && ( Stop != null ) )
							Stop( this, new EventArgs() );
						_showStop = false;
					}
					this.Invalidate();
					break;
			}
		}

		protected override void OnPaintBackground(PaintEventArgs e)
		{
			// to get rid of flicker
		}

		protected override void OnPaint(PaintEventArgs e)
		{
			Pen blackPen = new Pen( Color.Black );

			// used for double-buffering
			Graphics offscreen = Graphics.FromImage( _imageBuffer );

			offscreen.FillRectangle( new SolidBrush( Color.White ), 0, 0, this.Width, this.Height );

			if ( this.Focused )
				offscreen.DrawRectangle( blackPen, 0, 0, this.Width - 1, _playImg.Height + 2 );

			if ( !_prevTrackEnabled )
				offscreen.DrawImage( _prevTrackImgDisabled, _startLeft, 1 );
			else if ( _prevTrackDown )
				offscreen.DrawImage( _prevTrackImgDown, _startLeft, 1 );
			else
				offscreen.DrawImage( _prevTrackImgUp, _startLeft, 1 );

			if ( _playMode == !PLAYING )
				offscreen.DrawImage( _playImg, _startMiddle, 1 );
			else if ( _showStop )
				offscreen.DrawImage( _stopImg, _startMiddle, 1 );
			else
				offscreen.DrawImage( _pauseImg, _startMiddle, 1 );

			if ( !_nextTrackEnabled )
				offscreen.DrawImage( _nextTrackImgDisabled, _startRight, 1 );
			else if ( _nextTrackDown )
				offscreen.DrawImage( _nextTrackImgDown, _startRight, 1 );
			else
				offscreen.DrawImage( _nextTrackImgUp, _startRight, 1 );

			// draw image to screen
			e.Graphics.DrawImage( _imageBuffer, 0, 0 );
		}
	}
}
