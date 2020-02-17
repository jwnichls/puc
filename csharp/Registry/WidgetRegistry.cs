using System;

namespace PUC.Registry
{
	/// <summary>
	/// Summary description for WidgetRegistry.
	/// </summary>
	public class WidgetRegistry
	{
		/*
		 * Member Variables
		 */
		protected string   _name;
		protected Decision _decisionTree;


		/*
		 * Constructor
		 */
		public WidgetRegistry( string name, Decision tree )
		{
			_name = name;
			_decisionTree = tree;
		}


		/*
		 * Member Methods
		 */
		public string GetName()
		{
			return _name;
		}

		public override string ToString()
		{
			return _decisionTree.ToString();
		}

		public PUC.CIO.ConcreteInteractionObject ChooseWidget( PUC.ApplianceObject ao )
		{
			return _decisionTree.ChooseWidget( ao );
		}

		public PUC.CIO.ConcreteInteractionObject ChooseWidget( PUC.GroupNode g )
		{
			return _decisionTree.ChooseWidget( g );
		}
	}
}
