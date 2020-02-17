using System;
using System.Collections;
using PUC.UIGeneration;

namespace PUC
{
	/// <summary>
	/// Represents some knowledge of organization, which will be applied at
	/// user interface generation time.  When a specification is parsed
	/// all organizers are set to "none."  During the generation process,
	/// these organizers are set to various values, based in part on
	/// dependency information and in part by how many widgets are able to
	/// fit on to a single screen.
	/// </summary>
	public interface IOrganizer
	{
		Hashtable AddOrganization( GroupNode group, InterfaceNode currentNode );
	}
}
