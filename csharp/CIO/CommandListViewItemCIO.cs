using System;

using PhoneControls;

using PUC;
using PUC.Communication;


namespace PUC.CIO
{
	/// <summary>
	/// CommandListViewItemCIO
	/// </summary>
	public class CommandListViewItemCIO : ListViewItemCIO
	{
		/*
		 * Constants
		 */

		public const string CIO_NAME = "CommandListViewItemCIO";

		
		/*
		 * Dynamic Loading Code
		 */

		public static ConcreteInteractionObject CreateCommandListViewItemCIO( ApplianceObject ao )
		{
			return new CommandListViewItemCIO( (ApplianceCommand)ao );
		}


		/*
		 * Member Variables
		 */

		protected ApplianceCommand _command;


		/*
		 * Events
		 */
		
		public override event EventHandler ItemActivated;
		public override event EventHandler Changed;

		
		/*
		 * Constructor
		 */

		public CommandListViewItemCIO( ApplianceCommand cmd )
		{
			_command   = cmd;

			_command.EnableChangedEvent += new PUC.ApplianceObject.EnableChangedHandler(this.enableChanged);
			_command.LabelChangedEvent += new PUC.ApplianceObject.LabelChangedHandler(this.labelChanged);
		}


		/*
		 * Properties
		 */

		public override string Label
		{
			get
			{
				if ( _command.Labels != null )
					return _command.Labels.GetLabelByPixelLength( this.Font, this.Width );
				else 
					return _command.Name;
			}
		}

		public override bool Enabled
		{
			get
			{
				return _command.Enabled;
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

			_command.InvokeCommand();
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
