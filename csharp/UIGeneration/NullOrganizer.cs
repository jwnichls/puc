using System;
using System.Collections;
using PUC;

namespace PUC.UIGeneration
{
	/// <summary>
	/// This organizer represents a lack of organization.  It is
	/// the default organizer for all groups.
	/// 
	/// To eliminate object creation overhead, the NullOrganizer is 
	/// created as a Singleton object, and the same object is referenced 
	/// wherever a NullOrganizer is needed.  To get a NullOrganizer 
	/// object, use the static method GetTheNullOrganizer().
	/// </summary>
	public class NullOrganizer : PUC.IOrganizer
	{
		/*
		 * Member Variables
		 */

		protected static NullOrganizer _nullOrganizer;


		/*
		 * Static Methods
		 */

		static NullOrganizer()
		{
			_nullOrganizer = new NullOrganizer();
		}

		public static NullOrganizer GetTheNullOrganizer()
		{
			return _nullOrganizer;
		}


		/*
		 * Constructor
		 */

		protected NullOrganizer()
		{
		}


		/*
		 * IOrganizer Methods
		 */

		public Hashtable AddOrganization( GroupNode group, InterfaceNode currentNode )
		{
			return null;
		}
	}
}
