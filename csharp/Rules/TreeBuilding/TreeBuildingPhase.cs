using System;
using System.Collections;

using PUC.Rules;
using PUC.UIGeneration;


namespace PUC.Rules.TreeBuilding
{
	/// <summary>
	/// This is the implementation of the rule phase in which
	/// the specification may be modified.  This phase takes 
	/// the appliance structure as input, which includes the
	/// specification tree as well as some other extracted data
	/// from the parsing process (e.g., depended object counts)
	/// </summary>
	public class TreeBuildingPhase : RulePhase
	{
		/*
		 * Member Variables
		 */

		protected ArrayList _rules;


		/*
		 * Constructor
		 */

		public TreeBuildingPhase()
		{
			_rules = new ArrayList();
		}


		/*
		 * Process Method
		 */

		public override object Process(object o, UIGenerator ui)
		{
			Appliance a = (Appliance)o;

			PanelNode panel = (PanelNode)ui.InterfaceRoot;
			GroupNode group = a.GetRoot();

			phaseHelper( group, panel );

			ui.InterfaceRoot = recoverRoot( panel );

			return ui.InterfaceRoot;
		}

		protected void phaseHelper( GroupNode g, PanelNode p )
		{
			PanelNode panel = p;

			if ( g == null )
				return;

			panel = invokeRules( g, panel );

			if ( g.IsObject() )
				return;

			BranchGroupNode bg = (BranchGroupNode)g;

			for( int i = 0; i < bg.Children.Count; i++ )
			{
				phaseHelper( (GroupNode)bg.Children[ i ], panel );
			}
		}

		protected PanelNode invokeRules( GroupNode g, PanelNode p )
		{
			PanelNode output = p;

			for( int i = 0; i < _rules.Count; i++ )
				output = ((TreeBuildingRule)_rules[ i ]).Process( g, output );

			return output;
		}

		protected InterfaceNode recoverRoot( InterfaceNode currentRoot )
		{
			while( currentRoot.GetParent() != null )
				currentRoot = currentRoot.GetParent();

			return currentRoot;
		}


		/*
		 * Access to Rules
		 */

		public void AddRule( TreeBuildingRule rule )
		{
			_rules.Add( rule );
		}

		public IEnumerator GetRules()
		{
			return _rules.GetEnumerator();
		}
	}
}
