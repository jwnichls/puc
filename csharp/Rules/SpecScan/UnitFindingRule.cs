using System;

using PUC;
using PUC.CIO;
using PUC.Registry;
using PUC.Rules;
using PUC.UIGeneration;


namespace PUC.Rules.SpecScan
{
	/// <summary>
	/// This rule finds "units" within the specification.  Units are
	/// defined to be
	/// </summary>
	public class UnitFindingRule : SpecScanRule
	{
		/*
		 * Constants
		 */


		/*
		 * Process Method
		 */
		public override void Process( Appliance a )
		{
			// start searching for units
			searchHelper( a.GetRoot(), a );			
		}

		protected void searchHelper( GroupNode g, Appliance a )
		{
			if ( g.Decorations[ UnitDecision.DECISION_KEY ] != null )
				return;

			if ( g.HasType() ) 
			{
				SmartCIO cio = a.GetUIGenerator().Core.SmartCIOMgr.GetSmartCIO( g.HighlevelType, g );

				if ( cio != null ) 
				{
					cio.TemplateGroup.Decorations[ UnitDecision.DECISION_KEY ] = new UnitDecision( cio );
				}
			}

			if ( g.IsObject() && ((ObjectGroupNode)g).Object.HighlevelType != null )
			{
				SmartCIO cio = a.GetUIGenerator().Core.SmartCIOMgr.GetSmartCIO( ((ObjectGroupNode)g).Object.HighlevelType, g );

				if ( cio != null )
				{
					cio.TemplateGroup.Decorations[ UnitDecision.DECISION_KEY ] = new UnitDecision( cio );
				}
			}
			else if ( !g.IsObject() )
			{
				BranchGroupNode bg = (BranchGroupNode)g;

				if ( bg.Count == 0 ) return;

				for( int i = 0; i < bg.Children.Count; i++ )
				{
					searchHelper( (GroupNode)bg.Children[ i ], a );
				}
			}
		}
	}

	/// <summary>
	/// The result of the UnitFindingRule.  This decision encapsulates the
	/// ConcreteInteractionObject that was chosen for representing the unit.
	/// </summary>
	public class UnitDecision : Decision
	{
		/*
		 * Constants
		 */

		public const string DECISION_KEY = "unit";


		/*
		 * Member Variables
		 */

		protected ConcreteInteractionObject _cio;


		/*
		 * Constructor
		 */

		public UnitDecision( ConcreteInteractionObject cio )
			: this( null, cio )
		{
		}

		public UnitDecision( 
			Decision baseDecision,
			ConcreteInteractionObject cio )
			: base( baseDecision )
		{
			_cio = cio;
		}


		/*
		 * Properties
		 */

		public ConcreteInteractionObject CIO
		{
			get
			{
				return _cio;
			}
		}
	}
}
