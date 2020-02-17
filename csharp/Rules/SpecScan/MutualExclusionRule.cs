using System;
using System.Collections;
using PUC;
using PUC.Rules;
using PUC.Types;
using PUC.UIGeneration;

namespace PUC.Rules.SpecScan
{
	/// <summary>
	/// The MutualExclusionRule scans the parsed specfication and
	/// finds variables that have groups that are mutually exclusive
	/// with respect to the value of that variable.
	/// </summary>
	public class MutualExclusionRule : SpecScanRule
	{	
		/*
		 * Process Method
		 */

		public override void Process( Appliance a )
		{
			generateOrganization( a.GetRoot(), a.GetDependedObjects() );
		}

		
		protected void generateOrganization( GroupNode root, ArrayList dependedObjects )
		{
			IEnumerator e = dependedObjects.GetEnumerator();

			while( e.MoveNext() )
			{
				ApplianceState state = (ApplianceState)e.Current;

				BranchGroupNode g = findInTree( root, state );

				if ( g != null )
					generateOrganizationAtGroupNode( g, state );
			}
		}
		
		protected BranchGroupNode findInTree( GroupNode g, 
			ApplianceObject ao )
		{
			// if a unit has been found the represents this portion of the 
			// group tree, then don't search for mutual exclusion within it
			// (any such relationships will presumably be handled by the 
			//  the custom CIO)
			if ( g.Decorations[ UnitDecision.DECISION_KEY ] != null )
				return null;

			if ( g.IsObject() )
			{
				if ( ((ObjectGroupNode)g).Object == ao )
					return g.Parent;
			}
			else
			{
				IEnumerator e = ((BranchGroupNode)g).Children.GetEnumerator();
				while( e.MoveNext() )
				{
					BranchGroupNode br = findInTree( (GroupNode)e.Current, ao );
					if ( br != null )
						return br;
				}
			}

			return null;
		}

		protected void generateOrganizationAtGroupNode( BranchGroupNode g, ApplianceState state )
		{
			/*
			 * Step #1: Extract and combine dependencies from each group
			 * at this level of the tree, ignoring repeated states.
			 * 
			 * Need to create a data structure here.  It will be a Hashtable
			 * of ArrayLists, hashed on the GroupNode ref.
			 */

			Hashtable groupDeps = collectDependencies( g, state );

			/*
			 * Step #2: For every group & object that depends on the 
			 * state, check for mutual exclusion.
			 */

			ArrayList mutexSets = findMutualExclusion( g, state, groupDeps );

			ArrayList childSets = (ArrayList)mutexSets[ 0 ];
			ArrayList depSets = (ArrayList)mutexSets[ 1 ];

			/*
			 * Step #3: Save the relevent information into a Decision and 
			 * store it in the root node of the tree.
			 */

			Decision d = new MutualExclusionDecision( state, childSets, depSets );

			// TODO: Allow multiple Multiple MutualExclusionDecisions to be 
			// associated with a particular group
			if ( g.Decorations[ MutualExclusionDecision.DECISION_KEY ] == null )
				g.Decorations.Add( MutualExclusionDecision.DECISION_KEY, d );
		}

		protected Hashtable collectDependencies( GroupNode g,
												 ApplianceState state )
		{
			Hashtable result = new Hashtable();

			BranchGroupNode bg = (BranchGroupNode)g;
			IEnumerator e = bg.Children.GetEnumerator();
			while( e.MoveNext() ) 
			{
				GroupNode ng = (GroupNode)e.Current;

				ArrayList deps = collectDependency( ng, state );

				filterDependencies( deps );

				result[ ng ] = deps;
			}

			return result;
		}

		protected ArrayList collectDependency( GroupNode g,
											   ApplianceState state )
		{
			if ( g.IsObject() )
			{
				ApplianceObject obj = ((ObjectGroupNode)g).Object;

				if ( obj == state )
					return new ArrayList();

				return getDependenciesOfState( state, obj );
			}
			else
			{
				ArrayList results = new ArrayList();

				IEnumerator e = ((BranchGroupNode)g).Children.GetEnumerator();
				while( e.MoveNext() )
					results.AddRange( collectDependency( (GroupNode)e.Current, state ) );

				return results;
			}
		}

		protected ArrayList getDependenciesOfState( ApplianceState state,
													ApplianceObject obj )
		{
			return getDepsOfStateHelper( state, obj.Dependencies );
		}

		protected ArrayList getDepsOfStateHelper( ApplianceState state,
												  Dependency d )
		{
			
			if ( d == null ) return new ArrayList();

			if ( !(d is Formula) )
			{
				ArrayList results = new ArrayList();

				if ( d is StateDependency && ((StateDependency)d).State == state )
					results.Add( d );

				return results;
			}
			else 
			{
				Formula df = (Formula)d;
				ArrayList results = new ArrayList();

				if ( df is OR )
				{
					IEnumerator e = df.GetDependencies();

					while( e.MoveNext() )
					{
						ArrayList childResult = getDepsOfStateHelper( state, (Dependency)e.Current );

						if ( childResult.Count > 0 )
							results.AddRange( childResult );
						else
						{
							results.Clear();
							return results;
						}
					}
				}
				else if ( df is AND )
				{
					IEnumerator e = df.GetDependencies();

					while( e.MoveNext() )
						results.AddRange( getDepsOfStateHelper( state, (Dependency)e.Current ) );
				}
				else if ( df is ApplyOver )
				{
					// TODO: do anything here?
				}

				return results;
			}		
		}

		protected void filterDependencies( ArrayList deps )
		{
			// Step #1: Filter dependencies into groups

			ArrayList greaterThan = new ArrayList();
			ArrayList lessThan = new ArrayList();
			ArrayList equals = new ArrayList();

			IEnumerator e = deps.GetEnumerator();
			while( e.MoveNext() )
			{
				Dependency d = (Dependency)e.Current;

				if ( d is EqualsDependency )
					equals.Add( d );
				else if ( d is LessThanDependency )
					lessThan.Add( d );
				else if ( d is GreaterThanDependency )
					greaterThan.Add( d );
				else if ( d is WholeSetDependency )
				{
					deps.Clear();
					deps.Add( d );
					return;
				}
				else throw new NotSupportedException( "unknown dependency" );
			}

			deps.Clear();

			// Step #2: Merge greater-than (gt) and less-than (lt) dependencies

			StateDependency gtDep = mergeVector( greaterThan );
			StateDependency ltDep = mergeVector( lessThan );

			// Step #3: Check that filtered gt and lt dependencies don't 
			// overlap.  If they do, we're done. (return a new WholeSetDependency)

			if ( gtDep != null && ltDep != null )
			{
				Dependency d = gtDep.Merge( ltDep );
				if ( d != null )
				{
					// d must be a WholeSetDependency.  Make it the only
					// thing in deps, and return.

					deps.Add( d );
					return;
				}
			}

			// Step #4: Merge with equals dependencies

			if ( gtDep != null )
				deps.Add( gtDep );
			if ( ltDep != null )
				deps.Add( ltDep );

			e = equals.GetEnumerator();
			while( e.MoveNext() )
			{
				StateDependency d = (StateDependency)e.Current;
				StateDependency m = null;

				for( int i = 0; i < deps.Count; i++ ) 
				{
					m = d.Merge( (StateDependency)deps[ i ] );

					if ( m != null )
					{
						deps[ i ] = m;
						break;
					}
				}

				if ( m == null )
					deps.Add( d );
			}
		}

		protected StateDependency mergeVector( ArrayList array )
		{
			IEnumerator e = array.GetEnumerator();
			StateDependency d = null;

			if ( e.MoveNext() )
				d = (StateDependency)e.Current;

			while( e.MoveNext() )
			{
				StateDependency nd = (StateDependency)e.Current;

				d = nd.Merge( d );
			}

			return d;
		}

		protected ArrayList findMutualExclusion( GroupNode g,
			ApplianceState state,
			Hashtable groupDeps )
		{
			ArrayList childSets = new ArrayList();
			ArrayList depSets = new ArrayList();

			// The first element in childSet is that set of all children
			// that do not depend on this state at all (there aren't
			// necessarily any)

			ArrayList noDepChildren = new ArrayList();
			ArrayList noDeps = new ArrayList();
			childSets.Add( noDepChildren );
			depSets.Add( noDeps );

			BranchGroupNode bg = (BranchGroupNode)g;

			IEnumerator e = bg.Children.GetEnumerator();
			while( e.MoveNext() )
			{
				bool cont = false;
				GroupNode c = (GroupNode)e.Current;

				if ( c.IsObject() && ((ObjectGroupNode)c).Object == state ) continue; // don't consider the
				// depended child

				// if deps is null, then something is wrong
				ArrayList deps = (ArrayList)groupDeps[ c ];

				if ( deps.Count == 0 )
				{
					noDepChildren.Add( c );
					continue;
				}

				ArrayList dset = null;
				for( int i = 1; i < depSets.Count; i++ )
				{
					dset = (ArrayList)depSets[ i ];

					// check if this element is mutex with sets[ i ]
					ArrayList merge = mergeTwoVectors( deps, dset );

					if ( merge.Count != (deps.Count + dset.Count) )
					{
						depSets[ i ] = merge;
						dset = (ArrayList)childSets[ i ];
						dset.Add( c );

						cont = true;
						break;
					}
				}

				if ( cont )
					continue;

				depSets.Add( deps );
				dset = new ArrayList();
				dset.Add( c );
				childSets.Add( dset );
			}

			ArrayList array = new ArrayList();
			array.Add( childSets );
			array.Add( depSets );
			return array;
		}

		protected ArrayList mergeTwoVectors( ArrayList array1, ArrayList array2 )
		{
			ArrayList result = new ArrayList();

			result.AddRange( array1 );

			IEnumerator e = array2.GetEnumerator();
			while( e.MoveNext() )
			{
				bool cont = false;
				StateDependency d1 = (StateDependency)e.Current;

				for( int i = 0; i < result.Count; i++ )
				{
					StateDependency d2 = (StateDependency)result[ i ];

					StateDependency dm = d1.Merge( d2 );
					if ( dm != null )
					{
						if ( dm is WholeSetDependency )
						{
							result.Clear();
							result.Add( dm );
							return result;
						}

						result[ i ] = dm;
						cont = true;
						break;
					}
				}

				if ( cont )
					continue;

				result.Add( d1 );
			}

			return result;
		}
	}

	public class MutualExclusionDecision : Decision
	{
		/*
		 * Constants
		 */

		public const string DECISION_KEY = "mutexscan";


		/*
		 * Member Variables
		 */

		protected ArrayList		 _childSets;
		protected ArrayList		 _depSets;
		protected ApplianceState _state;


		/*
		 * Constructor
		 */

		public MutualExclusionDecision( ApplianceState state, ArrayList childSets, ArrayList depSets )
		{
			_childSets = childSets;
			_depSets = depSets;
			_state = state;
		}


		/*
		 * Properties
		 */

		public ArrayList DependencySets
		{
			get
			{
				return _depSets;
			}
		}

		public ArrayList ChildSets
		{
			get
			{
				return _childSets;
			}
		}

		public ApplianceState State
		{
			get
			{
				return _state;
			}
		}
	}
}
