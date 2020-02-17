using System;
using System.Net;
using System.Windows.Forms;

namespace PUC
{
	/// <summary>
	/// Methods for managing the different servers that a 
	/// PUC connects to.
	/// </summary>
	public interface IServerManager
	{
		MenuItem GetServerMenu();
		void AddServer( ServerInfo s );
		ServerInfo DoesServerExist( IPAddress ip );
	}
}
