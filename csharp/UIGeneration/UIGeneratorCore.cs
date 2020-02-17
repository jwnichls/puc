using System;
using System.Collections;

using PUC;
using PUC.CIO;
using PUC.Communication;
using PUC.Parsers;
using PUC.Registry;
using PUC.Rules;
using PUC.Types;


namespace PUC.UIGeneration
{
	/// <summary>
	/// Generates and displays an interface from a spec.
	/// </summary>
	public class UIGeneratorCore
	{
		/*
		 * Member Variables
		 */

		protected PanelCIO       _panel;

		protected LayoutVariables _layoutVars;

		protected UIGenerator     _ui;


		protected ArrayList _rulePhases;

		protected SmartCIOManager _smartCIOManager;

		protected WidgetRegistry _widgetRegistry;


		/*
		 * Constructor
		 */

		public UIGeneratorCore( UIGenerator ui, ArrayList rulePhases, 
								SmartCIOManager smartCIOManager, 
								WidgetRegistry widgetRegistry )
		{
			_panel = new PanelCIO();
			_layoutVars = new LayoutVariables();

			_rulePhases = rulePhases;
			_smartCIOManager = smartCIOManager;
			_widgetRegistry = widgetRegistry;

			_ui = ui;
		}

		/*
		 * Properties
		 */

		public ArrayList RulePhases
		{
			get
			{
				return _rulePhases;
			}
		}

		public SmartCIOManager SmartCIOMgr
		{
			get
			{
				return _smartCIOManager;
			}
			set
			{
				_smartCIOManager = value;
			}
		}

		public WidgetRegistry ObjectRegistry
		{
			get
			{
				return _widgetRegistry;
			}
			set
			{
				_widgetRegistry = value;
			}
		}

		public LayoutVariables LayoutVars
		{
			get
			{
				return _layoutVars;
			}
		}

		public PanelCIO CIOContainer
		{
			get
			{
				return _panel;
			}
		}

		public PanelCIO Panel
		{
			get
			{
				return _panel;
			}
		}


		/*
		 * Member Methods
		 */

		public void GenerateUI( Appliance a )
		{
			/*
			 * Setup dependency listeners
			 */

			IEnumerator e = a.GetDependedObjects().GetEnumerator();
			while( e.MoveNext() )
			{
				ApplianceState state = (ApplianceState)e.Current;
				DependencyListener listen = new DependencyListener( state.GetReverseDeps() );
				state.ValueChangedEvent += new ApplianceState.ValueChangedHandler(listen.ValueChanged);
			}

			/*
			 * Call the Rule Phases, as set by the main frame at load-time,
			 * in order with the Appliance object as the first input.
			 */

			_panel.RemoveAllCIOs();

			object input = a;
			IEnumerator phaseEnum = _rulePhases.GetEnumerator();
			while ( phaseEnum.MoveNext() )
			{
				input = ((RulePhase)phaseEnum.Current).Process( input, _ui );
			}

			_panel.FinalSizeNotify();
			_ui.Invalidate();

			// compute dependencies based on initial state

			e = a.VariableTable.GetObjectEnumerator();
			while( e.MoveNext() )
				((ApplianceObject)e.Current).EvalDependencies();
		}
	}	

	/// <summary>
	/// Listener class that helps with the evaluation of dependency
	/// information for graying out controls.
	/// </summary>
	public class DependencyListener
	{
		/*
		 * Member Variables
		 */

		ArrayList _depObjs;


		/*
		 * Constructor
		 */

		public DependencyListener( ArrayList depObjs )
		{
			_depObjs = depObjs;
		}


		/*
		 * Event Handler
		 */

		public void ValueChanged( ApplianceState state )
		{
			IEnumerator e = _depObjs.GetEnumerator();
			while( e.MoveNext() )
			{
				ApplianceObject obj = (ApplianceObject)e.Current;
				obj.EvalDependencies();
			}
		}
	}
}
