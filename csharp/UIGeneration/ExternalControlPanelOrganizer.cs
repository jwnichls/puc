using System;
using System.Collections;

namespace PUC.UIGeneration
{
	/// <summary>
	/// This organizer creates a set of panels that are controlled by a
	/// widget outside of the panels.  It is assumed that such a widget 
	/// exists (and is typically enforced by some manipulation to the group
	/// earlier in the generation process)
	/// </summary>
	public class ExternalControlPanelOrganizer : ControlOrganizer
	{
		/*
		 * Member Variables
		 */

		/*
		 * Constructor
		 */

		public ExternalControlPanelOrganizer( ApplianceState state,
											  ArrayList children,
											  ArrayList dependencies
											 )
			: base( state )
		{
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
			VerticallyNonOverlappingPanelsNode non = new VerticallyNonOverlappingPanelsNode();
			OverlappingPanelsNode over = new OverlappingPanelsNode();

			currentNode.InsertAsParent( non );
			non.AddPanel( over );

			Hashtable deps = new Hashtable( _dependencies.Count );
			Hashtable panels = new Hashtable( _dependencies.Count );

			IEnumerator e = _dependencies.Keys.GetEnumerator();
			PanelNode newP = null;
			while( e.MoveNext() )
			{
				GroupNode g = (GroupNode)e.Current;
				ArrayList aryDeps = (ArrayList)_dependencies[ g ];

				newP = new PanelNode( g );

				over.AddPanel( newP );

				deps[ aryDeps ] = newP;
				panels[ g ] = newP;
			}

			_activePanel = newP;

			_dependencies = deps;
			_uiValid = true;

			this.ValueChanged( _state );

			return panels;
		}

	}
}
