using System;
using System.Collections;

using PhoneControls;

using PUC;
using PUC.CIO;
using PUC.Rules;
using PUC.Rules.SpecScan;
using PUC.UIGeneration.ListRepresentation;


namespace PUC.Rules.BuildList
{
	/// <summary>
	/// Summary description for PutAllObjectsInListRule.
	/// </summary>
	public class ObjectsInListRule : BuildListRule
	{
		/*
		 * Process Method
		 */

		public override ListNode Process( GroupNode group, ListNode list )
		{
			if ( group.Decorations[ UnitDecision.DECISION_KEY ] != null )
			{
				// get the values that we need
				UnitDecision decision = 
					(UnitDecision)group.Decorations[ UnitDecision.DECISION_KEY ];

				CIOListItemNode item = new CIOListItemNode( decision.CIO );
				item.Decorations[ GroupDecision.DECISION_KEY ] = 
					new GroupDecision( group );
				
				// determine whether this CIO should be on a panel
				bool onPanel = decision.CIO is ControlBasedCIO;

                item.Decorations[ PanelDecision.DECISION_KEY ] = 
					new PanelDecision( decision, onPanel );

				if ( onPanel )
				{
					LabelDictionary labels;
					if ( decision.CIO is SmartCIO )
						labels = ((SmartCIO)decision.CIO).Labels;
					else
						labels = ((ApplianceState)((StateLinkedCIO)decision.CIO).GetApplObj()).Labels;

					if ( labels != null )
					{
						PanelListNode pNode = new PanelListNode( labels );
						pNode.Add( item );
						list.Add( pNode );
					}
				}
				else
					list.Add( item );
			}

			return list;
		}
	}


	/// <summary>
	/// A result of the ObjectsInListRule that informs later rules
	/// whether this list item must be located in a panel or a list.
	/// </summary>
	public class PanelDecision : Decision
	{
		/*
		 * Constants
		 */

		public const string DECISION_KEY = "panel";


		/*
		 * Member Variables
		 */

		protected bool _panel;


		/*
		 * Constructor
		 */

		public PanelDecision( bool panel )
			: this( null, panel )
		{
		}

		public PanelDecision( 
			Decision baseDecision,
			bool panel )
			: base( baseDecision )
		{
			_panel = panel;
		}


		/*
		 * Properties
		 */

		public bool IsPanel
		{
			get
			{
				return _panel;
			}
		}
	}


	/// <summary>
	/// A result of the ObjectsInListRule that links the 
	/// nodes created to the group tree.
	/// </summary>
	public class GroupDecision : Decision
	{
		/*
		 * Constants
		 */

		public const string DECISION_KEY = "group-link-decision";


		/*
		 * Member Variables
		 */

		protected GroupNode _group;


		/*
		 * Constructor
		 */

		public GroupDecision( GroupNode group )
			: this( null, group )
		{
		}

		public GroupDecision( 
			Decision baseDecision,
			GroupNode group )
			: base( baseDecision )
		{
			_group = group;
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
		}
	}
}
