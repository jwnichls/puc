using System;

using PUC;
using PUC.Rules;
using PUC.UIGeneration;


namespace PUC.Rules.TreeBuilding
{
	/// <summary>
	/// The base class for all rules used during the SpecChange
	/// rule phase.
	/// </summary>
	public abstract class TreeBuildingRule : Rule
	{
		/*
		 * Member Variables
		 */

		/*
		 * Constructor
		 */

		/*
		 * Process Rule Method
		 */

		public abstract PanelNode Process( GroupNode g, PanelNode p );
	}
}
