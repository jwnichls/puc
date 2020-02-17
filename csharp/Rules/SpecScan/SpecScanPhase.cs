using System;
using System.Collections;

using PUC.Rules;
using PUC.UIGeneration;


namespace PUC.Rules.SpecScan
{
	/// <summary>
	/// This is the implementation of the rule phase in which
	/// patterns are recognized in the raw specification which 
	/// may have meaning for the layout of the interface..  This 
	/// phase takes the appliance structure as input, which 
	/// includes the specification tree as well as some other 
	/// extracted data from the parsing process (e.g., depended 
	/// object counts)
	/// </summary>
	public class SpecScanPhase : RulePhase
	{
		/*
		 * Member Variables
		 */

		protected ArrayList _rules;


		/*
		 * Constructor
		 */

		public SpecScanPhase()
		{
			_rules = new ArrayList();
		}


		/*
		 * Process Method
		 */

		public override object Process(object o, UIGenerator ui )
		{
			Appliance a = (Appliance)o;

			IEnumerator e = _rules.GetEnumerator();
			while( e.MoveNext() )
			{
				((SpecScanRule)e.Current).Process( a );
			}

			return a;
		}


		/*
		 * Access to Rules
		 */

		public void AddRule( SpecScanRule rule )
		{
			_rules.Add( rule );
		}

		public IEnumerator GetRules()
		{
			return _rules.GetEnumerator();
		}
	}
}
