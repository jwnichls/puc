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
	public class PUCValue : ICloneable
	{
		/* 
		 * Member Variables
		 */

		/// <summary>
		/// Indicates whether the value of the space is defined.
		/// </summary>
		protected bool       _definedValue = false;

		/// <summary>
		/// Stores the value of this object
		/// </summary>
		protected object     _value        = null;

		/// <summary>
		/// Stores the space that the above value must be in.
		/// </summary>
		protected ValueSpace _valueSpace   = null;


		/*
		 * Constructors
		 */

		/// <summary>
		/// Create a new PUCValue with an undefined value in the given space
		/// </summary>
		/// <param name="space">The space in which values of this PUCValue must exist</param>
		public PUCValue( ValueSpace space ) 
		{
			_valueSpace = space;
		}

		public PUCValue( object value, ValueSpace space )
		{
			_valueSpace = space;

			this.Value = value;
		}

		/*
		 * Member Methods
		 */

		/// <summary>
		/// Property that allows access to this PUCValue's ValueSpace object.
		/// </summary>
		public ValueSpace Space
		{
			get
			{
				return _valueSpace;
			}
		}

		/// <summary>
		/// A generic method for outputting PUCValue objects.
		/// </summary>
		/// <returns></returns>
		public override string ToString()
		{
			return Name + " = " + Value.ToString();
		}

		/// <summary>
		/// Determines whether the value of the space is defined.
		/// </summary>
		/// <returns>whether the value of the space is defined</returns>
		public bool Defined
		{
			get
			{
				return _definedValue;
			}
		}

		public void Undefine()
		{
			_definedValue = false;
		}


		/*
		 * Abstract Methods
		 */
		/// <summary>
		/// Makes a copy of this PUCValue object.  Inherited from the
		/// ICloneable interface.
		/// </summary>
		/// <returns>a shallow copy of this PUCValue object</returns>
		public object Clone()
		{
			PUCValue val = new PUCValue( this._valueSpace );
			val._value = this._value;
			val._definedValue = this._definedValue;

			return val;
		}

		/// <summary>
		/// Returns the hash code for this PUCValue object.  The code is 
		/// calculated by the PUCValue's ValueSpace object.
		/// </summary>
		/// <returns>hash code for this PUCValue</returns>
		public override int GetHashCode()
		{
			return _valueSpace.GetHashCode(this);
		}

		/// <summary>
		/// Determines whether two PUCValue's are equal.  This computation
		/// is done by this PUCValue's ValueSpace object.
		/// </summary>
		/// <param name="obj">A PUCValue object to check equality with</param>
		/// <returns>whether this PUCValue is equal to another</returns>
		public override bool Equals(object obj)
		{
			return _valueSpace.Equals( (PUCValue)obj, this );
		}

		/// <summary>
		/// Returns the string-based name of the ValueSpace.
		/// </summary>
		/// <returns>the name of the ValueSpace</returns>
		public string Name
		{
			get
			{
				return _valueSpace.Name;
			}
		}

		/// <summary>
		/// Returns the integer constant describing the ValueSpace
		/// </summary>
		/// <returns>the integer constrant describing the ValueSpace</returns>
		public int SpaceID
		{
			get
			{
				return _valueSpace.Space;
			}
		}

		/// <summary>
		/// Returns the value stored in this ValueSpace.  It will need to be 
		/// casted as appropriate to obtain the stored value.
		/// </summary>
		/// <returns>the value stored in this ValueSpace</returns>
		public object Value
		{
			get
			{
				return _value;
			}
			set
			{
				object obj = _valueSpace.Validate( value );
				if ( obj != null )
				{
					_value = obj;
					_definedValue = true;
				}
			}
		}

		/// <summary>
		/// Compares the values within two value spaces, and returns a result 
		/// based on a comparison of the values stored within the space objects.  
		/// The function returns 0 if the values are equal, and non-zero if the 
		/// values are not equal.  If greater-than and less-than relations make 
		/// sense for the space (NumberSpaces), -1 is returned when this.value &lt; 
		/// val and 1 is returned when this.value &gt; val.
		/// 
		/// This function assumes that the ValueSpaces are equivalent.  The 
		/// results of this function are not meaningful otherwise, and an 
		/// exception may be thrown if the spaces are significantly different.
		/// </summary>
		/// <param name="space">a ValueSpace to compare to this one</param>
		/// <returns>an integer describing how the values of these spaces are related</returns>
		public int CompareValues( PUCValue val )
		{
			return this._valueSpace.CompareValues( this, val );
		}
	}
}
