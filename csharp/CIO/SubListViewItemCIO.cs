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
	/// SubListViewItemCIO
	/// </summary>
	public class SubListViewItemCIO : ListViewItemCIO
	{
		/*
		 * Member Variables
		 */

		protected PhoneListViewCIO _parentList;
		protected PhoneListViewCIO _subList;
		protected LabelDictionary  _labels;


		/*
		 * Events
		 */

		public override event EventHandler ItemActivated;
		public override event EventHandler Changed;


		/*
		 * Constructor
		 */

		public SubListViewItemCIO( PhoneListViewCIO parent, 
							       PhoneListViewCIO subList,
								   LabelDictionary dict )
		{
			_parentList = parent;
			_subList = subList;
			_labels = dict;

			((PhoneListView)_subList.GetControl()).BackButtonPressed +=
				new KeyPressEventHandler(this.backButtonPress);
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


		/*
		 * Member Methods
		 */

		public override void Activate()
		{
			if ( ItemActivated != null )
				ItemActivated( this, new EventArgs() );

			Control top = _parentList.GetControl().Parent;
			_subList.GetControl().Size = top.Size;
			_subList.GetControl().Location = new System.Drawing.Point( 0, 0 );

			PhonePUC.LeftMenuStackItem item = new PhonePUC.LeftMenuStackItem( "Back", new EventHandler(this.menuButtonPress) );
			Globals.PushLeftMenuStack( item );

			top.Controls.Add( _subList.GetControl() );
			top.Controls.Remove( _parentList.GetControl() );
			_subList.GetControl().Focus();
		}

		protected void backRequested()
		{
			Control top = _subList.GetControl().Parent;
			top.Controls.Add( _parentList.GetControl() );
			top.Controls.Remove( _subList.GetControl() );
			Globals.PopLeftMenuStack();
			_parentList.GetControl().Focus();
		}

		protected void backButtonPress( object source, KeyPressEventArgs e )
		{
			if ( _parentList.GetControl().Parent == null )
			{
				backRequested();
				e.Handled = true;
			}
		}

		protected void menuButtonPress( object source, EventArgs e )
		{
			backRequested();
		}
	}
}
