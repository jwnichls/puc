using System;

using PUC;
using PUC.Rules;
using PUC.UIGeneration;

namespace PUC.Rules.FixLayout
{
	/// <summary>
	/// This implements a set of rules that receive a GroupNode as
	/// input.  These rules may alter the group tree, but they must
	/// not move the group node that they are passed.  This ensures 
	/// that the phase will properly process all nodes of the group 
	/// tree.
	/// </summary>
	public abstract class FixLayoutRule : Rule
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

		public abstract InterfaceNode Process( LayoutProblem problem, InterfaceNode node, UIGenerator ui );
	}
}
