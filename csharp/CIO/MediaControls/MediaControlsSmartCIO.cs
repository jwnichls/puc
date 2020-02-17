using System;
using System.Collections;
using System.Drawing;
using System.Windows.Forms;

#if POCKETPC
using PocketPCControls;
#endif
#if DESKTOP
using DesktopControls;
#endif

using PUC;
using PUC.CIO;
using PUC.Communication;
using PUC.Types;


namespace PUC.CIO.MediaControls
{
	/// <summary>
	/// Summary description for TimeDurationSmartCIO.
	/// </summary>
	public class MediaControlsSmartCIO : SmartCIO
	{
		/*
		 * Constants
		 */

		public const string HIGH_LEVEL_TYPE = "media-controls";

		// constants for parsing multiple commands
		protected const string PLAY_LABEL			= "Play";
		protected const string STOP_LABEL			= "Stop";
		protected const string PAUSE_LABEL			= "Pause";
		protected const string REWIND_LABEL			= "Rewind";
		protected const string FFWD_LABEL			= "Fast-Forward";
		protected const string RECORD_LABEL			= "Record";
		protected const string NEXT_TRK_CMD_LABEL	= "NextTrack";
		protected const string PREV_TRK_CMD_LABEL	= "PrevTrack";
		protected const string MODE_STATE_LABEL     = "Mode";

		// constants for judging ImageButton layout
		public const int BTN_DEFAULT_HEIGHT			= 24;
		public const int BTN_SPACER					= 5;

		// constants for loading embedded images
		protected const string STANDARD_ICON		= "-icon.bmp";
		protected const string DISABLED_ICON		= "-disabled.bmp";
		protected const string HIGHLIGHT_ICON		= "-highlight.bmp";
		protected const string FFWD_IMG_NAME		= "ffwd";
		protected const string NEXTTRACK_IMG_NAME	= "nexttrack";
		protected const string PAUSE_IMG_NAME		= "pause";
		protected const string PLAY_IMG_NAME		= "play";
		protected const string PREVTRACK_IMG_NAME	= "prevtrack";
		protected const string RECORD_IMG_NAME		= "record";
		protected const string REWIND_IMG_NAME		= "rewind";
		protected const string STOP_IMG_NAME		= "stop";
#if POCKETPC
		protected const string PREFIX				= "PUC.CIO.MediaControls.";
#endif
#if DESKTOP
		protected const string PREFIX				= "DesktopPUC.CIO.MediaControls.";
#endif
#if SMARTPHONE
		protected const string PREFIX				= "PhonePUC.CIO.MediaControls.";
#endif


		/*
		 * Member Variables
		 */

		// valid in single state and sometimes in multiple state situations
		protected ApplianceState _playState;

		protected Hashtable      _valueToActionMap;
		protected ArrayList      _mediaActions;

		protected ImageButton	 _playBtn;
		protected ImageButton	 _stopBtn;
		protected ImageButton	 _pauseBtn;
		protected ImageButton	 _rewindBtn;
		protected ImageButton	 _fastFwdBtn;
		protected ImageButton	 _recordBtn;
		protected ImageButton	 _prevTrackBtn;
		protected ImageButton	 _nextTrackBtn;

		protected Size			 _requiredSize;


		/*
		 * Static Methods (for Dynamic Class Loading)
		 */

		public static SmartCIO CreateMediaControlsSmartCIO( PUC.GroupNode group )
		{
			return new MediaControlsSmartCIO( group );
		}

		
		/*
		 * Constructor
		 */

		public MediaControlsSmartCIO( GroupNode specSnippet )
			: base( new Panel(), specSnippet )
		{
			_mediaActions = new ArrayList();

			if ( _specSnippet.IsObject() )
			{
				// single state translations
				_playState = (ApplianceState)_objects[ SINGLE_STATE ];

				createButtonsFromState();
			}
			else
			{
				// multiple state translation

				_playState = (ApplianceState)_objects[ MODE_STATE_LABEL ];

				createButtonsFromState();

				IEnumerator objenum = _objects.Keys.GetEnumerator();
				while( objenum.MoveNext() )
				{
					string name = (string)objenum.Current;

					try 
					{
						ApplianceCommand cmd = (ApplianceCommand)_objects[ name ];

						switch( name )
						{
							case PLAY_LABEL:
								_playBtn = createImageButton( PLAY_IMG_NAME );
								_mediaActions.Add( new MediaCommandAction( _playBtn, cmd ) );
								doNotRenderObject( cmd );
								break;

							case STOP_LABEL:
								_stopBtn = createImageButton( STOP_IMG_NAME );
								_mediaActions.Add( new MediaCommandAction( _stopBtn, cmd ) );
								doNotRenderObject( cmd );
								break;

							case PAUSE_LABEL:
								_pauseBtn = createImageButton( PAUSE_IMG_NAME );
								_mediaActions.Add( new MediaCommandAction( _pauseBtn, cmd ) );
								doNotRenderObject( cmd );
								break;

							case REWIND_LABEL:
								_rewindBtn = createImageButton( REWIND_IMG_NAME );
								_mediaActions.Add( new MediaCommandAction( _rewindBtn, cmd ) );
								doNotRenderObject( cmd );
								break;

							case FFWD_LABEL:
								_fastFwdBtn = createImageButton( FFWD_IMG_NAME );
								_mediaActions.Add( new MediaCommandAction( _fastFwdBtn, cmd ) );
								doNotRenderObject( cmd );
								break;

							case RECORD_LABEL:
								_recordBtn = createImageButton( RECORD_IMG_NAME );
								_mediaActions.Add( new MediaCommandAction( _recordBtn, cmd ) );
								doNotRenderObject( cmd );
								break;

							case NEXT_TRK_CMD_LABEL:
								_nextTrackBtn = createImageButton( NEXTTRACK_IMG_NAME );
								_mediaActions.Add( new MediaCommandAction( _nextTrackBtn, cmd ) );
								doNotRenderObject( cmd );
								break;

							case PREV_TRK_CMD_LABEL:
								_prevTrackBtn = createImageButton( PREVTRACK_IMG_NAME );
								_mediaActions.Add( new MediaCommandAction( _prevTrackBtn, cmd ) );
								doNotRenderObject( cmd );
								break;
						}
					}
					catch( Exception ) 
					{
						// probably a ClassCastException because the object wasn't a 
						// command ignore this case (because this high-level type 
						// doesn't recognize states besides Mode)
					}
				}
			}

			// layout buttons
			layoutButtons();

			GetControl().Resize += new EventHandler(MediaControlsSmartCIO_Resize);
		}


		/*
		 * Properties
		 */

		/*
		 * Member Methods
		 */

		public override string GetStringValue()
		{
			if ( _playState != null )
			{
				return ((MediaStateValueAction)_valueToActionMap[ _playState.Value ]).Label;
			}
			else 
				return null;
		}

		protected void layoutButtons()
		{
			int y = 1;
			int width = Math.Max( 95, GetControl().Size.Width );

			ArrayList topRow = new ArrayList();

			if ( _playBtn != null )
				topRow.Add( _playBtn );

			if ( _stopBtn != null )
				topRow.Add( _stopBtn );

			if ( _pauseBtn != null )
				topRow.Add( _pauseBtn );

			if ( _recordBtn != null )
				topRow.Add( _recordBtn );

			ArrayList bottomRow = new ArrayList();

			if ( _prevTrackBtn != null )
				bottomRow.Add( _prevTrackBtn );

			if ( _rewindBtn != null )
				bottomRow.Add( _rewindBtn );

			if ( _fastFwdBtn != null )
				bottomRow.Add( _fastFwdBtn );

			if ( _nextTrackBtn != null )
				bottomRow.Add( _nextTrackBtn );


			//
			// analyze rows
			//

			if ( _playBtn == null && _stopBtn != null && _pauseBtn != null )
			{
				// swap pause and stop
				topRow.Insert( 0, topRow[ 1 ] );
				topRow.RemoveAt( 2 );
			}

			// consider whether or not to promote buttons from the bottom row to the top row
			int totalCount = topRow.Count + bottomRow.Count;

			if ( totalCount <= 4 )
			{
				for( int i = 0; i < bottomRow.Count; i++ )
					topRow.Add( bottomRow[ i ] );

				bottomRow = null;
			}
			else if ( totalCount == 5 && topRow.Count == 2 )
			{
				if ( _prevTrackBtn == null )
				{
					// promote next track button
					topRow.Add( _nextTrackBtn );
					bottomRow.RemoveAt( 2 );
				}
				else if ( _nextTrackBtn == null )
				{
					// promote prev track button
					topRow.Add( _prevTrackBtn );
					bottomRow.RemoveAt( 0 );
				}
				else
				{
					// promote the speed control
					topRow.Add( bottomRow[ 1 ] );
					bottomRow.RemoveAt( 1 );
				}
			}

			// determine which button on the bottom row to make big
			// (if applicable)
			int bottomThree = 0;

			if ( bottomRow != null && bottomRow.Count == 3 )
			{
				if ( _prevTrackBtn == null )
					// next track is made big
					bottomThree = 2;

				else if ( _nextTrackBtn == null )
					// prev track is made big
					bottomThree = 0;

				else
					// the speed control is made big
					bottomThree = 1;
			}

			y = layoutRow( topRow, y, width, 0 );

			if ( bottomRow != null )
				y = layoutRow( bottomRow, y, width, bottomThree );

			_requiredSize = new Size( 95, y );
		}

		protected int layoutRow( ArrayList btnRow, int y, int width, int threeLarge )
		{
			IEnumerator buttonRow;
			int x;
			ImageButton btn;

			int quarterwidth = ( width - ( 3 * BTN_SPACER ) ) / 4;
			int halfwidth = 2 * quarterwidth + BTN_SPACER;

			switch ( btnRow.Count )
			{
				case 4:
					buttonRow = btnRow.GetEnumerator();
					x = 0;

					while( buttonRow.MoveNext() )
					{
						btn = (ImageButton)buttonRow.Current;
						GetControl().Controls.Add( btn );
						btn.Location = new Point( x, y );
						btn.Size = new Size( quarterwidth, BTN_DEFAULT_HEIGHT );

						x += quarterwidth + BTN_SPACER;
					}

					y += BTN_DEFAULT_HEIGHT + BTN_SPACER;
					break;

				case 2:
					buttonRow = btnRow.GetEnumerator();
					x = 0;

					while( buttonRow.MoveNext() )
					{
						btn = (ImageButton)buttonRow.Current;
						GetControl().Controls.Add( btn );
						btn.Location = new Point( x, y );
						btn.Size = new Size( halfwidth, BTN_DEFAULT_HEIGHT );

						x += halfwidth + BTN_SPACER;
					}

					y += BTN_DEFAULT_HEIGHT + BTN_SPACER;
					break;

				case 3:
					x = 0;

					for( int i = 0; i < btnRow.Count; i++ )
					{
						btn = (ImageButton)btnRow[ i ];
						GetControl().Controls.Add( btn );

						if ( i == threeLarge )
						{
							btn.Location = new Point( x, y );
							btn.Size = new Size( halfwidth, BTN_DEFAULT_HEIGHT );

							x += halfwidth + BTN_SPACER;
						}
						else
						{
							btn.Location = new Point( x, y );
							btn.Size = new Size( quarterwidth, BTN_DEFAULT_HEIGHT );

							x += quarterwidth + BTN_SPACER;
						}
					}

					y += BTN_DEFAULT_HEIGHT + BTN_SPACER;
					break;

				case 1:
					btn = (ImageButton)btnRow[ 0 ];
					GetControl().Controls.Add( btn );
					
					btn.Location = new Point( 0, y );
					btn.Size = new Size( width, BTN_DEFAULT_HEIGHT );
					
					y += BTN_DEFAULT_HEIGHT + BTN_SPACER;
					break;
			}

			return y;
		}

		protected System.Drawing.Image getImageFromResource( string imgName )
		{
			string imageName = PREFIX + imgName;

			System.Reflection.Assembly thisExe = 
				System.Reflection.Assembly.GetExecutingAssembly();
			System.IO.Stream file = 
				thisExe.GetManifestResourceStream( imageName );

			return new Bitmap( file );
		}

		protected ImageButton createImageButton( string imgName )
		{
			ImageButton ret = new ImageButton();

			ret.ButtonUpImage = getImageFromResource( imgName + STANDARD_ICON );
			ret.ButtonDownImage = getImageFromResource( imgName + HIGHLIGHT_ICON );
			ret.DisabledImage = getImageFromResource( imgName + DISABLED_ICON );

			return ret;
		}
		
		protected void createButtonsFromState()
		{
			if ( _playState == null )
				return;

			doNotRenderObject( _playState );

			Hashtable labels = _playState.Type.ValueLabels;
			IEnumerator keys = labels.Keys.GetEnumerator();
			while( keys.MoveNext() )
			{
				object value = keys.Current;
				LabelDictionary labelDict = (LabelDictionary)labels[ value ];
				
				if ( labelDict.Contains( PLAY_LABEL ) )
				{
					_playBtn = createImageButton( PLAY_IMG_NAME );
					_mediaActions.Add( new MediaStateValueAction( _playBtn, _playState, value, PLAY_LABEL ) );
				}
				else if ( labelDict.Contains( STOP_LABEL ) )
				{
					_stopBtn = createImageButton( STOP_IMG_NAME );
					_mediaActions.Add( new MediaStateValueAction( _stopBtn, _playState, value, STOP_LABEL ) );
				}
				else if ( labelDict.Contains( PAUSE_LABEL ) )
				{
					_pauseBtn = createImageButton( PAUSE_IMG_NAME );
					_mediaActions.Add( new MediaStateValueAction( _pauseBtn, _playState, value, PAUSE_LABEL ) );
				}
				else if ( labelDict.Contains( REWIND_LABEL ) )
				{
					_rewindBtn = createImageButton( REWIND_IMG_NAME );
					_mediaActions.Add( new MediaStateValueAction( _rewindBtn, _playState, value, REWIND_LABEL ) );
				}
				else if ( labelDict.Contains( FFWD_LABEL ) )
				{
					_fastFwdBtn = createImageButton( FFWD_IMG_NAME );
					_mediaActions.Add( new MediaStateValueAction( _fastFwdBtn, _playState, value, FFWD_LABEL ) );
				}
				else if ( labelDict.Contains( RECORD_LABEL ) )
				{
					_recordBtn = createImageButton( RECORD_IMG_NAME );
					_mediaActions.Add( new MediaStateValueAction( _recordBtn, _playState, value, RECORD_LABEL ) );
				}
			}
		}

		public override void FinalSizeNotify()
		{
			
		}

		public override System.Drawing.Point GetControlOffset()
		{	
			ImageButton button = ((MediaAction)_mediaActions[ 0 ]).Button;

			System.Drawing.SizeF s = Globals.MeasureString( "A Sample", button.Font );

			int xoffset = ( button.Size.Width - (int)s.Width ) / 2;
			int yoffset = ( BTN_DEFAULT_HEIGHT - (int)s.Height ) / 2 + (int)s.Height;

			return new System.Drawing.Point( xoffset, yoffset );
		}

		public override System.Drawing.Size GetMinimumSize()
		{
			return _requiredSize;
		}

		public override PUC.Layout.PreferredSize GetPreferredSize()
		{
			return new PUC.Layout.PreferredSize( GetMinimumSize() );
		}

		private void MediaControlsSmartCIO_Resize(object sender, EventArgs e)
		{
			layoutButtons();
		}
	}

	public class MediaAction
	{
		/*
		 * Member Variables
		 */

		protected ImageButton		 _button;


		/*
		 * Constructors
		 */

		public MediaAction( ImageButton button )
		{
			_button = button;
		}


		/*
		 * Properties
		 */

		public ImageButton Button
		{
			get
			{
				return _button;
			}
		}
	}

	public class MediaCommandAction : MediaAction
	{
		/*
		 * Member Variables
		 */

		protected ApplianceCommand   _command;


		/*
		 * Constructors
		 */

		
		public MediaCommandAction( ImageButton button, ApplianceCommand cmd )
			: base( button )
		{
			_command = cmd;

			_command.EnableChangedEvent += new PUC.ApplianceObject.EnableChangedHandler(_command_EnableChangedEvent);

			_button.Activated += new EventHandler(_button_Activated);
		}


		/*
		 * Member Methods
		 */

		private void _command_EnableChangedEvent(ApplianceObject o)
		{
			_button.Enabled = _command.Enabled;
		}

		private void _button_Activated(object sender, EventArgs e)
		{
			_command.InvokeCommand();
		}
	}

	public class MediaStateValueAction : MediaAction
	{
		/*
		 * Member Variables
		 */

		protected ApplianceState     _state;
		protected object			 _value;
		protected string			 _label;


		/*
		 * Constructors
		 */

		public MediaStateValueAction( ImageButton button, ApplianceState state, object value, string label )
			: base( button )
		{
			_state = state;
			_value = value;

			_button.StayDown = true;

			_state.EnableChangedEvent += new PUC.ApplianceObject.EnableChangedHandler(_state_EnableChangedEvent);
			_state.TypeChangedEvent += new PUC.ApplianceState.TypeChangedHandler(_state_TypeChangedEvent);
			_state.ValueChangedEvent += new PUC.ApplianceState.ValueChangedHandler(_state_ValueChangedEvent);

			_button.Activated += new EventHandler(_button_Activated);
		}


		/*
		 * Properties
		 */

		public string Label
		{
			get
			{
				return _label;
			}
		}

		public object Value
		{
			get
			{
				return _value;
			}
		}


		/*
		 * Member Methods
		 */

		private void _button_Activated(object sender, EventArgs e)
		{
			if ( _state.Value == _value )
				return;
			else
				_state.RequestChange( _value );
		}

		private void _state_EnableChangedEvent(ApplianceObject o)
		{
			CheckStateEnable();
		}

		private void _state_TypeChangedEvent(ApplianceState s)
		{
			CheckStateEnable();
		}

		private void CheckStateEnable()
		{
			bool enable = _state.Enabled;

			if ( !enable )
			{
				_button.Enabled = false;
				return;
			}

			LabelDictionary labelDict = (LabelDictionary)_state.Type.ValueLabels[ _value ];
			_button.Enabled = labelDict.Enabled;
		}

		private void _state_ValueChangedEvent(ApplianceState s)
		{
			if ( s.Type.ValueSpace.CompareValues( s.Value, _value ) == 0 )
				_button.PushDown();
			else
				_button.PushUp();
		}
	}
}
