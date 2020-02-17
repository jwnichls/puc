using System;

using PhoneControls;


namespace PUC.CIO
{
	/// <summary>
	/// The base class of ConcreteInteractionObjects that are
	/// members of an interactive list.  Initially used for the
	/// SmartPhone interface generator.
	/// </summary>
	public abstract class ListViewItemCIO 
		: ConcreteInteractionObject, IPhoneListViewItem
	{
		/*
		 * Member Variables
		 */

		protected int _width;
		protected System.Drawing.Font _font;


		/*
		 * Events
		 */
		
		public abstract event EventHandler ItemActivated;
		public abstract event EventHandler Changed;


		/*
		 * Properties
		 */

		public abstract string Label
		{
			get;
		}

		public abstract bool Enabled
		{
			get;
			set;
		}

		public int Width
		{
			get
			{
				return _width;
			}
			set
			{
				_width = value;
			}
		}

		public System.Drawing.Font Font
		{
			get
			{
				return _font;
			}
			set
			{
				_font = value;
			}
		}

		/*
		 * Member Methods
		 */

		public abstract void Activate();
	}
}
