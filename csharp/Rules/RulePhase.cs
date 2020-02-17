using System;

using PUC.UIGeneration;


namespace PUC.Rules
{
	/// <summary>
	/// A RulePhase is a grouping of rules that all operate
	/// on the same input.  Each RulePhase contains an ordered set 
	/// of rules.  The implementation of RulePhase should be 
	/// accompanied by a set of Rule sub-classes that are designed
	/// for use with the RulePhase.
	/// </summary>
	public abstract class RulePhase
	{
		/*
		 * Abstract Methods
		 */

		/// <summary>
		/// This method applies all of the rules to the input and 
		/// returns the output of their application.  In cases where
		/// the input is being modified internally, then the returned
		/// value should be the same as the input value.
		/// </summary>
		/// <param name="o">the input to this rule phase</param>
		/// <returns>the output of the rule phase</returns>
		public abstract object Process( object o, UIGenerator ui );
	}
}
