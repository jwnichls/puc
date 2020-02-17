using System;

using PUC;


namespace PUC.UIGeneration.ListRepresentation
{
	/// <summary>
	/// This type of ListItemNode represents an appliance object
	/// that is included in a list.
	/// </summary>
	public class ObjectListItemNode : ListItemNode
	{
		/*
		 * Member Variables
		 */

		protected ApplianceObject _object;


		/*
		 * Constructor
		 */

		public ObjectListItemNode( ApplianceObject obj )
		{
			_object = obj;
		}


		/*
		 * Properties
		 */

		public ApplianceObject Object
		{
			get
			{
				return _object;
			}
			set
			{
				_object = value;
			}
		}
	}
}
