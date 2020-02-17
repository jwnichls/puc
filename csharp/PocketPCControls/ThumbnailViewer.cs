using System;
using System.Collections;
using System.Drawing;
using System.Windows.Forms;

namespace PocketPCControls
{
	/// <summary>
	/// A control for viewing image thumbnails.
	/// </summary>
	public class ThumbnailViewer : Control
	{
		/*
		 * Constants
		 */

		public const int THUMBNAIL_WIDTH	= 64;
		public const int THUMBNAIL_HEIGHT	= 64;
		public const int SPACER				= 8;
		public const int SCROLLBAR_WIDTH	= 20;

#if POCKETPC
		protected const string PREFIX		 = "PUC.PocketPCControls.";
#endif
#if DESKTOP
		protected const string PREFIX		 = "DesktopPUC.PocketPCControls.";
#endif

		protected const String NO_IMAGE_NAME = "noimage.bmp";


		/*
		 * Static Members & Constructor
		 */

		public readonly static Image NO_IMAGE;

		protected readonly static Pen			LIGHTGREY_PEN	= new Pen( Color.LightGray );
		protected readonly static Pen			BLACK_PEN		= new Pen( Color.Black );
		protected readonly static Pen			SELECTED_PEN	= new Pen( Color.DarkBlue );
		protected readonly static SolidBrush	WHITE_BRUSH		= new SolidBrush( Color.White );
		
		protected readonly static Rectangle SRCRECT	= 
			new Rectangle( 0, 0, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT );


		protected static System.Drawing.Image getImageFromResource( string imgName )
		{
			string imageName = PREFIX + imgName;

			System.Reflection.Assembly thisExe = 
				System.Reflection.Assembly.GetExecutingAssembly();
			System.IO.Stream file = 
				thisExe.GetManifestResourceStream( imageName );

			return new Bitmap( file );
		}

		static ThumbnailViewer()
		{
			NO_IMAGE = getImageFromResource( NO_IMAGE_NAME );
		}


		/*
		 * Events
		 */

		public event EventHandler SelectedIndexChanged;


		/*
		 * Member Variables
		 */

		protected ArrayList				_thumbnails;
		protected int					_selectedIndex;
		protected ScrollbarOrientation	_scrollbarLoc;

		protected int					_firstViewable;
		protected int					_rows;
		protected int					_cols;

		protected ScrollBar				_scrollbar;
		protected Image					_imageBuffer;


		/*
		 * Constructor
		 */

		public ThumbnailViewer()
		{
			_thumbnails = new ArrayList();
			_selectedIndex = -1;
			_scrollbarLoc = ScrollbarOrientation.Horizontal;

			_firstViewable = 0;
			_rows = _cols = 1;

			if ( _scrollbarLoc == ScrollbarOrientation.Horizontal )
			{
				_scrollbar = new HScrollBar();
				_scrollbar.Size = new Size( this.Width, SCROLLBAR_WIDTH );
				_scrollbar.Location = new Point( 0, this.Height - SCROLLBAR_WIDTH );
			}
			else
			{
				_scrollbar = new VScrollBar();
				_scrollbar.Size = new Size( SCROLLBAR_WIDTH, this.Height );
				_scrollbar.Location = new Point( this.Width - SCROLLBAR_WIDTH, 0 );
			}
			this.Controls.Add( _scrollbar );
			_scrollbar.ValueChanged += new EventHandler(ScrollBar_ValueChanged);
		}


		/*
		 * Indexor
		 */

		public Image this[ int idx ]
		{
			get
			{
				if ( idx >= _thumbnails.Count || _thumbnails[ idx ] == null )
					return null;

				return ((Thumbnail)_thumbnails[ idx ]).Image;
			}
			set
			{
				if ( idx >= _thumbnails.Count )
				{
					_thumbnails.Capacity = idx+1;

					for( int i = _thumbnails.Count; i <= idx; i++ )
						_thumbnails.Add( new Thumbnail( NO_IMAGE ) );

					setupScrollbars();
				}

				if ( value == null )
					value = NO_IMAGE;

				if ( ((Thumbnail)_thumbnails[ idx ]).Image == NO_IMAGE )
				{
					_thumbnails[ idx ] = new Thumbnail( value );

					if ( ( idx >= _firstViewable ) && ( idx < ( _firstViewable + ViewableCount ) ) )
						this.Invalidate();
				}
				else
				{
					((Thumbnail)_thumbnails[ idx ]).Image = value;

					if ( ( idx >= _firstViewable ) && ( idx < ( _firstViewable + ViewableCount ) ) )
						this.Invalidate( ((Thumbnail)_thumbnails[ idx ]).VisibleRect );
				}
			}
		}


		/*
		 * Properties
		 */

		public int Count
		{
			get
			{
				return _thumbnails.Count;
			}
		}

		public int SelectedIndex
		{
			get
			{
				return _selectedIndex;
			}
			set
			{
				if ( _selectedIndex != value )
				{
					_selectedIndex = value;
					this.Invalidate();
					DisplayItem( _selectedIndex );

					if ( SelectedIndexChanged != null )
						SelectedIndexChanged( this, new EventArgs() );
				}
			}
		}

		public int TopIndex
		{
			get
			{
				return _firstViewable;
			}
			set
			{
				_firstViewable = value;
			}
		}

		public int ViewableCount
		{
			get
			{
				return _rows * _cols;
			}
		}

		public ScrollbarOrientation ScrollbarOrientation
		{
			get
			{
				return _scrollbarLoc;
			}
			set
			{
				if ( value != _scrollbarLoc )
				{
					_scrollbarLoc = value;
					this.Controls.Remove( _scrollbar );
					_scrollbar.ValueChanged -= new EventHandler(ScrollBar_ValueChanged);

					if ( _scrollbarLoc == ScrollbarOrientation.Horizontal )
					{
						_scrollbar = new HScrollBar();
						_scrollbar.Size = new Size( this.Width, SCROLLBAR_WIDTH );
						_scrollbar.Location = new Point( 0, this.Height - SCROLLBAR_WIDTH );
					}
					else
					{
						_scrollbar = new VScrollBar();
						_scrollbar.Size = new Size( SCROLLBAR_WIDTH, this.Height );
						_scrollbar.Location = new Point( this.Width - SCROLLBAR_WIDTH, 0 );
					}
					this.Controls.Add( _scrollbar );
					_scrollbar.ValueChanged += new EventHandler(ScrollBar_ValueChanged);

					determineArrangement();

					this.Invalidate();
				}
			}
		}


		/*
		 * Member Methods
		 */

		public void DisplayItem( int idx )
		{
			// if the index is already in view, do nothing
			if ( idx >= _firstViewable && idx < _firstViewable + ViewableCount )
				return;

			if ( _scrollbarLoc == ScrollbarOrientation.Horizontal )
			{
				int col = idx / _rows;
				_firstViewable = col * _rows;
				this.Invalidate();
			}
			else
			{
				int row = idx / _cols;
				_firstViewable = row * _cols;
				this.Invalidate();
			}
		}

		/// <summary>
		/// Determine the number of rows and columns that this
		/// controls will use to show thumbnails.
		/// </summary>
		protected void determineArrangement()
		{
			int h = this.Height;
			int w = this.Width;

			if ( _scrollbarLoc == ScrollbarOrientation.Horizontal )
				h -= SCROLLBAR_WIDTH;
			else
				w -= SCROLLBAR_WIDTH;

			_rows = Math.Max( 1, h / ( THUMBNAIL_HEIGHT + SPACER ) );
			_cols = Math.Max( 1, w / ( THUMBNAIL_WIDTH + SPACER ) );

			setupScrollbars();
		}

		protected void setupScrollbars()
		{
			if ( _scrollbarLoc == ScrollbarOrientation.Horizontal )
			{
				int maxcols = (int)Math.Ceiling( (double)Count / (double)_rows );
				_scrollbar.Minimum = 0;
				_scrollbar.SmallChange = 1;
				_scrollbar.LargeChange = _cols;
				_scrollbar.Maximum = Math.Max( maxcols - 1, 0 );
				_scrollbar.Value = _firstViewable / _rows;

				if ( maxcols <= _cols )
					_scrollbar.Enabled = false;
				else
					_scrollbar.Enabled = true;
			}
			else
			{
				int maxrows = (int)Math.Ceiling( (double)Count / (double)_cols );
				_scrollbar.Minimum = 0;
				_scrollbar.SmallChange = 1;
				_scrollbar.LargeChange = _rows;
				_scrollbar.Maximum = maxrows - 1;
				_scrollbar.Value = _firstViewable / _cols;

				if ( maxrows <= _rows )
					_scrollbar.Enabled = false;
				else
					_scrollbar.Enabled = true;
			}
		}
		

		/*
		 * Control Methods
		 */
		
		protected override void OnResize(EventArgs e)
		{
			if ( _imageBuffer != null )
				_imageBuffer.Dispose();

			_imageBuffer = new Bitmap( this.Width, this.Height );

			if ( _scrollbarLoc == ScrollbarOrientation.Horizontal )
			{
				_scrollbar.Size = new Size( this.Width, SCROLLBAR_WIDTH );
				_scrollbar.Location = new Point( 0, this.Height - SCROLLBAR_WIDTH );
			}
			else
			{
				_scrollbar.Size = new Size( SCROLLBAR_WIDTH, this.Height );
				_scrollbar.Location = new Point( this.Width - SCROLLBAR_WIDTH, 0 );
			}

			determineArrangement();
			this.Invalidate();
		}

		protected override void OnMouseDown(MouseEventArgs e)
		{
			int start = _firstViewable;

			for( int i = 0; ( i < ViewableCount ) && ( (start + i) < Count ) ; i++ )
			{
				if ( _thumbnails[ start + i ] != null && 
					((Thumbnail)_thumbnails[ start + i ]).VisibleRect.Contains( e.X, e.Y ) )
				{
					_selectedIndex = start + i;

					if ( SelectedIndexChanged != null )
						SelectedIndexChanged( this, new EventArgs() );

					this.Invalidate();
					break;
				}
			}
		}
		
		protected override void OnPaintBackground(PaintEventArgs e)
		{
			// for double-buffering
		}

		protected override void OnPaint(PaintEventArgs e)
		{
			if ( _imageBuffer == null )
				OnResize( e );

			// get graphics object from buffer
			Graphics buf = Graphics.FromImage( _imageBuffer );

			// fill background
			buf.FillRectangle( WHITE_BRUSH, 0, 0, this.Width, this.Height );


			// draw thumbnails
			int idx = _firstViewable;

			if ( _scrollbarLoc == ScrollbarOrientation.Vertical )
			{
				for( int row = 0; row < _rows && idx < Count; row++ )
					for( int col = 0; col < _cols && idx < Count; col++, idx++ )
						drawThumbnail( buf, (Thumbnail)_thumbnails[ idx ], row, col, idx == _selectedIndex );
			}
			else
			{
				for( int col = 0; col < _cols && idx < Count; col++ )
					for( int row = 0; row < _rows && idx < Count; row++, idx++ )
						drawThumbnail( buf, (Thumbnail)_thumbnails[ idx ], row, col, idx == _selectedIndex );
			}

			// draw buffer to the screen
			e.Graphics.DrawImage( _imageBuffer, 0, 0 );
		}

		protected void drawThumbnail( Graphics g, Thumbnail tmbnail, int row, int col, bool selected )
		{
			int x = SPACER + col * ( THUMBNAIL_WIDTH + SPACER );
			int y = SPACER + row * ( THUMBNAIL_HEIGHT + SPACER );

			g.DrawRectangle( LIGHTGREY_PEN, x-1, y-1, THUMBNAIL_WIDTH+1, THUMBNAIL_HEIGHT+1 );

			if ( selected )
			{
				g.DrawRectangle( SELECTED_PEN, x-2, y-2, THUMBNAIL_WIDTH+3, THUMBNAIL_HEIGHT+3 );
				g.DrawRectangle( SELECTED_PEN, x-3, y-3, THUMBNAIL_WIDTH+5, THUMBNAIL_HEIGHT+5 );
			}

			tmbnail.VisibleRect = new Rectangle( x, y, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT );

			if ( tmbnail.Image.Width > THUMBNAIL_WIDTH || tmbnail.Image.Height > THUMBNAIL_HEIGHT )
			{
				// if the image is larger than a thumbnail size, scale it down to fit
				Rectangle scaledRect = new Rectangle( x, y, 0, 0 );
				if ( tmbnail.Image.Width > tmbnail.Image.Height )
				{
					scaledRect.Width = THUMBNAIL_WIDTH;
					scaledRect.Height = 
						(int)Math.Ceiling( (double)THUMBNAIL_WIDTH * ( (double)tmbnail.Image.Height / (double)tmbnail.Image.Width ) );
					scaledRect.Y = y + ( THUMBNAIL_HEIGHT - scaledRect.Height ) / 2;
				}
				else
				{
					scaledRect.Height = THUMBNAIL_HEIGHT;
					scaledRect.Width = 
						(int)Math.Ceiling( (double)THUMBNAIL_HEIGHT * ( (double)tmbnail.Image.Width / (double)tmbnail.Image.Height ) );
					scaledRect.X = x + ( THUMBNAIL_WIDTH - scaledRect.Width ) / 2;
				}

#if DESKTOP
				g.DrawImage( tmbnail.Image, scaledRect );
#endif
#if POCKETPC
				g.DrawImage( tmbnail.Image, scaledRect, new Rectangle( 0, 0, tmbnail.Image.Width, tmbnail.Image.Height ), GraphicsUnit.Pixel );
#endif
			}
			else
			{
				int nX = 0, nY = 0;
				if ( tmbnail.Image.Width < THUMBNAIL_WIDTH )
					nX = ( THUMBNAIL_WIDTH - tmbnail.Image.Width ) / 2;

				if ( tmbnail.Image.Height < THUMBNAIL_HEIGHT )
					nY = ( THUMBNAIL_HEIGHT - tmbnail.Image.Height ) / 2;

				Rectangle srcRect = SRCRECT;

				if ( tmbnail.Image.Width > THUMBNAIL_WIDTH )
					srcRect.X = ( tmbnail.Image.Width - THUMBNAIL_WIDTH ) / 2;

				if ( tmbnail.Image.Height > THUMBNAIL_HEIGHT )
					srcRect.Y = ( tmbnail.Image.Height - THUMBNAIL_HEIGHT ) / 2;

				Rectangle destRect = new Rectangle( x + nX, y + nY, THUMBNAIL_WIDTH - nX, THUMBNAIL_HEIGHT - nY );
				g.DrawImage( tmbnail.Image, destRect, srcRect, GraphicsUnit.Pixel );
			}
		}

		protected override void OnKeyDown(KeyEventArgs e)
		{
			switch( e.KeyCode )
			{
				case Keys.Up:
					if ( _selectedIndex != 0 )
					{
						_selectedIndex--;
						this.Invalidate();
					}
					break;

				case Keys.Down:
					if ( _selectedIndex != ( Count - 1 ) )
					{
						_selectedIndex++;
						this.Invalidate();
					}
					break;
			}
		}



		/*
		 * Event Handlers
		 */

		private void ScrollBar_ValueChanged(object sender, EventArgs e)
		{
			if ( _scrollbar.Value < 0 )
				return;

			if ( _scrollbarLoc == ScrollbarOrientation.Horizontal )
				_firstViewable = _scrollbar.Value * _rows;
			else
				_firstViewable = _scrollbar.Value * _cols;

			this.Invalidate();
		}


		/*
		 * Inner Class
		 */

		public class Thumbnail
		{
			/*
			 * Member Variables
			 */

			public Image		Image;
			public Rectangle	VisibleRect;


			/*
			 * Constructor
			 */

			public Thumbnail( Image image )
			{
				Image = image;
			}
		}
	}

	/// <summary>
	/// 
	/// </summary>
	public enum ScrollbarOrientation
	{
		Horizontal, Vertical
	}
}
