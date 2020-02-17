using System;

using PUC;
using PUC.Rules;
using PUC.Rules.GroupScan;
using PUC.UIGeneration;

namespace PUC.Rules.TreeBuilding
{
	/// <summary>
	/// This rule looks for PanelDecisions in the group tree, which
	/// specify that the objects within this group should be represented
	/// on the included panel.  The panel is returned for use with the 
	/// members of the group.
	/// </summary>
	public class GetPanelRule : TreeBuildingRule
	{
		/*
		 * Process Method
		 */
		public override PanelNode Process( GroupNode g, PanelNode p )
		{
			if ( g.Decorations[ PanelDecision.DECISION_KEY ] != null )
				return ((PanelDecision)g.Decorations[ PanelDecision.DECISION_KEY ]).Panel;
			
			return p;
		}
	}
}
