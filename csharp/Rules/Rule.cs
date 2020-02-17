using System;

namespace PUC.Rules
{
	/// <summary>
	/// Rule objects encapsulate the recognition and computation 
	/// necessary to find where a rule should be applied and to 
	/// apply it.  The application of a rule should be represented
	/// in an appropriate decision object.  This allows rules to 
	/// build on the work of previous rules, and may eventually 
	/// allow the system to rollback the application of certain 
	/// rules if an exception is uncovered.
	/// 
	/// Currently this base class does not perform any function.  
	/// It exists to semantically group all future rules, and may
	/// someday include functions that are important to all Rules.
	/// 
	/// Sub-classes of Rule should be created for each RulePhase
	/// (see RulePhase.cs) so that it is easy to identify rules 
	/// that belong to particular phase.
	/// </summary>
	public abstract class Rule
	{
	}
}
