using System;
using System.Windows.Forms;

using PUC.Layout;

#if POCKETPC || DESKTOP
using PocketPCControls;
#endif
#if SMARTPHONE
using PhoneControls;
#endif

namespace PUC.CIO
{
	/// <summary>
	/// Summary description for PanelCIO.
	/// </summary>
	public class ScrollingPanelCIO : ContainerCIO
	{
		/*
		 * Member Variables
		 */


		/*
		 * Constructor
		 */

		public ScrollingPanelCIO() 
#if POCKETPC || DESKTOP
			: base( new ScrollingPanel() )
#endif
#if SMARTPHONE
			: base( new PhoneScrollingPanel() )
#endif
		{			
		}


		/*
		 * Member Methods
		 */

		public override void AddCIO( ControlBasedCIO cio )
		{
			if ( cio == null ) return;

#if POCKETPC || DESKTOP
			ScrollingPanel c = (ScrollingPanel)this.GetControl();
#endif
#if SMARTPHONE
			PhoneScrollingPanel c = (PhoneScrollingPanel)this.GetControl();
#endif

			c.Controls.Add( cio.GetControl() );
			_cios.Add( cio );

			cio.SetVisible( true );
		}

		public override void RemoveCIO( ControlBasedCIO cio )
		{
			if ( cio == null ) return;

			cio.SetVisible( false );

#if POCKETPC || DESKTOP
			((ScrollingPanel)this.GetControl())
#endif
#if SMARTPHONE
			((PhoneScrollingPanel)this.GetControl())
#endif
				.Controls.Remove( cio.GetControl() );
			_cios.Remove( cio );
		}

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

		public override System.Drawing.Size InternalSize
		{
			get
			{
#if POCKETPC || DESKTOP
				return ((ScrollingPanel)GetControl()).InternalSize;
#endif
#if SMARTPHONE
				return ((PhoneScrollingPanel)GetControl()).InternalSize;
#endif
			}
		}
	}
}
