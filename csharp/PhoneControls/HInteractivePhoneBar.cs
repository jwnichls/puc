using System;
using System.Drawing;
using System.Windows.Forms;

namespace PhoneControls
{
	/// <summary>
	/// Summary description for HInteractivePhoneBar.
	/// </summary>
	public class HInteractivePhoneBar : Control
	{
		/*
		 * Member Variables
		 */

		protected HScrollBar _scrollBar;


		/*
		 * Events
		 */

		public event EventHandler ValueChanged;


		/*
		 * Constructor
		 */

		public HInteractivePhoneBar()
		{
			_scrollBar = new HScrollBar();
			this.Controls.Add( _scrollBar );

			_scrollBar.ValueChanged += new EventHandler(_scrollBar_ValueChanged);
			this.Resize += new EventHandler(HInteractivePhoneBar_Resize);
		}


		/*
		 * Properties
		 */

		public int Value
		{
			get
			{
				return _scrollBar.Value;
			}
			set
			{
				_scrollBar.Value = value;
			}
		}

		public int Minimum
		{
			get
			{
				return _scrollBar.Minimum;
			}
			set
			{
				_scrollBar.Minimum = value;
			}
		}

		public int Maximum
		{
			get
			{
				return _scrollBar.Maximum;
			}
			set
			{
				_scrollBar.Maximum = value;
			}
		}

		public int SmallChange
		{
			get
			{
				return _scrollBar.SmallChange;
			}
			set
			{
				_scrollBar.SmallChange = value;
			}
		}

		public int LargeChange
		{
			get
			{
				return _scrollBar.LargeChange;
			}
			set
			{
				_scrollBar.LargeChange = value;
			}
		}


		/*
		 * Event Handlers
		 */

		protected override void OnEnabledChanged(EventArgs e)
		{
			base.OnEnabledChanged (e);

			_scrollBar.Enabled = this.Enabled;
		}

		private void HInteractivePhoneBar_Resize(object sender, EventArgs e)
		{
			_scrollBar.Location = new Point( 1, 2 );
			_scrollBar.Size = new Size( this.Size.Width - 2, this.Size.Height - 4 );
		}

		protected override void OnPaint(PaintEventArgs e)
		{
			if ( this.Focused )
				e.Graphics.DrawRectangle( new Pen( Color.Black ), 0, 0, this.Size.Width - 1, this.Size.Height - 1 );
		}

		protected override void OnGotFocus(EventArgs e)
		{
			base.OnGotFocus (e);

			this.Invalidate();
		}

		protected override void OnLostFocus(EventArgs e)
		{
			base.OnLostFocus (e);

			this.Invalidate();
		}

		protected override void OnKeyDown(KeyEventArgs e)
		{
			int loc = 0;

			switch( e.KeyCode )
			{
				case Keys.Up:
					loc = Parent.Controls.GetChildIndex( this ) - 1;
					for( int i = loc; i >= 0; i-- )
						if ( Parent.Controls[ i ].Enabled )
						{
							Parent.Controls[ i ].Focus();
							break;
						}
					break;

				case Keys.Down:
					loc = Parent.Controls.GetChildIndex( this ) + 1;
					for( int i = loc; i < Parent.Controls.Count; i++ )
						if ( Parent.Controls[ i ].Enabled )
						{
							Parent.Controls[ i ].Focus();
							break;
						}
					break;

				case Keys.Left:
					_scrollBar.Value -= _scrollBar.SmallChange;
					break;

				case Keys.Right:
					_scrollBar.Value += _scrollBar.SmallChange;
					break;

				case Keys.D1:
					_scrollBar.Value = (int)(0.1 * getScrollBarMax());
					break;

				case Keys.D2:
					_scrollBar.Value = (int)(0.2 * getScrollBarMax());
					break;

				case Keys.D3:
					_scrollBar.Value = (int)(0.3 * getScrollBarMax());
					break;

				case Keys.D4:
					_scrollBar.Value = (int)(0.4 * getScrollBarMax());
					break;

				case Keys.D5:
					_scrollBar.Value = (int)(0.5 * getScrollBarMax());
					break;

				case Keys.D6:
					_scrollBar.Value = (int)(0.6 * getScrollBarMax());
					break;

				case Keys.D7:
					_scrollBar.Value = (int)(0.7 * getScrollBarMax());
					break;

				case Keys.D8:
					_scrollBar.Value = (int)(0.8 * getScrollBarMax());
					break;

				case Keys.D9:
					_scrollBar.Value = (int)(0.9 * getScrollBarMax());
					break;

				case Keys.D0:
					_scrollBar.Value = 0;
					break;

				// the # key
				case Keys.F9:
					_scrollBar.Value = (int)getScrollBarMax();
					break;

				// the * key
				case Keys.F8:
					_scrollBar.Value = (int)getScrollBarMax();
					break;
			}
		}

		protected double getScrollBarMax()
		{
			// return (double)( _scrollBar.Maximum - _scrollBar.LargeChange + 1 );
			return _scrollBar.Maximum;
		}

		private void _scrollBar_ValueChanged(object sender, EventArgs e)
		{
			if ( ValueChanged != null )
				ValueChanged( this, e );
		}
	}
}
