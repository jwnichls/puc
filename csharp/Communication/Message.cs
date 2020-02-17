using System;
using System.Collections;
using System.IO;
using System.Windows.Forms;
using System.Xml;

/*
 * This file implements only the client messages!
 * $Id: Message.cs,v 1.22 2004/08/10 23:22:20 jeffreyn Exp $
 */
namespace PUC.Communication
{
	/// <summary>
	/// This is the base class for the set of classes that represent
	/// messages in the PUC communication framework.
	/// </summary>
	public abstract class Message
	{
		/*
		 * Member Variables
		 */
		public const string ROOT_STRING = "message";

		public const string UNDEFINED_STRING = "undefined";


		/*
		 * Member Methods
		 */
		public static Message Decode( XmlTextReader xml, MemoryStream binData )
		{
			Message result = null;

			// read the first element to ensure this is a message
			while( xml.Read() )
				if ( xml.NodeType == XmlNodeType.Element )
					if ( xml.Name != ROOT_STRING ) 
						return null;
					else
						break;

			// determine the particular message, so we can decode it
			// NOTE: This only parses client messages!

			while( xml.Read() )
				if ( xml.NodeType == XmlNodeType.Element )
					break;

			// only the StateChangeNotification accepts binary data
			if ( xml.Name == StateChangeNotification.TYPE_STRING )
				result = new StateChangeNotification( xml );
			else if ( xml.Name == BinaryStateChangeNotification.TYPE_STRING )
				result = new BinaryStateChangeNotification( xml, binData );
			else if ( xml.Name == StateChangeRequest.TYPE_STRING )
				result = new StateChangeRequest( xml );
			else if ( xml.Name == StateValueRequest.TYPE_STRING )
				result = new StateValueRequest( xml );
			else if ( xml.Name == FullStateRequest.TYPE_STRING )
				result = new FullStateRequest( xml );
			else if ( xml.Name == CommandInvokeRequest.TYPE_STRING )
				result = new CommandInvokeRequest( xml );
			else if ( xml.Name == DeviceSpec.TYPE_STRING )
				result = new DeviceSpec( xml );
			else if ( xml.Name == SpecRequest.TYPE_STRING )
				result = new SpecRequest( xml );
			else if ( xml.Name == ServerInformationRequest.TYPE_STRING )
				result = new ServerInformationRequest( xml );
			else if ( xml.Name == ServerInformation.TYPE_STRING )
				result = new ServerInformation( xml );
			else if ( xml.Name == AlertInformation.TYPE_STRING )
				result = new AlertInformation( xml );
			else if ( xml.Name == RegisterDevice.TYPE_STRING )
				result = new RegisterDevice( xml );
			else if ( xml.Name == UnregisterDevice.TYPE_STRING )
				result = new UnregisterDevice( xml );

			/*
			if ( !result.IsValid() )
				PUCFrame.AddLogLine( "Invalid Message Parsed: " + result.ToString() );
			*/

			if ( result == null || !result.IsValid() )
				return null;

			return result;
		}


		/*
		 * Abstract Methods
		 */
		public abstract bool IsValid();
		public abstract string GetXML();
		public override abstract string ToString(); 

		public virtual void PrepareMessage( Connection c ) { }

		public virtual bool HasBinaryData() 
		{
			return false;
		}

		public virtual Stream BinaryData
		{
			get
			{
				throw new NotSupportedException( "this message does not support binary data attachments" );
			}
		}

		public virtual int BinaryDataLength
		{
			get
			{
				throw new NotSupportedException( "this message does not support binary data attachments" );
			}
		}


		/*
		 * Main Testing Method
		 */

		#region Main Test Function
		public static void Main()
		{
			/*
			String[] values = { "Orange", "Apple", "Banana" };
			bool flag;
			*/
			Connection c = new Connection( System.Net.IPAddress.Parse("192.168.0.100"), 5150 );

			StateChangeNotification test, test2;
			StateChangeRequest test3, test4;

			/*
			 * Test #1: Undefined (SCN)
			 */

			test = new StateChangeNotification( "TestState" );
			test.PrepareMessage( c );
			test2 = new StateChangeNotification( new XmlTextReader( new StringReader( test.GetXML() ) ) );

			if ( !test2.Defined ) 
			{
				Console.WriteLine( "Test #1 Passed" );
			}
			else 
			{
				Console.WriteLine( "Test #1 Failed" );
				Application.Exit();
			}

			/*
			 * Test #2: Undefined (SCR)
			 */

			test3 = new StateChangeRequest( "TestState" );
			test3.PrepareMessage( c );
			test4 = new StateChangeRequest( new XmlTextReader( new StringReader( test3.GetXML() ) ) );

			Console.WriteLine( test3.GetXML() );

			if ( !test4.Defined ) 
			{
				Console.WriteLine( "Test #2 Passed" );
			}
			else 
			{
				Console.WriteLine( "Test #2 Failed" );
				Application.Exit();
			}

			/*
				* Test #3: Normal Value (SCN)
				*/

			test = new StateChangeNotification( "TestState", "success" );
			test.PrepareMessage( c );
			test2 = new StateChangeNotification( new XmlTextReader( new StringReader( test.GetXML() ) ) );

			if ( test2.Defined && test2.Value == "success" )
				Console.WriteLine( "Test #3 Passed" );
			else 
			{
				Console.WriteLine( "Test #3 Failed" );
				Application.Exit();
			}

			/*
			 * Test #4: Normal Value (SCR)
			 */

			test3 = new StateChangeRequest( "TestState", "success" );
			test3.PrepareMessage( c );
			test4 = new StateChangeRequest( new XmlTextReader( new StringReader( test3.GetXML() ) ) );

			Console.WriteLine( test3.GetXML() );

			if ( test4.Defined && test4.Value == "success" )
				Console.WriteLine( "Test #4 Passed" );
			else 
			{
				Console.WriteLine( "Test #4 Failed" );
				Application.Exit();
			}

			/*
			 * Test #5: List Data
			 *

			test = new StateChangeNotification( "TestState", values );
			test.PrepareMessage( c );
			test2 = new StateChangeNotification( new XmlTextReader( new StringReader( test.GetXML() ) ) );

			Console.Write( "\n" + test.GetXML() );

			flag = true;
			for( int i = 0; flag && i < test2.GetValues().Length; i++ ) 
			{
				flag = values[ i ] == test2.GetValues()[ i ];
			}

			if ( flag && test.GetOperation() == test2.GetOperation() && 
				test.GetVersion() == test2.GetVersion() )
				Console.WriteLine( "Test #5 Passed" );
			else 
			{
				Console.WriteLine( "Test #5 Failed" );
			} 

			/*
			 * Test #6: List Delete
			 *

			test = new StateChangeNotification( "TestState", 5, 10 );
			test.PrepareMessage( c );
			test2 = new StateChangeNotification( new XmlTextReader( new StringReader( test.GetXML() ) ) );

			Console.Write( "\n" + test.GetXML() );

			if ( test.GetOperation() == test2.GetOperation() && 
				test.GetVersion() == test2.GetVersion() &&
				test.GetBegin() == test2.GetBegin() &&
				test.GetEnd() == test2.GetEnd() )
				Console.WriteLine( "Test #6 Passed" );
			else 
			{
				Console.WriteLine( "Test #6 Failed" );
			} 
				
			/*
			 * Test #7: List Insert
			 *

			test = new StateChangeNotification( "TestState", 5, values );
			test.PrepareMessage( c );
			test2 = new StateChangeNotification( new XmlTextReader( new StringReader( test.GetXML() ) ) );

			Console.Write( "\n" + test.GetXML() );

			flag = true;
			for( int i = 0; flag && i < test2.GetValues().Length; i++ ) 
			{
				flag = values[ i ] == test2.GetValues()[ i ];
			}

			if ( flag && test.GetOperation() == test2.GetOperation() && 
				test.GetVersion() == test2.GetVersion() &&
				test.GetBegin() == test2.GetBegin() )
				Console.WriteLine( "Test #7 Passed" );
			else 
			{
				Console.WriteLine( "Test #7 Failed" );
			} 

			/*
			 * Test #8: List Replace
			 *

			test = new StateChangeNotification( "TestState", 5, 10, values );
			test.PrepareMessage( c );
			test2 = new StateChangeNotification( new XmlTextReader( new StringReader( test.GetXML() ) ) );

			Console.Write( "\n" + test.GetXML() );

			flag = true;
			for( int i = 0; flag && i < test2.GetValues().Length; i++ ) 
			{
				flag = values[ i ] == test2.GetValues()[ i ];
			}

			if ( flag && test.GetOperation() == test2.GetOperation() && 
				test.GetVersion() == test2.GetVersion() &&
				test.GetBegin() == test2.GetBegin() &&
				test.GetBegin() == test2.GetBegin() )
				Console.WriteLine( "Test #8 Passed" );
			else 
			{
				Console.WriteLine( "Test #8 Failed" );
			} 

			/*
			 * Test #9: SCR Data Elements
			 *

			test3 = new StateChangeRequest( "TestState", values );
			test3.PrepareMessage( c );
			test4 = new StateChangeRequest( new XmlTextReader( new StringReader( test3.GetXML() ) ) );

			Console.Write( "\n" + test3.GetXML() );

			flag = true;
			for( int i = 0; flag && i < test4.GetValues().Length; i++ ) 
			{
				flag = values[ i ] == test4.GetValues()[ i ];
			}

			if ( flag && test3.GetOperation() == test4.GetOperation() )
				Console.WriteLine( "Test #9 Passed" );
			else 
			{
				Console.WriteLine( "Test #9 Failed" );
			} 
			*/
		}
		#endregion
	}


	// StateValueRequest
	public class StateValueRequest : Message
	{
		/*
		 * Constants
		 */

		public const string TYPE_STRING = "state-value-request";

		public const string STATE_TAG   = "state";

		public const string DESIRED_HEIGHT_OPT	= "desired-height";
		public const string DESIRED_WIDTH_OPT	= "desired-width";


		/*
		 * Member Variables
		 */
		
		private string		_state	= null;
		private Hashtable	_params	= new Hashtable();
		private bool		_valid	= false;


		/*
		 * Constructors
		 */

		public StateValueRequest( string state )
		{
			_state = state;
			_valid = true;
		}

		public StateValueRequest( XmlTextReader xml ) 
		{
			string currentElem = null;

			while( xml.Read() ) 
			{
				if ( xml.NodeType == XmlNodeType.Element )
				{
					if ( xml.IsEmptyElement )
					{
						_params[ xml.Name ] = true;
						currentElem = null;
					}
					else
						currentElem = xml.Name;
				}
				else if ( xml.NodeType == XmlNodeType.Text )
				{
					if ( currentElem == null )
						continue;

					switch( currentElem )
					{
						case STATE_TAG:
							_state = xml.Value;
							break;
						default:
							_params[ currentElem ] = xml.Value;
							break;
					}

					currentElem = null;
				}
				else if ( xml.NodeType == XmlNodeType.EndElement )
					if ( xml.Name == TYPE_STRING )
						break;
			}

			_valid = ( _state != null );
		}


		/*
		 * Indexor
		 */

		public string this[ string param ]
		{
			get
			{
				return (string)_params[ param ];
			}
			set
			{
				_params[ param ] = value;
			}
		}


		/*
		 * Member Methods
		 */

		public override bool IsValid()
		{
			return _valid;
		}

		public override string GetXML()
		{
			StringWriter strout = new StringWriter();
			XmlTextWriter xmlout = new XmlTextWriter( strout );

			xmlout.WriteStartDocument();
			xmlout.WriteStartElement( ROOT_STRING );
			xmlout.WriteStartElement( TYPE_STRING );
			xmlout.WriteStartElement( STATE_TAG );
			xmlout.WriteString( _state );
			xmlout.WriteEndElement();			

			IEnumerator e = _params.GetEnumerator();
			while( e.MoveNext() )
			{
				DictionaryEntry entry = (DictionaryEntry)e.Current;
				xmlout.WriteStartElement( (string)entry.Key );
				xmlout.WriteString( (string)entry.Value );
				xmlout.WriteEndElement();
			}

			xmlout.WriteEndElement();
			xmlout.WriteEndElement();
			xmlout.WriteEndDocument();

			return strout.ToString();			
		}

		public override string ToString()
		{
			return TYPE_STRING + ": [ " + _state + " ]";
		}

	}


	// BinaryStateChangeNotification
	public class BinaryStateChangeNotification : Message
	{
		/*
		 * Constants
		 */
		public const string TYPE_STRING				= "binary-state-change-notification";
		public const string STATE_TAG				= "state";
		public const string CONTENT_TYPE_ATTRIBUTE	= "content-type";


		/*
		 * Member Variables
		 */
		private string	_state			= null;
		private string	_contentType	= null;
		private Stream	_binData		= null;
		private int		_binLength		= -1;
		private bool	_valid			= false;


		/*
		 * Constructors
		 */

		public BinaryStateChangeNotification( string state, string contentType )
		{
			_state = state;
			_contentType = contentType;
			_valid = true;
		}

		public BinaryStateChangeNotification( string state, Stream binData, string contentType, int length )
			: this( state, contentType )
		{
			SetBinaryData( binData, contentType, length );
		}

		public BinaryStateChangeNotification( XmlTextReader xml, MemoryStream binData )
		{
			string currentNode = "";

			while( xml.Read() )
			{
				if ( xml.NodeType == XmlNodeType.Element )
				{
					currentNode = xml.Name;

					if ( currentNode == STATE_TAG )
					{
						string cType = xml.GetAttribute( CONTENT_TYPE_ATTRIBUTE );
						if ( cType == null )
							throw new FormatException( TYPE_STRING + " must include content type information." );

						SetBinaryData( binData, cType, (binData == null?-1:(int)binData.Length) );
					}
				}
				else if ( xml.NodeType == XmlNodeType.Text )
				{
					if ( currentNode == STATE_TAG )
						_state = xml.Value;
				}
			}

			_valid = (_state != null);
		}


		/*
		 * Member Methods
		 */

		public override bool IsValid()
		{
			return _valid;
		}

		public override string GetXML()
		{
			StringWriter strout = new StringWriter();
			XmlTextWriter xml = new XmlTextWriter( strout );

			xml.WriteStartDocument();
			xml.WriteStartElement( ROOT_STRING );
			xml.WriteStartElement( TYPE_STRING );
			xml.WriteStartElement( STATE_TAG );
			xml.WriteAttributeString( CONTENT_TYPE_ATTRIBUTE, _contentType );
			xml.WriteString( _state );
			xml.WriteEndElement();
			xml.WriteEndElement();
			xml.WriteEndElement();
			xml.WriteEndDocument();

			return strout.ToString();			
		}

		public override string ToString()
		{
			string extraInfo = "no binary attachment";
			if ( HasBinaryData() )
				extraInfo = "binary data attached";

			return TYPE_STRING + ": [ " + _state + " (" + _contentType + ") = " + extraInfo + " ]";
		}

		public string ContentType
		{
			get
			{
				return _contentType;
			}
		}

		public string State
		{
			get
			{
				return _state;
			}
			set
			{
				_state = value;
			}
		}

		public override bool HasBinaryData() 
		{
			return (_binData != null);
		}

		public void SetBinaryData( Stream binData, string contentType, int length )
		{
			_binData = binData;
			_contentType = contentType;
			_binLength = length;
		}

		public override Stream BinaryData
		{
			get
			{
				return _binData;
			}
		}

		public override int BinaryDataLength
		{
			get
			{
				return _binLength;
			}
		}
	}


	// StateChangeNotification
	public class StateChangeNotification : Message
	{
		/*
		 * Member Variables
		 */
		public const string TYPE_STRING = "state-change-notification";
		private PUCData			_data	 = null;
		private bool			_valid   = false;


		/*
		 * Constructors
		 */
		public StateChangeNotification( string state, string val )
			: this( new ValueData( state, val ) )
		{
		}

		public StateChangeNotification( string state )
			: this( new ValueData( state ) )
		{
		}

		/// <summary>
		/// This constructor is used when you want to construct a 
		/// StateChangeNotification message that contains complex
		/// list information.
		/// </summary>
		public StateChangeNotification( PUCData data )
		{
			_data = data;

			_valid = true;
		}

		public StateChangeNotification( XmlTextReader xml )
		{
			try
			{
				_data = PUCData.Parse( xml, true );
	
				_valid = ( _data != null );
			}
			catch( Exception )
			{
				_valid = false;
			}
		}


		/*
		 * Overridden Methods
		 */
		public override bool IsValid() 
		{
			return _valid;
		}

		public override string GetXML() 
		{
			StringWriter strout = new StringWriter();
			XmlTextWriter xmlout = new XmlTextWriter( strout );

			xmlout.WriteStartDocument();
			xmlout.WriteStartElement( ROOT_STRING );
			xmlout.WriteStartElement( TYPE_STRING );

			if ( _data is ValueData )
				((ValueData)_data).WriteOldXML( xmlout );
			else
				_data.WriteXML( xmlout );

			xmlout.WriteEndElement();
			xmlout.WriteEndElement();
			xmlout.WriteEndDocument();

			return strout.ToString();
		}

		public override void PrepareMessage(Connection c)
		{

		}

		public override string ToString()
		{
			string extraInfo = "list data";
			if ( _data.IsValue() )
				extraInfo = ((ValueData)_data).State + " = " + ((ValueData)_data).Value;

			return TYPE_STRING + ": [ " + extraInfo + " ]";
		}

		public bool IsListData()
		{
			return !(_data is ValueData);
		}

		public string State
		{
			get
			{
				return ((ValueData)_data).State;
			}
		}

		public string Value
		{
			get
			{
				return ((ValueData)_data).Value;
			}
		}

		public PUCData Data
		{
			get
			{
				return _data;
			}
		}

		public bool Defined
		{
			get
			{
				return ((ValueData)_data).Defined;
			}
		}
	}

	// StateChangeRequest
	public class StateChangeRequest : Message
	{
		/*
		 * Member Variables
		 */
		public const string TYPE_STRING = "state-change-request";
		private PUCData	 _data	= null;
		private bool     _valid = false;

		/*
		 * Constructors
		 */
		public StateChangeRequest( string state, string val )
			: this( new ValueData( state, val ) )
		{
		}

		public StateChangeRequest( string state )
			: this( new ValueData( state ) )
		{
		}

		public StateChangeRequest( PUCData data )
		{
			_data = data;

			_valid = true;
		}

		public StateChangeRequest( XmlTextReader xml )
		{
			try
			{
				_data = PUCData.Parse( xml, true );
	
				_valid = ( _data != null );
			}
			catch( Exception )
			{
				_valid = false;
			}			
		}

		/*
		 * Overridden Methods
		 */
		public override bool IsValid() 
		{
			return _valid;
		}

		public override string GetXML() 
		{
			StringWriter strout = new StringWriter();
			XmlTextWriter xml = new XmlTextWriter( strout );

			xml.WriteStartDocument();
			xml.WriteStartElement( ROOT_STRING );
			xml.WriteStartElement( TYPE_STRING );

			if ( _data.IsValue() )
				((ValueData)_data).WriteOldXML( xml );
			else
				_data.WriteXML( xml );

			xml.WriteEndElement();
			xml.WriteEndElement();
			xml.WriteEndDocument();

			return strout.ToString();
		}

		public override string ToString()
		{
			string extraInfo = "list data";
			if ( _data.IsValue() )
				extraInfo = ((ValueData)_data).State + " = " + ((ValueData)_data).Value;

			return TYPE_STRING + ": [ " + extraInfo + " ]";
		}

		public bool IsListData()
		{
			return !_data.IsValue();
		}

		public string GetState()
		{
			return ((ValueData)_data).State;
		}

		public string Value
		{
			get
			{
				return ((ValueData)_data).Value;
			}
		}

		public PUCData Data
		{
			get
			{
				return _data;
			}
		}

		public bool Defined
		{
			get
			{
				return ((ValueData)_data).Defined;
			}
		}
	}
	
	// CommandInvokeRequest
	public class CommandInvokeRequest : Message
	{
		/*
		 * Member Variables
		 */
		public const string TYPE_STRING = "command-invoke-request";
		private string _command = "";
		private bool   _valid = false;

		/*
		 * Constructors
		 */
		public CommandInvokeRequest( string command )
		{
			_command = command;

			_valid = true;
		}

		public CommandInvokeRequest( XmlTextReader xml )
		{
			string currentElem = "";

			while( xml.Read() ) 
			{
				if ( xml.NodeType == XmlNodeType.Element )
					currentElem = xml.Name;
				else if ( xml.NodeType == XmlNodeType.Text )
				{
					switch( currentElem )
					{
						case "command":
							_command = xml.Value;
							break;
					}
				}
				else if ( xml.NodeType == XmlNodeType.EndElement )
					if ( xml.Name == TYPE_STRING )
						break;
			}

			_valid = ( _command != null );
		}

		/*
		 * Overridden Methods
		 */
		public override bool IsValid() 
		{
			return _valid;
		}

		public override string GetXML() 
		{
			StringWriter strout = new StringWriter();
			XmlTextWriter xml = new XmlTextWriter( strout );

			xml.WriteStartDocument();
			xml.WriteStartElement( ROOT_STRING );
			xml.WriteStartElement( TYPE_STRING );
			xml.WriteElementString( "command", _command );
			xml.WriteEndElement();
			xml.WriteEndElement();
			xml.WriteEndDocument();

			return strout.ToString();
		}

		public override string ToString()
		{
			return TYPE_STRING + ": [" + _command + "]";
		}

		public string GetCommand()
		{
			return _command;
		}
	}

	// SpecRequest
	public class SpecRequest : Message
	{
		/*
		 * Member Variables
		 */
		public const string TYPE_STRING = "spec-request";
		private bool   _valid = false;

		/*
		 * Constructors
		 */
		public SpecRequest()
		{
			_valid = true;
		}

		public SpecRequest( XmlTextReader xml )
		{
			_valid = true;
		}

		/*
		 * Overridden Methods
		 */
		public override bool IsValid() 
		{
			return _valid;
		}

		public override string GetXML() 
		{
			StringWriter strout = new StringWriter();
			XmlTextWriter xml = new XmlTextWriter( strout );

			xml.WriteStartDocument();
			xml.WriteStartElement( ROOT_STRING );
			xml.WriteStartElement( TYPE_STRING );
			xml.WriteEndElement();
			xml.WriteEndElement();
			xml.WriteEndDocument();

			return strout.ToString();
		}

		public override string ToString()
		{
			return TYPE_STRING;
		}
	}

	// DeviceSpec
	public class DeviceSpec : Message
	{
		/*
		 * Member Variables
		 */
		public const string TYPE_STRING = "device-spec";
		private string _spec = "";
		private bool   _valid = false;

		/*
		 * Constructors
		 */
		public DeviceSpec( string spec )
		{
			_spec = spec;

			_valid = true;
		}

		public DeviceSpec( XmlTextReader xml )
		{
			string currentElem = "";

			while( xml.Read() ) 
			{
				if ( xml.NodeType == XmlNodeType.Element )
					currentElem = xml.Name;
				else if ( xml.NodeType == XmlNodeType.Text )
				{
					switch( currentElem )
					{
						case "spec":
							_spec = xml.Value;
							break;
					}
				}
				else if ( xml.NodeType == XmlNodeType.EndElement )
					if ( xml.Name == TYPE_STRING )
						break;
			}

			_valid = ( _spec != null );
		}

		/*
		 * Overridden Methods
		 */
		public override bool IsValid() 
		{
			return _valid;
		}

		public override string GetXML() 
		{
			StringWriter strout = new StringWriter();
			XmlTextWriter xml = new XmlTextWriter( strout );

			xml.WriteStartDocument();
			xml.WriteStartElement( ROOT_STRING );
			xml.WriteStartElement( TYPE_STRING );
			xml.WriteStartElement( "spec" );
			xml.WriteString( _spec );
			xml.WriteEndElement();
			xml.WriteEndElement();
			xml.WriteEndElement();
			xml.WriteEndDocument();

			return strout.ToString();
		}

		public override string ToString()
		{
			return TYPE_STRING + ": [" + _spec + "]";
		}

		public string GetSpec() 
		{
			return _spec;
		}
	}

	// FullStateRequest
	public class FullStateRequest : Message
	{
		/*
		 * Member Variables
		 */
		public const string TYPE_STRING = "full-state-request";
		private bool   _valid = false;

		/*
		 * Constructors
		 */
		public FullStateRequest()
		{
			_valid = true;
		}

		public FullStateRequest( XmlTextReader xml )
		{
			_valid = true;
		}

		/*
		 * Overridden Methods
		 */
		public override bool IsValid() 
		{
			return _valid;
		}

		public override string GetXML() 
		{
			StringWriter strout = new StringWriter();
			XmlTextWriter xml = new XmlTextWriter( strout );

			xml.WriteStartDocument();
			xml.WriteStartElement( ROOT_STRING );
			xml.WriteStartElement( TYPE_STRING );
			xml.WriteEndElement();
			xml.WriteEndElement();
			xml.WriteEndDocument();

			return strout.ToString();
		}

		public override string ToString()
		{
			return TYPE_STRING;
		}
	}
	
	// ServerInformationRequest
	public class ServerInformationRequest : Message
	{
		/*
		 * Member Variables
		 */
		public const string TYPE_STRING = "server-information-request";
		private bool   _valid = false;

		/*
		 * Constructors
		 */
		public ServerInformationRequest()
		{
			_valid = true;
		}

		public ServerInformationRequest( XmlTextReader xml )
		{
			_valid = true;
		}

		/*
		 * Overridden Methods
		 */
		public override bool IsValid() 
		{
			return _valid;
		}

		public override string GetXML() 
		{
			StringWriter strout = new StringWriter();
			XmlTextWriter xml = new XmlTextWriter( strout );

			xml.WriteStartDocument();
			xml.WriteStartElement( ROOT_STRING );
			xml.WriteStartElement( TYPE_STRING );
			xml.WriteEndElement();
			xml.WriteEndElement();
			xml.WriteEndDocument();

			return strout.ToString();
		}

		public override string ToString()
		{
			return TYPE_STRING;
		}
	}

	// ServerInformation
	public class ServerInformation : Message
	{
		/*
		 * Member Variables
		 */
		public const string TYPE_STRING = "server-information";
		public const string SERVER_NAME_STRING = "server-name";
		public const string DEVICE_STRING = "device";
		public const string NAME_STRING = "name";
		public const string PORT_STRING = "port";
		private string _serverName;
		private ArrayList _deviceInfo = null;
		private bool   _valid = false;

		public struct DeviceInfo
		{
			private string _deviceName;
			private int _devicePort;

			public DeviceInfo( string name, int port )
			{
				_deviceName = name;
				_devicePort = port;
			}

			public string GetDeviceName()
			{
				return _deviceName;
			}

			public int GetDevicePort()
			{
				return _devicePort;
			}

			public string GetDeviceUniqueString()
			{
				return _deviceName + ":" + _devicePort;
			}
		}

		/*
		 * Constructors
		 */
		// this message can only be received by the client...

		public ServerInformation( XmlTextReader xml )
		{
			string currentElem = "";
			_deviceInfo = new ArrayList();

			while( xml.Read() ) 
			{
				if ( xml.NodeType == XmlNodeType.Element )
					if ( xml.Name == DEVICE_STRING )
					{
						DeviceInfo d = HandleDevice( xml );
						if ( d.GetDevicePort() < 0 || 
							 d.GetDeviceName() == null )
							return; // causes message to remain invalid
						_deviceInfo.Add( d );
					}
					else
						currentElem = xml.Name;
				else if ( xml.NodeType == XmlNodeType.Text )
				{
					switch( currentElem )
					{
						case SERVER_NAME_STRING:
							_serverName = xml.Value;
							break;
					}
				}
				else if ( xml.NodeType == XmlNodeType.EndElement )
					if ( xml.Name == TYPE_STRING )
						break;
			}

			_valid = ( _serverName != null );
		}

		public static DeviceInfo HandleDevice( XmlTextReader xml )
		{
			string currentElem = "";
			string deviceName = "";
			int portNum = -1;

			while( xml.Read() )
			{
				if ( xml.NodeType == XmlNodeType.Element )
					currentElem = xml.Name;
				else if ( xml.NodeType == XmlNodeType.Text )
					switch( currentElem ) 
					{
						case NAME_STRING:
							deviceName = xml.Value;
							break;
						case PORT_STRING:
							portNum = Int32.Parse( xml.Value );
							break;
					}
				else if ( xml.NodeType == XmlNodeType.EndElement )
					if ( xml.Name == DEVICE_STRING )
					{
						return new DeviceInfo( deviceName, portNum );
					}
			}

			return new DeviceInfo( null, -1 );
		}

		public ServerInformation( String name, IEnumerator devices )
		{
			_deviceInfo = new ArrayList();

			while( devices.MoveNext() )
			{
				IDevice2 dev = (IDevice2)devices.Current;

				if ( dev.IsRunning() )
					_deviceInfo.Add( new DeviceInfo( dev.Name, dev.Port ) );
			}

			_serverName = name;

			_valid = true;
		}

		public ServerInformation( String name, DeviceInfo[] devices ) 
		{
			_deviceInfo = new ArrayList();

			IEnumerator e = devices.GetEnumerator();
			while( e.MoveNext() )
				_deviceInfo.Add( e.Current );

			_serverName = name;

			_valid = true;
		}

		/*
		 * Overridden Methods
		 */
		public override bool IsValid() 
		{
			return _valid;
		}

		public override string GetXML() 
		{
			StringWriter strout = new StringWriter();
			XmlTextWriter xml = new XmlTextWriter( strout );

			xml.WriteStartDocument();
			xml.WriteStartElement( ROOT_STRING );
			xml.WriteStartElement( TYPE_STRING );
			xml.WriteElementString( SERVER_NAME_STRING, _serverName );

			IEnumerator e = _deviceInfo.GetEnumerator();
			while( e.MoveNext() )
			{
				DeviceInfo di = (DeviceInfo)e.Current;

				xml.WriteStartElement( DEVICE_STRING );
				xml.WriteElementString( NAME_STRING, di.GetDeviceName() );
				xml.WriteElementString( PORT_STRING, di.GetDevicePort().ToString() );
				xml.WriteEndElement();
			}

			xml.WriteEndElement();
			xml.WriteEndElement();
			xml.WriteEndDocument();

			return strout.ToString();
		}

		public override string ToString()
		{
			return TYPE_STRING;
		}

		public string GetServerName()
		{
			return _serverName;
		}

		public IEnumerator GetDevices()
		{
			return _deviceInfo.GetEnumerator();
		}
	}

	// AlertInformation
	public class AlertInformation : Message
	{
		/*
		 * Member Variables
		 */
		public const string TYPE_STRING = "alert-information";
		private bool   _valid = false;
		private string _alertMessage;

		/*
		 * Constructors
		 */
		public AlertInformation( string alertmsg )
		{
			_alertMessage = alertmsg;
			_valid = true;
		}

		public AlertInformation( XmlTextReader xml )
		{
			while( xml.Read() ) 
			{
				if ( xml.NodeType == XmlNodeType.Text )
				{
					_alertMessage = xml.Value;
					break;
				}
				else if ( xml.NodeType == XmlNodeType.EndElement )
					if ( xml.Name == TYPE_STRING )
						break;
			}

			_valid = ( _alertMessage != null );
		}

		/*
		 * Overridden Methods
		 */
		public override bool IsValid() 
		{
			return _valid;
		}

		public string GetAlertMessage()
		{
			return _alertMessage;
		}

		public override string GetXML() 
		{
			StringWriter strout = new StringWriter();
			XmlTextWriter xml = new XmlTextWriter( strout );

			xml.WriteStartDocument();
			xml.WriteStartElement( ROOT_STRING );
			xml.WriteStartElement( TYPE_STRING );
			xml.WriteString( _alertMessage );
			xml.WriteEndElement();
			xml.WriteEndElement();
			xml.WriteEndDocument();

			return strout.ToString();
		}

		public override string ToString()
		{
			return TYPE_STRING;
		}
	}

	// RegisterDevice
	public class RegisterDevice : Message
	{
		/*
		 * Member Variables
		 */
		public const string TYPE_STRING = "register-device";
		public const string NAME_STRING = "name";
		public const string PORT_STRING = "port";
		private bool   _valid = false;
		private string _deviceName = null;
		private int    _devicePort = 0;

		/*
		 * Constructors
		 */
		public RegisterDevice( string deviceName, int devicePort )
		{
			_deviceName = deviceName;
			_devicePort = devicePort;
			_valid = true;
		}

		public RegisterDevice( XmlTextReader xml )
		{
			int parseFlag = 0;

			while( xml.Read() ) 
			{
				if ( xml.NodeType == XmlNodeType.Text )
				{
					switch( parseFlag )
					{
						case 1: // store deviceName
							_deviceName = xml.Value;
							parseFlag = 0;
							break;

						case 2: // store devicePort
							_devicePort = Int32.Parse( xml.Value );
							parseFlag = 1;
							break;
					}
				}
				else if ( xml.NodeType == XmlNodeType.Element ) 
				{
					switch( xml.Name ) 
					{
						case NAME_STRING: 
							parseFlag = 1; 
							break;
						case PORT_STRING:
							parseFlag = 2;
							break;
						default:
							parseFlag = 0;
							break;
					}
				}
				else if ( xml.NodeType == XmlNodeType.EndElement )
					if ( xml.Name == TYPE_STRING )
						break;
			}

			_valid = ( _deviceName != null ) && ( _devicePort > 0 );
		}

		/*
		 * Overridden Methods
		 */
		public override bool IsValid() 
		{
			return _valid;
		}

		public string GetDeviceName()
		{
			return _deviceName;
		}

		public int GetDevicePort()
		{
			return _devicePort;
		}

		public override string GetXML() 
		{
			StringWriter strout = new StringWriter();
			XmlTextWriter xml = new XmlTextWriter( strout );

			xml.WriteStartDocument();
			xml.WriteStartElement( ROOT_STRING );
			xml.WriteStartElement( TYPE_STRING );
			xml.WriteElementString( NAME_STRING, _deviceName );
			xml.WriteElementString( PORT_STRING, _devicePort.ToString() );
			xml.WriteEndElement();
			xml.WriteEndElement();
			xml.WriteEndDocument();

			return strout.ToString();
		}

		public override string ToString()
		{
			return TYPE_STRING;
		}
	}

	// UnregisterDevice
	public class UnregisterDevice : Message
	{
		/*
		 * Member Variables
		 */
		public const string TYPE_STRING = "unregister-device";
		public const string PORT_STRING = "port";
		private bool   _valid = false;
		private int    _devicePort = 0;

		/*
		 * Constructors
		 */
		public UnregisterDevice( int devicePort )
		{
			_devicePort = devicePort;
			_valid = true;
		}

		public UnregisterDevice( XmlTextReader xml )
		{
			int parseFlag = 0;

			while( xml.Read() ) 
			{
				if ( xml.NodeType == XmlNodeType.Text )
				{
					switch( parseFlag )
					{
						case 2: // store devicePort
							_devicePort = Int32.Parse( xml.Value );
							parseFlag = 1;
							break;
					}
				}
				else if ( xml.NodeType == XmlNodeType.Element ) 
				{
					switch( xml.Name ) 
					{
						case PORT_STRING:
							parseFlag = 2;
							break;
						default:
							parseFlag = 0;
							break;
					}
				}
				else if ( xml.NodeType == XmlNodeType.EndElement )
					if ( xml.Name == TYPE_STRING )
						break;
			}

			_valid = ( _devicePort > 0 );
		}

		/*
		 * Overridden Methods
		 */
		public override bool IsValid() 
		{
			return _valid;
		}

		public int GetDevicePort()
		{
			return _devicePort;
		}

		public override string GetXML() 
		{
			StringWriter strout = new StringWriter();
			XmlTextWriter xml = new XmlTextWriter( strout );

			xml.WriteStartDocument();
			xml.WriteStartElement( ROOT_STRING );
			xml.WriteStartElement( TYPE_STRING );
			xml.WriteElementString( PORT_STRING, _devicePort.ToString() );
			xml.WriteEndElement();
			xml.WriteEndElement();
			xml.WriteEndDocument();

			return strout.ToString();
		}

		public override string ToString()
		{
			return TYPE_STRING;
		}
	}

	/// <summary>
	/// This abstract class is the base class for
	/// all structures that describe a data in a PUC
	/// message.
	/// </summary>
	public abstract class PUCData
	{
		/*
		 * Constructor
		 */

		/// <summary>
		/// Used when constructing the message on the sending
		/// side.
		/// </summary>
		protected PUCData()
		{
		}

		/// <summary>
		/// Used when parsing a message received from another 
		/// source.
		/// </summary>
		/// <param name="xml"></param>
		protected PUCData( XmlTextReader xml )
		{
			ParseContents( xml );
		}


		/*
		 * Member Methods
		 */

		public abstract void WriteXML( XmlTextWriter xmlout );
		public abstract void ParseContents( XmlTextReader xml );
		public abstract void ApplyData( VariableTable varTable );
		public abstract void ApplyData( string fullName, VariableTable varTable, Hashtable table, ArrayList indices );

		public virtual bool IsValue()
		{
			return false;
		}

		public int[] getIntArray( ArrayList indices )
		{
			return (int[])indices.ToArray( Type.GetType( "System.Int32" ) );
		}


		/*
		 * Static Parsing Method
		 */

		/// <summary>
		/// This method parses a xml reader for an arbitrary 
		/// member of the PUCData family. The objects returned
		/// vary slightly depending on whether the top level is
		/// being parsed or not.
		/// </summary>
		/// <param name="xml">the Xml stream to parse from</param>
		/// <param name="topLevel">a flag indicating whether we are parsing the top level of a message</param>
		/// <returns>a PUCData object representing the data contained in XML</returns>
		public static PUCData Parse( XmlTextReader xml, bool topLevel )
		{
			while( xml.Read() )
			{
				if ( xml.NodeType == XmlNodeType.Element )
				{
					switch( xml.Name )
					{
						case ValueData.TAG:
							if ( topLevel ) 
							{
								if ( xml.GetAttribute( ValueData.STATE_ATTRIBUTE ) == null )
								{
									// must be parsing the old version of the state/value message
									return ParseOldStateValue( xml );
								}
								else
									return new ValueData( xml );
							}
							else
								return new MultipleValueData( xml );

						case ValueData.STATE_ATTRIBUTE:
							if ( !topLevel )
								throw new FormatException( ValueData.STATE_ATTRIBUTE + " is only allowed as an element name on the top level" );

							return ParseOldStateValue( xml );

						case ListData.DATA_TAG:
							return new ListData( xml );

						case ListData.CHANGE_TAG:
							return new ListData( xml );

						case ListInsert.INSERT_TAG:
							return new ListInsert( xml );

						case ListReplace.REPLACE_TAG:
							return new ListReplace( xml );

						case ListDelete.DELETE_TAG:
							return new ListDelete( xml );
					}
				}
			}

			return null;
		}

		public static ValueData ParseOldStateValue( XmlTextReader xml )
		{
			int parseState = 0; // 0 = nothing, 1 = state, 2 = value
			string state = null;
			string value = null;
			bool undefined = false;

			do
			{
				if ( xml.NodeType == XmlNodeType.Element )
				{
					switch( xml.Name )
					{
						case ValueData.TAG: // value
							parseState = 2;
							break;

						case ValueData.STATE_ATTRIBUTE:
							parseState = 1;
							break;

						case ValueData.UNDEFINED_TAG:
							undefined = true;
							break;
					}
				}
				else if ( xml.NodeType == XmlNodeType.Text )
				{
					if ( parseState == 1 ) // parse state
						state = xml.Value;
					else if ( parseState == 2 ) // parse value
						value = xml.Value;

					parseState = 0;
				}
			}

			while( xml.Read() );

			if ( state != null )
				if ( undefined )
					return new ValueData( state );
				else if ( value != null )
					return new ValueData( state, value );

			throw new FormatException( "Message does not contain state and value information." );
		}
	}

	/// <summary>
	/// This interface defines any object that stores the 
	/// name of a state.  It is used by PUCData objects.
	/// </summary>
	public interface IStateNameData 
	{
		string State
		{
			get;
			set;
		}
	}


	/// <summary>
	/// This class represents a concrete value in a PUC
	/// message.
	/// </summary>
	public class ValueData : PUCData, IStateNameData
	{
		/*
		 * Constant
		 */

		public const string TAG				= "value";
		public const string STATE_ATTRIBUTE	= "state";
		public const string UNDEFINED_TAG	= "undefined";


		/*
		 * Member Variables
		 */

		protected string _state;
		protected string _value;
		protected bool	 _defined;


		/*
		 * Constructor
		 */

		public ValueData( string state, string value )
		{
			_state = state;
			_value = value;
			_defined = ( _value != null );
		}

		public ValueData( string state )
		{
			_state = state;
			_defined = false;
		}

		public ValueData( XmlTextReader xml )
			: base( xml )
		{
		}


		/*
		 * Properties
		 */

		public string State
		{
			get
			{
				return _state;
			}
			set
			{
				_state = value;
			}
		}

		public string Value
		{
			get
			{
				return _value;
			}
			set
			{
				_value = value;
				_defined = value != null;
			}
		}

		public bool Defined
		{
			get
			{
				return _defined;
			}
		}


		/*
		 * Member Methods
		 */

		public override bool IsValue()
		{
			return true;
		}

		public override void WriteXML(XmlTextWriter xmlout)
		{
			xmlout.WriteStartElement( TAG );
			xmlout.WriteAttributeString( STATE_ATTRIBUTE, _state );

			if ( _defined )
				xmlout.WriteString( _value );
			else
			{
				xmlout.WriteStartElement( UNDEFINED_TAG );
				xmlout.WriteEndElement();
			}

			xmlout.WriteEndElement();
		}

		public void WriteOldXML( XmlTextWriter xmlout )
		{
			xmlout.WriteElementString( STATE_ATTRIBUTE, _state );

			if ( _defined )
				xmlout.WriteElementString( TAG, _value );
			else
			{
				xmlout.WriteStartElement( TAG );
				xmlout.WriteStartElement( UNDEFINED_TAG );
				xmlout.WriteEndElement(); // UNDEFINED_TAG
				xmlout.WriteEndElement(); // TAG
			}
		}

		public override void ParseContents(XmlTextReader xml)
		{
			if ( xml.Name != TAG )
				throw new FormatException( "Value does not start with " + TAG );

			_state = xml.GetAttribute( STATE_ATTRIBUTE );
			if ( _state == null )
				throw new FormatException( TAG + " tag does not include required " + STATE_ATTRIBUTE + " attribute." );

			while( xml.Read() )
			{
				if ( xml.NodeType == XmlNodeType.Element && xml.Name == UNDEFINED_TAG )
				{
					_defined = false;
					_value = null;
					return;
				}
				else if ( xml.NodeType == XmlNodeType.Text )
				{
					_value = xml.Value;
				}
				else if ( xml.NodeType == XmlNodeType.EndElement )
				{
					if ( xml.Name == TAG )
						return;
				}
			}
		}

		public override void ApplyData(VariableTable varTable)
		{
			// if this method is called, it means that a ValueData object is the 
			// only object in the state-change-notification
			ApplianceState state = (ApplianceState)varTable[ _state ];

			if (! this.Defined )
				state.Undefine();
			else
			{
				varTable.StoreValue( _state, _value );
				state.ValueChanged();
			}
		}

		public override void ApplyData( string fullName, VariableTable varTable, Hashtable table, ArrayList indices ) 
		{
			ApplianceState state = (ApplianceState)varTable[ fullName + "." + _state ];
			int index = (int)indices[ indices.Count-1 ];

			object value = state.Type.ValueSpace.Validate( _value );

			string[] names = _state.Split( VariableTable.NAME_SEPARATORS );
			ArrayList list = (ArrayList)table[ names[ 0 ] ];
			if ( list == null )
			{
				list = new ArrayList();
				table[ names[ 0 ] ] = list;
			}

			if ( names.Length == 1 )
			{
				if ( list.Count == index )
					list.Add( value );
				else if ( list.Count < index )
				{
					for( int i = list.Count; i < index; i++ )
						list.Add( null );
					list.Add( value );
				}
				else
					list[ index ] = value;
			}
			else
			{
				table = (Hashtable)list[ index ];
				if ( table == null )
				{
					table = new Hashtable();
					list[ index ] = table;
				}

				for( int i = 1; i < names.Length-1; i++ )
				{
					Hashtable currentTable = (Hashtable)table[ names[ i ] ];
					if ( currentTable == null )
					{
						currentTable = new Hashtable();
						table[ names[ i ] ] = currentTable;
					}

					table = currentTable;
				}

				table[ names[ names.Length-1 ] ] = value;
			}

			varTable.ScheduleStateUpdate( state );
		}
	}


	public class MultipleValueData : PUCData
	{
		/*
		 * Member Variables
		 */

		protected Hashtable _values;


		/*
		 * Constructors
		 */

		public MultipleValueData()
		{
			_values = new Hashtable();
		}

		public MultipleValueData( XmlTextReader xml )
			: base( xml )
		{
		}


		/*
		 * Properties
		 */

		public ValueData this[ string stateName ]
		{
			get
			{
				return (ValueData)_values[ stateName ];
			}
		}

		public int Count
		{
			get
			{
				return _values.Count;
			}
		}


		/*
		 * Member Methods
		 */

		public void AddValue( ValueData val )
		{
			_values[ val.State ] = val;
		}

		public void RemoveValue( ValueData val )
		{
			_values.Remove( val.State );
		}

		public override void ParseContents(XmlTextReader xml)
		{
			_values = new Hashtable();

			do
			{
				if ( xml.NodeType == XmlNodeType.Element && xml.Name == ValueData.TAG )
					AddValue( new ValueData( xml ) );
				else if ( xml.NodeType == XmlNodeType.EndElement && xml.Name != ValueData.TAG )
					return;
			}

			while( xml.Read() );
		}

		public override void WriteXML(XmlTextWriter xmlout)
		{
			IEnumerator e = _values.Values.GetEnumerator();
			while( e.MoveNext() )
				((ValueData)e.Current).WriteXML( xmlout );
		}

		public override void ApplyData(VariableTable varTable)
		{
			// this shouldn't ever happen
			throw new NotSupportedException( "Attempt to call root ApplyData() method for MultipleValueData" );
		}

		public override void ApplyData( string fullName, VariableTable varTable, Hashtable table, ArrayList indices ) 
		{
			IEnumerator e = _values.Values.GetEnumerator();
			while( e.MoveNext() )
				((ValueData)e.Current).ApplyData( fullName, varTable, table, indices );
		}
	}


	/// <summary>
	/// The base class of any data operation.
	/// </summary>
	public abstract class OpData : PUCData, IStateNameData
	{
		/*
		 * Constants
		 */

		public const string STATE_ATTRIBUTE	= "state";


		/*
		 * Member Variables
		 */

		protected string	_state;


		/*
		 * Constructors
		 */

		protected OpData( string state )
		{
			_state = state;
		}

		protected OpData( XmlTextReader xml )
			: base( xml )
		{
		}


		/*
		 * Properties
		 */

		public string State
		{
			get
			{
				return _state;
			}
			set
			{
				_state = value;
			}
		}


		/*
		 * Member Methods
		 */

		protected Hashtable getListTable( string state, string fullName, Hashtable table, int index )
		{
			String[] names = state.Split( VariableTable.NAME_SEPARATORS );
			ArrayList list = (ArrayList)table[ names[ 0 ] ];
			if ( list == null )
			{
				list = new ArrayList();
				table[ names[ 0 ] ] = list;
				if ( list.Count == index )
					list.Add( null );
				else if ( list.Count < index )
				{
					for( int i = list.Count; i <= index; i++ )
						list.Add( null );
				}
			}

			table = (Hashtable)list[ index ];
			if ( table == null )
			{
				table = new Hashtable();
				list[ index ] = table;
			}

			for( int i = 1; i < names.Length; i++ )
			{
				Hashtable currentTable = (Hashtable)table[ names[ i ] ];
				if ( currentTable == null )
				{
					currentTable = new Hashtable();
					table[ names[ i ] ] = currentTable;
				}
				table = currentTable;
			}

			return table;
		}
	}


	/// <summary>
	/// This class represents any data operation
	/// that contains multiple elements.
	/// </summary>
	public abstract class ElementData : OpData
	{
		/*
		 * Constants
		 */

		public const string ELEMENT_TAG		= "el";


		/*
		 * Member Variables
		 */

		protected ArrayList _elements;


		/*
		 * Constructors
		 */

		protected ElementData( string state )
			: base( state )
		{
			_elements = new ArrayList();
		}

		protected ElementData( XmlTextReader xml )
			: base( xml )
		{
		}


		/*
		 * Properties
		 */

		public PUCData this[ int index ]
		{
			get
			{
				return (PUCData)_elements[ index ];
			}
		}

		public int Count
		{
			get
			{
				return _elements.Count;
			}
		}


		/*
		 * Member Variables
		 */

		public void AddElement( PUCData data )
		{
			_elements.Add( data );
		}

		public void InsertElement( int index, PUCData data )
		{
			_elements.Insert( index, data );
		}

		public void RemoveElementAt( int index )
		{
			_elements.RemoveAt( index );
		}

		public override void WriteXML(XmlTextWriter xmlout)
		{
			IEnumerator e = _elements.GetEnumerator();
			while( e.MoveNext() )
			{
				xmlout.WriteStartElement( ELEMENT_TAG );
				((PUCData)e.Current).WriteXML( xmlout );
				xmlout.WriteEndElement();
			}
		}

		public override void ParseContents(XmlTextReader xml)
		{
			_elements = new ArrayList();

			// this makes sure that we've seen an </el> for every <el>
			// that we've seen.  This is how we determine when we're
			// done parsing this collection of elements.
			int pairCheck = 0;

			do
			{
				if ( xml.NodeType == XmlNodeType.Element && xml.Name == ELEMENT_TAG )
				{
					pairCheck++;
					_elements.Add( PUCData.Parse( xml, false ) );
				}
				else if ( xml.NodeType == XmlNodeType.EndElement && xml.Name == ELEMENT_TAG )
					pairCheck--;
				else if ( pairCheck == 0 && 
						  xml.NodeType == XmlNodeType.EndElement && 
						  xml.Name != ELEMENT_TAG )
					return;
			}

			while( xml.Read() );
		}

		public override void ApplyData( string fullName, VariableTable varTable, Hashtable table, ArrayList indices )
		{
			// every class deriving from this one should find it's new Hashtable and pass
			// it to this method.  So, this method will not implement the search for the
			// proper table.  -1 should be passed as the index if this is not in a nested 
			// list

			int start = (int)indices[ indices.Count-1 ];
			for( int i = 0; i < _elements.Count; i++ )
			{
				indices[ indices.Count-1 ] = start + i;
				((PUCData)_elements[ i ]).ApplyData( fullName, varTable, table, indices );
			}
		}
	}

	public class ListData : ElementData
	{
		/*
		 * Constants
		 */

		public const string DATA_TAG		= "data";
		public const string CHANGE_TAG		= "change";
		public const string INDEX_ATTRIBUTE = "index";


		/*
		 * Enumeration
		 */

		public enum SelectionType { One, All };


		/*
		 * Member Variables
		 */

		protected SelectionType _select;
		protected int _index;


		/*
		 * Constructors
		 */

		public ListData( String state )
			: base( state )
		{
			Index = -1;
		}

		public ListData( String state, int index )
			: base( state )
		{
			Index = index;
		}

		public ListData( XmlTextReader xml )
			: base( xml )
		{
		}


		/*
		 * Properties
		 */

		public SelectionType Selection
		{
			get
			{
				return _select;
			}
		}

		public int Index
		{
			get
			{
				return _index;
			}
			set
			{
				_index = value;

				if ( _index < 0 )
					_select = SelectionType.All;
				else
					_select = SelectionType.One;
			}
		}


		/*
		 * Member Methods
		 */

		public override void ParseContents(XmlTextReader xml)
		{
			if ( xml.NodeType == XmlNodeType.Element && 
				 !(xml.Name == DATA_TAG || xml.Name == CHANGE_TAG ))
				throw new FormatException( "Incorrect parsing function called." );

			_state = xml.GetAttribute( STATE_ATTRIBUTE );
			if ( _state == null )
				throw new FormatException( DATA_TAG + " elements must have " + STATE_ATTRIBUTE + " attribute." );

			string numStr = xml.GetAttribute( INDEX_ATTRIBUTE );
			if ( numStr != null )
			{
				_select = SelectionType.One;
				_index = Int32.Parse( numStr );
			}
			else
				_select = SelectionType.All;

			base.ParseContents( xml );
		}

		public override void WriteXML(XmlTextWriter xmlout)
		{
			if ( _select == SelectionType.One )
				xmlout.WriteStartElement( CHANGE_TAG );
			else
				xmlout.WriteStartElement( DATA_TAG );

			xmlout.WriteAttributeString( STATE_ATTRIBUTE, _state );
			if ( _select == SelectionType.One )
				xmlout.WriteAttributeString( INDEX_ATTRIBUTE, _index.ToString() );

			base.WriteXML( xmlout );

			xmlout.WriteEndElement();
		}

		public override void ApplyData(VariableTable varTable)
		{
			Hashtable table = (Hashtable)varTable.GetListStructure( _state );

			applyDataCommon( _state, varTable, table, new ArrayList() );
		}

		public override void ApplyData(string fullName, VariableTable varTable, Hashtable table, ArrayList indices)
		{
			table = getListTable( _state, fullName, table, (int)indices[ indices.Count-1 ] );			

			applyDataCommon( fullName + "." + _state, varTable, table, indices );
		}

		public void applyDataCommon( string fullName, VariableTable varTable, Hashtable table, ArrayList indices )
		{
			if ( _select == SelectionType.All )
			{
				// create null items list
				ArrayList nullItems = new ArrayList();
				for( int i = 0; i < _elements.Count; i++ )
					nullItems.Add( null );

				// clear all items from the list
				IEnumerator e = table.Keys.GetEnumerator();
				while( e.MoveNext() )
				{
					if ( ListGroupNode.LIST_LENGTH_STATE == (string)e.Current ||
						ListGroupNode.LIST_SELECTION_STATE == (string)e.Current )
						continue;

					((ArrayList)table[e.Current]).Clear();
					((ArrayList)table[e.Current]).InsertRange( 0, nullItems );	
				}

				indices.Add( 0 );
				base.ApplyData( fullName , varTable, table, indices );
				indices.RemoveAt( indices.Count-1 );

				// update list length
				ListGroupNode lg = varTable.GetListGroup( fullName );
				varTable.StoreValue( lg.LengthState.FullName, _elements.Count );
				lg.LengthState.ValueChanged();

				// fire list changed event
				lg.ListChanged( varTable, new RefreshAllListEventArgs( getIntArray( indices ) ) );
			}
			else // _select == SelectionType.One
			{
				// index-1 because lists are 1-indexed
				indices.Add( _index-1 );
				base.ApplyData( fullName, varTable, table, indices );
				indices.RemoveAt( indices.Count-1 );
			}
		}
	}

	public class ListInsert : ElementData
	{
		/*
		 * Constants
		 */

		public const string INSERT_TAG		= "insert";
		public const string AFTER_ATTRIBUTE	= "after";


		/*
		 * Member Variables
		 */

		protected int _after;


		/*
		 * Constructors
		 */

		public ListInsert( string state, int after )
			: base( state )
		{
			_after = after;
		}

		public ListInsert( XmlTextReader xml )
			: base( xml )
		{
		}


		/*
		 * Properties
		 */

		public int After
		{
			get
			{
				return _after;
			}
			set
			{
				_after = value;
			}
		}


		/*
		 * Member Methods
		 */

		public override void ParseContents(XmlTextReader xml)
		{
			if ( xml.NodeType == XmlNodeType.Element && xml.Name != INSERT_TAG )
				throw new FormatException( "Incorrect parsing function called." );

			_state = xml.GetAttribute( STATE_ATTRIBUTE );
			if ( _state == null )
				throw new FormatException( INSERT_TAG + " elements must have " + STATE_ATTRIBUTE + " attribute." );

			string numStr = xml.GetAttribute( AFTER_ATTRIBUTE );
			if ( numStr == null )
				throw new FormatException( INSERT_TAG + " tag must have a " + AFTER_ATTRIBUTE + " attribute." );
			_after = Int32.Parse( numStr );

			base.ParseContents( xml );
		}

		public override void WriteXML(XmlTextWriter xmlout)
		{
			xmlout.WriteStartElement( INSERT_TAG );
			xmlout.WriteAttributeString( STATE_ATTRIBUTE, _state );
			xmlout.WriteAttributeString( AFTER_ATTRIBUTE, _after.ToString() );

			base.WriteXML( xmlout );

			xmlout.WriteEndElement();
		}

		public override void ApplyData(VariableTable varTable)
		{
			Hashtable table = (Hashtable)varTable.GetListStructure( _state );

			applyDataCommon( _state, varTable, table, new ArrayList() );
		}

		public override void ApplyData(string fullName, VariableTable varTable, Hashtable table, ArrayList indices)
		{
			table = getListTable( _state, fullName, table, (int)indices[ indices.Count-1 ] );			

			applyDataCommon( fullName + "." + _state, varTable, table, indices );
		}

		public void applyDataCommon( string fullName, VariableTable varTable, Hashtable table, ArrayList indices )
		{
			// make a collection of null items to insert into every list
			ArrayList nullItems = new ArrayList();
			for( int i = 0; i < _elements.Count; i++ )
				nullItems.Add( null );

			// insert items into every list
			IEnumerator e = table.Keys.GetEnumerator();
			while( e.MoveNext() )
			{
				if ( ListGroupNode.LIST_LENGTH_STATE == (string)e.Current ||
					 ListGroupNode.LIST_SELECTION_STATE == (string)e.Current )
					continue;

				((ArrayList)table[e.Current]).InsertRange( _after, nullItems );
			}

			indices.Add( _after );
			base.ApplyData( fullName, varTable, table, indices );
			indices.RemoveAt( indices.Count-1 );

			// update list length
			ListGroupNode lg = varTable.GetListGroup( fullName );
			int oldLen = (int)lg.LengthState.Value;
			varTable.StoreValue( lg.LengthState.FullName, oldLen + _elements.Count );
			lg.LengthState.ValueChanged();

			// fire list changed event
			lg.ListChanged( varTable, new InsertListEventArgs( getIntArray( indices ), _after, _elements.Count ) );
		}
	}

	public class ListReplace : ElementData
	{
		/*
		 * Constants
		 */

		public const string REPLACE_TAG		 = "replace";
		public const string BEGIN_ATTRIBUTE  = "begin";
		public const string LENGTH_ATTRIBUTE = "length";


		/*
		 * Member Variables
		 */

		protected int _begin;
		protected int _length;


		/*
		 * Constructors
		 */

		public ListReplace( string state, int begin, int length )
			: base( state )
		{
			_begin = begin;
			_length = length;
		}

		public ListReplace( XmlTextReader xml )
			: base( xml )
		{
		}


		/*
		 * Properties
		 */

		public int Begin
		{
			get
			{
				return _begin;
			}
			set
			{
				_begin = value;
			}
		}

		public int Length
		{
			get
			{
				return _length;
			}
			set
			{
				_length = value;
			}
		}


		/*
		 * Member Methods
		 */

		public override void ParseContents(XmlTextReader xml)
		{
			if ( xml.NodeType == XmlNodeType.Element && xml.Name != REPLACE_TAG )
				throw new FormatException( "Incorrect parsing function called." );

			_state = xml.GetAttribute( STATE_ATTRIBUTE );
			if ( _state == null )
				throw new FormatException( REPLACE_TAG + " elements must have " + STATE_ATTRIBUTE + " attribute." );

			string numStr = xml.GetAttribute( BEGIN_ATTRIBUTE );
			if ( numStr == null )
				throw new FormatException( REPLACE_TAG + " tag must have a " + BEGIN_ATTRIBUTE + " attribute." );
			_begin = Int32.Parse( numStr );

			numStr = xml.GetAttribute( LENGTH_ATTRIBUTE );
			if ( numStr == null )
				throw new FormatException( REPLACE_TAG + " tag must have a " + LENGTH_ATTRIBUTE + " attribute." );
			_length = Int32.Parse( numStr );

			base.ParseContents( xml );
		}

		public override void WriteXML(XmlTextWriter xmlout)
		{
			xmlout.WriteStartElement( REPLACE_TAG );
			xmlout.WriteAttributeString( STATE_ATTRIBUTE, _state );
			xmlout.WriteAttributeString( BEGIN_ATTRIBUTE, _begin.ToString() );
			xmlout.WriteAttributeString( LENGTH_ATTRIBUTE, _length.ToString() );

			base.WriteXML( xmlout );

			xmlout.WriteEndElement();
		}

		public override void ApplyData(VariableTable varTable)
		{
			Hashtable table = (Hashtable)varTable.GetListStructure( _state );

			applyDataCommon( _state, varTable, table, new ArrayList() );
		}

		public override void ApplyData(string fullName, VariableTable varTable, Hashtable table, ArrayList indices)
		{
			table = getListTable( _state, fullName, table, (int)indices[ indices.Count-1 ] );

			applyDataCommon( fullName + "." + _state, varTable, table, indices );
		}

		public void applyDataCommon( string fullName, VariableTable varTable, Hashtable table, ArrayList indices )
		{
			if ( _elements.Count < _length )
			{
				// remove extra items from every list
				IEnumerator e = table.Keys.GetEnumerator();
				while( e.MoveNext() )
				{
					if ( ListGroupNode.LIST_LENGTH_STATE == (string)e.Current ||
						ListGroupNode.LIST_SELECTION_STATE == (string)e.Current )
						continue;

					((ArrayList)table[e.Current]).RemoveRange( (_begin-1) + _elements.Count, _length - _elements.Count );
				}
			}
			else if ( _elements.Count > _length )
			{
				// make a collection of null items to insert into every list
				ArrayList nullItems = new ArrayList();
				for( int i = 0; i < (_elements.Count-_length); i++ )
					nullItems.Add( null );

				// insert items into every list
				IEnumerator e = table.Keys.GetEnumerator();
				while( e.MoveNext() )
				{
					if ( ListGroupNode.LIST_LENGTH_STATE == (string)e.Current ||
						ListGroupNode.LIST_SELECTION_STATE == (string)e.Current )
						continue;

					((ArrayList)table[e.Current]).InsertRange( _begin+_length-1, nullItems );
				}
			}

			indices.Add( _begin-1 );
			base.ApplyData( fullName, varTable, table, indices );
			indices.RemoveAt( indices.Count-1 );

			// update list length
			ListGroupNode lg = varTable.GetListGroup( fullName );
			int oldLen = (int)lg.LengthState.Value;
			varTable.StoreValue( lg.LengthState.FullName, oldLen - _length + _elements.Count );
			lg.LengthState.ValueChanged();

			// fire list changed event
			if ( _elements.Count == _length )
				lg.ListChanged( varTable, new ChangeListEventArgs( getIntArray( indices ), _begin-1, _length ) );
			else
				lg.ListChanged( varTable, new ChangeListEventArgs( getIntArray( indices ), _begin-1, _length, _elements.Count ) );
		}
	}

	public class ListDelete : OpData
	{
		/*
		 * Constants
		 */

		public const string DELETE_TAG		 = "delete";
		public const string BEGIN_ATTRIBUTE  = "begin";
		public const string LENGTH_ATTRIBUTE = "length";


		/*
		 * Member Variables
		 */

		protected int	 _begin;
		protected int	 _length;


		/*
		 * Constructors
		 */

		public ListDelete( string state, int begin, int length )
			: base( state )
		{
			_begin = begin;
			_length = length;
		}

		public ListDelete( XmlTextReader xml )
			: base( xml )
		{
		}


		/*
		 * Properties
		 */

		public int Begin
		{
			get
			{
				return _begin;
			}
			set
			{
				_begin = value;
			}
		}

		public int Length
		{
			get
			{
				return _length;
			}
			set
			{
				_length = value;
			}
		}


		/*
		 * Member Methods
		 */

		public override void ParseContents(XmlTextReader xml)
		{
			if ( xml.NodeType == XmlNodeType.Element && xml.Name != DELETE_TAG )
				throw new FormatException( "Incorrect parsing function called." );

			_state = xml.GetAttribute( ElementData.STATE_ATTRIBUTE );
			if ( _state == null )
				throw new FormatException( DELETE_TAG + " elements must have " + ElementData.STATE_ATTRIBUTE + " attribute." );

			string numStr = xml.GetAttribute( BEGIN_ATTRIBUTE );
			if ( numStr == null )
				throw new FormatException( DELETE_TAG + " tag must have a " + BEGIN_ATTRIBUTE + " attribute." );
			_begin = Int32.Parse( numStr );

			numStr = xml.GetAttribute( LENGTH_ATTRIBUTE );
			if ( numStr == null )
				throw new FormatException( DELETE_TAG + " tag must have a " + LENGTH_ATTRIBUTE + " attribute." );
			_length = Int32.Parse( numStr );
		}

		public override void WriteXML(XmlTextWriter xmlout)
		{
			xmlout.WriteStartElement( DELETE_TAG );
			xmlout.WriteAttributeString( ElementData.STATE_ATTRIBUTE, _state );
			xmlout.WriteAttributeString( BEGIN_ATTRIBUTE, _begin.ToString() );
			xmlout.WriteAttributeString( LENGTH_ATTRIBUTE, _length.ToString() );
			xmlout.WriteEndElement();
		}

		public override void ApplyData(VariableTable varTable)
		{
			Hashtable table = (Hashtable)varTable.GetListStructure( _state );

			applyDataCommon( _state, varTable, table, new ArrayList() );
		}

		public override void ApplyData(string fullName, VariableTable varTable, Hashtable table, ArrayList indices)
		{
			table = getListTable( _state, fullName, table, (int)indices[ indices.Count-1 ] );			

			applyDataCommon( fullName + "." + _state, varTable, table, indices );
		}

		public void applyDataCommon( string fullName, VariableTable varTable, Hashtable table, ArrayList indices )
		{
			// remove extra items from every list
			IEnumerator e = table.Keys.GetEnumerator();
			while( e.MoveNext() )
			{
				if ( ListGroupNode.LIST_LENGTH_STATE == (string)e.Current ||
					ListGroupNode.LIST_SELECTION_STATE == (string)e.Current )
					continue;

				((ArrayList)table[e.Current]).RemoveRange( _begin-1, _length );
			}

			// update list length
			ListGroupNode lg = varTable.GetListGroup( fullName );
			int oldLen = (int)lg.LengthState.Value;
			varTable.StoreValue( lg.LengthState.FullName, oldLen - _length );
			lg.LengthState.ValueChanged();

			// fire list changed event
			lg.ListChanged( varTable, new DeleteListEventArgs( getIntArray( indices ), _begin-1, _length ) );
		}
	}
}
