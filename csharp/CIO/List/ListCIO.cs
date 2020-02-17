using System;
using System.Collections;

using PUC;
using PUC.CIO;

namespace PUC.CIO.List
{
	/// <summary>
	/// Summary description for ListCIO.
	/// </summary>
	public class ListCIO
	{
		/*
		 * Static Member Variables
		 */

		protected static Hashtable _cioFactories;
		public delegate ConcreteInteractionObject CreateCIO( PUC.ListGroupNode g );


		/*
		 * Static Constructor
		 */
		static ListCIO()
		{
			_cioFactories = new Hashtable();

#if POCKETPC || DESKTOP
			AddCIOFactory( OneDimCategoricalList.CIO_NAME,
				new CreateCIO(OneDimCategoricalList.CreateListCIO) );
			AddCIOFactory( DataGridListCIO.CIO_NAME, 
				new CreateCIO(DataGridListCIO.CreateListCIO) );
#endif
		}

		public static void AddCIOFactory( String name, CreateCIO fptr )
		{
			_cioFactories[ name ] = fptr;
		}

		public static CreateCIO GetCIOFactory( String name )
		{
			return (CreateCIO)_cioFactories[ name ];
		}
	}
}
