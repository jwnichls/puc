using System;
using System.Collections;
using System.Windows.Forms;
using PUC;
using PUC.Communication;
using PUC.Types;

namespace PUC.CIO
{
	/// <summary>
	/// Summary description for TabbedLinkedCIO.
	/// </summary>
	public class TabbedPanelsCIO : ContainerCIO
	{
		/*
		 * Constants
		 */

#if POCKETPC
		public const int MINIMUM_BOTTOM_PAD = 22;
#endif
#if DESKTOP
		public const int MINIMUM_BOTTOM_PAD = 26;
#endif
		public const int MINIMUM_TAB_PAD = 5;

		/*
		 * Member Variables
		 */

		// Hashtable  _indexItemMap;
		// Hashtable  _itemIndexMap;
		TabControl _tabControl;


		/*
		 * Constructor
		 */
		public TabbedPanelsCIO()
			: base( new System.Windows.Forms.Panel() )
		{
			Panel panel = (Panel)GetControl();
			_tabControl = new TabControl();
			panel.Controls.Add( _tabControl );

			_tabControl.Location = new System.Drawing.Point( 0, 0 );
			_tabControl.Size = panel.ClientSize;

			panel.Resize += new EventHandler(this.Resized);

			refreshDisplay();
		}


		/*
		 * Event Handlers
		 */

		public void Resized( object source, EventArgs a )
		{
			_tabControl.Size = GetControl().ClientSize;

			IEnumerator e = _tabControl.Controls.GetEnumerator();
			while( e.MoveNext() )
			{
				((Control)e.Current).Size = _tabControl.ClientSize;
			}

			refreshDisplay();
		}

		
		/*
		 * Member Methods
		 */

		public override void AddCIO(ControlBasedCIO cio)
		{
			AddTab( (TabbedPanelCIO)cio );
		}

		public int AddTab( TabbedPanelCIO panelCIO )
		{
			TabPage page = panelCIO.TabPage;

			_cios.Add( panelCIO );
			_tabControl.Controls.Add( page );

			LabelDictionary labels = panelCIO.Labels;
			panelCIO.ResolveEnabled();
			panelCIO.ChooseLabel( 0 );

			refreshDisplay();

			return _cios.Count - 1;
		}

		protected void refreshDisplay()
		{
			int labelWidth;
			int widthLeft = _tabControl.Size.Width;

			for( int i = 0; i < _cios.Count; i++ )
			{
				labelWidth = widthLeft / ( _cios.Count - i );

				TabbedPanelCIO page = (TabbedPanelCIO)_cios[ i ];
				widthLeft -= page.ChooseLabel( labelWidth );
				page.ResolveEnabled();
			}
		}

		public ContainerCIO GetContainerByValue( int idx )
		{
			return (ContainerCIO)_cios[ idx ];
		}

		public override System.Drawing.Size GetMinimumSize()
		{
			return new System.Drawing.Size( 0, MINIMUM_BOTTOM_PAD );
		}

		public override PUC.Layout.PreferredSize GetPreferredSize()
		{
			return new PUC.Layout.PreferredSize( GetMinimumSize() );
		}

		public override System.Drawing.Point GetControlOffset()
		{
			return new System.Drawing.Point( 0, 0 );
		}

		public override void FinalSizeNotify()
		{
			IEnumerator e = _cios.GetEnumerator();
			while( e.MoveNext() )
				((ControlBasedCIO)e.Current).FinalSizeNotify();
		}

		public override System.Drawing.Size InternalSize
		{
			get
			{
				return new System.Drawing.Size( GetControl().Width, GetControl().Height - MINIMUM_BOTTOM_PAD );
			}
		}
	}
}
