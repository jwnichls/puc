using System;
using System.Collections;

using PUC.Rules;
using PUC.Rules.SpecScan;
using PUC.UIGeneration;


namespace PUC.Rules.UnitScan
{
	/// <summary>
	/// This is the implementation of the rule phase in which
	/// all unclaimed areas of the group tree are traversed 
	/// (unclaimed areas are those not assigned to units).  In
	/// this phase it is expected that CIOs will be assigned
	/// to object..
	/// </summary>
	public class UnitScanPhase : RulePhase
	{
		/*
		 * Member Variables
		 */

		protected ArrayList _rules;


		/*
		 * Constructor
		 */

		public UnitScanPhase()
		{
			_rules = new ArrayList();
		}


		/*
		 * Process Method
		 */

		public override object Process(object o, UIGenerator ui)
		{
			Appliance a = (Appliance)o;
			GroupNode group = a.GetRoot();

			phaseHelper( group, ui );

			return a;
		}

		protected void phaseHelper( GroupNode g, UIGenerator ui )
		{
			if ( g == null ||
				 g.Decorations[ UnitDecision.DECISION_KEY ] != null )
				return;

			invokeRules( g, ui );

			if ( g.IsObject() || g.Decorations[ UnitDecision.DECISION_KEY ] != null )
				return;

			for( int i = 0; i < ((BranchGroupNode)g).Children.Count; i++ )
			{
				phaseHelper( (GroupNode)((BranchGroupNode)g).Children[ i ], ui );
			}
		}

		protected void invokeRules( GroupNode g, UIGenerator ui )
		{
			for( int i = 0; i < _rules.Count &&
				            g.Decorations[ UnitDecision.DECISION_KEY ] == null; i++ )
				((UnitScanRule)_rules[ i ]).Process( g, ui );
		}


		/*
		 * Access to Rules
		 */

		public void AddRule( UnitScanRule rule )
		{
			_rules.Add( rule );
		}

		public IEnumerator GetRules()
		{
			return _rules.GetEnumerator();
		}
	}
}
