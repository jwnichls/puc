using System;
using System.Collections;
using System.Net;

using PUC.Communication;

namespace PUC
{
	/// <summary>
	/// The C# equivalent of the Device2 interface from the
	/// Java codebase.
	/// </summary>
	public interface IDevice2
	{
		/// <summary>
		/// The human-readable name of the device supported by this class.
		/// </summary>
		string Name
		{
			get;
		}

		/// <summary>
		/// The XML specification of this device's functions.
		/// </summary>
		string Specification
		{
			get;
		}

		/// <summary>
		/// Method called by PUCServer handle an incoming message.
		/// </summary>
		/// <param name="c">Connection the message originated from.</param>
		/// <param name="m">The message to handle.</param>
		void HandleMessage( Connection c, Message m );

		/// <summary>
		/// Add a connection.
		/// </summary>
		/// <param name="c">the connection to add</param>
		void AddConnection( Connection c );

		/// <summary>
		/// Remove a connection.
		/// </summary>
		/// <param name="c">the connection to remove</param>
		void RemoveConnection( Connection c );

		/// <summary>
		/// Remove all connections to this device.
		/// </summary>
		void RemoveAllConnections();

		/// <summary>
		/// Take steps to configure the device (i.e. pop up a configuration dialog)
		/// </summary>
		void Configure();

		/// <summary>
		/// Determine whether the device has a (non-configuration) GUI.
		/// </summary>
		/// <returns></returns>
		bool HasGUI();

		/// <summary>
		/// Set the visiblity of the device GUI.
		/// </summary>
		/// <param name="visible">the new visibility</param>
		void SetGUIVisiblity( bool visible );

		/// <summary>
		/// Determine whether the device's GUI is currently visible on the screen.
		/// </summary>
		/// <returns></returns>
		bool IsGUIVisible();

		/// <summary>
		/// Begin reporting state and accepting messages.
		/// </summary>
		void Start();

		/// <summary>
		/// Stop reporting state and accepting messages.
		/// </summary>
		void Stop();

		/// <summary>
		/// Determines whether the device is running.
		/// </summary>
		/// <returns></returns>
		bool IsRunning();

		/// <summary>
		/// the status of this device
		/// </summary>
		string Status
		{
			get;
		}

		/// <summary>
		/// An event that allows other object to monitor when the status of this device changes.
		/// </summary>
		event EventHandler StatusChanged;

		/// <summary>
		/// the port that clients will connect to in order to exchange messages with this device
		/// </summary>
		int Port
		{
			get;
			set;
		}
	}
}
