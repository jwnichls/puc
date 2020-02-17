using System;
using System.Collections;

/*
 * This file contains the IConstraint interface and several classes that
 * implement it.
 */

namespace PUC.Types
{
	/// <summary>
	/// The Constraint interface is implemented by other objects to 
	/// create ValueSpace objects that are constrained by other 
	/// ValueSpaces.  This enables a very basic constraint system.
	///   
	/// These objects require "validation" after parsing, because the 
	/// Value state name is only known as a String at the end of the 
	/// process.  One of the later phases of the Parsers.SpecParser 
	/// resolves these Strings to ApplianceState objects.  The 
	/// get-methods will throw a NullReferenceException if they are 
	/// called before validation.
	/// </summary>
	public interface IConstraint : IResolvable
	{
		/// <summary>
		/// Returns an array of references to the value states.
		/// </summary>
		/// <returns>an array of references to the value states</returns>
		ApplianceState[] States
		{
			get;
		}

		/// <summary>
		/// The event that will be called when a constraint's value changes.
		/// Used to update any objects that need to be aware of changes.
		/// </summary>
		event EventHandler ConstraintUpdate;
	}

	/// <summary>
	/// A constraint implemented for setting the enable of EnumeratedSpace
	/// items based on the value of other BooleanSpace states.  These values
	/// are stored in the LabelDictionary object.
	/// </summary>
	public class EnableConstraint : IConstraint
	{
		/*
		 * Member Variables
		 */

		/// <summary>
		/// A dependency formula specifying when this constraint is
		/// enabled.
		/// </summary>
		protected Dependency	 _formula;

		/// <summary>
		/// Whether this EnableConstraint has resolved the states 
		/// in its dependency formula.
		/// </summary>
		protected bool           _valid;

		/// <summary>
		/// The states that this enable constraint relies upon.
		/// </summary>
		protected ArrayList		 _states;


		/*
		 * Events
		 */

		public event EventHandler ConstraintUpdate;


		/*
		 * Constructor
		 */
		/// <summary>
		/// Creates an EnableConstraint object with a dependency formula.
		/// </summary>
		/// <param name="formula"></param>
		public EnableConstraint( Dependency formula )
		{
			_valid = false;
			_states = new ArrayList();
			
			_formula = formula;
		}

		/// <summary>
		/// Creates an EnableConstraint object with a dependency formula.
		/// Also automatically creates a listener for the constrained state.
		/// </summary>
		/// <param name="constrainedState">a reference to the state that will be constrained</param>
		/// <param name="formula">a dependency formula specifying the constraints</param>
		public EnableConstraint( ApplianceState constrainedState,
			                     Dependency formula )
		{
			_valid = false;
			_states = new ArrayList();			
			_formula = formula;

			// create listener
			new TypeConstraintListener( constrainedState, this );
		}

		/// <summary>
		/// Creates an EnableConstraint object and parses any not
		/// operations implicit in the valueStateName.
		/// </summary>
		/// <param name="constrainedState">a reference to the state that will be constrained</param>
		/// <param name="enablestr">the state that the enable is based upon</param>
		public EnableConstraint( ApplianceState constrainedState,
								 string enablestr )
		{
			_valid = false;
			_states = new ArrayList();

			// create a dependency from the string
			if ( enablestr.StartsWith( "!" ) )
			{
				_formula = new NOT();
				enablestr = enablestr.Substring( 1 );

				((Formula)_formula).AddDependency( new EqualsDependency( enablestr, PUC.Parsers.SpecParser.ATTR_VALUE_TRUE, false, constrainedState.LineNumber ) );
			}
			else
				_formula = new EqualsDependency( enablestr, PUC.Parsers.SpecParser.ATTR_VALUE_TRUE, false, constrainedState.LineNumber );
			
			// create listener
			new TypeConstraintListener( constrainedState, this );
		}

		/*
		 * Properties
		 */
		
		public Dependency Formula
		{
			get
			{
				return _formula;
			}
		}

		public bool Valid
		{
			get
			{
				return _valid;
			}
		}

		public ApplianceState[] States
		{
			get
			{
				return (ApplianceState[])_states.ToArray( Type.GetType( "PUC.ApplianceState" ) );
			}
		}

		/// <summary>
		/// Gets the boolean value that this EnableConstraint represents.
		/// </summary>
		/// <returns>the boolean value that this EnableConstraint represents</returns>
		public bool Value
		{
			get
			{
				return _formula.IsSatisfied();
			}
		}


		/*
		 * Member Methods
		 */

		public bool ResolveObject( VariableTable varTable ) 
		{
			_formula = _formula.Simplify();
			resolveHelper( _formula, varTable );

			// an exception would have been throw if any states were
			// not resolved
			_valid = true;

			IEnumerator e = _states.GetEnumerator();
			while( e.MoveNext() )
				((ApplianceState)e.Current).ValueChangedEvent += 
					new ApplianceState.ValueChangedHandler(this.ChangeNotify);

			return _valid;
		}

		protected void resolveHelper( Dependency df, 
									  VariableTable varTable )
		{
			if ( df == null )
				return;

			if ( df is ApplyOver )
				((ApplyOver)df).ResolveObject( varTable );

			if ( df is Formula )
			{
				IEnumerator e = ((Formula)df).GetDependencies();
				while( e.MoveNext() )
					resolveHelper( (Dependency)e.Current, varTable );
			}
			else if ( df is StateDependency )
			{
				StateDependency d = (StateDependency)df;

				if (! d.ResolveObject( varTable ) )
					throw new PUC.Parsers.SpecParseException( d.LineNumber, "Problem resolving a dependency state name: " + d.StateName );
	
				_states.Add( d.State );

				if ( d.HasValue() && ((StateValueDependency)d).IsReference )
					_states.Add( ((StateValueDependency)d).ReferenceState );
			}

			if ( df is ApplyOver )
				((ApplyOver)df).FixDataWindows();
		}

		protected void ChangeNotify( ApplianceState state )
		{
			if ( ConstraintUpdate != null )
				ConstraintUpdate( this, new EventArgs() );
		}


		/// <summary>
		/// Returns a string-based description of this EnableConstraint
		/// </summary>
		/// <returns>a string-based description of this EnableConstraint</returns>
		public override string ToString()
		{
			return "EnableConstraint: [" + _formula.ToString() + "]";	
		}
	}

	/// <summary>
	/// This class is a work-around for supporting old specification parsers
	/// that were written before Constraints were updated to use events instead
	/// of storing the constrained object.  This listener works for constraints
	/// that modify types, like EnableConstraint and NumberConstraint.
	/// </summary>
	public class TypeConstraintListener
	{
		/*
		 * Member Variables
		 */
		ApplianceState _constrainedState;


		/*
		 * Constructor
		 */
		public TypeConstraintListener( ApplianceState state, IConstraint constraint )
		{
			_constrainedState = state;

			constraint.ConstraintUpdate += new EventHandler(this.handlerMethod);
		}


		/*
		 * Event Handler
		 */
		protected void handlerMethod( object source, EventArgs e )
		{
			_constrainedState.TypeChanged();
		}
	}

	/// <summary>
	/// This class is a work-around for supporting old specification parsers
	/// that were written before Constraints were updated to use events instead
	/// of storing the constrained object.  This listener works for constraints
	/// that modify labels, like StringConstraint.
	/// </summary>
	public class LabelConstraintListener
	{
		/*
		 * Member Variables
		 */
		ApplianceObject _constrainedObject;


		/*
		 * Constructor
		 */
		public LabelConstraintListener( ApplianceObject obj, IConstraint constraint )
		{
			_constrainedObject = obj;

			constraint.ConstraintUpdate += new EventHandler(this.handlerMethod);
		}


		/*
		 * Event Handler
		 */
		protected void handlerMethod( object source, EventArgs e )
		{
			_constrainedObject.LabelChanged();
		}
	}
}
