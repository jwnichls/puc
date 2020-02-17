using System;
using System.Collections;

using PhoneControls;

using PUC;
using PUC.CIO;
using PUC.Communication;
using PUC.Rules;
using PUC.Rules.BuildList;
using PUC.UIGeneration;
using PUC.UIGeneration.ListRepresentation;


namespace PUC.Rules.BuildConcreteList
{
	/// <summary>
	/// Summary description for PutAllObjectsInListRule.
	/// </summary>
	public class MakeListItemRule : BuildConcreteListRule
	{
		/*
		 * Process Method
		 */

		public override ConcreteInteractionObject Process( ListItemNode node, 
														   ConcreteInteractionObject cio,
														   UIGenerator ui )
		{
			if ( node.Decorations[ ItemDecision.DECISION_KEY ] == null &&
				 node is CIOListItemNode && 
				 !((PanelDecision)node.Decorations[ PanelDecision.DECISION_KEY ]).IsPanel &&
				 cio is PhoneListViewCIO )
			{
				// the item node represents an appliance object and it will be
				// contained within a list
				CIOListItemNode item = (CIOListItemNode)node;
				PhoneListViewCIO list = (PhoneListViewCIO)cio;
	
				IPhoneListViewItem listItem = (IPhoneListViewItem)item.CIO;

				list.AddItem( listItem );
				item.Decorations[ ItemDecision.DECISION_KEY ] = 
					new ListItemDecision( listItem );
			}

			return cio;
		}
	}
}
