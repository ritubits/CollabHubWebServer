package org.apache.collab.server;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.collab.server.Comparator.CompareGraphs;
import org.apache.collab.server.CreateDependencyGraph.RelTypes;
import org.apache.collab.server.CreateDependencyGraph.dGraphNodeType;
import org.apache.collab.server.CreateDependencyGraph.dMethodNodeType;
import org.apache.collab.server.CreateDependencyGraph.methodRelTypes;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ChildPropertyDescriptor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseBuilder;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.io.fs.FileUtils;

public class CreateUserArtifactGraph {

	String fileContent=null;
	String fileName=null;
	String collabName=null;
	 private dependencyGraphNodes dpGraph;
	 GraphDatabaseService graphDb;
	 Node artifactNode;
	    String tryBody=null;
	    
	    long lEndTime;//System.currentTimeMillis();
		long difference;
		long lStartTime;
		
	 private String DB_PATH_CLIENT = "neo4jDB/Client/";
	 private static final String SRC_URL = "D:\\TestGitProjectRepo\\ParallelCollab\\Ass1\\src";
	 
	    public  static enum dGraphNodeType implements Label {
	    	PROJECT, PACKAGE, CLASS, INTERFACE, METHOD, ATTRIBUTE;
	    }   
	    
	    public  static enum dMethodNodeType implements Label {
	    	VariableDeclarationNode, PACKAGE;
	    }  
	    
	    public static enum methodRelTypes implements RelationshipType
	    {
	    	BODY;
	    }
	    
	    public static enum RelTypes implements RelationshipType
	    {
	    	CONNECTING, DEPENDENCY;
	    }
	    
	public CreateUserArtifactGraph(String file, String fName, String cName) {
		// TODO Auto-generated constructor stub
		fileContent = file;
		fileName= fName;
		collabName= cName;
		DB_PATH_CLIENT = DB_PATH_CLIENT +collabName;
	}

/*    public  long getCpuTime( ) {
        ThreadMXBean bean = ManagementFactory.getThreadMXBean( );
        return bean.isCurrentThreadCpuTimeSupported( ) ?
            bean.getCurrentThreadCpuTime( ) : 0L;
    }*/
    
	public void createGraph()
	{
		System.out.println("DB_PATH_CLIENT:: "+ DB_PATH_CLIENT);
		initializeDB();
		
	}
	
    public void initializeDB() {
    //	lStartTime = System.currentTimeMillis();
    	try {
    		dpGraph = new dependencyGraphNodes();
    		
    		clearDb();
    		File dbDir = new File(DB_PATH_CLIENT);
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
    	
    //	lEndTime = System.currentTimeMillis();
    //	difference = lEndTime - lStartTime;

    //	System.out.println("Elapsed milliseconds after creation of user Graph: " + difference);
	}
    
    public Node createDB() throws Exception
    {
      Transaction tx =null;
        try 
        {
        	 System.out.println("creating transaction object");
        	 tx= graphDb.beginTx();

        	 createConnectingGraph();
        	 createDependencyGraph();
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
    return artifactNode;
    }
    
    
	public void createConnectingGraph() throws Exception
    {
        	//artifactNode = graphDb.createNode(dGraphNodeType.CLASS);      
        	System.out.println("created artifactNode object:: "+fileName);     	   
   	  	           	
			CompilationUnit cu = parse(fileContent, fileName);
			String smallClassName= parse(fileName);
			String className=null;
			String packName = null;
			if (cu.getPackage()!=null)
			{
				//System.out.println("package name is not null");
				packName = parsePackageName(cu.getPackage().toString());
				className= packName+"."+smallClassName;
			}
			else 
				{
				className= smallClassName;
				packName ="null";
				}
  
			
     				List<TypeDeclaration> types= cu.types();
     				String modifier=null;
     				Boolean isInterface= false;
     				String extend=null;     
     				String implemented=null;  
     				TypeDeclaration t;
     				for (AbstractTypeDeclaration t1: types)
     				{     	
     					if (t1 instanceof org.eclipse.jdt.core.dom.TypeDeclaration)
     					{
     						t= (org.eclipse.jdt.core.dom.TypeDeclaration)t1;
     					System.out.println("Creating Class");
     					isInterface= t.isInterface();   
     					modifier= Modifier.toString(t.getModifiers());
     					Type superClass= t.getSuperclassType();
     					if (superClass !=null) extend= superClass.toString();
     					
     				//	System.out.println("SuperClassType::"+extend);	
     					List implementedList= t.superInterfaceTypes();
     				//	System.out.println("SuperImplemented::"+implementedList);
     					
     					if (!implementedList.isEmpty())
     					{
     						implemented = implementedList.toString();
     					}
     					else 
     						{implemented ="[null]";
     					//	System.out.println("Implemented list is null");
     						}
        				if (isInterface)
        				{
        					artifactNode= dpGraph.createIndependentInterfaceNode(graphDb, smallClassName, className, cu.imports().toString(), packName, modifier);
        				}
        				else artifactNode= dpGraph.createIndependentClassNode(graphDb, smallClassName, className, cu.imports().toString(), packName, modifier, extend, implemented);
        				
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
     								
     								aNode= dpGraph.addAttributeNode(graphDb, artifactNode, smallAttributeName, attributeName,attributeModifier,attributeType, initializer );
     								}	//if    					
     							}//for variable declaration     					
     					}//field declaration
     					}//if
     				}//type declaration
     			    //for each file, get its Methods and add nodes
     				 getMethodGraph(cu, dpGraph, graphDb, artifactNode);      				

    }//for createCOnnectingGraph
	
	 public  void getMethodGraph(final CompilationUnit cu, final dependencyGraphNodes dpGraph, final GraphDatabaseService graphDb, final Node cNode) {
			
	  		cu.accept(new ASTVisitor() {
	  			String mName= null;
	  			String smallMethodName= null;
	  			String methodBody=null;
	  			List<MethodDeclaration> methods = new ArrayList<MethodDeclaration>();  		
	  			
	  			public boolean visit(MethodDeclaration node) {
	  				    methods.add(node);
	  				 //   System.out.println("MethodName:: "+node.getName());
	  				 //   if (node.getName().equals("addToNodeHashMap")) 
	  				 //   	System.out.println("MethodBody:: "+node.getBody());
	  				    int mod =node.getModifiers(); //get the int value of modifier
	  				 
	  				  smallMethodName = node.getName().toString();
	  				    mName= cNode.getProperty("canonicalName")+"."+smallMethodName;
	  				    
	  				  if (node.getBody() !=null) 
	  					  {
	  					//  System.out.println(node.getBody());
	  					  methodBody= transformMethodBody(cu, node.getBody());
	  					// System.out.println(methodBody);
	  					  }
	  				  else methodBody="null";
	  				    // add method node
	  				  String param=null;
	  				List parameterList= node.parameters();
	  				if (parameterList.isEmpty()) param = parameterList.toString();
	  				else param = "null";
	  				
	  				String returnTypeString=null;
	  				Type returnType= node.getReturnType2();
	  				if (returnType !=null) returnTypeString = returnType.toString();
	  				else returnTypeString = "null";
	  				
	  				    Node mNode= dpGraph.addMethodNode(graphDb, cNode, smallMethodName, mName, Modifier.toString(mod), returnTypeString,  param, methodBody);
	  				  if (node.getBody() !=null) visitMethodBlock(dpGraph,graphDb, node.getBody(), mNode);
	  				    return false; // do not continue 
	  				  }
	  			 
	  			 public List<MethodDeclaration> getMethods() {
	  				    return methods;
	  				  }
	  			});
	  	} 	
	 
	 public void visitMethodBlock(final dependencyGraphNodes dpGraph, final GraphDatabaseService graphDb, Block methodBlock, final Node mNode)
	 {
		 methodBlock.accept(new ASTVisitor()
		 {
	  			public boolean visit(VariableDeclarationStatement node){
	  				
						String attributeType = node.getType().toString();
					//	System.out.println("Variable::"+attributeType);
						
						String attributeModifier = Modifier.toString(node.getModifiers());
					//	System.out.println("Modifier::"+attributeModifier);
						
						List<VariableDeclarationFragment> fd= node.fragments();
	  					int i=0;
						for (VariableDeclarationFragment fArr: fd)
						{
							Object o = node.fragments().get(i);
							i++;
													
							if(o instanceof VariableDeclarationFragment){
								String smallAttributeName = ((VariableDeclarationFragment) o).getName().toString();
								String attributeName= mNode.getProperty("canonicalName")+"."+smallAttributeName;
								ChildPropertyDescriptor s2 = ((VariableDeclarationFragment) o).getNameProperty();

								Expression s = ((VariableDeclarationFragment) o).getInitializer();						
								String initializer = null;
								if (s!= null)  initializer = ((VariableDeclarationFragment) o).getInitializer().toString();
								else initializer = "null";
								//int s4 = ((VariableDeclarationFragment) o).getExtraDimensions();
								
						//		System.out.println("SimpleName()::"+smallAttributeName);
						//		System.out.println("CompleteName::"+attributeName);
						//		System.out.println("getInitializer::"+initializer);
								
								//create VariableDeclarationNode
								dpGraph.addVariableDeclarationNode(graphDb, mNode, smallAttributeName, attributeName,attributeModifier,attributeType, initializer );
							}	
						}
					return false;
				}
		 });
	 }
	 
	 public String transformMethodBody(final CompilationUnit cu, Block methodBlock)
	 {
		 String methodBody=null;
		 
		 Block block= methodBlock;
		 
			if (block != null) {			
				 int lineNumber = cu.getLineNumber(block.getStartPosition()) - 1;

		//		System.out.println("Line no:: "+lineNumber); 
				List<Statement> statements= block.statements();

				int i=0;
				String str=null;

				for (Statement st: statements)
					{
						if (st!=null)
						{
							str= block.statements().get(i).toString();
							if (st instanceof TryStatement)
							{							
							st.accept(new ASTVisitor() {								 
								String body=null;
								Block finallyBlock;
								List catchBody =null; 
								String strTry=null;
								public boolean visit(TryStatement node) {
		
								//	System.out.println("TryBlock: " + node.getBody());
									body = "try"+node.getBody();
									finallyBlock= node.getFinally();
									catchBody= node.catchClauses();
									if (finallyBlock ==null && catchBody !=null)
										strTry= body+ catchBody.toString();
									else 
										if (finallyBlock !=null && catchBody !=null)
											strTry= body+ catchBody.toString() + finallyBlock.statements().toString();
									
									//	System.out.println("TryBlock: " + strTry);
									tryBody= strTry;
									return true;
									
								}
					 
							});}
							
							if (tryBody ==null)
							{
								if (methodBody == null) methodBody = "["+lineNumber+"]:"+ str;
								else methodBody= methodBody + "["+lineNumber+"]:"+ str;
							}
							else
							{
								if (methodBody == null) methodBody = "["+lineNumber+"]:"+ tryBody;
								else methodBody= methodBody + "["+lineNumber+"]:"+ tryBody;
								//need to increment line No and parse trybody
							}
								
							lineNumber++;
							i++;
							tryBody=null;
						}
					}				
			}
			if (methodBody == null) return "null";
			else
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
	 
	//use ASTParse to parse string
	public static CompilationUnit parse(String str, String fileName) {
		// each str contains the str content of a single java file
		ASTParser parser = ASTParser.newParser(AST.JLS4);
		
		parser.setUnitName(fileName); 
		String[] sources = { "" };
		String[] classpath = { "" };
		//setEnvironment for resolve bindings even if the args is empty
		parser.setEnvironment(classpath, sources, new String[] { "UTF-8" }, true);
		
		parser.setSource(str.toCharArray());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setResolveBindings(true);
		parser.setBindingsRecovery(true);
		 // In order to parse 1.5 code, some compiler options need to be set to 1.5
		 Map options = JavaCore.getOptions();
		 JavaCore.setComplianceOptions(JavaCore.VERSION_1_6,options);
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
	 
    private void clearDb() {
		try {
			FileUtils.deleteRecursively(new File(DB_PATH_CLIENT));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}//clearDb
    
    void shutDown(GraphDatabaseService graphDb)
    {
        System.out.println( "Shutting down database ..." );
        graphDb.shutdown();
        System.out.println( "DB server shuting down complete" );
    }//db shutdown
    
	public void createDependencyGraph() throws Exception
    {
		//read file content into a string
		//call createGraphAST for each file
	    HashMap<Long, String> nodeHashMap = dpGraph.getNodeHashMap();
	    HashMap<Long, String> edgeHashMap = dpGraph.getEdgeHashMap();
	    HashMap<Long, String> methodHashMap = dpGraph.methodHashMap;
	    		
    	System.out.println("In createDependencyGraph():: "+fileName);     	   
	           	
		CompilationUnit cu = parse(fileContent, fileName);
		String smallClassName= fileName;
		String className=null;
		String packName = null;
		if (cu.getPackage()!=null)
		{
			//System.out.println("package name is not null");
			packName = parsePackageName(cu.getPackage().toString());
			className= packName+"."+smallClassName;
		}
		else 
			{
			className= smallClassName;
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
 				//	System.out.println("SuperClassType::"+superClass);	
 					
 					if (superClass !=null)
 					{
 						//search for Class node in hashmap
 				//		System.out.println("Searching for superClassNode::"+superClass);
 						idSuperNode= searchClassNode(superClass.toString(), nodeHashMap);
 						if (idSuperNode != -1)
 						{
 							//super class node found
 							//create dependency edge from current node to super class node
 							idCurrentClassNode= searchClassNode(smallClassName, nodeHashMap);
 							
 							dpGraph.addExtendsDependencyEdge(graphDb, idSuperNode, idCurrentClassNode);
 						}
 					}//if (superClass !=null)
 					
 				//	System.out.println("SuperInterfaceType::"+t.superInterfaceTypes());
 					List<SimpleType> interfacesImplemented= t.superInterfaceTypes();
 					//iterate over list and search for all nodes and add depedency edges
 					Long idinterfaceNode= (long) -1;
 					if (!interfacesImplemented.isEmpty())
 					{
 						//code goes here....
 						for (SimpleType interfaces: interfacesImplemented)
 						{
 						//	System.out.println("Interfaces::"+interfaces);
 							//search for the existence of interface in graph
 							idinterfaceNode= searchClassNode(interfaces.toString(), nodeHashMap);
 							if (idinterfaceNode != -1)
 	 						{
 	 							//interface node found
 	 							//create dependency edge from current node to interface node
 	 							idCurrentClassNode= searchClassNode(smallClassName, nodeHashMap);
 	 							
 	 							dpGraph.addImplementsDependencyEdge(graphDb, idinterfaceNode, idCurrentClassNode);
 	 						}//if (idinterfaceNode != -1)
 						}//for (SimpleType interfaces: interfacesImplemented)
 						
 					}//if (!interfacesImplemented.isEmpty()) 					
 				}//for (TypeDeclaration t: types)
 				
 				//create calls edges
 				System.out.println("ClassName:::"+className);
 				visitFileAST(dpGraph, graphDb, cu, className);

     	
/*****************************************************************************/			
			//create uses dependency between class and class based on type of attribute node
			//obtain all class nodes from graph
			Node attributeNode;
			String attributeType;
			Iterable<Relationship> relations;
			Node ownerClassNode;
			Long attributeNodeClassId= (long) -1;
			Long otherNodeClassId= (long) -1;
			
			ResourceIterator<Node> aNodes= graphDb.findNodes(dGraphNodeType.ATTRIBUTE);
			while (aNodes.hasNext() )
			{
				attributeNode= aNodes.next();
				attributeType= (String)attributeNode.getProperty("dataType");
			//	System.out.println("dataType::"+attributeType);
				relations=attributeNode.getRelationships(RelTypes.CONNECTING);
				
				for (Relationship r: relations)
				{
					ownerClassNode=r.getOtherNode(attributeNode);
				//	System.out.println("classNode::"+ownerClassNode.getProperty("name"));
					attributeNodeClassId= ownerClassNode.getId();
				//	System.out.println("classNodeID::"+ownerClassNode.getId());
					//System.out.println("classNode::"+r.getOtherNode(attributeNode));
					otherNodeClassId= searchClassNode(attributeType, nodeHashMap);
					if (otherNodeClassId != -1)
						{
							//class node found
							//create dependency edge from current class node to class node
						//uses relationship		
						System.out.println("Creating edge from Class to class for attribute node");
							dpGraph.addDependencyEdge(graphDb, otherNodeClassId, attributeNodeClassId, "USES");
						}//if (idinterfaceNode != -1)
				}
			}
			
/*****************************************************************************/
			//create uses dependency between method and class based on type of attribute node
			//obtain all attribute nodes from graph
			Node attributeNode1;
			String attributeType1;		
			Node attributeMethodNode;
			Long attributeMethodNodeId= (long) -1;
			Long otherClassNodeId= (long) -1;
			
			ResourceIterator<Node> methodANodes= graphDb.findNodes(dMethodNodeType.VariableDeclarationNode);
			while (methodANodes.hasNext() )
			{
				attributeNode1= methodANodes.next();
				attributeType1= (String)attributeNode1.getProperty("dataType");
			//	System.out.println("dataType::"+attributeType);
				relations=attributeNode1.getRelationships(methodRelTypes.BODY);
				
				for (Relationship r: relations)
				{
					attributeMethodNode=r.getOtherNode(attributeNode1);
				//	System.out.println("classNode::"+ownerClassNode.getProperty("name"));
					attributeMethodNodeId= attributeMethodNode.getId();
				//	System.out.println("classNodeID::"+ownerClassNode.getId());
					//System.out.println("classNode::"+r.getOtherNode(attributeNode));
					otherClassNodeId= searchClassNode(attributeType1, nodeHashMap);
					if (otherClassNodeId != -1)
						{
							//class node found
							//create dependency edge from current method node to class node
						//uses relationship							
							dpGraph.addDependencyEdge(graphDb, otherClassNodeId, attributeMethodNodeId, "USES");
						}//if (idinterfaceNode != -1)
				}
			}
/*****************************************************************************/
    }//dependency Graph
	
	
	 public void visitFileAST(final dependencyGraphNodes dpGraph, final GraphDatabaseService graphDb, final CompilationUnit cu, final String className)
	 {
		 // visit each method invocation node
		 cu.accept(new ASTVisitor()
		 {			
			 String currentMethodName=null;
			 String invokedMethodName=null;	
			 String currentParentName=null;
		 			
	  			public boolean visit(ClassInstanceCreation node){
	  				
	  				String classInstanceCreation=node.getType().toString();
                   ITypeBinding typeBinding = node.resolveTypeBinding();
                   if (typeBinding != null) {
                  	 IType type = (IType)typeBinding.getJavaElement();

                //      System.out.println("Type Qualified Name: " + typeBinding.getQualifiedName());
                //      System.out.println("Type Binary Name: " + typeBinding.getBinaryName());
		 		//		System.out.println("Type Name: " + typeBinding.getName());
		 				
		 				//if (typeBinding.getBinaryName() !=null) classInstanceCreation= typeBinding.getBinaryName();
		 			//	else 
		 					if (typeBinding.getQualifiedName()!=null) classInstanceCreation= typeBinding.getQualifiedName();
		 			//	System.out.println("Type getDeclaringClass: " + typeBinding.getDeclaringClass());
                   }
                   
     /*              IMethodBinding binding = node.resolveConstructorBinding();
                   if (binding != null) {
                   	 ITypeBinding type = binding.getDeclaringClass();
                       if (type != null) {
                           System.out.println("Decl ClassInstanceCreation: " + type.getName());
                       }
                   } */
             //      System.out.println("ClassInstanceCreation: "+classInstanceCreation);
	  				MethodDeclaration parentMethodDeclarationNode= (MethodDeclaration) getParentMethodDeclarationNode(node);
	  				if (parentMethodDeclarationNode !=null)
	  					currentParentName = className+"."+parentMethodDeclarationNode.getName().toString();
	  				else
	  					currentParentName = className;
	  			//	System.out.println("currentParentName: "+currentParentName);
	  				//create node from currentParentname to node.getType() if exists
	  				
	  				Long invokeClassNodeId = searchNode(graphDb, dGraphNodeType.CLASS, "canonicalName", classInstanceCreation);
	  				Long currentNodeId = searchNode(graphDb, dGraphNodeType.METHOD, "canonicalName", currentParentName);
	  				
	  				if (currentNodeId == (long)-1) currentNodeId= searchNode(graphDb, dGraphNodeType.CLASS, "canonicalName", currentParentName);
	  				
	  				if ((invokeClassNodeId != (long) -1) && (currentNodeId != (long) -1))
	  				{
	  					System.out.println("Creating edge from ClassInstanceCreation");
	  					dpGraph.addDependencyEdge(graphDb, invokeClassNodeId, currentNodeId, "USES");
	  				}
	  				return true;
	  			}
	  			
	  			public boolean visit(MethodInvocation node){
	  			//	System.out.println("MethodInvocation Identifier: "+ node.getName().getIdentifier());
	  				Expression expression = node.getExpression();
                   if (expression != null) {
                       ITypeBinding typeBinding = expression.resolveTypeBinding();
                      
                       if (typeBinding != null) {
                       	 IType type = (IType)typeBinding.getJavaElement();

                  //         System.out.println("Type: " + typeBinding.getQualifiedName());
                           invokedMethodName = typeBinding.getQualifiedName()+"."+node.getName();
                       }
                   }
                   
              /*     IMethodBinding binding = node.resolveMethodBinding();
                   if (binding != null) {
                   	 ITypeBinding type = binding.getDeclaringClass();
                       if (type != null) {
                           System.out.println("Decl Method: " + type.getQualifiedName());
                           invokedMethodName = type.getQualifiedName()+"."+node.getName();
                       }
                   } */                               
            //       System.out.println("invokedMethodName: " +invokedMethodName);
	  				//get parent nodes till you reach the MethodDeclaration node
	  				MethodDeclaration parentMethodDeclarationNode= (MethodDeclaration) getParentMethodDeclarationNode(node);
	  				currentMethodName = className+"."+parentMethodDeclarationNode.getName().toString();
	  		//		System.out.println("currentMethodName: "+currentMethodName);
	  	
	  				//create calls edge from currentMethodName to invokedMethodName
	  				Long invokeMethodNodeId = searchNode(graphDb, dGraphNodeType.METHOD, "canonicalName", invokedMethodName);
	  				Long currentMethodNodeId = searchNode(graphDb, dGraphNodeType.METHOD, "canonicalName", currentMethodName);
	  				
	  				if ((invokeMethodNodeId != (long) -1) && (currentMethodNodeId != (long) -1))
	  				{
	  					System.out.println("Creating edge from MethodInvocation");
	  					dpGraph.addDependencyEdge(graphDb, invokeMethodNodeId, currentMethodNodeId, "CALLS");
	  				}
	  				return true;
	  			}
		 });
	 }
	 
	 public Long searchNode(final GraphDatabaseService graphDb, Label nodeType, String key, String nodeName )
	 {
		 //returns the id of the last node found
		 //assumes only one such node exists found
		 //modify to search on the canonical name
		Long nodeId = (long)-1;
		ResourceIterator<Node> nodes= graphDb.findNodes(nodeType, key, nodeName);
		while (nodes.hasNext() )
		{
			nodeId= nodes.next().getId();
		}
		return nodeId;					
	 }
	 
	 
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
	
	public String parse(String fileName)
	{
		String fName= null;
		int i = fileName.indexOf(".java");
		fName= fileName.substring(0, i);
		return fName;
	}
	
	 public ASTNode getParentMethodDeclarationNode(ASTNode node)
	 {
		 if (node instanceof  MethodDeclaration)
			 return (ASTNode)(node);
		 else 
			 { if (node != null)
				 return (getParentMethodDeclarationNode(node.getParent()));
			 else return null;
			 }
	
			 
	 }
}
