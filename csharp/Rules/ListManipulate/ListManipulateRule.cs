using System;

using PhoneControls;

using PUC;
using PUC.CIO;
using PUC.Rules;
using PUC.UIGeneration;
using PUC.UIGeneration.ListRepresentation;


namespace PUC.Rules.ListManipulate
{
	/// <summary>
	/// The base class for all rules used during the SpecChange
	/// rule phase.
	/// </summary>
	public abstract class ListManipulateRule : Rule
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

		/// <summary>
		/// This process methods returns a boolean that indicates whether the 
		/// phase will need to restart its iteration on the current ListNode.
		/// </summary>
		public abstract bool Process( ListItemNode node, UIGenerator ui );
	}
}
