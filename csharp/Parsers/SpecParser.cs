using System;
using System.Collections;
using System.IO;
using System.Xml;
using PUC.Types;

namespace PUC.Parsers
{
	/// <summary>
	/// Summary description for SpecParser.
	/// </summary>
	public class SpecParser
	{
		/*
		 * Constants
		 */

		// version accepted by this parser
		public const string SPEC_PARSER_VERSION = "PUC/2.0";

		// tag constants
		public const string SPEC_TAG			= "spec";
		public const string GROUPINGS_TAG		= "groupings";
		public const string GROUP_TAG			= "group";
		public const string LIST_GROUP_TAG		= "list-group";
		public const string UNION_GROUP_TAG		= "union-group";
		public const string STATE_TAG			= "state";
		public const string TYPE_TAG			= "type";
		public const string PRIORITY_TAG		= "priority";
		public const string VALUE_SPACE_TAG		= "valueSpace";
		public const string VALUE_LABEL_TAG		= "valueLabels";
		public const string EXPCT_SPACE_TAG		= "expectedValues";
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
		public const string EQUALS_TAG			= "equals";
		public const string LESSTHAN_TAG		= "lessthan";
		public const string GREATERTHAN_TAG		= "greaterthan";
		public const string ITEM_COUNT_TAG		= "item-count";
		public const string REFVALUE_TAG		= "refvalue";
		public const string REFSTRING_TAG		= "refstring";
		public const string EXPLANATION_TAG		= "explanation";
		public const string PHONETIC_TAG		= "phonetic";
		public const string TTS_TAG				= "text-to-speech";
		public const string OBJECT_REF_TAG		= "object-ref";
		public const string APPLY_TYPE_TAG		= "apply-type";
		public const string NUMBER_TAG			= "number";
		public const string DEFINED_TAG			= "defined";
		public const string UNDEFINED_TAG		= "undefined";
		public const string SELECTION_TYPE_TAG  = "selection-type";

		// spec xml file attribute names
		public const string NAME_ATTRIBUTE		= "name";
		public const string INDEX_ATTRIBUTE		= "index";
		public const string ACCESS_ATTRIBUTE	= "access";
		public const string IGNORE_ATTRIBUTE	= "ignore";
		public const string STATE_ATTRIBUTE		= "state";
		public const string ENABLE_ATTRIBUTE	= "enable";
		public const string PRIORITY_ATTRIBUTE	= "priority";
		public const string TEXT_ATTRIBUTE		= "text";
		public const string RECORDING_ATTRIBUTE = "recording";
		public const string IS_A_ATTRIBUTE		= "is-a";
		public const string VERSION_ATTRIBUTE	= "version";
		public const string TYPE_NAME_ATTRIBUTE = "type-name";
		public const string PREFERRED_ATTRIBUTE = "preferred";

		public const string IGNORE_ATTR_ALL		= "all";
		public const string IGNORE_ATTR_PRNT	= "parent";

		public const string ACCESS_READ_ONLY	= "ReadOnly";
		public const string ACCESS_WRITE_ONLY	= "WriteOnly";

		public const string SEL_TYPE_ONE		= "one";
		public const string SEL_TYPE_MULTIPLE	= "multiple";

		public const string ATTR_VALUE_TRUE		= "true";
		public const string ATTR_VALUE_FALSE	= "false";


		/*
		 * Static Member Variables
		 */
		protected static VariableTable		_varTable;
		protected static Hashtable			_types;
		protected static ArrayList			_constraints;
		protected static Appliance			_currentAppliance;

		
		/*
		 * Static Methods
		 */
		public static void Parse( TextReader reader, Appliance appl ) 
		{
			_currentAppliance = appl;
			XmlTextReader xmlStream = null;

			try 
			{
				xmlStream = new XmlTextReader( reader );
				xmlStream.XmlResolver = null;

				// Parse for version number

				string version = GetVersion( xmlStream );

				switch( version )
				{
					// PUC/2.0
					case SpecParser.SPEC_PARSER_VERSION:
						break;

					// PUC/2.1
					case SpecParser21.SPEC_PARSER_VERSION:
						SpecParser21.Parse( xmlStream, _currentAppliance );
						return;

					// PUC/2.2
					case SpecParser22.SPEC_PARSER_VERSION:
						SpecParser22.Parse( xmlStream, _currentAppliance );
						return;

					default:
						Globals.GetFrame( _currentAppliance )
							.AddLogLine( "Specification version unknown: " + version );
						Globals.GetFrame( _currentAppliance )
							.AddLogLine( "Error on line number " + xmlStream.LineNumber );
						return;
				}

				// Phase 1.  Parse the XML

				Globals.GetFrame( _currentAppliance )
					.AddLogLine( "Parsing Specification Version " + SpecParser.SPEC_PARSER_VERSION );
				Globals.GetFrame( _currentAppliance )
					.AddLogLine( "Phase #1: Parsing XML" );

				_varTable = new VariableTable( "Unknown" );
				_types = new Hashtable();
				_constraints = new ArrayList();

				GroupNode root = HandleSpec( xmlStream );

				// Phase 2.  Remove Dependency Hierarchy
				//---------------------------------------
				// This second pass is used to clean up the parsing of 
				// dependencies.  So far, no group-level active-if blocks
				// have been integrated into the states within that group.

				Globals.GetFrame( _currentAppliance ).AddLogLine( "Phase #2: Fix Dependencies" );

				fixDepend( root, null, null );

				// Phase 3.  Resolve Names and Create Reverse Dependency Lists
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

				// Phase 4.  Validate All Constraint Objects
				//-------------------------------------------
				// So far, no constraint objects have references to the states
				// whose values they depend upon.  This phase resolve those
				// names and validates each constraint.

				Globals.GetFrame( _currentAppliance ).AddLogLine( "Phase #4: Validate Constraint Objects" );

				e = _constraints.GetEnumerator();
				while( e.MoveNext() )
				{
					IConstraint c = (IConstraint)e.Current;

					if ( c.ResolveObject( _varTable ) )
					{
						IEnumerator en = c.States.GetEnumerator();
						while( en.MoveNext() )
							((ApplianceState)en.Current).MakeConstraintVariable();
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

		/// <summary>
		/// Handles the parsing of the specification document at the
		/// very beginning.
		/// </summary>
		/// <param name="xml">the XmlTextReader to read from</param>
		private static string GetVersion( XmlTextReader xml )
		{
			while( xml.Read() ) 
				if ( xml.NodeType == XmlNodeType.Element &&
					xml.Name == SPEC_TAG )
				{
					return xml.GetAttribute( VERSION_ATTRIBUTE );
				}

			return null;
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

		private static void resolveNames( Dependency d,
										  Hashtable dstates,
										  VariableTable varTable,
										  ApplianceObject ao )
		{
			if ( d == null ) return;

			if ( d is Formula )
			{
				Formula df = (Formula)d;

				IEnumerator e = df.GetDependencies();
				while( e.MoveNext() )
					resolveNames( (Dependency)e.Current, dstates, varTable, ao );
			}
			else if ( d is StateDependency )
			{
				StateDependency sd = (StateDependency)d;

				if (! sd.ResolveObject( varTable ) )
					throw new SpecParseException( d.LineNumber, "Problem resolving a dependency state name: " + sd.StateName );
	
				ApplianceState s = sd.State;

				s.AddReverseDependency( ao );
				dstates[ s.Name ] = s;

				if ( d.HasValue() && ((StateValueDependency)d).IsReference )
				{
					s = ((StateValueDependency)d).ReferenceState;

					s.AddReverseDependency( ao );
					dstates[ s.Name ] = s;
				}
			}
		}


		/// <summary>
		/// Handles the parsing of elements contains within the SPEC_TAG
		/// </summary>
		/// <param name="xml">the XmlTextReader to read from</param>
		private static GroupNode HandleSpec( XmlTextReader xml )
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
							applianceLabels = HandleLabelDict( xml, null, LABELS_TAG );
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

		private static BranchGroupNode HandleGroupings( XmlTextReader xml )
		{
			BranchGroupNode root = new BranchGroupNode();
			root.Name = _varTable.SpecName;

			HandleGroup( xml, root, _varTable, GROUPINGS_TAG );

			if ( root.Count <= 0 )
				throw new SpecParseException( xml.LineNumber, "No groups defined within groupings tag." );

			return root;
		}

		private static BranchGroupNode HandleGroup( XmlTextReader xml, BranchGroupNode group, IBranchDataWindow window, string endTag )
		{
			group.LineNumber = xml.LineNumber;

			Hashtable nameChecker = new Hashtable();

			if ( endTag != GROUPINGS_TAG )
			{
				string name = xml.GetAttribute( NAME_ATTRIBUTE );
				if ( name == null )
					throw new SpecParseException( xml.LineNumber, "group tag found with no name." );
				if ( name == _varTable.SpecName )
					throw new SpecParseException( xml.LineNumber, "group name must be different than specification name." );
				group.Name = name;

				if ( group is ListGroupNode )
				{
					IndexedDataWindow newWindow = new IndexedDataWindow( (ListGroupNode)group );
					window.AddChildWindow( newWindow );
					((ListGroupNode)group).DataWindow = newWindow;

					window = newWindow;
				}
			}

			string priorityStr = xml.GetAttribute( PRIORITY_ATTRIBUTE );
			if ( priorityStr != null )
				group.Priority = Int32.Parse( priorityStr );

			string type = xml.GetAttribute( IS_A_ATTRIBUTE );
			if ( type != null )
				group.HighlevelType = type;

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

							HandleGroup( xml, (BranchGroupNode)g, window, GROUP_TAG );

							if ( nameChecker[ g.Name ] != null )
								throw new SpecParseException( g.LineNumber, "Group " + g.Name + " has same name as another element under parent " + group.Name + " at line " + ((GroupNode)nameChecker[ g.Name ]).LineNumber );
							nameChecker[ g.Name ] = g;

							break;

						case LIST_GROUP_TAG:
							g = new ListGroupNode( _currentAppliance );
							group.Children.Add( g );
							g.Parent = group;

							HandleGroup( xml, (BranchGroupNode)g, window, LIST_GROUP_TAG );

							if ( nameChecker[ g.Name ] != null )
								throw new SpecParseException( g.LineNumber, "Group " + g.Name + " has same name as another element under parent " + group.Name + " at line " + ((GroupNode)nameChecker[ g.Name ]).LineNumber );
							nameChecker[ g.Name ] = g;

							_varTable.RegisterListGroup( g.FullPath, (ListGroupNode)g );

							((ListGroupNode)g).CreateExtraStates( _varTable, window );

							break;

						case SELECTION_TYPE_TAG:
							if ( !( group is ListGroupNode ) )
								throw new SpecParseException( xml.LineNumber, SELECTION_TYPE_TAG + " element is only allowed within " + LIST_GROUP_TAG + " elements." );
							
							string typeName = xml.GetAttribute( TYPE_NAME_ATTRIBUTE );
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

							HandleGroup( xml, (BranchGroupNode)g, window, LIST_GROUP_TAG );

							if ( nameChecker[ g.Name ] != null )
								throw new SpecParseException( g.LineNumber, "Group " + g.Name + " has same name as another element under parent " + group.Name + " at line " + ((GroupNode)nameChecker[ g.Name ]).LineNumber );
							nameChecker[ g.Name ] = g;

							((UnionGroupNode)g).CreateExtraStates( _currentAppliance, _varTable );

							break;

						case ACTIVEIF_TAG:
							group._dependencies = HandleActiveIf( xml, group );
							break;

						case LABELS_TAG:
							group.Labels = HandleLabelDict( xml, null, LABELS_TAG );
							break;

						case COMMAND_TAG:
							ApplianceCommand cmd = HandleCommand( xml );

							if ( nameChecker[ cmd.Name ] != null )
								throw new SpecParseException( cmd.LineNumber, "Command " + cmd.Name + " has same name as another element under parent " + group.Name + " at line " + ((GroupNode)nameChecker[ cmd.Name ]).LineNumber );
							nameChecker[ cmd.Name ] = cmd;
							_varTable.RegisterObject( cmd.MakeFullName( group.FullPath ), cmd );

							g = new ObjectGroupNode( cmd );
							group.Children.Add( g );
							g.Parent = group;
							break;

						case EXPLANATION_TAG:
							ApplianceExplanation expl = HandleExplanation( xml );

							if ( nameChecker[ expl.Name ] != null )
								throw new SpecParseException( expl.LineNumber, "Command " + expl.Name + " has same name as another element under parent " + group.Name + " at line " + ((GroupNode)nameChecker[ expl.Name ]).LineNumber );
							nameChecker[ expl.Name ] = expl;
							_varTable.RegisterObject( expl.MakeFullName( group.FullPath ), expl );

							g = new ObjectGroupNode( expl );
							group.Children.Add( g );
							g.Parent = group;
							break;

						case STATE_TAG:
							ApplianceState state = HandleState( xml );

							if ( nameChecker[ state.Name ] != null )
								throw new SpecParseException( state.LineNumber, "Command " + state.Name + " has same name as another element under parent " + group.Name + " at line " + ((GroupNode)nameChecker[ state.Name ]).LineNumber );
							nameChecker[ state.Name ] = state;
							_varTable.RegisterObject( state.MakeFullName( group.FullPath ), state );

							window.AddChildWindow( state );

							g = new ObjectGroupNode( state );
							group.Children.Add( g );
							g.Parent = group;
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
				else if ( xml.NodeType == XmlNodeType.Element && xml.Name == REFVALUE_TAG )
				{
					string statestr = xml.GetAttribute( STATE_ATTRIBUTE );
					value = new NumberConstraint( null, statestr );
					_constraints.Add( value );
				}
				else if ( xml.NodeType == XmlNodeType.Text )
					value = new IntNumber( Int32.Parse( xml.Value ) );
			}

			return value;
		}

		private static Dependency HandleActiveIf( XmlTextReader xml, GroupNode g )
		{
			g._depignore = GroupNode.NO_IGNORE;
			string ignore = xml.GetAttribute( IGNORE_ATTRIBUTE );
			if ( ignore != null )
			{
				if ( ignore == IGNORE_ATTR_ALL )
					g._depignore = GroupNode.IGNORE_ALL;
				else if ( ignore == IGNORE_ATTR_PRNT )
					g._depignore = GroupNode.IGNORE_PARENT;
			}

			return HandleActiveIf( xml );
		}

		private static void HandleFormula( XmlTextReader xml,
										   string returntag,
										   Formula df )
		{
			while( xml.Read() )
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
					if ( xml.NodeType == XmlNodeType.Text )
					{
						val = xml.Value;
						isRef = false;
						break;
					}
					else if ( xml.NodeType == XmlNodeType.Element && xml.Name == REFVALUE_TAG )
					{
						val = xml.GetAttribute( STATE_ATTRIBUTE );
						isRef = true;
						break;
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

			d.LineNumber = xml.LineNumber;
			return d;
		}

		private static LabelDictionary HandleLabelDict( XmlTextReader xml, 
														ApplianceObject obj,
														string returntag )
		{
			LabelDictionary labels = new LabelDictionary();
			bool preferred = false;
			bool lookingForText = false;

			while( xml.Read() )
				if ( xml.NodeType == XmlNodeType.Element )
				{
					if ( xml.Name == LABEL_TAG )
					{
						preferred = 
							( xml.GetAttribute( PREFERRED_ATTRIBUTE ) != null ) && 
							( xml.GetAttribute( PREFERRED_ATTRIBUTE ) == ATTR_VALUE_TRUE );

						lookingForText = true;
					}
					else if ( xml.Name == REFSTRING_TAG )
					{
						string statestr = xml.GetAttribute( STATE_ATTRIBUTE );
						if ( statestr == null )
							throw new FormatException( "Reference string must define a state to reference to." );

						if ( obj == null )
							throw new FormatException( "Reference strings are not allowed in group labels (for now)." );

						StringConstraint sc = new StringConstraint( obj, statestr );
						_constraints.Add( sc );
						labels.AddLabel( sc, preferred );
						lookingForText = false;
						preferred = false;
					}
				}
				else if ( xml.NodeType == XmlNodeType.Text ) 
				{
					if ( lookingForText ) 
					{
						labels.AddLabel( new PUC.Types.StringValue( xml.Value ), preferred );
						lookingForText = false;
						preferred = false;
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

			string pstr = xml.GetAttribute( PRIORITY_ATTRIBUTE );
			string highlevelType = xml.GetAttribute( IS_A_ATTRIBUTE );
			int priority = -1;
			if ( pstr != null )
				priority = Int32.Parse( pstr );

			// PUCFrame.GlobalLogBox.AppendText( "Found Command: " + name );

			ApplianceCommand cmd = new ApplianceCommand( _currentAppliance, name, priority );
			cmd.SetNetworkHandler();

			cmd.HighlevelType = highlevelType;

			while( xml.Read() )
				if ( xml.NodeType == XmlNodeType.Element ) 
				{
					if ( xml.Name == ACTIVEIF_TAG )
						cmd.Dependencies = HandleActiveIf( xml );
					else if ( xml.Name == LABELS_TAG )
						cmd.Labels = HandleLabelDict( xml, cmd, LABELS_TAG );
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
			
			string pstr = xml.GetAttribute( PRIORITY_ATTRIBUTE );
			int priority = -1;
			if ( pstr != null )
				priority = Int32.Parse( pstr );

			// PUCFrame.GlobalLogBox.AppendText( "Found Explanation: " + name );

			ApplianceExplanation expl = new ApplianceExplanation( _currentAppliance, name, priority );

			while( xml.Read() )
				if ( xml.NodeType == XmlNodeType.Element ) 
				{
					if ( xml.Name == ACTIVEIF_TAG )
						expl.Dependencies = HandleActiveIf( xml );
					else if ( xml.Name == LABELS_TAG )
						expl.Labels = HandleLabelDict( xml, expl, LABELS_TAG );
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
			string pstr   = xml.GetAttribute( PRIORITY_ATTRIBUTE );
			string highlevelType = xml.GetAttribute( IS_A_ATTRIBUTE );
			int priority = -1;
			if ( pstr != null )
				priority = Int32.Parse( pstr );

			bool nowrite = (access == ACCESS_READ_ONLY);

			ApplianceState state = new ApplianceState( _currentAppliance, name, priority, nowrite );
			state.SetNetworkHandler();

			state.HighlevelType = highlevelType;

			// PUCFrame.GlobalLogBox.AppendText( "Found State: " + name );

			while( xml.Read() )
				if ( xml.NodeType == XmlNodeType.Element ) 
				{
					if ( xml.Name == ACTIVEIF_TAG )
						state.Dependencies = HandleActiveIf( xml );
					else if ( xml.Name == LABELS_TAG )
						state.Labels = HandleLabelDict( xml, state, LABELS_TAG );
					else if ( xml.Name == APPLY_TYPE_TAG ) 
					{
						string typename = xml.GetAttribute( NAME_ATTRIBUTE );
						if ( typename == null )
							throw new FormatException( APPLY_TYPE_TAG + " must contain the name of the type to apply!" );

						PUCType type = (PUCType)_types[ typename ];
						if ( type == null )
							throw new FormatException( "The " + APPLY_TYPE_TAG + " can only apply types that were defined earlier in the specification document." );

						state.Type = (PUCType)type.Clone();
					}
					else if ( xml.Name == TYPE_TAG )
					{
						state.Type = HandleType( xml, state );
					}
				}
				else if ( xml.NodeType == XmlNodeType.EndElement )
				{
					if ( xml.Name == STATE_TAG )
						return state;
				}

			return state;			
		}

		private static PUCType HandleType( XmlTextReader xml, ApplianceState state ) 
		{
			string name = xml.GetAttribute( NAME_ATTRIBUTE );

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
							valspc = HandleValueSpace( xml, true, xml.Name, state );
							break;

						case EXPCT_SPACE_TAG:
							expspace = HandleValueSpace( xml, false, EXPCT_SPACE_TAG, state );
							break;

						case VALUE_LABEL_TAG:
							if ( valspc == null )
								throw new FormatException( VALUE_SPACE_TAG + " must be declared before " + VALUE_LABEL_TAG );

							vallabels = HandleValueLabels( xml, state, valspc );
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

			if ( name != null )
			{
				if ( _types.Contains( name ) )
					throw new SpecParseException( type.LineNumber, "Type " + name + " has already been defined." );

				_types.Add( name, type );
			}

			return type;
		}

		private static ValueSpace HandleValueSpace( XmlTextReader xml,
													bool examineLastTag,
													string returntag,
													ApplianceState state )
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
							
							return HandleInteger( xml, state );

						case FIXEDPT_TAG:
							if ( xml.IsEmptyElement )
								throw new FormatException( FIXEDPT_TAG + " must contain tag " + POINTPOS_TAG );

							return HandleFixedPt( xml, state );

						case FLOATINGPT_TAG:
							if ( xml.IsEmptyElement )
								return new FloatingPtSpace();

							return HandleFloatingPt( xml, state );
					}
				}
				else if ( xml.NodeType == XmlNodeType.EndElement && 
						  xml.Name == TYPE_TAG ) 
				{
					throw new FormatException( TYPE_TAG + " does not contain a valid type group." );
				}
			}

			// shouldn't ever get here
			return null;
		}

		private static Hashtable HandleValueLabels( XmlTextReader xml, 
													ApplianceState state, 
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

						string enablestr = xml.GetAttribute( ENABLE_ATTRIBUTE );

						LabelDictionary label = HandleLabelDict( xml, state, MAP_TAG );

						if ( enablestr != null ) 
						{
							EnableConstraint ec = new EnableConstraint( state, enablestr );
							_constraints.Add( ec );
							label.EnableConstraint = ec;
						}

						labels.Add( index, label );
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

			// shouldn't get here
			return null;
		}
		
		private static IntegerSpace HandleInteger( XmlTextReader xml,
												   ApplianceState state )
		{
			bool lookingMin  = false;
			bool lookingMax  = false;
			bool lookingIncr = false;

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
							lookingMin = true;
							break;

						case MAXIMUM_TAG:
							isRanged = true;
							lookingMax = true;
							break;

						case INCREMENT_TAG:
							isIncrement = true;
							lookingIncr = true;
							break;

						case REFVALUE_TAG:
							string statestr = xml.GetAttribute( STATE_ATTRIBUTE );
							if ( statestr == null )
								throw new FormatException( REFVALUE_TAG + " requires attribute " + STATE_ATTRIBUTE );

							if ( lookingMin ) 
							{
								min = new NumberConstraint( state, statestr );
								_constraints.Add( min );
								lookingMin = false;
							}
							else if ( lookingMax )
							{
								max = new NumberConstraint( state, statestr );
								_constraints.Add( max );
								lookingMax = false;
							}
							else if ( lookingIncr )
							{
								incr = new NumberConstraint( state, statestr );
								_constraints.Add( incr );
								lookingIncr = false;
							}
							break;
					}
				}
				else if ( xml.NodeType == XmlNodeType.Text )
				{
					if ( lookingMin )
						min = new IntNumber( xml.Value );
					else if ( lookingMax )
						max = new IntNumber( xml.Value );
					else if ( lookingIncr )
						incr = new IntNumber( xml.Value );

					lookingMin = lookingMax = lookingIncr = false;
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

			// shouldn't get here
			return null;
		}

		private static FixedPtSpace HandleFixedPt( XmlTextReader xml,
												   ApplianceState state )
		{
			bool lookingPos  = false;
			bool lookingMin  = false;
			bool lookingMax  = false;
			bool lookingIncr = false;

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
							lookingMin = true;
							break;

						case MAXIMUM_TAG:
							isRanged = true;
							lookingMax = true;
							break;

						case INCREMENT_TAG:
							isIncrement = true;
							lookingIncr = true;
							break;

						case REFVALUE_TAG:
							string statestr = xml.GetAttribute( STATE_ATTRIBUTE );
							if ( statestr == null )
								throw new FormatException( REFVALUE_TAG + " requires attribute " + STATE_ATTRIBUTE );

							if ( lookingMin ) 
							{
								min = new NumberConstraint( state, statestr );
								_constraints.Add( min );
								lookingMin = false;
							}
							else if ( lookingMax )
							{
								max = new NumberConstraint( state, statestr );
								_constraints.Add( max );
								lookingMax = false;
							}
							else if ( lookingIncr )
							{
								incr = new NumberConstraint( state, statestr );
								_constraints.Add( incr );
								lookingIncr = false;
							}
							break;
					}
				}
				else if ( xml.NodeType == XmlNodeType.Text )
				{
					if ( lookingPos )
						pos = Int32.Parse( xml.Value );
					else if ( lookingMin )
						min = new DoubleNumber( xml.Value );
					else if ( lookingMax )
						max = new DoubleNumber( xml.Value );
					else if ( lookingIncr )
						incr = new DoubleNumber( xml.Value );

					lookingPos = lookingMin = lookingMax = lookingIncr = false;
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

			// shouldn't get here
			return null;
		}

		private static FloatingPtSpace HandleFloatingPt( XmlTextReader xml, 
														 ApplianceState state )
		{
			bool lookingMin  = false;
			bool lookingMax  = false;

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
							lookingMin = true;
							break;

						case MAXIMUM_TAG:
							isRanged = true;
							lookingMax = true;
							break;

						case REFVALUE_TAG:
							string statestr = xml.GetAttribute( STATE_ATTRIBUTE );
							if ( statestr == null )
								throw new FormatException( REFVALUE_TAG + " requires attribute " + STATE_ATTRIBUTE );

							if ( lookingMin ) 
							{
								min = new NumberConstraint( state, statestr );
								_constraints.Add( min );
								lookingMin = false;
							}
							else if ( lookingMax )
							{
								max = new NumberConstraint( state, statestr );
								_constraints.Add( max );
								lookingMax = false;
							}
							break;
					}
				}
				else if ( xml.NodeType == XmlNodeType.Text )
				{
					if ( lookingMin )
						min = new DoubleNumber( xml.Value );
					else if ( lookingMax )
						max = new DoubleNumber( xml.Value );

					lookingMin = lookingMax = false;
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

			// shouldn't get here
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

	public class SpecParseException : Exception
	{
		/*
		 * Member Variables
		 */

		protected int _lineNumber;


		/*
		 * Constructors
		 */

		public SpecParseException( int lineNumber, string msg, Exception e )
			: base( msg, e )
		{
			_lineNumber = lineNumber;
		}

		public SpecParseException( int lineNumber, string msg )
			: base( msg )
		{
			_lineNumber = lineNumber;
		}

		public SpecParseException( int lineNumber )
			: this( lineNumber, "" )
		{
		}


		/*
		 * Properties
		 */

		public int LineNumber
		{
			get
			{
				return _lineNumber;
			}
		}
	}
}
	