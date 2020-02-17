using System;

using PUC;


namespace PUC.UIGeneration.ListRepresentation
{
	/// <summary>
	/// This type of ListNode represents a sub-list of items that are 
	/// displayed on a panel and linked with some label.  This is used 
	/// for objects that have CIOs that can only be placed on panels.
	/// It may be used for grouping multiple panel items within a labeled
	/// group as well.
	/// </summary>
	public class PanelListNode : ListNode
	{
		/*
		 * Member Variables
		 */

		protected LabelDictionary _labels;


		/*
		 * Constructor
		 */

		public PanelListNode( LabelDictionary labels )
		{
			_labels = labels;
		}


		/*
		 * Properties
		 */

		public LabelDictionary Labels
		{
			get
			{
				return _labels;
			}
			set
			{
				_labels = value;
			}
		}
	}
}
