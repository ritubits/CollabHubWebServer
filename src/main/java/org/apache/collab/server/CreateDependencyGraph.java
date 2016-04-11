package org.apache.collab.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.HashMap;
import java.lang.reflect.Modifier;

import org.eclipse.jface.text.Document;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ChildPropertyDescriptor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.neo4j.graphdb.factory.GraphDatabaseBuilder;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.io.fs.FileUtils;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;

import java.lang.reflect.TypeVariable;

import org.eclipse.jdt.core.dom.Statement;
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
	        try 
	        {
	        	 System.out.println("creating transaction object");
	        	 tx= graphDb.beginTx();

	        	 createConnectingGraph(files);
	        	 createDependencyGraph(files);
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

	public void createConnectingGraph(File[] files) throws Exception
    {

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
     		//	System.out.println(filePath);
     			 if(f.isFile() && (f.getName().contains(".java"))){
     				 //print filename
     			//	 System.out.println("In file: "+ f.getName());
     				 
     				 //add class node with fileName f.getName()- java
     				 int index = (f.getName()).indexOf(".java");      	   
   	  	           	
     				CompilationUnit cu = parse(readFileToString(filePath));
     				String className= null;
     				String smallClassName= (f.getName()).substring(0, index);;
     				String packName = null;
     				if (cu.getPackage()!=null)
     				{
     					//System.out.println("package name is not null");
     					packName = parsePackageName(cu.getPackage().toString());
     					className= packName+"."+(f.getName()).substring(0, index);
     				}
     				else 
     					{
     					className= (f.getName()).substring(0, index);
     					packName ="null";
     					}
     			//	System.out.println("cu.types:: "+cu.types());
     				List<TypeDeclaration> types= cu.types();
     				String modifier=null;
     				Boolean isInterface= false;
     				String extend=null;     
     				for (TypeDeclaration t: types)
     				{     	
     					System.out.println("Creating Class");
     					isInterface= t.isInterface();   
     					modifier= Modifier.toString(t.getModifiers());
     					Type superClass= t.getSuperclassType();
     					if (superClass !=null) extend= superClass.toString();
     					
     					System.out.println("SuperClassType::"+extend);	
     					
        				if (isInterface)
        				{
        					cNode= dpGraph.addConnectingInterfaceNode(graphDb, rootNode, smallClassName, className, cu.imports().toString(), packName, modifier);
        				}
        				else cNode= dpGraph.addConnectingClassNode(graphDb, rootNode, smallClassName, className, cu.imports().toString(), packName, modifier, extend);
        				
     					//System.out.println("Class modifiers::"+Modifier.toString(t.getModifiers()));
     					FieldDeclaration[] fieldArr = t.getFields();
     					String attributeModifier=null;
     					String attributeType=null;
     					for (FieldDeclaration fArr: fieldArr)
     					{     						     					
     						attributeType = fArr.getType().toString();
     					//	System.out.println("field type::"+attributeType);
     						attributeModifier= Modifier.toString(fArr.getModifiers());
     					//	System.out.println("field modifiers::"+Modifier.toString(fArr.getModifiers()));
     						
     						List<VariableDeclarationFragment> frag = fArr.fragments();
     	  					int i=0;
     	  					String initializer=null;
     	  					String smallAttributeName =null;
     	  					String attributeName=null;
     	  					Node aNode=null;
     						for (VariableDeclarationFragment fd: frag)
     						{
     							Object o = fArr.fragments().get(i);
     							i++;
     													
     							if(o instanceof VariableDeclarationFragment){
     								smallAttributeName = ((VariableDeclarationFragment) o).getName().toString();
     								ChildPropertyDescriptor s2 = ((VariableDeclarationFragment) o).getNameProperty();

     								Expression s3 = ((VariableDeclarationFragment) o).getInitializer();
     								if (s3!=null) initializer = s3.toString();
     								int s4 = ((VariableDeclarationFragment) o).getExtraDimensions();
     								
     						//		System.out.println("SimpleName()::"+smallAttributeName);
     								attributeName= className+"."+smallAttributeName;
     						//		System.out.println("getInitializer::"+s3);
     								
     								aNode= dpGraph.addAttributeNode(graphDb, cNode, smallAttributeName, attributeName,attributeModifier,attributeType, initializer );
     								}	//if    					
     							}//for variable declaration     					
     					}//field declaration
     				}//type declaration
     			    //for each file, get its Methods and add nodes
     				 getMethodGraph(cu, dpGraph, graphDb, cNode);      				
     			 }// for all java files
     		 }//for all files
    }//for createCOnnectingGraph


	public void createDependencyGraph(File[] files) throws Exception
    {
		//read file content into a string
		//call createGraphAST for each file
	    HashMap<Long, String> nodeHashMap = dpGraph.getNodeHashMap();
	    HashMap<Long, String> edgeHashMap = dpGraph.getEdgeHashMap();
	    
	    System.out.println("Creating Dependency Graph");
     	String filePath = null;
     	Node cNode= null;
     	for (File f : files ) {
 			 filePath = f.getAbsolutePath();
 			System.out.println(filePath);
 			 if(f.isFile() && (f.getName().contains(".java"))){
 				 //print filename
 				 System.out.println("In file: "+ f.getName());
 				 
 				 //add class node with fileName f.getName()- java
 				 int index = (f.getName()).indexOf(".java");      	   
	  	           	
 				CompilationUnit cu = parse(readFileToString(filePath));
 				String className= null;
 				String smallClassName= (f.getName()).substring(0, index);;
 				String packName = null;
 				if (cu.getPackage()!=null)
 				{
 					//System.out.println("package name is not null");
 					packName = parsePackageName(cu.getPackage().toString());
 					className= packName+"."+(f.getName()).substring(0, index);
 				}
 				else 
 					{
 					className= (f.getName()).substring(0, index);
 					packName ="null";
 					}
 			//	System.out.println("cu.types:: "+cu.types());
 				List<TypeDeclaration> types= cu.types();
 				String modifier=null;
 				Boolean isInterface= false;
 				Long idSuperNode=(long) -1;  
 				Long idCurrentClassNode=(long) -1; 
 				for (TypeDeclaration t: types)
 				{     	
 					isInterface= t.isInterface();   
 					Type superClass= t.getSuperclassType();
 					System.out.println("SuperClassType::"+superClass);	
 					
 					if (superClass !=null)
 					{
 						//search for Class node in hashmap
 						System.out.println("Searching for superClassNode::"+superClass);
 						idSuperNode= searchClassNode(superClass.toString(), nodeHashMap);
 						if (idSuperNode != -1)
 						{
 							//super class node found
 							//create dependency edge from current node to super class node
 							idCurrentClassNode= searchClassNode(smallClassName, nodeHashMap);
 							
 							dpGraph.addDependencyEdge(graphDb, idSuperNode, idCurrentClassNode);
 						}
 					}
 				}
 			 }
     	}
    }//dependency Grpah
	
	public Long searchClassNode(String className, HashMap <Long,String> nodeHashMap)
	{
		//get id of class name in nodeHashMap else return -1
		Long id = (long) -1;

			    for (Map.Entry<Long, String> entry : nodeHashMap.entrySet()) {
			        if (Objects.equals(className, entry.getValue())) {
			            id= entry.getKey();
			            break;
			        }
			    }
				return id;		
	}
	
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
			//Document document= new Document(cu.getSource());
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
  			String mName= null;
  			String smallMethodName= null;
  			List<MethodDeclaration> methods = new ArrayList<MethodDeclaration>();  		
  			
  			public boolean visit(MethodDeclaration node) {
  				    methods.add(node);
  				//    System.out.println("MethodName:: "+node.getName());  				  
  				    int mod =node.getModifiers(); //get the int value of modifier
  			//	 System.out.println("Body:: "+node.getBody().toString());
  				 //print each statement
  			//	 System.out.println("Body::"+getMethodBodyString(node.getBody()));
  				  smallMethodName = node.getName().toString();
  				    mName= cNode.getProperty("canonicalName")+"."+smallMethodName;
  				    // add method node
  				    Node mNode= dpGraph.addMethodNode(graphDb, cNode, smallMethodName, mName, Modifier.toString(mod), node.getReturnType2().toString(),  node.parameters().toString());
				    
  				    return false; // do not continue 
  				  }
  			 
  			 public List<MethodDeclaration> getMethods() {
  				    return methods;
  				  }
  			 
  	/*		public boolean visit(FieldDeclaration fd){
				Object o = fd.fragments().get(0);
				if(o instanceof VariableDeclarationFragment){
					String s = ((VariableDeclarationFragment) o).getName().getFullyQualifiedName();
					System.out.println("Variable::"+s);
				//	if(s.toUpperCase().equals(s))
				//	System.out.println("-------------field: " + s);
				}

				return false;
			}*/
  			});
   
  	} 		
    
	 
  //  public  void getAttributeGraph(final CompilationUnit cu, final dependencyGraphNodes dpGraph, final GraphDatabaseService graphDb, final Node cNode) {
    	public  void getAttributeVisitor(final CompilationUnit cu) {
    	cu.accept(new ASTVisitor() {
    		 
    		Set names = new HashSet();
/*			public boolean visit(VariableDeclarationFragment node) {
				SimpleName name = node.getName();
				this.names.add(name.getIdentifier());
				System.out.println("Declaration of '"+name+"' at line"+cu.getLineNumber(name.getStartPosition()));
				System.out.println("Fully Qualified Name::"+name.getFullyQualifiedName());
				return false; // do not continue to avoid usage info
			}*/
 
/*			public boolean visit(SimpleName node) {
				if (this.names.contains(node.getIdentifier())) {
		//		System.out.println("Usage of '" + node + "' at line " +	cu.getLineNumber(node.getStartPosition()));
				}
				return true;
			}*/
			
  			public boolean visit(FieldDeclaration fd){
  				List<VariableDeclarationFragment> fieldArr= fd.fragments();

  					int i=0;
					for (VariableDeclarationFragment fArr: fieldArr)
					{
						Object o = fd.fragments().get(i);
						i++;
												
						if(o instanceof VariableDeclarationFragment){
							SimpleName s = ((VariableDeclarationFragment) o).getName();
							ChildPropertyDescriptor s2 = ((VariableDeclarationFragment) o).getNameProperty();

							Expression s3 = ((VariableDeclarationFragment) o).getInitializer();						
							int s4 = ((VariableDeclarationFragment) o).getExtraDimensions();
							
							System.out.println("SimpleName()::"+s);
							System.out.println("getInitializer::"+s3);


						//	if(s.toUpperCase().equals(s))
						//	System.out.println("-------------field: " + s);
						}	
					}
				return false;
			}
 
 /* 			public boolean visit(SingleVariableDeclaration  node){
  				SimpleName name = node.getName();
				//this.names.add(name.getIdentifier());
				System.out.println("SingleVariableDeclaration::modifier()::"+Modifier.toString(node.getModifiers()));
				System.out.println("SingleVariableDeclaration::name::"+node.getName());
				System.out.println("SingleVariableDeclaration::initializer::"+node.getInitializer());
				
				return false;
			}*/
		});
    }
    
	 public void parseFiles(File[] files)
	 {
     	//parsing files

	 }
	 
	 public String getMethodBodyString(Block methodBlock)
	 {
		 String methodBody=null;
		 
		 Block block= methodBlock;
			if (block != null) {
				List<Statement> statements= block.statements();
			//	if (statements.size() > 0) {
			//		Statement last= statements.get(statements.size() - 1);
				methodBody= statements.toString();
			//	}
			}
			return methodBody;
	 }
	 
	 public String parsePackageName(String pName)
	 {
		 String packName=null;
		 int index = pName.indexOf(" ")+1;
		 packName= pName.substring(index, pName.length()-2);
//		 System.out.println("PackageName:::"+packName);
		 return packName;				 
	 }
    void shutDown(GraphDatabaseService graphDb)
    {
        System.out.println("line 227");
        System.out.println( "Shutting down database ..." );

        graphDb.shutdown();
        System.out.println( "DB server shuting down complete" );

    }
    
    private void clearDb() {
		try {
			FileUtils.deleteRecursively(new File(DB_PATH));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
