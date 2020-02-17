using System;
using System.Collections;

using PUC.CIO;
using PUC.Layout;

namespace PUC.UIGeneration
{
	/// <summary>
	/// This object represents nodes that branch to multiple 
	/// horizontally-aligned non-overlapping panels.
	/// </summary>
	public class HorizontallyNonOverlappingPanelsNode : MultiplePanelNode
	{
		/*
		 * Member Variables
		 */

		protected LineCIO	_lineCIO;
		protected int		_lineLoc;


		/*
		 * Constructor
		 */

		public HorizontallyNonOverlappingPanelsNode( PanelNode vertPanel,
													 InterfaceNode horizPanel ) 
			: base()
		{
			vertPanel.InsertAsParent( this );
			AddPanel( horizPanel );

			vertPanel.SetVertical( true );
		}

		public HorizontallyNonOverlappingPanelsNode() : base()
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

				_minSize.Width += size.Width;
				_minSize.Height = Math.Max( size.Height, _minSize.Height );
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
					prefWidth += size.Width;

				if ( prefHeight == PreferredSize.INFINITE || size.Height == PreferredSize.INFINITE )
					prefHeight = PreferredSize.INFINITE;
				else
					prefHeight = Math.Max( size.Height, prefHeight );
			}

			_prefSize = new PUC.Layout.PreferredSize( prefWidth, prefHeight );
		}

		public override void AddComponents( PUC.CIO.ContainerCIO container, LayoutVariables vars )
		{
			Container = container;

			_lineCIO = new PUC.CIO.LineCIO();
			Container.AddCIO( _lineCIO );

			IEnumerator e = _panels.GetEnumerator();
			while( e.MoveNext() )
				((InterfaceNode)e.Current).AddComponents( container, vars );

			this.CalculateMinimumSize( vars );
			this.CalculatePreferredSize( vars );
		}

		public override void DoLayout( LayoutVariables vars )
		{
			_lineCIO.GetControl().Size = new System.Drawing.Size( 1, _bounds.Height );
			_lineCIO.GetControl().Location = new System.Drawing.Point( _lineLoc, 0 );

			int[] minimum = new int[ _panels.Count ];
			int[] preferred = new int[ _panels.Count ];

			for( int i = 0; i < _panels.Count; i++ )
			{
				minimum[ i ] = ((InterfaceNode)_panels[ i ]).MinimumSize.Width;
				preferred[ i ] = ((InterfaceNode)_panels[ i ]).PreferredSize.Width;
			}

			int[] widths = LayoutAlgorithms.AllocateSizeValues( _bounds.Width, minimum, preferred, vars.RowPadding );

			int width = 0;
			for( int i = 0; i < _panels.Count; i++ )
			{
				InterfaceNode node = (InterfaceNode)_panels[ i ];

				node.SetLocation( _bounds.X + width, _bounds.Y );
				node.SetSize( widths[ i ], _bounds.Height );
				width += widths[ i ];

				if ( ( i == ( _panels.Count - 1 ) ) &&
					 ( width < _bounds.Width ) )
				{
					node.SetSize( widths[ i ] + ( _bounds.Width - width ), _bounds.Height );
				}

				node.DoLayout( vars );
			}
		}

		public override string ToString()
		{
			return "VerticalNoOverlapPanelsNode - " + base.ToString();
		}

	}
}
