/**
 * NudgeDisplayObjectWithSprings.java
 *
 * A nudging iterator that also maintains the no-overlap condition by
 * modeling springs to keep display objects apart.  
 *
 * NOTE: This implementation currently doesn't model springs, and
 * instead uses a rather naive implementation of nudging overlapping
 * objects by the same amount as the randomly chosen element.
 *
 * @author Jeffrey Nichols
 */

// Package Definition

package edu.cmu.hcii.puc.test;


// Import Declarations

import edu.cmu.hcii.jfogarty.gadget.GadgetObject;

import edu.cmu.hcii.jfogarty.gadget.displayobject.GadgetDisplayObject;

import edu.cmu.hcii.jfogarty.gadget.iteration.GadgetIteration;
import edu.cmu.hcii.jfogarty.gadget.iteration.GadgetIterationResult;
import edu.cmu.hcii.jfogarty.gadget.iteration.undo.CompositeUndo;
import edu.cmu.hcii.jfogarty.gadget.iteration.undo.RelativeMoveUndo;

import edu.cmu.hcii.jfogarty.gadget.property.GadgetPropertyKey;

import edu.cmu.hcii.jfogarty.gadget.util.GadgetDisplayObjectList;
import edu.cmu.hcii.jfogarty.gadget.util.GadgetDisplayObjectListIterator;
import edu.cmu.hcii.jfogarty.gadget.util.GadgetObjectList;

import java.awt.Point;
import java.awt.Rectangle;

import java.util.Random;


// Class Definition

public class NudgeDisplayObjectWithSprings extends GadgetIteration 
{
  private
  static    GadgetPropertyKey           NUDGE_RADIUS_KEY;

  private
  static    Random                      st_rRandom;

  private   GadgetDisplayObjectList     m_gdolIterate;

  static {
    st_rRandom = new Random();

    NUDGE_RADIUS_KEY = new GadgetPropertyKey();
  }

  public NudgeDisplayObjectWithSprings(GadgetDisplayObjectList gdolIterate) {
    m_gdolIterate = gdolIterate;
  }

  public NudgeDisplayObjectWithSprings(GadgetDisplayObjectList gdolIterate, int iRadius) {
    this(gdolIterate);

    setNudgeRadius(iRadius);
  }

  public GadgetIterationResult doIterate() {
    boolean                             bValid;
    double                              dDirection;
    double                              dDistance;
    GadgetDisplayObject                 gdoIterate;
    GadgetIterationResult               girResult;
    int                                 iNudgeX;
    int                                 iNudgeY;
    Point                               pNewLocation;
    GadgetObjectList                    golUndoList;
    GadgetDisplayObjectListIterator     gdoliItems;

    // Pick an object to move
    gdoIterate = m_gdolIterate.get(st_rRandom.nextInt(m_gdolIterate.size()));

    bValid = false;
    iNudgeX = 0;
    iNudgeY = 0;
    pNewLocation = null;
    while(!bValid) {
      // Do the random part of moving it
      dDirection = st_rRandom.nextDouble() * 2 * Math.PI;
      dDistance = st_rRandom.nextDouble() * getNudgeRadius();

      // Maybe I should figure out which should be sin and cos or 
      // something here, but it shouldn't matter since we just want
      // to nudge it anywhere within the radius
      iNudgeX = (int) (dDistance * Math.cos(dDirection));
      iNudgeY = (int) (dDistance * Math.sin(dDirection));

      pNewLocation = new Point(gdoIterate.getLocation());
      pNewLocation.translate(iNudgeX, iNudgeY);

      // The new location is valid if there was actually a nudge
      bValid  = Math.abs(iNudgeX) + Math.abs(iNudgeY) > 0;
    }

    // initialize the list to keep track of all move undos
    golUndoList = new GadgetObjectList();
    
    // record the nudge move and apply it
    golUndoList.add( new RelativeMoveUndo(gdoIterate, -iNudgeX, -iNudgeY) );
    gdoIterate.setLocation(pNewLocation);

    // loop through the other GDOs and make any necessary spring
    // transformations 
    gdoliItems = m_gdolIterate.listIterator();
    Rectangle rIterate = gdoIterate.getBounds();
    while( gdoliItems.hasNext() ) {
	GadgetDisplayObject gdoItem = gdoliItems.next();

	if ( gdoItem == gdoIterate ) continue;

	Rectangle rItem = gdoItem.getBounds();

	if ( rItem.intersects( rIterate ) ) {
	    // NAIVE:JWN: For now, we'll just assume that nudging the
	    // other item by the same amount will be enough
	    
	    golUndoList.add( new RelativeMoveUndo(gdoItem, -iNudgeX, -iNudgeY ) );
	    pNewLocation = new Point( gdoItem.getLocation() );
	    pNewLocation.translate( iNudgeX, iNudgeY );
	    gdoItem.setLocation( pNewLocation );
	}
    }

    // Set the undo record on the iteration result
    girResult = new GadgetIterationResult();
    girResult.setIterationUndo( new CompositeUndo( golUndoList ) );

    return girResult;   
  }

  public int getNudgeRadius() {
    synchronized(NUDGE_RADIUS_KEY) {
      return ((Integer) getProperty(NUDGE_RADIUS_KEY)).intValue();
    }
  }

  public void setNudgeRadius(int iRadius) {
    synchronized(NUDGE_RADIUS_KEY) {
      setProperty(NUDGE_RADIUS_KEY, new Integer(iRadius));
    }
  }
}


