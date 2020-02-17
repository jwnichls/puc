using System;
using System.Collections;
using System.Windows.Forms;
using PUC;
using PUC.CIO;

namespace PUC.UIGeneration
{
	/// <summary>
	/// Summary description for FullWidthRow.
	/// </summary>
	public class FullWidthRow : Row
	{
		/*
		 * Constants
		 */

		protected const int COMPONENT_INDEX = 0;


		/*
		 * Constructor
		 */

		public FullWidthRow( GroupNode g, PanelNode panel, ConcreteInteractionObject cio )
			: base( g, panel )
		{
			addCIO( cio );
		}


		/*
		 * Member Methods
		 */

		public override void AddComponents( ContainerCIO container, LayoutVariables vars )
		{
			ControlBasedCIO stateCIO = (ControlBasedCIO)_CIOs[ COMPONENT_INDEX ];

			container.AddCIO( stateCIO );

			_maxTextOffset = stateCIO.GetControlOffset().Y;
			this.CalculateMinimumSize( vars );
			this.CalculatePreferredSize( vars );
		}

		public override void CalculateMinimumSize(LayoutVariables vars)
		{
			ControlBasedCIO stateCIO = (ControlBasedCIO)_CIOs[ COMPONENT_INDEX ];

			this.MinimumSize = stateCIO.GetMinimumSize();
		}

		public override void CalculatePreferredSize( LayoutVariables vars )
		{
			ControlBasedCIO stateCIO = (ControlBasedCIO)_CIOs[ COMPONENT_INDEX ];

			this.PreferredSize = stateCIO.GetPreferredSize();
		}

		public override void DoLayout(ContainerCIO container, LayoutVariables vars)
		{
			ControlBasedCIO stateCIO = (ControlBasedCIO)_CIOs[ COMPONENT_INDEX ];

			System.Windows.Forms.Control control = stateCIO.GetControl();
			control.Bounds = this.GetBounds();

			if ( stateCIO is LabelLinkedCIO )
				((Label)control).TextAlign = System.Drawing.ContentAlignment.TopCenter;
		}

		public override string ToString()
		{
			String ret = "FullWidthRow\r\n";

			StateLinkedCIO stateCIO = (StateLinkedCIO)_CIOs[ COMPONENT_INDEX ];

			ret += "0. StateCIO = " + stateCIO.GetApplObj().Name + " - [" + stateCIO.GetControl().Bounds.X + "," + stateCIO.GetControl().Bounds.Y + "," + stateCIO.GetControl().Bounds.Width + "," + stateCIO.GetControl().Bounds.Height + "]\r\n";

			return ret;
		}

	}
}
