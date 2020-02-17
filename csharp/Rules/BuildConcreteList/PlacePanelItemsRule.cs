using System;
using System.Collections;

using PhoneControls;

using PUC;
using PUC.CIO;
using PUC.Communication;
using PUC.Rules;
using PUC.Rules.BuildList;
using PUC.Types;
using PUC.UIGeneration;
using PUC.UIGeneration.ListRepresentation;


namespace PUC.Rules.BuildConcreteList
{
	/// <summary>
	/// Summary description for PutAllObjectsInListRule.
	/// </summary>
	public class PlacePanelItemsRule : BuildConcreteListRule
	{
		/*
		 * Constants
		 */

		/// <summary>
		/// Use this because we don't have any reliable information about
		/// what height a given component will prefer.
		/// </summary>
		protected const int CONTROL_HEIGHT = 25;		


		/*
		 * Process Method
		 */

		public override ConcreteInteractionObject Process( ListItemNode node, 
														   ConcreteInteractionObject cio,
														   UIGenerator ui )
		{
			if ( node.Decorations[ ItemDecision.DECISION_KEY ] == null &&
				 node is CIOListItemNode &&
				 cio is ScrollingPanelCIO )
			{
				// the item node represents an appliance object and it will be
				// placed on the ScrollingPanelCIO
				CIOListItemNode item = (CIOListItemNode)node;
				ScrollingPanelCIO panel = (ScrollingPanelCIO)cio;
				LabelDictionary labels;
				if ( item.CIO is StateLinkedCIO )
					labels = ((ApplianceState)((StateLinkedCIO)item.CIO).GetApplObj()).Labels;
				else if ( item.CIO is SmartCIO )
					labels = ((SmartCIO)item.CIO).Labels;

				// get information about the bottom of the current 
				// panel's layout
				PanelLayoutDecision panelDecision = 
					(PanelLayoutDecision)node.Parent.Decorations[ PanelLayoutDecision.DECISION_KEY ];

				if ( panelDecision == null )
				{
					// make one
					panelDecision = new PanelLayoutDecision();
					node.Parent.Decorations.Add( PanelLayoutDecision.DECISION_KEY, panelDecision );
				}

				LabelCIO label = null;
				if ( item.CIO.HasLabel() )
				{
					label = (LabelCIO)item.CIO.GetLabelCIO();
					panel.AddCIO( label );
				}
				
				ControlBasedCIO control = (ControlBasedCIO)item.CIO;
				panel.AddCIO( control );

				// do some sizing here
				int width = ui.Size.Width - 2*ui.LayoutVars.RowPadding - 5;

				if ( item.CIO.HasLabel() )
				{
					// layout label CIO
					label.GetControl().Location = 
						new System.Drawing.Point( ui.LayoutVars.RowPadding, panelDecision.Bottom );
					label.GetControl().Size = 
						new System.Drawing.Size( width, label.GetMinimumSize().Height );
					panelDecision.Bottom += label.GetControl().Size.Height + ui.LayoutVars.RowPadding ;

					label.FinalSizeNotify();
				}

				control.GetControl().Location = 
					new System.Drawing.Point( ui.LayoutVars.RowPadding, panelDecision.Bottom );
				control.GetControl().Size = 
					new System.Drawing.Size( width, ((ControlBasedCIO)item.CIO).GetMinimumSize().Height );
				panelDecision.Bottom += ((ControlBasedCIO)item.CIO).GetMinimumSize().Height + 2 * ui.LayoutVars.RowPadding;

				item.Decorations[ ItemDecision.DECISION_KEY ] = 
					new PanelItemDecision( item.CIO );
			}

			return cio;
		}
	}


}
