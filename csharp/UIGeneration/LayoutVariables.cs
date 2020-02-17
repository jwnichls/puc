using System;
using PUC;

namespace PUC.UIGeneration
{
	/// <summary>
	/// This is an object used to pass variables used for layout to all of
	/// the relevant components.
	/// </summary>
	public class LayoutVariables
	{
		/*
		 * Member Variables
		 */

		/// <summary>
		/// Specifies the percentage of the column that a label should
		/// occupy in a one column row.  This should be a value between 0
		/// and 1.0.
		/// </summary>
		public double OneColLabelPcnt;

		/// <summary>
		/// Specifies the percentage of the column that the first label
		/// should occupy in a two column row.  This should be a value
		/// between 0 and 1.0.
		/// </summary>
		public double TwoColLabel1Pcnt;

		/// <summary>
		/// Specifies the percentage of the column that the second label
		/// should occupy in a two column row.  This should be a value
		/// between 0 and 1.0.
		/// </summary>
		public double TwoColLabel2Pcnt;

		/// <summary>
		/// Specifies the padding between rows.
		/// </summary>
		public int RowPadding;

		/// <summary>
		/// Specifies whether the size of the panel network should be
		/// confined to the size of the screen.  Set this to false if the
		/// interface is being created in a panel that scrolls.  The H
		/// variable controls constraints in the horizontal dimension.
		/// </summary>
		public bool ConstrainPanelsToScreenH;

		/// <summary>
		/// Specifies whether the size of the panel network should be
		/// confined to the size of the screen.  Set this to false if the
		/// interface is being created in a panel that scrolls.  The V
		/// variable controls constraints in the vertical dimension.
		/// </summary>
		public bool ConstrainPanelsToScreenV;

		/// <summary>
		/// Specifies the distance between the baselines of the text in each
		/// row.
		/// </summary>
		public int BaselineSpacing;

		/// <summary>
		/// Specifies the vertical offset from the top of the panel where the 
		/// baseline starts.
		/// </summary>
		public int BaselineStart;

		/// <summary>
		/// An array list of LayoutProblem objects that are created by the 
		/// layout algorithm to mark locations where it was not able to create
		/// a proper arrangment of widgets.
		/// </summary>
		protected System.Collections.Hashtable _problems;

		/*
		 * Constructors
		 */

		public LayoutVariables( int rowPad,
								double c1LabelPcnt,
								double c2Label1Pcnt,
								double c2Label2Pcnt,
								int baselineSpacing,
								int baselineStart )
		{
			RowPadding = rowPad;
			OneColLabelPcnt = c1LabelPcnt;
			TwoColLabel1Pcnt = c2Label1Pcnt;
			TwoColLabel2Pcnt = c2Label2Pcnt;
			BaselineSpacing = baselineSpacing;
			BaselineStart = baselineStart;

			ConstrainPanelsToScreenH = true;
			ConstrainPanelsToScreenV = true;

			_problems = new System.Collections.Hashtable();
		}

		public LayoutVariables()
			: this( 3, 0.40, 0.35, 0.35, 27, 21 )
		{
		}


		/*
		 * Properties
		 */

		public System.Collections.Hashtable LayoutProblems
		{
			get
			{
				return _problems;
			}
		}
	}
}
