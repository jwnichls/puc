using System;
using System.Collections;
using System.Windows.Forms;

using PocketPCControls;

using PUC;
using PUC.CIO;
using PUC.Communication;
using PUC.Types;


namespace PUC.CIO.Dimmer
{
	/// <summary>
	/// Summary description for DimmerSmartCIO.
	/// </summary>
	public class DimmerSmartCIO : SmartCIO
	{
		/*
		 * Constants
		 */

		public string HIGH_LEVEL_TYPE = "dimmer";

		// constants for parsing multiple states
		protected string ON_LABEL   = "On";
		protected string OFF_LABEL	= "Off";
		protected string DIM_LABEL  = "Dim";

		// constants for judging button layout
		public const int BTN_DEFAULT_HEIGHT = 24;

		// constants for judging scrollbar layout
		public const int SB_MINIMUM_HEIGHT = 15;
		public const int SB_DEFAULT_HEIGHT = 20;
		public const int SB_MINIMUM_WIDTH  = 30;

		public const int SPACER = 3;


		/*
		 * Member Variables
		 */

		protected HScrollBar	_dimmerControl;
		protected Button		_onButton;
		protected Button		_offButton;

		protected ApplianceState	_dimState;
		protected ApplianceCommand	_onCommand;
		protected ApplianceCommand	_offCommand;


		// for preventing loops when using TimeSlider
		protected Hashtable _sentValues;


		/*
		 * Static Methods (for Dynamic Class Loading)
		 */

		public static SmartCIO CreateDimmerSmartCIO( PUC.GroupNode group )
		{
			return new DimmerSmartCIO( group );
		}

		
		/*
		 * Constructor
		 */

		public DimmerSmartCIO( GroupNode specSnippet )
			: base( new Panel(), specSnippet )
		{
			if ( _specSnippet.IsObject() )
			{
				// single state translation

				// this means that there is only a dimmer and no on or off switches

				_dimState = (ApplianceState)_objects[ SINGLE_STATE ];
				_onCommand = null;
				_offCommand = null;
			}
			else
			{
				// multiple state translation

				_dimState = (ApplianceState)_objects[ DIM_LABEL ];
				_onCommand = (ApplianceCommand)_objects[ ON_LABEL ];
				_offCommand = (ApplianceCommand)_objects[ OFF_LABEL ];
			}

			if ( _dimState != null )
			{
				doNotRenderObject( _dimState );

				_dimmerControl = new HScrollBar();
				_dimmerControl.Minimum = 0;
				_dimmerControl.SmallChange = 1;
				_dimmerControl.LargeChange = 10;
				_dimmerControl.Maximum = 109;
				GetControl().Controls.Add( _dimmerControl );

				_sentValues = new Hashtable();
				
				_dimState.ValueChangedEvent += new PUC.ApplianceState.ValueChangedHandler(this.ValueChanged);
				_dimState.EnableChangedEvent += new PUC.ApplianceObject.EnableChangedHandler(this.EnableChanged);
				_dimmerControl.ValueChanged += new EventHandler(this._dimmerControl_ValueChanged);
			}

			System.Drawing.Font f = new System.Drawing.Font( "Tahoma", 9, System.Drawing.FontStyle.Regular );

			if ( _onCommand != null )
			{
				doNotRenderObject( _onCommand );

				_onButton = new Button();
				_onButton.Text = "On";
				_onButton.Font = f;
				GetControl().Controls.Add( _onButton );

				_onCommand.EnableChangedEvent += new PUC.ApplianceObject.EnableChangedHandler(this.EnableChanged);
				_onButton.Click += new EventHandler(_onButton_Click);
			}

			if ( _offCommand != null )
			{
				doNotRenderObject( _offCommand );

				_offButton = new Button();
				_offButton.Text = "Off";
				_offButton.Font = f;
				GetControl().Controls.Add( _offButton );

				_offCommand.EnableChangedEvent += new PUC.ApplianceObject.EnableChangedHandler(this.EnableChanged);
				_offButton.Click += new EventHandler(_offButton_Click);
			}

			GetControl().Resize += new EventHandler(this.Resized);
		}


		/*
		 * Properties
		 */


		/*
		 * Member Methods
		 */

		public override string GetStringValue()
		{
			if ( _dimState != null )
			{
				return _dimState.Value.ToString() + "%";
			}
			else
				return null;
		}


		public void ValueChanged( ApplianceState state )
		{
			string val = _dimState.Value.ToString();

			if ( _sentValues[ val ] != null && 
				((int)_sentValues[ val ]) > 0 )
			{
				int cnt = (int)_sentValues[ val ];
				_sentValues[ val ] = --cnt;
			}
			else
				_dimmerControl.Value = (int)_dimState.Value;
		}

		public void EnableChanged( ApplianceObject ao )
		{
			_dimmerControl.Enabled = _dimState.Enabled;
			_onButton.Enabled = _onCommand.Enabled;
			_offButton.Enabled = _offCommand.Enabled;
		}

		public void Resized( object source, EventArgs e )
		{
			int x = 0;
			System.Drawing.SizeF s = new System.Drawing.SizeF( 0, 0 );
			
			if ( _offButton != null )
				s = Globals.MeasureString( "Off", _offButton.Font );
			else if ( _onButton != null )
				s = Globals.MeasureString( "On", _onButton.Font );
			
			if ( _offButton != null )
			{
				_offButton.Location = new System.Drawing.Point( x, 1 );
				_offButton.Size = new System.Drawing.Size( 10 + (int)s.Width, BTN_DEFAULT_HEIGHT );

				x += _offButton.Size.Width + SPACER;
			}

			if ( _dimmerControl != null )
			{
				_dimmerControl.Location = new System.Drawing.Point( x, 1 );
				_dimmerControl.Size = new System.Drawing.Size( GetControl().Width - ( 2 * ( 10 + SPACER + (int)s.Width ) ), BTN_DEFAULT_HEIGHT );

				x += _dimmerControl.Size.Width + SPACER;
			}

			if ( _onButton != null )
			{
				_onButton.Location = new System.Drawing.Point( x, 1 );
				_onButton.Size = new System.Drawing.Size( 10 + (int)s.Width, BTN_DEFAULT_HEIGHT );
			}
		}

		public override void FinalSizeNotify()
		{
			
		}

		public override System.Drawing.Point GetControlOffset()
		{
			System.Drawing.SizeF s = Globals.MeasureString( _offButton.Text, _offButton.Font );	

			int xoffset = ( _offButton.Size.Width - (int)s.Width ) / 2;
			int yoffset = ( _offButton.Size.Height - (int)s.Height ) / 2 + (int)s.Height;

			return new System.Drawing.Point( xoffset, yoffset );
		}

		public override System.Drawing.Size GetMinimumSize()
		{
			System.Drawing.SizeF s = Globals.MeasureString( _offButton.Text, _offButton.Font );	

			int w = 2 * ( 10 + SPACER + (int)s.Width ) + SB_MINIMUM_WIDTH;
			int h = BTN_DEFAULT_HEIGHT + 1;

			return new System.Drawing.Size( w, h );
		}

		public override PUC.Layout.PreferredSize GetPreferredSize()
		{
			return new PUC.Layout.PreferredSize( GetMinimumSize() );
		}

		private void _onButton_Click(object sender, EventArgs e)
		{
			_onCommand.InvokeCommand();
		}

		private void _offButton_Click(object sender, EventArgs e)
		{
			_offCommand.InvokeCommand();
		}

		private void _dimmerControl_ValueChanged(object sender, EventArgs e)
		{
			string val = _dimmerControl.Value.ToString();
			string stateval;

			if ( _dimState.Defined )
				stateval = _dimState.Value.ToString();
			else 
				stateval = "";

			// don't echo state change requests for notifications
			if ( val == stateval ) return;

			_dimState.RequestChange( _dimmerControl.Value );
 
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
}
