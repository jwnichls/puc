using System;
using System.Collections;

using PhoneControls;

using PUC;
using PUC.CIO;
using PUC.Rules;

namespace PUC.Rules.BuildList
{
	/// <summary>
	/// Summary description for PutAllObjectsInListRule.
	/// </summary>
	public class PutAllObjectsInListRule : BuildListRule
	{
		/*
		 * Process Method
		 */

		public override void Process( Appliance a, PhoneListViewCIO cio )
		{
			IEnumerator e = a.GetObjects().Values.GetEnumerator();
			while( e.MoveNext() )
			{
				PhoneListViewItem item = 
					new PhoneListViewItem( a, (ApplianceObject)e.Current );

				cio.AddItem( item );
			}
		}
	}
}
