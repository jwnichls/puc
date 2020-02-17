using System;

namespace PUC.Types
{
	/// <summary>
	/// IPUCValue represents an indirect reference to an
	/// object.  This is used to abstract away whether a 
	/// value is constant or based on the value of some
	/// other state.
	/// </summary>
	public interface IPUCValue
	{
		object Value
		{
			get;
		}

		bool IsSameValue( IPUCValue val );
	}

	/// <summary>
	/// An IPUCValue object representing a constant value. 
	/// Instances of this object are immutable.
	/// </summary>
	public class PUCValue : IPUCValue
	{
		/*
		 * Member Variables
		 */

		protected object _value;


		/*
		 * Constructor
		 */

		public PUCValue( object value )
		{
			_value = value;
		}


		/*
		 * Member Variables
		 */

		#region IPUCValue Members

		public object Value
		{
			get
			{
				return _value;
			}
		}

		public virtual bool IsSameValue( IPUCValue val )
		{
			if ( val is PUCValue )
			{
				if ( val.Value != this.Value )
					return false;

				return true;
			}

			return false;
		}	

		#endregion
	}

	public class ValueConstraint : IPUCValue, IConstraint
	{
		/*
		 * Member Variables
		 */

		protected ValueDataWindow	_state;
		protected string			_stateName;

		protected bool				_valid;


		/*
		 * Events
		 */

		public event System.EventHandler ConstraintUpdate;


		/*
		 * Constructor
		 */

		public ValueConstraint( ApplianceState state )
		{
			_state = new ValueDataWindow( state );
			_stateName = state.Name;

			State.ValueChangedEvent += new PUC.ApplianceState.ValueChangedHandler(this.StateValueChanged);

			_valid = true;
		}

		public ValueConstraint( string stateName )
		{
			_stateName = stateName;
			_valid = false;
		}


		/*
		 * Properties
		 */

		public ApplianceState State
		{
			get
			{
				return _state.State;
			}
		}

		public IDataWindow StateWindow
		{
			get
			{
				return _state;
			}
		}


		/*
		 * Member Methods
		 */

		public bool DoesValueSpaceMatch( ApplianceState state )
		{
			return _state.State.Type.ValueSpace.IsSameSpace( state.Type.ValueSpace );
		}

		protected void StateValueChanged( ApplianceState state )
		{
			if ( ConstraintUpdate != null )
				ConstraintUpdate( this, new EventArgs() );
		}

		protected void setInvalid()
		{
			_valid = false;

			ConstraintUpdate = null;
		}

		#region IPUCValue Members

		public object Value
		{
			get
			{
				return _state.Value;
			}
		}

		#endregion

		#region IConstraint Members

		public ApplianceState[] States
		{
			get
			{
				return new ApplianceState[] { State };
			}
		}

		public virtual bool IsSameValue( IPUCValue val )
		{
			if ( val is ValueConstraint )
			{
				ValueConstraint vc = (ValueConstraint)val;

				if ( vc._stateName != this._stateName )
					return false;

				return true;
			}

			return false;
		}	

		#endregion

		#region IResolvable Members

		public virtual bool ResolveObject(VariableTable varTable)
		{
			ApplianceState state = (ApplianceState)varTable[ _stateName ];
			_valid = ( state != null );

			if ( _valid )
			{
				state.ValueChangedEvent += new PUC.ApplianceState.ValueChangedHandler(this.StateValueChanged);
				_state = new ValueDataWindow( state );
			}

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

}
