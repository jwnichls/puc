using System;

namespace PUC.UIGeneration.ListRepresentation
{
	/// <summary>
	/// The ListItemNode is the base class of all nodes in the 
	/// ListRepresentation data structure.
	/// </summary>
	public abstract class ListItemNode : Decorator
	{
		/*
		 * Constants
		 */

		/*
		 * Member Variables
		 */

		protected ListNode _parent;


		/*
		 * Constructor
		 */

		public ListItemNode()
		{
		}


		/*
		 * Properties
		 */

		public ListNode Parent
		{
			get
			{
				return _parent;
			}
			set
			{
				_parent = value;
			}
		}


		/*
		 * Member Methods
		 */

		public void Promote()
		{
			if ( _parent != null && _parent.Parent != null )
			{
				ListNode p = _parent;
				p.Remove( this );
				p.Parent.Add( this );
			}
			else
				throw new NotSupportedException( "can't promote because too " + 
												 "high in the hierarchy" );
		}

		public void MoveTo( ListNode list )
		{
			if ( _parent != null )
				_parent.Remove( this );

			list.Add( this );
		}
	}
}
