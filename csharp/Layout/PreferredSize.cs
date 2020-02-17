using System;

namespace PUC.Layout
{
	/// <summary>
	/// Describes the preferred size of an object.  For each dimension
	/// this may either be a concrete value or INFINITE.
	/// </summary>
	public class PreferredSize
	{
		/*
		 * Constants
		 */

		public const int INFINITE = -1;


		/*
		 * Member Variables
		 */

		protected int		_width;
		protected int		_height;


		/*
		 * Constructors
		 */

		public PreferredSize( int width, int height )
		{
			_width = width;
			_height = height;
		}

		public PreferredSize( System.Drawing.Size size )
		{
			_width = size.Width;
			_height = size.Height;
		}


		/*
		 * Properties
		 */

		public int Width
		{
			get
			{
				return _width;
			}
		}

		public int Height
		{
			get
			{
				return _height;
			}
		}
	}
}
