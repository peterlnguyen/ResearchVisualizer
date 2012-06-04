/*
w * SimpleGraphView2.java
 *
 * Created on March 20, 2007, 7:49 PM
 *
 * Copyright March 20, 2007 Grotto Networking
 */

import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;

import edu.uci.ics.jung.io.PajekNetReader;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFrame;

import org.apache.commons.collections15.FactoryUtils;
import org.apache.commons.collections15.Transformer;

/**
 * Demonstrates how to use the DefaultModalGraph mouse's key listener to
 * change modes.  This is one additional line of code from
 * InterativeGraphView1.  Usage: type a "t" while the mouse is over the graph
 * for transf`orming mode and a "p" for picking mode.
 * @author Dr. Greg M. Bernstein
 */

public class InteractiveGraphView22 {
    static Graph<Integer, String> g;
    static ArrayList<Float> xcoords = new ArrayList<Float>();
    static ArrayList<Float> ycoords = new ArrayList<Float>();
    static ArrayList<ArrayList<Integer>> nextNode = new ArrayList<ArrayList<Integer>>();
    static Map<Integer, Integer> uniqueNodesAndNumOfHits = new HashMap<Integer, Integer>();

    /** Creates a new instance of SimpleGraphView */
    public InteractiveGraphView22() {
        // Graph<V, E> where V is the type of the vertices and E is the type of the edges
        g = new SparseMultigraph<Integer, String>();
        
        for(int i = 1; i < 35; i++) {
        	g.addVertex(i);
        }
        
        // reads/adds in vertices and edges
        try {
	    	FileInputStream in = new FileInputStream(System.getProperty("user.dir") + "//src//" + "graph.txt");
	    	BufferedReader br = new BufferedReader(new InputStreamReader(in));
	    	 
	    	String strLine;
	    	String[] myArray;
	    	         
	    	while ((strLine = br.readLine()) != null) {
	    		myArray = strLine.split("\t");
	    		int first = Integer.parseInt(myArray[0]);
	    		int second = Integer.parseInt(myArray[1]);
	    		g.addEdge(first + "-" + second, first, second);
	    	}
	    	in.close();
    	} catch(IOException e) {
    		e.printStackTrace();
    	}
    }
    
    // populates path list of nodes (order of nodes that are hit) to ArrayList<Integer> nextNode
    public static void initNextNode(String filename) {
    	try {
	    	FileInputStream in = new FileInputStream(System.getProperty("user.dir") + "//src//" + filename);
	    	BufferedReader br = new BufferedReader(new InputStreamReader(in));
	    	 
	    	String strLine;
	    	String[] myArray;
	    	         
	    	while ((strLine = br.readLine()) != null) {
	    		//System.out.println(strLine);
	    		myArray = strLine.split(",");
	    		for(int numOfVertices = 0; numOfVertices < myArray.length; numOfVertices++) {
	    			int vertex = Integer.parseInt(myArray[numOfVertices]);
	    			if(numOfVertices == 0) {
	    				nextNode.add(new ArrayList<Integer>());
	    			}
	    			else {
		    			int currentLine = nextNode.size() - 1;
	    				nextNode.get(currentLine).add(vertex);
	    				addNodeIfUnique(vertex);
	    			}
	    		}
	    		//System.out.println("Current line parsed: " + nextNode.get(nextNode.size() - 1).toString());
	    	}
	    	in.close();
    	} catch(IOException e) {
    		e.printStackTrace();
    	}
    }
    
    public static void sleep(int ms) {
        try {
			Thread.sleep(ms);
		} 
        catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    private static void writeToImageFile(String imageFileName, JComponent jPanel1) {
    	   BufferedImage bufImage = ScreenImage.createImage((JComponent) jPanel1);
    	   try {
    	       File outFile = new File(imageFileName);
    	       ImageIO.write(bufImage, "jpeg", outFile);
    	       System.out.println("wrote image to " + imageFileName);
    	   } catch (Exception e) {
    	       System.out.println("writeToImageFile(): " + e.getMessage());
    	   }
    }
 
    
    /**
     * @param args the command line arguments
     */
	public static void main(String[] args) {
    	
    	//initNextNode("epidemic_100.csv");
    	//initNextNode("epidemic_300.csv");
    	initNextNode("random_walk_300.csv");
    	//initNextNode("random_walk_100.csv");
    	

        JFrame frame = new JFrame("Interactive Graph View 2");
        InteractiveGraphView22 sgv = new InteractiveGraphView22(); // Creates the graph...
        Layout<Integer, String> layout = new  StaticLayout<Integer, String>(sgv.g);
        
	    for(int j = 0; j < nextNode.size(); j++) 
	    {
	        if(j == 0) { layout = new  FRLayout<Integer, String>(sgv.g); }
	        layout.setSize(new Dimension(300,300));
	        VisualizationViewer<Integer,String> vv = new VisualizationViewer<Integer,String>(layout);
	        vv.setPreferredSize(new Dimension(350,350));	     
	        
	        /* changes node color */
	        final int z = j;
	        Transformer<Integer,Paint> vertexPaint = new Transformer<Integer,Paint>() {
	            public Paint transform(Integer i) {
	            	
	            	// if current node being drawn is one of the nodes being accessed, colors it black
	            	for(int a = 0; a < nextNode.get(z).size(); a++) {
	            		int currentNodeNum = nextNode.get(z).get(a);
		            	if(currentNodeNum == (int)i)
		            	{			     
		            		incrementNodeHitCount(i);
		            		return Color.RED;
		            	}
	            	}
	            	
	            	// if current node being drawn is NOT one of the nodes being accessed, colors appropriately
	            	for(Map.Entry<Integer, Integer> entry : uniqueNodesAndNumOfHits.entrySet()) {
	            		int currentNodeNum = entry.getKey();
	            		int numOfHits = entry.getValue();
	            		if(i == currentNodeNum) {
	            			return Color.WHITE;
	            		}
	            	}
	                return Color.WHITE;
	            }
	        };
	        
	        /* changes node size */
	        Transformer<Integer,Shape> vertexSize = new Transformer<Integer,Shape>(){
	            public Shape transform(Integer i){
	                Ellipse2D circle = new Ellipse2D.Double(-15, -15, 30, 30);

	            	double scaleAmount;
	            	int nodeHitCount;
	            	nodeHitCount = getNodeHitCount(i);
	            	
	            	scaleAmount = Math.sqrt(Math.pow(nodeHitCount, 1.05) * 0.001) + .20;
            		return AffineTransform.getScaleInstance(scaleAmount, scaleAmount).createTransformedShape(circle);
	            }
	        }; 

	        /* Generates new coordinates if first graph, else loads coords from previous graphs */
	        Double coords;
        	if(j == 0)
        	{
        		for(int i = 1; i < layout.getGraph().getVertexCount(); i++) 
        		{
        			// Re-aligns all the coordinates by +20x and +20y to center the image.
	        		xcoords.add( Float.valueOf((String.valueOf(((AbstractLayout<Integer, String>) layout).getX(i)))) + 20);
	        		ycoords.add( Float.valueOf((String.valueOf(((AbstractLayout<Integer, String>) layout).getY(i)))) + 20);
	        		
		        	coords = new Point2D.Double(xcoords.get(i-1), ycoords.get(i-1));
		        	layout.setLocation(i, coords);
	        	}
        	}
        	else
        	{
        		// loads vertex locations from arraylist.
        		for(int i = 1; i < layout.getGraph().getVertexCount(); i++) 
        		{
		        	coords = new Point2D.Double(xcoords.get(i-1), ycoords.get(i-1));
		        	layout.setLocation(i, coords);
		        }
        	}          
   
	        vv.getRenderContext().setLabelOffset(1000);
	        vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint); // allows node color changes
	        vv.getRenderContext().setVertexShapeTransformer(vertexSize); // allows node size changes
	        DefaultModalGraphMouse gm = new DefaultModalGraphMouse();
	        gm.setMode(ModalGraphMouse.Mode.TRANSFORMING);
	        vv.setGraphMouse(gm); 	     
	        
	        // Add the mouses mode key listener to work it needs to be added to the visualization component
	        frame = new JFrame("Interactive Graph View 2");
	        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	        vv.setBounds(100, 100, 500, 500);
	        frame.getContentPane().add(vv);
	        frame.pack();
	        
	        frame.setVisible(true);
	        // needs to sleep to allow the file to be correctly drawn and written to the file
	        sleep(2000);
	        
	        int n = j + 1;
	        String filename = "epidemic_100" + n + ".jpeg"; 
	        writeToImageFile(filename, vv);

    	}
	    notifyEndOfLoop();
    } 
    
    public static void notifyEndOfLoop()
    {
    	System.out.println("Notification: End of loop.");
    }
    
    public static void addNodeIfUnique(int newNode) {
    	if(!uniqueNodesAndNumOfHits.containsKey(newNode)) {
    		uniqueNodesAndNumOfHits.put(newNode, 0);
    	}
    }
    
    // attempted to color nodes based on amount of hits, but the Color class doesn't recognize many hex colors, and just paints them black
    // this method is not used
    public static Color getColorForNumOfHits(int currentNodeNum) {	
    	if(currentNodeNum == 0) return new Color(0xFFFFFF); // 1
    	if(currentNodeNum <= 10) return new Color(0xFFFFF0); // 2 
    	if(currentNodeNum <= 20) return new Color(0xEEEED1); // 3
    	if(currentNodeNum <= 30) return new Color(0xEEE8AA); // 4
    	if(currentNodeNum <= 37) return new Color(0xFBEC5D); // 5
    	if(currentNodeNum <= 44) return new Color(0xFFFF00); // 6
    	if(currentNodeNum <= 50) return new Color(0xFFD700); // 7
    	if(currentNodeNum <= 56) return new Color(0xFFCC11); // 8
    	if(currentNodeNum <= 61) return new Color(0xFCB514); // 9
    	if(currentNodeNum <= 66) return new Color(0xFFA824); // 10
    	if(currentNodeNum <= 70) return new Color(0xFF8C00); // 11
    	if(currentNodeNum <= 74) return new Color(0xDD7500); // 12
    	if(currentNodeNum <= 77) return new Color(0x8B4500); // 13
    	if(currentNodeNum <= 80) return new Color(0x603311); // 14
    	if(currentNodeNum <= 83) return new Color(0x993300); // 15
    	if(currentNodeNum <= 86) return new Color(0xC73F17); //16
    	if(currentNodeNum <= 89) return new Color(0xCC1100); // 17
    	if(currentNodeNum <= 92) return new Color(0xEE0000); // 18
    	else return new Color(3, 3, 3);
    }
    
    public static void incrementNodeHitCount(int currentNodeNum) {
    	uniqueNodesAndNumOfHits.put(currentNodeNum, uniqueNodesAndNumOfHits.get(currentNodeNum) + 1);
    }
    
    public static int getNodeHitCount(int currentNodeNum) {
    	if(!uniqueNodesAndNumOfHits.containsKey(currentNodeNum)) return 0; // was used as a hack because a non-existing node number was being called
    	return uniqueNodesAndNumOfHits.get(currentNodeNum);
    }
    
    public static void resetNodeHitCount() {
    	for(Map.Entry<Integer, Integer> entry : uniqueNodesAndNumOfHits.entrySet()) {
    		int currentNodeNum = entry.getKey();
    		uniqueNodesAndNumOfHits.put(currentNodeNum, 0);
    	}
    }
 
    
}
