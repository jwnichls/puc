using System;
using System.Collections;
using System.Windows.Forms;

using PhoneControls;

using PUC;
using PUC.CIO;
using PUC.Communication;
using PUC.Rules;
using PUC.Types;
using PUC.UIGeneration;
using PUC.UIGeneration.ListRepresentation;


namespace PUC.Rules.BuildConcreteList
{
	/// <summary>
	/// Summary description for PutAllObjectsInListRule.
	/// </summary>
	public class MakeValueSubListRule : BuildConcreteListRule
	{
		/*
		 * Process Method
		 */

		public override ConcreteInteractionObject Process( ListItemNode node, 
														   ConcreteInteractionObject cio,
														   UIGenerator ui )
		{
			if ( node.Decorations[ ItemDecision.DECISION_KEY ] == null &&
				 node is StateValueListNode && 
				 cio is PhoneListViewCIO )
			{
				// the item node represents an appliance object and it will be
				// contained within a list
				StateValueListNode newList = (StateValueListNode)node;
				PhoneListViewCIO list = (PhoneListViewCIO)cio;
	
				PhoneListViewCIO subList = new PhoneListViewCIO();

				LabelDictionary labels = null;

				if ( newList.State.Type.ValueLabels != null )
				{
					labels = 
						(LabelDictionary)newList.State.Type.ValueLabels[ newList.Value ];
				}

				if ( labels == null )
				{
					labels = new LabelDictionary();
					labels.AddLabel( new StringValue( newList.Value.ToString() ) );
				}

				IPhoneListViewItem listItem =
					new SubListViewItemCIO( list, subList, labels ); 

				ItemActivationListener l = new ItemActivationListener( newList.State,
																	   newList.Value );

				listItem.ItemActivated += new EventHandler(l.itemActivated);

				list.AddItem( listItem );
				newList.Decorations[ ItemDecision.DECISION_KEY ] = 
					new ListItemDecision( listItem );

				return subList;
			}

			return cio;
		}
	}

	public class ItemActivationListener 
	{
		/*
		 * Member Variables
		 */

		protected ApplianceState _state;
		protected object		 _value;


		/*
		 * Constructor
		 */

		public ItemActivationListener( ApplianceState state, object val )
		{
			_state     = state;
			_value     = val;
		}


		/*
		 * Handler
		 */

		public void itemActivated( object source, EventArgs evt )
		{
			_state.RequestChange( _value );
		}
	}
}
