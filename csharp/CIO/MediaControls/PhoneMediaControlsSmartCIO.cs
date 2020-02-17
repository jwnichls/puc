using System;
using System.Collections;
using System.Drawing;
using System.Windows.Forms;

using PhoneControls;

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


		/*
		 * Member Variables
		 */

		// valid in single state and sometimes in multiple state situations
		protected ApplianceState _playState;

		protected Hashtable      _valueToTypeMap;

		protected Size			 _requiredSize = new Size( 108, 44 );


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
			: base( new PhoneMediaControls(), specSnippet )
		{
			_valueToTypeMap = new Hashtable();

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
						MediaCommandAction m;

						switch( name )
						{
							case PLAY_LABEL:
								m = new MediaCommandAction( cmd );
								((PhoneMediaControls)GetControl()).Play += new EventHandler(m.Activate);
								break;

							case STOP_LABEL:
								m = new MediaCommandAction( cmd );
								((PhoneMediaControls)GetControl()).Stop += new EventHandler(m.Activate);
								break;

							case PAUSE_LABEL:
								m = new MediaCommandAction( cmd );
								((PhoneMediaControls)GetControl()).Pause += new EventHandler(m.Activate);
								break;

							case NEXT_TRK_CMD_LABEL:
								m = new MediaCommandAction( cmd );
								((PhoneMediaControls)GetControl()).NextTrack += new EventHandler(m.Activate);
								((PhoneMediaControls)GetControl()).NextTrackEnabled = cmd.Enabled;
								cmd.EnableChangedEvent += new PUC.ApplianceObject.EnableChangedHandler(NextTrack_EnableChangedEvent);
								break;

							case PREV_TRK_CMD_LABEL:
								m = new MediaCommandAction( cmd );
								((PhoneMediaControls)GetControl()).PrevTrack += new EventHandler(m.Activate);
								((PhoneMediaControls)GetControl()).PrevTrackEnabled = cmd.Enabled;
								cmd.EnableChangedEvent += new PUC.ApplianceObject.EnableChangedHandler(PrevTrack_EnableChangedEvent);
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
		}


		/*
		 * Properties
		 */

		/*
		 * Member Methods
		 */

		protected void createButtonsFromState()
		{
			if ( _playState == null )
				return;

			_playState.ValueChangedEvent += new PUC.ApplianceState.ValueChangedHandler(_playState_ValueChangedEvent);

			Hashtable labels = _playState.Type.ValueLabels;
			IEnumerator keys = labels.Keys.GetEnumerator();
			while( keys.MoveNext() )
			{
				object value = keys.Current;
				LabelDictionary labelDict = (LabelDictionary)labels[ value ];
				MediaStateAction m;

				if ( labelDict.Contains( PLAY_LABEL ) )
				{
					_valueToTypeMap[ PLAY_LABEL ] = value;
					m = new MediaStateAction( _playState, value );
					((PhoneMediaControls)GetControl()).Play += new EventHandler(m.Activate);
				}
				else if ( labelDict.Contains( STOP_LABEL ) )
				{
					_valueToTypeMap[ STOP_LABEL ] = value;
					m = new MediaStateAction( _playState, value );
					((PhoneMediaControls)GetControl()).Stop += new EventHandler(m.Activate);
				}
				else if ( labelDict.Contains( PAUSE_LABEL ) )
				{
					_valueToTypeMap[ PAUSE_LABEL ] = value;
					m = new MediaStateAction( _playState, value );
					((PhoneMediaControls)GetControl()).Pause += new EventHandler(m.Activate);
				}
			}
		}

		public override string GetStringValue()
		{
			if ( _playState != null )
			{
				object val = _playState.Value;

				IEnumerator e = _valueToTypeMap.GetEnumerator();
				while( e.MoveNext() )
				{
					DictionaryEntry de = (DictionaryEntry)e.Current;
					if ( de.Value == val )
						return (string)de.Key;
				}

				return "";
			}
			else 
				return null;
		}

		public override void FinalSizeNotify()
		{
			
		}

		public override System.Drawing.Point GetControlOffset()
		{	
			return new System.Drawing.Point( 0, 0 );
		}

		public override System.Drawing.Size GetMinimumSize()
		{
			return _requiredSize;
		}

		public override PUC.Layout.PreferredSize GetPreferredSize()
		{
			return new PUC.Layout.PreferredSize( GetMinimumSize() );
		}

		private void _playState_ValueChangedEvent(ApplianceState s)
		{
			object val = s.Value;

			if ( _valueToTypeMap[ val ] != null )
			{
				switch( (string)_valueToTypeMap[ val ] )
				{
					case PLAY_LABEL:
						((PhoneMediaControls)GetControl()).Playing = true;
						break;

					default:
						((PhoneMediaControls)GetControl()).Playing = false;
						break;
				}
			}
		}

		private void NextTrack_EnableChangedEvent(ApplianceObject o)
		{
			((PhoneMediaControls)GetControl()).NextTrackEnabled = o.Enabled;
		}

		private void PrevTrack_EnableChangedEvent(ApplianceObject o)
		{
			((PhoneMediaControls)GetControl()).PrevTrackEnabled = o.Enabled;
		}
	}

	public interface IMediaAction
	{
		void Activate(object sender, EventArgs e);
	}

	public class MediaCommandAction : IMediaAction
	{
		/*
		 * Member Variables
		 */

		protected ApplianceCommand _cmd;


		/*
		 * Constructor
		 */

		public MediaCommandAction( ApplianceCommand cmd )
		{
			_cmd = cmd;
		}


		/*
		 * Member Methods
		 */

		public void Activate(object sender, EventArgs e)
		{
			_cmd.InvokeCommand();		
		}
	}

	public class MediaStateAction : IMediaAction
	{
		/*
		 * Member Variables
		 */

		protected ApplianceState _state;
		protected object		 _value;


		/*
		 * Constructor
		 */

		public MediaStateAction( ApplianceState state, object value )
		{
			_state = state;
			_value = value;
		}


		/*
		 * Member Methods
		 */

		public void Activate(object sender, EventArgs e)
		{
			_state.RequestChange( _value );
		}
	}
}
