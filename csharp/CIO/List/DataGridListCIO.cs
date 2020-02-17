using System;
using System.Collections;
using System.Data;
using System.Windows.Forms;

using PUC;
using PUC.CIO;
using PUC.Layout;

namespace PUC.CIO.List
{
	/*
	 * TODO
	 * 
	 * Import values into DataTables
	 * Verify and export changed values from DataTable to network
	 * Multiple Selections
	 * Dependencies on list appliance objects
	 */

	/// <summary>
	/// This interaction object links arbitrary list structures with a
	/// DataGrid control that allows the user to arbitrarily manipulate 
	/// the lists.
	/// </summary>
	public class DataGridListCIO : ControlBasedCIO
	{
		/*
		 * Constants
		 */

		public const string CIO_NAME		= "DataGridListCIO";
		public const string INDEX_COL_NAME	= "Index";

		
		/*
		 * Static Methods
		 */

		public static ConcreteInteractionObject CreateListCIO( ListGroupNode g )
		{
			return new DataGridListCIO( g );
		}


		/*
		 * Member Variables
		 */

		protected ListGroupNode		_listGroup;

		protected Label				_listLabel;
		protected DataGrid			_listGrid;

		protected DataSet			_dataSet;

		protected Hashtable			_columnToStateMap;


		/*
		 * Constructor
		 */

		public DataGridListCIO( ListGroupNode g )
			: base( new Panel() )
		{
			Panel p = (Panel)this.GetControl();

			_listGroup = g;

			// 
			// This section creates, places, and sizes (to some extent) the
			// control within the panel.
			//
			System.Drawing.SizeF size;

			_listGrid = new DataGrid();
			p.Controls.Add( _listGrid );

			if ( _listGroup.Labels != null )
			{
				_listLabel = new Label();
				_listLabel.Text = _listGroup.Labels.GetFirstLabel();
				_listLabel.Location = new System.Drawing.Point( 0, 0 );
				
				size = Globals.MeasureString( _listLabel.Text, _listLabel.Font );
				_listLabel.Size = new System.Drawing.Size( (int)size.Width, (int)size.Height );

				p.Controls.Add( _listLabel );

				_listGrid.Location = new System.Drawing.Point( 0, _listLabel.Height + 3 );
			}
			else
			{
				_listGrid.Location = new System.Drawing.Point( 0, 0 );
			}

			p.Resize += new EventHandler(p_Resize);

			
			//
			// Setup the DataGrid to deal with this list structure
			//
			setupDataGrid();

			// 
			// hook into appropriate events
			//

			_listGroup.ListDataChanged += new ListEvent(_listGroup_ListDataChanged);
			_listGroup.SelectionState.ValueChangedEvent += new PUC.ApplianceState.ValueChangedHandler(SelectionState_ValueChangedEvent);
			// TODO: hook into appropriate DataGrid events
		}

		/*
		 * This is a very complex method that translates the group tree 
		 * structure into DataTables that the DataGrid control recognizes.
		 * This method also creates the DataGridTableStyle objects that 
		 * define how each list will be displayed within the control.
		 * 
		 * Here are some conventions that I am using in this method.  
		 * Each DataTable is named for the full path of the list group 
		 * that it represents.  Lists with multiple dimensions are 
		 * represented by a DataTable for each dimension.  Every table has
		 * a column for each of the state variables and commands that it
		 * contains.  In this case, commands are represented as binary 
		 * values within the list.  Every table also has a special column
		 * named "Index" that contains the index of the list item.  This
		 * special column is used with a DataRelation object to create
		 * parent/child table relations that are used to represent the
		 * dimensionality of lists.
		 * 
		 * Each state variable has a DataGridColumnStyle associated with 
		 * it to define how the variable may be modified.  State variables 
		 * with boolean types and commands are represented as 
		 * DataGridBoolColumn objects.  Everything else is represented as a
		 * DataGridTextBoxColumn, possibly with specific formatting set that
		 * is consistent with the type of the state variable.
		 * 
		 * In the future I may look at writing or borrowing implementations 
		 * for other DataGridColumnStyle objects.
		 * 
		 * This method creates the initial table and then calls the helper
		 * method to do the search and create any additional columns or 
		 * tables.
		 */
		protected void setupDataGrid()
		{
			// build DataSet
			_dataSet = new DataSet();
			_columnToStateMap = new Hashtable();

			// create the main table
			DataTable main = new DataTable( _listGroup.FullPath );
			// add the special column with an integer type
			main.Columns.Add( INDEX_COL_NAME, Type.GetType( "System.Int32" ) );
			// set primary key
			main.PrimaryKey = new DataColumn[] { main.Columns[ INDEX_COL_NAME ] };
			// add the table to the DataSet
			_dataSet.Tables.Add( main );

			// create DataGridTableStyle object for main table
			// we must wait to add this TableStyle until after
			// we have added every column to the table and every
			// ColumnStyle to the TableStyle.
			DataGridTableStyle tableStyle = new DataGridTableStyle();
			tableStyle.MappingName = main.TableName;
			// we set ReadOnly to true initially...we must change
			// this setting if any of the contained state variables
			// are not ReadOnly.
			tableStyle.ReadOnly = true;

			ArrayList listTables = new ArrayList();
			listTables.Add( main );

			gridSetupHelper( _listGroup, listTables, main, tableStyle );

			_listGrid.DataSource = main;
			_listGrid.TableStyles.Add( tableStyle );
		}

		protected void gridSetupHelper( BranchGroupNode group, 
										ArrayList listTables,
										DataTable current, 
										DataGridTableStyle style )
		{
			IEnumerator e = group.Children.GetEnumerator();
			while( e.MoveNext() )
			{
				if ( e.Current is ListGroupNode )
				{
					ListGroupNode lg = (ListGroupNode)e.Current;

					// create a new table
					DataTable table = new DataTable( lg.FullPath );
					// create array for storing primary keys
					DataColumn[] keys = new DataColumn[ listTables.Count + 1 ];
					// add the table to the DataSet
					_dataSet.Tables.Add( table );

					IEnumerator list = listTables.GetEnumerator();
					for( int i = 0; list.MoveNext(); i++ )
					{
						DataTable parentTable = (DataTable)list.Current;
						string parentIndexCol = parentTable.TableName + "." + INDEX_COL_NAME;

						// add a column to reference the parent table index
						table.Columns.Add( parentIndexCol, Type.GetType( "System.Int32" ) );

						// add column to primary key array
						keys[ i ] = table.Columns[ parentIndexCol ];
					}

					// add the special column with an integer type
					table.Columns.Add( INDEX_COL_NAME, Type.GetType( "System.Int32" ) );
					table.Columns[ INDEX_COL_NAME ].Unique = true;
					keys[ listTables.Count ] = table.Columns[ INDEX_COL_NAME ];

					// set primary keys
					table.PrimaryKey = keys;

					// determine relation name
					string relationName = lg.Name;
					if ( lg.Labels != null )
						relationName = lg.Labels.GetShortestLabel();
					// make the parent/child DataRelation to the DataSet
					DataRelation r = new DataRelation( relationName, 
						current.Columns[ INDEX_COL_NAME ], 
						table.Columns[ current.TableName + "." + INDEX_COL_NAME ] );
					// add relation to the DataSet
					_dataSet.Relations.Add( r );

					// create the DataGridTableStyle
					DataGridTableStyle tableStyle = new DataGridTableStyle();
					tableStyle.MappingName = table.TableName;
					tableStyle.ReadOnly = true;

					listTables.Add( table );

					gridSetupHelper( lg, listTables, table, tableStyle );

					listTables.RemoveAt( listTables.Count-1 );

					// add TableStyle after all columns have been found
					_listGrid.TableStyles.Add( tableStyle );
				}
				else if ( e.Current is BranchGroupNode )
				{
					gridSetupHelper( (BranchGroupNode)e.Current, listTables, current, style );
				}
				else if ( e.Current is ObjectGroupNode )
				{
					ApplianceObject ao = ((ObjectGroupNode)e.Current).Object;

					string name = ao.FullName.Substring( current.TableName.Length + 1 );
					if ( ao.State )
					{
						DataGridColumnStyle columnStyle;
						Type type;
						
						if ( ((ApplianceState)ao).Type.ValueSpace.Space == 
							PUC.Types.ValueSpace.BOOLEAN_SPACE )
						{
							type = true.GetType();
							columnStyle = new DataGridBoolColumn();
						}
						else
						{
							type = "".GetType();
							columnStyle = new DataGridTextBoxColumn();
						}
						columnStyle.MappingName = name;

						columnStyle.HeaderText = ao.Labels.GetShortestLabel();
						columnStyle.ReadOnly = ((ApplianceState)ao).ReadOnly;

						if ( !columnStyle.ReadOnly )
							style.ReadOnly = false;

						if ( ((ApplianceState)ao).Type.ValueSpace.Space ==
							PUC.Types.ValueSpace.BINARY_SPACE )
							columnStyle.ReadOnly = true;

						DataColumn col = new DataColumn( name, type );
						_columnToStateMap[ col ] = ao;
						current.Columns.Add( col );
						style.GridColumnStyles.Add( columnStyle );
					}
					else if ( ao is ApplianceCommand )
					{
						DataGridBoolColumn columnStyle = new DataGridBoolColumn();
						columnStyle.MappingName = name;
						columnStyle.ReadOnly = style.ReadOnly = false;

						DataColumn col = new DataColumn( name, true.GetType() );
						_columnToStateMap[ col ] = ao;
						current.Columns.Add( col );
						style.GridColumnStyles.Add( columnStyle );
					}
				}
			}
		}


		/*
		 * Overridden CIO Methods
		 */

		public override void FinalSizeNotify()
		{
			// TODO: Implement this (if necessary)
		}

		public override System.Drawing.Point GetControlOffset()
		{
			// TODO: Implement this properly
			return new System.Drawing.Point( 0, 0 );
		}

		public override System.Drawing.Size GetMinimumSize()
		{
			// TODO: Implement this properly
			return new System.Drawing.Size( 200, 200 );
		}

		public override PUC.Layout.PreferredSize GetPreferredSize()
		{
			// TODO: Implement this properly
			return new PreferredSize( PreferredSize.INFINITE, PreferredSize.INFINITE );
		}


		/*
		 * Event Handlers
		 */

		private void p_Resize(object sender, EventArgs e)
		{
			_listGrid.Size = new System.Drawing.Size( GetControl().Width, GetControl().Height - _listGrid.Location.Y );
		}

		protected void refreshListData( ListGroupNode listGroup, DataTable table, ListEventArgs a, int start, int length )
		{
			IndexedDataWindow win = listGroup.DataWindow;
			int[] parentIndices = a.ParentIndices;
			System.Int32[] keys;

			if ( parentIndices == null )
				keys = new int[ 1 ];
			else
			{
				keys = new int[ parentIndices.Length+1 ];
				
				for( int i = 0; i < parentIndices.Length; i++ )
					keys[ i ] = parentIndices[ i ];
			}

			System.Diagnostics.Debug.Assert( keys.Length == table.PrimaryKey.Length );

			for( int i = 1; i <= length; i++ )
				// we start with 1 to work with 1-indexed PUC lists
			{
				keys[ keys.Length-1 ] = win.Index = start + i;

				DataRow row = null;

				try // have had some trouble with Rows.Find in an empty table (not sure if that is the only time though)
				{
					row = table.Rows.Find( keys );
				}
				catch( Exception ) { }

				if ( row == null )
				{
					// row does not exist...build a new one
					row = table.NewRow();
					
					// fill in the primary keys
					for( int j = 0; j < table.PrimaryKey.Length; j++ )
						row[ table.PrimaryKey[ j ] ] = keys[ j ];

					// add the row
					table.Rows.Add( row );
				}

				// update data in the row
				IEnumerator cols = table.Columns.GetEnumerator();
				while( cols.MoveNext() )
				{
					ApplianceObject ao = (ApplianceObject)_columnToStateMap[ cols.Current ];

					if ( ao != null && ao.State )
					{
						ApplianceState state = (ApplianceState)ao;

						if ( state.Type.ValueSpace.Space == PUC.Types.ValueSpace.BOOLEAN_SPACE )
                            row[ (DataColumn)cols.Current ] = (bool)state.Value;
						else
							row[ (DataColumn)cols.Current ] = state.Value.ToString();
					}
				}

				row.AcceptChanges();
			}

			table.AcceptChanges();
		}

		protected void deleteListData( ListGroupNode listGroup, DataTable table, int[] parentIndices, int start, int length )
		{
			// TODO: Have to think about removing appropriate rows in child tables

			System.Int32[] keys;

			if ( parentIndices == null )
				keys = new int[ 1 ];
			else
			{
				keys = new int[ parentIndices.Length+1 ];
				
				for( int i = 0; i < parentIndices.Length; i++ )
					keys[ i ] = parentIndices[ i ];
			}

			System.Diagnostics.Debug.Assert( keys.Length == table.PrimaryKey.Length );

			for( int i = 1; i <= length; i++ )
				// we start with 1 to work with 1-indexed PUC lists
			{
				keys[ keys.Length-1 ] = start + i;

				DataRow row = table.Rows.Find( keys );
				
				if ( row != null )
					table.Rows.Remove( row );
			}

			table.AcceptChanges();
		}

		protected void renumberListData( ListGroupNode listGroup, DataTable table, int[] parentIndices, int start, int newstart, int length )
		{
			// TODO: have to think about doing renumbering in child tables

			System.Int32[] keys;

			if ( parentIndices == null )
				keys = new int[ 1 ];
			else
			{
				keys = new int[ parentIndices.Length+1 ];
				
				for( int i = 0; i < parentIndices.Length; i++ )
					keys[ i ] = (System.Int32)parentIndices[ i ];
			}

			System.Diagnostics.Debug.Assert( keys.Length == table.PrimaryKey.Length );

			for( int i = 0; i <= length; i++ )
				// we start with 1 to work with 1-indexed PUC lists
			{
				keys[ keys.Length-1 ] = (System.Int32)start + i;

				DataRow row = table.Rows.Find( keys );
				
				row[ INDEX_COL_NAME ] = newstart + i;

				row.AcceptChanges();
			}

			table.AcceptChanges();
		}

		private void _listGroup_ListDataChanged(ListGroupNode listGroup, ListEventArgs a)
		{
			DataTable table = _dataSet.Tables[ listGroup.FullPath ];
			System.Diagnostics.Debug.Assert( table != null );

			if ( a is RefreshAllListEventArgs )
			{
				refreshListData( listGroup, table, a, 0, (int)listGroup.LengthState.Value );
			}
			else if ( a is DeleteListEventArgs )
			{
				deleteListData( listGroup, table, a.ParentIndices, ((DeleteListEventArgs)a).Start, ((DeleteListEventArgs)a).Length );
			}
			else if ( a is ChangeListEventArgs )
			{
			}
			else if ( a is InsertListEventArgs )
			{
				InsertListEventArgs ia = (InsertListEventArgs)a;

				renumberListData( listGroup, table, ia.ParentIndices, ia.InsertAfter+1, ia.InsertAfter + ia.ItemCount + 1, ia.InsertAfter );
				refreshListData( listGroup, table, ia, ia.InsertAfter+1, ia.ItemCount );
			}
		}

		private void SelectionState_ValueChangedEvent(ApplianceState s)
		{

		}
	}
}
