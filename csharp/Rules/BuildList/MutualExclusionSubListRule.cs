using System;
using System.Collections;

using PhoneControls;

using PUC;
using PUC.Rules;
using PUC.Rules.SpecScan;
using PUC.Types;
using PUC.UIGeneration;
using PUC.UIGeneration.ListRepresentation;


namespace PUC.Rules.BuildList
{
	/// <summary>
	/// This rule looks for conditions where using a tabbed panel
	/// would make sense, based upon the findings of the 
	/// MutualExclusionRule.
	/// </summary>
	public class MutualExclusionSubListRule : BuildListRule
	{
		/*
		 * Process Method
		 */

		public override ListNode Process( GroupNode g, ListNode list )
		{
			// look for MutualExclusionDecisions

			object o = g.Decorations[ MutualExclusionDecision.DECISION_KEY ];

			if ( o != null && o is MutualExclusionDecision )
			{
				MutualExclusionDecision d = (MutualExclusionDecision)o;

				if ( !d.Handled && 
					d.State.Type.ValueSpace is EnumeratedSpace )
				{
					EnumeratedSpace espc = (EnumeratedSpace)d.State.Type.ValueSpace;

					if ( espc.GetItemCount() == ( d.DependencySets.Count - 1 ) )
					{
						for( int i = 1; i < d.DependencySets.Count; i++ )
						{
							ArrayList dep = (ArrayList)d.DependencySets[ i ];

							if ( dep.Count != 1 )
							{
								// Globals.AddLogLine("size! " + dep.Count );
								return list;
							}
							if ( ! ( dep[ 0 ] is EqualsDependency ) )
							{
								// Globals.AddLogLine("not equals!");
								return list;
							}
						}
					}
					else return list;

					// we need a pointer to the GroupNode that contains the state
					// variable that we are looking at (because tree manipulations
					// will need to use it)

					ObjectGroupNode stateGroup = null;

					BranchGroupNode bg = (BranchGroupNode)g;
					IEnumerator e = bg.Children.GetEnumerator();
					while( e.MoveNext() )
						if ( e.Current is ObjectGroupNode && 
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
							midG.Children.Add( newG );

							childOrder.Insert( i-1, newG );
						}
						else
						{
							childOrder.Insert( i-1, dset[ 0 ] );
						}
					}

					d.DependencySets.RemoveAt( 0 );

					// now create StateValueListNodes from the re-orged group tree
					// these nodes will be picked up by an additional rule later
					// in the generation process
					StateValueListNode newList = null;
					for( int i = 0; i < childOrder.Count; i++ )
					{
						GroupNode group = (GroupNode)childOrder[ i ];
						ArrayList aryDeps = (ArrayList)d.DependencySets[ i ];

						EqualsDependency eqDep = (EqualsDependency)aryDeps[ 0 ];

						newList = new StateValueListNode( d.State, eqDep.Value );
						list.Add( newList );

						group.Decorations.Add( ListNodeDecision.DECISION_KEY, new ListNodeDecision( newList, d ) );
					}
					
					d.Handled = true;
				}
			}

			return list;
		}
	}

	public class ListNodeDecision : Decision
	{
		/*
		 * Constants
		 */

		public const string DECISION_KEY = "listnode";


		/*
		 * Member Variables
		 */

		protected ListNode _listNode;


		/*
		 * Constructor
		 */

		public ListNodeDecision( ListNode listNode )
		{
			_listNode = listNode;
		}

		public ListNodeDecision( ListNode listNode, Decision baseDecision )
			: base( baseDecision )
		{
			_listNode = listNode;
		}


		/*
		 * Properties
		 */

		public ListNode Node
		{
			get
			{
				return _listNode;
			}
		}
	}
}
