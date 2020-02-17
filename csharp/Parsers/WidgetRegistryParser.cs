using System;
using System.Collections;
using System.IO;
using System.Xml;
using PUC.CIO;
using PUC.Registry;

namespace PUC.Parsers
{
	/// <summary>
	/// Summary description for WidgetRegistryParser.
	/// </summary>
	public class WidgetRegistryParser
	{
		/*
		 * Constants
		 */
		protected const String CONDITION_TAG       = "condition";
		protected const String DECISION_TAG        = "decision";
		protected const String DECISION_LIB_TAG    = "decision-library";
		protected const String TYPE_TAG            = "type";
		protected const String WIDGET_REGISTRY_TAG = "widget-registry";
		protected const String WIDGET_TAG          = "widget";

		protected const String DEF_PACKAGE_ATTRIB  = "default-package";
		protected const String NAME_ATTRIB         = "name";
		protected const String TYPE_ATTRIB         = "type";
		protected const String VALUE_ATTRIB        = "value";


		/*
		 * Static Parsing Methods
		 */
		public static WidgetRegistry Parse( String filename )
		{
			try
			{
				return Parse( new StreamReader( filename ) );
			}
			catch( Exception )
			{
				Globals.GetDefaultLog().AddLogLine( "Widget registry parse failed (file not found?): " + filename );
			}

			return null;
		}

		public static WidgetRegistry Parse( TextReader reader )
		{
			XmlTextReader xml = new XmlTextReader( reader );

			return HandleStart( xml );
		}

		private static WidgetRegistry HandleStart( XmlTextReader xml )
		{
			while( xml.Read() ) 
				if ( xml.NodeType == XmlNodeType.Element &&
					xml.Name == WIDGET_REGISTRY_TAG )
				{
					return HandleWidgetRegistry( xml );
				}

			return null;
		}

		private static WidgetRegistry HandleWidgetRegistry( XmlTextReader xml )
		{
			string name = xml.GetAttribute( NAME_ATTRIB );
			Decision tree = null;

			while( xml.Read() )
				if ( xml.NodeType == XmlNodeType.Element &&
					xml.Name == DECISION_TAG )
				{
					tree = HandleDecision( xml );
					break;
				}

			return new WidgetRegistry( name, tree );
		}

		private static Decision HandleDecision( XmlTextReader xml )
		{
			string type = xml.GetAttribute( TYPE_ATTRIB );
			Hashtable table = new Hashtable();

			while( xml.Read() )
				if ( xml.NodeType == XmlNodeType.Element &&
					xml.Name == CONDITION_TAG )
				{
					HandleCondition( xml, table );
				}
				else if ( xml.NodeType == XmlNodeType.EndElement &&
					      xml.Name == DECISION_TAG )
				{
					break;
				}

			return Decision.GetDecisionFactory( type )( table );
		}

		private static void HandleCondition( XmlTextReader xml, Hashtable table )
		{
			string val = xml.GetAttribute( VALUE_ATTRIB );
			Decision d = null;

			while( xml.Read() )
				if ( xml.NodeType == XmlNodeType.Element )
					switch( xml.Name )
					{
						case DECISION_TAG:
							d = HandleDecision( xml );
							break;
						case WIDGET_TAG:
							d = HandleWidget( xml );
							break;
					}
				else if ( xml.NodeType == XmlNodeType.EndElement &&
					      xml.Name == CONDITION_TAG )
				{
					break;
				}

			table[ val ] = d;
		}

		private static Decision HandleWidget( XmlTextReader xml )
		{
			string cioName = null;

			while( xml.Read() )
				if ( xml.NodeType == XmlNodeType.Text )
					cioName = xml.Value;
				else if ( xml.NodeType == XmlNodeType.EndElement &&
					      xml.Name == WIDGET_TAG )
				{
					break;
				}

			return new WidgetDecision( cioName );
		}
	}
}
