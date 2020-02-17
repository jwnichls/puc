using System;
using System.Collections;

using PUC;
using PUC.Rules;
using PUC.Rules.SpecScan;
using PUC.Types;
using PUC.UIGeneration;

namespace PUC.Rules.GroupScan
{
	/// <summary>
	/// The power panel rule looks for situations in the group tree 
	/// where all the other states depend on a particular value of 
	/// one state, and that one state has a boolean type.  This closely
	/// approximates that state to being a power state.  This intended
	/// to cause an organization in which there is a power button on one
	/// panel, and all of the controls and another power button on an 
	/// overlapping panel.
	/// </summary>
	public class PowerPanelRule : GroupScanRule
	{
		/*
		 * Process Method
		 */

		public override void Process( GroupNode g, UIGenerator ui )
		{
			// look for MutualExclusionDecisions

			object o = g.Decorations[ MutualExclusionDecision.DECISION_KEY ];

			if ( o != null && o is MutualExclusionDecision )
			{
				MutualExclusionDecision d = (MutualExclusionDecision)o;

				if ( !d.Handled &&
					 d.State.Type.ValueSpace is BooleanSpace &&
					 ((ArrayList)d.ChildSets[ 0 ]).Count == 0 )
				{
					// we need a pointer to the GroupNode that contains the state
					// variable that we are looking at (because tree manipulations
					// will need to use it)

					BranchGroupNode bg = (BranchGroupNode)g;
					ObjectGroupNode stateGroup = null;

					IEnumerator e = bg.Children.GetEnumerator();
					while( e.MoveNext() )
						if ( ((GroupNode)e.Current).IsObject() && 
							((ObjectGroupNode)e.Current).Object == d.State )
						{
							stateGroup = (ObjectGroupNode)e.Current;
							break;
						}

					// now we modify the tree to incluce a power panel

					BranchGroupNode newG = new BranchGroupNode();

					newG.Parent = bg;
					newG.Children = bg.Children;

					bg.Children = new ArrayList();

					// make two groups under the common group
					bg.Children.Add( stateGroup );
					bg.Children.Add( newG );

					d.State.InternalController = true;

					e = newG.Children.GetEnumerator();
					while( e.MoveNext() )
						((GroupNode)e.Current).Parent = newG;

					ArrayList n = (ArrayList)d.DependencySets[ 0 ];
					ArrayList i = (ArrayList)d.DependencySets[ 1 ];

					EqualsDependency equalDep = (EqualsDependency)i[ 0 ];
					if ( (bool)equalDep.Value )
						n.Add( new EqualsDependency( equalDep.State, "false" ) );
					else 
						n.Add( new EqualsDependency( equalDep.State, "true" ) );

					OrganizationDecision newDecision = new InternalOverlapOrgDecision( d, d.State, bg.Children, d.DependencySets );
					g.Decorations.Add( OrganizationDecision.DECISION_KEY, newDecision );

					// make sure that other rules don't try to reorganize this node
					d.Handled = true;
				}
			}
		}
	}
}
