using System;
using System.Collections;
using System.Drawing;
using System.Windows.Forms;

namespace PhoneControls
{
	/// <summary>
	/// This is a custom control that represents the standard
	/// appearance listview on the Smartphone.
	/// </summary>
	public class PhoneListView : Control
	{
		/*
		 * Constants
		 */

		protected const int    NUM_ITEMS_VISIBLE = 9;
		protected const string MORE_LABEL        = "More...";

		protected const char   BACK_KEY_CHAR     = (char)27;


		/*
		 * Member Variables
		 */

		protected ArrayList _items;
		protected int _selectedIndex;

		protected Bitmap _imageBuffer;

		protected int  _firstIndex;
		protected bool _moreButtonUsed;

		protected bool _activateNow;

		Brush _unselectedBrush = new SolidBrush( Color.Black );
		Brush _selectedBrush = new SolidBrush( Color.White );
		Brush _highlightBrush = new SolidBrush( Color.DarkBlue );
		Brush _nohlightBrush = new SolidBrush( Color.White );
		Brush _disabledBrush = new SolidBrush( Color.Gray );


		/*
		 * Events
		 */

		public event EventHandler         SelectedIndexChanged;
		public event EventHandler         ItemActivated;
		public event EventHandler		  MoreItemActivated;
		public event EventHandler		  BackButtonHandled;
		public event KeyPressEventHandler BackButtonPressed;


		/*
		 * Constructor
		 */

		public PhoneListView()
		{
			this.KeyUp += new KeyEventHandler(this.list_keyUp);
			this.KeyDown += new KeyEventHandler(this.list_keyDown);
			this.KeyPress += new KeyPressEventHandler(this.list_keyPress);
			this.Resize += new EventHandler(this.list_resize);

			_items = new ArrayList();
			_selectedIndex = 0;
			_firstIndex = 0;
			_moreButtonUsed = false;
			_activateNow = false;

			_imageBuffer = new Bitmap( this.Size.Width, this.Size.Height );
		}


		/*
		 * Properties
		 */

		public int SelectedIndex
		{
			get
			{
				if ( _selectedIndex == ( NUM_ITEMS_VISIBLE - 1 ) && _moreButtonUsed )
					return -1;
				else if ( ( _selectedIndex + _firstIndex ) > _items.Count )
					return -1;
				else
					return _selectedIndex + _firstIndex;
			}
			set
			{
				if ( value < 0 || value >= _items.Count )
					throw new IndexOutOfRangeException( "index out of range" );

				// make sure selected index is visible
				if ( value < _firstIndex ||
					 value > ( _firstIndex + ( NUM_ITEMS_VISIBLE - 1 ) ) )
				{
					_firstIndex = value;
					_selectedIndex = 0;
				}
				else
				{
					_selectedIndex = value - _firstIndex;
				}

				this.Invalidate();
			}
		}

		public int Count
		{
			get
			{
				return _items.Count;
			}
		}

		public IPhoneListViewItem this[ int idx ]
		{
			get
			{
				return (IPhoneListViewItem)_items[ idx ];
			}
			set
			{
				_items[ idx ] = value;
				this.Invalidate();
			}
		}


		/*
		 * Member Methods
		 */

		protected void itemChanged( object source, EventArgs e )
		{
			this.Invalidate();
		}

		protected void changeSelectedIndex( int newVal ) 
		{
			_selectedIndex = newVal;

			if ( SelectedIndexChanged != null )
				SelectedIndexChanged( this, new EventArgs() );
		}

		public void AddItem( IPhoneListViewItem item )
		{
			item.Font = this.Font;

			_items.Add( item );
			item.Changed += new EventHandler(this.itemChanged);
			this.Invalidate();
		}

		public void RemoveItem( object obj )
		{
			_items.Remove( obj );
			((IPhoneListViewItem)obj).Changed -= new EventHandler(this.itemChanged);

			if ( _firstIndex >= _items.Count )
				_firstIndex = 0;

			this.Invalidate();
		}

		public void RemoveItemAt( int idx )
		{
			IPhoneListViewItem item = (IPhoneListViewItem)_items[idx];
			_items.RemoveAt( idx );
			item.Changed -= new EventHandler(this.itemChanged);

			if ( _firstIndex >= _items.Count )
				_firstIndex = 0;

			this.Invalidate();
		}

		protected override void OnPaintBackground(PaintEventArgs e)
		{
			// for double-buffering 
		}

		protected override void OnPaint(PaintEventArgs e)
		{
			// used for double-buffering
			Graphics offscreen = Graphics.FromImage( _imageBuffer );

			offscreen.FillRectangle( _nohlightBrush, 0, 0, this.Size.Width, this.Size.Height );

			int itemSpace = this.Size.Height / NUM_ITEMS_VISIBLE;
			int textHeight = (int)e.Graphics.MeasureString( "Hgllyf!", this.Font ).Height;
			int itemOffset = ( itemSpace - textHeight ) / 2;

			int top = itemOffset;
			for( int i = 0; i < ( NUM_ITEMS_VISIBLE - 1 ); i++ )
			{
				if ( ( i + _firstIndex ) >= _items.Count )
					break;

				IPhoneListViewItem item = (IPhoneListViewItem)_items[ i + _firstIndex ];
				item.Width = this.Size.Width;

				drawItemHelper( offscreen, item.Label, item.Enabled, i, top, itemSpace, itemOffset, item.Font );

				top += itemSpace;
			}

			if ( ( ( NUM_ITEMS_VISIBLE ) + _firstIndex ) < _items.Count )
			{
				_moreButtonUsed = true;
				drawItemHelper( offscreen, MORE_LABEL, true, NUM_ITEMS_VISIBLE - 1, top, itemSpace, itemOffset, this.Font );
			}
			else if ( ( ( NUM_ITEMS_VISIBLE ) + _firstIndex ) == _items.Count )
			{
				_moreButtonUsed = false;
				IPhoneListViewItem item = 
					(IPhoneListViewItem)_items[ NUM_ITEMS_VISIBLE - 1 + _firstIndex ];
				item.Width = this.Size.Width;

				drawItemHelper( offscreen, item.Label, item.Enabled, NUM_ITEMS_VISIBLE - 1, top, itemSpace, itemOffset, item.Font );
			}

			e.Graphics.DrawImage( _imageBuffer, 0, 0 );

			if ( _activateNow )
			{
				_activateNow = false;

				IPhoneListViewItem item = 
					(IPhoneListViewItem)_items[ _firstIndex + _selectedIndex ];

				item.Activate();

				if ( ItemActivated != null )
					ItemActivated( this, new EventArgs() );
			}
		}

		private void drawItemHelper( Graphics g, string label, bool enabled, int index, int top, int itemHeight, int itemOffset, Font font ) 
		{
			if ( index == _selectedIndex )
			{
				Brush b = enabled ? _selectedBrush : _disabledBrush;
				g.FillRectangle( _highlightBrush, 0, top, this.Size.Width, itemHeight );
				g.DrawString( (index+1) + "  " + label, font, b, 5, top );
			}
			else
			{
				Brush b = enabled ? _unselectedBrush : _disabledBrush;
				g.DrawString( (index+1) + "  " + label, font, b, 5, top );
			}
		}

		protected void list_resize( object source, EventArgs e )
		{
			if ( _imageBuffer != null )
				_imageBuffer.Dispose();
			
			_imageBuffer = new Bitmap( this.Size.Width, this.Size.Height );
		}

		public void GoBack()
		{
			_firstIndex -= NUM_ITEMS_VISIBLE;
			if ( _firstIndex < 0 )
				_firstIndex = 0;

			changeSelectedIndex( 0 );
			this.Invalidate();
		}

		protected void list_keyPress( object source, KeyPressEventArgs e )
		{
			if ( e.KeyChar == BACK_KEY_CHAR )
			{
				// handle a press of the back key
				if ( _firstIndex > 0 )
				{
					GoBack();
					e.Handled = true;

					if ( BackButtonHandled != null )
						BackButtonHandled( this, new EventArgs() );
				}
				else
				{
					if ( BackButtonPressed != null )
						BackButtonPressed( this, e );
				}
			}
		}

		private void activateMoreButton()
		{
			if ( MoreItemActivated != null )
				MoreItemActivated( this, new EventArgs() );

			// more button activated
			_firstIndex += ( NUM_ITEMS_VISIBLE - 1 );
			changeSelectedIndex( 0 );
			this.Invalidate();
		}

		protected void list_keyUp( object source, KeyEventArgs e )
		{
			/*
			if ( e.KeyCode == Keys.Back )
				MessageBox.Show( "backspace down" );

			e.Handled = true;
			*/
		}

		protected void list_keyDown( object source, KeyEventArgs e )
		{
			switch( e.KeyCode )
			{
				case Keys.Return:
					if ( _selectedIndex == ( NUM_ITEMS_VISIBLE - 1 ) &&
						 _moreButtonUsed )
					{
						activateMoreButton();
					}
					else
					{
						// do something else for other activations
						IPhoneListViewItem item = 
							(IPhoneListViewItem)_items[ _firstIndex + _selectedIndex ];

						item.Activate();

						if ( ItemActivated != null )
							ItemActivated( this, new EventArgs() );
					}
					break;

				case Keys.Up:
					if ( _selectedIndex != 0 )
					{
						changeSelectedIndex( _selectedIndex - 1 );
						this.Invalidate();
						e.Handled = true;
					}
					break;

				case Keys.Down:
					if ( _selectedIndex < ( NUM_ITEMS_VISIBLE - 1 ) &&
						 ( _selectedIndex + _firstIndex ) < ( _items.Count - 1 ) )
					{
						changeSelectedIndex( _selectedIndex + 1 );
						this.Invalidate();
						e.Handled = true;
					}
					break;

				case Keys.D1:
					changeSelectedIndex( 0 );
					_activateNow = true;
					this.Invalidate();
					e.Handled = true;
					break;

				case Keys.D2:
					if ( ( _firstIndex + 1 ) >= _items.Count )
						break;

					changeSelectedIndex( 1 );
					_activateNow = true;
					this.Invalidate();
					e.Handled = true;
					break;

				case Keys.D3:
					if ( ( _firstIndex + 2 ) >= _items.Count )
						break;

					changeSelectedIndex( 2 );
					_activateNow = true;
					this.Invalidate();
					e.Handled = true;
					break;

				case Keys.D4:
					if ( ( _firstIndex + 3 ) >= _items.Count )
						break;

					changeSelectedIndex( 3 );
					_activateNow = true;
					this.Invalidate();
					e.Handled = true;
					break;

				case Keys.D5:
					if ( ( _firstIndex + 4 ) >= _items.Count )
						break;

					changeSelectedIndex( 4 );
					_activateNow = true;
					this.Invalidate();
					e.Handled = true;
					break;

				case Keys.D6:
					if ( ( _firstIndex + 5 ) >= _items.Count )
						break;

					changeSelectedIndex( 5 );
					_activateNow = true;
					this.Invalidate();
					e.Handled = true;
					break;

				case Keys.D7:
					if ( ( _firstIndex + 6 ) >= _items.Count )
						break;

					changeSelectedIndex( 6 );
					_activateNow = true;
					this.Invalidate();
					e.Handled = true;
					break;

				case Keys.D8:
					if ( ( _firstIndex + 7 ) >= _items.Count )
						break;

					changeSelectedIndex( 7 );
					_activateNow = true;
					this.Invalidate();
					e.Handled = true;
					break;
			
				case Keys.D9:
					if ( ( _firstIndex + 8 ) >= _items.Count )
						break;

					changeSelectedIndex( 8 );

					if ( _moreButtonUsed )
						activateMoreButton();
					else
						_activateNow = true;

					this.Invalidate();
					e.Handled = true;

					break;

					/*
				default:
					MessageBox.Show( "Code: " + e.KeyCode.ToString() );
					e.Handled = true;
					break;
					*/
			}			
		}
	}

	public interface IPhoneListViewItem
	{
		/*
		 * Events
		 */
		
		event EventHandler ItemActivated;
		event EventHandler Changed;


		/*
		 * Properties
		 */

		string Label
		{
			get;
		}

		bool Enabled
		{
			get;
			set;
		}

		int Width
		{
			get;
			set;
		}

		System.Drawing.Font Font
		{
			get;
			set;
		}

		/*
		 * Member Methods
		 */

		void Activate();
	}
}
