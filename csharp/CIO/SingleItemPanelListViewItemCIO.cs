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
	public class SingleItemPanelListViewItemCIO : ListViewItemCIO
	{
		/*
		 * Member Variables
		 */

		protected PhoneListViewCIO _parentList;
		protected ScrollingPanelCIO _panel;
		protected ApplianceState _state;


		/*
		 * Events
		 */

		public override event EventHandler ItemActivated;
		public override event EventHandler Changed;


		/*
		 * Constructor
		 */

		public SingleItemPanelListViewItemCIO( PhoneListViewCIO parentList, 
						  			 ApplianceState state )
		{
			_parentList = parentList;
			_state = state;
			_panel = new ScrollingPanelCIO();

			/*
			_panel = new PanelCIO( new PhonePanel() );

			((PhonePanel)_panel.GetControl()).BackButtonPressed +=
				new KeyPressEventHandler(this.backButtonPress);
			*/

			_state.EnableChangedEvent += new PUC.ApplianceObject.EnableChangedHandler(this.enableChanged);
			_state.LabelChangedEvent += new PUC.ApplianceObject.LabelChangedHandler(this.labelChanged);
			_state.ValueChangedEvent += new PUC.ApplianceState.ValueChangedHandler(this.valueChanged);
		}


		/*
		 * Properties
		 */

		public override string Label
		{
			get
			{
				string stateName  = null;
				string stateValue = null;
				int widthLeft = this.Width;

				if ( _state.Labels != null )
					stateName = _state.Labels.GetLabelByPixelLength( this.Font, widthLeft / 2 );
				else 
					stateName = _state.Name;

				widthLeft -= (int)Globals.MeasureString( stateName + " - ", this.Font ).Width;

				object val = _state.Value;
				Hashtable valLabels = _state.Type.ValueLabels;

				if ( valLabels != null && valLabels[ val ] != null )
					stateValue = ((LabelDictionary)valLabels[ val ]).GetLabelByPixelLength( this.Font, widthLeft );
				else if ( _state.Defined )
					stateValue = val.ToString();
				else
					stateValue = "";

				return stateName + " - " + stateValue;
			}
		}

		public override bool Enabled
		{
			get
			{
				return _state.Enabled;
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

		protected void enableChanged( ApplianceObject o )
		{
			if ( Changed != null )
				Changed( this, new EventArgs() );
		}

		protected void labelChanged( ApplianceObject o )
		{
			if ( Changed != null )
				Changed( this, new EventArgs() );
		}

		protected void valueChanged( ApplianceState o )
		{
			if ( Changed != null )
				Changed( this, new EventArgs() );
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
