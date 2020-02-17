/**
 * GadgetPUCLayout.java
 *
 * An experiment in using the gadget toolkit to automatically layout
 * dialog-box style interfaces.  Hopefully this will be the basis for
 * new layout phase in the PUC system.
 *
 * @author Jeffrey Nichols
 */

// Package Definition

package edu.cmu.hcii.puc.test;


// Import Declarations

import edu.cmu.hcii.jfogarty.gadget.GadgetObject;

import edu.cmu.hcii.jfogarty.gadget.algorithm.*;
import edu.cmu.hcii.jfogarty.gadget.algorithm.branching.BranchingAlgorithm;
import edu.cmu.hcii.jfogarty.gadget.algorithm.branching.BranchingAlgorithmListener;
import edu.cmu.hcii.jfogarty.gadget.algorithm.branching.branchgenerator.ApplyIterationToBestBranches;
import edu.cmu.hcii.jfogarty.gadget.algorithm.branching.convergeddetector.LimitBranchingDepth;
import edu.cmu.hcii.jfogarty.gadget.algorithm.convergeddetector.NumberOfIterationsWithoutAcceptance;
import edu.cmu.hcii.jfogarty.gadget.algorithm.externalizable.GadgetAlgorithmExternalizer;

import edu.cmu.hcii.jfogarty.gadget.displayobject.GadgetCanvas;
import edu.cmu.hcii.jfogarty.gadget.displayobject.GadgetDisplayObject;
import edu.cmu.hcii.jfogarty.gadget.displayobject.basicshape.BasicShape;
import edu.cmu.hcii.jfogarty.gadget.displayobject.basicshape.FilledRectangle;

import edu.cmu.hcii.jfogarty.gadget.evaluation.composite.GadgetCompositeEvaluation;
import edu.cmu.hcii.jfogarty.gadget.evaluation.composite.comparator.CompositeEvaluationComparator;
import edu.cmu.hcii.jfogarty.gadget.evaluation.composite.comparator.WeightedCompare;
import edu.cmu.hcii.jfogarty.gadget.evaluation.geometry.MinimizeAreaDisplayObjectsBoundingBox;
import edu.cmu.hcii.jfogarty.gadget.evaluation.geometry.MinimizeAreaDisplayObjectsConvexHull;
import edu.cmu.hcii.jfogarty.gadget.evaluation.graph.MaximizeConnectivityCount;

import edu.cmu.hcii.jfogarty.gadget.iteration.GadgetIterationResult;
import edu.cmu.hcii.jfogarty.gadget.iteration.composite.ApplyAllCompositeIteration;
import edu.cmu.hcii.jfogarty.gadget.iteration.composite.GadgetCompositeIteration;
import edu.cmu.hcii.jfogarty.gadget.iteration.composite.RandomChoiceCompositeIteration;
import edu.cmu.hcii.jfogarty.gadget.iteration.location.NudgeDisplayObject;
import edu.cmu.hcii.jfogarty.gadget.iteration.SwapPropertyBetweenTwoDisplayObject;

import edu.cmu.hcii.jfogarty.gadget.property.GadgetPropertyChangeListener;
import edu.cmu.hcii.jfogarty.gadget.property.GadgetPropertyKey;

import edu.cmu.hcii.jfogarty.gadget.util.GadgetDisplayObjectList;
import edu.cmu.hcii.jfogarty.gadget.util.GadgetDisplayObjectListIterator;
import edu.cmu.hcii.jfogarty.gadget.util.GadgetObjectList;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Checkbox;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Panel;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.TextField;
import java.awt.Toolkit;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentAdapter;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JPanel;


// Class Definition

public class GadgetPUCLayout extends Panel
{
    //**************************
    // Constants
    //**************************

    protected final static int COLUMN_MARGIN   = 5;
    protected final static int WINDOW_HEIGHT   = 300;
    protected final static int VERTICAL_MARGIN = 10;
    protected final static int ROW_HEIGHT      = 30;


    //**************************
    // Member Variables
    //**************************

    protected Label                       m_aryLabels[];
    protected Component                   m_aryComponents[];

    protected Hashtable                   m_hCompToGDO;

    private   GadgetAlgorithm             m_gaAlgorithm;
    private   GadgetDisplayObjectList     m_gdolItemsList;
    private   GadgetDisplayObjectList     m_gdolComponentsList;

    private   JFrame                      m_fFrame;
    private   GadgetCanvas                m_gcCanvas;

    protected int                         m_nCols;
    protected int                         m_nColWidth;
    

    //**************************
    // Static Main Function
    //**************************
    
    public static void main(String[] args){
	if ( args.length < 2 ) {
	    System.out.println( "Usage: GadgetPUCLayout <# of grid cols> <width of cols>" );
	}
	else {
	    int nCols = Integer.parseInt( args[0] );
	    int nColWidth = Integer.parseInt( args[1] );
	    
	    GadgetPUCLayout pGPL = new GadgetPUCLayout( nCols, nColWidth );
	    
	    int nSize = nCols * nColWidth + (nCols + 1) * COLUMN_MARGIN;
	    
	    Frame f = new Frame();
	    Insets i = f.getInsets();

	    pGPL.setLocation( i.left, i.top );
	    pGPL.setSize( nSize, WINDOW_HEIGHT - i.top - i.bottom );

	    f.add( pGPL );

	    f.setSize( nSize + i.left + i.right, 
		       WINDOW_HEIGHT );

	    f.setVisible( true );
	}
    }

    public GadgetPUCLayout( int nCols, int nColWidth ) {

	// save the variables
	m_nCols = nCols;
	m_nColWidth = nColWidth;

	// remove the layout manager
	setLayout( null );

	// Create the display object list
	m_gdolItemsList = new GadgetDisplayObjectList();
	m_gdolComponentsList = new GadgetDisplayObjectList();
	
	// Create the widgets and their labels
	createUIObjects();

	// add property change listener
	addComponentListener( new ComponentAdapter() {
		public void componentResized( ComponentEvent e ) {
		    Iterator pI = m_gdolItemsList.iterator();
		    
		    while ( pI.hasNext() ) {
			WidgetGDO pWGDO = (WidgetGDO)pI.next();
			Component pC = pWGDO.getWidget();
			
			Rectangle r = pWGDO.getBounds();
			pC.setSize( r.width, r.height );
			pC.setLocation( r.x, r.y );
		    }
		}
	    });

	m_hCompToGDO = new Hashtable();
    }

    public Dimension getPreferredSize() {
	return new Dimension( m_nCols * m_nColWidth + (m_nCols + 1) * COLUMN_MARGIN,
			      WINDOW_HEIGHT );
    }

    public void addNotify() {

	super.addNotify();

	for( int i = 0; i < m_aryLabels.length; i++ ) {
	    if ( m_aryLabels[ i ] != null ) {
		m_aryLabels[ i ].setSize( m_aryLabels[ i ].getPreferredSize() );
		//m_aryLabels[ i ].setLocation( 0, ROW_HEIGHT * i );
		//m_aryComponents[ i ].setLocation( m_aryLabels[ i ].getPreferredSize().width + COLUMN_MARGIN, ROW_HEIGHT * i );
	    }
	    else {
		//m_aryComponents[ i ].setLocation( COLUMN_MARGIN, ROW_HEIGHT * i );
	    }
	    
	    m_aryComponents[ i ].setSize( m_aryComponents[ i ].getPreferredSize() );

	}

	// Create our visual objects
	createVisualObjects();
	
	// Set up the optimization
	createOptimizationObjects();
	
	// Create the algorithm listener for repainting
	createAlgorithmListeners();
	
	m_gaAlgorithm.start();	
    }

    public void createUIObjects() {

	m_aryLabels = new Label[ 5 ];
	m_aryComponents = new Component[ 5 ];

	m_aryLabels[ 0 ] = new Label( "Name:", Label.RIGHT );
	m_aryComponents[ 0 ] = new TextField();

	m_aryLabels[ 1 ] = new Label( "Occupation:", Label.RIGHT );
	Choice c = new Choice();
	c.add( "Teacher" );
	c.add( "Student" );
	c.add( "Accountant" );
	c.add( "Designer" );
	c.add( "Unemployed" );
	m_aryComponents[ 1 ] = c;

	m_aryLabels[ 2 ] = null;
	m_aryLabels[ 3 ] = null;
	m_aryLabels[ 4 ] = null;

	m_aryComponents[ 2 ] = new Checkbox( "U.S. Citizen" );

	m_aryComponents[ 3 ] = new Button( "Submit" );
	m_aryComponents[ 4 ] = new Button( "Cancel" );

	for( int i = 0; i < m_aryLabels.length; i++ ) {
	    if ( m_aryLabels[ i ] != null )
		add( m_aryLabels[ i ] );

	    add( m_aryComponents[ i ] );
	}
    }
    
    public void createVisualObjects() {
	// Create the canvas
	m_gcCanvas = new GadgetCanvas();
	m_gcCanvas.setSize( this.getSize().width,
			    this.getSize().height );
	Point p = new Point( this.getSize().width/2,
			     this.getSize().height/2 );
	m_gcCanvas.setCenter( p );
	m_gcCanvas.setLocation( p );
	
	// Set up the frame
	m_fFrame = new JFrame();
	m_fFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
	
	// Get the canvas painted in the frame
	m_fFrame.getContentPane().setLayout(new BorderLayout());
	m_fFrame.getContentPane().add( new JPanel() {
		public Dimension getPreferredSize() {
		    return new Dimension(m_gcCanvas.getWidth(), m_gcCanvas.getHeight());
		}
		
		public void paintComponent(Graphics gPaint) {
		    gPaint.clearRect(getBounds().x, getBounds().y, getBounds().width, getBounds().height);
		    if(m_gaAlgorithm != null) {
			m_gaAlgorithm.paintCanvas(m_gcCanvas, gPaint);
		    }
		    
		    int nCurrentCol = COLUMN_MARGIN;
		    for( int i = 0; i < m_nCols * 2; i += 2 ) {
			int nLeftEdge = nCurrentCol;
			int nRightEdge = nCurrentCol + m_nColWidth;
			
			nCurrentCol += m_nColWidth + COLUMN_MARGIN;

			gPaint.drawLine( nLeftEdge, VERTICAL_MARGIN,
					 nRightEdge, VERTICAL_MARGIN );
			gPaint.drawLine( nLeftEdge, WINDOW_HEIGHT - VERTICAL_MARGIN,
					 nRightEdge, WINDOW_HEIGHT - VERTICAL_MARGIN );
			gPaint.drawLine( nLeftEdge, VERTICAL_MARGIN,
					 nLeftEdge, WINDOW_HEIGHT - VERTICAL_MARGIN );
			gPaint.drawLine( nRightEdge, VERTICAL_MARGIN,
					 nRightEdge, WINDOW_HEIGHT - VERTICAL_MARGIN );
		    }

		}
	    }, 
				        BorderLayout.CENTER );
	
	// Show it
	m_fFrame.pack();
	m_fFrame.show();	

	// Create the layout objects
	for( int i = 0; i < m_aryLabels.length; i++ ) {

	    WidgetGDO gdo;

	    if ( m_aryLabels[ i ] != null ) {

		gdo = new WidgetGDO( m_aryLabels[ i ] );

		m_gdolItemsList.add( gdo );
		m_hCompToGDO.put( m_aryLabels[ i ], gdo );
	    }
	    
	    gdo = new WidgetGDO( m_aryComponents[ i ] );

	    m_gdolItemsList.add( gdo );
	    m_gdolComponentsList.add( gdo );
	    m_hCompToGDO.put( m_aryComponents[ i ], gdo );
	}

	m_gcCanvas.addDisplayObjectList( m_gdolItemsList );
    }
    
    public void createAlgorithmListeners() {
	m_gaAlgorithm.addAlgorithmListener( new BranchingAlgorithmListener() {
		public void algorithmBestStateEncountered() {
		    Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent( new ComponentEvent( GadgetPUCLayout.this, ComponentEvent.COMPONENT_RESIZED ) );
		}
		
		public void algorithmConverged() {
		    System.out.println( "Converged!" );
		    m_gaAlgorithm.stop();
		    m_fFrame.dispose();
		}
		
		public void branchingNextBranch() {
		    m_fFrame.repaint();
		}
		
		public void iterationAccepted(GadgetIterationResult girIterationResult) {
		    System.out.println( "Iteration accepted." );
		    m_fFrame.repaint();
		}
		
		public void iterationRejected(GadgetIterationResult girIterationResult) {
		}
	    });
    }
    
    public void createOptimizationObjects() {
	CompositeEvaluationComparator       cecComparator;
	GadgetCompositeEvaluation           gceEvaluation;
	GadgetCompositeIteration            gciIteration;
	GadgetObjectList                    golEvaluationList;
	GadgetObjectList                    golIterationList;
	Iterator                            iColorIterator;
	Vector                              vWeights;
	
	// weights for the evaluators
	vWeights = new Vector();
	
	// list for the evaluators
	golEvaluationList = new GadgetObjectList();

	// create the evaluators
	for( int i = 0; i < m_aryLabels.length; i++ ) {
	    if ( i != 0 ) {
		for( int j = 0; j < i; j++ ) {
		    golEvaluationList.add( new KeepDisplayObjectAboveAnother( (GadgetDisplayObject)m_hCompToGDO.get( m_aryComponents[ j ] ),
									      (GadgetDisplayObject)m_hCompToGDO.get( m_aryComponents[ i ] ) ) );
		    vWeights.add(new Double(Math.exp((m_aryLabels.length - i))));
		}
	    }
	    
	    if ( m_aryLabels[ i ] != null ) {
		golEvaluationList.add( new MaintainPreferredHeight( (GadgetDisplayObject)m_hCompToGDO.get( m_aryLabels[ i ] ),
								    (double)m_aryLabels[ i ].getPreferredSize().height, 0.5 ) );
		vWeights.add(new Double(10));
		
		golEvaluationList.add( new KeepMinimumWidthOrLarger( (GadgetDisplayObject)m_hCompToGDO.get( m_aryLabels[ i ] ),
								     (double)m_aryLabels[ i ].getMinimumSize().width ) );
		vWeights.add(new Double(5));
	    }

	    golEvaluationList.add( new MaintainPreferredHeight( (GadgetDisplayObject)m_hCompToGDO.get( m_aryComponents[ i ] ),
								(double)m_aryComponents[ i ].getPreferredSize().height, 0.5 ) );
	    vWeights.add(new Double(10));

	    golEvaluationList.add( new KeepMinimumWidthOrLarger( (GadgetDisplayObject)m_hCompToGDO.get( m_aryComponents[ i ] ),
								 (double)m_aryComponents[ i ].getMinimumSize().width ) );
	    vWeights.add(new Double(5));

	    if ( m_aryLabels[ i ] == null ) continue;

	    golEvaluationList.add( new AlignLabelWithWidget( (GadgetDisplayObject)m_hCompToGDO.get( m_aryLabels[ i ] ), 
							     (GadgetDisplayObject)m_hCompToGDO.get( m_aryComponents[ i ] ), 
							     0.5 ) );
	    vWeights.add(new Double(2.5));
	}

	golEvaluationList.add( new MinimizeVerticalLines( m_gdolComponentsList,
							  COLUMN_MARGIN - 1 ) );
	vWeights.add(new Double(1));

	golEvaluationList.add( new AlignDisplayObjectsToGrid( m_gdolItemsList,
							      m_nCols,
							      m_nColWidth,
							      COLUMN_MARGIN,
							      VERTICAL_MARGIN,
							      this.getSize().height - VERTICAL_MARGIN,
							      0.5 ) );
	vWeights.add(new Double(1));

	Rectangle rBounds = new Rectangle( 0, 0, getSize().width, getSize().height );
	golEvaluationList.add(
			      new KeepDisplayObjectsOnScreen( m_gdolItemsList, rBounds )
				  );
	vWeights.add(new Double(100));

	
	// Set up the comparator for the composite
	cecComparator = new WeightedCompare(vWeights);
	
	// Set up the evaluators in a composite
	gceEvaluation = new GadgetCompositeEvaluation(golEvaluationList, cecComparator);
	
	// Create the iterators
	golIterationList = new GadgetObjectList();
	golIterationList.add( new NudgeDisplayObject(new GadgetDisplayObjectList(m_gdolItemsList), 10) );
	golIterationList.add( new ResizeDisplayObject(new GadgetDisplayObjectList(m_gdolItemsList), 5) );

	// Set up the iterations in a composite
	gciIteration = new RandomChoiceCompositeIteration(golIterationList);

	// Set up the algorithm
	m_gaAlgorithm = new NaiveHillClimbingAlgorithm(gciIteration, gceEvaluation);
	m_gaAlgorithm.setConvergedDetector(new NumberOfIterationsWithoutAcceptance(10000));
    }
}

