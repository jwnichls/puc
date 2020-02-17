using System;
using System.Collections;

using PUC;
using PUC.UIGeneration;

namespace PUC.Rules.FixLayout
{
	/// <summary>
	/// Summary description for FixLayoutWithTabsRule.
	/// </summary>
	public class FixLayoutWithTabsRule : FixLayoutRule
	{
		/// <summary>
		/// This rule looks for InsufficientHeight layout problems, and attempts
		/// to fix them by splitting controls onto multiple tabbed panels.
		/// </summary>
		/// <param name="problem">the layout problem to fix</param>
		/// <param name="node">the root of the interface tree</param>
		/// <param name="ui">the UIGenerator object, which contains global variables to the layout process</param>
		public override InterfaceNode Process(LayoutProblem problem, InterfaceNode root, UIGenerator ui)
		{
			if ( problem is InsufficientHeight )
			{
				InsufficientHeight prob = (InsufficientHeight)problem;

				// we will use tabs if the group associated with this panel 
				// has only BranchGroupNodes as children, and each of those
				// groups has labels

				// Step #1: check if the above criteria are true

				if ( prob.Panel.Group is BranchGroupNode )
				{
					BranchGroupNode bg = (BranchGroupNode)prob.Panel.Group;

					if ( !bg.ContainsOnlyGroups() )
						return root;

					while( bg.Count == 1 )
					{
						bg = (BranchGroupNode)bg.Children[ 0 ];

						if ( !bg.ContainsOnlyGroups() )
							return root;
					}

					IEnumerator e = bg.Children.GetEnumerator();
					while( e.MoveNext() )
						if ( ((GroupNode)e.Current).Labels == null )
							return root;

					// If we get here, all of the criteria have been met.

					// Now we insert our tabbed panel.
					TabbedOverlappingPanelsNode tabs = new TabbedOverlappingPanelsNode( bg );
					prob.Panel.Container.GetControl().Controls.Remove( prob.Panel.GetContainerCIO().GetControl() );

					if ( prob.Panel.GetParent() != null )
					{
						prob.Panel.GetParent().AddPanel( tabs );
						prob.Panel.GetParent().RemovePanel( prob.Panel );
					}
					else
					{
						root = tabs;
					}

					// now we need to distribute the rows from the panel into the new tabbed panels
					IEnumerator row = prob.Panel.Rows.GetEnumerator();
					while( row.MoveNext() )
					{
						Row r = (Row)row.Current;
						GroupNode g = r.Group;
						while( tabs[ g ] == null )
							g = g.Parent;

						((PanelNode)tabs[ g ].GetChildNode()).AddRow( r );
					}
					
					// finally, add the components and the do the layout for the tabs
					tabs.SetLocation( prob.Panel.GetBounds().X, prob.Panel.GetBounds().Y );
					tabs.SetSize( prob.Panel.GetBounds().Width, prob.Panel.GetBounds().Height );
					tabs.AddComponents( prob.Panel.Container, ui.LayoutVars );
					tabs.DoLayout( ui.LayoutVars );
				}
			}
			
			return root;
		}
	}
}
