/**
 * FileDialogListener.java
 *
 * An interface that can be implemented to interpret responses from a
 * FileDialog object. 
 *
 * @author Jeff Nichols
 */

// Package Definition

package edu.cmu.hcii.puc.devices;


// Import Declarations


// Class Definition

public interface FileDialogListener {

    public void fileChosen( boolean bCancelled, String sFilename );
}

