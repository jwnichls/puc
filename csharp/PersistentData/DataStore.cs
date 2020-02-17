using System;
using System.Collections;
using System.IO;
using System.Xml;

namespace PUC.PersistentData
{
	/// <summary>
	/// PersistentData is an object that maintains information that 
	/// persists across different executions of the PUC.  This could 
	/// be used for storing preferences information, caching 
	/// specifications for offline generation of interfaces, or saving 
	/// user changes to a generated interface.
	/// </summary>
	
	/*
	 * Persistent data is stored as an XML file specified in the 
	 * constructor of this object.  The format of this file is
	 * (roughly):
	 *   <chunk>
	 *     <key>string-based key</key>
	 *	   <value type="string|int|double|xml">
	 *       the value (depends on type)
	 *	   </value>
	 *	 </chunk>
	 *   <chunk> ... </chunk>
	 */

	public class DataStore
	{
		/*
		 * Constants
		 */

		public const string PUC_DATA_FILE = ".\\pucdata.xml";

		public const string DATA_STRING = "data";
		public const string CHUNK_STRING = "chunk";
		public const string KEY_STRING   = "key";
		public const string VALUE_STRING = "value";

		public const string TYPE_ATTR    = "type";
		public const string TYPE_STRING  = "string";
		public const string TYPE_INT     = "int";
		public const string TYPE_DOUBLE  = "double";
		public const string TYPE_XML     = "xml";

		/*
		 * Member Variables
		 */

		/// <summary>
		/// The name of the file persistent data is stored in and retrieved
		/// from.
		/// </summary>
		protected string    _filename;

		/// <summary>
		/// The state of this persistent data store.  Data cannot be stored
		/// or retrieved from an invalid store.
		/// </summary>
		protected bool      _valid;

		/// <summary>
		/// The data stored by the structure.  The hashtable keys are those 
		/// given in the data store, and the values are classes that contain
		/// the type and data.
		/// </summary>
		protected Hashtable _data;

		/// <summary>
		/// The current write head into the persistent file.  This is used 
		/// for appending new chunks.
		/// </summary>
		protected StreamWriter _writehead;

		/// <summary>
		/// Specifies whether the system should write the store to disk after
		/// every change.  Used by the BeginSet and CommitSet methods.
		/// </summary>
		protected bool _noWriteMode;

		/*
		 * Constructor
		 */

		public DataStore( string file )
		{
			_filename = Path.GetFullPath( file );
			_noWriteMode = false;

			LoadFile();

			SetWriteHead( true );
		}

		~DataStore()
		{
			if ( _writehead == null ) return;

			_writehead.Close();
		}


		/*
		 * Member Methods
		 */

		public void BeginSet()
		{
			_noWriteMode = true;
		}

		public void CommitSet()
		{
			if ( _noWriteMode )
			{				
				_noWriteMode = false;

				RewriteFile();
			}
		}

		protected void SetWriteHead( bool append )
		{
			if ( _writehead != null )
				_writehead.Close();

			_writehead = new StreamWriter( _filename, append );

			if ( !append )
			{
				_writehead.WriteLine( "<?xml version=\"1.0\" encoding=\"utf-8\"?>" );
				_writehead.WriteLine( "<" + DATA_STRING + ">" );
			}
		}

		public bool IsKeyValid( string key )
		{
			return _data[ key ] != null;
		}

		public NodeType GetKeyType( string key )
		{
			return ((DataNode)_data[ key ]).Type;
		}

		public string GetStringData( string key )
		{
			if (! IsKeyValid( key ) ) return null;

			return ((DataNode)_data[ key ]).GetStringValue();
		}

		public int GetIntData( string key )
		{
			return ((DataNode)_data[ key ]).GetIntValue();
		}

		public double GetDoubleData( string key )
		{
			return ((DataNode)_data[ key ]).GetDoubleValue();
		}

		public XmlTextReader GetXmlReader( string key )
		{
			return ((DataNode)_data[ key ]).GetXmlReader();
		}

		public void Set( string key, string data ) 
		{
			StringNode s = new StringNode( data );

			StoreData( key, s );
		}

		public void Set( string key, int data )
		{
			IntNode i = new IntNode( data );

			StoreData( key, i );
		}

		public void Set( string key, double data )
		{
			DoubleNode d = new DoubleNode( data );

			StoreData( key, d );
		}

		protected void StoreData( string key, DataNode d )
		{
			if (! IsKeyValid( key ) )
			{
				if (! _noWriteMode )
					WriteNodeToFile( key, d );

				_data[ key ] = d;
			}
			else
			{
				_data[ key ] = d;

				if (! _noWriteMode )
					RewriteFile();
			}
		}

		protected void WriteNodeToFile( string key, DataNode d )
		{
			_writehead.WriteLine( "<" + CHUNK_STRING + ">" );

			_writehead.WriteLine( "<" + KEY_STRING + ">" + key + "</" + KEY_STRING + ">" );

			_writehead.Write( "<" + VALUE_STRING + " " );

			switch( d.Type )
			{
				case NodeType.String:
					_writehead.Write( TYPE_ATTR + "=\"" + TYPE_STRING + "\">" );
					break;

				case NodeType.Int:
					_writehead.Write( TYPE_ATTR + "=\"" + TYPE_INT + "\">" );
					break;

				case NodeType.Double:
					_writehead.Write( TYPE_ATTR + "=\"" + TYPE_DOUBLE + "\">" );
					break;

				case NodeType.Xml:
					_writehead.Write( TYPE_ATTR + "=\"" + TYPE_XML + "\">" );
					break;
			}

			_writehead.WriteLine( d.GetStringValue() + "</" + VALUE_STRING + ">" );

			_writehead.WriteLine( "</" + CHUNK_STRING + ">" );
			_writehead.Flush();
		}
		
		protected void RewriteFile()
		{
			SetWriteHead( false );

			IEnumerator e = _data.Keys.GetEnumerator();
			while( e.MoveNext() )
			{
				string key = (string)e.Current;
				WriteNodeToFile( key, (DataNode)_data[ key ] );
			}
		}

		protected void LoadFile()
		{
			if ( _writehead != null )
				_writehead.Close();

			_data = new Hashtable();

			XmlTextReader xml = null;

			try 
			{
				xml = new XmlTextReader( new StreamReader( _filename ) );
			
				_data = new Hashtable();

				while( xml.Read() )
				{
					if ( xml.NodeType == XmlNodeType.Element &&
						 xml.Name == CHUNK_STRING )
						HandleChunk( xml );
				}

				_valid = true;
			}
			catch( FileNotFoundException )
			{
				SetWriteHead( false );
			}
			catch( Exception )
			{
			}
			finally
			{
				if ( xml != null )
					xml.Close();
			}
		}

		protected void HandleChunk( XmlTextReader xml )
		{
			int lookingFor = 0;  // 0 = nothing, 1 = key, 2 = value
			string key = null;
			NodeType type = NodeType.String;
			string data = null;
			DataNode node = null;

			while( xml.Read() )
			{
				if ( xml.NodeType == XmlNodeType.Element )
				{
					if ( xml.Name == KEY_STRING )
						lookingFor = 1;
					else if ( xml.Name == VALUE_STRING )
					{
						string strType = xml.GetAttribute( TYPE_ATTR );

						lookingFor = 2;

						switch( strType )
						{
							case TYPE_INT:
								type = NodeType.Int;
								break;

							case TYPE_DOUBLE:
								type = NodeType.Double;
								break;

							case TYPE_XML:
								type = NodeType.Xml;
								lookingFor = 0;
								break;
						}
					}
				}
				else if ( xml.NodeType == XmlNodeType.Text )
				{
					switch( lookingFor ) 
					{
						case 1:
							key = xml.Value;
							break;

						case 2:
							data = xml.Value;
							break;
					}

					lookingFor = 0;
				}
				else if ( xml.NodeType == XmlNodeType.EndElement &&
						  xml.Name == CHUNK_STRING )
				{
					switch( type )
					{
						case NodeType.String:
							node = new StringNode( data );
							break;

						case NodeType.Int:
							node = new IntNode( Int32.Parse( data ) );
							break;

						case NodeType.Double:
							node = new DoubleNode( Double.Parse( data ) );
							break;

						case NodeType.Xml:
							break;
					}

					break;
				}
			}

			if ( key != null && node != null )
				_data[ key ] = node;
		}

		public void Close()
		{
			_writehead.Close();

			_valid = false;
		}

		public static void Main( string[] args )
		{
			Console.WriteLine( "Starting Persistent DataStore test..." );

			DataStore store = new DataStore( DataStore.PUC_DATA_FILE );

			store.Set( "registry-file", "c:\\My Documents\\research\\controller\\puc\\registry\\pocketpc.xml" );
			store.Set( "recent-servers-count", 2 );
			store.Set( "random-double", 4.56 );

			store.Close();

			store = new DataStore( DataStore.PUC_DATA_FILE );

			Console.WriteLine( "registry-file: {0}", store.GetStringData( "registry-file" ) );
			Console.WriteLine( "random-double: {0}", store.GetDoubleData( "random-double" ) );

			store.Set( "random-double", 7.56 );

			Console.WriteLine( "random-double: {0}", store.GetDoubleData( "random-double" ) );

			store.Close();

			store = new DataStore( DataStore.PUC_DATA_FILE );

			Console.WriteLine( "registry-file: {0}", store.GetStringData( "registry-file" ) );
			Console.WriteLine( "random-double: {0}", store.GetDoubleData( "random-double" ) );
		}
	}
}
