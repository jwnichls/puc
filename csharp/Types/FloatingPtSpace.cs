using System;

namespace PUC.Types
{
	/// <summary>
	/// A ValueSpace that stores floating point values that may be ranged or
	/// incremented.
	/// </summary>
	public class FloatingPtSpace : NumberSpace
	{
		/*
		 * Member Variables
		 */

		/// <summary>
		/// Whether this FloatingPtSpace has a range.
		/// </summary>
		protected bool _ranged;

		/// <summary>
		/// The inclusive minimum of this FloatingPtSpace's range.
		/// </summary>
		protected IPUCNumber _min;

		/// <summary>
		/// The inclusive maximum of this FloatingPtSpace's range.
		/// </summary>
		protected IPUCNumber _max;


		/*
		 * Constructors
		 */

		/// <summary>
		/// Class constructor.
		/// </summary>
		public FloatingPtSpace()
		{
			_ranged = false;
		}

		/// <summary>
		/// Class constructor that creates a ranged FloatingPtSpace.
		/// </summary>
		/// <param name="min">the inclusive minimum of the range</param>
		/// <param name="max">the inclusive maximum of the range</param>
		public FloatingPtSpace( IPUCNumber min, IPUCNumber max )
		{
			_ranged = true;
			_min = min;
			_max = max;

			if ( _min.GetDoubleValue() >= _max.GetDoubleValue() && 				
				!( _min is NumberConstraint ) &&
				!( _max is NumberConstraint ) )
				throw new ArgumentException( "FloatingPtSpace: minimum must be less than maximum" );
		}


		/*
		 * Overridden Methods
		 */
		/// <summary>
		/// Returns a string descriptor of this FloatingPtSpace.
		/// </summary>
		/// <returns>a string descriptor of this FloatingPtSpace</returns>
		public override string ToString() 
		{
			string ret = base.ToString();

			if ( _ranged ) 
			{	
				ret += " : max = " + _max + " min = " + _min;
			}

			return ret;
		}

		/// <summary>
		/// Returns a shallow copy of this FloatingPtSpace.
		/// </summary>
		/// <returns>a shallow copy of this FloatingPtSpace</returns>
		public override object Clone()
		{
			FloatingPtSpace clone = new FloatingPtSpace();

			clone._ranged = _ranged;
			clone._min = _min;
			clone._max = _max;

			return clone;
		}


		/// <summary>
		/// Returns "floating point" 
		/// </summary>
		/// <returns>"floating point"</returns>
		public override string Name
		{
			get
			{
				return "floating point";
			}
		}

		/// <summary>
		/// Returns ValueSpace.FLOATING_PT_SPACE.
		/// </summary>
		/// <returns>ValueSpace.FLOATING_PT_SPACE</returns>
		public override int Space
		{
			get
			{
				return ValueSpace.FLOATING_PT_SPACE;
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
				return val;

			return null;
		}

		/// <summary>
		/// Compares the values of two FloatingPtSpaces.  
		/// </summary>
		/// <param name="space">the space to compare to this one</param>
		/// <returns>0 if the value of the two sets are equals, 
		/// 1 if the value passed in is greater than this space's value,
		/// -1 if the value passed in is less than this space's value</returns>
		/// <exception cref="InvalidCastException">thrown if space is not an instance of FloatingPtSpace</exception>
		public override int CompareValues( object val1, object val2 )
		{
			if ( val1 != null && val2 != null ) return 0;
			else if ( val1 == null ^ val2 == null ) return -1;

			double fltVal1 = (double)val1;
			double fltVal2 = (double)val2;

			if ( fltVal1 == fltVal2 ) return 0;
			else if ( fltVal1 > fltVal2 ) return 1;
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
			double ret = Double.Parse( val );

			if (! validateNoStrCheck( ret ) )
				throw new FormatException( "convertString: value not within correct range" );

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
			if ( val is double ) 
			{
				double dblval = (double)val;

				return !_ranged || ( dblval >= _min.GetDoubleValue() && 
									 dblval <= _max.GetDoubleValue() );
			}

			return false;
		}

		/// <summary>
		/// Returns whether this FloatingPtSpace is ranged.
		/// </summary>
		/// <returns>whether this FloatingPtSpace is ranged</returns>
		public bool IsRanged()
		{
			return _ranged;
		}

		/// <summary>
		/// Returns the inclusive minimum range of this FloatingPtSpace.  This 
		/// value is meaningless if this FloatingPtSpace is not ranged.
		/// </summary>
		/// <returns>the inclusive minimum range of this FloatingPtSpace</returns>
		public IPUCNumber GetMinimum()
		{
			return _min;
		}

		/// <summary>
		/// Returns the inclusive maximum range of this FloatingPtSpace.  This 
		/// value is meaningless if this FloatingPtSpace is not ranged.
		/// </summary>
		/// <returns>the inclusive maximum range of this FloatingPtSpace</returns>
		public IPUCNumber GetMaximum()
		{
			return _max;
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
			}
		}

		public override bool IsSameSpace(ValueSpace space)
		{
			if ( space is FloatingPtSpace )
			{
				FloatingPtSpace fs = (FloatingPtSpace)space;

				if ( fs.IsRanged() && this.IsRanged() )
				{
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
			Console.WriteLine( "FloatingPtSpace Test Sequence" );
			Console.WriteLine( "-----------------------------" );

			FloatingPtSpace regDbl  = new FloatingPtSpace();
			FloatingPtSpace rngDbl  = new FloatingPtSpace( new DoubleNumber( 0 ), 
														   new DoubleNumber( 10 ) );

			int test = 0;

			// Test #1 
			test++; 
			try 
			{
				regDbl.SetValue( 5.3 );
		      
				if ( ((double)regDbl.Value) == 5.3 ) 
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
				rngDbl.SetValue( 5.3 );
		      
				if ( ((double)rngDbl.Value) == 5.3 ) 
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
				rngDbl.SetValue( (double)11 );
		      
				Console.WriteLine( "Error setting value #" + test );
				return;
			}
			catch( Exception ) 
			{
				if ( ((double)rngDbl.Value) == 5.3 ) 
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
				rngDbl.SetValue( (double)10 );
		      
				if ( ((double)rngDbl.Value) == (double)10 ) 
				{
					Console.WriteLine( "Passed #" + test );
				}
				else throw new System.NullReferenceException( "dumb" );
			}
			catch( Exception e ) 
			{
				Console.WriteLine( e.Message );
				Console.WriteLine( "Error setting value #" + test + " to " + (double)regDbl.Value);
				return;
			}
			
			// Test #5 
			test++; 
			try 
			{
				regDbl.SetValue( (double)11 );
		      
				if ( ((double)regDbl.Value) == (double)11 ) 
				{
					Console.WriteLine( "Passed #" + test );
				}
				else throw new System.NullReferenceException( "dumb" );
			}
			catch( Exception e) 
			{
				Console.WriteLine( e.Message );
				Console.WriteLine( "Error setting value #" + test + " to " + (double)regDbl.Value);
				return;
			}

			// Test #6 
			test++; 
			try 
			{
				regDbl.SetValue( true );
		      
				Console.WriteLine( "Error setting value #" + test );
				return;
			}
			catch( Exception ) 
			{
				if ( ((double)regDbl.Value) == (double)11 ) 
				{
					Console.WriteLine( "Passed #" + test );
				}
				else 
				{
					Console.WriteLine( "Error setting value #" + test );
					return;
				}
			}

			FloatingPtSpace clnDbl = (FloatingPtSpace)rngDbl.Clone();
			
			// Test #7 
			test++; 
			try 
			{
				clnDbl.SetValue( 11 );
		      
				Console.WriteLine( "Error setting value #" + test );
				return;
			}
			catch( Exception ) 
			{
				if ( ((double)clnDbl.Value) == (double)10 ) 
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
				clnDbl.SetValue( 5.3 );
		      
				if ( ((double)clnDbl.Value) == 5.3 ) 
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
			
			// Test #9 
			test++; 
			try 
			{
				clnDbl.SetValue( (double)10 );
		      
				if ( ((double)clnDbl.Value) == 10 ) 
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
				clnDbl.SetValue( "10" );
		      
				if ( ((double)clnDbl.Value) == 10 ) 
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
			
			// Test #11 
			test++; 
			try 
			{
				clnDbl.SetValue( "abcd" );

				Console.WriteLine( "Error setting value #" + test );
				return;
			}
			catch( Exception ) 
			{		      
				if ( ((double)clnDbl.Value) == (double)10 ) 
				{
					Console.WriteLine( "Passed #" + test );
				}
				else 
				{
					Console.WriteLine( "Error setting value #" + test );
					return;
				}
			}

			Console.WriteLine( "FloatingPtSpace PASSED" );
		}*/	
	}
}
