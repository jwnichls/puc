using System;
using System.Collections;
using System.Windows.Forms;

using PUC;
using PUC.CIO;
using PUC.Communication;
using PUC.Parsers;
using PUC.Registry;
using PUC.Rules;
using PUC.Types;
using PUC.UIGeneration.ListRepresentation;


namespace PUC.UIGeneration
{
	/// <summary>
	/// Generates and displays an interface from a spec.
	/// </summary>
	public class UIGenerator : Panel
	{
		/*
		 * Member Variables
		 */

		protected UIGeneratorCore _core;

		protected ListNode _listRoot;


		/*
		 * Constructor
		 */

		public UIGenerator( ArrayList rulePhases, 
			                SmartCIOManager smartCIOManager,
			                WidgetRegistry registry )
		{
			_core = new UIGeneratorCore( this, rulePhases, smartCIOManager, registry );
		}


		/*
		 * Properties
		 */

		public UIGeneratorCore Core
		{
			get
			{
				return _core;
			}
		}

		public LayoutVariables LayoutVars
		{
			get
			{
				return _core.LayoutVars;
			}
		}

		public PanelCIO CIOContainer
		{
			get
			{
				return _core.CIOContainer;
			}
		}

		public ListNode ListRoot
		{
			get
			{
				return _listRoot;
			}
			set
			{
				_listRoot = value;
			}
		}

		public PanelCIO Panel
		{
			get
			{
				return _core.Panel;
			}
		}


		/*
		 * Member Methods
		 */

		protected override void OnResize(EventArgs e)
		{
			base.OnResize(e);

			_core.Panel.GetControl().Size = this.Size;
		}

		public void GenerateUI( Appliance a )
		{
			_listRoot = new RootListNode();

			this.Controls.Add( _core.Panel.GetControl() );

			_core.GenerateUI( a );
		}
	}	
}
