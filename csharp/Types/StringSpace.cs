using System;

namespace PUC.Types
{
	/// <summary>
	/// Summary description for StringSpace.
	/// </summary>
	public class StringSpace : ValueSpace
	{
		/*
		 * Member Variables
		 */

		protected IPUCNumber _minCharacters;
		protected IPUCNumber _aveCharacters;
		protected IPUCNumber _maxCharacters;


		/*
		 * Constructor
		 */

		public StringSpace( IPUCNumber minChars, IPUCNumber aveChars, IPUCNumber maxChars )
		{
			_minCharacters = minChars;
			_aveCharacters = aveChars;
			_maxCharacters = maxChars;
		}

		public StringSpace( IPUCNumber minChars, IPUCNumber maxChars )
			: this( minChars, null, maxChars )
		{
		}

		public StringSpace( IPUCNumber maxChars )
			: this( null, null, maxChars )
		{
		}

		/// <summary>
		/// Constructor that initializes space with no character length
		/// information.
		/// </summary>
		public StringSpace()
			: this( null, null, null )
		{
		}


		/*
		 * Properties
		 */

		public IPUCNumber MinimumChars
		{
			get
			{
				return _minCharacters;
			}
			set
			{
				_minCharacters = value;
			}
		}

		public IPUCNumber AverageChars
		{
			get
			{
				return _aveCharacters;
			}
			set
			{
				_aveCharacters = value;
			}
		}

		public IPUCNumber MaximumChars
		{
			get
			{
				return _maxCharacters;
			}
			set
			{
				_maxCharacters = value;
			}
		}


		/*
		 * Overridden Methods
		 */
		/// <summary>
		/// Returns a shallow copy of this StringSpace.
		/// </summary>
		/// <returns>a shallow copy of this StringSpace</returns>
		public override object Clone()
		{
			return new StringSpace( _minCharacters, _aveCharacters, _maxCharacters );
		}

		/// <summary>
		/// Returns "string" 
		/// </summary>
		/// <returns>"string"</returns>
		public override string Name
		{
			get
			{
				return "string";
			}
		}

		/// <summary>
		/// Returns ValueSpace.STRING_SPACE.
		/// </summary>
		/// <returns>ValueSpace.STRING_SPACE</returns>
		public override int Space
		{
			get
			{
				return ValueSpace.STRING_SPACE;
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
				return val;

			return null;
		}

		/// <summary>
		/// Compares the values of two StringSpaces.  
		/// </summary>
		/// <param name="space">the space to compare to this one</param>
		/// <returns>0 if the value of the two sets are equals, 
		/// -1 if the value passed in is less than this space's value</returns>
		/// <exception cref="InvalidCastException">thrown if space is not an instance of StringSpace</exception>
		public override int CompareValues( object val1, object val2 )
		{
			if ( val1 == null && val2 == null ) return 0;
			else if ( val1 != null ^ val2 != null ) return -1;

			if ( ((string)val1) == ((string)val2) ) return 0;
			else return -1;
		}

		/// <summary>
		/// Register any constraints in this type with the appropriate 
		/// state variable.
		/// </summary>
		/// <param name="state"></param>
		public override void RegisterConstraints(ApplianceState state)
		{
			if ( _minCharacters != null && _minCharacters is NumberConstraint )
				new TypeConstraintListener( state, (NumberConstraint)_minCharacters );

			if ( _maxCharacters != null && _maxCharacters is NumberConstraint )
				new TypeConstraintListener( state, (NumberConstraint)_maxCharacters );

			if ( _aveCharacters != null && _aveCharacters is NumberConstraint )
				new TypeConstraintListener( state, (NumberConstraint)_aveCharacters );
		}

		public override bool IsSameSpace(ValueSpace space)
		{
			if ( space is StringSpace )
			{
				StringSpace sspace = (StringSpace)space;
		
				if ( sspace._minCharacters != null && _minCharacters != null )
				{
					if ( !sspace._minCharacters.IsSameValue( _minCharacters ) )
						return false;
				}
				else if ( sspace._minCharacters != _minCharacters )
					// return false if both aren't null
					return false;

				if ( sspace._maxCharacters != null && _maxCharacters != null )
				{
					if ( !sspace._maxCharacters.IsSameValue( _maxCharacters ) )
						return false;
				}
				else if ( sspace._maxCharacters != _maxCharacters )
					// return false if both aren't null
					return false;

				if ( sspace._aveCharacters != null && _aveCharacters != null )
				{
					if ( !sspace._aveCharacters.IsSameValue( _aveCharacters ) )
						return false;
				}
				else if ( sspace._aveCharacters != _aveCharacters )
					// return false if both aren't null
					return false;

				return true;
			}

			return false;
		}



		/*
		 * Test Method
		 *
		public static void Main()
		{
			Console.WriteLine( "StringSpace Test Sequence" );
			Console.WriteLine( "-------------------------" );

			StringSpace regStr = new StringSpace();

			int test = 0;

		    // Test #1
			test++; 
			try {
			regStr.SetValue( "hello" );
		      
				if ( ((string)regStr.Value) == "hello" ) {
					Console.WriteLine( "Passed #" + test );
				}
				else throw new System.NullReferenceException( "dumb" );
			}
			catch( Exception ) {
				Console.WriteLine( "Error setting value #" + test );
				return;
			}

			// Test #2
			test++; 
			try {
				regStr.SetValue( "dumb" );
		      
				if ( ((string)regStr.Value) == "dumb" ) {
					Console.WriteLine( "Passed #" + test );
				}
				else throw new NullReferenceException( "dumb" );
			}
			catch( Exception ) {
				Console.WriteLine( "Error setting value #" + test );
				return;
			}

			// Test #3
			test++; 
			try {
				regStr.SetValue( 5 );
		      
				Console.WriteLine( "Error setting value #" + test );
				return;
			}
			catch( Exception ) {
				if ( ((string)regStr.Value) == "dumb"  ) {
					Console.WriteLine( "Passed #" + test );
				}
				else {
					Console.WriteLine( "Error setting value #" + test );
					return;
				}
			}

			StringSpace clnStr = (StringSpace)regStr.Clone();

			// Test #4
			test++;
			if ( ((string)clnStr.Value) == "dumb" ) {
				Console.WriteLine( "Passed #" + test );
			}
			else {
				Console.WriteLine( "Error setting value #" + test );
				return;
			}

			// Test #5
			test++;
			try 
			{
				if ( clnStr.CompareValues( regStr ) != 0 )
					throw new NullReferenceException( "dumb" );

				Console.WriteLine( "Passed #" + test );
			}
			catch( Exception ) 
			{
				Console.WriteLine( "Error comparing values at #" + test );
				Console.WriteLine( "Got value " + clnStr.CompareValues( regStr ) +
					" " + clnStr.Value.ToString() + " " +
					regStr.Value.ToString() );
				return;
			}

			// Test #6
			test++; 
			try {
				clnStr.SetValue( "hello" );
		      
				if ( ((string)clnStr.Value) == "hello" ) {
					Console.WriteLine( "Passed #" + test );
				}
				else throw new NullReferenceException( "dumb" );
			}
			catch( Exception ) {
				Console.WriteLine( "Error setting value #" + test );
				return;
			}

			Console.WriteLine( "StringSpace PASSED" );
		}*/
	}
}
