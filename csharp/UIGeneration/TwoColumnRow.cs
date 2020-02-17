using System;
using System.Collections;
using System.Windows.Forms;
using PUC;
using PUC.CIO;

namespace PUC.UIGeneration
{
	/// <summary>
	/// Summary description for TwoColumnRow.
	/// </summary>
	public class TwoColumnRow : Row
	{
		/*
		 * Constants
		 */

		protected const int LABEL1_INDEX = 0;
		protected const int COMPONENT1_INDEX = 1;
		protected const int LABEL2_INDEX = 2;
		protected const int COMPONENT2_INDEX = 3;


		/*
		 * Constructor
		 */

		public TwoColumnRow( PanelNode panel, 
			ConcreteInteractionObject cio1, 
			ConcreteInteractionObject cio2 )
			: base( panel )
		{
			addCIO( cio1.GetLabelCIO() );
			addCIO( cio1 );
			addCIO( cio2.GetLabelCIO() );
			addCIO( cio2 );
		}


		/*
		 * Member Methods
		 */

		public override void AddComponents( ContainerCIO container )
		{
			LabelCIO labelCIO1 = (LabelCIO)_CIOs[ LABEL1_INDEX ];
			StateLinkedCIO stateCIO1 = (StateLinkedCIO)_CIOs[ COMPONENT1_INDEX ];
			LabelCIO labelCIO2 = (LabelCIO)_CIOs[ LABEL2_INDEX ];
			StateLinkedCIO stateCIO2 = (StateLinkedCIO)_CIOs[ COMPONENT2_INDEX ];

			container.AddCIO( labelCIO1 );
			container.AddCIO( stateCIO1 );
			container.AddCIO( labelCIO2 );
			container.AddCIO( stateCIO2 );
		}

		public override System.Drawing.Size GetPreferredSize(LayoutVariables vars)
		{
			LabelCIO labelCIO1 = (LabelCIO)_CIOs[ LABEL1_INDEX ];
			StateLinkedCIO stateCIO1 = (StateLinkedCIO)_CIOs[ COMPONENT1_INDEX ];
			LabelCIO labelCIO2 = (LabelCIO)_CIOs[ LABEL2_INDEX ];
			StateLinkedCIO stateCIO2 = (StateLinkedCIO)_CIOs[ COMPONENT2_INDEX ];

			System.Drawing.Size ret = new System.Drawing.Size( 0, 0 );

			ret.Height = Math.Max( stateCIO1.GetPreferredSize().Height,
								   stateCIO2.GetPreferredSize().Height );

			ret.Width = ( labelCIO1 != null ? labelCIO1.GetPreferredSize().Width : 0 ) +
						stateCIO1.GetPreferredSize().Width +
				        ( labelCIO2 != null ? labelCIO2.GetPreferredSize().Width : 0 ) +
						stateCIO2.GetPreferredSize().Width;

			ret.Height += vars.RowPadding;
			ret.Width += 3 * vars.RowPadding;

			return ret;			
		}

		public override int DoLayout(ContainerCIO container, int topY, LayoutVariables vars)
		{
			LabelCIO labelCIO1 = (LabelCIO)_CIOs[ LABEL1_INDEX ];
			StateLinkedCIO stateCIO1 = (StateLinkedCIO)_CIOs[ COMPONENT1_INDEX ];
			LabelCIO labelCIO2 = (LabelCIO)_CIOs[ LABEL2_INDEX ];
			StateLinkedCIO stateCIO2 = (StateLinkedCIO)_CIOs[ COMPONENT2_INDEX ];

			System.Windows.Forms.Label label = (Label)labelCIO1.GetControl();
			Control control = stateCIO1.GetControl();

			System.Drawing.Size prefSize1 = stateCIO1.GetPreferredSize();
			System.Drawing.Size prefSize2 = stateCIO2.GetPreferredSize();

			int rowHeight = Math.Max( prefSize1.Height, prefSize2.Height ); 
			int columnWidth = ( container.InternalSize.Width - 3 * vars.RowPadding ) / 2;
			int col1LabelWidth = (int)Math.Round( columnWidth * vars.TwoColLabel1Pcnt );
			int col1CompWidth = columnWidth - col1LabelWidth;
			int col2LabelWidth = (int)Math.Round( columnWidth * vars.TwoColLabel2Pcnt );
			int col2CompWidth = columnWidth = col2LabelWidth;

			control = stateCIO1.GetControl();
			control.Size = new System.Drawing.Size( col1CompWidth, rowHeight );
			control.Location = new System.Drawing.Point( col1LabelWidth + 2 * vars.RowPadding,
				topY - stateCIO1.GetControlOffset().Y );

			if ( labelCIO1 != null ) 
			{
				label = (Label)labelCIO1.GetControl();
				label.TextAlign = System.Drawing.ContentAlignment.TopRight;
				label.Size = new System.Drawing.Size( col1LabelWidth, rowHeight );
				label.Location = new System.Drawing.Point( vars.RowPadding,
					topY - labelCIO1.GetControlOffset().Y );
				labelCIO1.SetLabelText();
			}

			control = stateCIO2.GetControl();
			control.Size = new System.Drawing.Size( col2CompWidth, rowHeight );
			control.Location = new System.Drawing.Point( columnWidth + col2LabelWidth + 3 * vars.RowPadding,
				topY - stateCIO2.GetControlOffset().Y );

			if ( labelCIO2 != null ) 
			{
				label = (Label)labelCIO2.GetControl();
				label.TextAlign = System.Drawing.ContentAlignment.TopRight;
				label.Size = new System.Drawing.Size( col2LabelWidth, rowHeight );
				label.Location = new System.Drawing.Point( columnWidth + 2 * vars.RowPadding,
					topY - labelCIO2.GetControlOffset().Y );
				labelCIO2.SetLabelText();
			}

			return 1;
		}

		public override string ToString()
		{
			String ret = "TwoColumnRow\r\n";

			LabelCIO labelCIO1 = (LabelCIO)_CIOs[ LABEL1_INDEX ];
			StateLinkedCIO stateCIO1 = (StateLinkedCIO)_CIOs[ COMPONENT1_INDEX ];
			LabelCIO labelCIO2 = (LabelCIO)_CIOs[ LABEL2_INDEX ];
			StateLinkedCIO stateCIO2 = (StateLinkedCIO)_CIOs[ COMPONENT2_INDEX ];

			if ( labelCIO1 != null )
			{
				ret += "0. LabelCIO - [" + labelCIO1.GetControl().Bounds.X + "," + labelCIO1.GetControl().Bounds.Y + "," + labelCIO1.GetControl().Bounds.Width + "," + labelCIO1.GetControl().Bounds.Height + "]\r\n";
			}

			ret += "1. StateCIO1 = " + stateCIO1.GetApplObj().Name + " - [" + stateCIO1.GetControl().Bounds.X + "," + stateCIO1.GetControl().Bounds.Y + "," + stateCIO1.GetControl().Bounds.Width + "," + stateCIO1.GetControl().Bounds.Height + "]\r\n";

			ret += "2. LabelCIO - [" + labelCIO2.GetControl().Bounds.X + "," + labelCIO2.GetControl().Bounds.Y + "," + labelCIO2.GetControl().Bounds.Width + "," + labelCIO2.GetControl().Bounds.Height + "]\r\n";

			ret += "3. StateCIO2 = " + stateCIO2.GetApplObj().Name + " - [" + stateCIO2.GetControl().Bounds.X + "," + stateCIO2.GetControl().Bounds.Y + "," + stateCIO2.GetControl().Bounds.Width + "," + stateCIO2.GetControl().Bounds.Height + "]\r\n";

			return ret;
		}

	}
}
