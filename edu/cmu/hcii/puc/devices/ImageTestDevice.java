/*
 * ImageTestDevice.java
 * 
 * This device tests the list support of a PUC client by implementing
 * the simple_list.xml specification.  It will most likely be used for
 * testing the list communication protocol features.
 *
 * 10/3/2003 - JWN
 */

// Package Definition

package edu.cmu.hcii.puc.devices;


// Import Declarations

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.StringTokenizer;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import com.maya.puc.common.*;

import com.sun.image.codec.jpeg.*;

import org.jdom.Document;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;


// Class Definiton

public class ImageTestDevice extends AbstractDevice2 {

    //**************************
    // Constants
    //**************************

    protected final static String SPEC_NAME       = "image-test.xml";

    protected final static String IMAGE_STATE     = "Test.Image.Image";


    //**************************
    // Member Variables
    //**************************

    protected BufferedImage m_pImage;


    //**************************
    // Constructor
    //**************************
    
    public ImageTestDevice() {

	m_pImage = null;
    }


    //**************************
    // Helper Methods
    //**************************

    //**************************
    // AbstractDevice2 Methods
    //**************************

    public void start() {

	super.start( AbstractDevice2.STATUS_ACTIVE );
    }

    public void stop() {

	super.stop();
    }

    public String getName() {

	return "Image Test Device";
    }

    public int getDefaultPort() {

	return 5181;
    }

    public String getSpecFileName() {

	return SPEC_NAME;
    }

    public void configure() {

	JFileChooser chooser = new JFileChooser();

	int returnVal = chooser.showOpenDialog( null );
	if(returnVal == JFileChooser.APPROVE_OPTION) {

	    File f = chooser.getSelectedFile();

	    try {
		JPEGImageDecoder decoder = JPEGCodec.createJPEGDecoder( new FileInputStream( f ) );
		m_pImage = decoder.decodeAsBufferedImage();

		sendAll( new Message.BinaryStateChangeNotification( IMAGE_STATE, "image/jpeg" ) );
	    }
	    catch( Exception e ) {

		e.printStackTrace();
	    }
	}	
    }

    public boolean hasGUI() { 

	return false; 
    }

    public void setGUIVisibility(boolean isVisible) {
    }

    public boolean isGUIVisible() { 

	return false; 
    }

    public void handleMessage( PUCServer.Connection conn, Message msg ) {

	if ( msg instanceof Message.FullStateRequest ) {

	    if ( m_pImage != null ) {
		
		conn.send( new Message.BinaryStateChangeNotification( IMAGE_STATE, "image/jpeg" ) );
	    }
	}
	else if ( msg instanceof Message.StateValueRequest ) {

	    Message.StateValueRequest svrqst = (Message.StateValueRequest)msg;
	    if ( svrqst.getState().equals( IMAGE_STATE ) ) {

		BufferedImage newImage = m_pImage;
		
		try {

		    // determine if image is to be scaled
		    if ( svrqst.get( Message.StateValueRequest.DESIRED_HEIGHT_OPT ) != null ||
			 svrqst.get( Message.StateValueRequest.DESIRED_WIDTH_OPT ) != null ) {
			
			double ratio = ((double)m_pImage.getWidth()) / 
			               ((double)m_pImage.getHeight());
			int h, w;
			
			if ( svrqst.get( Message.StateValueRequest.DESIRED_HEIGHT_OPT ) == null ) {
			    
			    w = Integer.parseInt( svrqst.get( Message.StateValueRequest.DESIRED_WIDTH_OPT ) );
			    h = (int)(((double)w)*1/ratio);
			}
			else if ( svrqst.get( Message.StateValueRequest.DESIRED_WIDTH_OPT ) == null ) {

			    h = Integer.parseInt( svrqst.get( Message.StateValueRequest.DESIRED_HEIGHT_OPT ) );
			    w = (int)(((double)h)*ratio);
			}
			else {
			    w = Integer.parseInt( svrqst.get( Message.StateValueRequest.DESIRED_WIDTH_OPT ) );
			    h = Integer.parseInt( svrqst.get( Message.StateValueRequest.DESIRED_HEIGHT_OPT ) );

			    if ( w >= (int)( ((double)h) * ratio ) )
				w = (int)( ((double)h) * ratio );
			    else
				h = (int)( ((double)w) * 1/ratio );
			}

			// scale the image
			newImage = new BufferedImage( w, h, BufferedImage.TYPE_INT_RGB );
			Graphics2D g2 = newImage.createGraphics();
			g2.setRenderingHint( RenderingHints.KEY_INTERPOLATION, 
					     RenderingHints.VALUE_INTERPOLATION_BILINEAR );
			g2.drawImage( m_pImage, 0, 0, w, h, null );
		    }

		    // get image into a InputStream
		    ByteArrayOutputStream tempOut = new ByteArrayOutputStream();
		    JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder( tempOut );
		    JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(newImage);
		    param.setQuality( 50f, false );
		    encoder.setJPEGEncodeParam( param );
		    encoder.encode( newImage );

		    conn.send( new Message.BinaryStateChangeNotification( IMAGE_STATE, new ByteArrayInputStream( tempOut.toByteArray() ), "image/jpeg", tempOut.size() ) );
		}
		catch( Exception e ) {

		    e.printStackTrace();
		}
	    }
	}
	else if ( msg instanceof Message.SpecRequest ) {
	    
	    try {
		conn.send( new Message.DeviceSpec( getSpec() ) );
	    }
	    catch( Exception e ) {
		e.printStackTrace();
	    }
	}	
    }
}
