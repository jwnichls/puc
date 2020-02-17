using System;
using System.Collections;
using System.Windows.Forms;

using PUC;
using PUC.CIO;
using PUC.Layout;

namespace PUC.UIGeneration
{
	/// <summary>
	/// Summary description for OneColumnRow.
	/// </summary>
	public class OneColumnRow : Row
	{
		/*
		 * Constants
		 */

		protected const int LABEL_INDEX = 0;
		protected const int COMPONENT_INDEX = 1;


		/*
		 * Constructor
		 */

		public OneColumnRow( GroupNode g, PanelNode panel, ConcreteInteractionObject cio )
			: base( g, panel )
		{
			addCIO( cio.GetLabelCIO() );
			addCIO( cio );
		}


		/*
		 * Member Methods
		 */

		public override void AddComponents( ContainerCIO container, LayoutVariables vars )
		{
			LabelCIO labelCIO = (LabelCIO)_CIOs[ LABEL_INDEX ];
			ControlBasedCIO stateCIO = (ControlBasedCIO)_CIOs[ COMPONENT_INDEX ];

			container.AddCIO( labelCIO );
			container.AddCIO( stateCIO );

			if ( labelCIO != null )
				_maxTextOffset = Math.Max( labelCIO.GetControlOffset().Y, stateCIO.GetControlOffset().Y );
			else
				_maxTextOffset = stateCIO.GetControlOffset().Y;

			this.CalculateMinimumSize( vars );
			this.CalculatePreferredSize( vars );
		}

		public override void CalculateMinimumSize(LayoutVariables vars)
		{
			LabelCIO labelCIO = (LabelCIO)_CIOs[ LABEL_INDEX ];
			ControlBasedCIO stateCIO = (ControlBasedCIO)_CIOs[ COMPONENT_INDEX ];

			System.Drawing.Size cioSize = stateCIO.GetMinimumSize();

			if ( _parent.IsVertical() )
			{
				_minSize = cioSize;

				if ( labelCIO != null )
				{
					labelCIO.UseMinimumLabel();
					System.Drawing.Size labelSize = labelCIO.GetMinimumSize();

					_minSize.Height += labelSize.Height + vars.RowPadding;
					_minSize.Width = Math.Max( _minSize.Width, labelSize.Width );
				}
			}
			else
			{
				_minSize.Height = cioSize.Height;
				_minSize.Width = vars.RowPadding + 
					(int)Math.Ceiling( cioSize.Width / ( 1.0 - vars.OneColLabelPcnt ) );

				if ( labelCIO != null )
					_minSize.Height = Math.Max( _minSize.Height, labelCIO.GetMinimumSize().Height );
			}
		}

		public override void CalculatePreferredSize(LayoutVariables vars)
		{
			LabelCIO labelCIO = (LabelCIO)_CIOs[ LABEL_INDEX ];
			ControlBasedCIO stateCIO = (ControlBasedCIO)_CIOs[ COMPONENT_INDEX ];

			PreferredSize cioSize = stateCIO.GetPreferredSize();
			int prefWidth = cioSize.Width, prefHeight = cioSize.Height;

			if ( _parent.IsVertical() )
			{
				if ( labelCIO != null )
				{
					labelCIO.UseMinimumLabel();
					PreferredSize labelSize = labelCIO.GetPreferredSize();

					if ( prefHeight != PreferredSize.INFINITE )
						prefHeight += labelSize.Height + vars.RowPadding;

					if ( prefWidth != PreferredSize.INFINITE )
						prefWidth = Math.Max( prefWidth, labelSize.Width );
				}
			}
			else
			{
				if ( prefWidth != PreferredSize.INFINITE )
					prefWidth = vars.RowPadding +
						(int)Math.Ceiling( prefWidth / ( 1.0 - vars.OneColLabelPcnt ) );

				if ( labelCIO != null && prefHeight != PreferredSize.INFINITE )
					prefHeight = Math.Max( labelCIO.GetPreferredSize().Height, prefHeight );
			}

			_prefSize = new PreferredSize( prefWidth, prefHeight );
		}

		public override void DoLayout(ContainerCIO container, LayoutVariables vars)
		{
			LabelCIO labelCIO = (LabelCIO)_CIOs[ LABEL_INDEX ];
			ControlBasedCIO stateCIO = (ControlBasedCIO)_CIOs[ COMPONENT_INDEX ];

			System.Windows.Forms.Label label = null;
			Control control = stateCIO.GetControl();
			int topY = 0;

			if ( _parent.IsVertical() )
			{
				if ( labelCIO != null )
				{
					labelCIO.UseMinimumLabel();
					label = (Label)labelCIO.GetControl();
					labelCIO.SetAlignment( System.Drawing.ContentAlignment.TopCenter );
					label.Size = new System.Drawing.Size( _bounds.Width, labelCIO.GetMinimumSize().Height );
					label.Location = new System.Drawing.Point( _bounds.X, _bounds.Y );
					labelCIO.SetLabelText();

					topY = label.Size.Height + vars.RowPadding;
				}

				if ( stateCIO is LabelLinkedCIO )
					((Label)control).TextAlign = System.Drawing.ContentAlignment.TopCenter;

				int[] size = LayoutAlgorithms.AllocateSizeValues( 
					_bounds.Height - topY, 
					new int[ 1 ] { stateCIO.GetMinimumSize().Height },
					new int[ 1 ] { stateCIO.GetPreferredSize().Height }, 
					vars.RowPadding );

				control.Size = new System.Drawing.Size( _bounds.Width, size[ 0 ] );
				control.Location = new System.Drawing.Point( _bounds.X, _bounds.Y + topY );
			}
			else
			{
				int labelWidth = (int)Math.Round( vars.OneColLabelPcnt * ( _bounds.Width - vars.RowPadding ) );
				int compWidth = _bounds.Width - labelWidth - vars.RowPadding;

				int[] textOffsets = 
					LayoutAlgorithms.GetTextHeightOffsets( LayoutAlgorithms.GetArrayFromArrayList( _CIOs ) );

				control.Size = new System.Drawing.Size( compWidth, _bounds.Height );
				control.Location = new System.Drawing.Point( _bounds.X + labelWidth + vars.RowPadding,
															 _bounds.Y + textOffsets[ 1 ] );

				if ( labelCIO != null )
				{
					labelCIO.UseMinimumLabel();
					label = (Label)labelCIO.GetControl();
					label.TextAlign = System.Drawing.ContentAlignment.TopRight;
					label.Size = new System.Drawing.Size( labelWidth, _bounds.Height - textOffsets[ 0 ] );
					label.Location = new System.Drawing.Point( _bounds.X, _bounds.Y + textOffsets[ 0 ] );
					labelCIO.SetLabelText();
				}
			}
		}

		public override string ToString()
		{
			String ret = "OneColumnRow\r\n";

			LabelCIO labelCIO = (LabelCIO)_CIOs[ LABEL_INDEX ];
			StateLinkedCIO stateCIO = (StateLinkedCIO)_CIOs[ COMPONENT_INDEX ];

			if ( labelCIO != null )
			{
				ret += "0. LabelCIO - [" + labelCIO.GetControl().Bounds.X + "," + labelCIO.GetControl().Bounds.Y + "," + labelCIO.GetControl().Bounds.Width + "," + labelCIO.GetControl().Bounds.Height + "]\r\n";
			}

			ret += "1. StateCIO = " + stateCIO.GetApplObj().Name + " - [" + stateCIO.GetControl().Bounds.X + "," + stateCIO.GetControl().Bounds.Y + "," + stateCIO.GetControl().Bounds.Width + "," + stateCIO.GetControl().Bounds.Height + "]\r\n";

			return ret;
		}

	}
}
