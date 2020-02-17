using System;
using System.Collections;

using PUC.Rules.SpecScan;

namespace PUC.Registry
{
	/// <summary>
	/// Summary description for Decision.
	/// </summary>
	public abstract class Decision
	{
		/*
		 * Constants
		 */
		public const String DEFAULT = "default";
		
		public const String TRUE           = "true";
		public const String FALSE          = "false";


		/*
		 * Member Variables
		 */
		protected Hashtable _choices;
		
		protected static Hashtable _decisions = new Hashtable();
		public delegate Decision CreateDecision( Hashtable choices );


		/*
		 * Static Constructor
		 */
		static Decision()
		{
			AddDecisionType( AllLabelsDecision.DECISION_LABEL, 
							 new CreateDecision( AllLabelsDecision.CreateAllLabelsDecision ) );
			AddDecisionType( BoundedNumberDecision.DECISION_LABEL, 
				             new CreateDecision( BoundedNumberDecision.CreateBoundedNumberDecision ) );
			AddDecisionType( ConstraintDecision.DECISION_LABEL, 
				             new CreateDecision( ConstraintDecision.CreateConstraintDecision ) );
			AddDecisionType( DependedUponOnceDecision.DECISION_LABEL, 
				             new CreateDecision( DependedUponOnceDecision.CreateDependedUponOnceDecision ) );
			AddDecisionType( InternalControlDecision.DECISION_LABEL, 
				             new CreateDecision( InternalControlDecision.CreateInternalControlDecision ) );
			AddDecisionType( ObjectDecision.DECISION_LABEL, 
							 new CreateDecision( ObjectDecision.CreateObjectDecision ) );
			AddDecisionType( ReadOnlyDecision.DECISION_LABEL, 
				             new CreateDecision( ReadOnlyDecision.CreateReadOnlyDecision ) );
			AddDecisionType( ValueSpaceDecision.DECISION_LABEL, 
				             new CreateDecision( ValueSpaceDecision.CreateValueSpaceDecision ) );
			AddDecisionType( ListDecision.DECISION_LABEL, 
							 new CreateDecision( ListDecision.CreateListDecision ) );
			AddDecisionType( DimensionDecision.DECISION_LABEL, 
							 new CreateDecision( DimensionDecision.CreateDimensionDecision ) );
			AddDecisionType( ReadonlyElementsDecision.DECISION_LABEL,
							 new CreateDecision( ReadonlyElementsDecision.CreateReadonlyElementsDecision ) );
		}


		/*
		 * Constructor
		 */
		public Decision( Hashtable choices )
		{
			_choices = choices;
		}


		/*
		 * Abstract Member Methods
		 */
		public abstract PUC.CIO.ConcreteInteractionObject ChooseWidget( PUC.ApplianceObject ao );
		public abstract PUC.CIO.ConcreteInteractionObject ChooseWidget( PUC.GroupNode ao );


		/*
		 * Member Methods
		 */
		public override String ToString() 
		{
			String result = "Conditions:\r\n";

			IEnumerator e = _choices.Keys.GetEnumerator();
			while( e.MoveNext() )
			{
				String key = (String)e.Current;

				result += key + "\r\n=\r\n" + _choices[ key ].ToString();
			}

			return result;
		}


		/*
		 * Static Member Methods
		 */
		public static void AddDecisionType( String name, CreateDecision fptr )
		{
			_decisions[ name ] = fptr;
		}

		public static CreateDecision GetDecisionFactory( String name )
		{
			return (CreateDecision)_decisions[ name ];
		}
	}

	public class AllLabelsDecision : Decision
	{
		/*
		 * Constants
		 */
		public const String DECISION_LABEL = "alllabels";


		/*
		 * Dynamic Loading Code
		 *
		static AllLabelsDecision()
		{
			Decision.AddDecisionType( DECISION_LABEL, 
									  new CreateDecision(AllLabelsDecision.CreateAllLabelsDecision) );
		}
		*/

		public static Decision CreateAllLabelsDecision( Hashtable choices )
		{
			return new AllLabelsDecision( choices );
		}

		/*
		 * Constructor
		 */
		public AllLabelsDecision( Hashtable choices ) : base( choices )
		{
		}
		
	
		/*
		 * Member Methods
		 */
		public override PUC.CIO.ConcreteInteractionObject ChooseWidget(  
																		ApplianceObject ao)
		{
			ApplianceState state = (ApplianceState)ao;
			Decision d = null;

			switch( state.Type.ValueSpace.Space )
			{
				case PUC.Types.ValueSpace.BOOLEAN_SPACE:
					if ( state.Type.ValueLabels != null &&
						state.Type.ValueLabels.Count == 2 )
					{
						d = (Decision)_choices[ TRUE ];
					}
					else
						d = (Decision)_choices[ FALSE ];
					break;

				default:
					d = (Decision)_choices[ FALSE ];
					break;
			}

			if ( d != null )
				return d.ChooseWidget( ao );
			else
				return null;
		}

		public override PUC.CIO.ConcreteInteractionObject ChooseWidget(GroupNode ao)
		{
			return null;
		}

		public override string ToString()
		{
			return "ALLLABELS Decision\r\n" + base.ToString();
		}

	}


	public class BoundedNumberDecision : Decision
	{
		/*
		 * Constants
		 */
		public const String DECISION_LABEL = "boundednumber";


		/*
		 * Dynamic Loading Code
		 *
		static BoundedNumberDecision()
		{
			Decision.AddDecisionType( DECISION_LABEL, 
				new CreateDecision(BoundedNumberDecision.CreateBoundedNumberDecision) );
		}
		*/

		public static Decision CreateBoundedNumberDecision( Hashtable choices )
		{
			return new BoundedNumberDecision( choices );
		}


		/*
		 * Constructor
		 */
		public BoundedNumberDecision( Hashtable choices ) : base( choices )
		{
		}
		
	
		/*
		 * Member Methods
		 */
		public override PUC.CIO.ConcreteInteractionObject ChooseWidget(  
			ApplianceObject ao)
		{
			ApplianceState state = (ApplianceState)ao;
			PUC.Types.ValueSpace vs = state.Type.ValueSpace;

			Decision d = null;

			if ( vs.Space == PUC.Types.ValueSpace.INTEGER_SPACE &&
				 ((PUC.Types.IntegerSpace)vs).IsRanged() )

				d = (Decision)_choices[ TRUE ];

			else if ( vs.Space == PUC.Types.ValueSpace.FIXED_PT_SPACE &&
				      ((PUC.Types.FixedPtSpace)vs).IsRanged() )

				d = (Decision)_choices[ TRUE ];

			else if ( vs.Space == PUC.Types.ValueSpace.FLOATING_PT_SPACE &&
					  ((PUC.Types.FloatingPtSpace)vs).IsRanged() )

				d = (Decision)_choices[ TRUE ];

			else
				d = (Decision)_choices[ FALSE ];

			if ( d != null )
				return d.ChooseWidget( ao );
			else
				return null;
		}

		public override PUC.CIO.ConcreteInteractionObject ChooseWidget( GroupNode g )
		{
			return null;
		}

		public override string ToString()
		{
			return "BOUNDED-NUMBER Decision\r\n" + base.ToString();
		}

	}


	public class ConstraintDecision : Decision
	{
		/*
		 * Constants
		 */
		public const String DECISION_LABEL = "constraint";


		/*
		 * Dynamic Loading Code
		 *
		static ConstraintDecision()
		{
			Decision.AddDecisionType( DECISION_LABEL, 
				new CreateDecision(ConstraintDecision.CreateConstraintDecision) );
		}
		*/

		public static Decision CreateConstraintDecision( Hashtable choices )
		{
			return new ConstraintDecision( choices );
		}


		/*
		 * Constructor
		 */
		public ConstraintDecision( Hashtable choices ) : base( choices )
		{
		}
		
	
		/*
		 * Member Methods
		 */
		public override PUC.CIO.ConcreteInteractionObject ChooseWidget(  
			ApplianceObject ao)
		{
			ApplianceState state = (ApplianceState)ao;
			Decision d = null;

			if ( state.ConstraintVariable )
				d = (Decision)_choices[ TRUE ];
			else
				d = (Decision)_choices[ FALSE ];

			if ( d != null )
				return d.ChooseWidget( ao );
			else
				return null;
		}

		public override PUC.CIO.ConcreteInteractionObject ChooseWidget(GroupNode ao)
		{
			return null;
		}

		public override string ToString()
		{
			return "CONSTRAINT Decision\r\n" + base.ToString();
		}

	}


	public class DependedUponOnceDecision : Decision
	{
		/*
		 * Constants
		 */
		public const String DECISION_LABEL = "depended-upon-once";


		/*
		 * Dynamic Loading Code
		 *
		static DependedUponOnceDecision()
		{
			Decision.AddDecisionType( DECISION_LABEL, 
				new CreateDecision(DependedUponOnceDecision.CreateDependedUponOnceDecision) );
		}
		*/

		public static Decision CreateDependedUponOnceDecision( Hashtable choices )
		{
			return new DependedUponOnceDecision( choices );
		}


		/*
		 * Constructor
		 */
		public DependedUponOnceDecision( Hashtable choices ) : base( choices )
		{
		}
		
	
		/*
		 * Member Methods
		 */
		public override PUC.CIO.ConcreteInteractionObject ChooseWidget(  
			ApplianceObject ao)
		{
			ApplianceState state = (ApplianceState)ao;
			Decision d = null;

			if ( state.GetReverseDependencyCount() == 1 )
				d = (Decision)_choices[ TRUE ];
			else
				d = (Decision)_choices[ FALSE ];

			if ( d != null )
				return d.ChooseWidget( ao );
			else
				return null;
		}

		public override PUC.CIO.ConcreteInteractionObject ChooseWidget(GroupNode ao)
		{
			return null;
		}

		public override string ToString()
		{
			return "DEPENDED-UPON-ONCE Decision\r\n" + base.ToString();
		}

	}


	public class InternalControlDecision : Decision
	{
		/*
		 * Constants
		 */
		public const String DECISION_LABEL = "internalcontroller";


		/*
		 * Dynamic Loading Code
		 *
		static InternalControlDecision()
		{
			Decision.AddDecisionType( DECISION_LABEL, 
				new CreateDecision(InternalControlDecision.CreateInternalControlDecision) );
		}
		*/

		public static Decision CreateInternalControlDecision( Hashtable choices )
		{
			return new InternalControlDecision( choices );
		}


		/*
		 * Constructor
		 */
		public InternalControlDecision( Hashtable choices ) : base( choices )
		{
		}
		
	
		/*
		 * Member Methods
		 */
		public override PUC.CIO.ConcreteInteractionObject ChooseWidget(  
			ApplianceObject ao)
		{
			ApplianceState state = (ApplianceState)ao;
			Decision d = null;

			if ( state.InternalController )
				d = (Decision)_choices[ TRUE ];
			else
				d = (Decision)_choices[ FALSE ];

			if ( d != null )
				return d.ChooseWidget( ao );
			else
				return null;
		}

		public override PUC.CIO.ConcreteInteractionObject ChooseWidget(GroupNode ao)
		{
			return null;
		}

		public override string ToString()
		{
			return "INTERNAL-CONTROLLER Decision\r\n" + base.ToString();
		}

	}


	public class ObjectDecision : Decision
	{
		/*
		 * Constants
		 */
		public const String DECISION_LABEL = "object";

		public const String COMMAND        = "command";
		public const String STATE          = "state";
		public const String EXPLANATION    = "explanation";


		/*
		 * Dynamic Loading Code
		 *
		static ObjectDecision()
		{
			Decision.AddDecisionType( DECISION_LABEL, 
				new CreateDecision(ObjectDecision.CreateObjectDecision) );
		}
		*/

		public static Decision CreateObjectDecision( Hashtable choices )
		{
			return new ObjectDecision( choices );
		}


		/*
		 * Constructor
		 */
		public ObjectDecision( Hashtable choices ) : base( choices )
		{
		}
		
	
		/*
		 * Member Methods
		 */
		public override PUC.CIO.ConcreteInteractionObject ChooseWidget(  
			ApplianceObject ao)
		{
			Decision d = null;

			if ( ao.State )
				d = (Decision)_choices[ STATE ];
			else if ( ao.Explanation )
				d = (Decision)_choices[ EXPLANATION ];
			else
				d = (Decision)_choices[ COMMAND ];

			if ( d == null )
				d = (Decision)_choices[ DEFAULT ];

			if ( d != null )
				return d.ChooseWidget( ao );
			else
				return null;
		}

		public override PUC.CIO.ConcreteInteractionObject ChooseWidget(GroupNode ao)
		{
			return null;
		}

		public override string ToString()
		{
			return "OBJECT Decision\r\n" + base.ToString();
		}

	}


	public class ReadOnlyDecision : Decision
	{
		/*
		 * Constants
		 */
		public const String DECISION_LABEL = "readonly";


		/*
		 * Dynamic Loading Code
		 *
		static ReadOnlyDecision()
		{
			Decision.AddDecisionType( DECISION_LABEL, 
				new CreateDecision(ReadOnlyDecision.CreateReadOnlyDecision) );
		}
		*/

		public static Decision CreateReadOnlyDecision( Hashtable choices )
		{
			return new ReadOnlyDecision( choices );
		}


		/*
		 * Constructor
		 */
		public ReadOnlyDecision( Hashtable choices ) : base( choices )
		{
		}
		
	
		/*
		 * Member Methods
		 */
		public override PUC.CIO.ConcreteInteractionObject ChooseWidget(  
			ApplianceObject ao)
		{
			ApplianceState state = (ApplianceState)ao;
			Decision d = null;

			if ( state.ReadOnly )
				d = (Decision)_choices[ TRUE ];
			else
				d = (Decision)_choices[ FALSE ];

			if ( d != null )
				return d.ChooseWidget( ao );
			else
				return null;
		}

		public override PUC.CIO.ConcreteInteractionObject ChooseWidget(GroupNode ao)
		{
			return null;
		}

		public override string ToString()
		{
			return "READONLY Decision\r\n" + base.ToString();
		}

	}


	public class ValueSpaceDecision : Decision
	{
		/*
		 * Constants
		 */
		public const String DECISION_LABEL = "valuespace";


		/*
		 * Dynamic Loading Code
		 *
		static ValueSpaceDecision()
		{
			Decision.AddDecisionType( DECISION_LABEL, 
				new CreateDecision(ValueSpaceDecision.CreateValueSpaceDecision) );
		}
		*/

		public static Decision CreateValueSpaceDecision( Hashtable choices )
		{
			return new ValueSpaceDecision( choices );
		}


		/*
		 * Constructor
		 */
		public ValueSpaceDecision( Hashtable choices ) : base( choices )
		{
		}
		
	
		/*
		 * Member Methods
		 */
		public override PUC.CIO.ConcreteInteractionObject ChooseWidget(  
			ApplianceObject ao)
		{
			ApplianceState state = (ApplianceState)ao;

			Decision d = (Decision)_choices[ state.Type.ValueSpace.Name ];

			if ( d == null )
				d = (Decision)_choices[ DEFAULT ];

			if ( d != null )
				return d.ChooseWidget( ao );
			else
				return null;
		}

		public override PUC.CIO.ConcreteInteractionObject ChooseWidget(GroupNode ao)
		{
			return null;
		}

		public override string ToString()
		{
			return "VALUESPACE Decision\r\n" + base.ToString();
		}

	}


	public class WidgetDecision : Decision
	{
		/*
		 * Member Variables
		 */
		protected String _widgetName;
		

		/*
		 * Constructor
		 */
		public WidgetDecision( String name ) : base( null )
		{
			_widgetName = name;

			// Java version initializes here...
			// We just check to see if the CIO is in either of the 
			// factory lists

			if ( PUC.CIO.ConcreteInteractionObject.GetCIOFactory( _widgetName ) == null && 
				 PUC.CIO.List.ListCIO.GetCIOFactory( _widgetName ) == null )
				Globals.GetDefaultLog().AddLogLine( "WidgetDecision created with unavailable CIO: " + _widgetName );
		}
		
	
		/*
		 * Member Methods
		 */
		public override PUC.CIO.ConcreteInteractionObject ChooseWidget(  
			ApplianceObject ao)
		{
			PUC.CIO.ConcreteInteractionObject.CreateCIO fptr = PUC.CIO.ConcreteInteractionObject.GetCIOFactory( _widgetName );
			if ( fptr == null )
				return null;

			return fptr( ao );
		}

		public override PUC.CIO.ConcreteInteractionObject ChooseWidget(GroupNode g)
		{
			PUC.CIO.List.ListCIO.CreateCIO fptr = PUC.CIO.List.ListCIO.GetCIOFactory( _widgetName );
			if ( fptr == null )
				return null;

			return fptr( (ListGroupNode)g );
		}

		public override string ToString()
		{
			return "Widget: " + _widgetName + "\r\n";
		}

	}

	public class ListDecision : Decision
	{
		/*
		 * Constants
		 */
		public const String DECISION_LABEL = "list";


		/*
		 * Dynamic Loading Code
		 *
		static ListDecision()
		{
			Decision.AddDecisionType( DECISION_LABEL, 
				new CreateDecision(ListDecision.CreateListDecision) );
		}
		*/

		public static Decision CreateListDecision( Hashtable choices )
		{
			return new ListDecision( choices );
		}


		/*
		 * Constructor
		 */
		public ListDecision( Hashtable choices ) : base( choices )
		{
		}
		
	
		/*
		 * Member Methods
		 */
		public override PUC.CIO.ConcreteInteractionObject ChooseWidget(  
			ApplianceObject ao)
		{
			Decision d = (Decision)_choices[ FALSE ];

			if ( d != null )
				return d.ChooseWidget( ao );
			else
				return null;
		}

		public override PUC.CIO.ConcreteInteractionObject ChooseWidget(GroupNode g )
		{
			Decision d = null;

			if ( g.IsList() )
				d = (Decision)_choices[ TRUE ];
			else
				d = (Decision)_choices[ FALSE ];

			if ( d != null )
				return d.ChooseWidget( g );
			else
				return null;
		}

		public override string ToString()
		{
			return "LIST Decision\r\n" + base.ToString();
		}

	}

	public class DimensionDecision : Decision
	{
		/*
		 * Constants
		 */
		public const String DECISION_LABEL = "dimension";


		/*
		 * Dynamic Loading Code
		 *
		static DimensionDecision()
		{
			Decision.AddDecisionType( DECISION_LABEL, 
				new CreateDecision(DimensionDecision.CreateDimensionDecision) );
		}
		*/

		public static Decision CreateDimensionDecision( Hashtable choices )
		{
			return new DimensionDecision( choices );
		}


		/*
		 * Constructor
		 */
		public DimensionDecision( Hashtable choices ) : base( choices )
		{
		}
		
	
		/*
		 * Member Methods
		 */
		public override PUC.CIO.ConcreteInteractionObject ChooseWidget(  
			ApplianceObject ao)
		{
			return null;
		}

		public override PUC.CIO.ConcreteInteractionObject ChooseWidget(GroupNode g )
		{
			Decision d = null;

			PUC.Rules.SpecScan.ListDecision ld = 
				(PUC.Rules.SpecScan.ListDecision)
				g.Decorations[ PUC.Rules.SpecScan.ListDecision.DECISION_KEY ];

			if ( ld == null )
				return null;

			d = (Decision)_choices[ ld.Dimensions.ToString() ];

			if ( d != null )
				return d.ChooseWidget( g );
			else
				return null;
		}

		public override string ToString()
		{
			return "DIMENSION Decision\r\n" + base.ToString();
		}
	}

	public class ReadonlyElementsDecision : Decision
	{
		/*
		 * Constants
		 */
		public const String DECISION_LABEL = "readonly-elements";


		/*
		 * Dynamic Loading Code
		 *
		static DimensionDecision()
		{
			Decision.AddDecisionType( DECISION_LABEL, 
				new CreateDecision(DimensionDecision.CreateDimensionDecision) );
		}
		*/

		public static Decision CreateReadonlyElementsDecision( Hashtable choices )
		{
			return new ReadonlyElementsDecision( choices );
		}


		/*
		 * Constructor
		 */
		public ReadonlyElementsDecision( Hashtable choices ) : base( choices )
		{
		}
		
	
		/*
		 * Member Methods
		 */
		public override PUC.CIO.ConcreteInteractionObject ChooseWidget(  
			ApplianceObject ao)
		{
			return null;
		}

		public override PUC.CIO.ConcreteInteractionObject ChooseWidget(GroupNode g )
		{
			Decision d = null;

			if ( readonlyHelper( (BranchGroupNode)g ) )
				d = (Decision)_choices[ TRUE ];
			else
				d = (Decision)_choices[ FALSE ];

			if ( d != null )
				return d.ChooseWidget( g );
			else
				return null;
		}

		protected bool readonlyHelper( BranchGroupNode bg )
		{
			IEnumerator e = bg.Children.GetEnumerator();
			while( e.MoveNext() )
			{
				if ( e.Current is ObjectGroupNode )
				{
					if ( ((ObjectGroupNode)e.Current).Object.State &&
						!((ApplianceState)((ObjectGroupNode)e.Current).Object).ReadOnly )
						return false;
				}
				else
				{
					if ( !readonlyHelper( (BranchGroupNode)e.Current ) )
						return false;
				}
			}

			return true;
		}

		public override string ToString()
		{
			return "READONLY-ELEMENTS Decision\r\n" + base.ToString();
		}
	}
}
