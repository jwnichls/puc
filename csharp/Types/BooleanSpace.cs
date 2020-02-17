using System;

namespace PUC.Types
{
	/// <summary>
	/// Summary description for BooleanSpace.
	/// </summary>
	public class BooleanSpace : ValueSpace
	{
		/*
		 * Member Variables
		 */

		/*
		 * Overridden Methods
		 */
		/// <summary>
		/// Returns a shallow copy of this BooleanSpace.
		/// </summary>
		/// <returns>a shallow copy of this BooleanSpace</returns>
		public override object Clone()
		{
			return new BooleanSpace();
		}

		/// <summary>
		/// Returns "boolean" 
		/// </summary>
		/// <returns>"boolean"</returns>
		public override string Name
		{
			get
			{
				return "boolean";
			}
		}

		/// <summary>
		/// Returns ValueSpace.BOOLEAN_SPACE.
		/// </summary>
		/// <returns>ValueSpace.BOOLEAN_SPACE</returns>
		public override int Space
		{
			get
			{
				return ValueSpace.BOOLEAN_SPACE;
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
					bool boolval = Boolean.Parse( (string)val );
					return boolval;
				}
				catch(FormatException) 
				{
					return null;
				}
			}
			else if ( val is bool )
				return (bool)val;

			return null;
		}

		/// <summary>
		/// Compares the values of two BooleanSpaces.  
		/// </summary>
		/// <param name="space">the space to compare to this one</param>
		/// <returns>0 if the value of the two sets are equals, 
		/// -1 if the value passed in is not equal</returns>
		/// <exception cref="InvalidCastException">thrown if space is not an instance of BooleanSpace</exception>
		public override int CompareValues( object val1, object val2 )
		{
			if ( val1 == null && val2 == null ) return 0;
			else if ( val1 != null ^ val2 != null ) return -1;

			if ( ((bool)val1) == ((bool)val2) ) return 0;
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
			return Boolean.Parse( val );
		}

		/// <summary>
		/// Register any constraints in this type with the appropriate 
		/// state variable.
		/// </summary>
		/// <param name="state"></param>
		public override void RegisterConstraints(ApplianceState state)
		{
			// do nothing...there can't be any constraints in this class
		}

		public override bool IsSameSpace(ValueSpace space)
		{
			return space is BooleanSpace;
		}


		/*
		 * Test Method
		 *
		public static void Main()
		{
			Console.WriteLine( "BooleanSpace Test Sequence" );
			Console.WriteLine( "--------------------------" );

			BooleanSpace regBool  = new BooleanSpace();
			BooleanSpace regBool2 = new BooleanSpace();

			int test = 0;

			// Test #1
			test++; 
			try 
			{
				regBool.SetValue( true );
		      
				if ( ((bool)regBool.Value) == true ) 
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
				regBool2.SetValue( false );
		      
				if ( ((bool)regBool2.Value) == false ) 
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
				regBool.SetValue( false );
		      
				if ( ((bool)regBool.Value) == false ) 
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

			// Test #4
			test++; 
			try 
			{
				regBool.SetValue( 5 );
		      
				Console.WriteLine( "Error setting value #" + test );
				return;
			}
			catch( Exception ) 
			{
				if ( ((bool)regBool.Value) == false ) 
				{
					Console.WriteLine( "Passed #" + test );
				}
				else 
				{
					Console.WriteLine( "Error setting value #" + test );
					return;
				}
			}

			// Test #5
			test++; 
			try 
			{
				regBool.SetValue( "true" );
		      
				if ( ((bool)regBool.Value) == true ) 
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

			// Test #6
			test++; 
			try 
			{
				regBool2.SetValue( "false" );
		      
				if ( ((bool)regBool2.Value) == false ) 
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

			test++; 
			// Test #7 
			try 
			{
				regBool.SetValue( "1" );
		      
				if ( ((bool)regBool.Value) == true ) 
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

			test++; 
			// Test #8
			try 
			{
				regBool2.SetValue( "0" );
		      
				if ( ((bool)regBool2.Value) == false ) 
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
				regBool2.SetValue( true );
				regBool.SetValue( true );
		      
				if ( regBool2.CompareValues( regBool ) == 0 ) 
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

			Console.WriteLine( "BooleanSpace PASSED" );
		}*/
	}
}
