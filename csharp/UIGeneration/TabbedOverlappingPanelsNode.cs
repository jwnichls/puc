using System;
using System.Collections;
using System.Drawing;

using PUC;
using PUC.CIO;
using PUC.Layout;
using PUC.Rules.GroupScan;
using PUC.Types;

namespace PUC.UIGeneration
{
	public class TabbedOverlappingPanelsNode : MultiplePanelNode
	{
		/*
		 * Member Variables
		 */

		ControlBasedCIO _tabbedPanel;

		ArrayList _tabNodes;

		Hashtable _groupToTabMap;


		/*
		 * Constructor
		 */

		public TabbedOverlappingPanelsNode( ApplianceState state )
		{
			TabbedLinkedCIO tabPanel = new TabbedLinkedCIO( state );
			_tabbedPanel = tabPanel;
			_tabNodes = new ArrayList();

			EnumeratedSpace espc = (EnumeratedSpace)state.Type.ValueSpace;

			for( int i = 1; i <= espc.GetItemCount(); i++ )
			{
				TabbedPanelCIO p = (TabbedPanelCIO)tabPanel.GetContainerByValue( i );

				TabPanelNode tp = new TabPanelNode( p, null );
				_tabNodes.Add( tp );
				AddPanel( tp );
			}
		}

		public TabbedOverlappingPanelsNode( BranchGroupNode g )
		{
			_tabbedPanel = new TabbedPanelsCIO();
			_tabNodes = new ArrayList();

			_groupToTabMap = new Hashtable();

			IEnumerator e = g.Children.GetEnumerator();
			while( e.MoveNext() )
			{
				TabbedPanelCIO p = new TabbedPanelCIO( ((GroupNode)e.Current).Labels );
				((TabbedPanelsCIO)_tabbedPanel).AddTab( p );

				TabPanelNode tp = new TabPanelNode( p, (GroupNode)e.Current );
				_groupToTabMap[ e.Current ] = tp;
				_tabNodes.Add( tp );
				AddPanel( tp );

				((GroupNode)e.Current).Decorations[ PanelDecision.DECISION_KEY ] = 
					new PanelDecision( (PanelNode)tp.GetChildNode() );
			}
		}


		/*
		 * Indexor
		 */

		public TabPanelNode this[ GroupNode g ]
		{
			get
			{
				return (TabPanelNode)_groupToTabMap[ g ];
			}
		}


		/*
		 * Member Methods
		 */

		public void InitGroupToTabMap()
		{
			_groupToTabMap = new Hashtable();
			IEnumerator e = _tabNodes.GetEnumerator();
			while( e.MoveNext() )
				_groupToTabMap[ ((PanelNode)((TabPanelNode)e.Current).GetChildNode()).Group ] =	e.Current;
		}

		public TabPanelNode GetNodeByValue( int idx )
		{
			return (TabPanelNode)_tabNodes[ idx - 1 ];
		}

		public ControlBasedCIO GetTabbedCIO()
		{
			return _tabbedPanel;
		}


		/*
		 * InterfaceNode Methods
		 */

		public override void SetLocation(int x, int y)
		{
			base.SetLocation( x, y );

			_tabbedPanel.GetControl().Location = new System.Drawing.Point( x, y );
		}	

		public override void SetSize(int width, int height)
		{
			base.SetSize( width, height );

			_tabbedPanel.GetControl().Size = new System.Drawing.Size( width, height );
		}

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

			// add padding for height of tabs
			_minSize.Height += GetTabbedCIO().GetMinimumSize().Height;
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

			// add padding for height of tabs
			if ( prefHeight != PreferredSize.INFINITE )
				prefHeight += GetTabbedCIO().GetMinimumSize().Height;

			_prefSize = new PUC.Layout.PreferredSize( prefWidth, prefHeight );
		}

		public override void AddComponents( ContainerCIO container, LayoutVariables vars )
		{
			Container = container;
			Container.AddCIO( _tabbedPanel );

			IEnumerator e = _panels.GetEnumerator();
			while( e.MoveNext() )
				((InterfaceNode)e.Current).AddComponents( container, vars );

			this.CalculateMinimumSize( vars );
			this.CalculatePreferredSize( vars );
		}

		public override void DoLayout( LayoutVariables vars )
		{
			IEnumerator e = _panels.GetEnumerator();
			
			Size minSize = GetTabbedCIO().GetMinimumSize();
			Rectangle tabPanelBounds = new Rectangle( 0, 0, _bounds.Width - minSize.Width, _bounds.Height - minSize.Height );

			while( e.MoveNext() )
			{
				InterfaceNode node = (InterfaceNode)e.Current;

				node.SetSize( tabPanelBounds.Width, tabPanelBounds.Height );
				node.SetLocation( tabPanelBounds.X, tabPanelBounds.Y );

				node.DoLayout( vars );
			}
		}

		public override string ToString()
		{
			return "TabbedOverlappingPanelsNode - " + base.ToString ();
		}
	}
}
