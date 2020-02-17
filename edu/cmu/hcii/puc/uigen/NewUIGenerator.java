/**
 * NewUIGenerator.java
 *
 * Generates and displays a user interface from a spec.  Implements
 * new algorithm, with design decisions encapsulated in command
 * objects and (maybe) some backtracking.
 *
 * Revision History
 * ----------------
 * 07/23/2002: (JWN) Created file.
 */

// Package Definition

package edu.cmu.hcii.puc.uigen;


// Import Declarations

import java.awt.Button;
import java.awt.Color;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.MenuBar;
import java.awt.Panel;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import java.lang.*;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import edu.cmu.hcii.puc.*;
import edu.cmu.hcii.puc.cio.*;

import edu.cmu.hcii.puc.parser.WidgetRegistryParser;
import edu.cmu.hcii.puc.registry.WidgetRegistry;

import edu.cmu.hcii.puc.types.*;

import com.maya.puc.common.*;


// Class Definition

public class NewUIGenerator extends UIGenerator {

    //**************************
    // Member Variables
    //**************************


    //**************************
    // Constructor
    //**************************

    public NewUIGenerator( MenuBar m ) {

	super( m );

	m_pWidgetRegistry = WidgetRegistryParser.parse( Globals.getFilePrefix() + Globals.getRegistryFile() );
    }


    //**************************
    // Generation
    //**************************

    public void generateUI( Appliance a ) {

	super.generateUI( a );
	
	/*
	 * This interface generator proceeds in several phases.
	 *
	 * Phase #1:  Traverse group tree, find mutually exclusive
	 * situations and build an initial version of the design
	 * decision tree.  Placeholders are created in the tree where
	 * we believe separate panels will be used.
	 *
	 * Phase #2:  For each placeholder, the set of interaction
	 * objects is collected and Abstract Interaction Objects
	 * (AIOs) are created.  The AIO objects will be used later to
	 * manage the types of widgets that may be used for a
	 * particular interactor (a little different than previous
	 * usages of AIO)
	 *
	 * Phase #3:  For each placeholder, we determine how multiple
	 * panels will be placed relative to each other and how
	 * components will be placed within panels.
	 *
	 * Phase #4:  Decisions are made about how to place components
	 * within panels.  At this point, we may discover problems
	 * with our earlier decisions and need to make changes.
	 *
	 * Phase #5:  The concrete interface is assembled and
	 * displayed.
	 *
	 * 
	 * Because of Phase #4, the processing of phase #3 & #4 must be
	 * incremental and separable, so that parts of each phase can
	 * be recomputed in order to fix a problem.
	 */

        /*
	 * Initialization
	 */

        tree = null;

        /*
	 * Phase #1
	 */
      
        beginPhaseOne();

    }

    //**************************
    // Protected Helper Methods
    //**************************

    protected void generateHelper( GroupNode g, 
				   Appliance a, 
				   PanelNode p ) {

	if ( g.m_Object != null ) {

	    ConcreteInteractionObject pCIO = m_pWidgetRegistry.chooseWidget( a, g.m_Object );
	    
	    if ( pCIO != null ) {
		if ( pCIO.prefersFullWidth() ) 
		    p.addRow( new FullWidthRow( p, pCIO ) );
		else
		    p.addRow( new OneColumnRow( p, pCIO ) );
	    }

	    return;
	}

	if ( !g.containsGroups() && 
	     g.hasNullOrganizer() &&
	     g.size() == 2 ) {
	    
	    ConcreteInteractionObject pCIO1 =
		m_pWidgetRegistry.chooseWidget( a, ((GroupNode)g.m_Children.elementAt( 0 )).m_Object );
	    ConcreteInteractionObject pCIO2 =
		m_pWidgetRegistry.chooseWidget( a, ((GroupNode)g.m_Children.elementAt( 1 )).m_Object );
	    
	    if ( pCIO1 == null && pCIO2 != null ) {
		p.addRow( new OneColumnRow( p, pCIO2 ) );
		return;
	    }
	    if ( pCIO2 == null && pCIO1 != null ) {
		p.addRow( new OneColumnRow( p, pCIO1 ) );
		return;
	    }
	    if ( pCIO1 == null && pCIO2 == null )
		return;

	    if ( pCIO1.hasLabel() && 
		 pCIO2.hasLabel() &&
		 !p.isVertical() ) {
		p.addRow( new TwoColumnRow( p, pCIO1, pCIO2 ) );
		return;
	    }
	    else if ( !pCIO1.hasLabel() && !pCIO2.hasLabel() &&
		      g.m_Labels != null ) {
		p.addRow( new LabeledTwoCompRow( p, 
						 new LabelCIO( g.m_Labels ), 
						 pCIO1, pCIO2 ) );
		return;
	    }
	    else {
		p.addRow( new OneColumnRow( p, pCIO1 ) );
		p.addRow( new OneColumnRow( p, pCIO2 ) );
		return;
	    }
	}

	Enumeration en = g.m_Children.elements();
	
	g.callOrganizer( p );
	
	while( en.hasMoreElements() ) {
	    GroupNode newG = (GroupNode)en.nextElement();
	    PanelNode newP = g.getContainerForChild( newG );
	    
	    if ( newP == null ) newP = p; // there is no new
	    // container, so use the
	    // current one. 
	    
	    generateHelper( newG, a, newP );
	}
    }

    /**
     * This method starts from the node that started off the algorithm
     * and works up the tree until it finds the root.
     */
    protected void recoverRoot() {
	while( m_pInterfaceRoot.getParent() != null )
	    m_pInterfaceRoot = m_pInterfaceRoot.getParent();
    }
    
    /**
     * findInTree
     *
     * Uses pass-by-reference with the parameter pList to return all
     * occurrences of state variable that are found within the group
     * tree. 
     */
    protected void findInTree( GroupNode g, ApplianceObject ao, 
			       Vector pList ) {

	if ( g.m_Object != null ) {
	    if ( g.m_Object == ao ) {
		pList.addElement( g.m_Parent );
	    }
	}
	else {
	    Enumeration en = g.m_Children.elements();

	    while( en.hasMoreElements() ) {
		GroupNode newG = (GroupNode)en.nextElement();

		findInTree( newG, ao, pList );
	    }
	}
    }


    protected void generateOrganization( GroupNode root, 
					 Vector dependedObjs ) {

	Enumeration en = dependedObjs.elements();

	Vector tempV = new Vector();

	while( en.hasMoreElements() ) {
	    ApplianceState as = (ApplianceState)en.nextElement();

	    tempV.removeAllElements();
	    findInTree( root, as, tempV );

	    Enumeration en2 = tempV.elements();

	    while( en2.hasMoreElements() ) {
		GroupNode g = (GroupNode)en2.nextElement();

		generateOrganizationAtGroupNode( g, as );
	    }
	}
    }

    protected Hashtable findDuplicateObjectsInTree( GroupNode g ) {
	
	Hashtable dupObjects = new Hashtable();
	Hashtable statesFound = new Hashtable();

	findDupsHelper( g, dupObjects, statesFound );

	return dupObjects;
    }

    protected void findDupsHelper( GroupNode g, 
				   Hashtable dObjs, 
				   Hashtable h ) {

	if ( g.m_Object != null ) {
	    
	    if ( h.get( g.m_Object.m_sName ) != null )
		dObjs.put( g.m_Object.m_sName, g.m_Object );
	    else
		h.put( g.m_Object.m_sName, g.m_Object );
	}
	else {
	    
	    Enumeration en = g.m_Children.elements();

	    while( en.hasMoreElements() ) {
		GroupNode ng = (GroupNode)en.nextElement();

		findDupsHelper( ng, dObjs, h );
	    }
	}
    }

    protected Hashtable collectDependencies( GroupNode g,
					     ApplianceState s,
					     Hashtable dupObjects ) {

	Hashtable result = new Hashtable();

	Enumeration en = g.m_Children.elements();

	while( en.hasMoreElements() ) {
	    GroupNode ng = (GroupNode)en.nextElement();

	    Vector deps = collectDependency( ng, s, dupObjects );

	    filterDependencies( deps );
		
	    result.put( ng, deps );
	}
	
	return result;
    }

    /**
     * mergeVector
     *
     * Merges a Vector of dependencies which are assumed to overlap.
     * This can be assumed if the Vector contains entirely
     * greater-than or less-than dependencies.
     */
    protected Dependency mergeVector( Vector pV ) {

	Enumeration en;
	en = pV.elements();
	Dependency d = null;

	if ( en.hasMoreElements() )
	    d = (Dependency)en.nextElement();

	while( en.hasMoreElements() ) {
	    Dependency nd = (Dependency)en.nextElement();

	    d = nd.merge( d );
	}

	return d;
    }

    protected void filterDependencies( Vector pDeps ) {

	// Step #1: Filter dependencies into groups

	Vector pGreaterThan = new Vector();
	Vector pLessThan = new Vector();
	Vector pEquals = new Vector();

	Enumeration en = pDeps.elements();

	while( en.hasMoreElements() ) {
	    Dependency d = (Dependency)en.nextElement();

	    if ( d instanceof EqualsDependency )
		pEquals.addElement( d );
	    else if ( d instanceof GreaterThanDependency )
		pGreaterThan.addElement( d );
	    else if ( d instanceof LessThanDependency )
		pLessThan.addElement( d );
	    else if ( d instanceof WholeSetDependency ) {

		// If there is a WholeSetDependency in this group,
		// just return it as the only member of the Vector

		pDeps.removeAllElements();
		pDeps.addElement( d );
		return;
	    }
	    else throw new ClassCastException( "Unrecognized dependency!" );
	}

	pDeps.removeAllElements(); // at this point, we've stored all
 	                           // the dependencies elsewhere, so
	                           // we can empty pDeps in
	                           // anticipation of filling it in
	                           // below.  

	// Step #2: Merge greater-than (g-t) and less-than (l-t) dependencies

	Dependency gtDep = mergeVector( pGreaterThan );
	Dependency ltDep = mergeVector( pLessThan );
	

	// Step #3: Check that filtered g-t and l-t dependencies don't 
	//          overlap.  If they do, we're done. (return a new
	//          WholeSetDependency)

	if ( gtDep != null && ltDep != null ) {
	    Dependency d = gtDep.merge( ltDep );
	    if ( d != null ) {
		// d must be a WholeSetDependency.  Make it the only thing
		// in pDeps and return
		
		pDeps.addElement( d );
		return;
	    }
	}

	// Step #4: Merge with equals dependencies.

	if ( gtDep != null ) 
	    pDeps.addElement( gtDep );
	if ( ltDep != null )
	    pDeps.addElement( ltDep );

	en = pEquals.elements();

ELEM:	while( en.hasMoreElements() ) {
	    Dependency d = (Dependency)en.nextElement();

	    for( int i = 0; i < pDeps.size(); i++ ) {

		Dependency m = d.merge( (Dependency)pDeps.elementAt( i ) );

		if ( m != null ) {
		    pDeps.setElementAt( m, i );
		    continue ELEM;  // break out of this loop and
		                    // continue in the outer loop
 		                    // (hey, I liked goto's  ;-)
		}
	    }

	    pDeps.addElement( d );
	}
    }

    protected Vector collectDependency( GroupNode g,
					ApplianceState s,
					Hashtable dupObjects ) {

	if ( g.m_Object != null ) {
	    if ( dupObjects.get( g.m_Object.m_sName ) != null ||
		 g.m_Object == s )
		return new Vector();

	    return getDependenciesOfState( s, g.m_Object, dupObjects );
	}
	else {

	    Vector pResults = new Vector();

	    Enumeration en = g.m_Children.elements();

	    while( en.hasMoreElements() ) {
		GroupNode ng = (GroupNode)en.nextElement();

		Vector r = collectDependency( ng, s, dupObjects );

		Enumeration en2 = r.elements();
		while( en2.hasMoreElements() ) 
		    pResults.addElement( en2.nextElement() );
	    }

	    return pResults;
	}
    }

    protected Vector getDependenciesOfState( ApplianceState pState,
					     ApplianceObject pObject,
					     Hashtable dupObjects ) {

	Vector deps = null;

	if ( dupObjects.get( pObject.m_sName ) == null )
	    deps = getDepsOfStateHelper( pState, 
					 pObject.m_pDependencies );

	return deps;
    }

    protected Vector getDepsOfStateHelper( ApplianceState pState,
					   DependencyFormula df ) {

	Vector pResults = new Vector();

	// FIXME:JWN: Handle a degenerate case...maybe should have a
	// NullDependencyFormula to get rid of this.  
	if ( df == null ) return pResults; 

	if ( df instanceof OR ) {

	    Enumeration en = df.getDependencies();
	    
	    while( en.hasMoreElements() ) {
		Dependency d = (Dependency)en.nextElement();
		
		if ( d.getState() == pState ) {
		    pResults.addElement( d );
		}
		else {
		    if ( pResults.size() > 0 )
			Globals.printLog( "WARNING: Dependency OR assumption violated.\n" );

		    pResults.removeAllElements();
		    return pResults;
		}
	    }

	    Enumeration en2 = df.getFormulas();
	    
	    while( en2.hasMoreElements() ) {
		Vector v = getDepsOfStateHelper( pState, 
						 (DependencyFormula)en2.nextElement() );

		if ( v.size() == 0 ) {
		    if ( pResults.size() > 0 )
			Globals.printLog( "WARNING: Dependency OR assumption violated.\n" );
		    pResults.removeAllElements();
		    return pResults;
		}
	    }
	}
	else {

	    Enumeration en = df.getDependencies();

	    while( en.hasMoreElements() ) {
		Dependency d = (Dependency)en.nextElement();

		if ( d.getState() == pState ) {
		    pResults.addElement( d );
		}
	    }

	    Enumeration en2 = df.getFormulas();

	    while( en2.hasMoreElements() ) {
		Vector v = getDepsOfStateHelper( pState, 
						 (DependencyFormula)en2.nextElement() );

		Enumeration en3 = v.elements();

		while( en3.hasMoreElements() ) 
		    pResults.addElement( en3.nextElement() );
	    }
	}

	return pResults;
    }

    /**
     * mergeTwoVectors
     *
     * This is different than the mergeVector function.  It takes two
     * vectors, which may or may not contain dependencies that
     * overlap, and merges them into a single Vector of
     * non-overlapping dependencies.
     * It is assumeed that each of the Vectors that are passed to this
     * function contain non-overlapping dependencies.
     */
    protected Vector mergeTwoVectors( Vector v1, Vector v2 ) {

	Vector vResult = new Vector();

	Enumeration en = v1.elements();
	while( en.hasMoreElements() ) 
	    vResult.addElement( en.nextElement() );
	
	en = v2.elements();
ELEM:	while( en.hasMoreElements() ) {
	    Dependency d1 = (Dependency)en.nextElement();
	    
	    for( int i = 0; i < vResult.size(); i++ ) {
		Dependency d2 = (Dependency)vResult.elementAt( i );

		Dependency dm = d1.merge( d2 );
		if ( dm != null ) {
		    if ( dm instanceof WholeSetDependency ) {
			vResult.removeAllElements();
			vResult.addElement( dm );
			return vResult;
		    }

		    vResult.setElementAt( dm, i );
		    continue ELEM;
		}
	    }

	    vResult.addElement( d1 );
        }

	return vResult;
    }

    protected Vector findMutualExclusion( GroupNode g,
					  ApplianceState s,
					  Hashtable groupDeps ) {
	
	Vector vChildSets = new Vector();
	Vector vDepSets = new Vector();

	// The first element in vChildSet is that set of all children
	// that do not depend on this state at all (there aren't
	// neccesarily any)

	Vector noDepChildren = new Vector();
	Vector noDeps = new Vector();
	vChildSets.addElement( noDepChildren );
	vDepSets.addElement( noDeps );


	Enumeration en = g.m_Children.elements();

ELEM:	while( en.hasMoreElements() ) {
	    GroupNode c = (GroupNode)en.nextElement();

	    if ( c.m_Object == s ) continue;  // don't consider the
	                                      // depended child 

	    // if deps is null, then something is wrong
	    Vector deps = (Vector)groupDeps.get( c );
	    
	    if ( deps.size() == 0 ) {
		noDepChildren.addElement( c );
		continue ELEM;
	    }

	    for( int i = 1; i < vDepSets.size(); i++ ) {
		Vector set = (Vector)vDepSets.elementAt( i );

		// check if this element is mutex with vSets[i]
		Vector vMerge = mergeTwoVectors( deps,
						 set
					       );

		if ( vMerge.size() != (deps.size() + set.size()) ) {
		    vDepSets.setElementAt( vMerge, i );
		    Vector vSet = (Vector)vChildSets.elementAt( i );
		    vSet.addElement( c );
		    continue ELEM;
		}
	    }

	    vDepSets.addElement( deps );
	    Vector vSet = new Vector();
	    vSet.addElement( c );
	    vChildSets.addElement( vSet );
	}

	Vector v = new Vector();
	v.addElement( vChildSets );
	v.addElement( vDepSets );
	return v;
    }
				    
    protected void generateOrganizationAtGroupNode( GroupNode g, 
						    ApplianceState s ) {

	// Step #1: Mark those states that occur more than once within
	// this group tree

	Hashtable dupObjects = findDuplicateObjectsInTree( g );

	// Step #2: Extract and combine dependencies from each group
	// at this level of the tree, ignoring repeated states.

	// need to create a data structure here
	// it will be a hashtable of Vectors, 
	// hashed on the GroupNode ref.

	Hashtable groupDeps = collectDependencies( g, s, dupObjects );

	// Step #3: For every group & object that depends on the
	// state, check for mutual exclusion.

	Vector mutexSets = findMutualExclusion( g, s, groupDeps );

	Vector vChildSets = (Vector)mutexSets.elementAt( 0 );
	Vector vDepSets = (Vector)mutexSets.elementAt( 1 );

	// Step #4: Based upon the amount of mutual exclusion, choose
	// and apply organization to this group

	System.out.println( "-----------------------------------------" );
	System.out.println( "Mutual Exclusion Report: " + s.m_sName );

	int nCount = 0;

	System.out.print( "Number of Sets: " + vChildSets.size() + " " );
	Enumeration en2 = vChildSets.elements();
	while( en2.hasMoreElements() ) {
	    Vector vTemp = (Vector)en2.nextElement();
	    Enumeration en3 = vTemp.elements();
	    System.out.print( "(" );
	    while( en3.hasMoreElements() ) {
		GroupNode gn = (GroupNode)en3.nextElement();
		if ( gn.m_Object != null ) System.out.print( gn.m_Object.m_sName );
	    }
	    System.out.print( ")" );
	    System.out.print( "[" + vTemp.size() + "]" );
	    nCount += vTemp.size();
	}
	System.out.println("");

	System.out.println( "Dependencies: " + vDepSets.toString() );

	// we need a pointer to the GroupNode that contains the state
	// variable that we are looking at (because tree manipulations
	// will need to use it)

	GroupNode stateGroup = null;

	Enumeration en = g.m_Children.elements();
	while( en.hasMoreElements() ) {
	    GroupNode sg = (GroupNode)en.nextElement();

	    if ( sg.m_Object != null && sg.m_Object == s ) {
		stateGroup = sg;
		break;
	    }
	}

	// HACK HACK HACK HACK HACK HACK HACK HACK (remove this!)
	// if only one is affected, deal with it in some other way
	// HACK HACK HACK HACK HACK HACK HACK HACK (remove this!)
	// also get nCount from above...
	
	nCount -= ((Vector)vChildSets.elementAt(0)).size();
	if ( nCount < 2 ) return;


	// Now manipulate the tree, based upon what we've found

	if ( s.m_Type.getValueSpace() instanceof BooleanSpace &&
	     ((Vector)vChildSets.elementAt(0)).size() == 0 ) {

	    GroupNode newG = new GroupNode();

	    newG.m_Parent = g;
	    newG.m_Children = g.m_Children;

	    g.m_Children = new Vector();

	    // make two groups under the common group
	    g.m_Children.addElement( stateGroup );
	    g.m_Children.addElement( newG );

	    s.setInternalController( true );

	    en = newG.m_Children.elements();
	    while( en.hasMoreElements() ) {
		GroupNode gC = (GroupNode)en.nextElement();
		gC.m_Parent = newG;
	    }

	    Vector n = (Vector)vDepSets.elementAt(0);
	    Vector i = (Vector)vDepSets.elementAt(1);

	    EqualsDependency d = (EqualsDependency)i.elementAt(0);
	    if (((Boolean)d.getValueSpace().getValue()).booleanValue() )
		n.addElement( new EqualsDependency( d.getState(), "false" ) );
	    else
		n.addElement( new EqualsDependency( d.getState(), "true" ) );

	    g.setOrganizer( new InternalControlPanelOrganizer( this, 
							       s,
							       g.m_Children, 
							       vDepSets ) );
	}
	else {
	    // Formerly:
	    // else if ( ((Vector)vChildSets.elementAt(0)).size() == 0 ) {
	    // 
	    // Not sure why...  Elements in set(0) should be translated
	    // into new group node topG.

	    boolean bUseTab = false;
	    if ( s.m_Type.getValueSpace() instanceof EnumeratedSpace ) {
		EnumeratedSpace pSpace = (EnumeratedSpace)s.m_Type.getValueSpace();

		if ( pSpace.getNumItems() == ( vDepSets.size() - 1 ) ) {
		    bUseTab = true;
		    for( int i = 1; i < vDepSets.size(); i++ ) {
			Vector vDep = (Vector)vDepSets.elementAt( i );

			if ( vDep.size() != 1 ) {
			    System.out.println( "size! " + vDep.size() );
			    bUseTab = false;
			    break;
			}
			if (! ( vDep.elementAt( 0 ) instanceof
			      EqualsDependency ) ) {
			    System.out.println( "not equals!" );
			    bUseTab = false;
			    break;
			}
		    }
		}
	    } 

	    Vector vChildOrder = new Vector();

	    g.m_Children.removeElement( stateGroup );

	    GroupNode midG = new GroupNode();

	    midG.m_Children = g.m_Children;
	    en = midG.m_Children.elements();
	    while( en.hasMoreElements() ) {
		GroupNode gC = (GroupNode)en.nextElement();
		gC.m_Parent = midG;
	    }

	    g.m_Children = new Vector();
	    
	    Vector vSet = (Vector)vChildSets.elementAt( 0 );
	    if ( vSet.size() > 0 ) {
		en = vSet.elements();
		while( en.hasMoreElements() ) {
		    GroupNode c = (GroupNode)en.nextElement();
		    
		    c.m_Parent = g;
		    
		    midG.m_Children.removeElement( c );
		    g.m_Children.addElement( c );
		}
	    }

	    if ( !bUseTab )
		g.m_Children.addElement( stateGroup );
	    g.m_Children.addElement( midG );
	    midG.m_Parent = g;
	    stateGroup.m_Parent = g;

	    for( int i = 1; i < vChildSets.size(); i++ ) {
		vSet = (Vector)vChildSets.elementAt( i );

		if ( vSet.size() > 1 ) {
		    GroupNode newG = new GroupNode();
		    
		    newG.m_Parent = midG;
		    newG.m_Children = new Vector();

		    en = vSet.elements();
		    while( en.hasMoreElements() ) {
			GroupNode c = (GroupNode)en.nextElement();

			c.m_Parent = newG;

			midG.m_Children.removeElement( c );
			newG.m_Children.addElement( c );
		    }

		    vChildOrder.insertElementAt( newG,
						 i-1 );
		}
		else if ( vSet.size() == 0 ) {

		    GroupNode newG = new GroupNode();
		    
		    newG.m_Parent = midG;
		    newG.m_Children = new Vector();

		    midG.m_Children.addElement( newG );

		    vChildOrder.insertElementAt( newG,
						 i-1 );
		}
		else {
		    vChildOrder.insertElementAt( vSet.elementAt(0),
						 i-1 );
		}
	    }

	    vDepSets.removeElementAt( 0 );

	    if ( bUseTab )
		midG.setOrganizer( new
		    TabbedControlPanelOrganizer( this,
						 s, 
						 vChildOrder,
						 vDepSets ) );
	    else
		midG.setOrganizer( new 
		    ExternalControlPanelOrganizer( this,
						   s,
						   vChildOrder, 
						   vDepSets,
						   false ) );
	}
    }
}
