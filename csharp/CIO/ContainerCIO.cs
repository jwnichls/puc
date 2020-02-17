using System;
using System.Collections;
using System.Windows.Forms;

using PUC.Layout;


namespace PUC.CIO
{
	/// <summary>
	/// Summary description for ContainerCIO.
	/// </summary>
	public abstract class ContainerCIO : IndependentCIO
	{
		/*
		 * Member Variables
		 */

		protected ArrayList _cios;


		/*
		 * Constructor
		 */
		public ContainerCIO( Control control ) : base( control )
		{
			_cios = new ArrayList();
		}


		/*
		 * Properties
		 */


		/*
		 * Member Methods
		 */

		public virtual void AddCIO( ControlBasedCIO cio )
		{
			if ( cio == null ) return;

			Control c = this.GetControl();

			c.Controls.Add( cio.GetControl() );
			_cios.Add( cio );

			cio.SetVisible( true );
		}

		public virtual void RemoveCIO( ControlBasedCIO cio )
		{
			if ( cio == null ) return;

			cio.SetVisible( false );

			this.GetControl().Controls.Remove( cio.GetControl() );
			_cios.Remove( cio );
		}

		public void RemoveAllCIOs()
		{
			Control c = this.GetControl();

			c.Controls.Clear();
			_cios.Clear();
		}

		public int GetControlCount()
		{
			return this.GetControl().Controls.Count;
		}

		public override void FinalSizeNotify()
		{
			IEnumerator e = _cios.GetEnumerator();
			while( e.MoveNext() )
				((ControlBasedCIO)e.Current).FinalSizeNotify();
		}

		/// <summary>
		/// Return the area in which other controls could be placed
		/// </summary>
		public virtual System.Drawing.Size InternalSize
		{
			get
			{
				return this.GetControl().Size;
			}
		}
	}
}
