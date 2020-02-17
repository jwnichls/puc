/*
 * This file contains objects that represent various layout
 * problems that may be encountered by the interface generator.
 * A set of rules is used to correct these problems.
 */

using System;

using PUC;

namespace PUC.UIGeneration
{
	/// <summary>
	/// The base class of any objects that describe layout
	/// problems.
	/// </summary>
	public abstract class LayoutProblem : Decorator
	{
		/*
		 * Abstract Methods
		 */
		public abstract string Name
		{
			get;
		}
	}

	public abstract class PanelSizeProblem : LayoutProblem
	{
		/* 
		 * Member Variables
		 */

		protected PanelNode _panel;

		/*
		 * Constructors
		 */

		public PanelSizeProblem( PanelNode panel )
		{
			_panel = panel;
		}


		/*
		 * Properties
		 */

		public PanelNode Panel
		{
			get
			{
				return _panel;
			}
		}
	}

	public class InsufficientHeight : PanelSizeProblem
	{
		/*
		 * Constructor
		 */

		public InsufficientHeight( PanelNode panel )
			: base( panel )
		{
		}


		/*
		 * Properties
		 */

		public override string Name
		{
			get
			{
				return "Insufficient Height";
			}
		}

	}
}
