using System;
using System.Collections;
using PUC;

namespace PUC.UIGeneration
{
	/// <summary>
	/// This organizer creates a set of panels that are controlled by a
	/// widget outside of the panels.  It is assumed that such a widget 
	/// exists (and is typically enforced by some manipulation to the group
	/// earlier in the generation process)
	/// </summary>
	public class TabbedControlPanelOrganizer : ControlOrganizer
	{
		/*
		 * Member Variables
		 */
	
		protected UIGenerator _ui;
		protected bool _vertical;


		/*
		 * Constructor
		 */

		public TabbedControlPanelOrganizer( UIGenerator ui,
			ApplianceState state,
			ArrayList children,
			ArrayList dependencies,
			bool vertical
			)
			: base( state )
		{
			_ui = ui;
			_vertical = vertical;

			_dependencies = new Hashtable( children.Count );

			for( int i = 0; i < children.Count; i++ )
			{
				GroupNode g = (GroupNode)children[ i ];
				ArrayList deps = (ArrayList)dependencies[ i ];

				_dependencies[ g ] = deps;
			}
		}


		/*
		 * Organizer Method
		 */

		public override Hashtable AddOrganization(GroupNode group, InterfaceNode currentNode)
		{
			TabbedOverlappingPanelsNode tab = new TabbedOverlappingPanelsNode( _state );
			MultiplePanelNode non = null;

			if ( _vertical )
				non = new HorizontallyNonOverlappingPanelsNode( (PanelNode)currentNode, tab );
			else
			{
				non = new VerticallyNonOverlappingPanelsNode();
				currentNode.InsertAsParent( non );
				non.AddPanel( tab );
			}

			Hashtable deps = new Hashtable( _dependencies.Count );
			Hashtable panels = new Hashtable( _dependencies.Count );

			IEnumerator e = _dependencies.Keys.GetEnumerator();
			PanelNode newP = null;
			while( e.MoveNext() )
			{
				GroupNode g = (GroupNode)e.Current;
				ArrayList aryDeps = (ArrayList)_dependencies[ g ];

				EqualsDependency eqDep = (EqualsDependency)aryDeps[ 0 ];

				try
				{
					newP = (PanelNode)tab.GetNodeByValue( (int)eqDep.Value ).GetChildNode();
					newP.Group = g;
				}
				catch( Exception ) { Globals.GetFrame( eqDep.State.Appliance ).AddLogLine( "Error in TabbedControlPanelOrganizer... Line 82" ); }

				deps[ aryDeps ] = newP;
				panels[ g ] = newP;
			}

			tab.InitGroupToTabMap();

			_activePanel = newP;

			_dependencies = deps;
			_uiValid = true;

			this.ValueChanged( _state );

			return panels;
		}
	}
}
