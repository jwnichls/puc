using System;

using PUC;
using PUC.Types;


namespace PUC.UIGeneration.ListRepresentation
{
	/// <summary>
	/// This type of ListNode represents a sub-list of items that are 
	/// linked with the value of a particular variable.  This is 
	/// typically used when a mutually exclusive situation is found 
	/// in the specification.
	/// </summary>
	public class StateValueListNode : ListNode
	{
		/*
		 * Member Variables
		 */

		protected ApplianceState _state;
		protected object		 _value;


		/*
		 * Constructor
		 */

		public StateValueListNode( ApplianceState state, object val )
		{
			_state = state;
			_value = val;
		}


		/*
		 * Properties
		 */

		public ApplianceState State
		{
			get
			{
				return _state;
			}
			set
			{
				_state = value;
			}
		}

		public object Value
		{
			get
			{
				return _value;
			}
			set
			{
				_value = value;
			}
		}
	}
}
