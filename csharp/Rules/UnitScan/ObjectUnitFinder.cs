using System;

using PUC;
using PUC.CIO;
using PUC.Rules;
using PUC.Rules.SpecScan;
using PUC.Registry;
using PUC.UIGeneration;


namespace PUC.Rules.UnitScan
{
	/// <summary>
	/// This rule assigns CIOs to appliance objects that are not a
	/// part of a 
	/// </summary>
	public class ObjectUnitFinder : UnitScanRule
	{
		/*
		 * Process Method
		 */

		public override void Process( GroupNode g, UIGenerator ui )
		{
			if ( !g.IsObject() )
				return;

			ObjectGroupNode og = (ObjectGroupNode)g;

			ConcreteInteractionObject cio = ui.Core.ObjectRegistry.ChooseWidget( (ApplianceObject)og.Object );

			if ( cio != null )
			{
				g.Decorations.Add( UnitDecision.DECISION_KEY, new UnitDecision( cio ) );
			}
		}

	}
}
