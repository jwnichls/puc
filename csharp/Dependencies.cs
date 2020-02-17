using System;
using System.Collections;
using PUC.Types;

/*
 * This file contains the classes necessary for describing dependencies.
 * Included are the classes for describing dependency equations and the
 * logical relations that group them together (AND, OR, etc.)
 */

namespace PUC
{
	/// <summary>
	/// The base class of all dependencies.
	/// </summary>
	public abstract class Dependency 
	{
		/*
		 * Constants
		 */

		public enum Comparison { Disjoint, Overlap, Same };


		/*
		 * Member Variables
		 */

		protected int _lineNumber;

		
		/*
		 * Constructor
		 */
	
		public Dependency( int lineNumber )
		{
			_lineNumber = lineNumber;
		}

		public Dependency()
		{
			_lineNumber = -1;
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


		/*
		 * Member Methods
		 */

		public virtual bool IsFormula()
		{
			return false;
		}

		public virtual bool HasState()
		{
			return false;
		}

		public virtual bool HasValue()
		{
			return false;
		}


		/*
		 * Abstract Methods
		 */

		public abstract bool IsSatisfied();

		public abstract Dependency Simplify();

		public abstract Dependency GetOppositeDependencies();
	}

	/// <summary>
	/// Represents a logic formula that involves Dependency objects.  One
	/// list is stored internally for storing Dependency objects.
	/// </summary>
	public abstract class Formula : Dependency
	{
		/*
		 * Member Variables
		 */

		protected ArrayList _dependencies;


		/*
		 * Constructor
		 */

		public Formula( int lineNumber )
			: base( lineNumber )
		{
			_dependencies = new ArrayList();
		}

		public Formula()
		{
			_dependencies = new ArrayList();
		}


		/*
		 * Indexor
		 */
	
		public Dependency this[ int index ]
		{
			get
			{
				return (Dependency)_dependencies[ index ];
			}
		}


		/*
		 * Properties
		 */

		public int Count
		{
			get
			{
				return _dependencies.Count;
			}
		}


		/*
		 * Member Methods
		 */

		public IEnumerator GetDependencies()
		{
			return _dependencies.GetEnumerator();
		}

		public virtual void AddDependency( Dependency d )
		{
			if ( d != null )
				_dependencies.Add( d );
		}

		public bool IsEmpty()
		{
			return Count == 0;
		}


		/*
		 * Overridden Methods
		 */

		public override bool IsFormula()
		{
			return true;
		}
	}

	/// <summary>
	/// The base class of any dependency on a state variable.
	/// </summary>
	public abstract class StateDependency : Dependency, IResolvable
	{
		/*
		 * Member Variables
		 */

		protected string			_stateName;
		
		protected ValueDataWindow	_state;
		protected bool				_valid;


		/*
		 * Constructor
		 */

		public StateDependency( string stateName, int lineNumber )
			: base( lineNumber )
		{
			_stateName = stateName;
			_valid = false;
		}

		public StateDependency( ApplianceState state )
		{
			_state = new ValueDataWindow( state );
			_stateName = state.FullName;
			_valid = true;
		}


		/*
		 * Properties
		 */

		public string StateName
		{
			get
			{
				return _stateName;
			}
		}

		public ApplianceState State
		{
			get
			{
				return _state.State;
			}
		}

		public ValueDataWindow StateWindow
		{
			get
			{
				return _state;
			}
		}


		/*
		 * Member Methods
		 */

		public override bool HasState()
		{
			return true;
		}

		public override Dependency Simplify()
		{
			return this;
		}

		#region IResolvable Members

		public virtual bool ResolveObject(VariableTable varTable)
		{
			ApplianceState stateVar = (ApplianceState)varTable[ _stateName ];
			_valid = stateVar != null;

			if ( _valid )
				_state = new ValueDataWindow( stateVar );

			return _valid;
		}

		public virtual bool Valid
		{
			get
			{
				return _valid;
			}
		}

		#endregion

		/*
		 * Abstract Methods
		 */

		public abstract Dependency.Comparison Compare( StateDependency d );
		public abstract StateDependency Merge( StateDependency d );

		public abstract Dependency.Comparison CompareEquals( EqualsDependency d );
		public abstract Dependency.Comparison CompareGreaterThan( GreaterThanDependency d );
		public abstract Dependency.Comparison CompareLessThan( LessThanDependency d );
	}

	public abstract class StateValueDependency : StateDependency
	{
		/*
		 * Member Variables
		 */

		protected string			_stringValue;
		protected bool				_isRef;

		protected IPUCValue			_value;


		/*
		 * Constructor
		 */

		public StateValueDependency( string stateName, string value, bool isRef, int lineNumber )
			: base( stateName, lineNumber )
		{
			_stringValue = value;
			_isRef = isRef;
		}

		public StateValueDependency( ApplianceState state, string value )
			: base( state )
		{
			_stringValue = value;
			_isRef = false;

			try
			{
				_valid = false;
				_value = new PUCValue( State.Type.ValueSpace.Validate( _stringValue ) );
			}
			catch( Exception )
			{
				throw new PUC.Parsers.SpecParseException( 0, "Value does not match value space of state " + State.Name );
			}

			_valid = true;
		}

		public StateValueDependency( ApplianceState state, ApplianceState refState )
			: base( state )
		{
			_stringValue = refState.FullName;
			_isRef = true;
			_valid = false;

			if ( !state.Type.ValueSpace.IsSameSpace( refState.Type.ValueSpace ) )
				throw new PUC.Parsers.SpecParseException( 0, "Value space of state " + state.FullName + " does not match space of state " + refState.FullName );

			_value = new ValueConstraint( refState );

			_valid = true;
		}


		/*
		 * Properties
		 */

		public virtual bool IsReference
		{
			get
			{
				return _isRef;
			}
		}

		public IDataWindow ReferenceWindow
		{
			get
			{
				if ( !_isRef )
					return null;

				return ((ValueConstraint)_value).StateWindow;
			}
		}

		public object Value
		{
			get
			{
				return _value.Value;
			}
		}
	
		public ApplianceState ReferenceState
		{
			get
			{
				if ( !_isRef )
					return null;

				return ((ValueConstraint)_value).State;
			}
		}
		
		public string ValueString
		{
			get
			{
				return _stringValue;
			}
		}

		
		/*
		 * Member Methods
		 */

		public override bool HasValue()
		{
			return true;
		}

		public override bool ResolveObject(VariableTable varTable)
		{
			if ( !base.ResolveObject (varTable) )
				return false;

			if ( _isRef )
			{
				ApplianceState stateVar = (ApplianceState)varTable[ _stringValue ];
				_valid = stateVar != null;

				if ( _valid )
					_value = new ValueConstraint( stateVar );
			}
			else
			{
				try
				{
					_value = new PUCValue( State.Type.ValueSpace.Validate( _stringValue ) );
				}
				catch( Exception )
				{
					_valid = false;
					throw new PUC.Parsers.SpecParseException( this.LineNumber, "String value does not match the value space of state " + State.Name );
				}
			}

			return _valid;
		}
	}

	/****************************************************************
	 * Non-State Dependencies
	 ****************************************************************/

	public class TrueDependency : Dependency
	{
		/*
		 * Member Methods
		 */

		public override bool IsSatisfied()
		{
			return true;
		}

		public override Dependency Simplify()
		{
			return this;
		}

		public override Dependency GetOppositeDependencies()
		{
			return new FalseDependency();
		}
	}

	public class FalseDependency : Dependency
	{
		/*
		 * Member Methods
		 */

		public override bool IsSatisfied()
		{
			return true;
		}

		public override Dependency Simplify()
		{
			return this;
		}

		public override Dependency GetOppositeDependencies()
		{
			return new TrueDependency();
		}
	}


	/****************************************************************
	 * Dependency Formulas
	 ****************************************************************/

	public class AND : Formula
	{
		/*
		 * Member Methods
		 */

		public override Dependency Simplify()
		{
			if ( IsEmpty() ) return null;

			AND newF = new AND();

			IEnumerator e = GetDependencies();

			while( e.MoveNext() )
			{
				Dependency d = ((Dependency)e.Current).Simplify();

				if ( d is FalseDependency )
					return (Dependency)e.Current;
				else if ( d.IsFormula() )
				{
					Formula df = (Formula)d;

					if ( d is AND )
					{
						// collapse nested AND formulas into each other
						IEnumerator e2 = df.GetDependencies();
						while( e2.MoveNext() )
							newF.AddDependency( (Dependency)e2.Current );
					}
					else if ( d != null )
						newF.AddDependency( d );
				}
				else if ( !(d is TrueDependency) )
					// remove TrueDependencies from within an AND
					newF.AddDependency( (Dependency)e.Current );
			}

			if ( newF.Count == 1 )
				return newF[ 0 ];
			else if ( newF.Count == 0 )
				return null;
			else
				return newF;
		}

		public override bool IsSatisfied()
		{
			bool total = true;

			IEnumerator e = GetDependencies();
			while( total && e.MoveNext() )
				total = total && ((Dependency)e.Current).IsSatisfied();

			return total;
		}

		public override Dependency GetOppositeDependencies()
		{
			OR newF = new OR();

			IEnumerator e = GetDependencies();
			while( e.MoveNext() )
				newF.AddDependency( ((Dependency)e.Current).GetOppositeDependencies() );

			return newF;
		}
	}

	public class OR : Formula
	{
		/*
		 * Member Methods
		 */

		public override Dependency Simplify()
		{
			if ( IsEmpty() ) return null;

			OR newF = new OR();

			IEnumerator e = GetDependencies();

			while( e.MoveNext() )
			{
				Dependency d = ((Dependency)e.Current).Simplify();

				if ( d is TrueDependency )
					return (Dependency)e.Current;
				else if ( d.IsFormula() )
				{
					Formula df = (Formula)d;

					if ( d is OR )
					{
						// collapse nested OR formulas into each other
						IEnumerator e2 = df.GetDependencies();
						while( e2.MoveNext() )
							newF.AddDependency( (Dependency)e2.Current );
					}
					else if ( d != null )
						newF.AddDependency( d );
				}
				else if ( !(d is FalseDependency) )
					// remove FalseDependencies from within an OR
					newF.AddDependency( (Dependency)e.Current );
			}

			if ( newF.Count == 1 )
				return newF[ 0 ];
			else if ( newF.Count == 0 )
				return null;
			else
				return newF;
		}

		public override bool IsSatisfied()
		{
			bool total = false;

			IEnumerator e = GetDependencies();
			while( !total && e.MoveNext() )
				total = total || ((Dependency)e.Current).IsSatisfied();

			return total;
		}

		public override Dependency GetOppositeDependencies()
		{
			AND newF = new AND();

			IEnumerator e = GetDependencies();
			while( e.MoveNext() )
				newF.AddDependency( ((Dependency)e.Current).GetOppositeDependencies() );

			return newF;
		}

	}

	public class NOT : Formula
	{
		/*
		 * Member Methods
		 */

		public override void AddDependency(Dependency d)
		{
			System.Diagnostics.Debug.Assert( Count == 0 );

			base.AddDependency (d);
		}

		public override Dependency Simplify()
		{
			return this[ 0 ].Simplify().GetOppositeDependencies();
		}

		public override bool IsSatisfied()
		{
			return !this[ 0 ].IsSatisfied();
		}

		public override Dependency GetOppositeDependencies()
		{
			return this[ 0 ];
		}

	}

	public class ApplyOver : Formula, IResolvable
	{
		/*
		 * Constants
		 */

		public enum ApplyType { Any, All, None };


		/*
		 * Member Variables
		 */

		protected ApplyType			_trueIf;
		protected string			_listGroupName;
		protected ListGroupNode		_listGroup;
		protected bool				_valid;

		protected IndexedDataWindow _indexWindow;


		/*
		 * Constructors
		 */

		public ApplyOver( ListGroupNode list, ApplyType type )
		{
			_trueIf = type;
			_listGroup = list;
			_listGroupName = list.FullPath;

			_indexWindow = new IndexedDataWindow( _listGroup.DataWindow );
			_valid = true;
		}

		public ApplyOver( string listName, int lineNumber, ApplyType type )
			: base( lineNumber )
		{
			_listGroupName = listName;
			_trueIf = type;

			_valid = false;
		}


		/*
		 * Properties
		 */

		public ApplyType TrueIf
		{
			get
			{
				return _trueIf;
			}
		}

		public ListGroupNode List
		{
			get
			{
				return _listGroup;
			}
		}

		public string ListName
		{
			get
			{
				return _listGroupName;
			}
		}


		/*
		 * Member Methods
		 */

		public override void AddDependency(Dependency d)
		{
			base.AddDependency (d);

			if ( _valid )
				changeIndexWindow( d );
		}

		protected void changeIndexWindow( Dependency d )
		{
			try
			{
				if ( d.IsFormula() )
				{
					IEnumerator e = ((Formula)d).GetDependencies();
					while( e.MoveNext() )
						changeIndexWindow( (Dependency)e.Current );
				}
				else if ( d is StateDependency )
				{
					StateDependency sd = (StateDependency)d;

					changeIndexWindow( sd.StateWindow );

					if ( sd is StateValueDependency && ((StateValueDependency)sd).IsReference )
						changeIndexWindow( ((StateValueDependency)sd).ReferenceWindow );
				}
			}
			catch( NullReferenceException e )
			{
				throw new PUC.Parsers.SpecParseException( d.LineNumber, "A dependency within an apply-over block does not refer to a variable within the referenced list group.", e );
			}
		}

		protected void changeIndexWindow( IDataWindow dwin )
		{
			if ( dwin.Parent is IndexedDataWindow )
			{
				IndexedDataWindow iwin = (IndexedDataWindow)dwin.Parent;
				
				if ( iwin.ListGroup == _listGroup )
				{
					dwin.Parent.RemoveChildWindow( dwin );
					_indexWindow.AddChildWindow( dwin );
				}
			}
			else
				changeIndexWindow( dwin.Parent );
		}

		public void FixDataWindows()
		{
			changeIndexWindow( this );
		}

		public override bool IsSatisfied()
		{
			if ( !_valid || !_listGroup.Defined )
				return false;

			// this will store the number of items in the list that 
			// return true for these dependencies.  It is used for 
			// determining whether the all or none conditions are met.
			int trueCount = 0;

			int length = (int)_listGroup.LengthState.Value;
			for( int i = 0; i < length; i++ )
			{
				_indexWindow.Index = i+1; // +1 to match PUC list indices

				bool total = true;

				IEnumerator e = GetDependencies();
				while( total && e.MoveNext() )
					total = total && ((Dependency)e.Current).IsSatisfied();

				if ( total )
				{
					trueCount++;

					if ( _trueIf == ApplyType.Any )
						return true;
					else if ( _trueIf == ApplyType.None )
						return false;
				}
			}

			if ( trueCount == 0 )
				return _trueIf == ApplyType.None;
			else
				return _trueIf == ApplyType.All && trueCount == length;
		}

		public override Dependency Simplify()
		{
			if ( IsEmpty() ) return null;

			ApplyOver newF = null;
			if ( _valid )
			{
				newF = new ApplyOver( _listGroup, _trueIf );
				newF._lineNumber = _lineNumber;
			}
			else
			{
				newF = new ApplyOver( _listGroupName, _lineNumber, _trueIf );
			}

			IEnumerator e = GetDependencies();

			while( e.MoveNext() )
			{
				Dependency d = ((Dependency)e.Current).Simplify();

				if ( d is FalseDependency )
					return (Dependency)e.Current;
				else if ( d.IsFormula() )
				{
					Formula df = (Formula)d;

					if ( d is AND )
					{
						// collapse nested AND formulas into each other
						IEnumerator e2 = df.GetDependencies();
						while( e2.MoveNext() )
							newF.AddDependency( (Dependency)e2.Current );
					}
					else if ( d != null )
						newF.AddDependency( d );
				}
				else if ( !(d is TrueDependency) )
					// remove TrueDependencies from within an AND
					newF.AddDependency( (Dependency)e.Current );
			}

			if ( newF.Count == 0 )
				return null;
			else
				return newF;
		}

		public override Dependency GetOppositeDependencies()
		{
			ApplyOver newF = null;
			if ( _valid )
			{
				newF = new ApplyOver( _listGroup, _trueIf );
				newF._lineNumber = _lineNumber;
			}
			else
			{
				newF = new ApplyOver( _listGroupName, _lineNumber, _trueIf );
			}

			switch( _trueIf )
			{
				case ApplyType.Any:
					newF._trueIf = ApplyType.None;
					break;

				case ApplyType.None:
					newF._trueIf = ApplyType.Any;
					break;

				case ApplyType.All:
					newF._trueIf = ApplyType.Any;
					// also negate formulas below
					break;
			}

			IEnumerator e = GetDependencies();
			while( e.MoveNext() )
			{
				if ( _trueIf == ApplyType.All )
					newF.AddDependency( ((Dependency)e.Current).GetOppositeDependencies() );
				else
					newF.AddDependency( (Dependency)e.Current );
			}

			return newF;
		}

		#region IResolvable Members

		public bool ResolveObject(VariableTable varTable)
		{
			_listGroup = varTable.GetListGroup( _listGroupName );

			if ( _listGroup == null )
				throw new PUC.Parsers.SpecParseException( _lineNumber, "List group references by " + PUC.Parsers.SpecParser21.APPLY_OVER_TAG + " element could not be found: " + _listGroupName );

			_valid = true;
			_indexWindow = new IndexedDataWindow( _listGroup.DataWindow );

			return _valid;
		}

		public bool Valid
		{
			get
			{
				return _valid;
			}
		}

		#endregion
	}


	/****************************************************************
	 * State Dependency Objects
	 ****************************************************************/

	public class WholeSetDependency : StateDependency
	{
		/*
		 * Constructors
		 */

		public WholeSetDependency( ApplianceState state )
			: base( state )
		{
		}

		public WholeSetDependency( string stateName, int lineNumber )
			: base( stateName, lineNumber )
		{
		}


		/*
		 * Member Methods
		 */

		public override bool IsSatisfied()
		{
			return State.Defined;
		}

		public override Dependency.Comparison Compare(StateDependency d)
		{
			if ( d.State != State ||
				 d is UndefinedDependency )
				return Dependency.Comparison.Disjoint;

			if ( d is WholeSetDependency )
				return Dependency.Comparison.Same;

			return Dependency.Comparison.Overlap;
		}

		public override StateDependency Merge(StateDependency d)
		{
			if ( d.Compare( this ) == Dependency.Comparison.Disjoint )
				return null;
			else
				return this;
		}

		/*
		 * Supposedly Protected Member Methods
		 */

		public override PUC.Dependency.Comparison CompareEquals(EqualsDependency d)
		{
			System.Diagnostics.Debug.Assert( d.State == State );

			return Dependency.Comparison.Overlap;
		}

		public override PUC.Dependency.Comparison CompareGreaterThan(GreaterThanDependency d)
		{
			System.Diagnostics.Debug.Assert( d.State == State );

			return Dependency.Comparison.Overlap;
		}

		public override PUC.Dependency.Comparison CompareLessThan(LessThanDependency d)
		{
			System.Diagnostics.Debug.Assert( d.State == State );

			return Dependency.Comparison.Overlap;
		}

		public override Dependency GetOppositeDependencies()
		{
			return new UndefinedDependency( this.State );
		}
	}

	public class UndefinedDependency : StateDependency
	{
		/*
		 * Constructors
		 */

		public UndefinedDependency( ApplianceState state )
			: base( state )
		{
		}

		public UndefinedDependency( string stateName, int lineNumber )
			: base( stateName, lineNumber )
		{
		}

		
		/*
		 * Member Methods
		 */

		public override PUC.Dependency.Comparison Compare(StateDependency d)
		{
			if ( d.State == State && d is UndefinedDependency )
				return Dependency.Comparison.Same;

			return Dependency.Comparison.Disjoint;
		}

		public override StateDependency Merge(StateDependency d)
		{
			if ( d.Compare( this ) == Dependency.Comparison.Same )
				return this;
			else
				return null;
		}

		public override bool IsSatisfied()
		{
			return !State.Defined;
		}


		/*
		 * Supposedly Protected Methods
		 */

		public override PUC.Dependency.Comparison CompareEquals(EqualsDependency d)
		{
			return Dependency.Comparison.Disjoint;
		}

		public override PUC.Dependency.Comparison CompareGreaterThan(GreaterThanDependency d)
		{
			return Dependency.Comparison.Disjoint;
		}

		public override PUC.Dependency.Comparison CompareLessThan(LessThanDependency d)
		{
			return Dependency.Comparison.Disjoint;
		}

		public override Dependency GetOppositeDependencies()
		{
			return new WholeSetDependency( this.State );
		}
	}

	public class EqualsDependency : StateValueDependency
	{
		/*
		 * Constructors
		 */

		public EqualsDependency( ApplianceState state, string value )
			: base( state, value )
		{
		}

		public EqualsDependency( ApplianceState state1, ApplianceState state2 )
			: base( state1, state2 )
		{
		}

		public EqualsDependency( string stateName, string value, bool isRef, int lineNumber )
			: base( stateName, value, isRef, lineNumber )
		{
		}


		/*
		 * Member Methods
		 */

		public override PUC.Dependency.Comparison Compare(StateDependency d)
		{
			if ( d.State != State )
				return Dependency.Comparison.Disjoint;

			return d.CompareEquals( this );
		}

		public override StateDependency Merge(StateDependency d)
		{
			Dependency.Comparison r = Compare( d );

			switch( r )
			{
				case Comparison.Disjoint:
					return null;

				default:
					return d;
			}
		}

		public override bool IsSatisfied()
		{
			return State.Type.ValueSpace.CompareValues( this.Value, _state.Value ) == 0;
		}

		public override Dependency GetOppositeDependencies()
		{
			OR newF = new OR();

			if ( _valid )
			{
				if ( this.IsReference )
				{
					newF.AddDependency( new LessThanDependency( this.State, this.ReferenceState ) );
					newF.AddDependency( new GreaterThanDependency( this.State, this.ReferenceState ) );
				}
				else
				{
					newF.AddDependency( new LessThanDependency( this.State, this.ValueString ) );
					newF.AddDependency( new GreaterThanDependency( this.State, this.ValueString ) );
				}
				newF.AddDependency( new UndefinedDependency( this.State ) );
			}
			else
			{
				newF.AddDependency( new LessThanDependency( this.StateName, this.ValueString, this.IsReference, this.LineNumber ) );
				newF.AddDependency( new GreaterThanDependency( this.StateName, this.ValueString, this.IsReference, this.LineNumber ) );
				newF.AddDependency( new UndefinedDependency( this.StateName, this.LineNumber ) );
			}

			return newF;
		}


		/*
		 * Supposedly Protected Members
		 */

		public override PUC.Dependency.Comparison CompareEquals(EqualsDependency d)
		{
			if ( _isRef != d.IsReference )
				return Comparison.Disjoint;

			if ( _isRef )
			{
				if ( this.ReferenceState == d.ReferenceState )
					return Comparison.Same;
				else
					return Comparison.Disjoint;
			}
			else
			{
				if ( State.Type.ValueSpace.CompareValues( this.Value, d.Value ) == 0 )
					return Comparison.Same;
				else
					return Comparison.Disjoint;
			}
		}

		public override PUC.Dependency.Comparison CompareGreaterThan(GreaterThanDependency d)
		{
			if ( _isRef || d.IsReference )
				return Comparison.Disjoint;

			if ( State.Type.ValueSpace.CompareValues( this.Value, d.Value ) == 1 )
				return Comparison.Overlap;
			else
				return Comparison.Disjoint;
		}

		public override PUC.Dependency.Comparison CompareLessThan(LessThanDependency d)
		{
			if ( _isRef || d.IsReference )
				return Comparison.Disjoint;

			if ( State.Type.ValueSpace.CompareValues( this.Value, d.Value ) == -1 )
				return Comparison.Overlap;
			else
				return Comparison.Disjoint;
		}
	}

	public class GreaterThanDependency : StateValueDependency
	{
		/*
		 * Constructors
		 */

		public GreaterThanDependency( ApplianceState state, string value )
			: base( state, value )
		{
		}

		public GreaterThanDependency( ApplianceState state1, ApplianceState state2 )
			: base( state1, state2 )
		{
		}

		public GreaterThanDependency( string stateName, string value, bool isRef, int lineNumber )
			: base( stateName, value, isRef, lineNumber )
		{
		}


		/* 
		 * Member Methods
		 */

		public override PUC.Dependency.Comparison Compare(StateDependency d)
		{
			if ( d.State != this.State )
				return Comparison.Disjoint;

			return d.CompareGreaterThan( this );
		}

		public override StateDependency Merge(StateDependency d)
		{
			Comparison r = Compare( d );

			switch( r )
			{
				case Comparison.Same:
					return d;

				case Comparison.Overlap:
					if ( d is EqualsDependency )
						return this;
					else if ( d is LessThanDependency )
						return new WholeSetDependency( State );
					else if ( d is WholeSetDependency )
						return d;
					else if ( d is GreaterThanDependency )
					{
						if ( State.Type.ValueSpace.CompareValues( ((GreaterThanDependency)d).Value, this.Value ) == 1 )
							return d;
						else
							return this;
					}
					else 
						return null;

				default:
					return null;
			}
		}

		public override bool IsSatisfied()
		{
			return State.Type.ValueSpace.CompareValues( _state.Value, this.Value ) == 1;
		}

		public override bool ResolveObject(VariableTable varTable)
		{
			bool r = base.ResolveObject (varTable);

			if ( r && !(State.Type.ValueSpace is NumberSpace) )
				throw new NotSupportedException( "GreaterThanDependencies must be associated with numeric ValueSpaces!" );

			return r;
		}

		public override Dependency GetOppositeDependencies()
		{
			OR newF = new OR();

			if ( _valid )
			{
				if ( IsReference )
				{
					newF.AddDependency( new EqualsDependency( this.State, this.ReferenceState ) );
					newF.AddDependency( new LessThanDependency( this.State, this.ReferenceState ) );
				}
				else
				{
					newF.AddDependency( new EqualsDependency( this.State, this.ValueString ) );
					newF.AddDependency( new LessThanDependency( this.State, this.ValueString ) );
				}
				newF.AddDependency( new UndefinedDependency( this.State ) );
			}
			else
			{
				newF.AddDependency( new EqualsDependency( this.StateName, this.ValueString, this.IsReference, this.LineNumber ) );
				newF.AddDependency( new LessThanDependency( this.StateName, this.ValueString, this.IsReference, this.LineNumber ) );
				newF.AddDependency( new UndefinedDependency( this.StateName, this.LineNumber ) );
			}

			return newF;
		}


		/*
		 * Supposedly Protected Member Methods
		 */

		public override PUC.Dependency.Comparison CompareEquals(EqualsDependency d)
		{
			return d.CompareGreaterThan( this );
		}

		public override PUC.Dependency.Comparison CompareGreaterThan(GreaterThanDependency d)
		{
			if ( _isRef || d.IsReference )
				return Comparison.Disjoint;

			if ( State.Type.ValueSpace.CompareValues( d.Value, this.Value ) == 0 )
				return Comparison.Same;

			return Comparison.Overlap;
		}

		public override PUC.Dependency.Comparison CompareLessThan(LessThanDependency d)
		{
			if ( _isRef || d.IsReference )
				return Comparison.Disjoint;

			if ( State.Type.ValueSpace.CompareValues( d.Value, this.Value ) == -1 )
				return Comparison.Overlap;

			return Comparison.Disjoint;
		}
	}

	public class LessThanDependency : StateValueDependency
	{
		/* 
		 * Constructors
		 */

		public LessThanDependency( ApplianceState state, string value )
			: base( state, value )
		{
		}

		public LessThanDependency( ApplianceState state1, ApplianceState state2 )
			: base( state1, state2 )
		{
		}

		public LessThanDependency( string stateName, string value, bool isRef, int lineNumber )
			: base( stateName, value, isRef, lineNumber )
		{
		}


		/*
		 * Member Methods
		 */

		public override PUC.Dependency.Comparison Compare(StateDependency d)
		{
			if ( d.State != State )
				return Comparison.Disjoint;

			return d.CompareLessThan( this );
		}

		public override StateDependency Merge(StateDependency d)
		{
			Comparison r = Compare( d );

			switch( r )
			{
				case Comparison.Same:
					return d;

				case Comparison.Overlap:
					if ( d is EqualsDependency )
						return this;
					else if ( d is GreaterThanDependency )
						return new WholeSetDependency( State );
					else if ( d is WholeSetDependency )
						return d;
					else if ( d is LessThanDependency )
					{
						if ( State.Type.ValueSpace.CompareValues( ((LessThanDependency)d).Value, this.Value ) == -1 )
							return d;
						else
							return this;
					}
					else
						return null;
					
				default:
					return null;
			}
		}

		public override bool IsSatisfied()
		{
			return State.Type.ValueSpace.CompareValues( _state.Value, Value ) == -1;
		}

		public override bool ResolveObject(VariableTable varTable)
		{
			bool r = base.ResolveObject (varTable);

			if ( r && !(State.Type.ValueSpace is NumberSpace) )
				throw new NotSupportedException( "LessThanDependency must be associated with numeric ValueSpaces!" );

			return r;
		}

		public override Dependency GetOppositeDependencies()
		{
			OR newF = new OR();

			if ( _valid )
			{
				if ( IsReference )
				{
					newF.AddDependency( new EqualsDependency( this.State, this.ReferenceState ) );
					newF.AddDependency( new GreaterThanDependency( this.State, this.ReferenceState ) );
				}
				else
				{
					newF.AddDependency( new EqualsDependency( this.State, this.ValueString ) );
					newF.AddDependency( new GreaterThanDependency( this.State, this.ValueString ) );
				}
				newF.AddDependency( new UndefinedDependency( this.State ) );
			}
			else
			{
				newF.AddDependency( new EqualsDependency( this.StateName, this.ValueString, this.IsReference, this.LineNumber ) );
				newF.AddDependency( new GreaterThanDependency( this.StateName, this.ValueString, this.IsReference, this.LineNumber ) );
				newF.AddDependency( new UndefinedDependency( this.StateName, this.LineNumber ) );
			}

			return newF;
		}


		/*
		 * Supposedly Protected Member Methods
		 */

		public override PUC.Dependency.Comparison CompareEquals(EqualsDependency d)
		{
			return d.CompareLessThan( this );
		}

		public override PUC.Dependency.Comparison CompareGreaterThan(GreaterThanDependency d)
		{
			return d.CompareLessThan( this );
		}

		public override PUC.Dependency.Comparison CompareLessThan(LessThanDependency d)
		{
			if ( _isRef || d.IsReference )
				return Comparison.Disjoint;

			if ( State.Type.ValueSpace.CompareValues( this.Value, d.Value ) == 0 )
				return Comparison.Same;

			return Comparison.Overlap;
		}
	}
}
