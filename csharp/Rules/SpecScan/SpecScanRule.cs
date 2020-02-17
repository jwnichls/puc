using System;

using PUC.Rules;
using PUC.UIGeneration;


namespace PUC.Rules.SpecScan
{
	/// <summary>
	/// The base class for all rules used during the SpecChange
	/// rule phase.
	/// </summary>
	public abstract class SpecScanRule : Rule
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

		public abstract void Process( Appliance a );
	}
}
