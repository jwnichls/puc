using System;
using System.Collections;
using System.Drawing;
using System.Windows.Forms;

using PocketPCControls;

using PUC;
using PUC.CIO;
using PUC.CIO.Image;
using PUC.Communication;
using PUC.Layout;
using PUC.Types;

namespace PUC.CIO.ImageList
{
	/// <summary>
	/// Summary description for ImageSmartCIO.
	/// </summary>
	public class ImageListSmartCIO : SmartCIO
	{
		/*
		 * Constants
		 */

		public const string HIGH_LEVEL_TYPE = "image-list";

		public const string IMAGE_STATE_NAME = "Image";

		protected const int IMAGE_SPACER	 = 3;


		/*
		 * Static Methods
		 */

		public static SmartCIO CreateImageListSmartCIO( PUC.GroupNode group )
		{
			if ( group is ListGroupNode && ((ListGroupNode)group).SelectionType == SelectionType.One )
				return new ImageListSmartCIO( group );

			return null;
		}


		/*
		 * Member Variables
		 */

		protected ApplianceState	_imageState;


		/*
		 * Constructors
		 */

		public ImageListSmartCIO( GroupNode specSnippet )
			: base( new ThumbnailViewer(), specSnippet )
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

			// move template group above list-group
			((BranchGroupNode)_specSnippet).Children.Remove( _templateGroup );

			int parentIdx = _specSnippet.Parent.Children.IndexOf( _specSnippet, 0, _specSnippet.Parent.Count );
			if ( parentIdx < 0 )
				parentIdx = 0;

			_specSnippet.Parent.Children.Insert( parentIdx + 1, _templateGroup );
			_templateGroup.Parent = _specSnippet.Parent;

			// add labels to template and list groups
			LabelDictionary ldict = new LabelDictionary();
			ldict.AddLabel( new StringValue( "Detail View" ) );
			_specSnippet.Labels = ldict;

			ldict = new LabelDictionary();
			ldict.AddLabel( new StringValue( "Thumbnails" ) );
			_templateGroup.Labels = ldict;

			// register event handlers
			((ListGroupNode)_specSnippet).SelectionState.ValueChangedEvent += new PUC.ApplianceState.ValueChangedHandler(SelectionChanged);			
			((ThumbnailViewer)_control).SelectedIndexChanged += new EventHandler(SelectedIndexChanged);

			_imageState.VariableTable.BinaryDataChanged += new PUC.VariableTable.BinaryEventHandler(VariableTable_BinaryDataChanged);
		}


		/*
		 * Member Methods
		 */

		public override string GetStringValue()
		{
			return null;
		}

		private void VariableTable_BinaryDataChanged(BinaryStateChangeNotification bscn)
		{
			int imageIndex = -1;
			// TODO: Determine if this a message for the image state we're interested in,
			//       and get the index within the list for later
			try
			{
				string state = bscn.State;
				if (! state.EndsWith( _imageState.DataWindow ) )
					return;

				// -2 to remove separator char and ']'
				state = state.Substring( 0, state.Length - _imageState.DataWindow.Length - 2 );

				int startIdx = state.LastIndexOf( "[" );

				if ( startIdx < 0 )
					return;

				// + 1 to remove '['
				state = state.Substring( startIdx + 1 );

				// -1 because list indices are 1-indexed
				imageIndex = Int32.Parse( state ) - 1;
			}
			catch( Exception )
			{
				return;
			}

			// if we get here, we are interested in the message

			if ( bscn.HasBinaryData() )
			{
				// we need to try to create an image from the binary data
				try
				{
					System.Drawing.Image img = null;

					if ( ImageSmartCIO.IsTypeSupported( bscn.ContentType ) )
						img = new Bitmap( bscn.BinaryData );

					if ( ((BinarySpace)_imageState.Type.ValueSpace)[ ImageSmartCIO.SCALABLE_IMAGE_OPT ] != null &&
						 ((ThumbnailViewer)_control)[ imageIndex ] != ThumbnailViewer.NO_IMAGE && 
						 ( img.Width > ThumbnailViewer.THUMBNAIL_WIDTH ||
						   img.Height > ThumbnailViewer.THUMBNAIL_HEIGHT ) )
						return;

					((ThumbnailViewer)_control)[ imageIndex ] = img;
				}
				catch( Exception )
				{
					Globals.GetFrame( _imageState.Appliance )
						.AddLogLine( "Couldn't process binary image file for " + bscn.State + "." );
				}
			}
			else
			{
				// send a state-value-request to get the new binary value
				// if supported, send the height and width that we desire

				try
				{
					((ThumbnailViewer)_control)[ imageIndex ] = null;

					StateValueRequest svrqst = new StateValueRequest( bscn.State );

					if ( ((BinarySpace)_imageState.Type.ValueSpace)[ ImageSmartCIO.SCALABLE_IMAGE_OPT ] != null )
					{
						svrqst[ StateValueRequest.DESIRED_WIDTH_OPT ] = ThumbnailViewer.THUMBNAIL_WIDTH.ToString();
						svrqst[ StateValueRequest.DESIRED_HEIGHT_OPT ] = ThumbnailViewer.THUMBNAIL_HEIGHT.ToString();
					}

					if ( ImageSmartCIO.IsTypeSupported( bscn.ContentType ) )
						_imageState.Appliance.GetConnection().Send( svrqst );
				}
				catch( Exception )
				{
				}
			}
		}

		public override Point GetControlOffset()
		{
			return new Point( 0, 0 );
		}

		public override Size GetMinimumSize()
		{
			// intended to create a 2x2 grid with horiz. scroll bar
			return new Size( 152, 172 );
		}

		public override PreferredSize GetPreferredSize()
		{
			return new PreferredSize( PreferredSize.INFINITE, PreferredSize.INFINITE );
		}

		public override bool PrefersFullWidth()
		{
			return true;
		}

		public override void FinalSizeNotify()
		{			
		}

		private void SelectionChanged(ApplianceState s)
		{
			int idx = s.Value == null ? 0 : (int)s.Value;

			((ThumbnailViewer)_control).SelectedIndex = idx-1;
		}

		private void SelectedIndexChanged(object sender, EventArgs e)
		{
			int idx = ((ThumbnailViewer)_control).SelectedIndex + 1;

			if ( ((ListGroupNode)_specSnippet).SelectionState.Defined && 
				 idx == (int)((ListGroupNode)_specSnippet).SelectionState.Value )
				return;

			((ListGroupNode)_specSnippet).SelectionState.RequestChange( idx );
		}
	}
}
