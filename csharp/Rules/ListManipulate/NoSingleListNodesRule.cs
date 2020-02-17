using System;

using PhoneControls;

using PUC;
using PUC.CIO;
using PUC.Rules;
using PUC.UIGeneration;
using PUC.UIGeneration.ListRepresentation;


namespace PUC.Rules.ListManipulate
{
	/// <summary>
	/// The base class f/or all rules used during the SpecChange
	/// rule phase.
	/// </summary>
	public class NoSingleListNodesRule: ListManipulateRule
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
			// special case for root node
			if ( node.Parent == null )
			{
				// this happens only if the following pattern is seen:
				// ListNode(root-one child)->List Node
				// in this case, the child node is promoted to the root
				if ( node is ListNode )
				{
					ListNode listNode = (ListNode)node;

					if ( listNode.Items.Count == 1 &&
						listNode.Items[ 0 ] is ListNode )
					{
						ui.ListRoot = (ListNode)listNode.Items[ 0 ];
						ui.ListRoot.Parent = null;

						return true;
					}
				}

				return false;
			}

			if ( node is ListNode )
			{
				ListNode listNode = (ListNode)node;

				if ( listNode.Items.Count == 1 &&
					 listNode.Items[ 0 ] is ListNode )
				{
					ListNode newItem = (ListNode)listNode.Items[ 0 ];
					ListNode parent = listNode.Parent;

					int idx = parent.Items.IndexOf( listNode );
					parent.Items[ idx ] = newItem;
					newItem.Parent = parent;

					return true;
				}
			}
			
			return false;
		}
	}
}
