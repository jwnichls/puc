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
	public class AssignListNodeRule : BuildListRule
	{
		/*
		 * Process Method
		 */

		public override ListNode Process( GroupNode group, ListNode list )
		{
			if ( group.Decorations[ ListNodeDecision.DECISION_KEY ] != null )
			{
				ListNodeDecision d = 
					(ListNodeDecision)group.Decorations[ ListNodeDecision.DECISION_KEY ];

				return d.Node;
			}

			return list;
		}
	}
}
