using System;
using System.Collections;

namespace PUC.Types
{
	/// <summary>
	/// A mutable string that can also be constrained to the value
	/// of another object.
	/// </summary>
	public interface IPUCString : IPUCValue
	{
		/// <summary>
		/// Forces its sub-classes to override ToString()
		/// </summary>
		/// <returns>the value of this mutable string object</returns>
		string ToString();
	}

	public class StringValue : PUCValue, IPUCString
	{
		/*
		 * Member Variables
		 */

		/*
		 * Constructor
		 */

		/// <summary>
		/// Class constructor that takes the value to be assigned to 
		/// this StringValue.
		/// </summary>
		/// <param name="value">the value to be assigned to this StringValue</param>
		public StringValue( string value )
			: base( value )
		{
		}


		/*
		 * Member Methods
		 */

		/// <summary>
		/// Sets the value of this StringValue
		/// </summary>
		/// <param name="value">the value to be assigned to this StringValue</param>
		public void SetString( string value )
		{
			_value = value;
		}

		/// <summary>
		/// Returns the value of this StringValue
		/// </summary>
		/// <returns>the value of this StringValue</returns>
		public override string ToString()
		{
			return (string)_value;
		}
	}


	/// <summary>
	/// A PUCString whose value depends on the value of another state
	/// variable.
	/// </summary>
	public class StringConstraint : ValueConstraint,
									IPUCString
	{
		/*
		 * Member Variables
		 */


		/*
		 * Constructor
		 */
		public StringConstraint( string valueStateName )
			: base( valueStateName )
		{
		}

		public StringConstraint( ApplianceState valueState )
			: base( valueState )
		{
			if ( _valid && !(State.Type.ValueSpace is StringSpace) )
				setInvalid();
		}

		/// <summary>
		/// Creates a StringConstraint object.  Automatically create a listener for
		/// the constrainedObject.
		/// </summary>
		/// <param name="constrainedState">a reference to the state that will be constrained</param>
		/// <param name="valueStateName">the name of the state that will provide the value</param>
		public StringConstraint( ApplianceObject constrainedObject,
								 string valueStateName ) : this( valueStateName )
		{
			// add listener
			new LabelConstraintListener( constrainedObject, this );
		}


		/*
		 * Member Methods
		 */

		public override bool ResolveObject( VariableTable varTable ) 
		{
			base.ResolveObject( varTable );

			if ( _valid && !(State.Type.ValueSpace is StringSpace) )
				setInvalid();

			return _valid;
		}


		/// <summary>
		/// Returns the value of this StringConstraint
		/// </summary>
		/// <returns>the value of this StringConstraint</returns>
		public override string ToString()
		{
			if ( _valid && State.Defined )
				return _state.Value.ToString();
			else 
				return "";
		}
	}
}
