using System;

namespace PUC
{
	/// <summary>
	/// An interface for objects that implement functions
	/// for keeping a log.
	/// </summary>
	public interface ILogManager
	{
		void AddLogLine( string text );
		void AddLogText( string text );

		void HideLogPanel();
		void ShowLogPanel();
	}
}
