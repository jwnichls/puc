using System;
using System.Xml;

namespace PUC.PersistentData
{
	/// <summary>
	/// Summary description for DataNodes.
	/// </summary>
	public abstract class DataNode
	{
		public abstract NodeType Type
		{
			get;
		}

		public abstract string GetStringValue();

		public virtual int GetIntValue()
		{
			throw new FormatException("node cannot be converted to int");
		}

		public virtual double GetDoubleValue()
		{
			throw new FormatException("node cannot be converted to double");
		}

		public virtual XmlTextReader GetXmlReader()
		{
			throw new FormatException("node cannot be converted to xml reader");
		}
	}

	public enum NodeType
	{
		Int,
		String,
		Double,
		Xml
	}

	public class StringNode : DataNode
	{
		/*
		 * Member Variables
		 */

		protected string _data;


		/*
		 * Constructor
		 */

		public StringNode( string data )
		{
			_data = data;
		}


		/*
		 * Member Methods
		 */

		public override NodeType Type
		{
			get
			{
				return NodeType.String;
			}
		}

		public override string GetStringValue()
		{
			return _data;
		}

		public override int GetIntValue()
		{
			return Int32.Parse( _data );
		}

		public override double GetDoubleValue()
		{
			return Double.Parse( _data );
		}
	}

	public class IntNode : DataNode
	{
		/*
		 * Member Variables
		 */

		protected int _data;


		/*
		 * Constructor
		 */

		public IntNode( int data )
		{
			_data = data;
		}


		/*
		 * Member Methods
		 */

		public override NodeType Type
		{
			get
			{
				return NodeType.Int;
			}
		}

		public override string GetStringValue()
		{
			return _data.ToString();
		}

		public override int GetIntValue()
		{
			return _data;
		}

		public override double GetDoubleValue()
		{
			return (double)_data;
		}
	}

	public class DoubleNode : DataNode
	{
		/*
		 * Member Variables
		 */

		protected double _data;


		/*
		 * Constructor
		 */

		public DoubleNode( double data )
		{
			_data = data;
		}


		/*
		 * Member Methods
		 */

		public override NodeType Type
		{
			get
			{
				return NodeType.Double;
			}
		}

		public override string GetStringValue()
		{
			return _data.ToString();
		}

		public override int GetIntValue()
		{
			return (int)_data;
		}

		public override double GetDoubleValue()
		{
			return _data;
		}
	}
}
