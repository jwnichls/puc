using System;
using System.Collections;
using System.Windows.Forms;

namespace PhoneControls
{
	/// <summary>
	/// Summary description for ScrollingPanel.
	/// </summary>
	public class PhoneScrollingPanel : Control
	{
		/*
		 * Constants
		 */

		protected const int VSCROLL_WIDTH = 5;
		protected const int LABEL_HEIGHT  = 20;


		/*
		 * Events
		 */

		public event EventHandler ClientResized;


		/*
		 * Member Variables
		 */

		protected ScrollBarDisplay    _displayPolicy;
		protected ScrollBarDirections _directionPolicy;

		protected Panel				  _scrolledPanel;
		protected VScrollBar		  _vertBar;
		protected ScrolledCollection  _collection;


		/*
		 * Constructor
		 */

		public PhoneScrollingPanel()
			: this( ScrollBarDisplay.AsNeeded, 
			ScrollBarDirections.Vertical )
		{
		}

		public PhoneScrollingPanel( ScrollBarDisplay dPol, 
			ScrollBarDirections dirPol )
		{
			_displayPolicy = dPol;
			_directionPolicy = dirPol;

			_scrolledPanel = new Panel();
			_vertBar = new VScrollBar();

			_scrolledPanel.Location = new System.Drawing.Point( 0, 0 );
			((Control)this).Controls.Add( _scrolledPanel );

			_collection = new ScrolledCollection( this, _scrolledPanel );			

			this.Resize += new EventHandler(ScrollingPanel_Resize);
			_vertBar.ValueChanged += new EventHandler(_vertBar_ValueChanged);
		}


		/*
		 * Properties
		 */

		public ScrollBarDisplay DisplayPolicy
		{
			get
			{
				return _displayPolicy;
			}
			set
			{
				_displayPolicy = value;
			}
		}

		public ScrollBarDirections DirectionPolicy
		{
			get
			{
				return _directionPolicy;
			}
			set
			{
				_directionPolicy = value;
			}
		}

		public new virtual Control.ControlCollection Controls
		{
			get
			{
				return _collection;
			}
		}

		public virtual System.Drawing.Size InternalSize
		{
			get
			{
				return _scrolledPanel.Size;
			}
		}


		/*
		 * Event Handlers
		 */

		private void ScrollingPanel_Resize(object sender, EventArgs e)
		{
			sizeElements();
		}		

		private void _vertBar_ValueChanged(object sender, EventArgs e)
		{
			_scrolledPanel.Location = new System.Drawing.Point( _scrolledPanel.Location.X, -_vertBar.Value );
		}


		/*
		 * Member Methods
		 */

		public void UpdateScroll()
		{
			sizeElements();
		}

		/*
		 * Protected Helper Methods
		 */

		protected void resizeScrollPanel() 
		{
			// determine the sizing for scrolledPanel
			int scrlWidth = this.Size.Width - VSCROLL_WIDTH;
			int scrlHeight = 0;

			IEnumerator e = _scrolledPanel.Controls.GetEnumerator();
			while( e.MoveNext() )
			{
				Control c = (Control)e.Current;

				scrlHeight = Math.Max( c.Size.Height + c.Location.Y, scrlHeight );
			}

			_scrolledPanel.Size = new System.Drawing.Size( scrlWidth, scrlHeight );
		}

		protected void sizeElements() 
		{
			ScrollBarDirections barsToShow = 0x0;
			resizeScrollPanel();
			System.Drawing.Size scrlSize = _scrolledPanel.Size;

			switch ( _displayPolicy )
			{
				case ScrollBarDisplay.Always:
					barsToShow = _directionPolicy;
					break;

				case ScrollBarDisplay.Never:
					barsToShow = ScrollBarDirections.None;
					break;

				case ScrollBarDisplay.AsNeeded:
					barsToShow = barsNeeded();
					break;
			}

			int intWidth = this.Size.Width;
			int intHeight = this.Size.Height;

			if ( ( ScrollBarDirections.Vertical & barsToShow ) > 0 )
			{
				intWidth -= VSCROLL_WIDTH;

				base.Controls.Add( _vertBar );
			}
			else if ( ((Control)this).Controls.Contains( _vertBar ) )
				base.Controls.Remove( _vertBar );

			// determine settings for the scrollbar
			if ( ( ScrollBarDirections.Vertical & barsToShow ) > 0 )
			{
				_vertBar.Minimum = 0;
				_vertBar.Maximum = scrlSize.Height;
				_vertBar.LargeChange = intHeight;
				_vertBar.SmallChange = scrlSize.Height / 20;

				_vertBar.Location = new System.Drawing.Point( intWidth, 0 );
				_vertBar.Size = new System.Drawing.Size( VSCROLL_WIDTH, intHeight );
			}

			System.Drawing.Size newInternalSize = 
				new System.Drawing.Size( intWidth, _scrolledPanel.Size.Height );
			bool bcmp = !newInternalSize.Equals( _scrolledPanel.Size );
			_scrolledPanel.Size = newInternalSize;


			if ( bcmp && this.ClientResized != null )
				ClientResized( this, new EventArgs() );
		}

		protected ScrollBarDirections barsNeeded()
		{
			ScrollBarDirections ret = ScrollBarDirections.None;

			System.Drawing.Size intSize = this.Size;
			System.Drawing.Size scrlSize = _scrolledPanel.Size;

			if ( scrlSize.Height > intSize.Height )
				ret |= ScrollBarDirections.Vertical;

			return ret;
		}

		protected bool controlInView( Control ctl )
		{
			if ( !base.Controls.Contains( _vertBar ) )
				return true;

			return _vertBar.Value < ( Math.Max( ctl.Location.Y - LABEL_HEIGHT, 0 ) ) && 
				   ( _vertBar.Value + _vertBar.LargeChange - 1 ) > ( ctl.Location.Y + ctl.Size.Height );
		}

		protected void controlReceivedFocus( Control ctl )
		{
			if ( controlInView( ctl ) )
				return;

			int topLoc = ctl.Location.Y - LABEL_HEIGHT;
			int botLoc = ctl.Location.Y + ctl.Size.Height;

			// determine which direction we're scrolling
			if ( topLoc < _vertBar.Value )
			{
				// scrolling up
				_vertBar.Value = topLoc;
			}
			else
			{
				// scrolling down
				int distToScroll = botLoc - ( _vertBar.Value + _vertBar.LargeChange - 1 );
				_vertBar.Value = _vertBar.Value + distToScroll;
			}
		}

		/*
		 * ScrollingPanel.ScrolledCollection Class
		 */

		public class ScrolledCollection : Control.ControlCollection
		{
			/*
			 * Member Variables
			 */

			public PhoneScrollingPanel _panel;
			public Panel _scrolledPanel;


			/*
			 * Constructor
			 */

			public ScrolledCollection( PhoneScrollingPanel panel, Panel scrollPanel )
				: base( panel )
			{
				_panel = panel;
				_scrolledPanel = scrollPanel;
			}


			/*
			 * Overridden Panels
			 */

			public override void Add(Control value)
			{
				_scrolledPanel.Controls.Add( value );
				System.Drawing.Rectangle rect = new System.Drawing.Rectangle( 0, 0, _panel._scrolledPanel.Size.Width, _panel._scrolledPanel.Size.Height );
				if ( !rect.Contains( value.Bounds ) )
					_panel.UpdateScroll();

				value.Resize += new EventHandler(value_Resize);
				value.GotFocus += new EventHandler(value_GotFocus);
			}

			public override void Remove(Control value)
			{
				_scrolledPanel.Controls.Remove (value);

				value.Resize -= new EventHandler(value_Resize);
			}

			
			/*
			 * Event Listeners
			 */

			private void value_Resize(object sender, EventArgs e)
			{
				System.Drawing.Rectangle rect = new System.Drawing.Rectangle( 0, 0, _panel._scrolledPanel.Size.Width, _panel._scrolledPanel.Size.Height );
				if ( !rect.Contains( ((Control)sender).Bounds ) )
					_panel.UpdateScroll();
			}

			private void value_GotFocus(object sender, EventArgs e)
			{
				_panel.controlReceivedFocus( (Control)sender );
			}
		}
	}

	public enum ScrollBarDisplay 
	{
		Never,
		AsNeeded,
		Always
	}

	public enum ScrollBarDirections: byte
	{
		None = 0x0,
		Vertical = 0x1
	}
}
