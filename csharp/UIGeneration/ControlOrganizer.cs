using System;
using System.Collections;
using PUC;

namespace PUC.UIGeneration
{
	/// <summary>
	/// Summary description for ControlOrganizer.
	/// </summary>
	public abstract class ControlOrganizer : IOrganizer
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

		public ControlOrganizer( ApplianceState state )
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

					break;
				}
			}
		}


		/*
		 * Organizer Method
		 */

		public abstract Hashtable AddOrganization( GroupNode group, InterfaceNode currentNode );
	}
}
