using System;
using System.Collections;
using System.Windows.Forms;

namespace PUC.CIO
{
	/*
	 * This comment contains a lot of sizing information for the widgets of 
	 * the .NET Compact Framework.  The information will be used for writing 
	 * PreferredSize and Minimum Size methods.
	 * 
	 * Button
	 * Default Height: 24
	 * Minimum Left/Right Pad: 5
	 * Minimum Top/Bottom Pad: 3
	 * Ascent Line: topPad
	 * Label is centered (horiz and vert)
	 * 
	 * CheckBox
	 * Default Height: 24
	 * Minimum Height: labelHeight + 1
	 * Minimum Left Pad: 20
	 * Label is centered vertically
	 * check box is 15x15
	 * 
	 * ComboBox
	 * Default (Constrained) Height: 20 (labelHeight + 6)
	 * Minimum Left Pad: 3
	 * Minimum Right Pad: 16
	 * Minimum Top Pad: 2
	 * 
	 * Label
	 * Minimum Left/Right Pad: 0
	 * Minimum Top/Bottom Pad: 0
	 * Ascent Line: top
	 * basically text is placed in upper-left corner
	 * 
	 * ListBox
	 * Line Height: Label Height
	 * Line Pad: 0
	 * Minimum Left Pad: 3
	 * Minimum Right Pad: 1
	 * 
	 * ProgressBar
	 * Default Height: 24
	 * 
	 * Radio Button
	 * Default Height: 24
	 * Minimum Height: labelHeight + 1
	 * Minimum Left Pad: 20
	 * Label is centered vertically
	 * button circle has 15 pixel diameter (would be bounded by checkbox!)
	 * 
	 * Scrollbar
	 * Default Height: 20??
	 * 
	 * TabControl
	 * Minimum Bottom Pad: 22
	 * Tab Pad (one side): 5
	 * 
	 * TextBox
	 * Default Height: 22 
	 * Minimum Top Pad: 3
	 * Minimum Bottom Pad: 5
	 * Minimum Left/Right Pad: 3
	 * 
	 * TrackBar
	 * Default/Minimum Height: 24
	 */

	/// <summary>
	/// Summary description for ConcreteInteractionObject.
	/// </summary>
	public abstract class ConcreteInteractionObject : Decorator
	{
		/*
		 * Member Variables
		 */
		protected static Hashtable _cioFactories;
		public delegate ConcreteInteractionObject CreateCIO( PUC.ApplianceObject ao );


		/*
		 * Static Constructor
		 */
		static ConcreteInteractionObject()
		{
			_cioFactories = new Hashtable();
			AddCIOFactory( ButtonLinkedCIO.CIO_NAME,
						   new CreateCIO(ButtonLinkedCIO.CreateButtonLinkedCIO) );
			AddCIOFactory( CheckboxLinkedCIO.CIO_NAME,
				           new CreateCIO(CheckboxLinkedCIO.CreateCheckboxLinkedCIO) );
			AddCIOFactory( ExplanationCIO.CIO_NAME,
				           new CreateCIO(ExplanationCIO.CreateExplanationCIO) );
			AddCIOFactory( LabelLinkedCIO.CIO_NAME,
				           new CreateCIO(LabelLinkedCIO.CreateLabelLinkedCIO) );
			AddCIOFactory( ScrollbarLinkedCIO.CIO_NAME,
						   new CreateCIO(ScrollbarLinkedCIO.CreateScrollbarLinkedCIO) );
			AddCIOFactory( SelectionListLinkedCIO.CIO_NAME,
						   new CreateCIO(SelectionListLinkedCIO.CreateSelectionListLinkedCIO) );
			AddCIOFactory( TextFieldLinkedCIO.CIO_NAME,
						   new CreateCIO(TextFieldLinkedCIO.CreateTextFieldLinkedCIO) );

			#if SMARTPHONE
				AddCIOFactory( CommandListViewItemCIO.CIO_NAME,
							   new CreateCIO(CommandListViewItemCIO.CreateCommandListViewItemCIO) );
				AddCIOFactory( StateListViewItemCIO.CIO_NAME,
							   new CreateCIO(StateListViewItemCIO.CreateStateListViewItemCIO) );
				AddCIOFactory( ExplanationListViewItemCIO.CIO_NAME,
							   new CreateCIO(ExplanationListViewItemCIO.CreateExplanationListViewItemCIO) );
			#endif
		}


		/*
		 * Constructor
		 */
		public ConcreteInteractionObject()
		{
		}


		/*
		 * Member Methods
		 */
		public virtual bool HasLabel() 
		{
			return false;
		}

		public virtual LabelCIO GetLabelCIO()
		{
			return null;
		}


		/*
		 * Static Member Methods
		 */
		public static void AddCIOFactory( String name, CreateCIO fptr )
		{
			_cioFactories[ name ] = fptr;
		}

		public static CreateCIO GetCIOFactory( String name )
		{
			return (CreateCIO)_cioFactories[ name ];
		}
	}
}
