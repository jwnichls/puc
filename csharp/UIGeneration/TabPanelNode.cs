using System;
using System.Collections;
using System.Windows.Forms;

using PUC;
using PUC.CIO;
using PUC.Layout;

namespace PUC.UIGeneration
{
	/// <summary>
	/// This represents the node of an interface that is a panel
	/// within a tab control.
	/// </summary>
	public class TabPanelNode : MultiplePanelNode
	{
		/*
		 * Member Variables
		 */

		protected ContainerCIO _panel;


		/*
		 * Constructor
		 */

		public TabPanelNode( TabbedPanelCIO cio, GroupNode g )
		{
			_panel = cio;
			AddPanel( new PanelNode( g ) );
		}


		/*
		 * Member Methods
		 */

		public InterfaceNode GetChildNode()
		{
			return (InterfaceNode)_panels[ 0 ];
		}

		public ContainerCIO GetContainerCIO()
		{
			return _panel;
		}


		/*
		 * InterfaceNode Methods
		 */

		public override void AddComponents( ContainerCIO container, LayoutVariables vars )
		{
			Container = container;
			((InterfaceNode)_panels[ 0 ]).AddComponents( _panel, vars );

			this.CalculateMinimumSize( vars );
			this.CalculatePreferredSize( vars );
		}

		public override void CalculateMinimumSize(LayoutVariables vars)
		{
			((InterfaceNode)_panels[ 0 ]).CalculateMinimumSize( vars );
			_minSize = ((InterfaceNode)_panels[ 0 ]).MinimumSize;
		}

		public override void CalculatePreferredSize(LayoutVariables vars)
		{
			((InterfaceNode)_panels[ 0 ]).CalculatePreferredSize( vars );
			_prefSize = ((InterfaceNode)_panels[ 0 ]).PreferredSize; 
		}

		public override void DoLayout(LayoutVariables vars)
		{
			InterfaceNode node = (InterfaceNode)_panels[ 0 ];

			// set bounds to the control bounds, which are automattically set by
			// the TabControl
			Control c = _panel.GetControl();
			node.SetSize( c.Bounds.Width, c.Bounds.Height );
			node.SetLocation( 0, 0 );

			((InterfaceNode)_panels[ 0 ]).DoLayout( vars );
		}

		public override string ToString()
		{
			return "TabPanelNode - " + base.ToString();
		}
	}
}
