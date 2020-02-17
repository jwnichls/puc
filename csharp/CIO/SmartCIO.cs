using System;
using System.Collections;
using System.Windows.Forms;

using PUC;


namespace PUC.CIO
{
	/// <summary>
	/// A SmartCIO is a ConcreteInteractionObject that takes a portion
	/// of the specification as input and builds a widget or set of 
	/// widgets based upon it.  Particular SmartCIO objects are assigned
	/// to portions of the tree by the "type" element of the group tree.
	/// </summary>
	public abstract class SmartCIO : ControlBasedCIO
	{
		/*
		 * Constants
		 */

		protected const string SINGLE_STATE	= "SingleState";
	

		/*
		 * SmartCIO Factory
		 */

		public delegate SmartCIO CreateSmartCIO( PUC.GroupNode group );


		/*
		 * Member Variables
		 */

		protected GroupNode			_specSnippet;

		/*
		 * The template group is where the appliance objects that will be
		 * rendered by the Smart Template are moved to.  Any remaining
		 * objects are then rendered by the user interface generator like
		 * normal.
		 * 
		 * This process means that a Smart Template must call:
		 * DoNotRenderObject( ApplianceObject ao )
		 * for every object that it intends to render.
		 */
		protected BranchGroupNode	_templateGroup;

		protected LabelDictionary	_labels;

		protected Hashtable			_objects;


		/*
		 * Constructor
		 */

		static SmartCIO()
		{
		}

		public SmartCIO( Control control, GroupNode specSnippet )
			: base( control )
		{
			_specSnippet = specSnippet;

			// attempt to find labels
			if ( !specSnippet.IsObject() )
			{
				_labels = specSnippet.Labels;
			}
			else if ( specSnippet.IsObject() )
			{
				_labels = ((ObjectGroupNode)specSnippet).Object.Labels;
			}

			// create the Template group
			_templateGroup = new BranchGroupNode();


			// extract the objects that make up this type block
			_objects = new Hashtable();
			if ( _specSnippet.IsObject() )
			{
				_objects[ SINGLE_STATE ] = ((ObjectGroupNode)specSnippet).Object;

				// determine the location of this group in its parent
				int parentIdx = _specSnippet.Parent.Children.IndexOf( _specSnippet );
				if ( parentIdx < 0 ) // this should never happen
					parentIdx = 0;
					
				// make the template group
				_specSnippet.Parent.Children.Insert( parentIdx + 1, _templateGroup );
				_templateGroup.Parent = _specSnippet.Parent;
			}
			else	
			{
				extractObjects( specSnippet );

				// make the template group
				((BranchGroupNode)specSnippet).Children.Add( _templateGroup );
				_templateGroup.Parent = (BranchGroupNode)_specSnippet;
			}
		}


		/*
		 * Properties
		 */

		public BranchGroupNode TemplateGroup
		{
			get
			{
				return _templateGroup;
			}
		}
		
		protected Hashtable Objects
		{
			get
			{
				return _objects;
			}
		}
		
		public LabelDictionary Labels
		{
			get
			{
				return _labels;
			}
		}


		/*
		 * Member Methods
		 */

		/// <summary>
		/// This method must be defined by all SmartCIO objects to provide
		/// a textual string that represents the current value of this
		/// template.  The method will be used by the Smartphone for menu
		/// titles and by read-only lists for display in a ListView.
		/// </summary>
		/// <returns>the value of this template as a string</returns>
		public abstract string GetStringValue();

		protected void doNotRenderObject( ApplianceObject ao ) 
		{
			ObjectGroupNode objGrp = null;
			
			// find the object
			if ( _specSnippet.IsObject() && 
				 ((ObjectGroupNode)_specSnippet).Object == ao )
			{
				objGrp = (ObjectGroupNode)_specSnippet;
			}
			else
			{
				// find the object group that contains ao
				objGrp = findObjectGroup( _specSnippet, ao );
			}

			objGrp.Parent.Children.Remove( objGrp );
			_templateGroup.Children.Add( objGrp );
			objGrp.Parent = _templateGroup;
		}

		private ObjectGroupNode findObjectGroup( GroupNode g, ApplianceObject ao )
		{
			// + 1 to get rid of separatorChar
			String relName = ao.FullName.Substring( g.FullPath.Length + 1 );

			String[] names = relName.Split( VariableTable.NAME_SEPARATORS );

			bool found;
			GroupNode cursor = g;
			GroupNode newG = null;

			for( int i = 0; i < names.Length; i++ )
			{
				found = false;

				IEnumerator e = ((BranchGroupNode)cursor).Children.GetEnumerator();
				while( e.MoveNext() )
				{
					newG = (GroupNode)e.Current;
					if ( newG.Name == names[ i ] )
					{
						cursor = newG;
						found = true;
						break;
					}
				}

				if ( !found )
					return null;
			}

			return (ObjectGroupNode)cursor;
		}

		public override bool HasLabel()
		{
			return _labels != null;
		}

		public override LabelCIO GetLabelCIO()
		{
			if ( _labels == null )
				return null;

			return new LabelCIO( _labels );
		}

		private void extractObjects( GroupNode group )
		{
			if ( group.IsObject() )
			{
				_objects[ ((ObjectGroupNode)group).Object.Name ] = ((ObjectGroupNode)group).Object;
			}

			if ( !group.IsObject() )
			{
				IEnumerator e = ((BranchGroupNode)group).Children.GetEnumerator();
				while( e.MoveNext() )
				{
					extractObjects( (GroupNode)e.Current );
				}
			}
		}
	}
}
