using System;
using System.IO;
using System.Windows.Forms;
using System.Xml;

using DebugServer;

namespace PUC.Parsers
{
	/// <summary>
	/// Summary description for Converter20to21.
	/// </summary>
	public class Converter20to21
	{
		/*
		 * Constants
		 */

		// version accepted by this parser
		public const string SPEC_PARSER_VERSION = "PUC/2.0";
		public const string NEW_SPEC_VERSION	= "PUC/2.1";

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
		public const string NEW_VALUE_LABEL_TAG = "value-labels";
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
		public const string OLD_ITEMS_TAG		= "items";
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
		public const string OLD_SELECTION_TYPE  = "selectionType";
		public const string SELECTION_TYPE_TAG  = "selection-type";
		public const string STATIC_TAG			= "static";

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
		public const string OLD_TYPE_NAME_ATTR  = "typeName";
		public const string TYPE_NAME_ATTRIBUTE = "type-name";
		public const string PREFERRED_ATTRIBUTE = "preferred";
		public const string VALUE_ATTRIBUTE		= "value";

		public const string IGNORE_ATTR_ALL		= "all";
		public const string IGNORE_ATTR_PRNT	= "parent";

		public const string ACCESS_READ_ONLY	= "ReadOnly";
		public const string ACCESS_WRITE_ONLY	= "WriteOnly";
		public const string NEW_READ_ONLY		= "read-only";

		public const string SEL_TYPE_ONE		= "one";
		public const string SEL_TYPE_MULTIPLE	= "multiple";

		public const string ATTR_VALUE_TRUE		= "true";
		public const string ATTR_VALUE_FALSE	= "false";

	
		/*
		 * Static Member Methods
		 */
	
		/// <summary>
		/// Method with user interface for converting a specification.
		/// </summary>
		public static void Convert()
		{
			// ask user for the file to open
			OpenFileDialog openDlg = new OpenFileDialog();
			openDlg.Filter = "xml files (*.xml)|*.xml|All files (*.*)|*.*";

			if ( openDlg.ShowDialog() != DialogResult.OK )
				return;

			// ask user for the file to save as...
			SaveFileDialog saveDlg = new SaveFileDialog();
			saveDlg.Filter = "xml files (*.xml)|*.xml|All files (*.*)|*.*";

			if ( saveDlg.ShowDialog() != DialogResult.OK )
				return;

			ConvertFile( openDlg.FileName, saveDlg.FileName );
		}

		public static void ConvertFiles()
		{
			// ask user for the file to open
			OpenFileDialog openDlg = new OpenFileDialog();
			openDlg.Filter = "xml files (*.xml)|*.xml|All files (*.*)|*.*";
			openDlg.Multiselect = true;

			if ( openDlg.ShowDialog() != DialogResult.OK )
				return;

			for( int i = 0; i < openDlg.FileNames.Length; i++ )
			{
				string filename = openDlg.FileNames.GetValue( i ).ToString();
				filename = filename.Substring( 0, filename.Length - 3 );

				ConvertFile( filename + "xml", filename + "new.xml" );
			}
		}

		/// <summary>
		/// Method for converting a specification.
		/// </summary>
		/// <param name="oldfile"></param>
		/// <param name="newfile"></param>
		public static void ConvertFile( string oldfile, string newfile )
		{
			XmlTextReader xmlIn = null;
			XmlTextWriter xmlOut = null;

			ProgressBarDlg dlg = new ProgressBarDlg();
			dlg.Text = "Converting " + oldfile.Substring( oldfile.LastIndexOf("\\")+1 ) + "...";
			dlg.OkayPressed += new EventHandler(dlg_OkayPressed);
			dlg.Show();

			try
			{
				FileStream fin = new FileStream( oldfile, FileMode.Open );

				xmlIn = new XmlTextReader( new StreamReader( fin ) );
				xmlIn.XmlResolver = null;

				dlg.Minimum = 0;
				dlg.Value = 0;
				dlg.Maximum = (int)fin.Length;

				FileStream fout = new FileStream( newfile, FileMode.Create );

				xmlOut = new XmlTextWriter( fout, System.Text.Encoding.UTF8 );
				xmlOut.Formatting = Formatting.Indented;
				xmlOut.Indentation = 2;
				xmlOut.WriteStartDocument();

				while( xmlIn.Read() && !dlg.Canceling )
				{
					if ( xmlIn.NodeType == XmlNodeType.Element )
					{
						switch( xmlIn.Name )
						{
							case MAP_TAG:
								HandleMap( xmlIn, xmlOut );
								break;

							case MINIMUM_TAG:
								HandleStaticTag( xmlIn, xmlOut );
								break;

							case MAXIMUM_TAG:
								HandleStaticTag( xmlIn, xmlOut );
								break;

							case INCREMENT_TAG:
								HandleStaticTag( xmlIn, xmlOut );
								break;

							case AVERAGE_TAG:
								HandleStaticTag( xmlIn, xmlOut );
								break;

							case EQUALS_TAG:
								HandleStaticTag( xmlIn, xmlOut );
								break;

							case GREATERTHAN_TAG:
								HandleStaticTag( xmlIn, xmlOut );
								break;

							case LESSTHAN_TAG:
								HandleStaticTag( xmlIn, xmlOut );
								break;

							case VALUE_LABEL_TAG:
								xmlOut.WriteStartElement( NEW_VALUE_LABEL_TAG );
								break;

							case OLD_ITEMS_TAG:
								xmlOut.WriteStartElement( ITEM_COUNT_TAG );
								break;

							case OLD_SELECTION_TYPE:
								xmlOut.WriteStartElement( SELECTION_TYPE_TAG ); 
								break;

							case OBJECT_REF_TAG:
								// this element has been removed
								break;

							case VALUE_SPACE_TAG:
								// this element has been removed
								break;

							default:
								bool empty = xmlIn.IsEmptyElement;
								xmlOut.WriteStartElement( xmlIn.Name );

								// write out the attributes of this node
								if ( xmlIn.HasAttributes )
								{
									xmlIn.MoveToFirstAttribute();
									for( int i = 0; i < xmlIn.AttributeCount; i++ )
									{
										switch ( xmlIn.Name )
										{
											case ACCESS_ATTRIBUTE:
												if ( xmlIn.Value == ACCESS_READ_ONLY )
													xmlOut.WriteAttributeString( ACCESS_ATTRIBUTE, NEW_READ_ONLY );
												else
													throw new SpecParseException( xmlIn.LineNumber, ACCESS_ATTRIBUTE + " with unexpected value." );
												break;

											case PREFERRED_ATTRIBUTE:
												// this attribute doesn't exist anymore
												break;

											case VERSION_ATTRIBUTE:
												xmlOut.WriteAttributeString( VERSION_ATTRIBUTE, NEW_SPEC_VERSION );
												break;

											case OLD_TYPE_NAME_ATTR:
												xmlOut.WriteAttributeString( TYPE_NAME_ATTRIBUTE, xmlIn.Value );
												break;

											default:
												xmlOut.WriteAttributeString( xmlIn.Name, xmlIn.Value );
												break;
										}
										xmlIn.MoveToNextAttribute();
									}
								}

								// we don't get an end element if this is an 
								// empty element
								if ( empty )
									xmlOut.WriteEndElement();

								break;
						}
					}
					else if ( xmlIn.NodeType == XmlNodeType.Text )
					{
						xmlOut.WriteString( xmlIn.Value );
					}
					else if ( xmlIn.NodeType == XmlNodeType.EndElement )
					{
						if ( xmlIn.Name != VALUE_SPACE_TAG )
							xmlOut.WriteEndElement();
					}

					dlg.Value = (int)fin.Position;
				}

				xmlOut.WriteEndDocument();
			}
			catch( Exception e )
			{
				MessageBox.Show( "Exception: " + e.Message );
			}
			finally
			{
				if ( xmlIn != null )
					xmlIn.Close();

				if ( xmlOut != null )
					xmlOut.Close();

				if ( dlg.Canceling )
				{
					File.Delete( newfile );
					dlg.Dispose();
				}
			}
		}

		private static void HandleStaticTag( XmlTextReader xmlIn, XmlTextWriter xmlOut )
		{
			string endTag = xmlIn.Name;
			xmlOut.WriteStartElement( xmlIn.Name );

			// write out the attributes of this node
			if ( xmlIn.HasAttributes )
			{
				xmlIn.MoveToFirstAttribute();
				for( int i = 0; i < xmlIn.AttributeCount; i++ )
				{
					xmlOut.WriteAttributeString( xmlIn.Name, xmlIn.Value );
					xmlIn.MoveToNextAttribute();
				}
			}

			while( xmlIn.Read() )
			{
				if ( xmlIn.NodeType == XmlNodeType.Element )
				{
					switch( xmlIn.Name )
					{
						case REFVALUE_TAG:
							string state = xmlIn.GetAttribute( STATE_ATTRIBUTE );
							xmlOut.WriteStartElement( REFVALUE_TAG );
							xmlOut.WriteAttributeString( STATE_ATTRIBUTE, state );
							xmlOut.WriteEndElement();
							break;
					}
				}
				else if ( xmlIn.NodeType == XmlNodeType.Text )
				{
					// write out a static tag
					xmlOut.WriteStartElement( STATIC_TAG );
					xmlOut.WriteAttributeString( VALUE_ATTRIBUTE, xmlIn.Value );
					xmlOut.WriteEndElement();
				}
				else if ( xmlIn.NodeType == XmlNodeType.EndElement )
				{
					xmlOut.WriteEndElement();

					if ( xmlIn.Name == endTag )
						return;
				}
			}
		}

		private static void HandleMap( XmlTextReader xmlIn, XmlTextWriter xmlOut )
		{
			string indexstr = xmlIn.GetAttribute( INDEX_ATTRIBUTE );
			string enablestr = xmlIn.GetAttribute( ENABLE_ATTRIBUTE );

			xmlOut.WriteStartElement( MAP_TAG );
			xmlOut.WriteAttributeString( INDEX_ATTRIBUTE, indexstr );

			// start LABELS_TAG element
			xmlOut.WriteStartElement( LABELS_TAG );

			while( xmlIn.Read() )
			{
				if ( xmlIn.NodeType == XmlNodeType.Element )
				{
					bool empty = xmlIn.IsEmptyElement;
					xmlOut.WriteStartElement( xmlIn.Name );

					// write out the attributes of this node
					if ( xmlIn.HasAttributes )
					{
						xmlIn.MoveToFirstAttribute();
						for( int i = 0; i < xmlIn.AttributeCount; i++ )
						{
							switch ( xmlIn.Name )
							{
								case ACCESS_ATTRIBUTE:
									if ( xmlIn.Value == ACCESS_READ_ONLY )
										xmlOut.WriteAttributeString( ACCESS_ATTRIBUTE, NEW_READ_ONLY );
									else
										throw new SpecParseException( xmlIn.LineNumber, ACCESS_ATTRIBUTE + " with unexpected value." );
									break;

								case PREFERRED_ATTRIBUTE:
									// this attribute doesn't exist anymore
									break;

								default:
									xmlOut.WriteAttributeString( xmlIn.Name, xmlIn.Value );
									break;
							}
							xmlIn.MoveToNextAttribute();
						}
					}

					// we don't get an end element if this is an 
					// empty element
					if ( empty )
						xmlOut.WriteEndElement();
				}
				else if ( xmlIn.NodeType == XmlNodeType.Text )
				{
					xmlOut.WriteString( xmlIn.Value );
				}
				else if ( xmlIn.NodeType == XmlNodeType.EndElement )
				{
					if ( xmlIn.Name == MAP_TAG )
						break;

					xmlOut.WriteEndElement();
				}
			}
			
			// end LABELS_TAG element
			xmlOut.WriteEndElement();

			// if necessary, convert enable attribute into an active-if clause
			if ( enablestr != null )
			{
				xmlOut.WriteStartElement( ACTIVEIF_TAG );
				xmlOut.WriteStartElement( EQUALS_TAG );
				
				bool val = !enablestr.StartsWith( "!" );
				string state = val ? enablestr : enablestr.Substring( 1 );

				xmlOut.WriteAttributeString( STATE_ATTRIBUTE, state );

				xmlOut.WriteStartElement( STATIC_TAG );
				xmlOut.WriteAttributeString( VALUE_ATTRIBUTE, val.ToString() );
				xmlOut.WriteEndElement(); // STATIC_TAG

				xmlOut.WriteEndElement(); // EQUALS_TAG
				xmlOut.WriteEndElement(); // ACTIVEIF_TAG
			}

			xmlOut.WriteEndElement(); // MAP_TAG
		}

		private static void dlg_OkayPressed(object sender, EventArgs e)
		{
			((ProgressBarDlg)sender).Dispose();
		}
	}
}
