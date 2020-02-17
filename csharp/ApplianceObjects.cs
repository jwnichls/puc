using System;
using System.Collections;

using PUC.Communication;
using PUC.Types;


/*
 * This file contains all of the classes that define all of the 
 * ApplianceObjects.
 */

namespace PUC
{
	/// <summary>
	/// Summary description for ApplianceObject.
	/// </summary>
	public abstract class ApplianceObject : Decorator, ICloneable
	{
		/*
		 * Events
		 */

		public delegate void EnableChangedHandler( ApplianceObject o );
		public event EnableChangedHandler EnableChangedEvent;

		public delegate void LabelChangedHandler( ApplianceObject o );
		public event LabelChangedHandler LabelChangedEvent;

		
		/*
		 * Member Variables
		 */
		
		/// <summary>
		/// The name of this appliance object.
		/// </summary>
		protected string _name;

		/// <summary>
		/// The fully qualified name of this appliance object.
		/// </summary>
		protected string _fullName;

		/// <summary>
		/// The priority of this appliance object.
		/// </summary>
		protected int _priority;

		/// <summary>
		/// The enabled setting of this appliance object.
		/// </summary>
		protected bool _enabled;

		/// <summary>
		/// The labels associated with this appliance object.
		/// </summary>
		protected LabelDictionary _labels;

		/// <summary>
		/// The dependencies associated with this object.
		/// </summary>
		protected Dependency _dependencies;

		/// <summary>
		/// Defines a high-level associated with this state. (optionally specified)
		/// </summary>
		protected string _highlevelType;

		/// <summary>
		/// Defines the line number in the specification where this object starts.
		/// </summary>
		protected int _lineNumber;

		/// <summary>
		/// A reference to the global appliance object.
		/// </summary>
		protected Appliance _appliance;


		/*
		 * Constructors
		 */

		public ApplianceObject( Appliance appliance, string name, int priority )
		{
			_appliance = appliance;
			_name = name;
			_priority = priority;
			_enabled = true;
		}

		public ApplianceObject( Appliance appliance ) : this( appliance, null, -1 ) { }

		/// <summary>
		/// For cloning.
		/// </summary>
		protected ApplianceObject()
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

		public string FullName
		{
			get
			{
				return _fullName;
			}
			set
			{
				_fullName = value;
			}
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

		public string Name
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

		public Dependency Dependencies 
		{
			get
			{
				return _dependencies;
			}
			set
			{
				_dependencies = value;
			}
		}

		public bool Enabled 
		{ 
			get
			{
				return _enabled;
			}
			set
			{
				if ( value != _enabled ) 
				{
					_enabled = value;

					if ( EnableChangedEvent != null )
						EnableChangedEvent( this );
				}
			}
		}

		/// <summary>
		/// True if this appliance object represents a state variable.
		/// </summary>
		public virtual bool State 
		{
			get
			{
				return false;
			}
		}

		/// <summary>
		/// True if this appliance object represents an explanation.
		/// </summary>
		public virtual bool Explanation
		{
			get
			{
				return false;
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
		/// <param name="clone">the instance of the cloned object</param>
		protected void cloneHelper( ApplianceObject clone )
		{
			// copy the following values directly
			clone._name = _name;
			clone._priority = _priority;
			clone._enabled = _enabled;
			clone._highlevelType = _highlevelType;
			clone._lineNumber = _lineNumber;
			clone._appliance = _appliance;
			clone._fullName = _fullName;

			// TODO: do these need to be deep copied?
			clone._labels = _labels;
			clone._dependencies = _dependencies;
		}

		public string MakeFullName( string groupName )
		{
			return groupName + GroupNode.NAME_SEPARATOR + this.Name;
		}

		/// <summary>
		/// A convenience method that sets the enabled of this object to
		/// the result of evaluating its dependencies.
		/// </summary>
		public void EvalDependencies()
		{
			if ( _dependencies != null )
				Enabled = _dependencies.IsSatisfied();
		}

		/// <summary>
		/// Called by an external source to inform the object that
		/// its label has changed.  The object can then inform the 
		/// other objects that are listening.
		/// </summary>
		public void LabelChanged()
		{
			if ( LabelChangedEvent != null )
				LabelChangedEvent( this );
		}
	}

	/// <summary>
	/// Represents a command.
	/// </summary>
	public class ApplianceCommand : ApplianceObject
	{
		/*
		 * Event
		 */

		public delegate void InvocationRequestedHandler( ApplianceCommand cmd );
		public event InvocationRequestedHandler InvocationRequested;


		/*
		 * Constructors
		 */

		public ApplianceCommand( Appliance appliance ) 
			: base( appliance )
		{ 
		}

		public ApplianceCommand( Appliance appliance, string name, int priority )
			: base( appliance, name, priority )
		{
		}

		/// <summary>
		/// For cloning.
		/// </summary>
		protected ApplianceCommand()
		{
		}


		/*
		 * Member Methods
		 */

		public override object Clone()
		{
			// create the clone object
			ApplianceCommand cmd = new ApplianceCommand();

			// call the cloneHelper
			cloneHelper( cmd );

			// return the clone
			return cmd;
		}


		/// <summary>
		/// This method helps in the cloning process by allowing each 
		/// class in the hierarchy to clone their own member variables.
		/// The class being cloned will create a new instance of itself,
		/// and then call the cloneHelper in its super-class to get any
		/// member variables of its super classes to be copied.
		/// </summary>
		/// <param name="clone">the instance of the cloned object</param>
		protected void cloneHelper( ApplianceCommand clone )
		{
			base.cloneHelper( clone );

			// this may not always be appropriate...but should be given 
			// when I expect the cloning to be taking place
			clone.SetNetworkHandler();
		}

		/// <summary>
		/// Sets the default network handler for the InvocationRequested event.
		/// The handler, sendInvokeMessage, sends a CommandInvokeRequest message
		/// to the server.  This method should be called for all commands that
		/// a part of a PUC specification.
		/// </summary>
		public void SetNetworkHandler()
		{
			this.InvocationRequested += new InvocationRequestedHandler(this.sendInvokeMessage);
		}

		/// <summary>
		/// The default handler used for the InvocationRequested event.  This 
		/// handler sends a CommandInvokeRequest message to the server.
		/// </summary>
		/// <param name="cmd"></param>
		protected void sendInvokeMessage( ApplianceCommand cmd )
		{
			try
			{
				_appliance.GetConnection().Send( new CommandInvokeRequest( cmd.FullName ) );
			}
			catch( Exception )
			{
			}
		}

		/// <summary>
		/// Called by the control that is rendered to represent this command.
		/// </summary>
		public void InvokeCommand()
		{
			if ( InvocationRequested != null )
				InvocationRequested( this );
		}
	}


	public class ApplianceState : ApplianceObject, IDataWindow
	{
		/*
		 * Events
		 */

		public delegate void TypeChangedHandler( ApplianceState s );
		public virtual event TypeChangedHandler TypeChangedEvent;

		public delegate void ValueChangedHandler( ApplianceState s );
		public virtual event ValueChangedHandler ValueChangedEvent;

		public delegate void ValueChangeRequestedHandler( ApplianceState s, object value );
		public virtual event ValueChangeRequestedHandler ValueChangeRequested;

		
		/*
		 * Member Variables
		 */
		protected bool				_readonly;
		protected bool				_constraintVariable;

		protected bool				_defined;
		protected VariableTable		_varTable;
		protected PUCType			_type;

		protected bool				_internalController;

		protected ArrayList			_reverseDeps;

		protected IPUCValue			_defaultValue;

		protected Dependency		_requiredIf;


		/*
		 * IDataWindow Member Variables
		 */

		protected string[]			_windowNames;
		protected object			_windowRef;
		protected IBranchDataWindow	_windowParent;
		
		
		/*
		 * Constructors
		 */
		public ApplianceState( Appliance appliance, string name, bool nowrite ) 
			: this( appliance, name, -1, nowrite )
		{ 
		}

		public ApplianceState( Appliance appliance, string name, int priority, bool nowrite )
			: base( appliance, name, priority )
		{
			_readonly = nowrite;

			_reverseDeps = new ArrayList();

			_internalController = false;
			_constraintVariable = false;
			_defined = false;
		}

		/// <summary>
		/// For cloning.
		/// </summary>
		protected ApplianceState()
		{
		}


		/*
		 * Properties
		 */

		public void SetNetworkHandler()
		{
			ValueChangeRequested += new ValueChangeRequestedHandler(this.sendNetworkChangeRequest);
		}

		protected void sendNetworkChangeRequest( ApplianceState state, object value )
		{
			try
			{
				PUCData data = 
					(PUCData)_windowParent.AssemblePUCData( new ValueData( this.DataWindow, value.ToString() ) );
	
				_appliance.GetConnection().Send( new PUC.Communication.StateChangeRequest( data ) );
			}
			catch( Exception )
			{
			}
		}

		public void RequestChange( object value )
		{
			if ( ValueChangeRequested != null )
				ValueChangeRequested( this, value );
		}

		public virtual int AverageStringValueWidth
		{
			get
			{
				switch( _type.ValueSpace.Space ) 
				{
					case ValueSpace.BOOLEAN_SPACE:
						return false.ToString().Length;

					case ValueSpace.ENUMERATED_SPACE:
						IEnumerator l = _type.ValueLabels.Values.GetEnumerator();
						int len = 0;
						while( l.MoveNext() )
						{
							LabelDictionary labels = (LabelDictionary)l.Current;
							len = (int)Math.Max( len, labels.GetShortestLabel().Length );
						}
						return len;

					case ValueSpace.FIXED_PT_SPACE:
						FixedPtSpace f = (FixedPtSpace)_type.ValueSpace;
						if ( f.IsRanged() )
							return (int)Math.Max( f.GetMinimum().GetDoubleValue().ToString().Length,
												  f.GetMaximum().GetDoubleValue().ToString().Length );
						else
							return -1;

					case ValueSpace.FLOATING_PT_SPACE:
						FloatingPtSpace p = (FloatingPtSpace)_type.ValueSpace;
						if ( p.IsRanged() )
							return (int)Math.Max( p.GetMinimum().GetDoubleValue().ToString().Length,
								                  p.GetMaximum().GetDoubleValue().ToString().Length );
						else
							return Double.MaxValue.ToString().Length;

					case ValueSpace.INTEGER_SPACE:
						IntegerSpace i = (IntegerSpace)_type.ValueSpace;
						if ( i.IsRanged() )
							return (int)Math.Max( i.GetMinimum().GetIntValue().ToString().Length,
												  i.GetMaximum().GetIntValue().ToString().Length );
						else
							return Int32.MaxValue.ToString().Length;

					case ValueSpace.STRING_SPACE:
						StringSpace s = (StringSpace)_type.ValueSpace;
						if ( s.AverageChars != null )
							return s.AverageChars.GetIntValue();
						else if ( s.MaximumChars != null )
							return s.MaximumChars.GetIntValue();
						else
							return -1;

					default:
						return -1;
				}
			}
		}

		public virtual VariableTable VariableTable
		{
			get
			{
				return _varTable;
			}
			set
			{
				_varTable = value;
			}
		}

		public Dependency RequiredIf
		{
			get
			{
				return _requiredIf;
			}
			set
			{
				if ( value == null )
					_requiredIf = value;
				else
					_requiredIf = value.Simplify();
			}
		}
		
		public virtual PUCType Type
		{
			get
			{
				return _type;
			}
			set
			{
				_type = value;
			}
		}

		public override bool State
		{
			get
			{
				return true;
			}
		}

		public virtual bool ReadOnly
		{
			get
			{
				return _readonly;
			}
			set
			{
				_readonly = value;
			}
		}

		public virtual bool ConstraintVariable
		{
			get
			{
				return _constraintVariable;
			}
		}

		public virtual bool InternalController
		{
			get
			{
				return _internalController;
			}
			set
			{
				_internalController = value;
			}
		}

		public virtual bool Defined
		{
			get
			{
				return _defined;
			}
		}

		public virtual object Value
		{
			get
			{
				if ( _windowRef == null )
					return null;

				if ( _windowRef is ArrayList )
					// -1 because list indices are 1-indexed
					return ((ArrayList)_windowRef)[ ((IndexedDataWindow)_windowParent).Index-1 ];

				return ((Hashtable)_windowRef)[ _windowNames[ _windowNames.Length-1 ] ];
			}
		}

		public IPUCValue DefaultValue
		{
			get
			{
				return _defaultValue;
			}
			set
			{
				_defaultValue = value;
			}
		}


		/*
		 * Member Methods
		 */

		public override object Clone()
		{
			// create the clone object
			ApplianceState state = new ApplianceState();

			// call the cloneHelper
			cloneHelper( state );

			// return the clone
			return state;
		}

		/// <summary>
		/// This method helps in the cloning process by allowing each 
		/// class in the hierarchy to clone their own member variables.
		/// The class being cloned will create a new instance of itself,
		/// and then call the cloneHelper in its super-class to get any
		/// member variables of its super classes to be copied.
		/// </summary>
		/// <param name="clone">the instance of the cloned object</param>
		protected void cloneHelper( ApplianceState clone )
		{
			base.cloneHelper( clone );

			// this may not always be appropriate...but should be given 
			// when I expect the cloning to be taking place
			clone.SetNetworkHandler();

			// these events should not have any handlers yet
			System.Diagnostics.Debug.Assert( TypeChangedEvent == null );
			System.Diagnostics.Debug.Assert( ValueChangedEvent == null );

			// this shouldn't contain anything yet
			// assuming that it doesn't, we don't need to copy it
			System.Diagnostics.Debug.Assert( _reverseDeps.Count == 0 );

			// these items shouldn't be defined yet
			System.Diagnostics.Debug.Assert( _windowNames == null );
			System.Diagnostics.Debug.Assert( _windowRef == null );
			System.Diagnostics.Debug.Assert( _windowParent == null );

			// shallow copy these items
			clone._readonly = _readonly;
			clone._constraintVariable = _constraintVariable;
			clone._defined = _defined;
			clone._varTable = _varTable;
			clone._type = _type;
			clone._internalController = _internalController;
		}

		public virtual void MakeConstraintVariable()
		{
			_constraintVariable = true;
		}

		public virtual void AddReverseDependency( ApplianceObject o ) 
		{
			if ( !_reverseDeps.Contains( o ) )
				_reverseDeps.Add( o );
		}

		public virtual int GetReverseDependencyCount()
		{
			return _reverseDeps.Count;
		}

		public virtual ArrayList GetReverseDeps()
		{
			return _reverseDeps;
		}

		public virtual void Undefine()
		{
			_defined = false;

			if ( TypeChangedEvent != null )
				TypeChangedEvent( this );

			if ( ValueChangedEvent != null )
				ValueChangedEvent( this );
		}

		public virtual void TypeChanged()
		{
			if ( TypeChangedEvent != null )
				TypeChangedEvent( this );
		}

		public virtual void ValueChanged()
		{
			if ( !this.Valid && !(this.Type.ValueSpace is BinarySpace) )
				updateParentsIfNecessary();

			bool definedChanged = !_defined;
			_defined = true;

			if ( definedChanged && TypeChangedEvent != null )
				TypeChangedEvent( this );

			if ( ValueChangedEvent != null )
				ValueChangedEvent( this );
		}


		/*
		 * IDataWindow Properties and Methods
		 */

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
				string relName = _fullName.Substring( _windowParent.TopPath.Length + 1 );
				_windowNames = relName.Split( VariableTable.NAME_SEPARATORS );
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
					_windowRef = _windowParent.Reference;

					// length-1 is to ensure we get a pointer to data, rather than data
					for( int i = 0; i < _windowNames.Length-1; i++ )
						_windowRef = ((Hashtable)_windowRef)[ _windowNames[ i ] ];
				}

				// ensure we have a valid value
				if ( this.Type.ValueSpace is BinarySpace || this.Value != null )
					// to make sure CIOs update when parent DataWindows move
					ValueChanged();
			}
			catch( Exception )
			{
				_windowRef = null;
			}
		}
	}

	public class ApplianceExplanation : ApplianceObject
	{
		/*
		 * Constructors
		 */

		public ApplianceExplanation( Appliance appliance )
			: base( appliance )
		{ 
		}

		public ApplianceExplanation( Appliance appliance, string name, int priority )
			: base( appliance, name, priority )
		{
		}

		/// <summary>
		/// For cloning.
		/// </summary>
		protected ApplianceExplanation()
		{
		}


		/*
		 * Overridden Member Methods
		 */

		public override object Clone()
		{
			// create the clone object
			ApplianceExplanation expl = new ApplianceExplanation();

			// call the cloneHelper
			cloneHelper( expl );

			// return the clone
			return expl;
		}

		/// <summary>
		/// This method helps in the cloning process by allowing each 
		/// class in the hierarchy to clone their own member variables.
		/// The class being cloned will create a new instance of itself,
		/// and then call the cloneHelper in its super-class to get any
		/// member variables of its super classes to be copied.
		/// </summary>
		/// <param name="clone">the instance of the cloned object</param>
		protected void cloneHelper( ApplianceExplanation clone )
		{
			base.cloneHelper( clone );
		}
		
		public override bool Explanation
		{
			get
			{
				return true;
			}
		}
	}
}
