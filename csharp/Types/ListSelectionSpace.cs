using System;
using System.Collections;

namespace PUC.Types
{
	/// <summary>
	/// Summary description for StringSpace.
	/// </summary>
	public class ListSelectionSpace : ValueSpace, IResolvable
	{
		/*
		 * Constants
		 */
	
		
		/*
		 * Member Variables
		 */
	
		protected string			_listName;
		protected bool				_resolved;
		
		protected ListGroupNode		_list;
		protected IndexedDataWindow _window;
		protected Dependency		_formula;


		/*
		 * Constructor
		 */
		/// <summary>
		/// Constructor that initializes BinarySpace
		/// </summary>
		public ListSelectionSpace( ListGroupNode list, Dependency df )
		{
			_list = list;
			_listName = list.FullPath;
			_resolved = true;

			if ( df != null )
			{
				_formula = df.Simplify();
				_window = new IndexedDataWindow( _list.DataWindow );
				changeIndexWindow( _formula );
			}
		}

		public ListSelectionSpace( string list )
		{
			_listName = list;

			_list = null;
			_formula = null;
			_resolved = false;
		}


		/*
		 * Properties
		 */

		public ListGroupNode List
		{
			get
			{
				return _list;
			}
		}

		public string ListName
		{
			get
			{
				return _listName;
			}
			set
			{
				_listName = value;
			}
		}

		public Dependency Formula
		{
			get
			{
				return _formula;
			}
			set
			{
				if ( value == null )
				{
					_formula = null;
					return;
				}

				_formula = value.Simplify();

				if ( _resolved )
					changeIndexWindow( _formula );
			}
		}
		

		/*
		 * Protected Methods
		 */

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
				
				if ( iwin.ListGroup == _list )
				{
					dwin.Parent.RemoveChildWindow( dwin );
					_window.AddChildWindow( dwin );
				}
			}
			else
				changeIndexWindow( dwin.Parent );
		}


		/*
		 * Member Methods
		 */

		/// <summary>
		/// Checks whether a particular list index is selectable according 
		/// to this ListSelectionSpace.
		/// </summary>
		/// <param name="index">the index to check</param>
		/// <returns>whether the index may be selected</returns>
		public bool IsSelectable( int index )
		{
			if ( !_resolved )
				return false;

			// check bounds of the list
			if ( index < 1 )
				return false;
			
			if ( _list.Maximum != null && index > _list.Maximum.GetIntValue() )
				return false;

			// return true if no formula exists
			if ( _formula == null )
				return true;

			// check if the formula allows this index to be selected
			_window.Index = index;
			return _formula.IsSatisfied();
		}


		/*
		 * Overridden Methods
		 */
		/// <summary>
		/// Returns a shallow copy of this ListSelectionSpace.
		/// </summary>
		/// <returns>a shallow copy of this ListSelectionSpace</returns>
		public override object Clone()
		{
			return new ListSelectionSpace( _list, _formula );
		}

		/// <summary>
		/// Returns "list-selection" 
		/// </summary>
		/// <returns>"list-selection"</returns>
		public override string Name
		{
			get
			{
				return "list-selection";
			}
		}

		/// <summary>
		/// Returns ValueSpace.LIST_SELECTION_SPACE.
		/// </summary>
		/// <returns>ValueSpace.LIST_SELECTION_SPACE</returns>
		public override int Space
		{
			get
			{
				return ValueSpace.LIST_SELECTION_SPACE;
			}
		}

		/// <summary>
		/// Determines whether the provided value could be assigned to 
		/// this ValueSpace.
		/// </summary>
		/// <param name="val">the value to be validated</param>
		/// <returns>whether this value can be assigned to a list-selection space</returns>
		public override object Validate( object val )
		{
			if ( val is string ) 
			{
				try 
				{
					int intval = Int32.Parse( (string)val );
					if ( validateNoStrCheck( intval ) )
						return intval;
				}
				catch(FormatException) 
				{
					return null;
				}
			}
			else if ( validateNoStrCheck( val ) )
				return val;

			return null;
		}

		/// <summary>
		/// Validates that the provided value can be assigned to this 
		/// ValueSpace.  Assumes that the provided value is not a string.
		/// </summary>
		/// <param name="val">a non-string object that contains a value for this space</param>
		/// <returns>whether or not val is within this space</returns>
		private bool validateNoStrCheck( object val )
		{
			if ( val is Int32 ) 
			{
				int intval = (int)val;

				return IsSelectable( intval );
			}

			return false;
		}

		/// <summary>
		/// Compares the values of two ListSelectionSpaces.  This method 
		/// is exactly the same as the method for checking EnumeratedSpace 
		/// and IntegerSpace.
		/// </summary>
		/// <param name="space">the space to compare to this one</param>
		/// <returns>0 if the value of the two sets are equals, 
		/// -1 if the value passed in is less than this space's value</returns>
		/// <exception cref="InvalidCastException">thrown if space is not an instance of EnumeratedSpace</exception>
		public override int CompareValues( object val1, object val2 )
		{
			if ( val1 == null && val2 == null ) return 0;
			else if ( val1 != null ^ val2 != null ) return -1;

			int ival1 = (int)val1;
			int ival2 = (int)val2;

			if ( ival1 == ival2 ) return 0;
			else if ( ival1 > ival2 ) return 1;
			else return -1;
		}

		/// <summary>
		/// Register any constraints in this type with the appropriate 
		/// state variable.
		/// </summary>
		/// <param name="state"></param>
		public override void RegisterConstraints(ApplianceState state)
		{
			if ( _list.Maximum != null && _list.Maximum is IConstraint )
				new TypeConstraintListener( state, (IConstraint)_list.Maximum );
		}

		public override bool IsSameSpace(ValueSpace space)
		{
			if ( space is ListSelectionSpace )
			{
				ListSelectionSpace lspace = (ListSelectionSpace)space;

				if ( lspace.ListName != this.ListName )
					return false;

				if ( lspace.Formula != null && this.Formula != null )
				{
					// FIXME: JWN: This code is incorrect.  The dependency
					// formulas should be compared to see if they are the 
					// same.  Since comparing the formulas is a little tricky,
					// I'm going to leave this out.  Hopefully this doesn't 
					// create a bug.
					return true;
				}
				else if ( lspace.Formula == null && this.Formula == null )
					return true;
				else
					return false;

				// return true;
			}

			return false;
		}


		#region IResolvable Members

		public bool ResolveObject(VariableTable varTable)
		{
			_list = varTable.GetListGroup( _listName );
			_resolved = ( _list != null );
			
			if ( _resolved )
			{
				_window = new IndexedDataWindow( _list.DataWindow );
				changeIndexWindow( _formula );
			}

			return _resolved;
		}

		public bool Valid
		{
			get
			{
				return _resolved;
			}
		}

		#endregion
	}
}
