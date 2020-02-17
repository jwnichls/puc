using System;

using PUC;
using PUC.CIO;
using PUC.Rules;
using PUC.Rules.GroupScan;
using PUC.Rules.SpecScan;
using PUC.Types;
using PUC.UIGeneration;


namespace PUC.Rules.TreeBuilding
{
	/// <summary>
	/// Determines when a full width row should be used.
	/// </summary>
	public class OneColumnRowRule : TreeBuildingRule
	{
		/*
		 * Process Method
		 */
		public override PanelNode Process(GroupNode g, PanelNode p)
		{
			if ( g.Decorations[ UnitDecision.DECISION_KEY ] != null )
			{
				UnitDecision unitDecision = (UnitDecision)g.Decorations[ UnitDecision.DECISION_KEY ];
				ConcreteInteractionObject cio = unitDecision.CIO;
		
				if ( !unitDecision.Handled )
				{
					p.AddRow( new OneColumnRow( g, p, cio ) );

					unitDecision.Handled = true;
				}
			}

			return p;
		}
	}
}
