using System;
using System.Collections;

using PUC.Rules;
using PUC.UIGeneration;


namespace PUC.Rules.BuildConcrete
{
	/// <summary>
	/// This is the implementation of the rule phase in which
	/// the specification may be modified.  This phase takes 
	/// the appliance structure as input, which includes the
	/// specification tree as well as some other extracted data
	/// from the parsing process (e.g., depended object counts)
	/// </summary>
	public class BuildConcretePhase : RulePhase
	{
		/*
		 * Member Variables
		 */

		protected ArrayList _rules;


		/*
		 * Constructor
		 */

		public BuildConcretePhase()
		{
			_rules = new ArrayList();
		}


		/*
		 * Process Method
		 */

		public override object Process(object o, UIGenerator ui)
		{
			InterfaceNode root = (InterfaceNode)o;

			root.SetSize( ui.Size.Width, ui.Size.Height );

			// add components also calculates minimum and preferred sizes
			root.AddComponents( ui.Panel, ui.LayoutVars );
			root.DoLayout( ui.LayoutVars );

			// Globals.AddLogLine( _interfaceRoot.ToString() );
			object[] items = new object[ 2 ];
			items[ 0 ] = root;
			items[ 1 ] = ui.LayoutVars.LayoutProblems;

			return items;
		}


		/*
		 * Access to Rules
		 */

		public void AddRule( BuildConcreteRule rule )
		{
			_rules.Add( rule );
		}

		public IEnumerator GetRules()
		{
			return _rules.GetEnumerator();
		}
	}
}
