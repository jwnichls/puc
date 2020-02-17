using System;

using PUC;
using PUC.CIO;
using PUC.Rules;
using PUC.Rules.SpecScan;
using PUC.Registry;
using PUC.Types;
using PUC.UIGeneration;


namespace PUC.Rules.UnitScan
{
	/// <summary>
	/// This rule assigns CIOs to appliance objects that are not a
	/// part of a 
	/// </summary>
	public class ListUnitFinder : UnitScanRule
	{
		/*
		 * Process Method
		 */

		public override void Process( GroupNode g, UIGenerator ui )
		{
			if (! g.IsList() )
				return;

			ListGroupNode lg = (ListGroupNode)g;

			ConcreteInteractionObject cio = 
				ui.Core.ObjectRegistry.ChooseWidget( lg );
			g.Decorations.Add( ListDecision.DECISION_KEY, new ListDecision() );

			if ( cio != null )
			{
				g.Decorations.Add( UnitDecision.DECISION_KEY, new UnitDecision( cio ) );
			}
			else
			{
				// add in some components to allow the user to scroll through elements
				// in the list and make selections (if relevant)

				ApplianceState indexState = new ListIndexState( lg.Appliance, lg );
				ObjectGroupNode idxGrp = new ObjectGroupNode( indexState );

				lg.Children.Add( idxGrp );
				idxGrp.Parent = lg;

				if ( lg.SelectionType == SelectionType.One )
				{
					lg.SelectionState.ValueChangedEvent += new PUC.ApplianceState.ValueChangedHandler(((ListIndexState)indexState).SelectionChanged);
				}

				// TODO: Implement multiple selections code
			}
		}
	}

	public class ListIndexState : ApplianceState
	{
		/*
		 * Member Variables
		 */
	
		protected ListGroupNode     _listGroup;
		protected IndexedDataWindow _dataWindow;


		/*
		 * Events
		 */

		public override event ValueChangedHandler ValueChangedEvent;


		/*
		 * Constructors
		 */

		public ListIndexState( Appliance appliance, ListGroupNode lg )
			: base( appliance, "", false )
		{
			_listGroup = lg;
			_dataWindow = lg.DataWindow;

			IntegerSpace intSpace = 
				new IntegerSpace( new IntNumber( 1 ), 
				new NumberConstraint( this, lg.LengthState ) );

			this.Type = new PUCType( intSpace );

			this.Labels = new LabelDictionary();
			this.Labels.AddLabel( new StringValue( "List Index" ) );
			this.Labels.AddLabel( new StringValue( "Index" ) );

			this.ValueChangeRequested += new ValueChangeRequestedHandler(this.changeWindowIndex);
			_dataWindow.IndexChanged += new EventHandler(WindowIndexChanged);
		}


		/*
		 * Member Methods
		 */

		protected void changeWindowIndex( ApplianceState state, object value )
		{
			object val = _type.ValueSpace.Validate( value );

			if ( val != null )
				if ( _listGroup.SelectionType == SelectionType.One )
				{
					if ( !_listGroup.SelectionState.Defined || 
						(int)val != (int)_listGroup.SelectionState.Value )
						_listGroup.SelectionState.RequestChange( val );
				}
				else
					_dataWindow.Index = (int)val;
		}

		public override object Value
		{
			get
			{
				return _dataWindow.Index;
			}
		}

		public override bool Defined
		{
			get
			{
				return ((IntegerSpace)_type.ValueSpace).GetMaximum().GetIntValue() > 0;
			}
		}

		private void WindowIndexChanged(object sender, EventArgs e)
		{
			if ( ValueChangedEvent != null )
				ValueChangedEvent( this );
		}

		public void SelectionChanged(ApplianceState s)
		{
			_dataWindow.Index = (int)s.Value;
		}
	}


	/// <summary>
	/// One result of the ListUnitFinder rule.  This decision prevents 
	/// the interface generator for using non-list rules to create a UI
	/// for variables and commands that are in a list.
	/// </summary>
	public class ListDecision : Decision
	{
		/*
		 * Constants
		 */

		public const string DECISION_KEY = "list-found";


		/*
		 * Constructor
		 */

		public ListDecision()
			: this( null )
		{
		}

		public ListDecision( Decision baseDecision )
			: base( baseDecision )
		{
		}
	}
}
