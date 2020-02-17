using System;
using System.Collections;

using PhoneControls;

using PUC;
using PUC.CIO;
using PUC.Rules;
using PUC.UIGeneration;
using PUC.UIGeneration.ListRepresentation;


namespace PUC.Rules.BuildConcreteList
{
	/// <summary>
	/// This is the implementation of the rule phase for the
	/// phone interface generator which creates a list-based
	/// interface.
	/// </summary>
	public class BuildConcreteListPhase : RulePhase
	{
		/*
		 * Member Variables
		 */

		protected ArrayList _rules;


		/*
		 * Constructor
		 */

		public BuildConcreteListPhase()
		{
			_rules = new ArrayList();
		}


		/*
		 * Process Method
		 */

		/// <summary>
		/// Input to this rule phase is an appliance object, but the input to
		/// the rules of this phase are GroupNode objects.  This phase iterates
		/// down the group tree depth first, feeding each rule the GroupNodes as
		/// it goes.
		/// </summary>
		/// <param name="o">an appliance object</param>
		/// <param name="ui">a UIGenerator object that contains global variables for this process</param>
		/// <returns></returns>
		public override object Process(object o, UIGenerator ui )
		{
			ListNode root = (ListNode)o;

			// setup the initial list CIO
			PhoneListViewCIO list = new PhoneListViewCIO();

			list.GetControl().Size = ui.Size;
			list.GetControl().Location = new System.Drawing.Point( 0, 0 );

			ui.Panel.AddCIO( list );

			processHelper( root, list, ui );
		
			return list;
		}

		protected void processHelper( ListNode node, ConcreteInteractionObject cio, UIGenerator ui )
		{
			IEnumerator e = node.Items.GetEnumerator();
			while( e.MoveNext() )
			{
				ConcreteInteractionObject newCIO = cio;
				ListItemNode item = (ListItemNode)e.Current;

				newCIO = invokeRules( item, newCIO, ui );

				if ( item is ListNode )
					processHelper( (ListNode)item, newCIO, ui );
			}
		}

		protected ConcreteInteractionObject invokeRules( ListItemNode node, 
														 ConcreteInteractionObject cio, 
														 UIGenerator ui )
		{
			IEnumerator e = _rules.GetEnumerator();
			while( e.MoveNext() )
			{
				cio = ((BuildConcreteListRule)e.Current).Process( node, cio, ui );
			}

			return cio;
		}


		/*
		 * Access to Rules
		 */

		public void AddRule( BuildConcreteListRule rule )
		{
			_rules.Add( rule );
		}

		public IEnumerator GetRules()
		{
			return _rules.GetEnumerator();
		}
	}
}
