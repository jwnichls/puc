using System;
using System.Collections;
using System.Drawing;

using PUC.CIO;

namespace PUC.Layout
{
	/// <summary>
	/// This is a collection of reusable static methods 
	/// that are used by other components to perform 
	/// layout.  This class may not be instantiated.
	/// </summary>
	public class LayoutAlgorithms
	{
		/*
		 * Constructor
		 */

		// to prevent instantiations
		private LayoutAlgorithms() { }


		/*
		 * Static Methods
		 */

		public static int[] AllocateSizeValues( int bound, int[] minimum, int[] preferred, int spacing )
		{
			if ( minimum.Length != preferred.Length )
				throw new ArgumentException( "minimum and preferred size arrays must be of equal length" );

			int length = minimum.Length;

			// get sum of minimum and preferred heights
			int min = spacing * ( length - 1 );
			int pref = min;
			int infiniteCount = 0;

			for( int i = 0; i < length; i++ )
			{
				min += minimum[ i ];

				if ( preferred[ i ] == PreferredSize.INFINITE )
				{
					// if we see an infinite preferred size, we count it
					// and add the minimum size to the pref count (so we
					// have some idea of how much space it must have)
					infiniteCount++;
					pref += minimum[ i ];
				}
				else
					pref += preferred[ i ];
			}

			// if the allocated size is too small for the minimum sizing,
			// just return the minimum size array.
			if ( min >= bound )
				return minimum;

			// if the allocated size is smaller than the preferred sizing
			// and there are no infinite objects, then add the extra space
			// to 
			if ( pref > bound )
			{
				int extraSpace = bound - min;
				int alloc = 0;

				int[] size_ret = new int[ length ];

				if ( infiniteCount == 0 )
				{
					alloc = extraSpace / length;
					if ( alloc <= 0 )
						return minimum;
					
					// iterate through and add the extra allocation
					// to the size of each item (up to the preferred size
					// amount)
					for( int i = 0; i < length; i++ )
					{
						int diff = preferred[ i ] - minimum[ i ];
						if ( diff < alloc )
 							size_ret[ i ] = preferred[ i ];
						else
							size_ret[ i ] = minimum[ i ] + alloc;
					} 
				}
				else
				{
					// use minimum size for all controls except the infinites
					// allocate all of the extra space to those controls
					alloc = extraSpace / infiniteCount;

					if ( alloc <= 0 )
						return minimum;

					// iterate through, find the infinite controls, and add the
					// alloc to them
					for( int i = 0; i < length; i++ )
					{
						if ( preferred[ i ] == PreferredSize.INFINITE )
							size_ret[ i ] = minimum[ i ] + alloc;
						else
							size_ret[ i ] = minimum[ i ];
					}
				}

				return size_ret;
			}

			if ( infiniteCount == 0 && pref <= bound )
				return preferred;

			if ( pref == bound ) // seems unlikely, but it could happen
			{
				for( int i = 0; i < length && infiniteCount > 0; i++ )
					if ( preferred[ i ] == PreferredSize.INFINITE )
						preferred[ i ] = minimum[ i ];

				return preferred;
			}

			if ( pref < bound )
			{
				// give everything its preferred size, and give the extra space to
				// the controls that ask for infinite space
				int alloc = ( bound - pref ) / infiniteCount;

				for ( int i = 0; i < length; i++ )
					if ( preferred[ i ] == PreferredSize.INFINITE )
						preferred[ i ] = minimum[ i ] + alloc;

				return preferred;
			}

			// shouldn't ever get here
			return null;
		}

		public static int[] AllocateSizeValues( int bound, int[] minimum, int[] preferred, int[] offsets, int spacing, int minOffset )
		{
			if ( minimum.Length != preferred.Length )
				throw new ArgumentException( "minimum and preferred size arrays must be of equal length" );

			int length = minimum.Length;

			// get sum of minimum and preferred heights (including any extra space needed for offsets)
			int min = spacing * ( length - 1 );
			int pref = min;
			int infiniteCount = 0;

			for( int i = 0; i < length; i++ )
			{
				int baselineSpace = 0;

				// minimum size
				min += minimum[ i ];

				if ( ( i + 1 ) < length )
				{
					baselineSpace = ( minimum[ i ] - offsets[ i ] ) + 
						offsets[ i + 1 ] + spacing;

					if ( baselineSpace < minOffset )
						min += minOffset - baselineSpace;
				}

				// preferred size
				if ( preferred[ i ] == PreferredSize.INFINITE )
				{
					// if we see an infinite preferred size, we count it
					// and add the minimum size to the pref count (so we
					// have some idea of how much space it must have)
					infiniteCount++;
					pref += minimum[ i ];
				}
				else
				{
					pref += preferred[ i ];

					if ( ( i + 1 ) < length )
					{
						baselineSpace = ( preferred[ i ] - offsets[ i ] ) + 
							offsets[ i + 1 ] + spacing;

						if ( baselineSpace < minOffset )
							pref += minOffset - baselineSpace;
					}
				}
			}

			// if the allocated size is too small for the minimum sizing,
			// just return the minimum size array.
			if ( min >= bound )
				return minimum;

			// if the allocated size is smaller than the preferred sizing
			// and there are no infinite objects, then add the extra space
			// to 
			if ( pref > bound )
			{
				int extraSpace = bound - min;
				int alloc = 0;

				int[] size_ret = new int[ length ];

				if ( infiniteCount == 0 )
				{
					alloc = extraSpace / length;
					if ( alloc <= 0 )
						return minimum;
					
					// iterate through and add the extra allocation
					// to the size of each item (up to the preferred size
					// amount)
					for( int i = 0; i < length; i++ )
					{
						int diff = preferred[ i ] - minimum[ i ];
						if ( diff < alloc )
							size_ret[ i ] = preferred[ i ];
						else
							size_ret[ i ] = minimum[ i ] + alloc;
					} 
				}
				else
				{
					// use minimum size for all controls except the infinites
					// allocate all of the extra space to those controls
					alloc = extraSpace / infiniteCount;

					if ( alloc <= 0 )
						return minimum;

					// iterate through, find the infinite controls, and add the
					// alloc to them
					for( int i = 0; i < length; i++ )
					{
						if ( preferred[ i ] == PreferredSize.INFINITE )
							size_ret[ i ] = minimum[ i ] + alloc;
						else
							size_ret[ i ] = minimum[ i ];
					}
				}

				return size_ret;
			}

			if ( infiniteCount == 0 && pref <= bound )
				return preferred;

			if ( pref == bound ) // seems unlikely, but it could happen
			{
				for( int i = 0; i < length && infiniteCount > 0; i++ )
					if ( preferred[ i ] == PreferredSize.INFINITE )
						preferred[ i ] = minimum[ i ];

				return preferred;
			}

			if ( pref < bound )
			{
				// give everything its preferred size, and give the extra space to
				// the controls that ask for infinite space
				int alloc = ( bound - pref ) / infiniteCount;

				for ( int i = 0; i < length; i++ )
					if ( preferred[ i ] == PreferredSize.INFINITE )
						preferred[ i ] = minimum[ i ] + alloc;

				return preferred;
			}

			// shouldn't ever get here
			return null;
		}

		public static int[] GetTextHeightOffsets( ControlBasedCIO[] cios )
		{
			int max_offset = 0;
			for ( int i = 0; i < cios.Length; i++ )
				if ( cios[ i ] != null )
					max_offset = Math.Max( max_offset, cios[ i ].GetControlOffset().Y );
			
			int[] ret_offset = new int[ cios.Length ];
			for( int i = 0; i < cios.Length; i++ )
				if ( cios[ i ] != null )
					ret_offset[ i ] = max_offset - cios[ i ].GetControlOffset().Y;
				else
					ret_offset[ i ] = 0;

			return ret_offset;
		}

		public static ControlBasedCIO[] GetArrayFromArrayList( ArrayList cios )
		{
			ControlBasedCIO[] ary = new ControlBasedCIO[ cios.Count ];
			for( int i = 0; i < cios.Count; i++ )
				ary[ i ] = (ControlBasedCIO)cios[ i ];

			return ary;
		}
	}
}
