using System;
using System.Collections;
using System.Net;
using System.Windows.Forms;

using PUC.PersistentData;

namespace PUC
{
	/// <summary>
	/// Summary description for Globals.
	/// </summary>
	public class Globals
	{
		public const string REGISTRY_FILE_ATTR = "registry-file";
		public const string RECENT_SERVER_ATTR = "recent-server-list-max";
		public const string RECENT_SERVER_PRFX = "recent-server-";
		public const string RECENT_COUNT_ATTR  = "recent-server-count";

		protected static Hashtable _applianceFrameMap;

		protected static String _versionString = null;
		public static String GetVersionString() { return _versionString; }

		protected static PUC.PersistentData.DataStore _store;
		public static DataStore GetDataStore()
		{
			return _store;
		}

		public const bool SERVER_HIERARCHY = false;

		public static String GetWidgetRegistryFileName() 
		{ 	
			return _store.GetStringData( REGISTRY_FILE_ATTR ); 
		}

		public static int GetRecentServerMaximum()
		{
			return _store.GetIntData( RECENT_SERVER_ATTR );
		}

		public static IPUCFrame GetFrame( Appliance a ) 
		{ 
			return (IPUCFrame)_applianceFrameMap[ a ]; 
		}

		public static void AddFrameMapping( Appliance a, IPUCFrame f )
		{
			_applianceFrameMap[ a ] = f;
		}

		protected static MeasureStringControl _measureControl;

		public static System.Drawing.SizeF MeasureString( string s, System.Drawing.Font f )
		{
			return _measureControl.MeasureString( s, f );
		}

		protected static ILogManager _defaultLogManager;
		public static ILogManager GetDefaultLog()
		{
			return _defaultLogManager;
		}

		protected static IShutdown _shutdownMethod;
		public static void shutdown()
		{
			_shutdownMethod.shutdown();
		}

		protected static ICallbackManager _callbackManager;
		public static void AddEventCallback( IEventDispatcher e )
		{
			_callbackManager.AddEventCallback( e );
		}

		protected static IServerManager _serverManager;
		public static MenuItem GetServerMenu()
		{
			return _serverManager.GetServerMenu();
		}

		public static void AddServer( ServerInfo s )
		{
			_serverManager.AddServer( s );
		}

		public static ServerInfo DoesServerExist( IPAddress ip )
		{
			return _serverManager.DoesServerExist( ip );
		}

#if SMARTPHONE
		public static void PopLeftMenuStack()
		{
			((PhonePUC.PUCFrame)_callbackManager).PopLeftMenuStack();
		}

		public static void PushLeftMenuStack( PhonePUC.LeftMenuStackItem item )
		{
			((PhonePUC.PUCFrame)_callbackManager).PushLeftMenuStack( item );
		}
#endif

		public static void Init( String version, String optionsFilename,
			                     ILogManager defaultLog, IShutdown shutdown,
								 ICallbackManager callbacks,
								 IServerManager servers,
								 MeasureStringControl measure,
								 String defaultRegistryLoc )
		{
			_versionString = version;

			_defaultLogManager = defaultLog;
			_shutdownMethod = shutdown;
			_callbackManager = callbacks;
			_serverManager = servers;
			_measureControl = measure;

			_applianceFrameMap = new Hashtable();

			_store = new PUC.PersistentData.DataStore( optionsFilename );

			if (! _store.IsKeyValid( Globals.REGISTRY_FILE_ATTR ) )
			{
				_store.Set( Globals.REGISTRY_FILE_ATTR, defaultRegistryLoc );
				_store.Set( Globals.RECENT_SERVER_ATTR, 10 );
				_store.Set( Globals.RECENT_COUNT_ATTR, 0 );
			}
		}
	}
}
