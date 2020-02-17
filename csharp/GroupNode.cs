using System;
using System.Collections;

using PUC.Communication;
using PUC.Types;


namespace PUC
{
	/// <summary>
	/// GroupNode represents a node in the group tree, either as 
	/// parsed by the SpecParser or manipulated by the UIGenerator 
	/// objects.
	/// </summary>
	public abstract class GroupNode : Decorator, ICloneable
	{
		/*
		 * Constants
		 */
		public const int NO_IGNORE     = 0;
		public const int IGNORE_PARENT = 1;
		public const int IGNORE_ALL    = 2;

		public const string NAME_SEPARATOR = ".";


		/*
		 * Member Variables
		 */

		protected int             _priority;
		protected string		  _highlevelType;

		protected string		  _fullPath = null;
		protected BranchGroupNode _parent;
		protected LabelDictionary _labels;

		// variables used only during parsing (and thus public)
		public int                _depignore;
		public Dependency		  _dependencies;
		protected int			  _lineNumber;


		/*
		 * Constructor
		 */
		public GroupNode() 
		{
			_priority = -1;
			_parent = null;
			_labels = null;
			_depignore = NO_IGNORE;
			_dependencies = null;
		}


		/*
		 * Properties
		 */

		public int LineNumber
		{
			get
			{
				return _lineNumber;
			}
			set
			{
				_lineNumber = value;
			}
		}

		public LabelDictionary Labels
		{
			get
			{
				return _labels;
			}
			set
			{
				_labels = value;
			}
		}

		public abstract string Name
		{
			get;
			set;
		}

		public string HighlevelType
		{
			get
			{
				return _highlevelType;
			}
			set
			{
				_highlevelType = value;
			}
		}

		public int Priority
		{
			get
			{
				return _priority;
			}
			set
			{
				_priority = value;
			}
		}

		public string FullPath
		{
			get
			{
				if ( _fullPath == null )
				{
					if ( Parent == null )
						_fullPath = Name;
					else
						_fullPath = Parent.FullPath + NAME_SEPARATOR + Name;
				}

				return _fullPath;
			}
		}

		public virtual BranchGroupNode Parent
		{
			get
			{
				return _parent;
			}
			set
			{
				_parent = value;
			}
		}


		/*
		 * Member Methods
		 */

		public abstract object Clone();

		/// <summary>
		/// This method helps in the cloning process by allowing each 
		/// class in the hierarchy to clone their own member variables.
		/// The class being cloned will create a new instance of itself,
		/// and then call the cloneHelper in its super-class to get any
		/// member variables of its super classes to be copied.
		/// </summary>
		/// <param name="g">the instance of the cloned object</param>
		protected void cloneHelper( GroupNode g )
		{
			g._priority = _priority;
			g._highlevelType = _highlevelType;

			g._fullPath = _fullPath;
			g._parent = _parent;

			g._lineNumber = _lineNumber;
			g._depignore = _depignore;

			// TODO: does these need to be deep-copied?
			g._dependencies = _dependencies;
			g._labels = _labels;
		}
	
		public bool HasType()
		{
			return _highlevelType != null;
		}

		public virtual bool IsObject()
		{
			return false;
		}

		public virtual bool IsList()
		{
			return false;
		}

		public virtual bool IsUnion()
		{
			return false;
		}

		public abstract bool ContainsGroups();
		public abstract bool ContainsOnlyGroups();
	}

	public class ObjectGroupNode : GroupNode
	{
		/*
		 * Member Variables
		 */

		protected ApplianceObject _object;


		/*
		 * Constructor
		 */

		public ObjectGroupNode( ApplianceObject obj )
		{
			_object = obj;
		}

		/// <summary>
		/// For cloning purposes only.
		/// </summary>
		protected ObjectGroupNode()
		{
		}


		/*
		 * Properties
		 */

		public override string Name
		{
			get
			{
				return _object.Name;
			}
			set
			{
				throw new NotSupportedException( "ObjectGroupNode objects do allow their Name to be written." );
			}
		}

		public ApplianceObject Object
		{
			get
			{
				return _object;
			}
			set
			{
				_object = value;
			}
		}


		/*
		 * Member Methods
		 */

		public override object Clone()
		{
			// instantiate cloned object
			ObjectGroupNode obj = new ObjectGroupNode();

			// call the cloneHelper
			cloneHelper( obj );

			// return object
			return obj;
		}

		/// <summary>
		/// This method helps in the cloning process by allowing each 
		/// class in the hierarchy to clone their own member variables.
		/// The class being cloned will create a new instance of itself,
		/// and then call the cloneHelper in its super-class to get any
		/// member variables of its super classes to be copied.
		/// </summary>
		/// <param name="g">the instance of the cloned object</param>
		protected void cloneHelper( ObjectGroupNode g )
		{
			base.cloneHelper( g );

			// deep copy the object
			g._object = (ApplianceObject)_object.Clone();
		}

		public override bool IsObject()
		{
			return true;
		}

		public override bool ContainsGroups()
		{
			return false;
		}

		public override bool ContainsOnlyGroups()
		{
			return false;
		}
	}

	public class BranchGroupNode : GroupNode
	{
		/*
		 * Member Variables
		 */

		protected ArrayList       _children;
		protected Hashtable       _childContainers;

		protected string		  _name;

		protected ListGroupNode	  _parentList;

		
		/*
		 * Constructors
		 */

		public BranchGroupNode()
		{
			_children = new ArrayList();
			_childContainers = new Hashtable();
		}


		/*
		 * Properties
		 */

		public override string Name
		{
			get
			{
				return _name;
			}
			set
			{
				_name = value;
			}
		}

		public ArrayList Children
		{
			get
			{
				return _children;
			}
			set
			{
				_children = value;
			}
		}

		public Hashtable ChildContainers
		{
			get
			{
				return _childContainers;
			}
		}

		public int Count
		{
			get
			{
				return _children.Count;
			}
		}

		public override BranchGroupNode Parent
		{
			get
			{
				return base.Parent;
			}
			set
			{
				base.Parent = value;

				if ( value is ListGroupNode )
					_parentList = (ListGroupNode)value;
				else
					_parentList = value.ParentList;
			}
		}

		public ListGroupNode ParentList
		{
			get
			{
				return _parentList;
			}
		}


		/*
		 * Member Methods
		 */

		public override object Clone()
		{
			// instantiate cloned object
			BranchGroupNode obj = new BranchGroupNode();

			// call the cloneHelper
			cloneHelper( obj );

			// return object
			return obj;
		}

		/// <summary>
		/// This method helps in the cloning process by allowing each 
		/// class in the hierarchy to clone their own member variables.
		/// The class being cloned will create a new instance of itself,
		/// and then call the cloneHelper in its super-class to get any
		/// member variables of its super classes to be copied.
		/// </summary>
		/// <param name="g">the instance of the cloned object</param>
		protected void cloneHelper( BranchGroupNode g )
		{
			base.cloneHelper( g );

			g._name = _name;

			// I believe this will always be true...
			// if it is then there is no need to copy _childContainers
			System.Diagnostics.Debug.Assert( _childContainers.Count == 0 );

			// deep copy the children
			IEnumerator child = _children.GetEnumerator();
			while( child.MoveNext() )
				g._children.Add( ((GroupNode)child.Current).Clone() );
		}

#if !SMARTPHONE
		public PUC.UIGeneration.PanelNode GetContainerForChild( GroupNode child )
		{
			if ( _childContainers == null ) return null;

			return (PUC.UIGeneration.PanelNode)_childContainers[ child ];
		}
#endif

		public override bool ContainsGroups()
		{
			IEnumerator e = _children.GetEnumerator();
			while( e.MoveNext() )
			{
				if ( !((GroupNode)e.Current).IsObject() )
					return true;
			}

			return false;
		}

		public override bool ContainsOnlyGroups()
		{
			if ( this.Count <= 0 )
				return false;

			IEnumerator e = _children.GetEnumerator();
			while( e.MoveNext() )
			{
				if ( ((GroupNode)e.Current).IsObject() )
					return false;
			}

			return true;
		}
	}

	public enum SelectionType
	{
		One, Multiple
	}

	public class ListGroupNode : BranchGroupNode
	{
		/*
		 * Constants
		 */

		public const string LIST_LENGTH_STATE = "Length";
		public const string LIST_SELECTION_STATE = "Selection";
		public const string LIST_SELECTION_LENGTH_STATE = 
			LIST_SELECTION_STATE + "." + LIST_LENGTH_STATE;


		/*
		 * Member Variables
		 */

		protected Appliance				_appliance;
		protected PUC.Types.IPUCNumber	_itemCount;
		protected PUC.Types.IPUCNumber	_minimum;
		protected PUC.Types.IPUCNumber	_maximum;
		protected bool					_defined;
		protected bool					_selectionReadOnly;
		protected SelectionType			_selectionType;
		protected ApplianceState		_listLengthState;
		protected ApplianceState		_listSelectionState;
		protected ApplianceState		_listSelectionLengthState;
		protected IndexedDataWindow		_dataWindow;


		/*
		 * Events
		 */

		public event ListEvent ListDataChanged;

		public delegate void ListChangeRequestHandler( ListGroupNode lg, PUCData data );
		public event ListChangeRequestHandler ListChangeRequested;


		/*
		 * Constructor
		 */

		public ListGroupNode( Appliance appliance )
			: base()
		{
			_appliance = appliance;
			_minimum = null;
			_maximum = null;
			_itemCount = null;
			_selectionType = SelectionType.One;
			_defined = false;

			this.SetNetworkHandler();
		}

		/// <summary>
		/// For cloning purposes.
		/// </summary>
		protected ListGroupNode()
		{
		}


		/*
		 * Properties
		 */

		public Appliance Appliance
		{
			get
			{
				return _appliance;
			}
		}

		public IndexedDataWindow DataWindow
		{
			get
			{
				return _dataWindow;
			}
			set
			{
				_dataWindow = value;
			}
		}

		public IPUCNumber ItemCount
		{
			get
			{
				return _itemCount;
			}
			set
			{
				_itemCount = value;
			}
		}

		public IPUCNumber Minimum
		{
			get
			{
				return _minimum;
			}
			set
			{
				_minimum = value;
			}
		}

		public IPUCNumber Maximum
		{
			get
			{
				return _maximum;
			}
			set
			{
				_maximum = value;
			}
		}

		public bool Defined
		{
			get
			{
				return _defined;
			}
		}

		public SelectionType SelectionType
		{
			get
			{
				return _selectionType;
			}
			set
			{
				_selectionType = value;
			}
		}

		public bool SelectionReadOnly
		{
			get
			{
				if ( SelectionState != null )
					return SelectionState.ReadOnly;

				return _selectionReadOnly;
			}
			set
			{
				if ( SelectionState != null )
					throw new NotSupportedException( "Selection ReadOnly can only be set before extra states are created." );

				_selectionReadOnly = value;
			}
		}

		public ApplianceState LengthState
		{
			get
			{
				return _listLengthState;
			}
		}

		public ApplianceState SelectionState
		{
			get
			{
				return _listSelectionState;
			}
		}

		public ApplianceState SelectionLengthState
		{
			get
			{
				return _listSelectionLengthState;
			}
		}


		/*
		 * Member Methods
		 */

		public override object Clone()
		{
			// instantiate cloned object
			ListGroupNode obj = new ListGroupNode();

			// call the cloneHelper
			cloneHelper( obj );

			// return object
			return obj;
		}

		/// <summary>
		/// This method helps in the cloning process by allowing each 
		/// class in the hierarchy to clone their own member variables.
		/// The class being cloned will create a new instance of itself,
		/// and then call the cloneHelper in its super-class to get any
		/// member variables of its super classes to be copied.
		/// </summary>
		/// <param name="g">the instance of the cloned object</param>
		protected void cloneHelper( ListGroupNode g )
		{
			base.cloneHelper( g );

			// none of these objects should be defined yet
			System.Diagnostics.Debug.Assert( _listLengthState == null );
			System.Diagnostics.Debug.Assert( _listSelectionState == null );
			System.Diagnostics.Debug.Assert( _listSelectionLengthState == null );
			System.Diagnostics.Debug.Assert( _dataWindow == null );

			// shallow copy these
			g._appliance = _appliance;
			g._itemCount = _itemCount;
			g._minimum = _minimum;
			g._maximum = _maximum;
			g._defined = _defined;
			g._selectionReadOnly = _selectionReadOnly;
			g._selectionType = _selectionType;
		}

		public void SetNetworkHandler()
		{
			ListChangeRequested += new ListChangeRequestHandler(this.updateListData);
		}

		public void RequestChange( PUCData data )
		{
			if ( ListChangeRequested != null )
				ListChangeRequested( this, data );
		}

		protected virtual void updateListData( ListGroupNode lg, PUCData data )
		{
			try
			{
				_appliance.GetConnection().Send( new StateChangeRequest( data ) );
			}
			catch( Exception )
			{
			}
		}

		public override bool IsList()
		{
			return true;
		}

		public void CreateExtraStates( VariableTable varTable, IBranchDataWindow window )
		{
			// Add Length State
			_listLengthState = new ApplianceState( _appliance, LIST_LENGTH_STATE, true );
	
			PUC.Types.IntegerSpace intSpace;
			if ( _itemCount != null )
			{
				intSpace = new PUC.Types.IntegerSpace( _itemCount, _itemCount );
				if ( _itemCount is IConstraint )
					new TypeConstraintListener( _listLengthState, (IConstraint)_itemCount );
			}
			else if ( _minimum == null && _maximum == null )
				intSpace = new PUC.Types.IntegerSpace( new IntNumber( 0 ), new IntNumber( Int32.MaxValue ) );
			else if ( _minimum != null && _maximum != null )
			{
				intSpace = new PUC.Types.IntegerSpace( _minimum, _maximum );

				if ( _minimum is IConstraint )
					new TypeConstraintListener( _listLengthState, (IConstraint)_minimum );
				if ( _maximum is IConstraint )
					new TypeConstraintListener( _listLengthState, (IConstraint)_maximum );
			}
			else if ( _minimum != null )
			{
				intSpace = new IntegerSpace( _minimum, new IntNumber( Int32.MaxValue ) );

				if ( _minimum is IConstraint )
					new TypeConstraintListener( _listLengthState, (IConstraint)_minimum );
			}
			else
			{
				intSpace = new IntegerSpace( new IntNumber( 0 ), _maximum );

				if ( _maximum is IConstraint )
					new TypeConstraintListener( _listLengthState, (IConstraint)_maximum );
			}

			_listLengthState.Type = new PUCType( intSpace );

			GroupNode newG = new ObjectGroupNode( _listLengthState );
			newG.Parent = this;
			this.Children.Add( newG );
			varTable.RegisterObject( _listLengthState.MakeFullName( this.FullPath ), _listLengthState );
			window.AddChildWindow( _listLengthState );
			newG.Parent = null; // remove from group so it doesn't get in the way later
			this.Children.Remove( newG );

			// Add Selection States
			_listSelectionState = new ApplianceState( _appliance, LIST_SELECTION_STATE, _selectionReadOnly );
			_listSelectionState.Type = new PUCType( new IntegerSpace( new IntNumber( 0 ), new NumberConstraint( _listSelectionState, _listLengthState ) ) );

			if ( _selectionType == SelectionType.One )
			{
				// one selection
				newG = new ObjectGroupNode( _listSelectionState );
				newG.Parent = this;
				this.Children.Add( newG );
				_listSelectionState.FullName = _listSelectionState.MakeFullName( this.FullPath );
				_listSelectionState.SetNetworkHandler();
				window.AddChildWindow( _listSelectionState );
				newG.Parent = null; // remove from group so it doesn't get in the way later
				this.Children.Remove( newG );
			}
			else
			{
				// multiple selections
				BranchGroupNode selGroup = new BranchGroupNode();

				_listSelectionLengthState = new ApplianceState( _appliance, LIST_LENGTH_STATE, false );
				_listSelectionLengthState.Type = new PUCType( new IntegerSpace( new IntNumber( 0 ), new NumberConstraint( _listSelectionState, _listLengthState ) ) );
 
				this.Children.Add( selGroup );
				selGroup.Name = LIST_SELECTION_STATE;
				newG = new ObjectGroupNode( _listSelectionLengthState );
				newG.Parent = selGroup;
				selGroup.Children.Add( newG );
				newG = new ObjectGroupNode( _listSelectionState );
				newG.Parent = selGroup;
				selGroup.Children.Add( newG );

				varTable.RegisterObject( _listSelectionLengthState.MakeFullName( selGroup.FullPath ), _listSelectionLengthState );
				_listSelectionState.FullName = _listSelectionState.MakeFullName( selGroup.FullPath );
				window.AddChildWindow( _listSelectionLengthState );
				window.AddChildWindow( _listSelectionState );

				this.Children.Remove( selGroup );
			}

			varTable.RegisterObject( _listSelectionState.FullName, _listSelectionState );
		}

		public void ListChanged( VariableTable varTable, ListEventArgs e )
		{
			int listLength = (int)_listLengthState.Value;
			_defined = listLength > 0;

			if ( _dataWindow.Index <= listLength )
				_dataWindow.Update();

			if ( ListDataChanged != null )
				ListDataChanged( this, e );
		}
	}

	public class UnionGroupNode : BranchGroupNode
	{
		/*
		 * Constants
		 */

		public const string UNION_STATE = "ChildUsed";


		/*
		 * Member Variables
		 */

		protected ApplianceState _unionState;
		protected bool			 _readonly;


		/*
		 * Properties
		 */

		public bool ReadOnly
		{
			get
			{
				if ( _unionState != null )
					return _unionState.ReadOnly;
				else
					return _readonly;
			}
			set
			{
				if ( _unionState != null )
					throw new NotSupportedException( "ReadOnly properties can only be set before its implicit variables are created." );

				_readonly = value;
			}
		}


		/*
		 * Member Methods
		 */

		public override object Clone()
		{
			// instantiate cloned object
			UnionGroupNode obj = new UnionGroupNode();

			// call the cloneHelper
			cloneHelper( obj );

			// return object
			return obj;
		}

		/// <summary>
		/// This method helps in the cloning process by allowing each 
		/// class in the hierarchy to clone their own member variables.
		/// The class being cloned will create a new instance of itself,
		/// and then call the cloneHelper in its super-class to get any
		/// member variables of its super classes to be copied.
		/// </summary>
		/// <param name="g">the instance of the cloned object</param>
		protected void cloneHelper( UnionGroupNode g )
		{
			base.cloneHelper( g );

			// none of these objects should be defined yet
			System.Diagnostics.Debug.Assert( _unionState == null );

			// shallow copy these
			g._readonly = _readonly;
		}

		public override bool IsUnion()
		{
			return true;
		}

		public void CreateExtraStates( Appliance appliance, VariableTable varTable )
		{
			_unionState = new ApplianceState( appliance, UNION_STATE, _readonly );
			_unionState.Type = new PUCType( new StringSpace() );
			GroupNode newG = new ObjectGroupNode( _unionState );
			newG.Parent = this;
			this.Children.Add( newG );
			varTable.RegisterObject( _unionState.FullName, _unionState );
		}
	}

	public delegate void ListEvent( ListGroupNode listGroup, ListEventArgs a );

	public class ListEventArgs
	{
		/*
		 * Member Variables
		 */

		protected int[] _parentIndices;

		/*
		 * Constructor
		 */

		public ListEventArgs()
			: this( null )
		{
		}

		public ListEventArgs( int[] indices )
		{
			_parentIndices = indices;

			if ( _parentIndices != null && _parentIndices.Length == 0 )
				_parentIndices = null;
		}

		/*
		 * Properties
		 */

		public int[] ParentIndices
		{
			get
			{
				return _parentIndices;
			}
		}
	}

	public class RefreshAllListEventArgs : ListEventArgs
	{
		/*
		 * Constructors
		 *

		public RefreshAllListEventArgs() : base()
		{
		}*/

		public RefreshAllListEventArgs( int[] indices ) 
			: base( indices )
		{
		}
	}

	public class InsertListEventArgs : ListEventArgs
	{
		/*
			 * Member Variables
			 */

		protected int _insertAfter;
		protected int _itemCount;


		/*
		 * Constructor
		 *

		public InsertListEventArgs( int insertAfter, int itemCount )
			: base()
		{
			_insertAfter = insertAfter;
			_itemCount = itemCount;
		}*/

		public InsertListEventArgs( int[] indices, int insertAfter, int itemCount )
			: base( indices )
		{
			_insertAfter = insertAfter;
			_itemCount = itemCount;
		}


		/*
		 * Properties
		 */

		public int InsertAfter
		{
			get
			{
				return _insertAfter;
			}
			set
			{
				_insertAfter = value;
			}
		}

		public int ItemCount
		{
			get
			{
				return _itemCount;
			}
			set
			{
				_itemCount = value;
			}
		}
	}

	public class DeleteListEventArgs : ListEventArgs
	{
		/*
		 * Member Variables
		 */

		protected int _start;
		protected int _length;


		/*
		 * Constructor
		 *

		public DeleteListEventArgs( int start, int length )
			: base()
		{
			_start = start;
			_length = length;
		}
*/
		public DeleteListEventArgs( int[] indices, int start, int length )
			: base( indices )
		{
			_start = start;
			_length = length;
		}


		/*
		 * Properties
		 */

		public int Start
		{
			get
			{
				return _start;
			}
			set
			{
				_start = value;
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
	}

	public class ChangeListEventArgs : ListEventArgs
	{
		/*
		 * Member Variables
		 */

		protected int _start;
		protected int _oldLength;
		protected int _newLength;


		/*
		 * Constructor
		 *

		public ChangeListEventArgs( int start, int length )
		{
			_start = start;
			_oldLength = _newLength = length;
		}

		public ChangeListEventArgs( int start, int oldLength, int newLength )
		{
			_start = start;
			_oldLength = oldLength;
			_newLength = newLength;
		}
*/
		public ChangeListEventArgs( int[] indices, int start, int length )
			: base( indices )
		{
			_start = start;
			_oldLength = _newLength = length;
		}

		public ChangeListEventArgs( int[] indices, int start, int oldLength, int newLength )
			: base( indices )
		{
			_start = start;
			_oldLength = oldLength;
			_newLength = newLength;
		}


		/*
		 * Properties
		 */

		public int Start
		{
			get
			{
				return _start;
			}
			set
			{
				_start = value;
			}
		}

		public int OldLength
		{
			get
			{
				return _oldLength;
			}
			set
			{
				_oldLength = value;
			}
		}

		public int NewLength
		{
			get
			{
				return _newLength;
			}
			set
			{
				_newLength = value;
			}
		}
	}
}
