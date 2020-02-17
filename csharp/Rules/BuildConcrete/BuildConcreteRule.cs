using System;

using PUC;
using PUC.Rules;
using PUC.UIGeneration;


namespace PUC.Rules.BuildConcrete
{
	/// <summary>
	/// The base class for all rules used during the SpecChange
	/// rule phase.
	/// </summary>
	public abstract class BuildConcreteRule : Rule
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

		public abstract void Process( InterfaceNode node );
	}
}
