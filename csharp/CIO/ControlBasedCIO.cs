using System;
using System.Windows.Forms;

namespace PUC.CIO
{
	/// <summary>
	/// Summary description for ConcreteInteractionObject.
	/// </summary>
	public abstract class ControlBasedCIO : ConcreteInteractionObject
	{
		/*
		 * Member Variables
		 */
		protected Control _control;


		/*
		 * Constructor
		 */
		public ControlBasedCIO( Control control )
		{
			_control = control;
		}


		/*
		 * Member Methods
		 */
		public Control GetControl()
		{
			return _control;
		}

		public void SetVisible( bool visible )
		{
			_control.Visible = visible;
		}

		public abstract PUC.Layout.PreferredSize GetPreferredSize();

		public abstract System.Drawing.Size GetMinimumSize();

		/// <summary>
		/// Return the offset this control should have relative to
		/// the established baseline grid.
		/// </summary>
		/// <returns></returns>
		public abstract System.Drawing.Point GetControlOffset();

		public abstract void FinalSizeNotify();
			
		public virtual bool PrefersFullWidth() 
		{ 
			return false; 
		}

		/// <summary>
		/// Resets any internal caching that this control may do.
		/// Caching is usually done to prevent odd behaviors due to
		/// delays in the network.
		/// </summary>
		public virtual void ResetCache()
		{
		}
	}
}
