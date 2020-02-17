/**
 * Globals.java
 * 
 * This is a static class for storing global objects and parameters.
 * It abstracts part of the former role of the PUC class so that the
 * UI generation code can be reused in non-PUC client situations. 
 *
 * @author Jeff Nichols
 */

// Package Definition

package edu.cmu.hcii.puc;


// Import Declarations

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Container;
import java.awt.Insets;
import java.awt.Rectangle;

import java.io.*;

import java.util.Date;


// Class Definition

public class Globals {

    protected static String VERSION_STRING;
    public static String getVersionString() { return VERSION_STRING; }

    public static final boolean SERVER_HIERARCHY = false;

    protected static int SCREEN_WIDTH;
    public static int getScreenWidth() { return SCREEN_WIDTH; }

    protected static String WIDGET_REGISTRY_FILE;
    public static String getRegistryFile() { return WIDGET_REGISTRY_FILE; }

    protected static String m_sFilePrefix;
    public static String getFilePrefix() { return m_sFilePrefix; }

    protected static Container m_pFrame;
    public static Container getMainFrame() { return m_pFrame; }
    public static void validateMainFrame() { m_pFrame.validate(); }
    public static FontMetrics getFontMetricsObj( Font f ) {
	return m_pFrame.getFontMetrics( f ); 
    }
    public static Rectangle getUIPaneSize() { 
	Insets i = m_pFrame.getInsets();
	Rectangle rPaneSize = new Rectangle( i.left, i.top, 
					     m_pFrame.getSize().width - i.left - i.right,
					     m_pFrame.getSize().height - i.top - i.bottom );

	return rPaneSize; 
    }
    
    
    //**************************
    // Init Function
    //**************************

    public static void init( String sVersion, int nWidth, String sRegistry,
			     String sPrefix, Container pFrame ) {

	VERSION_STRING = sVersion;
	SCREEN_WIDTH = nWidth;
	WIDGET_REGISTRY_FILE = sRegistry;
	m_sFilePrefix = sPrefix;
	m_pFrame = pFrame;
    }


    //**************************
    // Logging Functions
    //**************************

    private static PrintWriter m_LogFile;

    protected static void startLogging(String prefix) {
        try {
            m_LogFile = new PrintWriter(new FileWriter(prefix + "puc.log",
                    true));

            m_LogFile.println(" --- " + (new Date()).toString() +
                    " --- PUC log starting...");
        } catch (Exception e) {
            System.err.println("Error opening log file!");
            e.printStackTrace();
        }
    }

    protected static void stopLogging() {
        if (m_LogFile != null) {
            m_LogFile.println(" --- " + (new Date()).toString() +
                    " --- PUC log stopping...");
            m_LogFile.close();
            m_LogFile = null;
        }
    }

    /**
     * Print a string to the log file.
     */
    public static void printLog(String msg) {
        if (m_LogFile != null)
            m_LogFile.println(msg);
    }

}
