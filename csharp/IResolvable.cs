using System;

namespace PUC
{
	/// <summary>
	/// IResolvable represents the class of objects that store 
	/// string names for ApplianceObjects or GroupNodes that later
	/// need to be resolved to actual object references using the
	/// VariableTable.
	/// </summary>
	public interface IResolvable
	{
		/// <summary>
		/// The method that is called in order to resolve an object.
		/// </summary>
		/// <param name="varTable">the VariableTable that objects are resolved against</param>
		/// <returns>whether or not the objects were successfully resolved</returns>
		bool ResolveObject( VariableTable varTable );

		/// <summary>
		/// Whether or not the objects were successfully resolved.  
		/// Not settable.
		/// </summary>
		bool Valid
		{
			get;
		}
	}
}
