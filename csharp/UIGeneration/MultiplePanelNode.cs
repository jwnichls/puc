using System;
using System.Collections;
using PUC.CIO;

namespace PUC.UIGeneration
{
	/// <summary>
	/// Summary description for MultiplePanelNode.
	/// </summary>
	public abstract class MultiplePanelNode : InterfaceNode
	{
		/*
		 * Member Variables
		 */

		protected ArrayList _panels;


		/*
		 * Constructors
		 */

		public MultiplePanelNode()
		{
			_panels = new ArrayList();
		}


		/*
		 * Properties
		 */

		public ArrayList Panels
		{
			get
			{
				return _panels;
			}
		}


		/*
		 * Member Methods
		 */

		public void AddPanel( InterfaceNode node )
		{
			_panels.Add( node );

			node.SetParent( this );
		}

		public void RemovePanel( InterfaceNode node )
		{
			_panels.Remove( node );

			node.SetParent( null );
		}


		/*
		 * InterfaceNode Methods
		 */

		public override abstract void AddComponents( ContainerCIO container, LayoutVariables vars );

		public override abstract void DoLayout( LayoutVariables vars );

		public override abstract void CalculateMinimumSize(LayoutVariables vars);
		public override abstract void CalculatePreferredSize( LayoutVariables vars );

		public override string ToString()
		{
			string ret = _bounds.ToString() + "\r\n";

			for( int i = 0; i < _panels.Count; i++ )
			{
				ret += i + " . " + _panels[ i ].ToString() + "\r\n";
			}

			return ret;
		}
	}
}
