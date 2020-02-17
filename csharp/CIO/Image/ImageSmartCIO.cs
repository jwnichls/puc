using System;
using System.Collections;
using System.Drawing;
using System.Windows.Forms;

using PocketPCControls;

using PUC;
using PUC.CIO;
using PUC.Communication;
using PUC.Layout;
using PUC.Types;

namespace PUC.CIO.Image
{
	/// <summary>
	/// Summary description for ImageSmartCIO.
	/// </summary>
	public class ImageSmartCIO : SmartCIO
	{
		/*
		 * Constants
		 */

		public const string HIGH_LEVEL_TYPE = "image";

		public const string IMAGE_STATE_NAME = "Image";

		public const string SCALABLE_IMAGE_OPT = "arbitrary-scaling";
		public const string FIXED_HEIGHT = "fixed-height";
		public const string FIXED_WIDTH  = "fixed-width";
		public const string MINIMUM_HEIGHT = "minimum-height";
		public const string MINIMUM_WIDTH  = "minimum-width";

		protected const int IMAGE_SPACER	 = 3;


		// accepted MIME types (others may work...more testing needed)
		public const string MIME_JPG	= "image/jpeg";
		public const string MIME_GIF	= "image/gif";
		public const string MIME_MS_BMP	= "image/x-ms-bmp";
		public const string MIME_PNG	= "image/png";


		/*
		 * Static Methods and Constructor
		 */

		protected static Hashtable _supportedTypes;

		static ImageSmartCIO() 
		{
			_supportedTypes = new Hashtable();
			_supportedTypes[ MIME_JPG ] = true;
			_supportedTypes[ MIME_GIF ] = true;
			_supportedTypes[ MIME_MS_BMP ] = true;
			_supportedTypes[ MIME_PNG ] = true;
		}

		public static bool IsTypeSupported( string type )
		{
			return _supportedTypes.ContainsKey( type );
		}

		public static SmartCIO CreateImageSmartCIO( PUC.GroupNode group )
		{
			return new ImageSmartCIO( group );
		}


		/*
		 * Member Variables
		 */

		protected ApplianceState	_imageState;
		protected ImageDrawer		_imageDrawer;
		protected Label				_imageLabel;


		/*
		 * Constructors
		 */

		public ImageSmartCIO( GroupNode specSnippet )
			: base( new Panel(), specSnippet )
		{
			if ( specSnippet.IsObject() )
			{
				_imageState = (ApplianceState)_objects[ SINGLE_STATE ];
			}
			else
			{
				_imageState = (ApplianceState)_objects[ IMAGE_STATE_NAME ];

				// look for labels in state if none were found in group
				if ( _labels == null )
					_labels = _imageState.Labels;
			}

			if ( _imageState.Type.ValueSpace is BinarySpace )
				doNotRenderObject( _imageState );

			Panel p = (Panel)GetControl();

			_imageDrawer = new ImageDrawer();
			p.Controls.Add( _imageDrawer );
            
			if ( _labels != null )
			{
				_imageLabel = new Label();
				_imageLabel.Text = _labels.GetShortestLabel();
				_imageLabel.Location = new Point( 0, 0 );
				_imageLabel.Width = p.Width;
				_imageLabel.Height = (int)Globals.MeasureString( _imageLabel.Text, _imageLabel.Font ).Height;
				p.Controls.Add( _imageLabel );

				_imageDrawer.Location = new Point( 0, _imageLabel.Height + IMAGE_SPACER );
				_imageDrawer.Size = new Size( p.Width, p.Height - _imageLabel.Height );
			}
			else
			{
				_imageDrawer.Location = new Point( 0, 0 );
				_imageDrawer.Size = p.Size;
			}

			p.Resize += new EventHandler(p_Resize);

			_imageState.VariableTable.BinaryDataChanged += new PUC.VariableTable.BinaryEventHandler(VariableTable_BinaryDataChanged);
			_imageState.ValueChangedEvent += new PUC.ApplianceState.ValueChangedHandler(imageState_ValueChangedEvent);
		}


		/*
		 * Member Methods
		 */

		public override string GetStringValue()
		{
			return null;
		}


		private void p_Resize(object sender, EventArgs e)
		{
			if ( _labels != null )
			{
				_imageLabel.Width = ((Control)sender).Width;
				_imageLabel.Text = _labels.GetLabelByPixelLength( _imageLabel.Font, _imageLabel.Width );
				_imageDrawer.Size = new Size( _imageLabel.Width, ((Control)sender).Height - _imageLabel.Height );
			}
			else
			{
				_imageDrawer.Size = ((Control)sender).Size;
			}
		}

		private void VariableTable_BinaryDataChanged(BinaryStateChangeNotification bscn)
		{
			if ( bscn.State != _imageState.DataName )
				return;

			// if we get here, we are interested in the message

			if ( bscn.HasBinaryData() )
			{
				// we need to try to create an image from the binary data

				try
				{
					if ( ImageSmartCIO.IsTypeSupported( bscn.ContentType ) )
						_imageDrawer.Image = new Bitmap( bscn.BinaryData );
				}
				catch( Exception )
				{
					Globals.GetFrame( _imageState.Appliance )
						.AddLogLine( "Couldn't process binary image file for " + _imageState.DataName + "." );
				}
			}
			else
			{
				// send a state-value-request to get the new binary value
				// if supported, send the height and width that we desire

				try
				{
					StateValueRequest svrqst = new StateValueRequest( _imageState.DataName );

					if ( ((BinarySpace)_imageState.Type.ValueSpace)[ SCALABLE_IMAGE_OPT ] != null )
					{
						svrqst[ StateValueRequest.DESIRED_WIDTH_OPT ] = _imageDrawer.Width.ToString();
						svrqst[ StateValueRequest.DESIRED_HEIGHT_OPT ] = _imageDrawer.Height.ToString();
					}

					if ( ImageSmartCIO.IsTypeSupported( bscn.ContentType ) )
					{
						_imageState.Appliance.GetConnection().Send( svrqst );

						_imageDrawer.Image = ThumbnailViewer.NO_IMAGE;
					}
				}
				catch( Exception )
				{
				}
			}
		}

		public override Point GetControlOffset()
		{
			/*
			if ( _imageLabel != null )
	            return new Point( 0, (int)Globals.MeasureString( _imageLabel.Text, _imageLabel.Font ).Height );
			*/

			return new Point( 0, 0 );
		}

		public override Size GetMinimumSize()
		{
			// these values are arbitrary, but chosen to match the 4:3 ratio that
			// likely to be common of many images.  Hopefully the specification
			// designer will have elected to give us more information.
			int w = 140, h = 105;
			BinarySpace space = (BinarySpace)_imageState.Type.ValueSpace;

			try
			{
				if ( space[ MINIMUM_WIDTH ] != null )
					w = Int32.Parse( (string)space[ MINIMUM_WIDTH ] );
				else if ( space[ FIXED_WIDTH ] != null )
					w = Int32.Parse( (string)space[ FIXED_WIDTH ] );

				if ( space[ MINIMUM_HEIGHT ] != null )
					h = Int32.Parse( (string)space[ MINIMUM_HEIGHT ] );
				else if ( space[ FIXED_HEIGHT ] != null )
					h = Int32.Parse( (string)space[ FIXED_HEIGHT ] );
			}
			catch( Exception )
			{
			}

			return new Size( w, h );
		}

		public override PreferredSize GetPreferredSize()
		{
			int w = PreferredSize.INFINITE, h = PreferredSize.INFINITE;
			
			BinarySpace space = (BinarySpace)_imageState.Type.ValueSpace;

			try
			{
				if ( space[ FIXED_WIDTH ] != null )
					w = Int32.Parse( (string)space[ FIXED_WIDTH ] );

				if ( space[ FIXED_HEIGHT ] != null )
					h = Int32.Parse( (string)space[ FIXED_HEIGHT ] );
			}
			catch( Exception )
			{
			}

			return new PreferredSize( w, h );
		}

		public override bool PrefersFullWidth()
		{
			return true;
		}

		public override void FinalSizeNotify()
		{			
		}

		/// <summary>
		/// This event should only ever be called if the data window underlying the 
		/// image state has moved, which would only happen if this image is in a 
		/// list of some kind.  If this occurs, we automatically send a StateValueRequest
		/// message for the new image.
		/// </summary>
		/// <param name="s">the state, presumably _imageState, whose value changed</param>
		private void imageState_ValueChangedEvent(ApplianceState s)
		{
			try
			{
				StateValueRequest svrqst = new StateValueRequest( _imageState.DataName );

				if ( ((BinarySpace)_imageState.Type.ValueSpace)[ SCALABLE_IMAGE_OPT ] != null )
				{
					svrqst[ StateValueRequest.DESIRED_WIDTH_OPT ] = _imageDrawer.Width.ToString();
					svrqst[ StateValueRequest.DESIRED_HEIGHT_OPT ] = _imageDrawer.Height.ToString();
				}

				_imageState.Appliance.GetConnection().Send( svrqst );
			}
			catch( Exception )
			{
			}			
		}
	}
}
