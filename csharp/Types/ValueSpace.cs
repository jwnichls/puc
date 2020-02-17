using System;

namespace PUC.Types
{
	/// <summary>
	/// Represents the space in which the value of appliance state exists.
	/// These objects have three functions: defining the values that can be 
	/// stored in a state variable, validating that a value is within the 
	/// acceptable range that can be stored in a variable, and storing the 
	/// value for a state variable.
	/// </summary>
	public abstract class ValueSpace : ICloneable
	{
		/*
		 * Constants
		 */
		/// <summary>
		/// Represents a boolean space.
		/// </summary>
		public const int BOOLEAN_SPACE     = 0;

		/// <summary>
		/// Represents an integer space.
		/// </summary>
		public const int INTEGER_SPACE     = 1;

		/// <summary>
		/// Represents a floating point space.
		/// </summary>
		public const int FLOATING_PT_SPACE = 2;

		/// <summary>
		/// Represents a fixed point space.
		/// </summary>
		public const int FIXED_PT_SPACE    = 3;
	
		/// <summary>
		/// Represents a string space.
		/// </summary>
		public const int STRING_SPACE      = 4;

		/// <summary>
		/// Represents an enumerated space.
		/// </summary>
		public const int ENUMERATED_SPACE  = 5;

		/// <summary>
		/// Represents a binary space.
		/// </summary>
		public const int BINARY_SPACE      = 6;

		/// <summary>
		/// Represents a list-selection space.
		/// </summary>
		public const int LIST_SELECTION_SPACE = 7;


		/* 
		 * Member Variables
		 */


		/*
		 * Member Methods
		 */
		/// <summary>
		/// A generic method for outputting ValueSpace-derived objects.
		/// </summary>
		/// <returns></returns>
		public override string ToString()
		{
			return Name;
		}


		/*
		 * Abstract Methods
		 */
		/// <summary>
		/// Makes a copy of this ValueSpace object.  Inherited from the
		/// ICloneable interface.
		/// </summary>
		/// <returns>a shallow copy of this ValueSpace object</returns>
		public abstract object Clone();

		/// <summary>
		/// Returns the string-based name of the ValueSpace.
		/// </summary>
		/// <returns>the name of the ValueSpace</returns>
		public abstract string Name
		{
			get;
		}

		/// <summary>
		/// Returns the integer constant describing the ValueSpace
		/// </summary>
		/// <returns>the integer constrant describing the ValueSpace</returns>
		public abstract int Space
		{
			get;
		}

		/// <summary>
		/// Validates the provided value to make sure that it can be assigned 
		/// to this ValueSpace.
		/// </summary>
		/// <param name="val">the value to validate</param>
		/// <returns>the value for the PUCValue to store, or null if not valid</returns>
		public abstract object Validate( object val );

		/// <summary>
		/// Compares the values within two value spaces, and returns a result 
		/// based on a comparison of the values stored within the space objects.  
		/// The function returns 0 if the values are equal, and non-zero if the 
		/// values are not equal.  If greater-than and less-than relations make 
		/// sense for the space (NumberSpaces), -1 is returned when space &lt; 
		/// this.value and 1 is returned when space &gt; this.value.
		/// 
		/// This function assumes that the ValueSpaces are equivalent.  The 
		/// results of this function are not meaningful otherwise, and an 
		/// exception may be thrown if the spaces are significantly different.
		/// </summary>
		/// <param name="space">a ValueSpace to compare to this one</param>
		/// <returns>an integer describing how the values of these spaces are related</returns>
		public abstract int CompareValues( object obj1, object obj2 );

		/// <summary>
		/// Creates the event handlers that update state variables when 
		/// constraints change.
		/// </summary>
		/// <param name="state">the state to be updated</param>
		public abstract void RegisterConstraints( ApplianceState state );

		/// <summary>
		/// Compares this ValueSpace to another in order to determine if
		/// the two spaces are identical.
		/// </summary>
		/// <param name="space">the space to compare to</param>
		/// <returns>whether the spaces are identical</returns>
		public abstract bool IsSameSpace( ValueSpace space );
	}


	/// <summary>
	/// A stub class inherited by all ValueSpaces that represent
	/// numeric values.
	/// </summary>
	public abstract class NumberSpace : ValueSpace
	{
	}
}
