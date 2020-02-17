using System;
using System.Collections;

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
	public class LabeledTwoCompRowRule : TreeBuildingRule
	{
		/*
		 * Process Method
		 */
		public override PanelNode Process(GroupNode g, PanelNode p)
		{
			if ( g is BranchGroupNode )
			{
				BranchGroupNode bg = (BranchGroupNode)g;

				if ( bg.Children.Count == 2 && 
					 !g.ContainsGroups() )
				{
					IEnumerator child = bg.Children.GetEnumerator();
					while( child.MoveNext() )
						if ( ((ObjectGroupNode)child.Current).Decorations[ UnitDecision.DECISION_KEY ] == null ||
							 ((UnitDecision)((ObjectGroupNode)child.Current).Decorations[ UnitDecision.DECISION_KEY ]).Handled ||
							 ((UnitDecision)((ObjectGroupNode)child.Current).Decorations[ UnitDecision.DECISION_KEY ]).CIO.HasLabel() )
							return p;

					LabelCIO labelCIO = null;
					if ( g.Labels != null )
						labelCIO = new LabelCIO( g.Labels );

					UnitDecision d = 
						(UnitDecision)((ObjectGroupNode)bg.Children[ 0 ]).Decorations[ UnitDecision.DECISION_KEY ];
					ConcreteInteractionObject cio1 = d.CIO;
					d.Handled = true;

					d = (UnitDecision)((ObjectGroupNode)bg.Children[ 1 ]).Decorations[ UnitDecision.DECISION_KEY ];
					ConcreteInteractionObject cio2 = d.CIO;
					d.Handled = true;

					LabeledTwoCompRow r = new LabeledTwoCompRow( g, p, labelCIO, cio1, cio2 );

					p.AddRow( r );
				}
			}

			return p;
		}
	}
}
