using System;
using System.Collections;
using System.Windows.Forms;

using PhoneControls;

using PUC;
using PUC.CIO;
using PUC.Communication;
using PUC.Rules;
using PUC.UIGeneration;
using PUC.UIGeneration.ListRepresentation;


namespace PUC.Rules.BuildConcreteList
{
	/// <summary>
	/// Summary description for PutAllObjectsInListRule.
	/// </summary>
	public class MakeSubListRule : BuildConcreteListRule
	{
		/*
		 * Process Method
		 */

		public override ConcreteInteractionObject Process( ListItemNode node, 
														   ConcreteInteractionObject cio,
														   UIGenerator ui )
		{
			if ( node.Decorations[ ItemDecision.DECISION_KEY ] == null &&
				 node is LabeledListNode && 
				 cio is PhoneListViewCIO )
			{
				// the item node represents an appliance object and it will be
				// contained within a list
				LabeledListNode newList = (LabeledListNode)node;
				PhoneListViewCIO list = (PhoneListViewCIO)cio;
	
				PhoneListViewCIO subList = new PhoneListViewCIO();

				IPhoneListViewItem listItem =
					new SubListViewItemCIO( list, subList, newList.Labels ); 

				list.AddItem( listItem );
				newList.Decorations[ ItemDecision.DECISION_KEY ] = 
					new ListItemDecision( listItem );

				return subList;
			}

			return cio;
		}
	}
}
