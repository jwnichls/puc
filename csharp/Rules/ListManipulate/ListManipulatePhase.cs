using System;
using System.Collections;

using PhoneControls;

using PUC;
using PUC.CIO;
using PUC.Rules;
using PUC.UIGeneration;
using PUC.UIGeneration.ListRepresentation;


namespace PUC.Rules.ListManipulate
{
	/// <summary>
	/// This is the implementation of the rule phase for the
	/// phone interface generator which creates a list-based
	/// interface.
	/// </summary>
	public class ListManipulatePhase : RulePhase
	{
		/*
		 * Member Variables
		 */

		protected ArrayList _rules;


		/*
		 * Constructor
		 */

		public ListManipulatePhase()
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

			processHelper( root, ui );
		
			return root;
		}

		protected void processHelper( ListNode node, UIGenerator ui )
		{
			Hashtable _handledNodes = new Hashtable();
			bool keepGoing = true;
			bool changed = false;

			while ( keepGoing )
			{
				changed = false;

				for( int i = 0; i < node.Items.Count; i++ )
				{
					ListItemNode item = (ListItemNode)node.Items[ i ];

					// skip items that have already been handled
					if ( _handledNodes[ item ] != null )
						continue;

					_handledNodes[ item ] = true;

					if ( item is ListNode )
						processHelper( (ListNode)item, ui );

					changed = invokeRules( item, ui );

					if ( changed )
						break;
				}

				// if we were iterating on the root, reload it
				if ( node.Parent == null )
					node = ui.ListRoot;

				if ( !changed )
					keepGoing = false;
			}
		}

		protected bool invokeRules( ListItemNode node, UIGenerator ui )
		{
			bool changed = false;
			bool tempChanged = false;

			IEnumerator e = _rules.GetEnumerator();
			while( e.MoveNext() )
			{
				tempChanged = ((ListManipulateRule)e.Current).Process( node, ui );

				if ( tempChanged )
					changed = true;
			}

			return changed;
		}


		/*
		 * Access to Rules
		 */

		public void AddRule( ListManipulateRule rule )
		{
			_rules.Add( rule );
		}

		public IEnumerator GetRules()
		{
			return _rules.GetEnumerator();
		}
	}
}
