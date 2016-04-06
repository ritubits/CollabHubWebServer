package org.apache.collab.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
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
	 private static final String SRC_URL = "D:\\TestGitProjectRepo\\ParallelCollab\\Ass1\\src";
	 private String projectName;
	 private dependencyGraphNodes dpGraph;
	 
	      GraphDatabaseService graphDb;
	      Node rootNode;
	    
	    
	    public  static enum dGraphNodeType implements Label {
	    	PROJECT, PACKAGE, CLASS, METHOD, ATTRIBUTE;
	    }   
	    

	    
	    public static void main(String args[]) {

	       	CreateDependencyGraph db= new CreateDependencyGraph();
	       	db.initializeDB(SRC_URL, "CollabProject");
				
		}
   
	    
	    public void initializeDB(String srcURL, String pName) {
			
	    	try {
	    		projectName = pName;
	    		dpGraph = new dependencyGraphNodes();
	    		System.out.println(srcURL);
	    		File root = new File(srcURL);
	    		//System.out.println(root.listFiles());
	    		File[] files = root.listFiles ( );
	    	//	parseFiles(files);
	    		
	    		clearDb();
	    		File dbDir = new File(DB_PATH);
	    		GraphDatabaseFactory graphFactory = new GraphDatabaseFactory();
	    		GraphDatabaseBuilder graphBuilder = graphFactory.newEmbeddedDatabaseBuilder(dbDir);
	    		 graphDb = graphBuilder.newGraphDatabase();                  
	            registerShutdownHook( graphDb );

				createDB(files);
				shutDown(graphDb);	
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	System.out.println("Out of DB");
				
		}

	public Node createDB(File[] files) throws Exception
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

        	rootNode.setProperty( "name", projectName );
        	rootNode.setProperty("nodeType", "PROJECT");
        	rootNode.addLabel(dGraphNodeType.PROJECT);

    		//call createGraphAST for each file
         	String filePath = null;
         	Node cNode= null;
         	for (File f : files ) {
     			 filePath = f.getAbsolutePath();
     			System.out.println(filePath);
     			 if(f.isFile() && (f.getName().contains(".java"))){
     				 //print filename
     				 System.out.println("In file: "+ f.getName());
     				 
     				 //add class node with fileName f.getName()
     				 cNode= dpGraph.addConnectingClassNode(graphDb, rootNode, f.getName(), "default", "default","default", "default");
     				
     				 CompilationUnit cu = parse(readFileToString(filePath));
     				 //for each file, get its Methods and add nodes
     				 getMethodGraph(cu, dpGraph, graphDb, cNode);
     				 
     				 //pass the entire AST of a file to create the dependency graph
     				 //dpGraph.createDependencyGraph(cu, graphDb, rootNode,f.getName());
     			 }
     		//	 System.out.println("here123");
     		 }
        	System.out.println("created graph");

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
        		 tx.close();
        		 System.out.println("Closing transaction object");
        		 
             }
        	            
        }
         
        return rootNode;
    }

    
		public   void createGraphAST(File[] files) throws Exception
		{
			
		//	rootNode= createGraphAST();	
			
		
		
		}//end of createGraphAST

		//read file content into a string
		public static String readFileToString(String filePath) throws IOException 
		{
			StringBuilder fileData = new StringBuilder(1000);
			BufferedReader reader = new BufferedReader(new FileReader(filePath));
	 
			char[] buf = new char[10];
			int numRead = 0;
			while ((numRead = reader.read(buf)) != -1) {
	
				String readData = String.valueOf(buf, 0, numRead);
				fileData.append(readData);
				buf = new char[1024];
			}
			reader.close();
			return  fileData.toString();	
		}
		
		//use ASTParse to parse string
		public static CompilationUnit parse(String str) {
			// each str contains the str content of a single java file
			ASTParser parser = ASTParser.newParser(AST.JLS3);
			parser.setSource(str.toCharArray());
			parser.setKind(ASTParser.K_COMPILATION_UNIT);
	 
			 // In order to parse 1.5 code, some compiler options need to be set to 1.5
			 Map options = JavaCore.getOptions();
			 JavaCore.setComplianceOptions(JavaCore.VERSION_1_5, options);
			 parser.setCompilerOptions(options);
			 
			final CompilationUnit cu = (CompilationUnit) parser.createAST(null);

			return cu;
			
			
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
	
    public  void getMethodGraph(final CompilationUnit cu, final dependencyGraphNodes dpGraph, final GraphDatabaseService graphDb, final Node cNode) {
		
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
    
	 
	 public void parseFiles(File[] files)
	 {
     	//parsing files

	 }
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
