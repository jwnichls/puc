using System;

namespace PUC.Types
{
	/// <summary>
	/// Summary description for EnumeratedSpace.
	/// </summary>
	public class EnumeratedSpace : NumberSpace
	{
		/*
		 * Member Variables
		 */

		/// <summary>
		/// The number of items in this EnumeratedSpace.
		/// </summary>
		protected int _itemcount;


		/*
		 * Constructor
		 */
		public EnumeratedSpace( int itemcount )
		{
			if ( itemcount <= 0 )
				throw new ArgumentException( "EnumeratedSpace must have more than 0 items" );

			_itemcount = itemcount;
		}

		/*
		 * Overridden Methods
		 */
		/// <summary>
		/// Returns a shallow copy of this EnumeratedSpace.
		/// </summary>
		/// <returns>a shallow copy of this EnumeratedSpace</returns>
		public override object Clone()
		{
			return new EnumeratedSpace( _itemcount );
		}


		/// <summary>
		/// Returns "enumerated" 
		/// </summary>
		/// <returns>"enumerated"</returns>
		public override string Name
		{
			get
			{
				return "enumerated";
			}
		}

		/// <summary>
		/// Returns ValueSpace.ENUMERATED_SPACE.
		/// </summary>
		/// <returns>ValueSpace.ENUMERATED_SPACE</returns>
		public override int Space
		{
			get
			{
				return ValueSpace.ENUMERATED_SPACE;
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
				return (int)val;

			return null;
		}

		/// <summary>
		/// Compares the values of two EnumeratedSpaces.  
		/// </summary>
		/// <param name="space">the space to compare to this one</param>
		/// <returns>0 if the value of the two sets are equals, 
		/// -1 if the value passed in is less than this space's value</returns>
		/// <exception cref="InvalidCastException">thrown if space is not an instance of EnumeratedSpace</exception>
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

				return intval > 0 && intval <= _itemcount;									
			}

			return false;
		}

		/// <summary>
		/// Returns the number of items in this EnumeratedSpace.
		/// </summary>
		/// <returns>the number of items in this EnumeratedSpace</returns>
		public int GetItemCount()
		{
			return _itemcount;
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
			return space is EnumeratedSpace && ((EnumeratedSpace)space).GetItemCount() == this._itemcount;
		}


		/*
		 * Test Method
		 *
		public static void Main()
		{
			Console.WriteLine( "EnumeratedSpace Test Sequence" );
			Console.WriteLine( "-----------------------------" );

			EnumeratedSpace regEnum  = new EnumeratedSpace( 4 );
			EnumeratedSpace regEnum2 = new EnumeratedSpace( 5 );

			int test = 0;

			// Test #1
			test++; 
			try 
			{
				regEnum.SetValue( 4 );
		      
				if ( ((int)regEnum.Value) == 4 ) 
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
				regEnum.SetValue( 5 );
		      
				Console.WriteLine( "Error setting value #" + test );
				return;
			}
			catch( Exception ) 
			{
				if ( ((int)regEnum.Value) == 4 ) 
				{
					Console.WriteLine( "Passed #" + test );
				}
				else 
				{
					Console.WriteLine( "Error setting value #" + test );
					return;
				}
			}

			// Test #3
			test++; 
			try 
			{
				regEnum.SetValue( 0 );
		      
				Console.WriteLine( "Error setting value #" + test );
				return;
			}
			catch( Exception ) 
			{
				if ( ((int)regEnum.Value) == 4 ) 
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
				regEnum.SetValue( 3 );
				regEnum2.SetValue( 3 );
		      
				regEnum.CompareValues( regEnum2 );

				Console.WriteLine( "(1) Error setting value #" + test );
				return;
			}
			catch( Exception ) 
			{
				if ( ((int)regEnum.Value) == 3 ) 
				{
					Console.WriteLine( "Passed #" + test );
				}
				else 
				{
					Console.WriteLine( "(2) Error setting value #" + test );
					return;
				}
			}

			// Test #5
			test++; 
			try 
			{
				regEnum2 = new EnumeratedSpace( 4 );
				regEnum2.SetValue( 3 );
		      
				if ( regEnum.CompareValues( regEnum2 ) == 0 ) 
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

			Console.WriteLine( "EnumeratedSpace PASSED" );
		}*/
	}
}
