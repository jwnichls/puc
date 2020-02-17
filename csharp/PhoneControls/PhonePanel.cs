using System;
using System.Windows.Forms;

namespace PhoneControls
{
	/// <summary>
	/// This is a custom panel for the phone that reports an 
	/// event when the back button is pressed.
	/// </summary>
	public class PhonePanel : Control
	{
		/*
		 * Constants
		 */

		protected const char   BACK_KEY_CHAR     = (char)27;


		/*
		 * Events
		 */

		public event KeyPressEventHandler BackButtonPressed;


		/*
		 * Constructor
		 */

		public PhonePanel() 
		{
			this.KeyPress += new KeyPressEventHandler(this.keyPress);
		}


		/*
		 * KeyPressHandler
		 */

		protected void keyPress( object source, KeyPressEventArgs e )
		{
			if ( e.KeyChar == BACK_KEY_CHAR )
			{
				MessageBox.Show( "panel back pressed!" );

				if ( BackButtonPressed != null )
					BackButtonPressed( this, e );
			}
		}
	}
}
