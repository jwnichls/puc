using System;

using PUC;
using PUC.Rules;
using PUC.Rules.GroupScan;
using PUC.UIGeneration;

namespace PUC.Rules.TreeBuilding
{
	/// <summary>
	/// This rule takes organizational decisions that were made earlier
	/// in the process and applies in the construction of the interface
	/// tree.
	/// </summary>
	public class OrganizeTreeRule : TreeBuildingRule
	{
		/*
		 * Process Method
		 */

		public override PanelNode Process( GroupNode g, PanelNode p )
		{
			if ( g.Decorations[ OrganizationDecision.DECISION_KEY ] != null ) 
			{
				OrganizationDecision d = (OrganizationDecision)g.Decorations[ OrganizationDecision.DECISION_KEY ];

				// determine which groups belong with which panels, and 
				// create the appropriate nodes in the interface tree
				d.AddOrganization( g, p );
			}

			return p;
		}

	}
}
