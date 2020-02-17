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
	/// This rule looks for conditions where using a tabbed panel
	/// would make sense, based upon the findings of the 
	/// MutualExclusionRule.
	/// </summary>
	public class OverlappingPanelsRule : GroupScanRule
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

				if ( !d.Handled )
				{
					// this will never occur on an ObjectGroupNode
					BranchGroupNode bg = (BranchGroupNode)g;
					
					// we need a pointer to the GroupNode that contains the state
					// variable that we are looking at (because tree manipulations
					// will need to use it)

					ObjectGroupNode stateGroup = null;

					IEnumerator e = bg.Children.GetEnumerator();
					while( e.MoveNext() )
						if ( ((GroupNode)e.Current).IsObject() && 
							((ObjectGroupNode)e.Current).Object == d.State )
						{
							stateGroup = (ObjectGroupNode)e.Current;
							break;
						}

					// re-order the tree

					ArrayList childOrder = new ArrayList();

					bg.Children.Remove( stateGroup );

					BranchGroupNode midG = new BranchGroupNode();

					midG.Children = bg.Children;
					e = midG.Children.GetEnumerator();
					while( e.MoveNext() )
						((GroupNode)e.Current).Parent = midG;

					bg.Children = new ArrayList();

					ArrayList dset = (ArrayList)d.ChildSets[ 0 ];
					if ( dset.Count > 0 )
					{
						e = dset.GetEnumerator();
						while( e.MoveNext() )
						{
							GroupNode c = (GroupNode)e.Current;

							c.Parent = bg;

							midG.Children.Remove( c );
							bg.Children.Add( c );
						}
					}

					bg.Children.Add( stateGroup );
					bg.Children.Add( midG );
					midG.Parent = bg;
					stateGroup.Parent = bg;

					for( int i = 1; i < d.ChildSets.Count; i++ ) 
					{
						dset = (ArrayList)d.ChildSets[ i ];

						if ( dset.Count > 1 )
						{
							BranchGroupNode newG = new BranchGroupNode();
							newG.Parent = midG;

							e = dset.GetEnumerator();
							while( e.MoveNext() )
							{
								GroupNode c = (GroupNode)e.Current;

								c.Parent = newG;

								midG.Children.Remove( c );
								newG.Children.Add( c );
							}

							childOrder.Insert( i-1, newG );
						}
						else if ( dset.Count == 0 )
						{
							BranchGroupNode newG = new BranchGroupNode();

							newG.Parent = midG;
							newG.Children = new ArrayList();

							midG.Children.Add( newG );

							childOrder.Insert( i-1, newG );
						}
						else
						{
							childOrder.Insert( i-1, dset[ 0 ] );
						}
					}

					d.DependencySets.RemoveAt( 0 );

					OrganizationDecision newDecision = new ExternalOverlapOrgDecision( d, d.State, childOrder, d.DependencySets );
					midG.Decorations.Add( OrganizationDecision.DECISION_KEY, newDecision );
					
					d.Handled = true;
				}
			}
		}
	}
}
