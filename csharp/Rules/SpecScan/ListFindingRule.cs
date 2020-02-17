using System;

using PUC;
using PUC.CIO;
using PUC.Registry;
using PUC.Rules;
using PUC.UIGeneration;


namespace PUC.Rules.SpecScan
{
	/// <summary>
	/// This rule finds lists within the specification.  This has two 
	/// purposes:
	/// 1) Marking the location of lists for the later rules that will do
	///    the actual rendering.
	/// 2) It determines the dimensionality of a list, which is important
	///    for deciding how a list will be rendered.
	/// </summary>
	public class ListFindingRule : SpecScanRule
	{
		/*
		 * Constants
		 */


		/*
		 * Process Method
		 */
		public override void Process( Appliance a )
		{
			// start searching for units
			searchHelper( a.GetRoot(), false, a );			
		}

		protected int searchHelper( GroupNode g, bool listBelow, Appliance a )
		{
			if ( g.IsObject() )
				return 0;

			BranchGroupNode bg = (BranchGroupNode)g;
			
			if ( bg.Count == 0 ) 
				return 0;

			ListDecision d = null;
			if ( bg.IsList() ) 
			{
				d = new ListDecision( !listBelow );
				bg.Decorations[ ListDecision.DECISION_KEY ] = d;
			}

			int dim = 0;
			for( int i = 0; i < bg.Children.Count; i++ )
				dim = Math.Max( dim, searchHelper( (GroupNode)bg.Children[ i ], d != null, a ) );

			if ( d != null )
				return d.Dimensions = dim + 1;

			return dim;
		}
	}

	/// <summary>
	/// The result of the ListFindingRule.  This decision encapsulates the
	/// dimensionality of this list and whether or not it is embedded in 
	/// another list.
	/// </summary>
	public class ListDecision : Decision
	{
		/*
		 * Constants
		 */

		public const string DECISION_KEY = "list-mark";


		/*
		 * Member Variables
		 */

		protected bool	_top;
		protected int	_dimensions;


		/*
		 * Constructor
		 */

		public ListDecision( bool top )
			: this( null, top )
		{
		}

		public ListDecision( 
			Decision baseDecision,
			bool top )
			: base( baseDecision )
		{
			_top = top;
			_dimensions = 1;
		}


		/*
		 * Properties
		 */

		public bool Top
		{
			get
			{
				return _top;
			}
		}

		public int Dimensions
		{
			get
			{
				return _dimensions;
			}
			set
			{
				_dimensions = value;
			}
		}
	}
}
