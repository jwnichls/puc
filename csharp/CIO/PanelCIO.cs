using System;
using System.Windows.Forms;

using PUC.Layout;


namespace PUC.CIO
{
	/// <summary>
	/// Summary description for PanelCIO.
	/// </summary>
	public class PanelCIO : ContainerCIO
	{
		/*
		 * Member Variables
		 */


		/*
		 * Constructor
		 */

		public PanelCIO() : base( new System.Windows.Forms.Panel() )
		{			
		}

		public PanelCIO( Control customPanel )
			: base( customPanel )
		{
		}


		/*
		 * Member Methods
		 */

		public override System.Drawing.Size GetMinimumSize()
		{
			return new System.Drawing.Size( 0, 0 );
		}

		public override PUC.Layout.PreferredSize GetPreferredSize()
		{
			return new PUC.Layout.PreferredSize( GetMinimumSize() );
		}

		public override System.Drawing.Point GetControlOffset()
		{
			return new System.Drawing.Point( 0, 0 );
		}
	}
}
