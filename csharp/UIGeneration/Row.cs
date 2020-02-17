using System;
using System.Collections;

using PUC;
using PUC.CIO;

namespace PUC.UIGeneration
{
	/// <summary>
	/// This class represents a row of controls in the interface.
	/// </summary>
	public abstract class Row
	{
		/*
		 * Member Variables
		 */

		protected ArrayList _CIOs;
		protected PanelNode _parent;
		protected GroupNode _group;

		protected System.Drawing.Rectangle  _bounds;
		protected System.Drawing.Size		_minSize;
		protected PUC.Layout.PreferredSize	_prefSize;
		protected int						_maxTextOffset;


		/*
		 * Constructor
		 */

		public Row( GroupNode g, PanelNode panel )
		{
			_CIOs = new ArrayList();
			_parent = panel;
			_group = g;

			_minSize = new System.Drawing.Size( 0, 0 );
			_prefSize = null;
			_maxTextOffset = 0;
		}


		/*
		 * Properties
		 */

		public GroupNode Group
		{
			get
			{
				return _group;
			}
		}

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

		public virtual int MaximumTextOffset
		{
			get
			{
				return _maxTextOffset;
			}
			set
			{
				_maxTextOffset = value;
			}
		}

		public ArrayList CIOs
		{
			get
			{
				return _CIOs;
			}
		}


		/*
		 * Member Methods
		 */

		protected void addCIO( ConcreteInteractionObject cio )
		{
			_CIOs.Add( cio );
		}

		public System.Drawing.Rectangle GetBounds()
		{
			return _bounds;
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

		public abstract void AddComponents( ContainerCIO container, LayoutVariables vars );

		public abstract void CalculateMinimumSize( LayoutVariables vars );
		public abstract void CalculatePreferredSize( LayoutVariables vars );

		public abstract void DoLayout( ContainerCIO container,
									   LayoutVariables vars );

		public override abstract string ToString();
	}
}
