using System;
using System.Collections;
using System.Drawing;
using PUC.Types;

namespace PUC
{
	/// <summary>
	/// Label dictionaries are the basis of the labeling system
	/// in the PUC interface generator.  This implementation of the
	/// label dictionary stores only the labels usable by the 
	/// graphical interface generator.
	/// </summary>
	public class LabelDictionary
	{
		/*
		 * Static Members & Methods
		 */

		public static UndefinedLabel UndefinedToken
		{
			get
			{
				return UndefinedLabel.GetToken();
			}
		}

		
		/*
		 * Member Variables
		 */
		protected ArrayList _labels;
		protected EnableConstraint _enable;
		protected int _preferredIndex;
		
		
		/*
		 * Constructors
		 */

		public LabelDictionary( ArrayList labels, EnableConstraint enable )
		{
			_labels = (ArrayList)labels.Clone();
			_preferredIndex = 0;
			_enable = enable;
		}

		public LabelDictionary( ArrayList labels, int prefIndex )
		{
			_labels = (ArrayList)labels.Clone();
			_preferredIndex = prefIndex;
			_enable = null;
		}

		public LabelDictionary( ArrayList labels ) 
		{
			_labels = (ArrayList)labels.Clone();
			_preferredIndex = 0;
			_enable = null;
		}

		public LabelDictionary( EnableConstraint enable )
		{
			_labels = new ArrayList();
			_preferredIndex = 0;
			_enable = enable;
		}

		public LabelDictionary()
		{
			_labels = new ArrayList();
			_preferredIndex = 0;
			_enable = null;
		}


		/*
		 * Member Methods
		 */

		public void RegisterConstraints( ApplianceState state )
		{
			if ( _enable != null )
				new TypeConstraintListener( state, _enable );

			RegisterConstraints( (ApplianceObject)state );
		}

		public void RegisterConstraints( ApplianceObject obj )
		{
			IEnumerator label = _labels.GetEnumerator();
			while( label.MoveNext() )
			{
				if ( label.Current is StringConstraint )
				{
					new LabelConstraintListener( obj, (StringConstraint)label.Current );
				}
			}
		}

		public int PreferredIndex
		{
			get
			{
				return _preferredIndex;
			}
			set
			{
				_preferredIndex = value;
			}
		}

		public void AddLabel( IPUCString label, bool preferred )
		{
			AddLabel( label );

			if ( preferred )
				_preferredIndex = _labels.Count - 1;
		}

		public void AddLabel( IPUCString label )
		{
			_labels.Add( label );
		}

		public bool Enabled
		{
			get
			{
				return _enable == null || _enable.Value;
			}
		}

		public string GetLabelByPixelLength( Font f, int pixels )
		{
			if ( _labels.Count == 0 )
				throw new ArgumentException( "No labels in this library!" );

			IEnumerator e = _labels.GetEnumerator();
			e.MoveNext();

			string result = e.Current.ToString();
			int closest = (int)Globals.MeasureString( e.Current.ToString(), f ).Width - pixels;
			bool foundShorter = closest < 0;
			closest = (int)Math.Abs( closest );

			while( e.MoveNext() )
			{
				int diff = (int)Globals.MeasureString( e.Current.ToString(), f ).Width - pixels;

				if ( foundShorter && diff > 0 ) continue;

				if ( !foundShorter && diff < 0 )
				{
					foundShorter = true;
					closest = Math.Abs( diff );
					result = e.Current.ToString();
					continue;
				}

				diff = Math.Abs( diff );
				
				if ( diff < closest )
				{
					closest = diff;
					result = e.Current.ToString();
				}
			}
			
			return result;
		}

		public string GetLabelByCharLength( int chars )
		{
			if ( _labels.Count == 0 )
				throw new ArgumentException( "No labels in this library!" );

			IEnumerator e = _labels.GetEnumerator();
			e.MoveNext();

			string result = e.Current.ToString();
			int closest = result.Length - chars;
			bool foundShorter = closest < 0;
			closest = Math.Abs( closest );

			while( e.MoveNext() )
			{
				int diff = e.Current.ToString().Length - chars;

				if ( foundShorter && diff > 0 ) continue;

				if ( !foundShorter && diff < 0 )
				{
					foundShorter = true;
					closest = Math.Abs( diff );
					result = e.Current.ToString();
					continue;
				}

				diff = Math.Abs( diff );
				
				if ( diff < closest )
				{
					closest = diff;
					result = e.Current.ToString();
				}
			}
			
			return result;
		}

		public string GetShortestLabel()
		{
			if ( _labels.Count == 0 )
				throw new ArgumentException( "No labels in this library!" );

			IEnumerator e = _labels.GetEnumerator();
			e.MoveNext();

			string result = e.Current.ToString();
			int closest = result.Length;

			while( e.MoveNext() )
			{
				if ( e.Current.ToString().Length < closest )
				{
					closest = e.Current.ToString().Length;
					result = e.Current.ToString();
				}
			}
			
			return result;
		}

		public string GetPreferredLabel()
		{
			return (string)_labels[ _preferredIndex ];
		}

		public string GetFirstLabel() 
		{
			return _labels[0].ToString();
		}

		public EnableConstraint EnableConstraint
		{
			get
			{
				return _enable;
			}
			set
			{
				_enable = value;
			}
		}

		public int Count
		{
			get
			{
				return _labels.Count;
			}
		}

		public bool Contains( string label )
		{
			IEnumerator e = _labels.GetEnumerator();
			while( e.MoveNext() )
			{
				if ( label == e.Current.ToString() )
					return true;
			}

			return false;
		}


		/*
		 * Protected Class
		 */
		public class UndefinedLabel
		{
			/*
			 * Static Members & Methods
			 */

			protected static UndefinedLabel _token = new UndefinedLabel();

			public static UndefinedLabel GetToken()
			{
				return _token;
			}


			/*
			 * Constructor
			 */

			private UndefinedLabel()
			{
			}
		}
	}
}
