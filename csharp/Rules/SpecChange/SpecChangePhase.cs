using System;
using System.Collections;
using PUC.Rules;
using PUC.UIGeneration;

namespace PUC.Rules.SpecChange
{
	/// <summary>
	/// This is the implementation of the rule phase in which
	/// the specification may be modified.  This phase takes 
	/// the appliance structure as input, which includes the
	/// specification tree as well as some other extracted data
	/// from the parsing process (e.g., depended object counts)
	/// </summary>
	public class SpecChangePhase : RulePhase
	{
		/*
		 * Member Variables
		 */

		protected ArrayList _rules;


		/*
		 * Constructor
		 */

		public SpecChangePhase()
		{
			_rules = new ArrayList();
		}


		/*
		 * Process Method
		 */

		public override object Process(object o, UIGenerator ui)
		{
			Appliance a = (Appliance)o;

			IEnumerator e = _rules.GetEnumerator();
			while( e.MoveNext() )
			{
				((SpecChangeRule)e.Current).Process( a );
			}

			return a;
		}


		/*
		 * Access to Rules
		 */

		public void AddRule( SpecChangeRule rule )
		{
			_rules.Add( rule );
		}

		public IEnumerator GetRules()
		{
			return _rules.GetEnumerator();
		}
	}
}
