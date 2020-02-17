using System;
using System.Collections;

using PhoneControls;

using PUC;
using PUC.CIO;
using PUC.Communication;
using PUC.Rules;
using PUC.Rules.BuildList;
using PUC.Types;
using PUC.UIGeneration;
using PUC.UIGeneration.ListRepresentation;


namespace PUC.Rules.BuildConcreteList
{
	/// <summary>
	/// Summary description for PutAllObjectsInListRule.
	/// </summary>
	public class MakePanelItemRule : BuildConcreteListRule
	{
		/*
		 * Process Method
		 */

		public override ConcreteInteractionObject Process( ListItemNode node, 
														   ConcreteInteractionObject cio,
														   UIGenerator ui )
		{
			if ( node.Decorations[ ItemDecision.DECISION_KEY ] == null &&
				node is PanelListNode && 
				cio is PhoneListViewCIO )
			{
				// the item node represents an appliance object and it will be
				// contained within a list
				PanelListNode newPanel = (PanelListNode)node;
				PhoneListViewCIO list = (PhoneListViewCIO)cio;
				ListViewItemCIO listItem = null; 
				ConcreteInteractionObject panel = null;

				if ( ((ListNode)node).Items.Count == 1 )
				{
					ConcreteInteractionObject statecio = ((CIOListItemNode)newPanel.Items[ 0 ]).CIO;
					if ( statecio is StateLinkedCIO )
					{
						StateLinkedCIO childCIO = 
							(StateLinkedCIO)statecio;
						listItem = new SingleItemPanelListViewItemCIO( 
							list, 
							(ApplianceState)childCIO.GetApplObj() );
						panel = ((SingleItemPanelListViewItemCIO)listItem).Panel;
					}
					else
					{
						listItem = new PanelListViewItemCIO( list, newPanel.Labels );
						panel = ((PanelListViewItemCIO)listItem).Panel;
					}
				}
				else
				{
					listItem = new PanelListViewItemCIO( list, newPanel.Labels );
					panel = ((PanelListViewItemCIO)listItem).Panel;
				}

				list.AddItem( listItem );
				newPanel.Decorations[ ItemDecision.DECISION_KEY ] = 
					new ListItemDecision( listItem );

				return panel;
			}

			return cio;
		}
	}
}
