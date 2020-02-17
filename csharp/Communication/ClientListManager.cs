using System;

namespace PUC.Communication
{
	/// <summary>
	/// Summary description for ListManager.
	/// </summary>
	public abstract class ListManager
	{
		public static int IncrementListVersion( Connection c, String listName )
		{
			throw new NotSupportedException( "This method should never be called by the client." );
		}

		public static bool CompareListVersions( Connection c, String listName, int version )
		{
			throw new NotSupportedException( "This method should never be called by the client." );
		}
	}
}
