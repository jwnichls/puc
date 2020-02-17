using System;
using System.Collections;

namespace PUC.Communication
{
	public class ListData : ICloneable
	{
		public string ListName;
		public int    Version;
		
		public ListData( string listName, int version )
		{
			ListName = listName;
			Version = version;
		}

		public object Clone()
		{
			return new ListData( this.ListName, this.Version );
		}
	}

	/// <summary>
	/// Summary description for ListManager.
	/// </summary>
	public abstract class ListManager
	{
		/*
		 * Member Variables
		 */
		protected static Hashtable _data;

		/*
		 * Static Constructor
		 */
		static ListManager()
		{
			_data = new Hashtable();
		}

		/*
		 * Static Methods
		 */
		public static int IncrementListVersion( Connection c, String listName )
		{
			Hashtable connTable = (Hashtable)_data[ c ];
			if ( connTable == null )
			{
				connTable = new Hashtable();
				_data[ c ] = connTable;
			}

			ListData data = (ListData)connTable[ listName ];
			if ( data == null )
			{	
				data = new ListData( listName, -1 );
				connTable[ listName ] = data;
			}

			return ++data.Version;
		}

		protected static ListData GetListVersion( Connection c, String listName )
		{
			Hashtable connTable = (Hashtable)_data[ c ];
			if ( connTable == null )
				return null;

			ListData data = (ListData)connTable[ listName ];
			if ( data == null )
				return null;

			return (ListData)data.Clone();
		}

		public static bool CompareListVersions( Connection c, String listName, int version )
		{
			ListData data = GetListVersion( c, listName );

			if ( data == null ) return false;

			return data.Version == version;
		}
	}
}
