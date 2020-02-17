using System;

namespace PUC.CIO
{
	/// <summary>
	/// Summary description for IndependentCIO.
	/// </summary>
	public abstract class IndependentCIO : ControlBasedCIO
	{
		/*
		 * Constructor
		 */
		public IndependentCIO( System.Windows.Forms.Control control ) : base( control )
		{
		}
	}
}
