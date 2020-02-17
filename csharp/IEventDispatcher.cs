using System;

namespace PUC
{
	/// <summary>
	/// An interface used for dispatching events within the 
	/// PUC protocol.  This is similar to the Listener interface
	/// used in Java event handling.
	/// </summary>
	public interface IEventDispatcher
	{
		void Dispatch();
	}

	/// <summary>
	/// An interface used for dispatching events into the user 
	/// interface thread.
	/// </summary>
	public interface ICallbackManager
	{
		void AddEventCallback( IEventDispatcher e );
	}
}
