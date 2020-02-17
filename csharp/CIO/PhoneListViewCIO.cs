using System;
using System.Collections;
using System.Windows.Forms;

using PhoneControls;

using PUC;
using PUC.Types;


namespace PUC.CIO
{
	/// <summary>
	/// A concrete interaction object for the standard list views
	/// seen on the phone.  These views have 9 items, all mapped to
	/// the physical buttons on the phone.  If the list includes more
	/// than nine items, the ninth item is reserved for "More..."
	/// item which goes to the next list with the rest of the items.
	/// </summary>
	public class PhoneListViewCIO : IndependentCIO
	{
		/*
		 * Member Variables
		 */


		/*
		 * Constructor
		 */

		public PhoneListViewCIO()
			: base( new PhoneListView() )
		{
			((PhoneListView)GetControl()).BackButtonHandled += new EventHandler(this.BackButtonHandler);
			((PhoneListView)GetControl()).MoreItemActivated += new EventHandler(this.HandleMoreItemActivate);
		}


		/*
		 * Protected Helper Methods
		 */

		protected void BackButtonHandler( object src, EventArgs e )
		{
			Globals.PopLeftMenuStack();
		}

		protected void HandleMoreItemActivate( object src, EventArgs e )
		{
			PhonePUC.LeftMenuStackItem item = new PhonePUC.LeftMenuStackItem( "Back", new EventHandler(this.menuButtonPress) );
			Globals.PushLeftMenuStack( item );			
		}

		protected void menuButtonPress( object source, EventArgs e )
		{
			Globals.PopLeftMenuStack();
			((PhoneListView)GetControl()).GoBack();
		}

		/*
		 * Member Methods
		 */

		public void AddItem( IPhoneListViewItem item )
		{
			((PhoneListView)GetControl()).AddItem( item );
		}

		public override void FinalSizeNotify()
		{
			// do nothing, as the list will occupy the full screen
		}

		public override System.Drawing.Point GetControlOffset()
		{
			return new System.Drawing.Point( 0, 0 );
		}

		public override bool PrefersFullWidth()
		{
			return true;
		}

		public override System.Drawing.Size GetMinimumSize()
		{
			return new System.Drawing.Size( 180, 240 );
		}

		public override PUC.Layout.PreferredSize GetPreferredSize()
		{
			return new PUC.Layout.PreferredSize( 180, 240 );
		}
	}
}
