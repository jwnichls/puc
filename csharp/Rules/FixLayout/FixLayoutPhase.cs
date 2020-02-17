using System;
using System.Collections;

using PUC;
using PUC.Rules;
using PUC.UIGeneration;


namespace PUC.Rules.FixLayout
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
	public class FixLayoutPhase : RulePhase
	{
		/*
		 * Member Variables
		 */

		protected ArrayList _rules;


		/*
		 * Constructor
		 */

		public FixLayoutPhase()
		{
			_rules = new ArrayList();
		}


		/*
		 * Process Method
		 */

		/// <summary>
		/// Input to this rule phase is an array that contains two items.  The
		/// first item is the current interface tree, and the second item is an
		/// array of LayoutProblem objects.  Rules of this phase attemp to fix 
		/// these problems.
		/// </summary>
		/// <param name="o">an array containg the interface tree and a list of layout problems</param>
		/// <param name="ui">a UIGenerator object that contains global variables for this process</param>
		/// <returns></returns>
		public override object Process(object o, UIGenerator ui )
		{
			object[] items = (object[])o;
			InterfaceNode root = (InterfaceNode)items[ 0 ];
			Hashtable problems = (Hashtable)((Hashtable)items[ 1 ]).Clone();

			IEnumerator problem = problems.Values.GetEnumerator();
			while( problem.MoveNext() )
			{
				IEnumerator rule = _rules.GetEnumerator();
				while( rule.MoveNext() )
				{
					root = ((FixLayoutRule)rule.Current).Process( (LayoutProblem)problem.Current, root, ui );
				}
			}
		
			ui.InterfaceRoot = root;
			return root;
		}


		/*
		 * Access to Rules
		 */

		public void AddRule( FixLayoutRule rule )
		{
			_rules.Add( rule );
		}

		public IEnumerator GetRules()
		{
			return _rules.GetEnumerator();
		}
	}
}
