using System;
using System.Collections;

using PUC;
using PUC.Rules;
using PUC.UIGeneration;


namespace PUC.Rules.TreeTraversal
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
	public class TreeTraversalPhase : RulePhase
	{
		/*
		 * Member Variables
		 */

		protected ArrayList _rules;


		/*
		 * Constructor
		 */

		public TreeTraversalPhase()
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
			InterfaceNode root = (InterfaceNode)o;

			processRules( root, ui );

			return root;
		}

		protected void processRules( InterfaceNode node, UIGenerator ui )
		{
			IEnumerator rule = _rules.GetEnumerator();
			while( rule.MoveNext() )
				((TreeTraversalRule)rule.Current).Process( node, ui );

			if ( node is MultiplePanelNode )
			{
				IEnumerator child = ((MultiplePanelNode)node).Panels.GetEnumerator();
				while( child.MoveNext() )
					processRules( (InterfaceNode)child.Current, ui );
			}
		}

		/*
		 * Access to Rules
		 */

		public void AddRule( TreeTraversalRule rule )
		{
			_rules.Add( rule );
		}

		public IEnumerator GetRules()
		{
			return _rules.GetEnumerator();
		}
	}
}
