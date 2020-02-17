using System;
using System.Collections;

using PUC;
using PUC.Rules;
using PUC.UIGeneration;


namespace PUC.Rules.GroupScan
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
	public class GroupScanPhase : RulePhase
	{
		/*
		 * Member Variables
		 */

		protected ArrayList _rules;


		/*
		 * Constructor
		 */

		public GroupScanPhase()
		{
			_rules = new ArrayList();
		}


		/*
		 * Process Method
		 */

		/// <summary>
		/// Input to this rule phase is an appliance object, but the input to
		/// the rules of this phase are GroupNode objects.  This phase iterates
		/// down the group tree depth first, feeding each rule the GroupNodes as
		/// it goes.
		/// </summary>
		/// <param name="o">an appliance object</param>
		/// <param name="ui">a UIGenerator object that contains global variables for this process</param>
		/// <returns></returns>
		public override object Process(object o, UIGenerator ui )
		{
			Appliance a = (Appliance)o;

			processHelper( a.GetRoot(), ui );
		
			return a;
		}

		protected void processHelper( GroupNode g, UIGenerator ui )
		{
			if ( g == null )
				return;

			invokeRules( g, ui );
		
			if ( g.IsObject() )
				return;
			
			BranchGroupNode bg = (BranchGroupNode)g;

			for( int i = 0; i < bg.Children.Count; i++ )
				processHelper( (GroupNode)bg.Children[ i ], ui );
		}

		protected void invokeRules( GroupNode g, UIGenerator ui )
		{
			IEnumerator e = _rules.GetEnumerator();
			while( e.MoveNext() )
			{
				((GroupScanRule)e.Current).Process( g, ui );
			}
		}


		/*
		 * Access to Rules
		 */

		public void AddRule( GroupScanRule rule )
		{
			_rules.Add( rule );
		}

		public IEnumerator GetRules()
		{
			return _rules.GetEnumerator();
		}
	}
}
