using System;
using System.Collections;
using System.Diagnostics;
using System.IO;
using System.Xml;
using PUC.Types;

namespace PUC.Parsers
{
	/// <summary>
	/// Summary description for SpecParser.
	/// </summary>
	public class SpecParser22
	{
		/*
		 * Constants
		 */

		// version accepted by this parser
		public const string SPEC_PARSER_VERSION = "PUC/2.2";

		// tag constants
		public const string SPEC_TAG			= "spec";
		public const string TYPES_TAG			= "types";
		public const string GROUPINGS_TAG		= "groupings";
		public const string GROUP_TAG			= "group";
		public const string LIST_GROUP_TAG		= "list-group";
		public const string UNION_GROUP_TAG		= "union-group";
		public const string STATE_TAG			= "state";
		public const string TYPE_TAG			= "type";
		public const string PRIORITY_TAG		= "priority";
		public const string VALUE_LABEL_TAG		= "value-labels";
		public const string MAP_TAG				= "map";
		public const string BINARY_TAG			= "binary";
		public const string BOOLEAN_TAG			= "boolean";
		public const string INTEGER_TAG			= "integer";
		public const string FIXEDPT_TAG			= "fixedpt";
		public const string FLOATINGPT_TAG		= "floatingpt";
		public const string STRING_TAG			= "string";
		public const string ENUMERATED_TAG		= "enumerated";
		public const string MINIMUM_TAG			= "min";
		public const string MAXIMUM_TAG			= "max";
		public const string AVERAGE_TAG			= "average";
		public const string INCREMENT_TAG		= "incr";
		public const string POINTPOS_TAG		= "pointpos";
		public const string ACTIVEIF_TAG		= "active-if";
		public const string LABEL_TAG			= "label";
		public const string LABELS_TAG			= "labels";
		public const string COMMAND_TAG			= "command";
		public const string AND_TAG				= "and";
		public const string OR_TAG				= "or";
		public const string NOT_TAG				= "not";
		public const string EQUALS_TAG			= "equals";
		public const string LESSTHAN_TAG		= "lessthan";
		public const string GREATERTHAN_TAG		= "greaterthan";
		public const string ITEM_COUNT_TAG		= "item-count";
		public const string REFVALUE_TAG		= "ref-value";
		public const string EXPLANATION_TAG		= "explanation";
		public const string PHONETIC_TAG		= "phonetic";
		public const string TTS_TAG				= "text-to-speech";
		public const string APPLY_TYPE_TAG		= "apply-type";
		public const string DEFINED_TAG			= "defined";
		public const string UNDEFINED_TAG		= "undefined";
		public const string SELECTION_TYPE_TAG  = "selections";
		public const string CONSTANT_TAG		= "constant";
		public const string APPLY_OVER_TAG		= "apply-over";
		public const string REQUIRED_IF_TAG		= "required-if";
		public const string DEFAULT_VALUE_TAG	= "default-value";
		public const string LIST_SELECTION_TAG  = "list-selection";
		public const string TRUE_TAG			= "true";
		public const string FALSE_TAG			= "false";

		// spec xml file attribute names
		public const string NAME_ATTRIBUTE		= "name";
		public const string INDEX_ATTRIBUTE		= "index";
		public const string ACCESS_ATTRIBUTE	= "access";
		public const string IGNORE_ATTRIBUTE	= "ignore";
		public const string STATE_ATTRIBUTE		= "state";
		public const string PRIORITY_ATTRIBUTE	= "priority";
		public const string TEXT_ATTRIBUTE		= "text";
		public const string RECORDING_ATTRIBUTE = "recording";
		public const string IS_A_ATTRIBUTE		= "is-a";
		public const string VERSION_ATTRIBUTE	= "version";
		public const string TYPE_NAME_ATTRIBUTE = "type-name";
		public const string TRUE_IF_ATTRIBUTE	= "true-if";
		public const string VALUE_ATTRIBUTE		= "value";
		public const string LIST_ATTRIBUTE		= "list";
		public const string NUMBER_ATTRIBUTE	= "number";

		public const string IGNORE_ATTR_ALL		= "all";
		public const string IGNORE_ATTR_NONE	= "none";
		public const string IGNORE_ATTR_PARENT	= "parent";

		public const string ACCESS_READ_ONLY	= "read-only";
		public const string ACCESS_READ_WRITE	= "read-write";

		public const string SEL_TYPE_ONE		= "one";
		public const string SEL_TYPE_MULTIPLE	= "multiple";

		public const string TRUE_IF_ANY			= "any";
		public const string TRUE_IF_ALL			= "all";
		public const string TRUE_IF_NONE		= "none";

		public const string ATTR_VALUE_TRUE		= "true";
		public const string ATTR_VALUE_FALSE	= "false";


		/*
		 * Static Member Variables
		 */
		protected static VariableTable		_varTable;
		protected static Hashtable			_types;
		protected static ArrayList			_resolvables;
		protected static Appliance			_currentAppliance;

		
		/*
		 * Static Methods
		 */
		public static void Parse( XmlTextReader xmlStream, Appliance appl ) 
		{
			_currentAppliance = appl;

			_varTable = new VariableTable( "Unknown" );
			_types = new Hashtable();
			_resolvables = new ArrayList();

			try 
			{
				// Phase 0.  Register some pre-defined types

				LabelDictionary on = new LabelDictionary();
				on.AddLabel( new StringValue( "On" ) );
				LabelDictionary off = new LabelDictionary();
				off.AddLabel( new StringValue( "Off" ) );
				Hashtable onOffLabels = new Hashtable();
				onOffLabels.Add( true, on );
				onOffLabels.Add( false, off );
				_types.Add( "OnOffType", new PUCType( -1, new BinarySpace(), onOffLabels ) );
 
				LabelDictionary yes = new LabelDictionary();
				yes.AddLabel( new StringValue( "Yes" ) );
				LabelDictionary no = new LabelDictionary();
				no.AddLabel( new StringValue( "No" ) );
				Hashtable yesNoLabels = new Hashtable();
				yesNoLabels.Add( true, yes );
				yesNoLabels.Add( false, no );
				_types.Add( "YesNoType", new PUCType( -1, new BinarySpace(), yesNoLabels ) );

				// Phase 1.  Parse the XML

				Globals.GetFrame( _currentAppliance )
					.AddLogLine( "Parsing Specification Version " + SpecParser21.SPEC_PARSER_VERSION );
				Globals.GetFrame( _currentAppliance )
					.AddLogLine( "Phase #1. Parsing XML" );

				BranchGroupNode root = HandleSpec( xmlStream );

				// Phase 2.  Register variable names & create data window structure
				//------------------------------------------------------------------
				// This second pass is used to create the appropriate data 
				// structures that will be needed later.  These structures 
				// are created during the XML parsing in previous versions 
				// of the parser, but that no longer works with the addition
				// of the the <types> block and the ability to define object
				// types anywhere in the specification.

				setupDataStructure( root, _varTable, _varTable );

				// Phase 3.  Remove Dependency Hierarchy
				//---------------------------------------
				// This second pass is used to clean up the parsing of 
				// dependencies.  So far, no group-level active-if blocks
				// have been integrated into the states within that group.

				Globals.GetFrame( _currentAppliance ).AddLogLine( "Phase #2: Fix Dependencies" );

				fixDepend( root, null, null );

				// Phase 4.  Resolve Names and Create Reverse Dependency Lists
				//-------------------------------------------------------------
				// The third pass does further work to clean up the dependencies.
				// So far, no dependency is associated with its state object (just
				// a string name), and no state knows anything about which objects
				// are dependent upon it.  This pass also checks that the values 
				// associated with each dependency have the correct type.

				Globals.GetFrame( _currentAppliance ).AddLogLine( "Phase #3: Resolve Names and Create Reverse Dependency Lists" );

				Hashtable dlist = new Hashtable();
				IEnumerator e = _varTable.GetObjectEnumerator();

				while( e.MoveNext() )
				{
					ApplianceObject ao = (ApplianceObject)e.Current;
					
					resolveNames( ao.Dependencies, dlist, _varTable, ao );
				}

				ArrayList reverseDepList = new ArrayList();
				e = dlist.Values.GetEnumerator();
				while( e.MoveNext() )
				{
					ApplianceState s = (ApplianceState)e.Current;

					int dcount = s.GetReverseDependencyCount();
					bool found = false;

					for( int i = 0; i < reverseDepList.Count; i++ )
						if ( dcount >= ((ApplianceState)reverseDepList[i]).GetReverseDependencyCount() )
						{
							reverseDepList.Insert( i, s );
							found = true;
							break;
						}

					if (! found )
						reverseDepList.Add( s );
				}

				// Phase 5.  Validate All Constraint Objects
				//-------------------------------------------
				// So far, no constraint objects have references to the states
				// whose values they depend upon.  This phase resolves those
				// names and validates each constraint.

				Globals.GetFrame( _currentAppliance ).AddLogLine( "Phase #4: Validate Constraint Objects" );

				e = _resolvables.GetEnumerator();
				while( e.MoveNext() )
				{
					IResolvable c = (IResolvable)e.Current;

					if ( c.ResolveObject( _varTable ) )
					{
						if ( c is IConstraint )
						{
							IEnumerator en = ((IConstraint)c).States.GetEnumerator();
							while( en.MoveNext() )
								((ApplianceState)en.Current).MakeConstraintVariable();
						}
					}
				}

				Globals.GetFrame( _currentAppliance ).AddLogLine( "Specification Parse Complete." );

				appl.SetParseVariables( root, _varTable, reverseDepList );
			}
			catch( SpecParseException spe )
			{
				Globals.GetFrame( _currentAppliance ).AddLogLine( "Parse Exception Occurred!" );
				Globals.GetFrame( _currentAppliance ).AddLogLine( "  " + spe.Message + "  Line: " + spe.LineNumber );
			}
			catch( Exception e )
			{
				Globals.GetFrame( _currentAppliance ).AddLogLine( "Exception Occurred!" );
				Globals.GetFrame( _currentAppliance ).AddLogLine( "  " + e.Message );
			}
			finally
			{
				_currentAppliance = null;
					
				if ( xmlStream != null )
					xmlStream.Close();
			}
		}

		private static void setupDataStructure( BranchGroupNode g, IBranchDataWindow win, VariableTable varTable )
		{
			if ( g is ListGroupNode )
			{
				IndexedDataWindow iwin = new IndexedDataWindow( (ListGroupNode)g );
				win.AddChildWindow( iwin );
				((ListGroupNode)g).DataWindow = iwin;

				varTable.RegisterListGroup( g.FullPath, (ListGroupNode)g );
	
				((ListGroupNode)g).CreateExtraStates( varTable, win );

				win = iwin;
			}
			else if ( g is UnionGroupNode )
			{
				((UnionGroupNode)g).CreateExtraStates( _currentAppliance, varTable );
			}

			IEnumerator child = g.Children.GetEnumerator();
			while( child.MoveNext() )
			{
				if ( child.Current is ObjectGroupNode )
				{
					ApplianceObject obj = ((ObjectGroupNode)child.Current).Object;
					
					varTable.RegisterObject( obj.MakeFullName( g.FullPath ), obj );

					if ( obj.Labels != null )
						obj.Labels.RegisterConstraints( obj );

					if ( obj is ApplianceState )
					{
						win.AddChildWindow( (ApplianceState)obj );
						((ApplianceState)obj).Type.RegisterConstraints( (ApplianceState)obj );
					}
				}
				else if ( child.Current is BranchGroupNode )
				{
					setupDataStructure( (BranchGroupNode)child.Current, win, varTable );
				}
				else
					throw new SpecParseException( g.LineNumber, "Encountered unexpected object of type " + child.Current.GetType().Name + " when parsing group tree." );
			}
		}

		private static void fixDepend( GroupNode group,
									   Dependency fromroot,
									   Dependency fromparent )
		{
			if ( group.IsObject() )
			{
				ObjectGroupNode objgrp = (ObjectGroupNode)group;

				AND a = new AND();

				a.AddDependency( objgrp.Object.Dependencies );
				
				if ( group.Parent._depignore != GroupNode.IGNORE_ALL )
					a.AddDependency( fromroot );

				if ( group.Parent._depignore != GroupNode.IGNORE_PARENT )
					a.AddDependency( fromparent );

				objgrp.Object.Dependencies = a.Simplify();

				return;
			}

			BranchGroupNode branchgrp = (BranchGroupNode)group;

			Dependency newRoot = null;

			if ( group._depignore != GroupNode.IGNORE_ALL )
			{
				newRoot = new AND();

				((Formula)newRoot).AddDependency( fromroot );

				if ( group._depignore < GroupNode.IGNORE_PARENT )
					((Formula)newRoot).AddDependency( fromparent );

				newRoot = newRoot.Simplify();
			}

			IEnumerator e = branchgrp.Children.GetEnumerator();
			while( e.MoveNext() )
				fixDepend( (GroupNode)e.Current, newRoot, group._dependencies );
		}

		private static void resolveNames( Dependency df,
										  Hashtable dstates,
										  VariableTable varTable,
										  ApplianceObject ao )
		{
			if ( df == null ) return;

			if ( df is ApplyOver )
				((ApplyOver)df).ResolveObject( varTable );

			if ( df is Formula )
			{
				IEnumerator e = ((Formula)df).GetDependencies();
				while( e.MoveNext() )
					resolveNames( (Dependency)e.Current, dstates, varTable, ao );
			}
			else if ( df is StateDependency )
			{
				StateDependency d = (StateDependency)df;

				if (! d.ResolveObject( varTable ) )
					throw new SpecParseException( d.LineNumber, "Problem resolving a dependency state name: " + d.StateName );
	
				ApplianceState s = d.State;

				s.AddReverseDependency( ao );
				dstates[ s.Name ] = s;

				if ( d.HasValue() && ((StateValueDependency)d).IsReference )
				{
					s = ((StateValueDependency)d).State;

					s.AddReverseDependency( ao );
					dstates[ s.Name ] = s;
				}
			}

			if ( df is ApplyOver )
				((ApplyOver)df).FixDataWindows();
		}


		/// <summary>
		/// Handles the parsing of elements contained within the SPEC_TAG
		/// </summary>
		/// <param name="xml">the XmlTextReader to read from</param>
		private static BranchGroupNode HandleSpec( XmlTextReader xml )
		{
			_varTable.SpecName = xml.GetAttribute( NAME_ATTRIBUTE );

			if ( _varTable.SpecName == null )
				throw new SpecParseException( xml.LineNumber, "The name attribute is required for every specification." );

			string version = xml.GetAttribute( VERSION_ATTRIBUTE );
			if ( version == null || version != SPEC_PARSER_VERSION )
				throw new SpecParseException( xml.LineNumber, "The parser does not support this specification language version." );

			BranchGroupNode root = null;
			LabelDictionary applianceLabels = null;

			while( xml.Read() )
				if ( xml.NodeType == XmlNodeType.Element ) 
				{
					switch( xml.Name ) 
					{
						case GROUPINGS_TAG:
							root = HandleGroupings( xml );
							break;

						case LABELS_TAG:
							applianceLabels = HandleLabelDict( xml, LABELS_TAG );
							break;

						case TYPES_TAG:
							HandleTypes( xml );
							break;
					}
				}

			if ( root == null )
				throw new SpecParseException( xml.LineNumber, "No groups defined in specification." );

			if ( applianceLabels == null )
				throw new SpecParseException( xml.LineNumber, "No human-readable labels defined for this appliance." );
			
			root.Labels = applianceLabels;

			return root;
		}

		/// <summary>
		/// Handles the parsing of the types section.
		/// </summary>
		/// <param name="xml">the XMLTextReader reading a valid spec</param>
		private static void HandleTypes( XmlTextReader xml )
		{
			while( xml.Read() )
			{
				if ( xml.NodeType == XmlNodeType.Element )
				{
					switch( xml.Name )
					{
						case GROUP_TAG:
							BranchGroupNode g = new BranchGroupNode();
							HandleGroup( xml, g, GROUP_TAG, true );
							break;

						case LIST_GROUP_TAG:
							ListGroupNode lg = new ListGroupNode( _currentAppliance );
							HandleGroup( xml, lg, LIST_GROUP_TAG, true );
							break;

						case UNION_GROUP_TAG:
							UnionGroupNode ug = new UnionGroupNode();
							HandleGroup( xml, ug, UNION_GROUP_TAG, true );
							break;

						case STATE_TAG:
							ApplianceState state = HandleState( xml );
							break;

						case COMMAND_TAG:
							ApplianceCommand cmd = HandleCommand( xml );
							break;

						case EXPLANATION_TAG:
							ApplianceExplanation expl = HandleExplanation( xml );
							break;

						case TYPE_TAG:
							PUCType type = HandleType( xml, true );
							break;

						default:
							throw new SpecParseException( xml.LineNumber, "Unexcepted element: " + xml.Name );
					}
				}
				else if ( xml.NodeType == XmlNodeType.EndElement )
				{
					if ( xml.Name == TYPES_TAG )
						return;
				}
			}
		}

		private static BranchGroupNode HandleGroupings( XmlTextReader xml )
		{
			BranchGroupNode root = new BranchGroupNode();
			root.Name = _varTable.SpecName;

			HandleGroup( xml, root, GROUPINGS_TAG, false );

			if ( root.Count <= 0 )
				throw new SpecParseException( xml.LineNumber, "No groups defined within groupings tag." );

			return root;
		}

		private static BranchGroupNode HandleGroup( XmlTextReader xml, BranchGroupNode group, string endTag, bool inTypesSection )
		{
			group.LineNumber = xml.LineNumber;

			Hashtable nameChecker = new Hashtable();

			if ( endTag != GROUPINGS_TAG )
			{
				string name = xml.GetAttribute( NAME_ATTRIBUTE );
				if ( name == null && !inTypesSection )
					throw new SpecParseException( xml.LineNumber, "group tag found with no name." );
				if ( name == _varTable.SpecName )
					throw new SpecParseException( xml.LineNumber, "group name must be different than specification name." );
				group.Name = name;

				try 
				{
					string pstr = xml.GetAttribute( PRIORITY_ATTRIBUTE );
					if ( pstr != null )
					{
						group.Priority = Int32.Parse( pstr );

						if ( group.Priority < 0 || group.Priority > 10 )
							throw new NotSupportedException();
					}
				}
				catch( Exception e )
				{
					throw new SpecParseException( xml.LineNumber, PRIORITY_ATTRIBUTE + " attribute must have numeric value of 0-10", e );
				}

				string type = xml.GetAttribute( IS_A_ATTRIBUTE );
				if ( type != null )
					group.HighlevelType = type;

				if ( group is UnionGroupNode )
				{
					string access = xml.GetAttribute( ACCESS_ATTRIBUTE );
					if ( access != null && access != ACCESS_READ_ONLY && access != ACCESS_READ_WRITE )
						throw new SpecParseException( xml.LineNumber, ACCESS_ATTRIBUTE + " must be one of these values: " + ACCESS_READ_ONLY + ", " + ACCESS_READ_WRITE );
					
					((UnionGroupNode)group).ReadOnly = (access == ACCESS_READ_ONLY);
				}

				string typeName = xml.GetAttribute( TYPE_NAME_ATTRIBUTE );
				if ( typeName == null && inTypesSection )
					throw new SpecParseException( xml.LineNumber, "Elements within the " + TYPES_TAG + " element must contain a " + TYPE_NAME_ATTRIBUTE + " attribute." );
				HandleObjectType( typeName, group );
			}

			// Globals.GetFrame( _currentAppliance ).AddLogLine( "Group found..." + name );

			GroupNode g = null;
			ListGroupNode lg = null;
			while( xml.Read() )
				if ( xml.NodeType == XmlNodeType.Element ) 
				{
					switch( xml.Name ) 
					{
						case GROUP_TAG:
							g = new BranchGroupNode();
							group.Children.Add( g );
							g.Parent = group;

							HandleGroup( xml, (BranchGroupNode)g, GROUP_TAG, false );

							if ( nameChecker[ g.Name ] != null )
								throw new SpecParseException( g.LineNumber, "Group " + g.Name + " has same name as another element under parent " + group.Name + " at line " + ((GroupNode)nameChecker[ g.Name ]).LineNumber );
							nameChecker[ g.Name ] = g;

							break;

						case LIST_GROUP_TAG:
							g = new ListGroupNode( _currentAppliance );
							group.Children.Add( g );
							g.Parent = group;

							HandleGroup( xml, (BranchGroupNode)g, LIST_GROUP_TAG, false );

							if ( nameChecker[ g.Name ] != null )
								throw new SpecParseException( g.LineNumber, "Group " + g.Name + " has same name as another element under parent " + group.Name + " at line " + ((GroupNode)nameChecker[ g.Name ]).LineNumber );
							nameChecker[ g.Name ] = g;

							break;

						case SELECTION_TYPE_TAG:
							if ( !( group is ListGroupNode ) )
								throw new SpecParseException( xml.LineNumber, SELECTION_TYPE_TAG + " element is only allowed within " + LIST_GROUP_TAG + " elements." );
							
							string typeName = xml.GetAttribute( NUMBER_ATTRIBUTE );
							if ( typeName == null )
								throw new SpecParseException( xml.LineNumber, SELECTION_TYPE_TAG + " tag requires " + TYPE_NAME_ATTRIBUTE + " attribute."  );

							switch( typeName )
							{
								case SEL_TYPE_ONE:
									((ListGroupNode)group).SelectionType = SelectionType.One;
									break;

								case SEL_TYPE_MULTIPLE:
									((ListGroupNode)group).SelectionType = SelectionType.Multiple;
									break;

								default:
									throw new SpecParseException( xml.LineNumber, typeName + " is not one of " + SEL_TYPE_ONE + " or " + SEL_TYPE_MULTIPLE + "." );
							}

							string access = xml.GetAttribute( ACCESS_ATTRIBUTE );
							if ( access != null && access != ACCESS_READ_ONLY && access != ACCESS_READ_WRITE )
								throw new SpecParseException( xml.LineNumber, ACCESS_ATTRIBUTE + " must be one of these values: " + ACCESS_READ_ONLY + ", " + ACCESS_READ_WRITE );
							((ListGroupNode)group).SelectionReadOnly = (access == ACCESS_READ_ONLY);

							break;

						case MINIMUM_TAG:
							if ( !( group is ListGroupNode ) )
								throw new SpecParseException( xml.LineNumber, SELECTION_TYPE_TAG + " element is only allowed within " + LIST_GROUP_TAG + " elements." );

							lg = (ListGroupNode)group;

							if ( lg.ItemCount != null )
								throw new SpecParseException( xml.LineNumber, MINIMUM_TAG + " cannot be specified with " + ITEM_COUNT_TAG + " in a " + LIST_GROUP_TAG + " element." );

							try
							{
								lg.Minimum = extractNumber( xml, MINIMUM_TAG );
							}
							catch( Exception )
							{
								throw new SpecParseException( xml.LineNumber, MINIMUM_TAG + " element must contain a number." );
							}
							break;

						case MAXIMUM_TAG:
							if ( !( group is ListGroupNode ) )
								throw new SpecParseException( xml.LineNumber, SELECTION_TYPE_TAG + " element is only allowed within " + LIST_GROUP_TAG + " elements." );

							lg = (ListGroupNode)group;

							if ( lg.ItemCount != null )
								throw new SpecParseException( xml.LineNumber, MAXIMUM_TAG + " cannot be specified with " + ITEM_COUNT_TAG + " in a " + LIST_GROUP_TAG + " element." );

							try
							{
								lg.Maximum = extractNumber( xml, MAXIMUM_TAG );
							}
							catch( Exception )
							{
								throw new SpecParseException( xml.LineNumber, MAXIMUM_TAG + " element must contain a number." );
							}
							break;

						case ITEM_COUNT_TAG:
							if ( !( group is ListGroupNode ) )
								throw new SpecParseException( xml.LineNumber, SELECTION_TYPE_TAG + " element is only allowed within " + LIST_GROUP_TAG + " elements." );

							lg = (ListGroupNode)group;

							if ( lg.Minimum != null || lg.Maximum != null )
								throw new SpecParseException( xml.LineNumber, ITEM_COUNT_TAG + " cannot be specified with " + MINIMUM_TAG + " or " + MAXIMUM_TAG + " elements in a " + LIST_GROUP_TAG + " element." );

							try
							{
								lg.ItemCount = extractNumber( xml, ITEM_COUNT_TAG );
							}
							catch( Exception )
							{
								throw new SpecParseException( xml.LineNumber, ITEM_COUNT_TAG + " element must contain a number." );
							}
							break;

						case UNION_GROUP_TAG:
							g = new UnionGroupNode();
							group.Children.Add( g );
							g.Parent = group;

							HandleGroup( xml, (BranchGroupNode)g, UNION_GROUP_TAG, false );

							if ( nameChecker[ g.Name ] != null )
								throw new SpecParseException( g.LineNumber, "Group " + g.Name + " has same name as another element under parent " + group.Name + " at line " + ((GroupNode)nameChecker[ g.Name ]).LineNumber );
							nameChecker[ g.Name ] = g;

							break;

						case ACTIVEIF_TAG:
							group._dependencies = HandleActiveIf( xml, group );
							break;

						case LABELS_TAG:
							group.Labels = HandleLabelDict( xml, LABELS_TAG );
							break;

						case COMMAND_TAG:
							ApplianceCommand cmd = HandleCommand( xml );

							if ( nameChecker[ cmd.Name ] != null )
								throw new SpecParseException( cmd.LineNumber, "Command " + cmd.Name + " has same name as another element under parent " + group.Name + " at line " + ((GroupNode)nameChecker[ cmd.Name ]).LineNumber );
							nameChecker[ cmd.Name ] = cmd;

							g = new ObjectGroupNode( cmd );
							group.Children.Add( g );
							g.Parent = group;
							break;

						case EXPLANATION_TAG:
							ApplianceExplanation expl = HandleExplanation( xml );

							if ( nameChecker[ expl.Name ] != null )
								throw new SpecParseException( expl.LineNumber, "Command " + expl.Name + " has same name as another element under parent " + group.Name + " at line " + ((GroupNode)nameChecker[ expl.Name ]).LineNumber );
							nameChecker[ expl.Name ] = expl;

							g = new ObjectGroupNode( expl );
							group.Children.Add( g );
							g.Parent = group;
							break;

						case STATE_TAG:
							ApplianceState state = HandleState( xml );

							if ( nameChecker[ state.Name ] != null )
								throw new SpecParseException( state.LineNumber, "Command " + state.Name + " has same name as another element under parent " + group.Name + " at line " + ((GroupNode)nameChecker[ state.Name ]).LineNumber );
							nameChecker[ state.Name ] = state;

							g = new ObjectGroupNode( state );
							group.Children.Add( g );
							g.Parent = group;
							break;

						case APPLY_TYPE_TAG:
							string applyTypeName = xml.GetAttribute( TYPE_NAME_ATTRIBUTE );

							if ( applyTypeName == null )
								throw new SpecParseException( xml.LineNumber, APPLY_TYPE_TAG + " must have a " + TYPE_NAME_ATTRIBUTE + " attribute specified." );

							Object type = GetObjectType( applyTypeName, xml.LineNumber );

							string aTname = xml.GetAttribute( NAME_ATTRIBUTE );
							string aTaccess = xml.GetAttribute( ACCESS_ATTRIBUTE );
							if ( aTaccess != null && aTaccess != ACCESS_READ_ONLY && aTaccess != ACCESS_READ_WRITE )
								throw new SpecParseException( xml.LineNumber, ACCESS_ATTRIBUTE + " must be one of these values: " + ACCESS_READ_ONLY + ", " + ACCESS_READ_WRITE );
							bool aTnowrite = (aTaccess == ACCESS_READ_ONLY);
							string aTpstr = xml.GetAttribute( PRIORITY_ATTRIBUTE );
							int aTpriority = -1;
							try 
							{
								if ( aTpstr != null )
								{
									aTpriority = Int32.Parse( aTpstr );

									if ( aTpriority < 0 || aTpriority > 10 )
										throw new NotSupportedException();
								}
							}
							catch( Exception e )
							{
								throw new SpecParseException( xml.LineNumber, PRIORITY_ATTRIBUTE + " attribute must have numeric value of 0-10", e );
							}

							GroupNode ng = null;
							if ( type is BranchGroupNode )
							{
								ng = (BranchGroupNode)type;

								if ( aTname != null )
									ng.Name = aTname;
								else if ( ng.Name == null )
									throw new SpecParseException( xml.LineNumber, applyTypeName + " does not define a default name and the " + APPLY_TYPE_TAG + " does not define a name." );

								if ( aTaccess != null )
								{
									if ( ng is UnionGroupNode )
										((UnionGroupNode)ng).ReadOnly = aTnowrite;
									else
										throw new SpecParseException( xml.LineNumber, applyTypeName + " defines a type where the " + ACCESS_ATTRIBUTE + " defined by the " + APPLY_TYPE_TAG + " element is invalid." );
								}	

								if ( aTpstr != null )
									ng.Priority = aTpriority;
							}
							else if ( type is ApplianceObject )
							{
								ApplianceObject obj = (ApplianceObject)type;
								if ( aTname != null )
									obj.Name = aTname;
								else if ( obj.Name == null )
									throw new SpecParseException( xml.LineNumber, applyTypeName + " does not define a default name and the " + APPLY_TYPE_TAG + " does not define a name." );

								if ( aTaccess != null )
								{
									if ( obj is ApplianceState )
										((ApplianceState)obj).ReadOnly = aTnowrite;
									else
										throw new SpecParseException( xml.LineNumber, applyTypeName + " defines a type where the " + ACCESS_ATTRIBUTE + " defined by the " + APPLY_TYPE_TAG + " element is invalid." );
								}	

								if ( aTpstr != null )
									obj.Priority = aTpriority;
							
								ng = new ObjectGroupNode( obj );
							}
							else
								throw new SpecParseException( xml.LineNumber, applyTypeName + " references a type that cannot be applied on line " + xml.LineNumber );

							group.Children.Add( ng );
							ng.Parent = group;

							break;
					}
				}
				else if ( xml.NodeType == XmlNodeType.EndElement )
				{
					if ( xml.Name == endTag )
						return group;
				}

			return group;
		}

		private static PUC.Types.IPUCNumber extractNumber( XmlTextReader xml, string endTag )
		{
			IPUCNumber value = null;

			while( xml.Read() )
			{
				if ( xml.NodeType == XmlNodeType.EndElement && xml.Name == endTag )
					return value;
				else if ( xml.NodeType == XmlNodeType.Element )
				{
					if ( value != null )
						throw new SpecParseException( xml.LineNumber, "Only one of " + CONSTANT_TAG + " or " + REFVALUE_TAG + " may be included within " + endTag );

					switch( xml.Name )
					{
						case REFVALUE_TAG:
							string statestr = xml.GetAttribute( STATE_ATTRIBUTE );

							if ( statestr == null )
								throw new SpecParseException( xml.LineNumber, REFVALUE_TAG + " element must contain a " + STATE_ATTRIBUTE + " attribute." );
							
							value = new NumberConstraint( statestr );
							_resolvables.Add( value );
							break;

						case CONSTANT_TAG:
							string valuestr = xml.GetAttribute( VALUE_ATTRIBUTE );

							if ( valuestr == null )
								throw new SpecParseException( xml.LineNumber, CONSTANT_TAG + " element must contain a " + VALUE_ATTRIBUTE + " attribute." );
							
							try
							{
								// try to read an integer first
								value = new IntNumber( valuestr );
							}
							catch( Exception )
							{
								// if that doesn't work, try a double
								value = new DoubleNumber( valuestr );
							}
							break;
					}
				}
			}

			return value;
		}

		private static Dependency HandleActiveIf( XmlTextReader xml, GroupNode g )
		{
			string ignore = xml.GetAttribute( IGNORE_ATTRIBUTE );
			if ( ignore != null )
			{
				switch( ignore )
				{
					case IGNORE_ATTR_ALL:
						g._depignore = GroupNode.IGNORE_ALL;
						break;
					case IGNORE_ATTR_PARENT:
						g._depignore = GroupNode.IGNORE_PARENT;
						break;
					case IGNORE_ATTR_NONE:
						g._depignore = GroupNode.NO_IGNORE;
						break;

					default:
						throw new SpecParseException( xml.LineNumber, IGNORE_ATTRIBUTE + " has an improper value." );
				}
			}

			return HandleActiveIf( xml );
		}

		private static void HandleFormula( XmlTextReader xml,
										   string returntag,
										   Formula df )
		{
			while( xml.Read() )
			{
				if ( df is NOT && df.Count > 1 )
					throw new SpecParseException( xml.LineNumber, "The NOT formula may only include one formula or dependency." );

				if ( xml.NodeType == XmlNodeType.Element )
				{
					if ( xml.Name == AND_TAG ) 
					{
						Formula n = new AND();
						df.AddDependency( n );
						HandleFormula( xml, AND_TAG, n );
					}
					else if ( xml.Name == OR_TAG )
					{
						Formula n = new OR();
						df.AddDependency( n );
						HandleFormula( xml, OR_TAG, n );
					}
					else if ( xml.Name == NOT_TAG )
					{
						if ( df is NOT )
							throw new SpecParseException( xml.LineNumber, "The NOT formula may not contain a NOT formula." );

						Formula n = new NOT();
						df.AddDependency( n );
						HandleFormula( xml, NOT_TAG, n );
					}
					else if ( xml.Name == APPLY_OVER_TAG )
					{
						string trueIf = xml.GetAttribute( TRUE_IF_ATTRIBUTE );
						if ( trueIf != null && 
							 trueIf != TRUE_IF_ANY &&
							 trueIf != TRUE_IF_ALL && 
							 trueIf != TRUE_IF_NONE )
							throw new SpecParseException( xml.LineNumber, TRUE_IF_ATTRIBUTE + " attribute does not one of the valid strings." );

						string list = xml.GetAttribute( LIST_ATTRIBUTE );
						if ( list == null )
							throw new SpecParseException( xml.LineNumber, LIST_ATTRIBUTE + " must be specified for the " + APPLY_OVER_TAG + " element." );

						ApplyOver.ApplyType type = ApplyOver.ApplyType.Any;
						if ( trueIf != null && trueIf == TRUE_IF_ALL )
							type = ApplyOver.ApplyType.All;
						else if ( trueIf != null && trueIf == TRUE_IF_NONE )
							type = ApplyOver.ApplyType.None;

						Formula n = new ApplyOver( list, xml.LineNumber, type );
						df.AddDependency( n );
						HandleFormula( xml, APPLY_OVER_TAG, n );
					}
					else if ( xml.Name == TRUE_TAG )
						df.AddDependency( new TrueDependency() );
					else if ( xml.Name == FALSE_TAG )
						df.AddDependency( new FalseDependency() );
					else if ( xml.Name == EQUALS_TAG )
						df.AddDependency( HandleDependency( xml, EQUALS_TAG ) );
					else if ( xml.Name == GREATERTHAN_TAG )
						df.AddDependency( HandleDependency( xml, GREATERTHAN_TAG ) );
					else if ( xml.Name == LESSTHAN_TAG )
						df.AddDependency( HandleDependency( xml, LESSTHAN_TAG ) );
					else if ( xml.Name == DEFINED_TAG )
						df.AddDependency( HandleDependency( xml, DEFINED_TAG ) );
					else if ( xml.Name == UNDEFINED_TAG )
						df.AddDependency( HandleDependency( xml, UNDEFINED_TAG ) );
				}
				else if ( xml.NodeType == XmlNodeType.EndElement )
				{
					if ( xml.Name == returntag )
						return;
				}
			}
		}

		private static Dependency HandleActiveIf( XmlTextReader xml )
		{
			Formula df = new AND();

			HandleFormula( xml, ACTIVEIF_TAG, df );

			return df;
		}

		private static Dependency HandleDependency( XmlTextReader xml, string tag )
		{
			string state = xml.GetAttribute( STATE_ATTRIBUTE );
			if ( state == null )
				throw new FormatException( "Dependency must define a state at line " + 
											xml.LineNumber );
			string val = "";
			bool isRef = false;

			if ( tag != DEFINED_TAG && tag != UNDEFINED_TAG )
				while( xml.Read() )
					if ( xml.NodeType == XmlNodeType.Element )
					{
						if ( xml.Name == REFVALUE_TAG )
						{
							val = xml.GetAttribute( STATE_ATTRIBUTE );

							if ( val == null )
								throw new SpecParseException( xml.LineNumber, REFVALUE_TAG + " element must contain a " + STATE_ATTRIBUTE + " attribute." );

							isRef = true;

							// needs to break while loop
							break;
						}
						else if ( xml.Name == CONSTANT_TAG )
						{
							val = xml.GetAttribute( VALUE_ATTRIBUTE );

							if ( val == null )
								throw new SpecParseException( xml.LineNumber, CONSTANT_TAG + " element must contain a " + VALUE_ATTRIBUTE + " attribute." );

							isRef = false;

							// needs to break while loop
							break;
						}
					}

			// PUCFrame.GlobalLogBox.AppendText( "Dep Value: " + val );

			Dependency d = null;
			switch( tag )
			{
				case EQUALS_TAG:
					d = new EqualsDependency( state, val, isRef, xml.LineNumber );
					break;
				case GREATERTHAN_TAG:
					d = new GreaterThanDependency( state, val, isRef, xml.LineNumber );
					break;
				case LESSTHAN_TAG:
					d = new LessThanDependency( state, val, isRef, xml.LineNumber );
					break;
				case DEFINED_TAG:
					d = new WholeSetDependency( state, xml.LineNumber );
					break;
				case UNDEFINED_TAG:
					d = new UndefinedDependency( state, xml.LineNumber );
					break;
			}

			return d;
		}

		private static LabelDictionary HandleLabelDict( XmlTextReader xml, 
														string returntag )
		{
			LabelDictionary labels = new LabelDictionary();
			bool lookingForText = false;

			while( xml.Read() )
				if ( xml.NodeType == XmlNodeType.Element )
				{
					if ( xml.Name == LABEL_TAG )
					{
						lookingForText = true;
					}
					else if ( xml.Name == REFVALUE_TAG )
					{
						string statestr = xml.GetAttribute( STATE_ATTRIBUTE );
						if ( statestr == null )
							throw new FormatException( "Reference string must define a state to reference to." );

						StringConstraint sc = new StringConstraint( statestr );
						_resolvables.Add( sc );
						labels.AddLabel( sc );
						lookingForText = false;
					}
				}
				else if ( xml.NodeType == XmlNodeType.Text ) 
				{
					if ( lookingForText ) 
					{
						labels.AddLabel( new PUC.Types.StringValue( xml.Value ) );
						lookingForText = false;
					}
				}
				else if ( xml.NodeType == XmlNodeType.EndElement )
				{
					if ( xml.Name == returntag )
						return labels;
				}

			return labels;
		}

		private static ApplianceCommand HandleCommand( XmlTextReader xml ) 
		{
			string name = xml.GetAttribute( NAME_ATTRIBUTE );
			if ( name == _varTable.SpecName )
				throw new SpecParseException( xml.LineNumber, "command may not have same name as specification." );

			string highlevelType = xml.GetAttribute( IS_A_ATTRIBUTE );

			int priority = -1;
			try 
			{
				string pstr   = xml.GetAttribute( PRIORITY_ATTRIBUTE );
				if ( pstr != null )
				{
					priority = Int32.Parse( pstr );

					if ( priority < 0 || priority > 10 )
						throw new NotSupportedException();
				}
			}
			catch( Exception e )
			{
				throw new SpecParseException( xml.LineNumber, PRIORITY_ATTRIBUTE + " attribute must have numeric value of 0-10", e );
			}

			// PUCFrame.GlobalLogBox.AppendText( "Found Command: " + name );

			ApplianceCommand cmd = new ApplianceCommand( _currentAppliance, name, priority );
			cmd.SetNetworkHandler();

			cmd.HighlevelType = highlevelType;
			
			string typeName = xml.GetAttribute( TYPE_NAME_ATTRIBUTE );
			HandleObjectType( typeName, cmd );

			while( xml.Read() )
				if ( xml.NodeType == XmlNodeType.Element ) 
				{
					if ( xml.Name == ACTIVEIF_TAG )
						cmd.Dependencies = HandleActiveIf( xml );
					else if ( xml.Name == LABELS_TAG )
						cmd.Labels = HandleLabelDict( xml, LABELS_TAG );
				}
				else if ( xml.NodeType == XmlNodeType.EndElement )
				{
					if ( xml.Name == COMMAND_TAG )
						return cmd;
				}


			return cmd;
		}

		private static ApplianceExplanation HandleExplanation( XmlTextReader xml ) 
		{
			string name = xml.GetAttribute( NAME_ATTRIBUTE );
			if ( name == _varTable.SpecName )
				throw new SpecParseException( xml.LineNumber, "explanation may not have same name as specification." );
			
			int priority = -1;
			try 
			{
				string pstr   = xml.GetAttribute( PRIORITY_ATTRIBUTE );
				if ( pstr != null )
				{
					priority = Int32.Parse( pstr );

					if ( priority < 0 || priority > 10 )
						throw new NotSupportedException();
				}
			}
			catch( Exception e )
			{
				throw new SpecParseException( xml.LineNumber, PRIORITY_ATTRIBUTE + " attribute must have numeric value of 0-10", e );
			}

			// PUCFrame.GlobalLogBox.AppendText( "Found Explanation: " + name );

			ApplianceExplanation expl = new ApplianceExplanation( _currentAppliance, name, priority );

			string typeName = xml.GetAttribute( TYPE_NAME_ATTRIBUTE );
			HandleObjectType( typeName, expl );

			while( xml.Read() )
				if ( xml.NodeType == XmlNodeType.Element ) 
				{
					if ( xml.Name == ACTIVEIF_TAG )
						expl.Dependencies = HandleActiveIf( xml );
					else if ( xml.Name == LABELS_TAG )
						expl.Labels = HandleLabelDict( xml, LABELS_TAG );
				}
				else if ( xml.NodeType == XmlNodeType.EndElement )
				{
					if ( xml.Name == EXPLANATION_TAG )
						return expl;
				}

			return expl;
		}

		private static ApplianceState HandleState( XmlTextReader xml ) 
		{
			string name   = xml.GetAttribute( NAME_ATTRIBUTE );
			if ( name == _varTable.SpecName )
				throw new SpecParseException( xml.LineNumber, "state may not have same name as specification." );

			string access = xml.GetAttribute( ACCESS_ATTRIBUTE );
			if ( access != null && access != ACCESS_READ_ONLY && access != ACCESS_READ_WRITE )
				throw new SpecParseException( xml.LineNumber, ACCESS_ATTRIBUTE + " must be one of these values: " + ACCESS_READ_ONLY + ", " + ACCESS_READ_WRITE );
			bool nowrite = (access == ACCESS_READ_ONLY);

			int priority = -1;
			try 
			{
				string pstr   = xml.GetAttribute( PRIORITY_ATTRIBUTE );
				if ( pstr != null )
				{
					priority = Int32.Parse( pstr );

					if ( priority < 0 || priority > 10 )
						throw new NotSupportedException();
				}
			}
			catch( Exception e )
			{
				throw new SpecParseException( xml.LineNumber, PRIORITY_ATTRIBUTE + " attribute must have numeric value of 0-10", e );
			}

			string highlevelType = xml.GetAttribute( IS_A_ATTRIBUTE );

			ApplianceState state = new ApplianceState( _currentAppliance, name, priority, nowrite );
			state.SetNetworkHandler();

			state.HighlevelType = highlevelType;

			string typeName = xml.GetAttribute( TYPE_NAME_ATTRIBUTE );
			HandleObjectType( typeName, state );

			// PUCFrame.GlobalLogBox.AppendText( "Found State: " + name );

			while( xml.Read() )
				if ( xml.NodeType == XmlNodeType.Element ) 
				{
					if ( xml.Name == ACTIVEIF_TAG )
						state.Dependencies = HandleActiveIf( xml );
					else if ( xml.Name == LABELS_TAG )
						state.Labels = HandleLabelDict( xml, LABELS_TAG );
					else if ( xml.Name == APPLY_TYPE_TAG ) 
					{
						string applyTypeName = xml.GetAttribute( TYPE_NAME_ATTRIBUTE );
						if ( applyTypeName == null )
							throw new FormatException( APPLY_TYPE_TAG + " must contain the name of the type to apply!" );

						PUCType type = GetStateType( applyTypeName, xml.LineNumber );
						if ( type == null )
							throw new FormatException( "The " + APPLY_TYPE_TAG + " can only apply types that were defined earlier in the specification document." );

						// NOTE TO SELF: Implementing type-name attribute for all
						// "object" type things

						state.Type = (PUCType)type.Clone();
					}
					else if ( xml.Name == TYPE_TAG )
					{
						state.Type = HandleType( xml, false );
					}
					else if ( xml.Name == DEFAULT_VALUE_TAG )
					{
						state.DefaultValue = HandleDefaultValue( xml, state.Type.ValueSpace );
					}
					else if ( xml.Name == REQUIRED_IF_TAG )
					{
						state.RequiredIf = new AND();
						HandleFormula( xml, REQUIRED_IF_TAG, (Formula)state.RequiredIf );
					}
				}
				else if ( xml.NodeType == XmlNodeType.EndElement )
				{
					if ( xml.Name == STATE_TAG )
						return state;
				}

			return state;			
		}

		private static IPUCValue HandleDefaultValue( XmlTextReader xml, ValueSpace space )
		{
			IPUCValue val = null;

			while( xml.Read() )
			{
				if ( xml.NodeType == XmlNodeType.Element )
				{
					switch( xml.Name )
					{
						case CONSTANT_TAG:
							string strval = xml.GetAttribute( VALUE_ATTRIBUTE );
							if ( strval == null )
								throw new SpecParseException( xml.LineNumber, CONSTANT_TAG + " element must include the " + VALUE_ATTRIBUTE + " attribute." );

							object objval = space.Validate( strval );
							if ( objval == null )
								throw new SpecParseException( xml.LineNumber, "Value of " + CONSTANT_TAG + " does not match space of variable." );

							val = new PUCValue( objval );
							break;

						case REFVALUE_TAG:
							string statename = xml.GetAttribute( STATE_ATTRIBUTE );
							if ( statename == null )
								throw new SpecParseException( xml.LineNumber, REFVALUE_TAG + " element must include the " + STATE_ATTRIBUTE + " attribute." );

							val = new ValueConstraint( statename );
							_resolvables.Add( val );
							break;

						default:
							throw new SpecParseException( xml.LineNumber, "Encountered unexpected element " + xml.Name );
					}
				}
				else if ( xml.NodeType == XmlNodeType.EndElement && 
					      xml.Name == DEFAULT_VALUE_TAG )
				{
					break;
				}
			}

			if ( val == null )
				throw new SpecParseException( xml.LineNumber, DEFAULT_VALUE_TAG + " element should include either a " + CONSTANT_TAG + " or " + REFVALUE_TAG + " element." );

			return val;
		}

		private static PUCType HandleType( XmlTextReader xml, bool inTypesSection ) 
		{
			string name = xml.GetAttribute( TYPE_NAME_ATTRIBUTE );

			if ( name == null && inTypesSection )
				throw new SpecParseException( xml.LineNumber, "The " + TYPE_TAG + " element must incluce a " + TYPE_NAME_ATTRIBUTE + " attribute when inside the " + TYPES_TAG + " element." );

			ValueSpace valspc    = null;
			Hashtable  vallabels = null;
			ValueSpace expspace  = null;

			while( xml.Read() )
			{
				if ( xml.NodeType == XmlNodeType.Element ) 
				{
					switch( xml.Name )
					{
						default:
							valspc = HandleValueSpace( xml, true, xml.Name );
							break;

						case VALUE_LABEL_TAG:
							if ( valspc == null )
								throw new FormatException( "The primitive type must be declared before " + VALUE_LABEL_TAG );

							vallabels = HandleValueLabels( xml, valspc );
							break;
					}
				}
				else if ( xml.NodeType == XmlNodeType.EndElement )
				{
					if ( xml.Name == TYPE_TAG )
						break;
				}
			}

			PUCType type = new PUCType( xml.LineNumber, valspc, vallabels, expspace );

			HandleStateType( name, type );

			return type;
		}

		private static void HandleStateType( string name, PUCType type )
		{
			if ( name != null )
			{
				// don't throw an exception is this is a re-define of an
				// built-in type
				if ( name == "OnOffType" || name == "YesNoType" )
					return;

				if ( _types.ContainsKey( name ) )
					throw new SpecParseException( type.LineNumber, "Type " + name + " has already been defined." );
				else
					_types.Add( name, type );
			}
		}

		private static void HandleObjectType( string name, ApplianceObject obj )
		{
			if ( name != null )
			{
				if ( _types.ContainsKey( name ) )
					throw new SpecParseException( obj.LineNumber, "Type " + name + " has already been defined." );
				else
					_types.Add( name, obj );
			}
		}

		private static void HandleObjectType( string name, GroupNode group )
		{
			if ( name != null )
			{
				if ( _types.ContainsKey( name ) )
					throw new SpecParseException( group.LineNumber, "Type " + name + " has already been defined." );
				else
					_types.Add( name, group );
			}
		}

		private static PUCType GetStateType( string name, int lineNumber )
		{
			object obj = _types[ name ];

			if ( obj == null )
				throw new SpecParseException( lineNumber, "Type " + name + " has not been defined." );

			if ( !( obj is PUCType) )
				throw new SpecParseException( lineNumber, "Type " + name + " defines an object type which cannot be applied in this location." );

			return (PUCType)obj;
		}

		private static object GetObjectType( string name, int lineNumber )
		{
			object obj = _types[ name ];

			if ( obj == null )
				throw new SpecParseException( lineNumber, "Type " + name + " has not been defined." );

			if ( !(obj is ICloneable) )
				throw new SpecParseException( lineNumber, "Type " + name + " defines a state type which cannot be applied in this location." );

			return ((ICloneable)obj).Clone();
		}

		private static ValueSpace HandleValueSpace( XmlTextReader xml,
													bool examineLastTag,
													string returntag )
		{
			// I believe logic short-cutting will cause the Read() to
			// be skipped the first time when examineLastTag is called.
			while( examineLastTag || xml.Read() )
			{
				examineLastTag = false;

				if ( xml.NodeType == XmlNodeType.Element )
				{
					switch( xml.Name )
					{
						case BINARY_TAG:
							if ( xml.IsEmptyElement )
								return new BinarySpace();

							return HandleBinary( xml );

						case BOOLEAN_TAG:
							return new BooleanSpace();

						case STRING_TAG:
							if ( xml.IsEmptyElement )
								return new StringSpace();

							return HandleString( xml );

						case ENUMERATED_TAG:
							return HandleEnumerated( xml );

						case INTEGER_TAG:
							if ( xml.IsEmptyElement )
								return new IntegerSpace();
							
							return HandleInteger( xml );

						case FIXEDPT_TAG:
							if ( xml.IsEmptyElement )
								throw new FormatException( FIXEDPT_TAG + " must contain tag " + POINTPOS_TAG );

							return HandleFixedPt( xml );

						case FLOATINGPT_TAG:
							if ( xml.IsEmptyElement )
								return new FloatingPtSpace();

							return HandleFloatingPt( xml );

						case LIST_SELECTION_TAG:
							return HandleListSelection( xml );
					}
				}
				else if ( xml.NodeType == XmlNodeType.EndElement && 
						  xml.Name == TYPE_TAG ) 
				{
					throw new FormatException( TYPE_TAG + " does not contain a valid type group." );
				}
			}

			Debug.Assert( true, "Shouldn't ever get here." );
			return null;
		}

		private static ListSelectionSpace HandleListSelection( XmlTextReader xml )
		{
			string listName = xml.GetAttribute( LIST_ATTRIBUTE );
			if ( listName == null )
				throw new SpecParseException( xml.LineNumber, LIST_SELECTION_TAG + " type must have a " + LIST_ATTRIBUTE + " attribute." );

			ListSelectionSpace lSpace = new ListSelectionSpace( listName );

			if ( xml.IsEmptyElement )
				return lSpace;

			while ( xml.Read() )
			{
				if ( xml.NodeType == XmlNodeType.Element )
				{
					if ( xml.Name == ACTIVEIF_TAG )
					{
						lSpace.Formula = HandleActiveIf( xml );
					}
					else
						throw new SpecParseException( xml.LineNumber, "Unexpected element: " + xml.Name );
				}
				else if ( xml.NodeType == XmlNodeType.EndElement &&
						  xml.Name == LIST_SELECTION_TAG )
					break;
			}

			return lSpace;
		}

		private static LabelDictionary HandleMap( XmlTextReader xml )
		{
			LabelDictionary label = null;
			Dependency df = null;

			while( xml.Read() )
			{
				if ( xml.NodeType == XmlNodeType.Element )
				{
					switch( xml.Name )
					{
						case LABELS_TAG:
							label = HandleLabelDict( xml, LABELS_TAG );
							break;

						case ACTIVEIF_TAG:
							if ( label == null )
								throw new SpecParseException( xml.LineNumber, ACTIVEIF_TAG + " occurred before " + LABELS_TAG + " within a " + MAP_TAG + " element." );

							df = HandleActiveIf( xml );

							break;
					}
				}
				else if ( xml.NodeType == XmlNodeType.EndElement && 
						  xml.Name == MAP_TAG )
					// stop parsing in this loop
					break;
			}

			if ( df != null )
			{
				EnableConstraint ec = new EnableConstraint( df );
				_resolvables.Add( ec );
				label.EnableConstraint = ec;
			}

			return label;
		}

		private static Hashtable HandleValueLabels( XmlTextReader xml, 
													ValueSpace valspc ) 
		{
			Hashtable labels = new Hashtable();

			while( xml.Read() )
			{
				if ( xml.NodeType == XmlNodeType.Element )
				{
					if ( xml.Name == MAP_TAG ) 
					{
						object index;
						string indexstr = xml.GetAttribute( INDEX_ATTRIBUTE );
						if ( indexstr == null )
							throw new SpecParseException( xml.LineNumber, MAP_TAG + " must specify an index!" );

						try
						{
							index = valspc.Validate( indexstr );

							if ( index == null )
								throw new SpecParseException( xml.LineNumber, MAP_TAG + " index was null.  It may not match value space. " + indexstr );
						}
						catch( Exception )
						{
							throw new SpecParseException( xml.LineNumber, MAP_TAG + " index does not match type of state variable" );
						}

						labels.Add( index, HandleMap( xml ) );
					}
				}
				else if ( xml.NodeType == XmlNodeType.EndElement )
				{
					if ( xml.Name == VALUE_LABEL_TAG )
						return labels;
				}
			}

			return labels;
		}

		private static BinarySpace HandleBinary( XmlTextReader xml )
		{
			BinarySpace bspc = new BinarySpace();

			string elemName = null;

			while( xml.Read() )
			{
				if ( xml.NodeType == XmlNodeType.Element )
				{
					if ( xml.IsEmptyElement )
						bspc[ xml.Name ] = true;
					else
						elemName = xml.Name;
				}
				else if ( xml.NodeType == XmlNodeType.Text )
				{
					if ( elemName != null )
					{
						bspc[ elemName ] = xml.Value;
						elemName = null;
					}
				}
				else if ( xml.NodeType == XmlNodeType.EndElement )
				{
					if ( xml.Name == BINARY_TAG )
						break;
				}
			}

			return bspc;
		}
			
		private static StringSpace HandleString( XmlTextReader xml )
		{
			IPUCNumber min  = null;
			IPUCNumber ave  = null;
			IPUCNumber max  = null;

			while( xml.Read() ) 
			{
				if ( xml.NodeType == XmlNodeType.Element )
				{
					switch( xml.Name )
					{
						case MINIMUM_TAG:
							min = extractNumber( xml, MINIMUM_TAG );
							break;

						case MAXIMUM_TAG:
							max = extractNumber( xml, MAXIMUM_TAG );
							break;

						case AVERAGE_TAG:
							ave = extractNumber( xml, AVERAGE_TAG );
							break;

						default:
							throw new SpecParseException( xml.LineNumber, "Unexpected element " + xml.Name + " found." );
					}
				}
				else if ( xml.NodeType == XmlNodeType.EndElement )
				{
					if ( xml.Name == STRING_TAG )
					{
						return new StringSpace( min, ave, max );
					}
				}
			}

			Debug.Assert( true, "Shouldn't ever get here." );
			return null;
		}
		
		private static IntegerSpace HandleInteger( XmlTextReader xml )
		{
			bool isRanged    = false;
			bool isIncrement = false;

			IPUCNumber min  = null;
			IPUCNumber max  = null;
			IPUCNumber incr = null;

			while( xml.Read() ) 
			{
				if ( xml.NodeType == XmlNodeType.Element )
				{
					switch( xml.Name )
					{
						case MINIMUM_TAG:
							isRanged = true;
							min = extractNumber( xml, MINIMUM_TAG );
							break;

						case MAXIMUM_TAG:
							isRanged = true;
							max = extractNumber( xml, MAXIMUM_TAG );
							break;

						case INCREMENT_TAG:
							isIncrement = true;
							incr = extractNumber( xml, INCREMENT_TAG );
							break;

						default:
							throw new SpecParseException( xml.LineNumber, "Unexpected element " + xml.Name + " found." );
					}
				}
				else if ( xml.NodeType == XmlNodeType.EndElement )
				{
					if ( xml.Name == INTEGER_TAG )
					{
						if ( isIncrement )
							return new IntegerSpace( min, max, incr );
						else if ( isRanged )
							return new IntegerSpace( min, max );
						else 
							return new IntegerSpace();
					}
				}
			}

			Debug.Assert( true, "Shouldn't ever get here." );
			return null;
		}

		private static FixedPtSpace HandleFixedPt( XmlTextReader xml )
		{
			bool lookingPos  = false;

			bool isRanged    = false;
			bool isIncrement = false;

			int    pos  = 0;
			IPUCNumber min  = null;
			IPUCNumber max  = null;
			IPUCNumber incr = null;

			while( xml.Read() ) 
			{
				if ( xml.NodeType == XmlNodeType.Element )
				{
					switch( xml.Name )
					{
						case POINTPOS_TAG:
							lookingPos = true;
							break;

						case MINIMUM_TAG:
							isRanged = true;
							min = extractNumber( xml, MINIMUM_TAG );
							break;

						case MAXIMUM_TAG:
							isRanged = true;
							max = extractNumber( xml, MAXIMUM_TAG );
							break;

						case INCREMENT_TAG:
							isIncrement = true;
							incr = extractNumber( xml, INCREMENT_TAG );
							break;

						default:
							throw new SpecParseException( xml.LineNumber, "Unexpected element " + xml.Name + " found." );
					}
				}
				else if ( xml.NodeType == XmlNodeType.Text )
				{
					if ( lookingPos )
						pos = Int32.Parse( xml.Value );

					lookingPos = false;
				}
				else if ( xml.NodeType == XmlNodeType.EndElement )
				{
					if ( xml.Name == FIXEDPT_TAG )
					{
						if ( isIncrement )
							return new FixedPtSpace( pos, min, max, incr );
						else if ( isRanged )
							return new FixedPtSpace( pos, min, max );
						else 
							return new FixedPtSpace( pos );
					}
				}
			}

			Debug.Assert( true, "Shouldn't get here." );
			return null;
		}

		private static FloatingPtSpace HandleFloatingPt( XmlTextReader xml )
		{
			bool isRanged    = false;

			IPUCNumber min  = null;
			IPUCNumber max  = null;

			while( xml.Read() ) 
			{
				if ( xml.NodeType == XmlNodeType.Element )
				{
					switch( xml.Name )
					{
						case MINIMUM_TAG:
							isRanged = true;
							min = extractNumber( xml, MINIMUM_TAG );
							break;

						case MAXIMUM_TAG:
							isRanged = true;
							max = extractNumber( xml, MAXIMUM_TAG );
							break;

						default:
							throw new SpecParseException( xml.LineNumber, "Unexpected element " + xml.Name + " found." );
					}
				}
				else if ( xml.NodeType == XmlNodeType.EndElement )
				{
					if ( xml.Name == FLOATINGPT_TAG )
					{
						if ( isRanged )
							return new FloatingPtSpace( min, max );
						else 
							return new FloatingPtSpace();
					}
				}
			}

			Debug.Assert( true, "Shouldn't get here." );
			return null;
		}

		private static EnumeratedSpace HandleEnumerated( XmlTextReader xml )
		{
			bool lookingItems = false;
			int items = 0;

			while( xml.Read() ) 
			{
				if ( xml.NodeType == XmlNodeType.Element )
				{
					switch( xml.Name )
					{
						case ITEM_COUNT_TAG:
							lookingItems = true;
							break;
					}
				}
				else if ( xml.NodeType == XmlNodeType.Text )
				{
					try
					{
						try
						{
							if ( lookingItems )
								items = Int32.Parse( xml.Value );

							return new EnumeratedSpace( items );
						}
						catch( ArgumentException )
						{
							throw new SpecParseException( xml.LineNumber, "Enumerated space must have an item count > 0" );
						}
					}
					catch( FormatException )
					{
						throw new SpecParseException( xml.LineNumber, "Enumerated value space item count not specified as a number." );
					}
				}
				else if ( xml.NodeType == XmlNodeType.EndElement )
					if ( xml.Name == ENUMERATED_TAG )
						throw new FormatException( ENUMERATED_TAG + " must contain " + ITEM_COUNT_TAG );
			}

			// shouldn't get here
			return null;
		}
	}
}
	