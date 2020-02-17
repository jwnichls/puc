using System;

using PhoneControls;

using PUC;
using PUC.CIO;
using PUC.Rules;
using PUC.UIGeneration;
using PUC.UIGeneration.ListRepresentation;


namespace PUC.Rules.BuildConcreteList
{
	/// <summary>
	/// The base class for all rules used during the SpecChange
	/// rule phase.
	/// </summary>
	public abstract class BuildConcreteListRule : Rule
	{
		/*
		 * Member Variables
		 */

		/*
		 * Constructor
		 */

		/*
		 * Process Rule Method
		 */

		public abstract ConcreteInteractionObject Process( ListItemNode node, 
														   ConcreteInteractionObject cio,
														   UIGenerator ui );
	}
}
