using System;
using System.Collections;
using System.Windows.Forms;

using PhoneControls;

using PUC;
using PUC.Communication;
using PUC.Types;


namespace PUC.CIO
{
	/// <summary>
	/// StateListViewItemCIO
	/// </summary>
	public class PanelListViewItemCIO : ListViewItemCIO
	{
		/*
		 * Member Variables
		 */

		protected PhoneListViewCIO _parentList;
		protected ScrollingPanelCIO _panel;
		protected LabelDictionary _labels;


		/*
		 * Events
		 */

		public override event EventHandler ItemActivated;
		public override event EventHandler Changed;


		/*
		 * Constructor
		 */

		public PanelListViewItemCIO( PhoneListViewCIO parentList, 
			LabelDictionary labels )
		{
			_parentList = parentList;
			_labels = labels;
			_panel = new ScrollingPanelCIO();

			/*
			_panel = new PanelCIO( new PhonePanel() );
			((PhonePanel)_panel.GetControl()).BackButtonPressed +=
				new KeyPressEventHandler(this.backButtonPress);
			*/
		}


		/*
		 * Properties
		 */

		public override string Label
		{
			get
			{
				return _labels.GetLabelByPixelLength( this.Font, this.Width );
			}
		}

		public override bool Enabled
		{
			get
			{
				return true;
			}
			set
			{
				throw new NotSupportedException( "can't set enabled of PhoneListViewItem" );
			}
		}

		public ScrollingPanelCIO Panel
		{
			get
			{
				return _panel;
			}
		}


		/*
		 * Member Methods
		 */

		public override void Activate()
		{
			if ( ItemActivated != null )
				ItemActivated( this, new EventArgs() );

			Control top = _parentList.GetControl().Parent;
			_panel.GetControl().Size = top.Size;
			_panel.GetControl().Location = new System.Drawing.Point( 0, 0 );

			PhonePUC.LeftMenuStackItem item = new PhonePUC.LeftMenuStackItem( "Back", new EventHandler(this.menuButtonPress) );
			Globals.PushLeftMenuStack( item );

			top.Controls.Add( _panel.GetControl() );
			top.Controls.Remove( _parentList.GetControl() );
			_panel.GetControl().Focus();
		}

		protected void backRequested()
		{
			Control top = _panel.GetControl().Parent;
			top.Controls.Add( _parentList.GetControl() );
			top.Controls.Remove( _panel.GetControl() );
			Globals.PopLeftMenuStack();
			_parentList.GetControl().Focus();
		}

		protected void backButtonPress( object source, KeyPressEventArgs e )
		{
			MessageBox.Show( "back button pressed" );

			if ( _parentList.GetControl().Parent == null )
			{
				Control top = _panel.GetControl().Parent;
				top.Controls.Add( _parentList.GetControl() );
				top.Controls.Remove( _panel.GetControl() );
				_parentList.GetControl().Focus();

				e.Handled = true;
			}
		}

		protected void menuButtonPress( object source, EventArgs a )
		{
			backRequested();
		}
	}
}
