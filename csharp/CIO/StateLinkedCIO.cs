using System;

namespace PUC.CIO
{
	/// <summary>
	/// Summary description for StateLinkedCIO.
	/// </summary>
	public abstract class StateLinkedCIO : ControlBasedCIO
	{
		/*
		 * Member Variables
		 */
		protected PUC.ApplianceObject _applObj;


		/*
		 * Constructor
		 */
		public StateLinkedCIO( PUC.ApplianceObject applObj,
							   System.Windows.Forms.Control control )
			: base( control )
		{
			_applObj = applObj;

			control.Enabled = _applObj.Enabled;
		}


		/*
		 * Member Methods
		 */
		public PUC.ApplianceObject GetApplObj()
		{
			return _applObj;
		}
	}
}
