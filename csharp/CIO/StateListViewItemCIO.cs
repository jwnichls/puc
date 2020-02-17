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
	public class StateListViewItemCIO : ListViewItemCIO
	{
		/*
		 * Constants
		 */

		public const string CIO_NAME = "StateListViewItemCIO";

		
		/*
		 * Dynamic Loading Code
		 */

		public static ConcreteInteractionObject CreateStateListViewItemCIO( ApplianceObject ao )
		{
			return new StateListViewItemCIO( (ApplianceState)ao );
		}

		
		/*
		 * Member Variables
		 */

		protected ApplianceState   _state;


		/*
		 * Events
		 */

		public override event EventHandler ItemActivated;
		public override event EventHandler Changed;


		/*
		 * Constructor
		 */

		public StateListViewItemCIO( ApplianceState state )
		{
			_state     = state;

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

				if ( _state.Labels != null )
					stateName = _state.Labels.GetShortestLabel();
				else 
					stateName = _state.Name;

				object val = _state.Value;
				Hashtable valLabels = _state.Type.ValueLabels;

				if ( valLabels != null && valLabels[ val ] != null )
					stateValue = ((LabelDictionary)valLabels[ val ]).GetShortestLabel();
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


		/*
		 * Member Methods
		 */

		public override void Activate()
		{
			if ( ItemActivated != null )
				ItemActivated( this, new EventArgs() );

			/* // TODO: Eventually do something here
			PUC.Communication.Message msg = new CommandInvokeRequest( _command.GetName() );

			try
			{
				_appliance.GetConnection().Send( msg );
			}
			catch( Exception ) { }
			*/
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
	}
}
