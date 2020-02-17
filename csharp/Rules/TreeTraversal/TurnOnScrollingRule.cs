using System;

using PocketPCControls;

using PUC.UIGeneration;

namespace PUC.Rules.TreeTraversal
{
	/// <summary>
	/// Summary description for TurnOnScrollingRule.
	/// </summary>
	public class TurnOnScrollingRule : TreeTraversalRule
	{
		public override void Process( InterfaceNode node, UIGenerator ui )
		{
			if ( node is PanelNode )
				((ScrollingPanel)((PanelNode)node).GetContainerCIO().GetControl()).DisplayPolicy = ScrollBarDisplay.AsNeeded;
		}
	}
}
