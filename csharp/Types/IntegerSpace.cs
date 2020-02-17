using System;

namespace PUC.Types
{
	/// <summary>
	/// A ValueSpace that stores integer values that may be ranged or
	/// incremented.
	/// </summary>
	public class IntegerSpace : NumberSpace
	{
		/*
		 * Member Variables
		 */

		/// <summary>
		/// Whether this IntegerSpace has a range.
		/// </summary>
		protected bool _ranged;

		/// <summary>
		/// The inclusive minimum of this IntegerSpace's range.
		/// </summary>
		protected IPUCNumber _min;

		/// <summary>
		/// The inclusive maximum of this IntegerSpace's range.
		/// </summary>
		protected IPUCNumber _max;

		/// <summary>
		/// Whether this IntegerSpace has an increment.
		/// </summary>
		protected bool _incremented;

		/// <summary>
		/// The increment of this IntegerSpace.  This means the
		/// value of this IntegerSpace must be (for some integer n):
		/// val = minimum + n * increment
		/// </summary>
		protected IPUCNumber _increment;


		/*
		 * Constructors
		 */

		/// <summary>
		/// Class constructor.
		/// </summary>
		public IntegerSpace()
		{
			_ranged = false;
			_incremented = false;
		}

		/// <summary>
		/// Class constructor that creates a ranged IntegerSpace.
		/// </summary>
		/// <param name="min">the inclusive minimum of the range</param>
		/// <param name="max">the inclusive maximum of the range</param>
		public IntegerSpace( IPUCNumber min, IPUCNumber max )
		{
			_ranged = true;
			_min = min;
			_max = max;

			if ( _min.GetIntValue() > _max.GetIntValue() && 
				!( _min is NumberConstraint ) &&
				!( _max is NumberConstraint ) )
				throw new ArgumentException( "IntegerSpace: minimum must be less than maximum" );

			_incremented = false;
		}

		/// <summary>
		/// Class constructor that creates a ranged, incremented IntegerSpace. 
		/// Incremented means that the value of this space must equal 
		/// <code>min + n * increment</code> for some integer n.
		/// </summary>
		/// <param name="min">the inclusive minimum of the range</param>
		/// <param name="max">the inclusive maximum of the range</param>
		/// <param name="increment">the increment of the IntegerSpace</param>
		public IntegerSpace( IPUCNumber min, IPUCNumber max, IPUCNumber increment ) 
		{		
			_ranged = true;
			_min = min;
			_max = max;

			if ( _min.GetIntValue() > _max.GetIntValue() )
				throw new ArgumentException( "IntegerSpace: minimum must be less or equal to maximum" );

			_incremented = true;
			_increment = increment;
		}

		/*
		 * Overridden Methods
		 */
		/// <summary>
		/// Returns a string descriptor of this IntegerSpace.
		/// </summary>
		/// <returns>a string descriptor of this IntegerSpace</returns>
		public override string ToString() 
		{
			string ret = base.ToString();

			if ( _ranged || _incremented ) 
			{	
				ret += " : ";

				ret += _ranged ? "max = " + _max + " min = " + _min : "";
				ret += _incremented ? "incr = " + _increment : "";
			}

			return ret;
		}

		/// <summary>
		/// Returns a shallow copy of this IntegerSpace.
		/// </summary>
		/// <returns>a shallow copy of this IntegerSpace</returns>
		public override object Clone()
		{
			IntegerSpace clone = new IntegerSpace();

			clone._ranged = _ranged;
			clone._min = _min;
			clone._max = _max;

			clone._incremented = _incremented;
			clone._increment = _increment;

			return clone;
		}

		/// <summary>
		/// Returns "integer" 
		/// </summary>
		/// <returns>"integer"</returns>
		public override string Name
		{
			get
			{
				return "integer";
			}
		}

		/// <summary>
		/// Returns ValueSpace.INTEGER_SPACE.
		/// </summary>
		/// <returns>ValueSpace.INTEGER_SPACE</returns>
		public override int Space
		{
			get
			{
				return ValueSpace.INTEGER_SPACE;
			}
		}

		/// <summary>
		/// Determines whether the provided value could be assigned to 
		/// this ValueSpace.
		/// </summary>
		/// <param name="val">the value to be validated</param>
		/// <returns>whether this value can be assigned to this ValueSpace</returns>
		public override object Validate( object val )
		{
			if ( val is string ) 
			{
				try 
				{
					int intval = Int32.Parse( (string)val );
					if ( validateNoStrCheck( intval ) )
						return intval;
				}
				catch(FormatException) 
				{
					return null;
				}
			}
			else if ( validateNoStrCheck( val ) )
				return val;

			return null;
		}

		/// <summary>
		/// Compares the values of two IntegerSpaces.  
		/// </summary>
		/// <param name="space">the space to compare to this one</param>
		/// <returns>0 if the value of the two sets are equals, 
		/// 1 if the value passed in is greater than this space's value,
		/// -1 if the value passed in is less than this space's value</returns>
		/// <exception cref="InvalidCastException">thrown if space is not an instance of IntegerSpace</exception>
		public override int CompareValues( object val1, object val2 )
		{
			if ( val1 == null && val2 == null ) return 0;
			else if ( val1 != null ^ val2 != null ) return -1;

			int ival1 = (int)val1;
			int ival2 = (int)val2;

			if ( ival1 == ival2 ) return 0;
			else if ( ival1 > ival2 ) return 1;
			else return -1;
		}

		/*
		 * Member Methods
		 */
		/// <summary>
		/// Converts the provided string into an object that can be 
		/// assigned to this ValueSpace.
		/// </summary>
		/// <param name="val">a string containing a value</param>
		/// <returns>the value converted into the appropriate object</returns>
		/// <exception cref="FormatException">thrown if the string does not contain the appropriate format</exception>
		protected object convertString( string val ) 
		{
			int ret = Int32.Parse( val );

			if (! validateNoStrCheck( ret ) )
				throw new FormatException( "convertString: Not within bounds of this object." );

			return ret;
		}

		/// <summary>
		/// Validates that the provided value can be assigned to this 
		/// ValueSpace.  Assumes that the provided value is not a string.
		/// </summary>
		/// <param name="val"></param>
		/// <returns></returns>
		private bool validateNoStrCheck( object val )
		{
			if ( val is Int32 ) 
			{
				int intval = (int)val;

				bool rng = !_ranged || ( intval >= _min.GetIntValue() && 
										 intval <= _max.GetIntValue() );

				bool incr = !_incremented || (
					Math.IEEERemainder( ((double)intval) - _min.GetDoubleValue(),
										_increment.GetDoubleValue() )
					== 0 );

				return rng && incr;									
			}

			return false;
		}

		/// <summary>
		/// Returns whether this IntegerSpace is ranged.
		/// </summary>
		/// <returns>whether this IntegerSpace is ranged</returns>
		public bool IsRanged()
		{
			return _ranged;
		}

		/// <summary>
		/// Returns the inclusive minimum range of this IntegerSpace.  This 
		/// value is meaningless if this IntegerSpace is not ranged.
		/// </summary>
		/// <returns>the inclusive minimum range of this IntegerSpace</returns>
		public IPUCNumber GetMinimum()
		{
			return _min;
		}

		/// <summary>
		/// Returns the inclusive maximum range of this IntegerSpace.  This 
		/// value is meaningless if this IntegerSpace is not ranged.
		/// </summary>
		/// <returns>the inclusive maximum range of this IntegerSpace</returns>
		public IPUCNumber GetMaximum()
		{
			return _max;
		}

		/// <summary>
		/// Returns whether this IntegerSpace has an increment.
		/// </summary>
		/// <returns>whether this IntegerSpace has an increment</returns>
		public bool IsIncremented()
		{
			return _incremented;
		}

		/// <summary>
		/// Returns the increment of this IntegerSpace.  This means that the
		/// value of this IntegerSpace must be equal to 
		/// <code>min + n * increment</code> for some integer n.
		/// </summary>
		/// <returns>the increment of this IntegerSpace</returns>
		public IPUCNumber GetIncrement()
		{
			return _increment;
		}

		/// <summary>
		/// Register any constraints in this type with the appropriate 
		/// state variable.
		/// </summary>
		/// <param name="state"></param>
		public override void RegisterConstraints(ApplianceState state)
		{
			if ( IsRanged() )
			{
				if ( _min is NumberConstraint )
					new TypeConstraintListener( state, (NumberConstraint)_min );

				if ( _max is NumberConstraint )
					new TypeConstraintListener( state, (NumberConstraint)_max );

				if ( IsIncremented() && _increment is NumberConstraint )
					new TypeConstraintListener( state, (NumberConstraint)_increment );
			}
		}

		public override bool IsSameSpace(ValueSpace space)
		{
			if ( space is IntegerSpace )
			{
				IntegerSpace ispace = (IntegerSpace)space;

				if ( ispace.IsRanged() && this.IsRanged() )
				{
					if ( ispace.IsIncremented() && this.IsIncremented() )
					{
						if ( !ispace.GetIncrement().IsSameValue( this.GetIncrement() ) )
							return false;
					}
					else return false;

					if ( !ispace.GetMinimum().IsSameValue( this.GetMinimum() ) )
						return false;

					if ( !ispace.GetMaximum().IsSameValue( this.GetMaximum() ) )
						return false;
				}
				else return false;

				return true;
			}

			return false;
		}


		/*
		 * Test Method
		 *
		public static void Main()
		{
			Console.WriteLine( "IntegerSpace Test Sequence" );
			Console.WriteLine( "--------------------------" );

			IntegerSpace regInt  = new IntegerSpace();
			IntegerSpace rngInt  = new IntegerSpace( new IntNumber( 0 ), 
													 new IntNumber( 10 ) );
			IntegerSpace incInt  = new IntegerSpace( new IntNumber( 0 ), 
													 new IntNumber( 10 ), 
													 new IntNumber( 2 ) );

			int test = 0;

			// Test #1 
			test++; 
			try 
			{
				regInt.SetValue( 5 );
		      
				if ( ((int)regInt.Value) == 5 ) 
				{
					Console.WriteLine( "Passed #" + test );
				}
				else throw new System.NullReferenceException( "dumb" );
			}
			catch( Exception ) 
			{
				Console.WriteLine( "Error setting value #" + test );
				return;
			}

			// Test #2 
			test++; 
			try 
			{
				rngInt.SetValue( 5 );
		      
				if ( ((int)rngInt.Value) == 5 ) 
				{
					Console.WriteLine( "Passed #" + test );
				}
				else throw new System.NullReferenceException( "dumb" );
			}
			catch( Exception ) 
			{
				Console.WriteLine( "Error setting value #" + test );
				return;
			}

			// Test #3 
			test++; 
			try 
			{
				incInt.SetValue( 5 );
		      
				Console.WriteLine( "Error setting value #" + test );
				return;
			}
			catch( Exception ) 
			{
				if ( ((int)incInt.Value) == 0 ) 
				{
					Console.WriteLine( "Passed #" + test );
				}
				else 
				{
					Console.WriteLine( "Error setting value #" + test );
					return;
				}
			}

			// Test #4 
			test++; 
			try 
			{
				incInt.SetValue( 6 );
		      
				if ( ((int)incInt.Value) == 6 ) 
				{
					Console.WriteLine( "Passed #" + test );
				}
				else throw new System.NullReferenceException( "dumb" );
			}
			catch( Exception ) 
			{
				Console.WriteLine( "Error setting value #" + test );
				return;
			}

			// Test #5 
			test++; 
			try 
			{
				rngInt.SetValue( 11 );
		      
				Console.WriteLine( "Error setting value #" + test );
				return;
			}
			catch( Exception ) 
			{
				if ( ((int)rngInt.Value) == 5 ) 
				{
					Console.WriteLine( "Passed #" + test );
				}
				else 
				{
					Console.WriteLine( "Error setting value #" + test );
					return;
				}
			}

			// Test #6 
			test++; 
			try 
			{
				regInt.SetValue( 11 );
		      
				if ( ((int)regInt.Value) == 11 ) 
				{
					Console.WriteLine( "Passed #" + test );
				}
				else throw new System.NullReferenceException( "dumb" );
			}
			catch( Exception ) 
			{
				Console.WriteLine( "Error setting value #" + test );
				return;
			}

			// Test #7 
			test++; 
			try 
			{
				incInt.SetValue( 11 );
		      
				Console.WriteLine( "Error setting value #" + test );
				return;
			}
			catch( Exception ) 
			{
				if ( ((int)incInt.Value) == 6 ) 
				{
					Console.WriteLine( "Passed #" + test );
				}
				else 
				{
					Console.WriteLine( "Error setting value #" + test );
					return;
				}
			}

			// Test #8 
			test++; 
			try 
			{
				incInt.SetValue( 12 );
		      
				Console.WriteLine( "Error setting value #" + test );
				return;
			}
			catch( Exception ) 
			{
				if ( ((int)incInt.Value) == 6 ) 
				{
					Console.WriteLine( "Passed #" + test );
				}
				else 
				{
					Console.WriteLine( "Error setting value #" + test );
					return;
				}
			}

			// Test #9 
			test++; 
			try 
			{
				incInt.SetValue( 10 );
		      
				if ( ((int)incInt.Value) == 10 ) 
				{
					Console.WriteLine( "Passed #" + test );
				}
				else throw new System.NullReferenceException( "dumb" );
			}
			catch( Exception ) 
			{
				Console.WriteLine( "Error setting value #" + test );
				return;
			}

			// Test #10 
			test++; 
			try 
			{
				incInt.SetValue( true );
		      
				Console.WriteLine( "Error setting value #" + test );
				return;
			}
			catch( Exception ) 
			{
				if ( ((int)incInt.Value) == 10 ) 
				{
					Console.WriteLine( "Passed #" + test );
				}
				else 
				{
					Console.WriteLine( "Error setting value #" + test );
					return;
				}
			}

			IntegerSpace clnInt = (IntegerSpace)incInt.Clone();
			
			// Test #11 
			test++; 
			try 
			{
				clnInt.SetValue( 5 );
		      
				Console.WriteLine( "Error setting value #" + test );
				return;
			}
			catch( Exception ) 
			{
				if ( ((int)clnInt.Value) == 10 ) 
				{
					Console.WriteLine( "Passed #" + test );
				}
				else 
				{
					Console.WriteLine( "Error setting value #" + test );
					return;
				}
			}

			// Test #12 
			test++; 
			try 
			{
				clnInt.SetValue( 6 );
		      
				if ( ((int)clnInt.Value) == 6 ) 
				{
					Console.WriteLine( "Passed #" + test );
				}
				else throw new System.NullReferenceException( "dumb" );
			}
			catch( Exception ) 
			{
				Console.WriteLine( "Error setting value #" + test );
				return;
			}
			
			// Test #13 
			test++; 
			try 
			{
				clnInt.SetValue( 11 );
		      
				Console.WriteLine( "Error setting value #" + test );
				return;
			}
			catch( Exception ) 
			{
				if ( ((int)clnInt.Value) == 6 ) 
				{
					Console.WriteLine( "Passed #" + test );
				}
				else 
				{
					Console.WriteLine( "Error setting value #" + test );
					return;
				}
			}

			// Test #14 
			test++; 
			try 
			{
				clnInt.SetValue( 12 );
		      
				Console.WriteLine( "Error setting value #" + test );
				return;
			}
			catch( Exception ) 
			{
				if ( ((int)clnInt.Value) == 6 ) 
				{
					Console.WriteLine( "Passed #" + test );
				}
				else 
				{
					Console.WriteLine( "Error setting value #" + test );
					return;
				}
			}

			// Test #15 
			test++; 
			try 
			{
				clnInt.SetValue( 10 );
		      
				if ( ((int)clnInt.Value) == 10 ) 
				{
					Console.WriteLine( "Passed #" + test );
				}
				else throw new System.NullReferenceException( "dumb" );
			}
			catch( Exception ) 
			{
				Console.WriteLine( "Error setting value #" + test );
				return;
			}

			// Test #16 
			test++; 
			try 
			{
				clnInt.SetValue( "10" );
		      
				if ( ((int)clnInt.Value) == 10 ) 
				{
					Console.WriteLine( "Passed #" + test );
				}
				else throw new System.NullReferenceException( "dumb" );
			}
			catch( Exception ) 
			{
				Console.WriteLine( "Error setting value #" + test );
				return;
			}
			
			
			// Test #17
			test++; 
			try 
			{
				regInt.SetValue( 4 );
				rngInt.SetValue( 4 );
		      
				if ( regInt.CompareValues( rngInt ) == 0 ) 
				{
					Console.WriteLine( "Passed #" + test );
				}
				else throw new System.NullReferenceException( "dumb" );
			}
			catch( Exception ) 
			{
				Console.WriteLine( "Error setting value #" + test );
				return;
			}

			Console.WriteLine( "IntegerSpace PASSED" );
		}*/
	}
}
