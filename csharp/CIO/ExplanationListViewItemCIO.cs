using System;

using PhoneControls;

using PUC;
using PUC.Communication;


namespace PUC.CIO
{
	/// <summary>
	/// CommandListViewItemCIO
	/// </summary>
	public class ExplanationListViewItemCIO : ListViewItemCIO
	{
		/*
		 * Constants
		 */

		public const string CIO_NAME = "ExplanationListViewItemCIO";

		
		/*
		 * Dynamic Loading Code
		 */

		public static ConcreteInteractionObject CreateExplanationListViewItemCIO( ApplianceObject ao )
		{
			return new ExplanationListViewItemCIO( (ApplianceExplanation)ao );
		}


		/*
		 * Member Variables
		 */

		protected ApplianceExplanation _explanation;


		/*
		 * Events
		 */
		
		public override event EventHandler ItemActivated;
		public override event EventHandler Changed;

		
		/*
		 * Constructor
		 */

		public ExplanationListViewItemCIO( ApplianceExplanation expl )
		{
			_explanation = expl;

			_explanation.EnableChangedEvent += new PUC.ApplianceObject.EnableChangedHandler(this.enableChanged);
			_explanation.LabelChangedEvent += new PUC.ApplianceObject.LabelChangedHandler(this.labelChanged);
		}


		/*
		 * Properties
		 */

		public override string Label
		{
			get
			{
				if ( _explanation.Labels != null )
					return _explanation.Labels.GetLabelByPixelLength( this.Font, this.Width );
				else 
					return _explanation.Name;
			}
		}

		public override bool Enabled
		{
			get
			{
				return _explanation.Enabled;
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
	}
}
