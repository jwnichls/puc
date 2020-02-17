using System;

using PUC;
using PUC.Rules;
using PUC.UIGeneration;


namespace PUC.Rules.UnitScan
{
	/// <summary>
	/// The base class for all rules used during the SpecChange
	/// rule phase.
	/// </summary>
	public abstract class UnitScanRule : Rule
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

		public abstract void Process( GroupNode g, UIGenerator ui );
	}
}
