using System;
using System.Collections;

using PhoneControls;

using PUC;
using PUC.CIO;
using PUC.Rules;
using PUC.Rules.SpecScan;
using PUC.UIGeneration.ListRepresentation;


namespace PUC.Rules.BuildList
{
	/// <summary>
	/// Summary description for PutAllObjectsInListRule.
	/// </summary>
	public class NamedGroupSubListRule : BuildListRule
	{
		/*
		 * Process Method
		 */

		public override ListNode Process( GroupNode group, ListNode list )
		{
			if ( group.Decorations[ MutualExclusionDecision.DECISION_KEY ] != null )
			{
				MutualExclusionDecision d = 
					(MutualExclusionDecision)group.Decorations[ MutualExclusionDecision.DECISION_KEY ];

				if ( d.Handled ) return list;
			}

			if ( group.Decorations[ ListNodeDecision.DECISION_KEY ] != null )
				return list;
			
			if ( group.Labels != null )
			{
				LabeledListNode item = new LabeledListNode( group.Labels );
				item.Group = group;
				
				list.Add( item );

				group.Decorations.Add( ListNodeDecision.DECISION_KEY, 
									   new ListNodeDecision( item ) );

				return item;
			}

			return list;
		}
	}
}
