using System;

namespace PUC.Types
{
	/// <summary>
	/// A ValueSpace that stores fixed point values that may be ranged or
	/// incremented.
	/// </summary>
	public class FixedPtSpace : NumberSpace
	{
		/*
		 * Member Variables
		 */

		/// <summary>
		/// The fixed location of the decimal point (counting in from the 
		/// right side).
		/// </summary>
		protected int _pointpos;

		/// <summary>
		/// The correction factor to multiply values by to get their internal
		/// storage value.
		/// </summary>
		protected int _pointCorrectionFactor;

		/// <summary>
		/// Whether this FixedPtSpace has a range.
		/// </summary>
		protected bool _ranged;

		/// <summary>
		/// The inclusive minimum of this FixedPtSpace's range.
		/// </summary>
		protected IPUCNumber _min;

		/// <summary>
		/// The inclusive maximum of this FixedPtSpace's range.
		/// </summary>
		protected IPUCNumber _max;

		/// <summary>
		/// Whether this FixedPtSpace has an increment.
		/// </summary>
		protected bool _incremented;

		/// <summary>
		/// The increment of this FixedPtSpace.  This means the
		/// value of this FixedPtSpace must be (for some integer n):
		/// val = minimum + n * increment
		/// </summary>
		protected IPUCNumber _increment;


		/*
		 * Constructors
		 */

		/// <summary>
		/// Class constructor.
		/// </summary>
		/// <param name="pointpos">the location of the decimal point counting from right</param>
		/// <exception cref="ArgumentException">thrown if pos is less than 0</exception>
		public FixedPtSpace( int pointpos )
		{
			if ( pointpos < 0 )
				throw new ArgumentException( "Point position of FixedPtSpace must be >= 0" );

			_pointpos = pointpos;
			_pointCorrectionFactor = calculateCorrectionFactor( _pointpos );

			_ranged = false;
			_incremented = false;
		}

		/// <summary>
		/// Class constructor that creates a ranged FixedPtSpace.
		/// </summary>
		/// <param name="pointpos">the location of the decimal point counting from right</param>
		/// <param name="min">the inclusive minimum of the range</param>
		/// <param name="max">the inclusive maximum of the range</param>
		/// <exception cref="ArgumentException">thrown if pos is less than 0</exception>
		public FixedPtSpace( int pointpos, IPUCNumber min, IPUCNumber max )
		{
			if ( pointpos < 0 )
				throw new ArgumentException( "Point position of FixedPtSpace must be >= 0" );

			_pointpos = pointpos;
			_pointCorrectionFactor = calculateCorrectionFactor( _pointpos );

			_ranged = true;
			_min = min;
			_max = max;

			if ( _min.GetDoubleValue() >= _max.GetDoubleValue() &&
				!( _min is NumberConstraint ) &&
				!( _max is NumberConstraint ) )
				throw new ArgumentException( "FixedPtSpace: minimum must be less than maximum" );

			_incremented = false;
		}

		/// <summary>
		/// Class constructor that creates a ranged, incremented FixedPtSpace. 
		/// Incremented means that the value of this space must equal 
		/// <code>min + n * increment</code> for some integer n.
		/// </summary>
		/// <param name="pointpos">the location of the decimal point counting from right</param>
		/// <param name="min">the inclusive minimum of the range</param>
		/// <param name="max">the inclusive maximum of the range</param>
		/// <param name="increment">the increment of the FixedPtSpace</param>
		/// <exception cref="ArgumentException">thrown if pos is less than 0</exception>
		public FixedPtSpace( int pointpos, IPUCNumber min, IPUCNumber max, IPUCNumber increment ) 
		{		
			if ( pointpos < 0 )
				throw new ArgumentException( "Point position of FixedPtSpace must be >= 0" );

			_pointpos = pointpos;
			_pointCorrectionFactor = calculateCorrectionFactor( _pointpos );

			_ranged = true;
			_min = min;
			_max = max;

			if ( _min.GetDoubleValue() >= _max.GetDoubleValue() )
				throw new ArgumentException( "FixedPtSpace: minimum must be less than maximum" );

			_incremented = true;
			_increment = increment;
		}

		/*
		 * Overridden Methods
		 */
		/// <summary>
		/// Returns a string descriptor of this FixedPtSpace.
		/// </summary>
		/// <returns>a string descriptor of this FixedPtSpace</returns>
		public override string ToString() 
		{
			string ret = base.ToString();

			if ( _ranged || _incremented ) 
			{	
				ret += " : ";

				ret += _ranged ? "max = " + _max + 
						" min = " + _min : "";
				ret += _incremented ? "incr = " + _increment : "";
			}

			return ret;
		}

		/// <summary>
		/// Returns a shallow copy of this FixedPtSpace.
		/// </summary>
		/// <returns>a shallow copy of this FixedPtSpace</returns>
		public override object Clone()
		{
			FixedPtSpace clone = new FixedPtSpace( _pointpos );

			clone._ranged = _ranged;
			clone._min = _min;
			clone._max = _max;

			clone._incremented = _incremented;
			clone._increment = _increment;

			return clone;
		}


		/// <summary>
		/// Returns "fixed point" 
		/// </summary>
		/// <returns>"fixed point"</returns>
		public override string Name
		{
			get
			{
				return "fixed point";
			}
		}

		/// <summary>
		/// Returns ValueSpace.FIXED_PT_SPACE.
		/// </summary>
		/// <returns>ValueSpace.FIXED_PT_SPACE</returns>
		public override int Space
		{
			get
			{
				return ValueSpace.FIXED_PT_SPACE;
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
					double dblval = Double.Parse( (string)val );
					if ( validateNoStrCheck( dblval ) )
						return dblval;
				}
				catch(FormatException) 
				{
					return null;
				}
			}
			else if ( validateNoStrCheck( val ) )
				return (double)val;

			return null;
		}

		/// <summary>
		/// Compares the values of two FixedPtSpaces.  
		/// </summary>
		/// <param name="space">the space to compare to this one</param>
		/// <returns>0 if the value of the two sets are equals, 
		/// 1 if the value passed in is greater than this space's value,
		/// -1 if the value passed in is less than this space's value</returns>
		/// <exception cref="InvalidCastException">thrown if space is not an instance of FixedPtSpace</exception>
		public override int CompareValues( object val1, object val2 )
		{
			if ( val1 == null && val2 == null ) return 0;
			else if ( val1 != null ^ val2 != null ) return -1;

			int fltVal1 = convertToInternal((double)val1);
			int fltVal2 = convertToInternal((double)val2);

			if ( fltVal1 == fltVal2 ) return 0;
			else if ( fltVal1 > fltVal2 ) return 1;
			else return -1;
		}

		/*
		 * Member Methods
		 */
		/// <summary>
		/// Calculates the multiplier for converting values to the internal integer
		/// representation.
		/// </summary>
		/// <param name="pos">the location of the decimal point counting from the right side</param>
		/// <returns>the multiplier for converting values to the internal integer representation.</returns>
		/// <exception cref="ArgumentException">thrown if pos is less than 0</exception>
		protected int calculateCorrectionFactor( int pos )
		{
			if ( pos < 0 )
				throw new ArgumentException( "Point position of FixedPtSpace must be >= 0" );

			return (int)Math.Pow( (double)10, (double)pos );
		}

		/// <summary>
		/// Converts a double value to the internal int format.
		/// </summary>
		/// <param name="val">the double value to convert</param>
		/// <returns>the internal representation of val</returns>
		private int convertToInternal( double val )
		{
			return (int)Math.Round( val * _pointCorrectionFactor );
		}

		/// <summary>
		/// Convernts an int value to the external double format.
		/// </summary>
		/// <param name="val">the int value to convert</param>
		/// <returns>the external representation of val</returns>
		private double convertToExternal( int val ) 
		{
			return ((double)val) / ((double)_pointCorrectionFactor);
		}

		/// <summary>
		/// Converts the provided string into an object that can be 
		/// assigned to this ValueSpace.  This includes converting the 
		/// value to the internal int representation.
		/// </summary>
		/// <param name="val">a string containing a value</param>
		/// <returns>the value converted into the appropriate object</returns>
		/// <exception cref="FormatException">thrown if the string does not contain the appropriate format</exception>
		protected object convertString( string val ) 
		{
			double ret = Double.Parse( val );

			if (! validateNoStrCheck( ret ) )
				throw new FormatException( "convertString: val not within bounds of space." );

			return convertToInternal( ret );
		}

		/// <summary>
		/// Validates that the provided value can be assigned to this 
		/// ValueSpace.  Assumes that the provided value is not a string.
		/// </summary>
		/// <param name="val"></param>
		/// <returns></returns>
		private bool validateNoStrCheck( object val )
		{
			if ( val is Double ) 
			{
				int intval = convertToInternal( (double)val );

				int min = 0, max = 0;

				if ( _ranged ) 
				{
					min = convertToInternal( _min.GetDoubleValue() );
					max = convertToInternal( _max.GetDoubleValue() );
				}

				bool rng = !_ranged || ( intval >= min && intval <= max);

				int increment = 0;
				if ( _incremented )
					increment = convertToInternal( _increment.GetDoubleValue() );

				bool incr = !_incremented || (
					Math.IEEERemainder( ((double)intval) - ((double)min),
					(double)increment )
					== 0 );

				return rng && incr;									
			}

			return false;
		}

		/// <summary>
		/// Returns whether this FixedPtSpace is ranged.
		/// </summary>
		/// <returns>whether this FixedPtSpace is ranged</returns>
		public bool IsRanged()
		{
			return _ranged;
		}

		/// <summary>
		/// Returns the inclusive minimum range of this FixedPtSpace.  This 
		/// value is meaningless if this FixedPtSpace is not ranged.
		/// </summary>
		/// <returns>the inclusive minimum range of this FixedPtSpace</returns>
		public IPUCNumber GetMinimum()
		{
			return _min;
		}

		/// <summary>
		/// Returns the inclusive maximum range of this FixedPtSpace.  This 
		/// value is meaningless if this FixedPtSpace is not ranged.
		/// </summary>
		/// <returns>the inclusive maximum range of this FixedPtSpace</returns>
		public IPUCNumber GetMaximum()
		{
			return _max;
		}

		/// <summary>
		/// Returns whether this FixedPtSpace has an increment.
		/// </summary>
		/// <returns>whether this FixedPtSpace has an increment</returns>
		public bool IsIncremented()
		{
			return _incremented;
		}

		/// <summary>
		/// Returns the increment of this FixedPtSpace.  This means that the
		/// value of this FixedPtSpace must be equal to 
		/// <code>min + n * increment</code> for some integer n.
		/// </summary>
		/// <returns>the increment of this FixedPtSpace</returns>
		public IPUCNumber GetIncrement()
		{
			return _increment;
		}


		/// <summary>
		/// Returns the point position of this FixedPtSpace.  This defines the
		/// precision of decimal point values that may be stored in this space.
		/// </summary>
		/// <returns>an integer decribing the number of decimal places allowed by this space</returns>
		public int GetPointPosition()
		{
			return _pointpos;
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
			if ( space is FixedPtSpace )
			{
				FixedPtSpace fs = (FixedPtSpace)space;

				if ( fs.GetPointPosition() != this.GetPointPosition() )
					return false;

				if ( fs.IsRanged() && this.IsRanged() )
				{
					if ( fs.IsIncremented() && this.IsIncremented() )
					{
						if ( !fs.GetIncrement().IsSameValue( this.GetIncrement() ) )
							return false;
					}
					else return false;

					if ( !fs.GetMinimum().IsSameValue( this.GetMinimum() ) )
						return false;

					if ( !fs.GetMaximum().IsSameValue( this.GetMaximum() ) )
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
			Console.WriteLine( "FixedPtSpace Test Sequence" );
			Console.WriteLine( "--------------------------" );

			FixedPtSpace regFxd  = new FixedPtSpace( 1 );
			FixedPtSpace rngFxd  = new FixedPtSpace( 1, new DoubleNumber( 0 ), new DoubleNumber( 10 ) );
			FixedPtSpace incFxd  = new FixedPtSpace( 1, new DoubleNumber( 0 ),
														new DoubleNumber( 12 ), 
														new DoubleNumber( 0.3 ) );

			int test = 0;

			// Test #1
			test++; 
			try 
			{
				regFxd.SetValue( 5.3 );
		      
				if ( ((double)regFxd.Value) == 5.3 ) 
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
				rngFxd.SetValue( 5.3 );
		      
				if ( ((double)rngFxd.Value) == 5.3 ) 
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
				incFxd.SetValue( 5.3 );
		      
				Console.WriteLine( "Error setting value #" + test );
				return;
			}
			catch( Exception ) 
			{
				if ( ((double)incFxd.Value) == 0 ) 
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
				incFxd.SetValue( 5.1 );
		      
				if ( ((double)incFxd.Value) == 5.1 ) 
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
				rngFxd.SetValue( 11 );
		      
				Console.WriteLine( "Error setting value #" + test );
				return;
			}
			catch( Exception ) 
			{
				if ( ((double)rngFxd.Value) == 5.3 ) 
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
				regFxd.SetValue( (double)11 );
		      
				if ( ((double)regFxd.Value) == (double)11 ) 
				{
					Console.WriteLine( "Passed #" + test );
				}
				else throw new System.NullReferenceException( "dumb" );
			}
			catch( Exception e) 
			{
				Console.WriteLine( e.Message );
				Console.WriteLine( "Error setting value #" + test + " to " + (double)regFxd.Value);
				return;
			}

			// Test #7 
			test++; 
			try 
			{
				incFxd.SetValue( (double)13 );
		      
				Console.WriteLine( "Error setting value #" + test );
				return;
			}
			catch( Exception ) 
			{
				if ( ((double)incFxd.Value) == 5.1 ) 
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
				incFxd.SetValue( (double)15 );
		      
				Console.WriteLine( "Error setting value #" + test );
				return;
			}
			catch( Exception ) 
			{
				if ( ((double)incFxd.Value) == 5.1 ) 
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
				incFxd.SetValue( (double)12 );
		      
				if ( ((double)incFxd.Value) == (double)12 ) 
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
				incFxd.SetValue( true );
		      
				Console.WriteLine( "Error setting value #" + test );
				return;
			}
			catch( Exception ) 
			{
				if ( ((double)incFxd.Value) == (double)12 ) 
				{
					Console.WriteLine( "Passed #" + test );
				}
				else 
				{
					Console.WriteLine( "Error setting value #" + test );
					return;
				}
			}

			FixedPtSpace clnFxd = (FixedPtSpace)incFxd.Clone();
			
			// Test #11
			test++; 
			try 
			{
				clnFxd.SetValue( 5.3 );
		      
				Console.WriteLine( "Error setting value #" + test );
				return;
			}
			catch( Exception ) 
			{
				if ( ((double)clnFxd.Value) == (double)12 ) 
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
				clnFxd.SetValue( (double)6 );
		      
				if ( ((double)clnFxd.Value) == (double)6 ) 
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
				clnFxd.SetValue( (double)11 );
		      
				Console.WriteLine( "Error setting value #" + test );
				return;
			}
			catch( Exception ) 
			{
				if ( ((double)clnFxd.Value) == (double)6 ) 
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
				clnFxd.SetValue( (double)15 );
		      
				Console.WriteLine( "Error setting value #" + test );
				return;
			}
			catch( Exception ) 
			{
				if ( ((double)clnFxd.Value) == (double)6 ) 
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
				clnFxd.SetValue( (double)12 );
		      
				if ( ((double)clnFxd.Value) == (double)12 ) 
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
				clnFxd.SetValue( "0.9" );
		      
				if ( ((double)clnFxd.Value) == 0.9 ) 
				{
					Console.WriteLine( "Passed #" + test );
				}
				else throw new System.NullReferenceException( "dumb" );
			}
			catch( Exception e ) 
			{
				Console.WriteLine( e.Message );
				Console.WriteLine( "Error setting value #" + test );
				return;
			}
			
			
			// Test #17
			test++; 
			try 
			{
				regFxd.SetValue( 5.6 );
				rngFxd.SetValue( 5.6 );
		      
				if ( regFxd.CompareValues( rngFxd ) == 0 ) 
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

			Console.WriteLine( "FixedPtSpace PASSED" );
		}*/
	}
}
