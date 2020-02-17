using System;
using System.Drawing;
using System.Net;
using System.Windows.Forms;

namespace PUC
{
	/// <summary>
	/// Summary description for IPUCFrame.
	/// </summary>
	public interface IPUCFrame : ILogManager
	{
		void AddActiveAppliance( Appliance a );
		void SetCurrentAppliance( Appliance a );
		void RemoveAppliance( Appliance a );
	}

	public class MeasureStringControl : System.Windows.Forms.Control
	{
		public System.Drawing.SizeF MeasureString( string str, Font f )
		{
			try
			{
				Graphics g = this.CreateGraphics();
				SizeF s = g.MeasureString( str, f );
				g.Dispose();

				return s;
			}
			catch( Exception )
			{
				return new SizeF( 0, 0 );
			}
		}
	}

	public class DeviceSwitcher
	{
		/*
		 * Member Variables
		 */

		protected Appliance _appliance;


		/*
		 * Constructor
		 */

		public DeviceSwitcher( Appliance a )
		{
			_appliance = a;
		}


		/*
		 * Event Handler
		 */

		public void DeviceMenuClicked( object source, EventArgs a )
		{
			Globals.GetFrame( _appliance ).SetCurrentAppliance( _appliance );
		}
	}
}
