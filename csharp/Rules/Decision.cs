using System;
using System.Collections;

namespace PUC.Rules
{
	/// <summary>
	/// A Decision is the result of the recognition and 
	/// processing of a Rule.  As rules are applied, the 
	/// affected object are "decorated" with Decision 
	/// objects.  The purpose of these objects is two-fold:
	/// 
	/// 1) To allow downstream rules to take advantage of
	///    the work of previous rules.
	///    
	/// 2) To provide a log of the computational process 
	///    that has been applied.  This log might later 
	///    be used to re-think rules that have already 
	///    been applied, or to ensure consistency of interfaces
	///    generated in the future.
	///    
	/// Decisions made be made in terms of previous decisions.
	/// For this reason, the decision constructor may take 
	/// another decision as a parameter, to allow algorithms 
	/// to "walk" the decision process.
	/// 
	/// Each rule should have one or more sub-classes of a 
	/// Decision class to represent the result of its 
	/// computation.
	/// </summary>
	public abstract class Decision
	{
		/*
		 * Member Variables
		 */

		protected Decision _rootDecision;
		protected bool	   _handled;

		// protected ArrayList _childDecisions;


		/*
		 * Constructor
		 */

		public Decision()
			: this( (Decision)null )
		{
		}

		public Decision( Decision root )
		{
			_rootDecision = root;
			_handled = false;
		}


		/*
		 * Properties
		 */

		public Decision RootDecision
		{
			get
			{
				return _rootDecision;
			}
		}

		public bool Handled
		{
			get
			{
				return _handled;
			}

			set
			{
				_handled = value;
			}
		}
	}
}
