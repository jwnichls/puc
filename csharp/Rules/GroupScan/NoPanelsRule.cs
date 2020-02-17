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
	/// This rule looks for conditions where adding structure based
	/// upon mutual exclusion findings will not be useful, and marks
	/// those MutualExclusionDecision objects as handled to prevent 
	/// other structure rules from being applied.
	/// </summary>
	public class NoPanelsRule : GroupScanRule
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

				if ( d.Handled ) return;

				int count = 0;

				for( int i = 1; i < d.ChildSets.Count; i++ ) 
					count += ((ArrayList)d.ChildSets[ i ]).Count;

				// if there is only one item or one set of items affected by 
				// this mutual exclusion, then mark it as handled to prevent
				// future rules from adding structure here
				if ( count < 2 || d.ChildSets.Count <= 2 ) 
					d.Handled = true;
			}
		}
	}
}
