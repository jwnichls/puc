package edu.cmu.hcii.puc.test;

import edu.cmu.hcii.jfogarty.gadget.GadgetObject;
import edu.cmu.hcii.jfogarty.gadget.displayobject.GadgetDisplayObject;
import edu.cmu.hcii.jfogarty.gadget.iteration.GadgetIteration;
import edu.cmu.hcii.jfogarty.gadget.iteration.GadgetIterationResult;
import edu.cmu.hcii.jfogarty.gadget.iteration.undo.RelativeMoveUndo;
import edu.cmu.hcii.jfogarty.gadget.property.GadgetPropertyKey;
import edu.cmu.hcii.jfogarty.gadget.util.GadgetDisplayObjectList;

import java.awt.Dimension;
import java.awt.Rectangle;

import java.util.Random;

/**
 * Resize a display object selected at random from a set of objects.
 *
 * 3/4/2002
 * Created class.  
 *
 * @author Jeffrey Nichols
 **/
public class ResizeDisplayObject extends GadgetIteration 
{
  private
  static    GadgetPropertyKey           RESIZE_RADIUS_KEY;

  private
  static    Random                      st_rRandom;

  private   GadgetDisplayObjectList     m_gdolIterate;

  static {
    st_rRandom = new Random();

    RESIZE_RADIUS_KEY = new GadgetPropertyKey();
  }

  public ResizeDisplayObject(GadgetDisplayObjectList gdolIterate) {
    m_gdolIterate = gdolIterate;
  }

  public ResizeDisplayObject(GadgetDisplayObjectList gdolIterate, int iRadius) {
    this(gdolIterate);

    setResizeRadius(iRadius);
  }

  public GadgetIterationResult doIterate() {
    boolean                             bValid;
    double                              dDirection;
    double                              dDistance;
    GadgetDisplayObject                 gdoIterate;
    GadgetIterationResult               girResult;
    int                                 iResizeW;
    int                                 iResizeH;
    Dimension                           pNewSize;
    RelativeResizeUndo                  rruUndo;

    // Pick an object to resize
    gdoIterate = m_gdolIterate.get(st_rRandom.nextInt(m_gdolIterate.size()));

    bValid = false;
    iResizeW = 0;
    iResizeH = 0;
    pNewSize = null;
    while(!bValid) {
      // Do the random part of resizing it
      dDirection = st_rRandom.nextDouble() * 2 * Math.PI;
      dDistance = st_rRandom.nextDouble() * getResizeRadius();

      // Maybe I should figure out which should be sin and cos or 
      // something here, but it shouldn't matter since we just want
      // to resize it anywhere within the given parameters
      iResizeW = (int) (dDistance * Math.sin(dDirection));
      iResizeH = (int) (dDistance * Math.cos(dDirection));

      pNewSize = new Dimension(gdoIterate.getBounds().width,
			       gdoIterate.getBounds().height);
      pNewSize.width += iResizeW;
      pNewSize.height += iResizeH;

      // The new location is valid if there was actually a nudge
      bValid  = Math.abs(iResizeW) + Math.abs(iResizeH) > 0;
    }
    
    // Keep an undo record of the move
    rruUndo = new RelativeResizeUndo(gdoIterate, -iResizeW, -iResizeH); 

    // Set the undo record on the iteration result
    girResult = new GadgetIterationResult();
    girResult.setIterationUndo(rruUndo);

    // Now apply the actual move
    gdoIterate.setSize(pNewSize);

    return girResult;   
  }

  public int getResizeRadius() {
    synchronized(RESIZE_RADIUS_KEY) {
      return ((Integer) getProperty(RESIZE_RADIUS_KEY)).intValue();
    }
  }

  public void setResizeRadius(int iRadius) {
    synchronized(RESIZE_RADIUS_KEY) {
      setProperty(RESIZE_RADIUS_KEY, new Integer(iRadius));
    }
  }
}


