using System;
using System.Collections;
using System.Windows.Forms;

namespace PocketPCControls
{
	/// <summary>
	/// Summary description for ScrollingPanel.
	/// </summary>
	public class ScrollingPanel : Panel
	{
		/*
		 * Constants
		 */

#if DESKTOP
		protected const int HSCROLL_HEIGHT = 20;
		protected const int VSCROLL_WIDTH  = 20;
#endif
#if POCKETPC
		protected const int HSCROLL_HEIGHT = 15;
		protected const int VSCROLL_WIDTH  = 15;
#endif

		/*
		 * Events
		 */

		public event EventHandler ClientResized;


		/*
		 * Member Variables
		 */

		protected ScrollBarDisplay    _displayPolicy;
		protected ScrollBarDirections _directionPolicy;

		protected Panel               _internalPanel;
		protected Panel				  _scrolledPanel;
		protected HScrollBar		  _horizBar;
		protected VScrollBar		  _vertBar;
		protected ScrolledCollection  _collection;


		/*
		 * Constructor
		 */

		public ScrollingPanel()
			: this( ScrollBarDisplay.AsNeeded, 
					ScrollBarDirections.Both )
		{
		}

		public ScrollingPanel( ScrollBarDisplay dPol, 
			ScrollBarDirections dirPol )
		{
			_displayPolicy = dPol;
			_directionPolicy = dirPol;

			_internalPanel = new Panel();
			_scrolledPanel = new Panel();
			_horizBar = new HScrollBar();
			_vertBar = new VScrollBar();

			((Control)this).Controls.Add( _internalPanel );
			_internalPanel.Controls.Add( _scrolledPanel );

			_internalPanel.Location = new System.Drawing.Point( 0, 0 );
			_scrolledPanel.Location = new System.Drawing.Point( 0, 0 );

			_collection = new ScrolledCollection( this, _scrolledPanel );			

			this.Resize += new EventHandler(ScrollingPanel_Resize);

			_horizBar.ValueChanged += new EventHandler(_horizBar_ValueChanged);
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

				sizeElements();
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

				sizeElements();
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
				return _internalPanel.Size;
			}
		}


		/*
		 * Event Handlers
		 */

		private void ScrollingPanel_Resize(object sender, EventArgs e)
		{
			sizeElements();
		}		

		private void _horizBar_ValueChanged(object sender, EventArgs e)
		{
			_scrolledPanel.Location = new System.Drawing.Point( -_horizBar.Value, _scrolledPanel.Location.Y );
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
			int scrlWidth = 0;
			int scrlHeight = 0;

			IEnumerator e = _scrolledPanel.Controls.GetEnumerator();
			while( e.MoveNext() )
			{
				Control c = (Control)e.Current;

				scrlWidth = Math.Max( c.Size.Width + c.Location.X, scrlWidth );
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

			if ( ( ScrollBarDirections.Horizontal & barsToShow ) > 0 )
			{
				intHeight -= HSCROLL_HEIGHT;
	
				base.Controls.Add( _horizBar );
			}
			else if ( ((Control)this).Controls.Contains( _horizBar ) )
				base.Controls.Remove( _horizBar );

			if ( ( ScrollBarDirections.Vertical & barsToShow ) > 0 )
			{
				intWidth -= VSCROLL_WIDTH;

				base.Controls.Add( _vertBar );
			}
			else if ( ((Control)this).Controls.Contains( _vertBar ) )
				base.Controls.Remove( _vertBar );

			// determine settings for the scrollbars
			if ( ( ScrollBarDirections.Horizontal & barsToShow ) > 0 )
			{
				_horizBar.Minimum = 0;
				_horizBar.Maximum = scrlSize.Width;
				_horizBar.LargeChange = intWidth;
				_horizBar.SmallChange = scrlSize.Width / 20;

				_horizBar.Location = new System.Drawing.Point( 0, intHeight );
				_horizBar.Size = new System.Drawing.Size( intWidth, HSCROLL_HEIGHT );
			}

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
				new System.Drawing.Size( intWidth, intHeight );
			bool bcmp = !newInternalSize.Equals( _internalPanel.Size );
			_internalPanel.Size = newInternalSize;


			if ( bcmp && this.ClientResized != null )
				ClientResized( this, new EventArgs() );
		}

		protected ScrollBarDirections barsNeeded()
		{
			ScrollBarDirections ret = ScrollBarDirections.None;

			System.Drawing.Size intSize = this.Size;
			System.Drawing.Size scrlSize = _scrolledPanel.Size;

			if ( scrlSize.Width > intSize.Width )
				ret |= ScrollBarDirections.Horizontal;

			if ( scrlSize.Height > intSize.Height )
				ret |= ScrollBarDirections.Vertical;

			if ( ( ret & ScrollBarDirections.Horizontal ) != 0 &&
				 scrlSize.Height > ( intSize.Height - HSCROLL_HEIGHT ) )
				ret |= ScrollBarDirections.Vertical;

			if ( ( ret & ScrollBarDirections.Vertical ) != 0 &&
				 scrlSize.Width > ( intSize.Width - VSCROLL_WIDTH ) )
				ret |= ScrollBarDirections.Horizontal;

			return ret;
		}

		/*
		 * ScrollingPanel.ScrolledCollection Class
		 */

		public class ScrolledCollection : Control.ControlCollection
		{
			/*
			 * Member Variables
			 */

			public ScrollingPanel _panel;
			public Panel _scrolledPanel;


			/*
			 * Constructor
			 */

			public ScrolledCollection( ScrollingPanel panel, Panel scrollPanel )
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
		Horizontal = 0x1,
		Vertical = 0x2,
		Both = 0x3
	}
}
