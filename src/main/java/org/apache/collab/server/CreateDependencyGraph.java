package org.apache.collab.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.neo4j.graphdb.factory.GraphDatabaseBuilder;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.io.fs.FileUtils;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.MethodDeclaration;
//import org.eclipse.jface.text.Document;



import java.util.ArrayList;
import java.util.List;

public class CreateDependencyGraph {

	
	 private final String DB_PATH = "target-DB-Client2/neo4j-dependencyGraph-db";
	 
	      GraphDatabaseService graphDb;
	      Node rootNode;
	    
	    
	    public  static enum dGraphNodeType implements Label {
	    	PROJECT, PACKAGE, CLASS, METHOD, ATTRIBUTE;
	    }   
	    
	    Relationship relationship;
	    
	    public static enum RelTypes implements RelationshipType
	    {
	    	CONNECTING, DEPENDENCY;
	    }
	    
/*	    public static void main(String args[]) {

	       	CreateDependencyGraph db= new CreateDependencyGraph();
	       	db.initializeDB();
				
		}
	*/    
	    
	    public void initializeDB() {
			
	    	try {
	       	 
	    		clearDb();
	    		File dbDir = new File(DB_PATH);
	    		GraphDatabaseFactory graphFactory = new GraphDatabaseFactory();
	    		GraphDatabaseBuilder graphBuilder = graphFactory.newEmbeddedDatabaseBuilder(dbDir);
	    		 graphDb = graphBuilder.newGraphDatabase();                  
	            registerShutdownHook( graphDb );

				createDB();
				shutDown(graphDb);	
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	System.out.println("Out of DB");
				
		}
	    
	public   void createDB() throws Exception
	{
		
		rootNode= createGraphAST();	

	}//end of createDB

	public Node createGraphAST() throws Exception
    {


      Transaction tx =null;
        
        // START SNIPPET: transaction
        try 
        {
            // Database operations go here
            // END SNIPPET: transaction
            // START SNIPPET: addData
        	
        	 System.out.println("creating transaction object");
        	 
        	 tx= graphDb.beginTx();

        	rootNode = graphDb.createNode(dGraphNodeType.PROJECT);
      
        	System.out.println("created rootNode object");

        	rootNode.setProperty( "name", "CollabProject" );
        	rootNode.setProperty("type", dGraphNodeType.PROJECT);

        	
        	System.out.println("setting root node properties");

            // START SNIPPET: transaction
            tx.success();
        }
        catch (Exception e)
        {
        	if ( tx != null )
            throw e;
        	e.printStackTrace();
        }
        finally {
        	
        	 if (tx != null) {
        		 tx.finish();
        		 System.out.println("Closing transaction object");
        		 
             }
        	            
        }
         
        return rootNode;
    }

	 private  void registerShutdownHook( final GraphDatabaseService graphDb )
	    {
	        // Registers a shutdown hook for the Neo4j instance so that it
	        // shuts down nicely when the VM exits (even if you "Ctrl-C" the
	        // running application).
	        Runtime.getRuntime().addShutdownHook( new Thread()
	        {
	            @Override
	            public void run()
	            {
	                graphDb.shutdown();
	            }
	        } );
	    }
	
 /*   public  void getMethodGraph(final CompilationUnit cu, final DependencyGraphEx dpGraph, final GraphDatabaseService graphDb, final Node cNode) {
		
  		cu.accept(new ASTVisitor() {
   
  			Set names = new HashSet();
  			String mName= null;
  			List<MethodDeclaration> methods = new ArrayList<MethodDeclaration>();
  			
  			public boolean visit(MethodDeclaration node) {
  				    methods.add(node);
  				    System.out.println(node.getName());
  				    mName= node.getName().toString();
  				    // add method node
  				    dpGraph.addMethodNode(graphDb, cNode, mName, "default", "default", "default",   "default",  "default",  "default");
  				    return false; // do not continue 
  				  }
  			 
  			 public List<MethodDeclaration> getMethods() {
  				    return methods;
  				  }
  			 
  			});
   
  	} 		
    */
    void shutDown(GraphDatabaseService graphDb)
    {
        System.out.println("line 227");
        System.out.println( "Shutting down database ..." );
        // START SNIPPET: shutdownServer
        graphDb.shutdown();
        System.out.println( "DB server shuting down complete" );
        // END SNIPPET: shutdownServer
    }
    
    private void clearDb() {
		try {
			FileUtils.deleteRecursively(new File(DB_PATH));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
