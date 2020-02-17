using System;
using System.Collections;
using System.Windows.Forms;

using PUC;
using PUC.CIO;
using PUC.Layout;

namespace PUC.UIGeneration
{
	/// <summary>
	/// Summary description for LabeledTwoCompRow.
	/// </summary>
	public class LabeledTwoCompRow : Row
	{
		/*
		 * Constants
		 */

		protected const int LABEL_INDEX = 0;
		protected const int COMPONENT1_INDEX = 1;
		protected const int COMPONENT2_INDEX = 2;


		/*
		 * Constructor
		 */

		public LabeledTwoCompRow( GroupNode g,
								  PanelNode panel, 
								  LabelCIO labelCIO,
								  ConcreteInteractionObject cio1, 
								  ConcreteInteractionObject cio2 )
			: base( g, panel )
		{
			addCIO( labelCIO );
			addCIO( cio1 );
			addCIO( cio2 );
		}


		/*
		 * Member Methods
		 */

		public override void AddComponents( ContainerCIO container, LayoutVariables vars )
		{
			LabelCIO labelCIO = (LabelCIO)_CIOs[ LABEL_INDEX ];
			StateLinkedCIO stateCIO1 = (StateLinkedCIO)_CIOs[ COMPONENT1_INDEX ];
			StateLinkedCIO stateCIO2 = (StateLinkedCIO)_CIOs[ COMPONENT2_INDEX ];

			if ( labelCIO != null )
				container.AddCIO( labelCIO );

			container.AddCIO( stateCIO1 );
			container.AddCIO( stateCIO2 );

			_maxTextOffset = Math.Max( stateCIO1.GetControlOffset().Y, stateCIO2.GetControlOffset().Y );

			if ( labelCIO != null )
				_maxTextOffset = Math.Max( labelCIO.GetControlOffset().Y, _maxTextOffset );

			this.CalculateMinimumSize( vars );
			this.CalculatePreferredSize( vars );
		}

		public override void CalculateMinimumSize(LayoutVariables vars)
		{
			LabelCIO labelCIO = (LabelCIO)_CIOs[ LABEL_INDEX ];
			StateLinkedCIO stateCIO1 = (StateLinkedCIO)_CIOs[ COMPONENT1_INDEX ];
			StateLinkedCIO stateCIO2 = (StateLinkedCIO)_CIOs[ COMPONENT2_INDEX ];

			System.Drawing.Size lblSize = new System.Drawing.Size( 0, 0 );

			if ( labelCIO != null )
			{
				labelCIO.UseMinimumLabel();
				lblSize = labelCIO.GetMinimumSize();
			}

			System.Drawing.Size cio1Size = stateCIO1.GetMinimumSize();
			System.Drawing.Size cio2Size = stateCIO2.GetMinimumSize();

			if ( _parent.IsVertical() )
			{
				_minSize.Height = lblSize.Height + 
								  cio1Size.Height +
								  cio2Size.Height +
								  2 * vars.RowPadding;

				_minSize.Width = Math.Max( lblSize.Width, Math.Max( cio1Size.Width, cio2Size.Width ) );
			}
			else
			{
				_minSize.Height = Math.Max( cio1Size.Height, cio2Size.Height );

				_minSize.Width = 
					(int)Math.Ceiling( ( cio1Size.Width + cio2Size.Width + 2 * vars.RowPadding ) / ( 1.0 - vars.OneColLabelPcnt ) );
			}
		}

		public override void CalculatePreferredSize(LayoutVariables vars)
		{
			LabelCIO labelCIO = (LabelCIO)_CIOs[ LABEL_INDEX ];
			StateLinkedCIO stateCIO1 = (StateLinkedCIO)_CIOs[ COMPONENT1_INDEX ];
			StateLinkedCIO stateCIO2 = (StateLinkedCIO)_CIOs[ COMPONENT2_INDEX ];

			int prefWidth = 0, prefHeight = 0;

			PreferredSize lblSize = new PreferredSize( 0, 0 );
			
			if ( labelCIO != null ) 
			{
				labelCIO.UseMinimumLabel();
				lblSize = labelCIO.GetPreferredSize();
			}

			PreferredSize cio1Size = stateCIO1.GetPreferredSize();
			PreferredSize cio2Size = stateCIO2.GetPreferredSize();

			if ( lblSize.Height == PreferredSize.INFINITE ||
				cio1Size.Height == PreferredSize.INFINITE || 
				cio2Size.Height == PreferredSize.INFINITE )
			{
				prefHeight = PreferredSize.INFINITE;
			}

			if ( lblSize.Width == PreferredSize.INFINITE ||
				cio1Size.Width == PreferredSize.INFINITE || 
				cio2Size.Width == PreferredSize.INFINITE )
			{
				prefWidth = PreferredSize.INFINITE;
			}


			if ( _parent.IsVertical() )
			{
				if ( prefHeight != PreferredSize.INFINITE )
					prefHeight = lblSize.Height + cio1Size.Height + 
						cio2Size.Height + 2 * vars.RowPadding;

				if ( prefWidth != PreferredSize.INFINITE )
					prefWidth = Math.Max( lblSize.Width, 
						Math.Max( cio1Size.Width, cio2Size.Width ) );
			}
			else
			{
				if ( prefHeight != PreferredSize.INFINITE )
					prefHeight = Math.Max( cio1Size.Height, cio2Size.Height );

				if ( prefWidth != PreferredSize.INFINITE )
					prefWidth = 
						(int)Math.Ceiling( ( cio1Size.Width + cio2Size.Width + 2 * vars.RowPadding ) / ( 1.0 - vars.OneColLabelPcnt ) );
			}			

			_prefSize = new PUC.Layout.PreferredSize( prefWidth, prefHeight );
		}

		public override void DoLayout(ContainerCIO container, LayoutVariables vars)
		{
			LabelCIO labelCIO = (LabelCIO)_CIOs[ LABEL_INDEX ];
			StateLinkedCIO stateCIO1 = (StateLinkedCIO)_CIOs[ COMPONENT1_INDEX ];
			StateLinkedCIO stateCIO2 = (StateLinkedCIO)_CIOs[ COMPONENT2_INDEX ];

			System.Windows.Forms.Label label = null;
			
			if ( labelCIO != null )
				label = (Label)labelCIO.GetControl();

			Control control1 = stateCIO1.GetControl();
			Control control2 = stateCIO2.GetControl();

			if ( _parent.IsVertical() )
			{
				int[] minHeights = null;
				int[] prefHeights = null;
				int[] heights = null;

				if ( labelCIO != null )
				{
					labelCIO.UseMinimumLabel();	
					labelCIO.SetAlignment( System.Drawing.ContentAlignment.TopCenter );

					// determine size allocations based on bounds, and minimum and preferred sizes
					// Height is the dimension to allocate in this case
					minHeights = new int[ 3 ] { labelCIO.GetMinimumSize().Height, 
												  stateCIO1.GetMinimumSize().Height,
												  stateCIO2.GetMinimumSize().Height  };
					prefHeights = new int[ 3 ] { labelCIO.GetPreferredSize().Height,
												   stateCIO1.GetPreferredSize().Height,
												   stateCIO2.GetPreferredSize().Height };

					heights = 
						LayoutAlgorithms.AllocateSizeValues( _bounds.Height, minHeights, prefHeights, vars.RowPadding );

					label.Size = new System.Drawing.Size( _bounds.Width, heights[ 0 ] );
					label.Location = new System.Drawing.Point( _bounds.X, _bounds.Y );
					labelCIO.SetLabelText();
				}
				else
				{
					minHeights = new int[ 3 ] { 0, 
												stateCIO1.GetMinimumSize().Height,
												stateCIO2.GetMinimumSize().Height };
					prefHeights = new int[ 3 ] { 0,
												 stateCIO1.GetPreferredSize().Height,
												 stateCIO2.GetPreferredSize().Height };

					heights = 
						LayoutAlgorithms.AllocateSizeValues( _bounds.Height, minHeights, prefHeights, vars.RowPadding );
				}

				control1.Size = new System.Drawing.Size( _bounds.Width, heights[ 1 ] );
				control1.Location = new System.Drawing.Point( _bounds.X, label.Size.Height + label.Location.Y + vars.RowPadding );

				if ( stateCIO1 is LabelLinkedCIO )
					((Label)control1).TextAlign = System.Drawing.ContentAlignment.TopCenter;

				control2.Size = new System.Drawing.Size( _bounds.Width, heights[ 2 ] );
				control2.Location = new System.Drawing.Point( _bounds.X, control1.Size.Height + control1.Location.Y + vars.RowPadding );

				if ( stateCIO2 is LabelLinkedCIO )
					((Label)control2).TextAlign = System.Drawing.ContentAlignment.TopCenter;
			}
			else
			{
				int labelWidth = (int)Math.Round( vars.OneColLabelPcnt * ( _bounds.Width - vars.RowPadding ) );
				int compWidth = ( _bounds.Width - labelWidth ) / 2 - vars.RowPadding;

				int[] textOffsets = 
					LayoutAlgorithms.GetTextHeightOffsets( LayoutAlgorithms.GetArrayFromArrayList( _CIOs ) );

				if ( labelCIO != null )
				{
					label.TextAlign = System.Drawing.ContentAlignment.TopRight;
					label.Size = new System.Drawing.Size( labelWidth, _bounds.Height );
					label.Location = new System.Drawing.Point( _bounds.X, _bounds.Y + textOffsets[ 0 ] );
					labelCIO.SetLabelText();
				}

				control1.Size = new System.Drawing.Size( compWidth, _bounds.Height );
				control1.Location = new System.Drawing.Point( _bounds.X + labelWidth + vars.RowPadding,
															  _bounds.Y + textOffsets[ 1 ] );

				control2.Size = new System.Drawing.Size( compWidth, _bounds.Height );
				control2.Location = new System.Drawing.Point( control1.Location.X + compWidth + vars.RowPadding,
															  _bounds.Y + textOffsets[ 2 ] );
			}
		}

		public override string ToString()
		{
			String ret = "LabeledTwoCompRow\r\n";

			LabelCIO labelCIO = (LabelCIO)_CIOs[ LABEL_INDEX ];
			StateLinkedCIO stateCIO1 = (StateLinkedCIO)_CIOs[ COMPONENT1_INDEX ];
			StateLinkedCIO stateCIO2 = (StateLinkedCIO)_CIOs[ COMPONENT2_INDEX ];

			if ( labelCIO != null )
			{
				ret += "0. LabelCIO - [" + labelCIO.GetControl().Bounds.X + "," + labelCIO.GetControl().Bounds.Y + "," + labelCIO.GetControl().Bounds.Width + "," + labelCIO.GetControl().Bounds.Height + "]\r\n";
			}

			ret += "1. StateCIO1 = " + stateCIO1.GetApplObj().Name + " - [" + stateCIO1.GetControl().Bounds.X + "," + stateCIO1.GetControl().Bounds.Y + "," + stateCIO1.GetControl().Bounds.Width + "," + stateCIO1.GetControl().Bounds.Height + "]\r\n";

			ret += "2. StateCIO2 = " + stateCIO2.GetApplObj().Name + " - [" + stateCIO2.GetControl().Bounds.X + "," + stateCIO2.GetControl().Bounds.Y + "," + stateCIO2.GetControl().Bounds.Width + "," + stateCIO2.GetControl().Bounds.Height + "]\r\n";

			return ret;
		}

	}
}
