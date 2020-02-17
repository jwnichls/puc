using System;
using System.Drawing;
using System.Collections;

using PUC.Layout;

namespace PUC.UIGeneration
{
	/// <summary>
	/// This object represents nodes that branch to multiple 
	/// non-overlapping panels.
	/// </summary>
	public class OverlappingPanelsNode : MultiplePanelNode
	{
		/*
		 * Member Variables
		 */

		/*
		 * Constructor
		 */

		public OverlappingPanelsNode() : base()
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
					prefWidth = Math.Max( size.Width, prefWidth );

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

			IEnumerator e = _panels.GetEnumerator();
			while( e.MoveNext() )
				((InterfaceNode)e.Current).AddComponents( container, vars );

			this.CalculateMinimumSize( vars );
			this.CalculatePreferredSize( vars );
		}

		public override void DoLayout( LayoutVariables vars )
		{
			IEnumerator e = _panels.GetEnumerator();
			
			while( e.MoveNext() )
			{
				InterfaceNode node = (InterfaceNode)e.Current;

				node.SetSize( _bounds.Width, _bounds.Height );
				node.SetLocation( _bounds.X, _bounds.Y );

				node.DoLayout( vars );
			}
		}

		public override string ToString()
		{
			return "OverlappingPanelsNode - " + base.ToString();
		}

	}
}
