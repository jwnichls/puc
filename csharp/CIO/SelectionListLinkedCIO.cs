using System;
using System.Collections;
using System.Windows.Forms;
using PUC;
using PUC.Communication;
using PUC.Types;

namespace PUC.CIO
{
	/// <summary>
	/// Summary description for SelectionListLinkedCIO.
	/// </summary>
	public class SelectionListLinkedCIO : StateLinkedCIO
	{
		/*
		 * Constants
		 */

		public const string CIO_NAME = "SelectionListLinkedCIO";

		public const int MINIMUM_TOP_PAD = 2;
		public const int MINIMUM_BOTTOM_PAD = 4;
		public const int MINIMUM_LEFT_PAD = 3;
		public const int MINIMUM_RIGHT_PAD = 16;


		/*
		 * Dynamic Loading Code
		 */

		public static ConcreteInteractionObject CreateSelectionListLinkedCIO( ApplianceObject ao )
		{
			return new SelectionListLinkedCIO( ao );
		}


		/*
		 * Member Variables
		 */

		Hashtable _itemIndexMap;
		Hashtable _indexItemMap;


		/*
		 * Constructor
		 */
		public SelectionListLinkedCIO( ApplianceObject ao )
			: base( ao, new System.Windows.Forms.ComboBox() )
		{
			ApplianceState state = (ApplianceState)GetApplObj();

			ComboBox cbox = (ComboBox)GetControl();

			cbox.DropDownStyle = ComboBoxStyle.DropDownList;

			_itemIndexMap = new Hashtable();
			_indexItemMap = new Hashtable();

			state.LabelChangedEvent += new ApplianceObject.LabelChangedHandler(this.LabelChanged);
			state.EnableChangedEvent += new ApplianceObject.EnableChangedHandler(this.EnableChanged);
			state.TypeChangedEvent += new ApplianceState.TypeChangedHandler(this.TypeChanged);
			state.ValueChangedEvent += new ApplianceState.ValueChangedHandler(this.ValueChanged);

			((ComboBox)GetControl()).SelectedIndexChanged += new EventHandler(this.SelectedIndexChanged);

			refreshDisplay();
		}


		/*
		 * Event Handlers
		 */

		public void LabelChanged( ApplianceObject ao )
		{
			typeChangeRefresh();
		}

		public void EnableChanged( ApplianceObject ao )
		{
			GetControl().Enabled = ao.Enabled;
		}

		public void TypeChanged( ApplianceState state )
		{
			typeChangeRefresh();
		}

		public void ValueChanged( ApplianceState state )
		{
			refreshDisplay();
		}

		public void SelectedIndexChanged( object source, EventArgs a )
		{
			object cidxobj = _itemIndexMap[ ((ComboBox)GetControl()).SelectedIndex ];

			if ( cidxobj == null || 
				 isStateIndexSame( cidxobj ) ) 
				return;

			((ApplianceState)GetApplObj()).RequestChange( cidxobj );

			// refreshDisplay();
		}


		/*
		 * Member Methods
		 */

		protected void typeChangeRefresh()
		{	
			ApplianceState state = (ApplianceState)GetApplObj();
			ComboBox cbox = (ComboBox)GetControl();

			try
			{
				Hashtable labels = state.Type.ValueLabels;	

				/*
				* This code may cause unneccesary flicker when displaying the 
				* ComboBox.  If it does, changes should be made here to add and
				* remove items based upon change in the state and its dependencies.
				*/

				object stateval = state.Value;

				if ( state.Type.ValueSpace is PUC.Types.EnumeratedSpace )
				{
					PUC.Types.EnumeratedSpace espc = (PUC.Types.EnumeratedSpace)state.Type.ValueSpace;
					
					//cbox.BeginUpdate();

					cbox.Items.Clear();
					_itemIndexMap.Clear();
					_indexItemMap.Clear();

					for( int i = 1; i <= espc.GetItemCount(); i++ )
					{
						object labelSpace = i;
						LabelDictionary ldict = (LabelDictionary)labels[ labelSpace ];

						if ( ldict.Enabled || 
							 ( state.Defined && i == (int)stateval ) )
						{
							string label = ldict.GetLabelByPixelLength( cbox.Font, 
								cbox.ClientSize.Width );

							cbox.Items.Add( label );
							_itemIndexMap[ cbox.Items.Count - 1 ] = i;
							_indexItemMap[ i ] = cbox.Items.Count - 1;

							if ( state.Defined && i == (int)stateval )
								cbox.SelectedIndex = cbox.Items.Count - 1;
						}
					}

					
					if ( !state.Defined )
					{
						object labelSpace = LabelDictionary.UndefinedToken;
						LabelDictionary ldict = (LabelDictionary)labels[ labelSpace ];

						if ( ldict != null )
						{
							string label = ldict.GetLabelByPixelLength( cbox.Font, 
								cbox.ClientSize.Width );

							cbox.Items.Add( label );
							cbox.SelectedIndex = cbox.Items.Count - 1;
						}
					}
					
					
					//cbox.EndUpdate();
				}
				else if ( state.Type.ValueSpace is PUC.Types.BooleanSpace )
				{
					// cbox.BeginUpdate();

					cbox.Items.Clear();
					_itemIndexMap.Clear();
					_indexItemMap.Clear();
					for( int i = 0; i < 2; i++ )
					{
						object labelSpace = ( i == 1 );
						LabelDictionary ldict = (LabelDictionary)labels[ labelSpace ];

						if ( ldict.Enabled )
						{
							string label = ldict.GetLabelByPixelLength( cbox.Font,
								cbox.Size.Width - MINIMUM_LEFT_PAD - MINIMUM_RIGHT_PAD );

							cbox.Items.Add( label );
							_itemIndexMap[ cbox.Items.Count - 1 ] = ( i == 1 );
							_indexItemMap[ ( i == 1 ) ] = cbox.Items.Count - 1;

							if ( state.Defined && ( i == 1 ) == (bool)stateval )
								cbox.SelectedIndex = cbox.Items.Count - 1;
						}
					}

					// TODO: think of a better way to do this
					// Currently undefined labels won't work
					if ( !state.Defined )
					{
						object labelSpace = LabelDictionary.UndefinedToken;
						LabelDictionary ldict = (LabelDictionary)labels[ labelSpace ];

						if ( ldict != null )
						{
							string label = ldict.GetLabelByPixelLength( cbox.Font, 
								cbox.ClientSize.Width );

							cbox.Items.Add( label );
							cbox.SelectedIndex = cbox.Items.Count - 1;
						}
					}


					// cbox.EndUpdate();
				}
				else 
					Globals.GetFrame( GetApplObj().Appliance )
						.AddLogLine( "SelectionListLinkedCIO does not know how to handle non-boolean/enumerated spaces" );
			}
			catch( Exception )
			{
				cbox.Items.Add( "--" );
			}
		}
		
		protected bool isStateIndexSame( object cidx )
		{
			object stateval = ((ApplianceState)GetApplObj()).Value;
			ApplianceState state = (ApplianceState)GetApplObj();

			if ( !state.Defined )
				return false;
			
			if (  state.Type.ValueSpace is PUC.Types.EnumeratedSpace )
			{
				return ((int)stateval) == ((int)cidx);
			}
			else if ( state.Type.ValueSpace is PUC.Types.BooleanSpace )
			{
				return ((bool)stateval) == ((bool)cidx);
			}

			return false;
		}

		protected void refreshDisplay()
		{
			ApplianceState state = (ApplianceState)GetApplObj();
			ComboBox cbox = (ComboBox)GetControl();

			if ( cbox.Items.Count == 0 )
				typeChangeRefresh();

			if ( cbox.Items.Count > 0 && state.Defined )
			{
				object oidx = _indexItemMap[ state.Value ];

				if ( oidx != null )
					cbox.SelectedIndex = (int)oidx;
				else
					typeChangeRefresh();
			}
		}

		public override bool HasLabel()
		{
			return GetApplObj().Labels != null;
		}

		public override LabelCIO GetLabelCIO()
		{
			if ( HasLabel() )
				return new LabelCIO( GetApplObj().Labels );

			return null;
		}

		public override System.Drawing.Size GetMinimumSize()
		{
			System.Drawing.SizeF max = new System.Drawing.SizeF( 0, 0 );
			IEnumerator e = ((ComboBox)GetControl()).Items.GetEnumerator();
			while( e.MoveNext() )
			{
				System.Drawing.SizeF s = Globals.MeasureString( (string)e.Current, GetControl().Font );	

				if ( s.Width > max.Width )
					max.Width = s.Width;

				if ( s.Height > max.Height )
					max.Height = s.Height;
			}

			int w = (int)max.Width + MINIMUM_RIGHT_PAD + MINIMUM_LEFT_PAD;
			int h = (int)max.Height + MINIMUM_TOP_PAD + MINIMUM_BOTTOM_PAD;

			return new System.Drawing.Size( w, h );
		}

		public override PUC.Layout.PreferredSize GetPreferredSize()
		{
			return new PUC.Layout.PreferredSize( GetMinimumSize() );
		}

		public override System.Drawing.Point GetControlOffset()
		{
			System.Drawing.SizeF s = Globals.MeasureString( "Test String", GetControl().Font );	

			return new System.Drawing.Point( MINIMUM_LEFT_PAD, MINIMUM_TOP_PAD + (int)s.Height );
		}

		public override void FinalSizeNotify()
		{
			refreshDisplay();
		}

	}
}
