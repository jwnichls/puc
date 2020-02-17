using System;
using System.Collections;
using System.Windows.Forms;

using PUC;
using PUC.CIO;
using PUC.Communication;
using PUC.Layout;

namespace PUC.CIO.List
{
	/// <summary>
	/// Summary description for OneDimCategoricalList.
	/// </summary>
	public class OneDimCategoricalList : ControlBasedCIO
	{
		/*
		 * Constants
		 */

		public const string CIO_NAME = "OneDimCategoricalListCIO";

		protected const int ROW_SPACING = 5; // TODO: Calculate the actual value for this


		/*
		 * Static Methods
		 */

		public static ConcreteInteractionObject CreateListCIO( PUC.ListGroupNode g )
		{
			return new OneDimCategoricalList( g );
		}


		/*
		 * Member Variables
		 */

		protected ListGroupNode		_listGroup;
		protected LabelDictionary	_listLabels;

		protected Label				_listLabel;
		protected ListView			_listView;

		protected ArrayList			_states;


		/*
		 * Constructor
		 */

		public OneDimCategoricalList( ListGroupNode g )
			: base( new Panel() )
		{
			Panel p = (Panel)this.GetControl();

			_listGroup = g;
			_listLabels = _listGroup.Labels;

			// for efficiency, disconnect data windows to internal states
			_listGroup.DataWindow.Clear();

			System.Drawing.SizeF size;

			_listView = new ListView();
			_listView.View = View.Details;
			_listView.FullRowSelect = true;
			p.Controls.Add( _listView );

			if ( _listLabels != null )
			{
				_listLabel = new Label();
				_listLabel.Text = _listLabels.GetShortestLabel();
				_listLabel.Location = new System.Drawing.Point( 0, 0 );
				size = Globals.MeasureString( _listLabel.Text, _listLabel.Font );
				_listLabel.Size = new System.Drawing.Size( (int)size.Width, (int)size.Height );
				p.Controls.Add( _listLabel );
				_listView.Location = new System.Drawing.Point( 0, _listLabel.Height + 3 );
			}
			else
			{
				_listView.Location = new System.Drawing.Point( 0, 0 );
			}

			p.Resize += new EventHandler(p_Resize);

			// determine if multiple selection is being used

#if POCKETPC
			// the CheckBoxes property allows multiple selection on a PocketPC
			_listView.CheckBoxes 
#endif
#if DESKTOP
			_listView.MultiSelect
#endif
				= _listGroup.SelectionType == SelectionType.Multiple;

			// extract the states

			_states = new ArrayList();
			extractStates( _listGroup, true );

			// identify the columns and set them up

			IEnumerator states = _states.GetEnumerator();
			while( states.MoveNext() )
			{
				ApplianceState s = (ApplianceState)states.Current;
				ColumnHeader ch = new ColumnHeader();
				ch.Text = s.Labels.GetShortestLabel();
				ch.Width = 20;
				_listView.Columns.Add( ch );
			}

			// hook into appropriate events

			_listGroup.ListDataChanged += new ListEvent(_listGroup_ListDataChanged);
			_listGroup.SelectionState.ValueChangedEvent += new PUC.ApplianceState.ValueChangedHandler(SelectionState_ValueChangedEvent);
			_listView.SelectedIndexChanged += new EventHandler(_listView_SelectedIndexChanged);
			
		}


		/*
		 * Member Methods
		 */

		protected void extractStates( BranchGroupNode bg, bool levelOne )
		{
			IEnumerator e = bg.Children.GetEnumerator();
			while( e.MoveNext() )
			{
				GroupNode ig = (GroupNode)e.Current;

				if ( levelOne && 
					 ( ig.Name == ListGroupNode.LIST_SELECTION_STATE ||
					   ig.Name == ListGroupNode.LIST_LENGTH_STATE ) )
					continue;

				if ( ig.IsObject() && ((ObjectGroupNode)ig).Object.State )
						_states.Add( ((ObjectGroupNode)ig).Object );

				if ( ! ig.IsObject() )
					extractStates( (BranchGroupNode)e.Current, false );
			}
		}

		public override void FinalSizeNotify()
		{
			int width = _listView.Width;
			int colWidth = width / _listView.Columns.Count;

			for( int i = 0; i < _states.Count; i++ )
			{
				_listView.Columns[i].Width = colWidth;
				_listView.Columns[i].Text = 
					((ApplianceState)_states[ i ]).Labels.GetLabelByPixelLength( _listView.Font, colWidth );
			}
		}

		protected int getMinimumWidth()
		{
			int width = 0;

			IEnumerator e = _states.GetEnumerator();
			while( e.MoveNext() )
			{
				System.Drawing.SizeF s = 
					Globals.MeasureString( ((ApplianceObject)e.Current).Labels.GetShortestLabel(), _listView.Font );

				width += (int)s.Width;
			}

			return width;
		}

		protected int getRowHeight()
		{
			System.Drawing.SizeF s = Globals.MeasureString( "Sample Test", _listView.Font );

			return (int)s.Height + ROW_SPACING;
		}

		public override System.Drawing.Size GetMinimumSize()
		{
			// We arbitrarily choose the minimum height as 6 rows, 
			// which hopefully corresponds to the column headers plus
			// 5 items of data.
			return new System.Drawing.Size( getMinimumWidth(), getRowHeight() * 6 );
		}

		public override PreferredSize GetPreferredSize()
		{
			int height = PreferredSize.INFINITE;

			try
			{
				if ( _listGroup.ItemCount != null )
					height = _listGroup.ItemCount.GetIntValue() * getRowHeight();
				else if ( _listGroup.Maximum != null )
					height = _listGroup.Maximum.GetIntValue() * getRowHeight();
			}
			catch( Exception )
			{
			}

			return new PreferredSize( PreferredSize.INFINITE, height );
		}

		public override bool PrefersFullWidth()
		{
			return true;
		}

		public override System.Drawing.Point GetControlOffset()
		{
			string lbl = _listLabels != null ? _listLabels.GetShortestLabel() : "A test labely";
			System.Drawing.SizeF s = Globals.MeasureString( lbl, _listView.Font );	

			return new System.Drawing.Point( 0, (int)s.Height );
		}

		protected void refreshListData( VariableTable varTable, int start, int length )
		{
			Hashtable data = (Hashtable)varTable.GetListStructure( _listGroup.FullPath );

			IEnumerator e = _states.GetEnumerator();
			e.MoveNext();
			refreshStateData( (ApplianceState)e.Current, data, start, length, true );

			// process the rest of the states
			while( e.MoveNext() )
				refreshStateData( (ApplianceState)e.Current, data, start, length, false );
		}

		protected void refreshStateData( ApplianceState state, Hashtable data, int start, int length, bool firstItem )
		{
			// first get the state name - the list name (+1 is for separator character)
			string noListName = state.FullName.Substring( _listGroup.FullPath.Length + 1 );
			string[] names = noListName.Split( VariableTable.NAME_SEPARATORS );

			ArrayList list = (ArrayList)data[ names[ 0 ] ];
			for( int i = 0; i < length; i++ )
			{
				object current = list[ start + i ];
				for( int j = 1; j < names.Length; j++ )
					current = ((Hashtable)current)[ names[ j ] ];

				ListViewItem lvi = _listView.Items[ start + i ];
				if ( firstItem )
					lvi.Text = current.ToString();
				else
				{
					ListViewItem.ListViewSubItem lvsi = new System.Windows.Forms.ListViewItem.ListViewSubItem();
					lvsi.Text = current.ToString();
					lvi.SubItems.Add( lvsi );
				}
			}
		}

		private void _listGroup_ListDataChanged(ListGroupNode listGroup, ListEventArgs a)
		{
			System.Diagnostics.Debug.Assert( listGroup == _listGroup );
				
			_listView.BeginUpdate();

			if ( a is RefreshAllListEventArgs )
			{
				_listView.Items.Clear();
				int length = (int)_listGroup.LengthState.Value;

				for( int i = 0; i < length; i++ )
				{
					_listView.Items.Add( new ListViewItem() );
				}

				refreshListData( _listGroup.Appliance.VariableTable, 0, length );
			}
			else if ( a is DeleteListEventArgs )
			{
				DeleteListEventArgs da = (DeleteListEventArgs)a;

				// +1 because indicies are inclusive
				for( int i = 0; i < da.Length; i++ )
					_listView.Items.RemoveAt( da.Start );
			}
			else if ( a is ChangeListEventArgs )
			{
				ChangeListEventArgs ca = (ChangeListEventArgs)a;

				int lenDiff;
				if ( ca.OldLength < ca.NewLength )
				{
					lenDiff = ca.NewLength - ca.OldLength;
					for( int i = 0; i < lenDiff; i++ )
						_listView.Items.Insert( ca.Start + i, new ListViewItem() );

					for( int i = lenDiff; i < ca.NewLength; i++ )
						_listView.Items[ ca.Start + i ].SubItems.Clear();
				}
				else
				{
					lenDiff = ca.OldLength - ca.NewLength;
					for( int i = 0; i < lenDiff; i++ )
						_listView.Items.RemoveAt( ca.Start + i );

					for( int i = ca.Start; i < (ca.Start + ca.NewLength); i++ )
						_listView.Items[ i ].SubItems.Clear();
				}

				refreshListData( _listGroup.Appliance.VariableTable, ca.Start, ca.NewLength );
			}
			else if ( a is InsertListEventArgs )
			{
				InsertListEventArgs ia = (InsertListEventArgs)a;

				for( int i = 0; i < ia.ItemCount; i++ )
					_listView.Items.Insert( ia.InsertAfter + i, new ListViewItem() );

				refreshListData( _listGroup.Appliance.VariableTable, ia.InsertAfter, ia.ItemCount );
			}

			_listView.EndUpdate();
		}

		private void SelectionState_ValueChangedEvent(ApplianceState s)
		{
			if ( _listGroup.SelectionType == SelectionType.One )
			{
				int selIndex = (int)_listGroup.SelectionState.Value;
				_listView.Items[ selIndex ].Selected = true;
			}
			else
			{
				// multiple selections
				ArrayList data = (ArrayList)s.VariableTable.GetListStructure( _listGroup.SelectionState.FullName );
				int selLength = (int)_listGroup.SelectionLengthState.Value;
				Hashtable selected = new Hashtable( selLength );
				for( int i = 0; i < selLength; i++ )
					selected.Add( (int)data[ i ], true );

				for( int i = 0; i < _listView.Items.Count; i++ )
					_listView.Items[ i ].Selected = ( selected[ i ] != null );
			}
		}

		private void _listView_SelectedIndexChanged(object sender, EventArgs e)
		{
			if ( _listGroup.SelectionType == SelectionType.One )
			{
				if ( _listView.SelectedIndices.Count == 0 )
					return;

				_listGroup.SelectionState.RequestChange( _listView.SelectedIndices[ 0 ] );
			}
			else
			{
				// multiple selections

				ListData data = new ListData( _listGroup.FullPath + "." + 
											 ListGroupNode.LIST_SELECTION_STATE );

				for( int i = 0; i < _listView.SelectedIndices.Count; i++ )
				{
					MultipleValueData values = new MultipleValueData();
					values.AddValue( new ValueData( ListGroupNode.LIST_SELECTION_STATE,
													i.ToString() ) );
				}

				_listGroup.RequestChange( data );
			}
		}

		private void p_Resize(object sender, EventArgs e)
		{
			if ( _listLabel != null )
			{
				_listLabel.Text = _listLabels.GetLabelByPixelLength( _listLabel.Font, GetControl().Width ) + ":";
				System.Drawing.SizeF size = Globals.MeasureString( _listLabel.Text, _listLabel.Font );
				_listLabel.Size = new System.Drawing.Size( GetControl().Width, (int)size.Height );

				_listView.Location = new System.Drawing.Point( 0, _listLabel.Height + 3 );
				_listView.Size = new System.Drawing.Size( GetControl().Width, GetControl().Height - _listView.Location.Y );
			}
			else
			{
				_listView.Size = new System.Drawing.Size( GetControl().Width, GetControl().Height );
			}
		}
	}
}
