using System;
using System.Collections;
using System.Windows.Forms;

using PocketPCControls;

using PUC;
using PUC.CIO;
using PUC.Layout;


namespace PUC.UIGeneration
{
	/// <summary>
	/// This class describes interface nodes that represent a
	/// single panel.
	/// </summary>
	public class PanelNode : InterfaceNode
	{
		/*
		 * Member Variables
		 */

		protected GroupNode			_group;
		protected ArrayList         _rows;
		protected ScrollingPanelCIO _panel;
		protected bool              _vertical;
		protected bool		        _doneLayout;
		protected int				_updateScroll;
		protected LayoutVariables   _savedVars;


		/*
		 * Constructor
		 */

		public PanelNode( GroupNode group )
		{
			_group = group;
			_rows = new ArrayList();
			_panel = new ScrollingPanelCIO();
			_vertical = false;
			_doneLayout = false;
			_updateScroll = 0;

			((ScrollingPanel)_panel.GetControl()).DisplayPolicy = ScrollBarDisplay.Never;
			((ScrollingPanel)_panel.GetControl()).ClientResized += new EventHandler(PanelNode_ClientResized);
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
			set
			{
				_group = value;
			}
		}

		public ArrayList Rows
		{
			get
			{
				return _rows;
			}
		}


		/*
		 * Member Methods
		 */

		public void AddRow( Row row )
		{
			_rows.Add( row );
		}

		public ContainerCIO GetContainerCIO()
		{
			return _panel;
		}

		public bool IsVertical()
		{
			return _vertical;
		}

		public void SetVertical( bool vertical )
		{
			_vertical = vertical;
		}


		/*
		 * InterfaceNode Methods
		 */

		public override void SetLocation(int x, int y)
		{
			base.SetLocation (x, y);

			if ( _panel != null )
				_panel.GetControl().Location = new System.Drawing.Point( x, y );
		}

		public override void SetSize(int width, int height)
		{
			base.SetSize (width, height);

			if ( _panel != null )
				_panel.GetControl().Size = new System.Drawing.Size( width, height );
		}

		public override void CalculateMinimumSize(LayoutVariables vars)
		{
			IEnumerator e = _rows.GetEnumerator();
			
			_minSize = new System.Drawing.Size( 0, 0 );

			Row lastRow = null;
			while( e.MoveNext() )
			{
				((Row)e.Current).CalculateMinimumSize( vars );

				System.Drawing.Size size = ((Row)e.Current).MinimumSize;

				_minSize.Width = Math.Max( size.Width, _minSize.Width );
				_minSize.Height += size.Height;

				if ( lastRow != null )
				{
					int baselineSpace = ( lastRow.MinimumSize.Height - lastRow.MaximumTextOffset ) + 
						((Row)e.Current).MaximumTextOffset + vars.RowPadding;
					
					if ( baselineSpace < vars.BaselineSpacing )
						_minSize.Height += vars.BaselineSpacing - baselineSpace;
				}

				lastRow = (Row)e.Current;
			}
		}

		public override void CalculatePreferredSize(LayoutVariables vars)
		{
			IEnumerator e = _rows.GetEnumerator();
			
			int prefWidth = 0;
			int prefHeight = 0;

			Row lastRow = null;
			while( e.MoveNext() )
			{
				((Row)e.Current).CalculatePreferredSize( vars );

				PreferredSize size = ((Row)e.Current).PreferredSize;

				if ( prefWidth == PreferredSize.INFINITE || size.Width == PreferredSize.INFINITE )
					prefWidth = PreferredSize.INFINITE;
				else
					prefWidth = Math.Max( size.Width, prefWidth );

				if ( prefHeight == PreferredSize.INFINITE || size.Height == PreferredSize.INFINITE )
					prefHeight = PreferredSize.INFINITE;
				else
				{
					prefHeight += size.Height;

					if ( lastRow != null )
					{
						int baselineSpace = ( lastRow.PreferredSize.Height - lastRow.MaximumTextOffset ) + 
							((Row)e.Current).MaximumTextOffset + vars.RowPadding;
						
						if ( baselineSpace < vars.BaselineSpacing )
							prefHeight += vars.BaselineSpacing - baselineSpace;
					}

					lastRow = (Row)e.Current;
				}
			}

			_prefSize = new PUC.Layout.PreferredSize( prefWidth, prefHeight );
		}

		public override void AddComponents( ContainerCIO container, LayoutVariables vars )
		{
			Container = container;
			Container.AddCIO( _panel );

			IEnumerator e = _rows.GetEnumerator();
			while( e.MoveNext() )
				((Row)e.Current).AddComponents( _panel, vars );

			this.CalculateMinimumSize( vars );
			this.CalculatePreferredSize( vars );
		}

		public override void DoLayout( LayoutVariables vars )
		{
			_savedVars = vars;
			_doneLayout = true;

			int[] minimum = new int[ _rows.Count ];
			int[] preferred = new int[ _rows.Count ];
			int[] offsets = new int[ _rows.Count ];

			for( int i = 0; i < _rows.Count; i++ )
			{
				minimum[ i ] = ((Row)_rows[ i ]).MinimumSize.Height;
				preferred[ i ] = ((Row)_rows[ i ]).PreferredSize.Height;
				offsets[ i ] = ((Row)_rows[ i ]).MaximumTextOffset;
			}

			int[] heights = LayoutAlgorithms.AllocateSizeValues( _bounds.Height - 2 * vars.RowPadding, minimum, preferred, offsets, vars.RowPadding, vars.BaselineSpacing );

			int height = vars.RowPadding;
			for( int i = 0; i < _rows.Count; i++ )
			{
				Row r = (Row)_rows[ i ];

				r.SetLocation( _bounds.X + vars.RowPadding, _bounds.Y + height );
				r.SetSize( _bounds.Width - 2 * vars.RowPadding, heights[ i ] );

				if ( ( i + 1 ) < _rows.Count )
				{
					int baselineSpace = ( heights[ i ] - r.MaximumTextOffset ) + 
						((Row)_rows[ i + 1 ]).MaximumTextOffset + vars.RowPadding;
					
					if ( baselineSpace < vars.BaselineSpacing )
					{
						int incr = vars.BaselineSpacing - baselineSpace;
						if ( r.PreferredSize.Height != PreferredSize.INFINITE )
							r.SetSize( r.GetBounds().Width, 
								Math.Min( r.GetBounds().Height + incr, r.PreferredSize.Height ) );
						else
							r.SetSize( r.GetBounds().Width,
								r.GetBounds().Height + incr );

						height += vars.BaselineSpacing - baselineSpace;
					}
				}

				height += heights[ i ] + vars.RowPadding;

				r.DoLayout( Container, vars );
			}

			if ( height > _bounds.Height )
				vars.LayoutProblems[ this ] = new InsufficientHeight( this );

			if ( _updateScroll < 3 )
				((ScrollingPanel)_panel.GetControl()).UpdateScroll();
		}

		public override string ToString()
		{
			string ret = "PanelNode: - [" + _panel.GetControl().Bounds.X + "," + _panel.GetControl().Bounds.Y + "," + _panel.GetControl().Bounds.Width + "," + _panel.GetControl().Bounds.Height + "]\r\n";

			for( int i = 0; i < _rows.Count; i++ )
				ret += _rows[ i ].ToString();

			return ret;
		}

		private void PanelNode_ClientResized(object sender, EventArgs e)
		{
			if ( _doneLayout )
			{
				_updateScroll++;
				_bounds.Width = ((ScrollingPanel)_panel.GetControl()).InternalSize.Width;
				_bounds.Height = ((ScrollingPanel)_panel.GetControl()).InternalSize.Height;

				DoLayout( _savedVars );
				_updateScroll--;
			}
		}
	}
}
