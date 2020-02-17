using System;
using System.Collections;

namespace PUC.UIGeneration.ListRepresentation
{
	/// <summary>
	/// The ListNode is the base class of all nodes in the
	/// ListRepresentation structure that contain other nodes.
	/// </summary>
	public abstract class ListNode : ListItemNode
	{
		/*
		 * Constant
		 */

		/*
		 * Member Variables
		 */

		protected ArrayList _items;
		protected GroupNode _group;


		/*
		 * Constructor
		 */

		public ListNode()
		{
			_items = new ArrayList();
		}


		/*
		 * Properties
		 */

		public ArrayList Items
		{
			get
			{
				return _items;
			}
		}

		public GroupNode Group
		{
			get
			{
				return _group;
			}
			set
			{
				_group = value;
			}
		}


		/*
		 * Methods
		 */

		public void Add( ListItemNode item )
		{
			_items.Add( item );
			item.Parent = this;
		}

		public void Remove( ListItemNode item )
		{
			if ( _items.Contains( item ) )
			{
				_items.Remove( item );
				item.Parent = null;
			}
		}
	}
}
