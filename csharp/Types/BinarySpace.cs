using System;
using System.Collections;

namespace PUC.Types
{
	/// <summary>
	/// Summary description for StringSpace.
	/// </summary>
	public class BinarySpace : ValueSpace
	{
		/*
		 * Constants
		 */
	
		


		/*
		 * Member Variables
		 */
	
		protected Hashtable _paramTable;


		/*
		 * Constructor
		 */
		/// <summary>
		/// Constructor that initializes BinarySpace
		/// </summary>
		public BinarySpace()
		{
			_paramTable = new Hashtable();
		}


		/*
		 * Indexor to access the parameter table
		 */

		public object this[ string param ]
		{
			get
			{
				return _paramTable[ param ];
			}
			set
			{
				_paramTable[ param ] = value;
			}
		}

		
		/*
		 * Overridden Methods
		 */
		/// <summary>
		/// Returns a shallow copy of this BinarySpace.
		/// </summary>
		/// <returns>a shallow copy of this BinarySpace</returns>
		public override object Clone()
		{
			BinarySpace bspc = new BinarySpace();

			IEnumerator e = _paramTable.GetEnumerator();
			while( e.MoveNext() )
				bspc._paramTable[ ((DictionaryEntry)e.Current).Key ] = ((DictionaryEntry)e.Current).Value;

			return bspc;
		}

		/// <summary>
		/// Returns "binary" 
		/// </summary>
		/// <returns>"binary"</returns>
		public override string Name
		{
			get
			{
				return "binary";
			}
		}

		/// <summary>
		/// Returns ValueSpace.BINARY_SPACE.
		/// </summary>
		/// <returns>ValueSpace.BINARY_SPACE</returns>
		public override int Space
		{
			get
			{
				return ValueSpace.BINARY_SPACE;
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
			return val;
		}

		/// <summary>
		/// Compares the values of two BinarySpaces.  This method returns 0 always, as there is no
		/// good, efficient way of comparing pieces of binary data.
		/// </summary>
		/// <param name="space">the space to compare to this one</param>
		/// <returns>0 if the value of the two sets are equals, 
		/// -1 if the value passed in is less than this space's value</returns>
		public override int CompareValues( object val1, object val2 )
		{
			return 0;
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
			if ( space is BinarySpace )
			{
				BinarySpace bspace = (BinarySpace)space;

				if ( bspace._paramTable.Count != _paramTable.Count )
					return false;

				IEnumerator param = _paramTable.GetEnumerator();
				while ( param.MoveNext() )
				{
					DictionaryEntry entry = (DictionaryEntry)param.Current;

					if ( !bspace._paramTable.ContainsKey( entry.Key ) ||
						 bspace._paramTable[ entry.Key ] != entry.Value )
						return false;
				}

				return true;
			}

			return false;
		}
	}
}
