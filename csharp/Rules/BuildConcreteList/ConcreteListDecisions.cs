using System;

using PhoneControls;

using PUC;
using PUC.CIO;
using PUC.Rules;

namespace PUC.Rules.BuildConcreteList
{
	/// <summary>
	/// The base class of all decisions that concreteize a 
	/// ListItemNode.
	/// </summary>
	public abstract class ItemDecision : Decision
	{
		/*
		 * Constants
		 */

		public const string DECISION_KEY = "item_decision";


		/*
		 * Constructor
		 */

		public ItemDecision()
			: base()
		{
		}

		public ItemDecision( Decision rootDecision )
			: base( rootDecision )
		{
		}
	}


	public class ListItemDecision : ItemDecision
	{
		/*
		 * Member Variables
		 */

		protected IPhoneListViewItem _listItem;


		/*
		 * Constructors
		 */

		public ListItemDecision( IPhoneListViewItem item )
			: base()
		{
			_listItem = item;
		}

		public ListItemDecision( IPhoneListViewItem item, Decision rootDecision )
			: base( rootDecision )
		{
			_listItem = item;
		}


		/*
		 * Properties
		 */

		public IPhoneListViewItem ListViewItem
		{
			get
			{
				return _listItem;
			}
		}
	}


	public class PanelItemDecision : ItemDecision
	{
		/*
		 * Member Variables
		 */

		protected ConcreteInteractionObject _cio;


		/*
		 * Constructors
		 */

		public PanelItemDecision( ConcreteInteractionObject cio )
			: base()
		{
			_cio = cio;
		}

		public PanelItemDecision( ConcreteInteractionObject cio, Decision rootDecision )
			: base( rootDecision )
		{
			_cio = cio;
		}


		/*
		 * Properties
		 */

		public ConcreteInteractionObject CIO
		{
			get
			{
				return _cio;
			}
		}
	}

	public class PanelLayoutDecision : Decision
	{
		/*
		 * Constant
		 */

		public const string DECISION_KEY = "panel_layout";


		/*
		 * Member Variables
		 */

		protected int _bottomOfPlacement;


		/*
		 * Constructors
		 */

		public PanelLayoutDecision()
			: base()
		{
			_bottomOfPlacement = 0;
		}

		public PanelLayoutDecision( Decision rootDecision )
			: base( rootDecision )
		{
			_bottomOfPlacement = 0;
		}


		/*
		 * Properties
		 */

		public int Bottom
		{
			get
			{
				return _bottomOfPlacement;
			}
			set
			{
				_bottomOfPlacement = value;
			}
		}
	}
}
