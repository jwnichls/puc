using System;
using System.Collections;

using PUC.Layout;

namespace PUC.UIGeneration
{
	/// <summary>
	/// This object represents nodes that branch to multiple 
	/// vertically oriented non-overlapping panels.
	/// </summary>
	public class VerticallyNonOverlappingPanelsNode : MultiplePanelNode
	{
		/*
		 * Member Variables
		 */

		/*
		 * Constructor
		 */

		public VerticallyNonOverlappingPanelsNode() : base()
		{
		}


		/*
		 * InterfaceNode Methods
		 */

		public override void CalculateMinimumSize(LayoutVariables vars)
		{
			IEnumerator e = _panels.GetEnumerator();
			
			_minSize = new System.Drawing.Size( 0, 0 );

			while( e.MoveNext() )
			{
				((InterfaceNode)e.Current).CalculateMinimumSize( vars );
				System.Drawing.Size size = ((InterfaceNode)e.Current).MinimumSize;

				_minSize.Width = Math.Max( size.Width, _minSize.Width );
				_minSize.Height += size.Height;
			}
		}

		public override void CalculatePreferredSize(LayoutVariables vars)
		{
			IEnumerator e = _panels.GetEnumerator();
			
			int prefWidth = 0;
			int prefHeight = 0;

			while( e.MoveNext() )
			{
				((InterfaceNode)e.Current).CalculatePreferredSize( vars );
				PreferredSize size = ((InterfaceNode)e.Current).PreferredSize;

				if ( prefWidth == PreferredSize.INFINITE || size.Width == PreferredSize.INFINITE )
					prefWidth = PreferredSize.INFINITE;
				else
					prefWidth = Math.Max( size.Width, prefWidth );

				if ( prefHeight == PreferredSize.INFINITE || size.Height == PreferredSize.INFINITE )
					prefHeight = PreferredSize.INFINITE;
				else
					prefHeight += size.Height;
			}

			_prefSize = new PUC.Layout.PreferredSize( prefWidth, prefHeight );
		}

		public override void AddComponents( PUC.CIO.ContainerCIO container, LayoutVariables vars )
		{
			Container = container;

			IEnumerator e = _panels.GetEnumerator();
			while( e.MoveNext() )
				((InterfaceNode)e.Current).AddComponents( container, vars );

			this.CalculateMinimumSize( vars );
			this.CalculatePreferredSize( vars );
		}

		public override void DoLayout( LayoutVariables vars )
		{
			int[] minimum = new int[ _panels.Count ];
			int[] preferred = new int[ _panels.Count ];

			for( int i = 0; i < _panels.Count; i++ )
			{
				minimum[ i ] = ((InterfaceNode)_panels[ i ]).MinimumSize.Height;
				preferred[ i ] = ((InterfaceNode)_panels[ i ]).PreferredSize.Height;
			}

			int[] heights = LayoutAlgorithms.AllocateSizeValues( _bounds.Height, minimum, preferred, vars.RowPadding );

			int height = 0;
			for( int i = 0; i < _panels.Count; i++ )
			{
				InterfaceNode node = (InterfaceNode)_panels[ i ];

				node.SetLocation( _bounds.X, _bounds.Y + height );
				node.SetSize( _bounds.Width, heights[ i ] );
				height += heights[ i ];

				if ( ( i == ( _panels.Count - 1 ) ) &&
					 ( height < _bounds.Height ) )
				{
					node.SetSize( _bounds.Width, heights[ i ] + ( _bounds.Height - height ) );
				}

				node.DoLayout( vars );
			}
		}

		public override string ToString()
		{
			return "NonOverlappingPanelsNode - " + base.ToString();
		}

	}
}
