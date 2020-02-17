using System;
using System.Drawing;
using System.IO;
using System.Windows.Forms;

namespace DesktopControls
{
	/// <summary>
	/// Summary description for ImageBtn.
	/// </summary>
	public class ImageButton : RadioButton
	{
		/*
		 * Member Variables
		 */

		protected Image _buttonUpImage;
		protected Image _buttonDownImage;
		protected Image _disabledImage;

		protected Image _imageBuffer;

		protected bool  _stayDown;

		protected bool  _mouseDown;
		protected bool  _activePicture;

		// brushes & pens

		Brush _backgroundBrush = new SolidBrush( Color.LightGray );
		Brush _selectedBrush   = new SolidBrush( Color.DarkGray );
		Pen   _blackPen        = new Pen( Color.Black );
		Pen	  _disabledBorder  = new Pen( Color.Gray );


		/*
		 * Events
		 */

		public event EventHandler Activated;


		/*
		 * Constructors
		 */

		public ImageButton()
		{
			_stayDown = false;
			_mouseDown = false;
			_activePicture = false;

			this.Appearance = Appearance.Button;

			this.Resize += new EventHandler(ImageButton_Resize);

			this.EnabledChanged +=new EventHandler(ImageButton_EnabledChanged);
		}


		/*
		 * Properties
		 */

		public bool StayDown
		{
			get
			{
				return _stayDown;
			}
			set
			{
				_stayDown = value;
			}
		}

		public Image ButtonUpImage
		{
			get
			{
				return _buttonUpImage;
			}
			set
			{
				_buttonUpImage = value;

				if ( this.Enabled && !_activePicture )
					this.Invalidate();
			}
		}

		public Image ButtonDownImage
		{
			get
			{
				return _buttonDownImage;
			}
			set
			{
				_buttonDownImage = value;

				if ( this.Enabled && _activePicture )
					this.Invalidate();
			}
		}

		public Image DisabledImage
		{
			get
			{
				return _disabledImage;
			}
			set
			{
				_disabledImage = value;

				if ( !this.Enabled )
					this.Invalidate();
			}
		}


		/*
		 * Member Methods
		 */

		public void PushDown()
		{
			_mouseDown = false;
			_activePicture = true;
			this.Checked = true;
			this.Invalidate();
		}

		public void PushUp()
		{
			_mouseDown = false;
			_activePicture = false;
			this.Checked = false;
			this.Invalidate();
		}


		/*
		 * Event Handlers
		 */

		protected override void OnMouseDown(MouseEventArgs e)
		{
			_mouseDown = true;
			_activePicture = true;

			if ( _stayDown && Activated != null )
				Activated( this, new EventArgs() );

			this.Checked = true;
			this.Invalidate();
		}

		protected override void OnMouseUp(MouseEventArgs e)
		{
			_mouseDown = false;

			if ( _stayDown ) return;

			if ( Activated != null )
				Activated( this, new EventArgs() );

			this.PushUp();
		}

		protected override void OnMouseMove(MouseEventArgs e)
		{
			if ( !_mouseDown || _stayDown )
				return;

			base.OnMouseMove(e);
			
			bool changed = false;

			if ( e.X >= 0 && e.Y >= 0 && e.X < this.Size.Width && e.Y < this.Size.Height ) 
			{
				changed = !_activePicture;
				_activePicture = true;
			}
			else
			{
				changed = _activePicture;
				_activePicture = false;
			}

			if ( changed )
				this.Invalidate();
		}

		protected override void OnPaintBackground(PaintEventArgs e)
		{
			// for double-buffering
		}

		protected override void OnPaint(PaintEventArgs e)
		{
			// used for double-buffering
			Graphics offscreen = Graphics.FromImage( _imageBuffer );

			base.OnPaint( new PaintEventArgs( offscreen, e.ClipRectangle ) );

			/*
			if ( this.Enabled )
				offscreen.DrawRectangle( _blackPen, 0, 0, this.Size.Width-1, this.Size.Height-1 );
			else
				offscreen.DrawRectangle( _disabledBorder, 0, 0, this.Size.Width-1, this.Size.Height-1 );

			if ( this.Enabled && _activePicture )
				offscreen.FillRectangle( _selectedBrush, 1, 1, this.Size.Width-2, this.Size.Height-2 );
			else
				offscreen.FillRectangle( _backgroundBrush, 1, 1, this.Size.Width-2, this.Size.Height-2 );
			*/

			if ( !this.Enabled && _disabledImage != null )
				drawImageCentered( _disabledImage, offscreen );
			else if ( _activePicture && _buttonDownImage != null )
				drawImageCentered( _buttonDownImage, offscreen );
			else if ( _buttonUpImage != null )
				drawImageCentered( _buttonUpImage, offscreen );

			e.Graphics.DrawImage( _imageBuffer, 0, 0 );
		}

		private void drawImageCentered( Image i, Graphics g )
		{
			int cx, cy;

			if ( i == null )
				return;

			cx = ( this.Size.Width - i.Width ) / 2;
			cy = ( this.Size.Height - i.Height ) / 2;

			if ( _activePicture )
			{
				// give the pressed image an offset
				cx++;
				cy++;
			}

			// set transparency key
			System.Drawing.Imaging.ImageAttributes imageAttr = new System.Drawing.Imaging.ImageAttributes();
			imageAttr.SetColorKey( backgroundImageColor( i ), backgroundImageColor( i ) );

			Rectangle imgRect = new Rectangle( cx, cy, i.Width, i.Height );

			g.DrawImage( i, imgRect, 0, 0, i.Width, i.Height, GraphicsUnit.Pixel, imageAttr );
		}

		private Color backgroundImageColor(Image image)
		{
			Bitmap bmp = new Bitmap(image);
			return bmp.GetPixel(0, 0);
		}

		private void ImageButton_Resize(object sender, EventArgs e)
		{
			if ( _imageBuffer != null )
				_imageBuffer.Dispose();
			
			_imageBuffer = new Bitmap( this.Size.Width, this.Size.Height );
			this.Invalidate();
		}

		private void ImageButton_EnabledChanged(object sender, EventArgs e)
		{
			this.Invalidate();
		}
	}
}
