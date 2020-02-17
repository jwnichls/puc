using System;
using System.Collections;

namespace PUC.Types
{
	/// <summary>
	/// A mutable number that can also be constrained to the value
	/// of another object.
	/// </summary>
	public interface IPUCNumber : IPUCValue
	{
		/// <summary>
		/// The abstract method to return an int value.
		/// </summary>
		/// <returns>the int value of this mutable number object</returns>
		int GetIntValue();

		/// <summary>
		/// The abstract method to return a double value.
		/// </summary>
		/// <returns>the double value of this mutable number object</returns>
		double GetDoubleValue();
	}

	public class DoubleNumber : PUCValue, IPUCNumber
	{
		/*
		 * Member Variables
		 */
		

		/*
		 * Constructor
		 */

		/// <summary>
		/// Class constructor that takes the value to be assigned to 
		/// this DoubleNumber.
		/// </summary>
		/// <param name="val">the value to be assigned to this DoubleNumber</param>
		public DoubleNumber( double val )
			: base( val )
		{
		}

		/// <summary>
		/// Class constructor that takes a string to be assigned to 
		/// this DoubleNumber.
		/// </summary>
		/// <param name="val">the value to be assigned to this DoubleNumber</param>
		public DoubleNumber( string strval )
			: base( Double.Parse( strval ) )
		{
		}


		/*
		 * Member Methods
		 */

		/// <summary>
		/// Sets the value of this DoubleNumber
		/// </summary>
		/// <param name="val">the value to be assigned to this DoubleNumber</param>
		public void SetDouble( double val )
		{
			_value = val;
		}

		/// <summary>
		/// The method to return an int value.
		/// </summary>
		/// <returns>the int value of this mutable number object</returns>
		public int GetIntValue()
		{
			return (int)((double)_value);
		}

		/// <summary>
		/// The method to return a double value.
		/// </summary>
		/// <returns>the double value of this mutable number object</returns>
		public double GetDoubleValue()
		{
			return (double)_value;
		}

		/// <summary>
		/// Returns the string representation of this class.
		/// </summary>
		/// <returns>the string representation of this class</returns>
		public override string ToString()
		{
			return _value.ToString();
		}
	}


	public class IntNumber : PUCValue, IPUCNumber
	{
		/*
		 * Member Variables
		 */
	

		/*
		 * Constructor
		 */

		/// <summary>
		/// Class constructor that takes the value to be assigned to 
		/// this IntNumber.
		/// </summary>
		/// <param name="val">the value to be assigned to this IntNumber</param>
		public IntNumber( int val )
			: base( val )
		{
		}

		/// <summary>
		/// Class constructor that takes a string to be assigned to 
		/// this IntNumber.
		/// </summary>
		/// <param name="val">the value to be assigned to this IntNumber</param>
		public IntNumber( string strval )
			: base( Int32.Parse( strval ) )
		{
		}

		/*
		 * Member Methods
		 */

		/// <summary>
		/// Sets the value of this IntNumber
		/// </summary>
		/// <param name="val">the value to be assigned to this IntNumber</param>
		public void SetInt( int val )
		{
			_value = val;
		}

		/// <summary>
		/// The method to return an int value.
		/// </summary>
		/// <returns>the int value of this mutable number object</returns>
		public int GetIntValue()
		{
			return (int)_value;
		}

		/// <summary>
		/// The method to return a double value.
		/// </summary>
		/// <returns>the double value of this mutable number object</returns>
		public double GetDoubleValue()
		{
			return (double)((int)_value);
		}

		/// <summary>
		/// Returns the string representation of this class.
		/// </summary>
		/// <returns>the string representation of this class</returns>
		public override string ToString()
		{
			return _value.ToString();
		}
	}


	/// <summary>
	/// A PUCint whose value depends on the value of another state
	/// variable.
	/// </summary>
	public class NumberConstraint : ValueConstraint, 
									IPUCNumber
	{
		/*
		 * Member Variables
		 */


		/*
		 * Constructor
		 */
		public NumberConstraint( string valueStateName )
			: base( valueStateName )
		{
		}

		public NumberConstraint( ApplianceState valueState )
			: base( valueState )
		{
			if ( !(State.Type.ValueSpace is NumberSpace) )
				setInvalid();
		}

		/// <summary>
		/// Creates a NumberConstraint object.  Automatically creates a listener
		/// for the constrained object.
		/// </summary>
		/// <param name="constrainedState">a reference to the state that will be constrained</param>
		/// <param name="valueStateName">the name of the state that will provide the value</param>
		public NumberConstraint( ApplianceState constrainedState,
								 string valueStateName ) : this( valueStateName )
		{
			// create the listener
			new TypeConstraintListener( constrainedState, this );
		}

		public NumberConstraint( ApplianceState constrainedState,
								 ApplianceState valueState ) : this( valueState )
		{
			// create the listener
			new TypeConstraintListener( constrainedState, this );
		}


		/*
		 * Member Methods
		 */

		public ApplianceState GetValueState()
		{
			return _state.State;
		}

		public override bool ResolveObject( VariableTable varTable ) 
		{
			base.ResolveObject( varTable );

			if( _valid && !( State.Type.ValueSpace is NumberSpace ) )
				setInvalid();

			return _valid;
		}

		/// <summary>
		/// The method to return an int value.
		/// </summary>
		/// <returns>the int value of this mutable number object</returns>
		public int GetIntValue()
		{
			if ( _valid && State.Defined )
				return Int32.Parse( _state.Value.ToString() );
			else
				return -1;
		}

		/// <summary>
		/// The method to return a double value.
		/// </summary>
		/// <returns>the double value of this mutable number object</returns>
		public double GetDoubleValue()
		{
			if ( _valid && State.Defined )
				return Double.Parse( _state.Value.ToString() );
			else
				return -1.0;
		}


		/// <summary>
		/// Returns the string representation of this class.
		/// </summary>
		/// <returns>the string representation of this class</returns>
		public override string ToString()
		{
			if ( _valid && State.Defined )
				return _state.Value.ToString();
			else 
				return "#NOT VALID#";
		}
	}
}
