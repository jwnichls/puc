using System;
using System.Collections;

namespace PUC
{
	/// <summary>
	/// This interface defines the methods that an object must 
	/// implement to be decorated by one or more other objects.
	/// This interface is implemented by the base class 
	/// Decorator.  This is the implementation of the Decorator
	/// design pattern.
	/// </summary>
	public interface IDecorator
	{
		Hashtable Decorations
		{
			get;
		}
	}

	/// <summary>
	/// The Decorator is a class that implements the 
	/// IDecorator interface.  It is used as the base 
	/// class for a number of data structure classes.
	/// </summary>
	public abstract class Decorator : IDecorator
	{
		/*
		 * Member Variables
		 */

		private Hashtable _decorators;


		/*
		 * Constructor
		 */

		public Decorator()
		{
			_decorators = new Hashtable();
		}


		/*
		 * IDecorator
		 */

		public Hashtable Decorations
		{
			get
			{
				return _decorators;
			}
		}
	}
}
