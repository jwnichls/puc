using System;
using System.Collections;

using PUC;


namespace PUC.CIO
{
	/// <summary>
	/// The SmartCIOManager manages the SmartCIOs that are installed
	/// in the PUC system.  Because there is no reasonable method of
	/// dynamic linking within .NET, the list of SmartCIOs is specified
	/// in code by the IPUCFrame object which initializes the 
	/// SmartCIOManager.
	/// </summary>
	public class SmartCIOManager
	{
		/*
		 * Member Variables
		 */

		protected Hashtable _cioMap;


		/*
		 * Constructor
		 */

		public SmartCIOManager()
		{
			_cioMap = new Hashtable();
		}


		/*
		 * Member Methods
		 */

		public void AddSmartCIO( String type, SmartCIO.CreateSmartCIO factory ) 
		{
			_cioMap[ type ] = factory;
		}

		public SmartCIO GetSmartCIO( String type, GroupNode g ) 
		{
			SmartCIO.CreateSmartCIO cioFactory = (SmartCIO.CreateSmartCIO)_cioMap[ type ];

			if ( cioFactory == null )
				return null;
			else
				return cioFactory( g );
		}
	}
}
