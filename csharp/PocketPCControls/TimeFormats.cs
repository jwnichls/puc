using System;

namespace PocketPCControls
{
	/// <summary>
	/// Summary description for TimeFormats.
	/// </summary>
	public enum TimeUnits : int
	{
		Hours = 0,
		Minutes = 1,
		Seconds = 2,
		Fraction = 3
	}

	public struct TimeFormat
	{
		public bool Valid;
		public int Minimum;
		public int Maximum;
		public int Increment;

		public TimeFormat( bool valid )
		{
			Valid = valid;
			Minimum = 0;
			Maximum = 59;
			Increment = 1;
		}

		public bool Different( TimeFormat format )
		{
			if ( Valid != format.Valid )
				return true;

			if ( Valid )
			{
				if ( Maximum != format.Maximum )
					return true;

				if ( Increment != format.Increment )
					return true;

				if ( Minimum != format.Minimum )
					return true;
			}

			return false;
		}
	}
}
