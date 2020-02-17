using System;

using PUC;


namespace PUC.UIGeneration.ListRepresentation
{
	/// <summary>
	/// This type of ListNode represents a sub-list of items that are 
	/// linked with some label.  This is typically done when a list of
	/// items is made from a labeled group in the specification.
	/// </summary>
	public class LabeledListNode : ListNode
	{
		/*
		 * Member Variables
		 */

		protected LabelDictionary _labels;


		/*
		 * Constructor
		 */

		public LabeledListNode( LabelDictionary labels )
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
