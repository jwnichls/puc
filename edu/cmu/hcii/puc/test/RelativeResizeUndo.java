package edu.cmu.hcii.puc.test;

import edu.cmu.hcii.jfogarty.gadget.GadgetObject;
import edu.cmu.hcii.jfogarty.gadget.displayobject.GadgetDisplayObject;
import edu.cmu.hcii.jfogarty.gadget.iteration.undo.IterationUndo;
import java.awt.Dimension;

/**
 * Undo an iteration by resizing the object the given
 * parameters.
 *
 * 3/4/2002
 * Created class.  
 *
 * @author Jeffrey Nichols
 **/
public class RelativeResizeUndo extends IterationUndo 
{
  private   GadgetDisplayObject         m_gdoObject;
  private   int                         m_iResizeW;
  private   int                         m_iResizeH;

  public RelativeResizeUndo(GadgetDisplayObject gdoObject, int iResizeW, int iResizeH) {
    m_gdoObject = gdoObject;
    m_iResizeW = iResizeW;
    m_iResizeH = iResizeH;
  }

  public void apply() {
    Dimension d = m_gdoObject.getSize();
    
    d.width += m_iResizeW;
    d.height += m_iResizeH;

    m_gdoObject.setSize( d );
  }
}


