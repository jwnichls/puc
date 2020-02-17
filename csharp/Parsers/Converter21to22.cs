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
	public class Converter21to22
	{
		/*
		 * Constants
		 */

		// version accepted by this parser
		public const string SPEC_PARSER_VERSION = "PUC/2.1";
		public const string NEW_SPEC_VERSION	= "PUC/2.2";

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
		public const string EQUALS_TAG			= "equals";
		public const string LESSTHAN_TAG		= "lessthan";
		public const string GREATERTHAN_TAG		= "greaterthan";
		public const string OLD_ITEMS_TAG		= "items";
		public const string ITEM_COUNT_TAG		= "item-count";
		public const string REFVALUE_TAG		= "refvalue";
		public const string NEW_REFVALUE_TAG	= "ref-value";
		public const string REFSTRING_TAG		= "refstring";
		public const string EXPLANATION_TAG		= "explanation";
		public const string PHONETIC_TAG		= "phonetic";
		public const string TTS_TAG				= "text-to-speech";
		public const string APPLY_TYPE_TAG		= "apply-type";
		public const string NUMBER_TAG			= "number";
		public const string DEFINED_TAG			= "defined";
		public const string UNDEFINED_TAG		= "undefined";
		public const string OLD_SELECTION_TYPE  = "selection-type";
		public const string SELECTIONS_TAG		= "selections";
		public const string STATIC_TAG			= "static";
		public const string CONSTANT_TAG		= "constant";
		public const string TYPES_TAG			= "types";
		public const string REQUIRED_IF_TAG		= "required-if";
		public const string NOT_REQUIRED_TAG	= "not-required";
		public const string DEFAULT_VALUE_TAG	= "default-value";

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
		public const string VALUE_ATTRIBUTE		= "value";
		public const string NUMBER_ATTRIBUTE	= "number";

		public const string IGNORE_ATTR_ALL		= "all";
		public const string IGNORE_ATTR_PRNT	= "parent";

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

				bool done = false;
				while( xmlIn.Read() && !dlg.Canceling )
				{
					done = false;

					if ( xmlIn.NodeType == XmlNodeType.Element )
					{
						switch( xmlIn.Name )
						{
							case STATIC_TAG:
								xmlOut.WriteStartElement( CONSTANT_TAG );
								break;

							case REFSTRING_TAG:
								xmlOut.WriteStartElement( NEW_REFVALUE_TAG );
								break;

							case REFVALUE_TAG:
								xmlOut.WriteStartElement( NEW_REFVALUE_TAG );
								break;

							case OLD_SELECTION_TYPE:
								HandleSelectionType( xmlIn, xmlOut );
								done = true;
								break;

							case APPLY_TYPE_TAG:
								HandleTypeTag( xmlIn, xmlOut );
								done = true;
								break;

							case TYPE_TAG:
								HandleTypeTag( xmlIn, xmlOut );
								done = true;
								break;

							default:
								xmlOut.WriteStartElement( xmlIn.Name );
								break;
						}

						if ( done ) continue;

						bool empty = xmlIn.IsEmptyElement;

						// write out the attributes of this node
						if ( xmlIn.HasAttributes )
						{
							xmlIn.MoveToFirstAttribute();
							for( int i = 0; i < xmlIn.AttributeCount; i++ )
							{
								switch ( xmlIn.Name )
								{
									case VERSION_ATTRIBUTE:
										xmlOut.WriteAttributeString( VERSION_ATTRIBUTE, NEW_SPEC_VERSION );
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
						xmlOut.WriteEndElement();
					}
					else if ( xmlIn.NodeType == XmlNodeType.Comment )
					{
						xmlOut.WriteComment( xmlIn.Value );
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

		private static void HandleSelectionType( XmlTextReader xmlIn, XmlTextWriter xmlOut )
		{
			xmlOut.WriteStartElement( SELECTIONS_TAG );

			// write out the attributes of this node
			if ( xmlIn.HasAttributes )
			{
				xmlIn.MoveToFirstAttribute();
				for( int i = 0; i < xmlIn.AttributeCount; i++ )
				{
					if ( xmlIn.Name == TYPE_NAME_ATTRIBUTE )
						xmlOut.WriteAttributeString( NUMBER_ATTRIBUTE, xmlIn.Value );
					else
						xmlOut.WriteAttributeString( xmlIn.Name, xmlIn.Value );

					xmlIn.MoveToNextAttribute();
				}
			}

			xmlOut.WriteEndElement();
		}

		private static void HandleTypeTag( XmlTextReader xmlIn, XmlTextWriter xmlOut )
		{
			string tag = xmlIn.Name;
			xmlOut.WriteStartElement( tag );

			// write out the attributes of this node
			if ( xmlIn.HasAttributes )
			{
				xmlIn.MoveToFirstAttribute();
				for( int i = 0; i < xmlIn.AttributeCount; i++ )
				{
					if ( xmlIn.Name == NAME_ATTRIBUTE )
						xmlOut.WriteAttributeString( TYPE_NAME_ATTRIBUTE, xmlIn.Value );
					else
						xmlOut.WriteAttributeString( xmlIn.Name, xmlIn.Value );

					xmlIn.MoveToNextAttribute();
				}
			}

			if ( tag == APPLY_TYPE_TAG )
				xmlOut.WriteEndElement();
		}

		private static void dlg_OkayPressed(object sender, EventArgs e)
		{
			((ProgressBarDlg)sender).Dispose();
		}
	}
}
