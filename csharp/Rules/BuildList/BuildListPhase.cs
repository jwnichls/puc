using System;
using System.Collections;

using PhoneControls;

using PUC;
using PUC.CIO;
using PUC.Rules;
using PUC.UIGeneration;
using PUC.UIGeneration.ListRepresentation;


namespace PUC.Rules.BuildList
{
	/// <summary>
	/// This is the implementation of the rule phase for the
	/// phone interface generator which creates a list-based
	/// interface.
	/// </summary>
	public class BuildListPhase : RulePhase
	{
		/*
		 * Member Variables
		 */

		protected ArrayList _rules;


		/*
		 * Constructor
		 */

		public BuildListPhase()
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

			ListNode root = ui.ListRoot;

			processHelper( a.GetRoot(), ui, root );
		
			return root;
		}

		protected void processHelper( GroupNode g, UIGenerator ui, ListNode node )
		{
			if ( g == null )
				return;

			node = invokeRules( g, ui, node );
		
			if ( g is ObjectGroupNode )
				return;
			
			BranchGroupNode bg = (BranchGroupNode)g;
			for( int i = 0; i < bg.Count; i++ )
				processHelper( (GroupNode)bg.Children[ i ], ui, node );
		}

		protected ListNode invokeRules( GroupNode g, UIGenerator ui, ListNode node )
		{
			IEnumerator e = _rules.GetEnumerator();
			while( e.MoveNext() )
			{
				node = ((BuildListRule)e.Current).Process( g, node );
			}

			return node;
		}


		/*
		 * Access to Rules
		 */

		public void AddRule( BuildListRule rule )
		{
			_rules.Add( rule );
		}

		public IEnumerator GetRules()
		{
			return _rules.GetEnumerator();
		}
	}
}
