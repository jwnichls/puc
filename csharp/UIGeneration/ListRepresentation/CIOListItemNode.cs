using System;

using PUC;
using PUC.CIO;


namespace PUC.UIGeneration.ListRepresentation
{
	/// <summary>
	/// This type of ListItemNode represents an appliance object
	/// that is included in a list.
	/// </summary>
	public class CIOListItemNode : ListItemNode
	{
		/*
		 * Member Variables
		 */

		protected ConcreteInteractionObject _cio;


		/*
		 * Constructor
		 */

		public CIOListItemNode( ConcreteInteractionObject cio )
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
			set
			{
				_cio = value;
			}
		}
	}
}
