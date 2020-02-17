using System;
using System.Collections;

using PUC;
using PUC.Communication;
using PUC.Types;


namespace PUC
{
	/// <summary>
	/// The VariableStore is an object that manages the storage
	/// for all ApplianceStates in a particular appliance.
	/// </summary>
	public class VariableTable : IBranchDataWindow
	{
		/*
		 * Constants
		 */

		
		/*
		 * Member Variables
		 */

		public static readonly char		 NAME_SEPARATOR  = '.';
		public static readonly char[]    NAME_SEPARATORS = new char[1] { NAME_SEPARATOR };

		protected string    _specName;
		protected Hashtable _dataTable;
		protected Hashtable _varTable;
		protected Hashtable _uniqueNameTable;
		protected Hashtable _objsByName;

		protected Hashtable _statesToUpdate;

		protected ArrayList	_windows;


		/*
		 * Delegates
		 */

		public delegate void BinaryEventHandler( BinaryStateChangeNotification bscn );


		/*
		 * Events
		 */

		public event BinaryEventHandler BinaryDataChanged;


		/*
		 * Constructor
		 */

		public VariableTable( string specName )
		{
			_specName = specName;
			_dataTable = new Hashtable();
			_varTable = new Hashtable();
			_objsByName = new Hashtable();
			_uniqueNameTable = new Hashtable();
			_windows = new ArrayList();

			_statesToUpdate = new Hashtable();
		}


		/*
		 * Properties
		 */

		public string SpecName
		{
			get
			{
				return _specName;
			}
			set
			{
				_specName = value;
			}
		}


		/*
		 * Indexor
		 */

		public ApplianceObject this [string name]
		{
			get
			{
				return (ApplianceObject)_objsByName[ GetFullName( name ) ];
			}
		}


		/*
		 * Private Methods
		 */

		private object getNextVarObject( Hashtable table, string[] names, int index )
		{
			string name = names[ index ];

			// search for array indices in name
			if ( name.StartsWith( "[" ) )
			{
				// name begins with index, so remove it
				int endIdx = name.IndexOf( "]" );

				// extract the variable name
				name = name.Substring( endIdx+1, name.Length-endIdx-1 );
			}

			if ( name.EndsWith( "]" ) )
			{
				// name ends with index, so remove it (indices are not recorded in var table)
				int beginIdx = name.IndexOf( "[" );

				// extract the variable name (name-[number])
				name = name.Substring( 0, beginIdx );
			}

			return table[ name ];
		}

		private object getNextDataObject( Hashtable table, ref string[] names, int index, bool create )
		{
			string name = names[ index ];

			/*
			 * This routine moves array declarations around within the names array.
			 * If [##] is at the beginning of the array name, then it should be parsed
			 * as a part of the current name.  Otherwise it should be moved to the 
			 * beginning of the next name.
			 */

			if ( name.EndsWith( "]" ) )
			{

				// name ends with index, so extract the number
				// note that we begin this search at position 1, because the
				// name may also begin with a [ if this is a nested list.
				int beginIdx = name.IndexOf( "[", 1 );

				if ( beginIdx < 0 )
					throw new FormatException( "Variable name ends with ] but has no [" );

				names[ index + 1 ] = name.Substring( beginIdx, name.Length - beginIdx ) + names[ index + 1 ];
				names[ index ] = name = name.Substring( 0, beginIdx );
			}
			
			if ( name.StartsWith( "[" ) )
			{
				// name ends with index, so extract the number
				int endIdx = name.IndexOf( "]" );

				string strNumber = name.Substring( 1, endIdx-1 );
				int number = Int32.Parse( strNumber );

				// extract the variable name (name-[number])
				string varName = name.Substring( endIdx+1, name.Length-endIdx-1 );

				// if create, check for existence first and create
				if ( create && table[ varName ] == null )
					table[ varName ] = new Hashtable( number+1 );

				// now find the next object
				Hashtable list = (Hashtable)table[ varName ];

				if ( create && list.Count < number )
					list[ number ] = new Hashtable();

				return list[ number ];
			}
			else
			{
				if ( create && table[ name ] == null )
					table[ name ] = new Hashtable();

				return table[ name ];
			}
		}

		private void storeInNextDataObject( Hashtable table, string name, object data )
		{
			if ( name.StartsWith( "[" ) )
			{
				// name ends with index, so extract the number
				int endIdx = name.IndexOf( "]" );

				string strNumber = name.Substring( 1, endIdx-1 );
				int number = Int32.Parse( strNumber );

				// extract the variable name (name-[number])
				string varName = name.Substring( endIdx+1, name.Length-endIdx-1 );

				// if create, check for existence first and create
				if ( table[ varName ] == null )
					table[ varName ] = new Hashtable( number+1 );

				// now find the next object
				Hashtable list = (Hashtable)table[ varName ];
				list[ number ] = data;
			}
			else
				table[ name ] = data;
		}

		protected void registerVariable( string fullVarName, ApplianceState state )
		{
			string[] names = fullVarName.Split( NAME_SEPARATORS );
			Hashtable current = _varTable;

			if ( names[ 0 ] != _specName )
				throw new FormatException( "Variable name does not start with unique appliance name." );

			if ( names[ names.Length - 1 ] != state.Name )
				throw new FormatException( "Variable name does not end with same name as ApplianceState object" );

			for( int i = 1; i < names.Length-1; i++ )
			{
				if ( current[ names[ i ] ] == null )
					current[ names[ i ] ] = new Hashtable();

				current = (Hashtable)current[ names[ i ] ]; 
			}

			current[ state.Name ] = state;
			state.VariableTable = this;
		}

		protected void registerUniqueNames( string fullVarName )
		{
			string[] names = fullVarName.Split( NAME_SEPARATORS );
			Hashtable current = _varTable;

			if ( names[ 0 ] != _specName )
				throw new FormatException( "Object name does not start with unique appliance name." );

			string fullName = names[ 0 ] + GroupNode.NAME_SEPARATOR;
			for( int i = 1; i < names.Length; i++ )
			{
				fullName += names[ i ];

				if ( _uniqueNameTable[ names[ i ] ] == null )
				{
					// we have identified a unique name
					_uniqueNameTable[ names[ i ] ] = fullName;
				}
				else
				{
					// we may have identified a duplicate name
					
					// first check to see if the name is registered as not being unique
					if ( ! ( _uniqueNameTable[ names[ i ] ] is DuplicateName  ) )
					{
						// it is not, so now check if this name and the registered one
						// point to the same instance
						if ( ((string)_uniqueNameTable[ names[ i ] ]) != fullName )
						{
							// our name and the one in the table represent different
							// instances, so put in a DuplicateName token
							_uniqueNameTable[ names[ i ] ] = DuplicateName.GetToken();
						}
					}
				}

				fullName += GroupNode.NAME_SEPARATOR;
			}
		}


		/*
		 * Member Methods
		 */

		public void ScheduleStateUpdate( ApplianceState state )
		{
			_statesToUpdate[ state ] = true;
		}

		public void UpdateStates() 
		{
			IEnumerator e = _statesToUpdate.Keys.GetEnumerator();
			while( e.MoveNext() )
				((ApplianceState)e.Current).ValueChanged();

			_statesToUpdate.Clear();
		}

		public string GetFullName( string partialName )
		{
			if ( partialName.StartsWith( _specName + GroupNode.NAME_SEPARATOR ) )
				// this name is already a full name
				return partialName;
			
			int idx = partialName.IndexOf( GroupNode.NAME_SEPARATOR );

			string firstName = partialName;
			if ( idx > 0 )
				firstName = partialName.Substring( 0, idx );

			if ( _uniqueNameTable[ firstName ] == null ||
				 _uniqueNameTable[ firstName ] is DuplicateName )
				// this name doesn't exist or is ambiguous
				return null;

			string fullName = (string)_uniqueNameTable[ firstName ];

			if ( partialName.Length - firstName.Length > 0 )
				fullName += partialName.Substring( firstName.Length, partialName.Length - firstName.Length );

			return fullName;
		}

		public void RegisterListGroup( string fullName, ListGroupNode lg )
		{
			string[] names = fullName.Split( NAME_SEPARATORS );
			Hashtable current = _varTable;

			if ( names[ 0 ] != _specName )
				throw new FormatException( "List group name does not start with unique appliance name." );

			if ( names[ names.Length - 1 ] != lg.Name )
				throw new FormatException( "List group name does not end with same name as ListGroupNode object" );

			for( int i = 1; i < names.Length-1; i++ )
			{
				if ( current[ names[ i ] ] == null )
					current[ names[ i ] ] = new Hashtable();

				current = (Hashtable)current[ names[ i ] ]; 
			}

			if ( current[ lg.Name ] == null )
				current[ lg.Name ] = new Hashtable();

			current = (Hashtable)current[ lg.Name ];
			current[ ListGroupTag.GetToken() ] = lg;
		}

		public ListGroupNode GetListGroup( string listName )
		{
			string[] names = GetFullName( listName ).Split( NAME_SEPARATORS );
			Hashtable current = _varTable;

			if ( names[ 0 ] != _specName )
				throw new FormatException( "List group name does not start with unique appliance name." );

			for( int i = 1; i < names.Length-1; i++ )
			{
				if ( current[ names[ i ] ] == null )
					current[ names[ i ] ] = new Hashtable();

				current = (Hashtable)current[ names[ i ] ]; 
			}

			if ( current[ names[ names.Length-1 ] ] == null )
				current[ names[ names.Length-1 ] ] = new Hashtable();

			current = (Hashtable)current[ names[ names.Length-1 ] ];
			return (ListGroupNode)current[ ListGroupTag.GetToken() ];
		}

		public object GetListStructure( string fullListName )
		{
			string[] names = fullListName.Split( NAME_SEPARATORS );
			Hashtable current = _dataTable;

			if ( names[ 0 ] != _specName )
				throw new FormatException( "Variable name does not start with unique appliance name." );

			for( int i = 1; i < names.Length-1; i++ )
			{
				current = (Hashtable)getNextDataObject( current, ref names, i, true );

				if ( current == null )
					return null;
			}

			return getNextDataObject( current, ref names, names.Length-1, true );
		}

		public void RegisterObject( string fullName, ApplianceObject ao )
		{
			_objsByName[ fullName ] = ao;

			registerUniqueNames( fullName );

			if ( ao.State )
				registerVariable( fullName, (ApplianceState)ao );

			ao.FullName = fullName;
		}

		protected ApplianceState storeValue( string fullVarName, object data )
		{
			string[] names = fullVarName.Split( NAME_SEPARATORS );
			Hashtable currentData = _dataTable;
			Hashtable currentVar  = _varTable;

			if ( names[ 0 ] != _specName )
				throw new FormatException( "Variable name does not start with unique appliance name." );

			for( int i = 1; i < names.Length-1; i++ )
			{
				currentData = (Hashtable)getNextDataObject( currentData, ref names, i, true );
				currentVar  = (Hashtable)getNextVarObject( currentVar, names, i );

				if ( currentVar == null )
					throw new FormatException( "Value to store does not have a corresponding variable object." );
			}

			ApplianceState state = (ApplianceState)getNextVarObject( currentVar, names, names.Length-1 );
			
			object dataToStore = state.Type.ValueSpace.Validate( data );
			if ( dataToStore == null )
				throw new FormatException( "Data is not valid value for this variable." );

			storeInNextDataObject( currentData, names[ names.Length-1 ], dataToStore );

			return state;
		}

		public void StoreValue( string fullVarName, object data )
		{
			storeValue( fullVarName, data );
		}

		/*
		public object GetValue( string fullVarName )
		{
			string[] names = fullVarName.Split( NAME_SEPARATORS );
			Hashtable current = _dataTable;

			if ( names[ 0 ] != _specName )
				throw new FormatException( "Variable name does not start with unique appliance name." );

			for( int i = 1; i < names.Length-1; i++ )
			{
				current = (Hashtable)getNextDataObject( current, ref names, i, false );

				if ( current == null )
					return null;
			}

			return getNextDataObject( current, ref names, names.Length-1, false );
		}
		*/
		
		public void ClearAll()
		{
			Clear();
			_dataTable.Clear();
			_varTable.Clear();
			_uniqueNameTable.Clear();
			_objsByName.Clear();
		}

		public IEnumerator GetObjectEnumerator()
		{
			return _objsByName.Values.GetEnumerator();
		}

		public void HandleBinaryStateChangeNotification( BinaryStateChangeNotification bscn )
		{
			if ( BinaryDataChanged != null )
				BinaryDataChanged( bscn );
		}

		public void HandleStateChangeNotification( StateChangeNotification scn )
		{
			if ( scn.IsListData() ) 
			{
				// store list data, and update list groups
				scn.Data.ApplyData( this );

				this.UpdateStates();
			}
			else
			{
				// deal with a single state change
				ApplianceState state;

				if ( scn.Defined )
				{
					try
					{
						state = storeValue( scn.State, scn.Value );
						state.ValueChanged();
					}
					catch( Exception )
					{
						Console.WriteLine( "Couldn't set value of state " + scn.State + 
							" to " + scn.Value );
					}
				}
				else
				{
					state = (ApplianceState)this[ scn.State ];
					state.Undefine();
				}
			}
		}

		/*
		 * IBranchDataWindow Methods
		 */

		public void AddChildWindow( IDataWindow win )
		{
			_windows.Add( win );
			win.Parent = this;
		}

		public void RemoveChildWindow( IDataWindow win )
		{
			if ( _windows.Contains( win ) )
			{
				win.Parent = null;
				_windows.Remove( win );
			}
		}

		public void Clear()
		{
			_windows.Clear();
		}

		public string DataName
		{
			get
			{
				return DataWindow;
			}
		}

		public string DataWindow
		{
			get
			{
				return _specName;
			}
		}

		public string TopPath
		{
			get
			{
				return _specName;
			}
		}

		public object Reference
		{
			get
			{
				return _dataTable;
			}
		}

		public IBranchDataWindow Parent
		{
			get
			{
				return null;
			}
			set
			{
				throw new NotSupportedException( "can't change parent of root window!" );
			}
		}

		public bool Valid
		{
			get
			{
				return true;
			}
		}

		public void Update()
		{
			// don't update if we don't have a valid pointer
			if ( !Valid )
				return;

			IEnumerator e = _windows.GetEnumerator();
			while( e.MoveNext() )
				((IDataWindow)e.Current).Update();
		}

		public IStateNameData AssemblePUCData( IStateNameData data )
		{
			data.State = _specName + NAME_SEPARATOR + data.State;

			return data;
		}


		/*
		 * Static Main Testing Method
		 */

#if VARTABLE_DEBUG
		public static void Main( string[] args )
		{
			VariableTable table = new VariableTable( "TestSpec" );

			ApplianceState state1 = new ApplianceState( "TestState1", false );
			state1.Type = new PUCType( new IntegerSpace() );
			table.RegisterObject( "TestSpec.Group1.Group2.TestState1", state1 );

			ApplianceState state2 = new ApplianceState( "TestState2", false );
			state2.Type = new PUCType( new EnumeratedSpace( 4 ) );
			table.RegisterObject( "TestSpec.Group1.TestState2", state2 );

			ApplianceState state3 = new ApplianceState( "Flag", false );
			state3.Type = new PUCType( new BooleanSpace() );
			table.RegisterObject( "TestSpec.Group1.ListGroup.Flag", state3 );

			ApplianceState state4 = new ApplianceState( "Date", false );
			state4.Type = new PUCType( new StringSpace() );
			table.RegisterObject( "TestSpec.Group1.Mailbox.Messages.Date", state4 );

			ApplianceState state5 = new ApplianceState( "TestState1", false );
			state5.Type = new PUCType( new StringSpace() );
			table.RegisterObject( "TestSpec.Group1.Group1.TestState1", state5 );

			table.StoreValue( "TestSpec.Group1.Mailbox[3].Messages[4].Date", "Mon, Jan 4 11:15am" );
			table.StoreValue( "TestSpec.Group1.ListGroup[2].Flag", true );
			table.StoreValue( "TestSpec.Group1.Group2.TestState1", 2 );

			bool flag = (bool)table.GetValue( "TestSpec.Group1.ListGroup[2].Flag" );
		}
#endif

		/*
		 * Private Inner Classes
		 */

		public class ListGroupTag
		{
			/*
			 * Static Methods
			 */

			private static ListGroupTag _token;

			public static ListGroupTag GetToken()
			{
				return _token;
			}

			static ListGroupTag()
			{
				_token = new ListGroupTag();
			}


			/*
			 * Constructor
			 */

			private ListGroupTag() { }
		}

		/// <summary>
		/// This is a singleton class whose only instantiation can
		/// be obtained through a static method. The instance is used
		/// in the Variable table to mark names that have duplicate 
		/// usages.
		/// </summary>
		private class DuplicateName
		{
			/*
			 * Static Methods
			 */

			private static DuplicateName _token;

			public static DuplicateName GetToken()
			{
				if ( _token == null )
					_token = new DuplicateName();

				return _token;
			}


			/*
			 * Constructor
			 */

			private DuplicateName()
			{
			}
		}
	}
}
