using System;
using System.Collections;

using PUC;
using PUC.Rules;
using PUC.UIGeneration;


namespace PUC.Rules.GroupScan
{
	/// <summary>
	/// The root interface for all organizational decisions.  The code in
	/// these decisions is expected to be invoked by later rules that 
	/// assemble the interface tree.
	/// </summary>
	public abstract class OrganizationDecision : Decision
	{
		/*
		 * Constants
		 */

		public const string DECISION_KEY = "organization";
		

		/*
		 * Constructor
		 */

		public OrganizationDecision()
			: this( null )
		{
		}

		public OrganizationDecision( Decision baseDecision )
			: base( baseDecision )
		{
		}


		/*
		 * Abstract Methods
		 */

		public abstract void AddOrganization( GroupNode group, InterfaceNode currentNode );
	}


	/// <summary>
	/// The base class for all organizational decisions that have a single 
	/// set of overlapping panels.
	/// </summary>
	public abstract class OverlappingOrgDecision : OrganizationDecision
	{
		/*
		 * Member Variables
		 */

		protected ApplianceState _state;

		protected bool           _uiValid;

		protected Hashtable      _dependencies;

		protected PanelNode      _activePanel;


		/*
		 * Constructor
		 */

		public OverlappingOrgDecision( ApplianceState state )
			: this( (Decision)null, state )
		{
		}

		public OverlappingOrgDecision( Decision baseDecision, ApplianceState state )
			: base( baseDecision )
		{
			_state = state;
			_uiValid = false;

			_state.ValueChangedEvent += new ApplianceState.ValueChangedHandler( this.ValueChanged );
		}


		/*
		 * Member Methods
		 */

		public void ValueChanged( ApplianceState state )
		{
			if (! _uiValid ) return;

			IEnumerator en = _dependencies.Keys.GetEnumerator();
			while( en.MoveNext() )
			{
				ArrayList deps = (ArrayList)en.Current;

				IEnumerator en2 = deps.GetEnumerator();
				bool cont = true;
				while( en2.MoveNext() )
				{
					if (! ((Dependency)en2.Current).IsSatisfied() ) 
					{
						cont = false;
						break;
					}
				}

				if ( cont )
				{
					PanelNode c = (PanelNode)_dependencies[ deps ];

					_activePanel.GetContainerCIO().GetControl().Visible = false;
					_activePanel = c;
					_activePanel.GetContainerCIO().GetControl().Visible = true;
					_activePanel.GetContainerCIO().GetControl().BringToFront();

					break;
				}
			}
		}


		/*
		 * Abstract Methods
		 */

		public override abstract void AddOrganization(GroupNode group, InterfaceNode currentNode);
	}


	/// <summary>
	/// This organization decision creates a set of panels that are controlled 
	/// by a widget outside of the panels.  It can be assumed that such a widget 
	/// exists (this is enforced by the rule that generates this decision)
	/// </summary>
	public class ExternalOverlapOrgDecision : OverlappingOrgDecision
	{
		/*
		 * Member Variables
		 */

		/*
		 * Constructor
		 */

		public ExternalOverlapOrgDecision( 
			Decision baseDecision,
			ApplianceState state,
			ArrayList children,
			ArrayList dependencies
			)
			: base( baseDecision, state )
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

		public override void AddOrganization(GroupNode group, InterfaceNode currentNode)
		{
			VerticallyNonOverlappingPanelsNode non = new VerticallyNonOverlappingPanelsNode();
			OverlappingPanelsNode over = new OverlappingPanelsNode();

			currentNode.InsertAsParent( non );
			non.AddPanel( over );

			Hashtable deps = new Hashtable( _dependencies.Count );

			IEnumerator e = _dependencies.Keys.GetEnumerator();
			PanelNode newP = null;
			while( e.MoveNext() )
			{
				GroupNode g = (GroupNode)e.Current;
				ArrayList aryDeps = (ArrayList)_dependencies[ g ];

				newP = new PanelNode( g );

				over.AddPanel( newP );

				deps[ aryDeps ] = newP;
				g.Decorations.Add( PanelDecision.DECISION_KEY, new PanelDecision( this, newP ) );
			}

			_activePanel = newP;

			_dependencies = deps;
			_uiValid = true;

			this.ValueChanged( _state );
		}
	}


	/// <summary>
	/// This decision creates a set of panels that are controlled by a
	/// widget within the panels.  It can be assumed that such a widget 
	/// exists (this is enforced by the rule that made this decision)
	/// </summary>
	public class InternalOverlapOrgDecision : OverlappingOrgDecision
	{
		/*
		 * Member Variables
		 */

		/*
		 * Constructor
		 */

		public InternalOverlapOrgDecision( 
			Decision baseDecision,
			ApplianceState state,
			ArrayList children,
			ArrayList dependencies
			)
			: base( baseDecision, state )
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

		public override void AddOrganization(GroupNode group, InterfaceNode currentNode)
		{
			VerticallyNonOverlappingPanelsNode non = new VerticallyNonOverlappingPanelsNode();
			OverlappingPanelsNode over = new OverlappingPanelsNode();

			currentNode.InsertAsParent( non );
			non.AddPanel( over );

			Hashtable deps = new Hashtable( _dependencies.Count );

			IEnumerator e = _dependencies.Keys.GetEnumerator();
			PanelNode newP = null;
			while( e.MoveNext() )
			{
				GroupNode g = (GroupNode)e.Current;
				ArrayList aryDeps = (ArrayList)_dependencies[ g ];

				newP = new PanelNode( g );

				over.AddPanel( newP );

				deps[ aryDeps ] = newP;
				g.Decorations.Add( PanelDecision.DECISION_KEY, new PanelDecision( this, newP ) );
			}

			_activePanel = newP;

			_dependencies = deps;
			_uiValid = true;

			this.ValueChanged( _state );
		}
	}


	/// <summary>
	/// This organizer creates a set of panels that are controlled by a 
	/// TabControl that is linked to an ApplianceState.
	/// </summary>
	public class TabbedOverlapOrgDecision : OverlappingOrgDecision
	{
		/*
		 * Member Variables
		 */
	
		protected UIGenerator _ui;
		protected bool _vertical;


		/*
		 * Constructor
		 */

		public TabbedOverlapOrgDecision( 
			Decision baseDecision,
			UIGenerator ui,
			ApplianceState state,
			ArrayList children,
			ArrayList dependencies,
			bool vertical
			)
			: base( baseDecision, state )
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

		public override void AddOrganization(GroupNode group, InterfaceNode currentNode)
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
				g.Decorations.Add( PanelDecision.DECISION_KEY, new PanelDecision( this, newP ) );
			}

			_activePanel = newP;

			_dependencies = deps;
			_uiValid = true;

			this.ValueChanged( _state );
		}
	}

	public class PanelDecision : Decision
	{
		/*
		 * Constants
		 */

		public const string DECISION_KEY = "panel";


		/*
		 * Member Variables
		 */

		protected PanelNode _panel;


		/*
		 * Constructor
		 */

		public PanelDecision( PanelNode p )
			: this( (Decision)null, p )
		{
		}

		public PanelDecision( Decision baseDecision, PanelNode p )
			: base( baseDecision )
		{
			_panel = p;
		}


		/*
		 * Properties
		 */

		public PanelNode Panel
		{
			get
			{
				return _panel;
			}
		}
	}
}
