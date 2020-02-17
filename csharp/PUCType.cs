using System;
using System.Collections;
using PUC.Types;

namespace PUC
{
	/// <summary>
	/// The PUCType defines a type as described in the PUC
	/// specification language.
	/// </summary>
	public class PUCType : ICloneable
	{
		/*
		 * Member Variables
		 */
		protected int			_lineNumber;
		protected ValueSpace	_valueSpace;
		protected ValueSpace	_expectedSpace;
		protected Hashtable		_valueLabels;

		/*
		 * Constructors
		 */
		public PUCType( int lineNumber, ValueSpace valspc, Hashtable labels, ValueSpace exp )
		{
			_lineNumber = lineNumber;
			_valueSpace = (ValueSpace)valspc.Clone();
			if ( labels != null ) _valueLabels = (Hashtable)labels.Clone();
			else _valueLabels = null;
			if ( exp != null ) _expectedSpace = (ValueSpace)exp.Clone();
			else _expectedSpace = null;
		}

		public PUCType( int lineNumber, ValueSpace space, Hashtable labels )
			: this( lineNumber, space, labels, null )
		{
		}

		public PUCType( int lineNumber, ValueSpace valspc )
			: this( lineNumber, valspc, null )
		{
		}

		public PUCType( ValueSpace valspc )
			: this( -1, valspc )
		{
		}

		/*
		 * Member Methods
		 */
		public int LineNumber
		{
			get
			{
				return _lineNumber;
			}
		}

		public ValueSpace ValueSpace
		{
			get
			{
				return _valueSpace;
			}
		}

		public ValueSpace ExpectedSpace
		{
			get
			{
				return _expectedSpace;
			}
		}

		public Hashtable ValueLabels
		{
			get
			{
				return _valueLabels;
			}
		}

		public void RegisterConstraints( ApplianceState state )
		{
			_valueSpace.RegisterConstraints( state );

			if ( _valueLabels != null )
			{
				IEnumerator labeldict = _valueLabels.Values.GetEnumerator();
				while( labeldict.MoveNext() )
					((LabelDictionary)labeldict.Current).RegisterConstraints( state );
			}
		}

		public object Clone()
		{
			PUCType newtype = new PUCType( (ValueSpace)_valueSpace.Clone() );

			newtype._lineNumber = _lineNumber;
			newtype._valueLabels = _valueLabels == null ? null : (Hashtable)_valueLabels.Clone();
			newtype._expectedSpace = _expectedSpace == null ? null :
										(ValueSpace)_expectedSpace.Clone();

			return newtype;
		}
	}
}
