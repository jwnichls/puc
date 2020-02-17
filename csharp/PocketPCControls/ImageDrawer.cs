using System;
using System.Drawing;
using System.Windows.Forms;

namespace PocketPCControls
{
	/// <summary>
	/// Summary description for ImageDrawer.
	/// </summary>
	public class ImageDrawer : Control
	{
		/*
		 * Constants
		 */

		protected const int SCROLLBAR_DIM = 10;


		/*
		 * Member Variables
		 */

		protected Image _image;


		/*
		 * Constructor
		 */

		public ImageDrawer()
		{
		}


		/*
		 * Properties
		 */

		public Image Image
		{
			get
			{
				return _image;
			}
			set
			{
				_image = value;
				this.Invalidate();
			}
		}


		/*
		 * Member Methods
		 */

		protected override void OnPaint(PaintEventArgs e)
		{
			base.OnPaint (e);

			if ( _image == null )
				return;

			Rectangle destRect = new Rectangle( 0, 0, _image.Width, _image.Height );

			if ( _image.Width > this.Width || _image.Height > this.Height )
			{
				// if the image is larger than the size of this component, scale it down to fit
				double imageRatio = (double)_image.Width / (double)_image.Height;
				double compRatio = (double)this.Width / (double)this.Height;

				if ( compRatio < imageRatio )
				{
					destRect.Width = this.Width;
					destRect.Height = 
						(int)Math.Ceiling( (double)this.Width * ( (double)_image.Height / (double)_image.Width ) );
				}
				else
				{
					destRect.Height = this.Height;
					destRect.Width = 
						(int)Math.Ceiling( (double)this.Height * ( (double)_image.Width / (double)_image.Height ) );
				}
			}

			if ( destRect.Width < this.Width )
				destRect.X = ( this.Width - destRect.Width ) / 2;

			if ( destRect.Height < this.Height )
				destRect.Y = ( this.Height - destRect.Height ) / 2;

			if ( _image != null )
#if DESKTOP
				e.Graphics.DrawImage( _image, destRect );
#endif
#if POCKETPC
				e.Graphics.DrawImage( _image, destRect, new Rectangle( 0, 0, _image.Width, _image.Height ), GraphicsUnit.Pixel );
#endif
		}

	}
}
