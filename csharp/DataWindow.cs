using System;
using System.Collections;

using PUC.Communication;


namespace PUC
{
	/// <summary>
	/// The interface that defines a DataWindow.  Objects that 
	/// implement this interface provide a view onto the data 
	/// that is stored in the VariableTable, which is linked
	/// to their parent DataWindows (if any).  This allows 
	/// pointers to certain types of data to stay independent
	/// of the whatever list indices defined beneath them.
	/// </summary>
	public interface IDataWindow
	{
		/// <summary>
		/// Gets the full name of this window in the data
		/// space (includes indices, etc).
		/// </summary>
		string DataName
		{
			get;
		}

		/// <summary>
		/// The name of the data window that this object 
		/// refers to.
		/// </summary>
		string DataWindow 
		{
			get;
		}

		/// <summary>
		/// The reference that the data window is currently
		/// attached to.  May be null if the reference does
		/// not exist yet or the value is undefined.
		/// </summary>
		object Reference
		{
			get;
		}

		/// <summary>
		/// Defines whether this window has a valid pointer 
		/// into the VariableTable.
		/// </summary>
		bool Valid
		{
			get;
		}

		/// <summary>
		/// Called by any number of methods to update the 
		/// current window, presumably because a parent window
		/// has been updated.
		/// </summary>
		void Update();

		/// <summary>
		/// The parent window of this DataWindow.  If this is the
		/// root data window, Parent is null.
		/// </summary>
		IBranchDataWindow Parent
		{
			get;
			set;
		}
	}

	/// <summary>
	/// This interface defines a data window that has other data 
	/// windows that depend upon it.
	/// </summary>
	public interface IBranchDataWindow : IDataWindow
	{
		/// <summary>
		/// Add a child window to this data window.
		/// </summary>
		/// <param name="win">the data window to add</param>
		void AddChildWindow( IDataWindow win );

		/// <summary>
		/// Remove an existing child window from this
		/// data window.  Nothing happens if win is not
		/// a child of this data window.
		/// </summary>
		/// <param name="win">the data window to remove</param>
		void RemoveChildWindow( IDataWindow win );

		/// <summary>
		/// Remove all child windows.  This is useful
		/// if you want to connect a whole set of windows
		/// from the data source.
		/// </summary>
		void Clear();

		/// <summary>
		/// This method is used to assemble the data package that
		/// will be transmitted in a PUC StateChangeRequest message.
		/// </summary>
		/// <param name="data">the package that has been constructed so far</param>
		/// <returns>a new PUCData object contructed from the package in data</returns>
		IStateNameData AssemblePUCData( IStateNameData data );

		/// <summary>
		/// Get the full path to the topmost group in this data window.
		/// This is used to determine the scope of child windows.
		/// </summary>
		string TopPath
		{
			get;
		}
	}

	public class ValueDataWindow : IDataWindow
	{
		/*
		 * Member Variables
		 */

		protected string[]			_windowNames;
		protected object			_windowRef;
		protected IBranchDataWindow	_windowParent;

		protected ApplianceState	_state;

		/*
		 * Constructor
		 */

		public ValueDataWindow( ApplianceState state )
		{
			_state = state;

			state.Parent.AddChildWindow( this );
			this.Update();
		}

		public ValueDataWindow( ValueDataWindow valueWindow )
		{
			_state = valueWindow._state;

			valueWindow.Parent.AddChildWindow( this );
			this.Update();
		}


		/*
		 * Properties
		 */

		public virtual object Value
		{
			get
			{
				if ( _windowRef == null )
				{
					Update();

					if ( _windowRef == null )
						return null;
				}

				if ( _windowRef is ArrayList )
					// -1 because list indices are 1-indexed
					return ((ArrayList)_windowRef)[ ((IndexedDataWindow)_windowParent).Index-1 ];

				return ((Hashtable)_windowRef)[ _windowNames[ _windowNames.Length-1 ] ];
			}
		}


		public ApplianceState State
		{
			get
			{
				return _state;
			}
		}

		#region IDataWindow Members

		public string DataName
		{
			get
			{
				return _windowParent.DataName + VariableTable.NAME_SEPARATOR + DataWindow;
			}
		}

		public string DataWindow
		{
			get
			{
				if ( _windowNames == null )
					return null;

				IEnumerator e = _windowNames.GetEnumerator();
				e.MoveNext();
				string names = (string)e.Current;
				while( e.MoveNext() )
					names += VariableTable.NAME_SEPARATOR + (string)e.Current;

				return names;
			}
		}

		public object Reference
		{
			get
			{
				return _windowRef;
			}
		}

		public bool Valid
		{
			get
			{
				return ( _windowNames != null ) && ( _windowRef != null );
			}
		}

		public void updateParentsIfNecessary()
		{
			IDataWindow window = this;
			
			while( !window.Parent.Valid )
				window = window.Parent;

			window.Update();
		}

		public void Update()
		{
			if ( _windowNames == null || _windowParent == null || !_windowParent.Valid )
				return;

			try
			{
				// we don't check for intermediate null results (which may happen if 
				// the VariableTable has not been fully populated yet), but instead
				// allow NullPointerExceptions to be thrown, which we catch.

				if ( _windowParent is IndexedDataWindow )
				{
					IndexedDataWindow idxParent = (IndexedDataWindow)_windowParent;

					_windowRef = ((Hashtable)_windowParent.Reference)[ _windowNames[ 0 ] ];

					if ( _windowNames.Length != 1 ) 
					{
						// -1 is because list indices are 1-indexed
						_windowRef = ((ArrayList)_windowRef)[ idxParent.Index-1 ];

						// length-1 is to ensure we get a pointer to data, rather than data
						for( int i = 1; i < _windowNames.Length-1; i++ )
							_windowRef = ((Hashtable)_windowRef)[ _windowNames[ i ] ];
					}
				}
				else
				{
					if ( !_state.Defined )
						return;

					_windowRef = _windowParent.Reference;

					// length-1 is to ensure we get a pointer to data, rather than data
					for( int i = 0; i < _windowNames.Length-1; i++ )
						_windowRef = ((Hashtable)_windowRef)[ _windowNames[ i ] ];
				}
			}
			catch( Exception )
			{
				_windowRef = null;
			}
		}

		public IBranchDataWindow Parent
		{
			get
			{
				return _windowParent;
			}
			set
			{
				_windowParent = value;

				if ( _windowParent == null )
				{
					_windowNames = null;
					_windowRef = null;
					return;
				}

				// + 1 to get rid of separator character
				string relName = _state.FullName.Substring( _windowParent.TopPath.Length + 1 );
				_windowNames = relName.Split( VariableTable.NAME_SEPARATORS );
			}
		}

		#endregion

	}


	/// <summary>
	/// A class that provides a DataWindow into a particular index
	/// of 
	/// </summary>
	public class IndexedDataWindow : IBranchDataWindow
	{
		/*
		 * Events
		 */

		public event EventHandler IndexChanged;


		/*
		 * Member Variables
		 */

		protected ArrayList			_windows;

		protected string[]			_windowNames;
		protected object			_windowRef;
		protected IBranchDataWindow	_windowParent;

		protected int				_index;

		protected ListGroupNode		_listGroup;


		/*
		 * Constructor
		 */

		public IndexedDataWindow( ListGroupNode listGroup ) 
		{
			_listGroup = listGroup;
			_listGroup.ListDataChanged += new ListEvent(listGroup_ListDataChanged);

			_windows = new ArrayList();
			_index = 1;
		}

		public IndexedDataWindow( IndexedDataWindow indexedWindow )
		{
			_windows = new ArrayList();

			this._listGroup = indexedWindow._listGroup;
			_listGroup.ListDataChanged += new ListEvent(listGroup_ListDataChanged);
			this._index = indexedWindow._index;

			indexedWindow.Parent.AddChildWindow( this );
		}


		/*
		 * Properties
		 */

		/// <summary>
		/// A 1-indexed value marking a location in the list.
		/// </summary>
		public int Index
		{
			get
			{
				return _index;
			}
			set
			{
				if ( _index != value )
				{
					_index = value;
					updateChildren();

					if ( IndexChanged != null )
						IndexChanged( this, new EventArgs() );
				}
			}
		}

		public ListGroupNode ListGroup
		{
			get
			{
				return _listGroup;
			}
		}


		/*
		 * Member Methods
		 */

		protected void updateChildren()
		{	
			IEnumerator e = _windows.GetEnumerator();
			while( e.MoveNext() )
				((IDataWindow)e.Current).Update();
		}

		#region IBranchDataWindow Members
		
		/*
		 * IBranchDataWindow Methods
		 */

		public void AddChildWindow(IDataWindow win)
		{
			_windows.Add( win );
			win.Parent = this;
		}

		public void RemoveChildWindow(IDataWindow win)
		{
			if ( _windows.Contains( win ) )
			{
				_windows.Remove( win );
				win.Parent = null;
			}
		}

		public void Clear()
		{
			_windows.Clear();
		}

		public IStateNameData AssemblePUCData(IStateNameData data)
		{
			ListData listData = new ListData( this.DataWindow, _index );

			PUCData d = (PUCData)data;

			if ( d is ValueData )
			{
				MultipleValueData mv = new MultipleValueData();
				mv.AddValue( (ValueData)d );
				d = mv;
			}

			listData.AddElement( d );
	
			OpData ret = listData;

			if ( _windowParent != null )
				ret = (OpData)_windowParent.AssemblePUCData( listData );

			return ret;
		}

		public string TopPath
		{
			get
			{
				return _listGroup.FullPath;
			}
		}

		#endregion

		#region IDataWindow Members

		public string DataName
		{
			get
			{
				return _windowParent.DataName + VariableTable.NAME_SEPARATOR + DataWindow + "[" + Index + "]";
			}
		}

		public string DataWindow
		{
			get
			{
				if ( _windowNames == null )
					return null;

				IEnumerator e = _windowNames.GetEnumerator();
				e.MoveNext(); // move to the first element
				string names = (string)e.Current;
				while( e.MoveNext() )
					names += VariableTable.NAME_SEPARATOR + (string)e.Current;

				return names;
			}
		}

		public object Reference
		{
			get
			{
				return _windowRef;
			}
		}

		public bool Valid
		{
			get
			{
				return ( _windowNames != null ) && ( _windowRef != null );
			}
		}

		public void Update()
		{
			if ( _windowNames == null || _windowParent == null || !_windowParent.Valid )
				return;

			try
			{
				// we don't check for intermediate null results (which may happen if 
				// the VariableTable has not been fully populated yet), but instead
				// allow NullPointerExceptions to be thrown, which we catch.

				if ( _windowParent is IndexedDataWindow )
				{
					IndexedDataWindow idxParent = (IndexedDataWindow)_windowParent;

					_windowRef = ((Hashtable)_windowParent.Reference)[ _windowNames[ 0 ] ];
					// -1 because list indices are 1-indexed
					_windowRef = ((ArrayList)_windowRef)[ idxParent.Index-1 ];

					for( int i = 1; i < _windowNames.Length; i++ )
						_windowRef = ((Hashtable)_windowRef)[ _windowNames[ i ] ];
				}
				else
				{
					_windowRef = _windowParent.Reference;
					for( int i = 0; i < _windowNames.Length; i++ )
						_windowRef = ((Hashtable)_windowRef)[ _windowNames[ i ] ];
				}

				updateChildren();
			}
			catch( Exception )
			{
				_windowRef = null;
			}
		}

		public IBranchDataWindow Parent
		{
			get
			{
				return _windowParent;
			}
			set
			{
				_windowParent = value;

				if ( _windowParent == null )
				{
					_windowNames = null;
					_windowRef = null;
					return;
				}

				// + 1 to get rid of separator character
				string relName = _listGroup.FullPath.Substring( _windowParent.TopPath.Length + 1 );
				_windowNames = relName.Split( VariableTable.NAME_SEPARATORS );

				this.Update();				
			}
		}

		#endregion

		private void listGroup_ListDataChanged(ListGroupNode listGroup, ListEventArgs a)
		{
			this.Update();
		}
	}
}
