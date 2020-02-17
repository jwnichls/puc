using System;
using PUC.Rules;

namespace PUC.Rules.SpecChange
{
	/// <summary>
	/// The base class for all rules used during the SpecChange
	/// rule phase.
	/// </summary>
	public abstract class SpecChangeRule : Rule
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
