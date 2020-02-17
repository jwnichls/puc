using System;
using PUC.CIO;

namespace PUC.UIGeneration
{
	/// <summary>
	/// This is the base class for the set of objects that can be
	/// represented in the interface tree.  The interface tree is created
	/// from the modified group tree in the dependency graph analysis
	/// phase.
	/// </summary>
	public abstract class InterfaceNode
	{
		/*
		 * Member Variables
		 */

		protected ContainerCIO				_container;

		protected System.Drawing.Rectangle	_bounds;
		protected MultiplePanelNode			_parent;

		protected System.Drawing.Size		_minSize;
		protected PUC.Layout.PreferredSize	_prefSize;


		/*
		 * Constructor
		 */

		public InterfaceNode()
		{
			_container = null;

			_bounds = new System.Drawing.Rectangle( 0, 0, 0, 0 );
			_parent = null;

			_minSize = new System.Drawing.Size( 0, 0 );
			_prefSize = null;
		}


		/*
		 * Properties
		 */

		public ContainerCIO Container
		{
			get
			{
				return _container;
			}
			set
			{
				_container = value;
			}
		}


		/*
		 * Member Methods
		 */

		public MultiplePanelNode GetParent()
		{
			return _parent;
		}

		public System.Drawing.Rectangle GetBounds()
		{
			return _bounds;
		}

		public void SetParent( MultiplePanelNode node )
		{
			_parent = node;
		}

		public virtual void SetLocation( int x, int y )
		{
			_bounds.X = x;
			_bounds.Y = y;
		}

		public virtual void SetSize( int width, int height )
		{
			_bounds.Height = height;
			_bounds.Width = width;
		}

		/// <summary>
		/// Inserts the node parameter as the parent of this
		/// node.  node becomes the child of the former parent
		/// and this node becomes this child of node.
		/// </summary>
		/// <param name="node">the node inserted as this node's parent</param>
		public void InsertAsParent( MultiplePanelNode node )
		{
			if ( _parent != null )
			{
				MultiplePanelNode oldparent = _parent;
				oldparent.RemovePanel( this );
				oldparent.AddPanel( node );
			}

			node.AddPanel( this );
		}

		
		/*
		 * Properties
		 */

		public virtual System.Drawing.Size MinimumSize
		{
			get
			{
				return _minSize;
			}
			set
			{
				_minSize = value;
			}
		}

		public virtual PUC.Layout.PreferredSize PreferredSize
		{
			get
			{
				return _prefSize;
			}
			set
			{
				_prefSize = value;
			}
		}


		/*
		 * Abstract Methods
		 */

		public abstract void AddComponents( ContainerCIO container, LayoutVariables vars );

		public abstract void CalculateMinimumSize( LayoutVariables vars );
		public abstract void CalculatePreferredSize( LayoutVariables vars );

		public abstract void DoLayout( LayoutVariables vars );

		public override abstract string ToString();
	}
}
