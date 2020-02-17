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
	public class TabbedLinkedCIO : StateLinkedCIO
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
#if SMARTPHONE
		public const int MINIMUM_BOTTOM_PAD = 22;
#endif
		public const int MINIMUM_TAB_PAD = 5;

		/*
		 * Member Variables
		 */

		// Hashtable  _indexItemMap;
		// Hashtable  _itemIndexMap;
		ArrayList  _panelCIOs;
		TabControl _tabControl;


		/*
		 * Constructor
		 */
		public TabbedLinkedCIO( ApplianceObject ao )
			: base( ao, new System.Windows.Forms.Panel() )
		{
			ApplianceState state = (ApplianceState)GetApplObj();
			Panel panel = (Panel)GetControl();
			_tabControl = new TabControl();
			panel.Controls.Add( _tabControl );

			_tabControl.Location = new System.Drawing.Point( 0, 0 );
			_tabControl.Size = panel.ClientSize;

			panel.Resize += new EventHandler(this.Resized);

			_panelCIOs = new ArrayList();

			object stateval = state.Value;
			EnumeratedSpace espc = (EnumeratedSpace)state.Type.ValueSpace;
			Hashtable labels = state.Type.ValueLabels;

			int labelWidth = _tabControl.ClientSize.Width / espc.GetItemCount();

			for( int i = 1; i <= espc.GetItemCount(); i++ )
			{
				object labelSpace = i;
				LabelDictionary ldict = (LabelDictionary)labels[ labelSpace ];

				TabbedPanelCIO pageCIO = new TabbedPanelCIO( ldict );

				TabPage page = pageCIO.TabPage;
				page.Enabled = ldict.Enabled;

				_tabControl.Controls.Add( page );
				_panelCIOs.Add( pageCIO );

				pageCIO.ChooseLabel( labelWidth - 2 * MINIMUM_TAB_PAD );

				if ( state.Defined && i == (int)stateval )
					_tabControl.SelectedIndex = i - 1;
			}

			state.LabelChangedEvent += new ApplianceObject.LabelChangedHandler(this.LabelChanged);
			state.EnableChangedEvent += new ApplianceObject.EnableChangedHandler(this.EnableChanged);
			state.TypeChangedEvent += new ApplianceState.TypeChangedHandler(this.TypeChanged);
			state.ValueChangedEvent += new ApplianceState.ValueChangedHandler(this.ValueChanged);

			_tabControl.SelectedIndexChanged += new EventHandler(this.SelectedIndexChanged);

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
		}

		public void LabelChanged( ApplianceObject ao )
		{
			refreshDisplay();
		}

		public void EnableChanged( ApplianceObject ao )
		{
			GetControl().Enabled = ao.Enabled;
		}

		public void TypeChanged( ApplianceState state )
		{
			typeChangeRefresh();
		}

		public void ValueChanged( ApplianceState state )
		{
			refreshDisplay();
		}

		public void SelectedIndexChanged( object source, EventArgs a )
		{
			if ( ((ApplianceState)GetApplObj()).Defined &&
				 ( _tabControl.SelectedIndex + 1 ) == (int)((ApplianceState)GetApplObj()).Value )
				return;

			((ApplianceState)GetApplObj()).RequestChange( _tabControl.SelectedIndex + 1 );
		}


		/*
		 * Member Methods
		 */

		protected void typeChangeRefresh()
		{	
			IEnumerator e = _panelCIOs.GetEnumerator();

			while( e.MoveNext() )
			{
				((TabbedPanelCIO)e.Current).ResolveEnabled();
			}
		}
		
		protected void refreshDisplay()
		{
			ApplianceState state = (ApplianceState)GetApplObj();

			if ( !state.Defined ) return;

			if ( (int)state.Value != 0 &&
				_tabControl.Controls.Count > 0 )
				_tabControl.SelectedIndex = (int)state.Value - 1;
		}

		public ContainerCIO GetContainerByValue( int idx )
		{
			return (ContainerCIO)_panelCIOs[ idx - 1 ];
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
			IEnumerator e = _panelCIOs.GetEnumerator();
			while( e.MoveNext() )
				((ControlBasedCIO)e.Current).FinalSizeNotify();
		}
	}


	public class TabbedPanelCIO : ContainerCIO
	{	
		/*
		 * Member Variables
		 */
		
		protected LabelDictionary _labels;


		/*
		 * Constructor
		 */

		public TabbedPanelCIO( LabelDictionary labels ) : base( new TabPage() )
		{
			_labels = labels;

			GetControl().Text = _labels.GetShortestLabel();
		}


		/*
		 * Member Methods
		 */

		public LabelDictionary Labels
		{
			get
			{
				return _labels;
			}
			set
			{
				_labels = value;
			}
		}

		public TabPage TabPage
		{
			get
			{
				return (TabPage)GetControl();
			}
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
		
		/// <summary>
		/// Chooses the most appropriate label based upon the space given
		/// by the interface generator.  
		/// </summary>
		/// <param name="width">the width allowed for the tab label</param>
		public int ChooseLabel( int width )
		{
			try
			{
				GetControl().Text = _labels.GetLabelByPixelLength( GetControl().Parent.Font,
																   width );
			}
			catch( Exception )
			{
				GetControl().Text = _labels.GetShortestLabel();
			}

			return (int)Globals.MeasureString( GetControl().Text, GetControl().Parent.Font ).Width;
		}

		/// <summary>
		/// Sets the enable of this control based upon the enable of its 
		/// label dictionary member.
		/// </summary>
		public void ResolveEnabled()
		{
			GetControl().Enabled = _labels.Enabled;
		}
	}
}
