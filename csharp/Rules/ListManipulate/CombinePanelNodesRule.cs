using System;
using System.Collections;

using PhoneControls;

using PUC;
using PUC.CIO;
using PUC.Rules;
using PUC.Rules.BuildList;
using PUC.UIGeneration;
using PUC.UIGeneration.ListRepresentation;


namespace PUC.Rules.ListManipulate
{
	/// <summary>
	/// The base class f/or all rules used during the SpecChange
	/// rule phase.
	/// </summary>
	public class CombinePanelNodesRule: ListManipulateRule
	{
		/*
		 * Member Variables
		 */

		/*
		 * Constructor
		 */

		/*
		 * Process Rule Method
		 */

		/// <summary>
		/// This method checks if for a pattern that looks like
		/// ListNode->ListNode(one child)->ListNode and removes the 
		/// intermediate ListNode.
		/// </summary>
		public override bool Process( ListItemNode node, UIGenerator ui ) 
		{
			// promotion rules can't work on the root node
			bool ableToPromote = node.Parent != null;

			if ( node is ListNode )
			{
				ListNode listNode = (ListNode)node;
				Hashtable panelNodes = new Hashtable( listNode.Items.Count );

				IEnumerator e = listNode.Items.GetEnumerator();
				while( e.MoveNext() )
				{
					if ( e.Current is PanelListNode )
					{
						//
						// ASSUMPTION: All items in a panel node share 
						// the same parent group
						//
						GroupDecision d = 
							(GroupDecision)((ListItemNode)((PanelListNode)e.Current).Items[ 0 ]).Decorations[ GroupDecision.DECISION_KEY ];
						if ( d != null )
						{
							ArrayList list = (ArrayList)panelNodes[ d.Group.Parent ];
							
							if ( list == null )
								list = new ArrayList( listNode.Items.Count );

							list.Add( e.Current );

							panelNodes[ d.Group.Parent ] = list;
						}
					}
				}

				e = panelNodes.Keys.GetEnumerator();
				while ( e.MoveNext() )
				{
					GroupNode parent = (GroupNode)e.Current;

					if ( parent.Labels == null )
						// has no labels, so abort for this group
						continue;

					// create a new PanelNode and move all items into this node
					PanelListNode newPanel = new PanelListNode( parent.Labels );
					ArrayList list = (ArrayList)panelNodes[ parent ];

					if ( list.Count <= 1 )
						continue;

					IEnumerator panels = list.GetEnumerator();
					while( panels.MoveNext() )
					{
						PanelListNode existPanel = (PanelListNode)panels.Current;
						IEnumerator items = existPanel.Items.GetEnumerator();
						while( items.MoveNext() )
							newPanel.Add( (ListItemNode)items.Current );

						listNode.Remove( existPanel );
					}

					listNode.Add( newPanel );
				}
			}
			
			return false;
		}
	}
}
