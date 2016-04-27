package org.apache.collab.server.Comparator;

import java.io.File;
import java.io.IOException;


import org.neo4j.logging.Log;
import org.neo4j.logging.LogProvider;
import org.neo4j.logging.NullLogProvider;
import org.apache.collab.server.dependencyGraphNodes;
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

public class CompareGraphs {

	InconsistencyCommunicator communicator=null;
	
	boolean DEBUG= true;
	 public  static enum dGraphNodeType implements Label {
	    	PROJECT, PACKAGE, CLASS, INTERFACE, METHOD, ATTRIBUTE;
	    }   
	    
	    public static enum RelTypes implements RelationshipType
	    {
	    	CONNECTING, DEPENDENCY;
	    }
	    
	    public static enum methodRelTypes implements RelationshipType
	    {
	    	BODY;
	    }
	 private static String DB_PATH_CLIENT = "neo4jDB/Client/";
	 private final String DB_PATH_SERVER = "neo4jDB/Server";
	 private dependencyGraphNodes dpGraph;
	 GraphDatabaseService graphDbServer;
	 GraphDatabaseService graphDbClient;
	 
	 Node rootNodeSever=null;
	 Node rootNodeClient =null;
	 
/*	  public static void main(String args[]) {

		  CompareGraphs db= new CompareGraphs();
		  DB_PATH_CLIENT = DB_PATH_CLIENT + "CollabClient";
	       	db.initializeDB("CollabClient");
				
		}*/
 
	    public void initializeDB(String collabName) {
			
	    	try {
	    		  DB_PATH_CLIENT = DB_PATH_CLIENT + "CollabClient";
	    		dpGraph = new dependencyGraphNodes();	    		
	    		File dbDirServer = new File(DB_PATH_SERVER);
	    		File dbDirClient = new File(DB_PATH_CLIENT);
	    		communicator = new InconsistencyCommunicator();
	    		
	    		//for server DB
	    		GraphDatabaseFactory graphFactoryServer = new GraphDatabaseFactory();
	    		GraphDatabaseBuilder graphBuilderServer = graphFactoryServer.newEmbeddedDatabaseBuilder(dbDirServer);
	    		 graphDbServer = graphBuilderServer.newGraphDatabase();  	    			    		 
	            registerShutdownHook( graphDbServer );

	        	//for client DB
	    		GraphDatabaseFactory graphFactoryClient = new GraphDatabaseFactory();
	    		GraphDatabaseBuilder graphBuilderClient = graphFactoryClient.newEmbeddedDatabaseBuilder(dbDirClient);
	    		 graphDbClient = graphBuilderClient.newGraphDatabase();                  
	            registerShutdownHook( graphDbClient );
	            
	      /*      ExecutionEngine engine = new ExecutionEngine(graphDbServer, new LogProvider() {
					
					@Override
					public Log getLog(String arg0) {
						// TODO Auto-generated method stub
						return null;
					}
					
					@Override
					public Log getLog(Class arg0) {
						// TODO Auto-generated method stub
						return null;
					}
				});

	            ExecutionResult result;
	            String nodeResult=null;
	            Transaction tx=null;
	            try 
	            {
	            	tx= graphDbServer.beginTx();
	                result = engine.execute( "match (n) return n" );

	             //   ExecutionResult execResult = execEngine.execute("MATCH (java:JAVA) RETURN java");
	                String results = result.dumpToString();
	                System.out.println(results);
	                
	               scala.collection.Iterable <Object> n_column = (Iterable<Object>) result.columnAs( "n" );
	                for ( Object node : IteratorUtil.asIterable( n_column ) )
	                {

	                    nodeResult = node + ": " + ((Node)node).getProperty( "name" );

	                    System.out.println(nodeResult);
	                }
	                tx.success();

	            }
	            catch (Exception e)
	            {
	            	e.printStackTrace();
	            }
	            finally {
	            	tx.close();
	            }*/
	            compareDB();
				
	            
				shutDown(graphDbServer);	
				shutDown(graphDbClient);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	System.out.println("Out of DB");
		}
	    
	    public void compareDB() throws Exception
	    {
	      Transaction txClient =null;
	      Transaction txServer =null;
	        try 
	        {
	        	 System.out.println("creating transaction object");
	        	 txClient= graphDbClient.beginTx();
	        	 txServer= graphDbServer.beginTx();

	        	 compareMainClassNode();
	        	compareDeletionOfNodes();
        	System.out.println("created graph");
            // START SNIPPET: transaction
        	txClient.success();
        	txServer.success();
        }
        catch (Exception e)
        {
        //	if ( txClient != null && txServer !=null)
        //    throw e;
        	e.printStackTrace();
        }
        finally {
        	
        	 if (txClient != null && txServer !=null) {
        		 txClient.close();
        		 txServer.close();
        		 System.out.println("Closing transaction object");        		 
             }        	            
        }         
      
	    }
	 
	    public void compareDeletionOfNodes()
				  {
			   Node clientClassNode= null;
			   Node clientNode=null;
			   Node serverClassNode= null;
			   boolean found=false;
			   //assumes find all class nodes.... only one such exist
			   ResourceIterator<Node> clientClassNodes= graphDbClient.findNodes(dGraphNodeType.CLASS);

				while (clientClassNodes.hasNext() )
				{
					clientNode= clientClassNodes.next();
								
					found = false;
					
					//get the corresponding class node at the server
					  String clientAtrributeName=null;
					  String serverAttributeName=null;
					  serverClassNode= graphDbServer.findNode(dGraphNodeType.CLASS, "name", clientNode.getProperty("name").toString());
					 Node otherNode;
					 String otherNodeType;
				
					 if (serverClassNode !=null)
					 {
						 //servernode found
						 Iterable<Relationship> relations= serverClassNode.getRelationships(RelTypes.CONNECTING);
							for (Relationship r: relations)
							{
								found=false;
								otherNode=r.getOtherNode(serverClassNode);
								otherNodeType= otherNode.getProperty("nodeType").toString();
								if (otherNodeType.equals("ATTRIBUTE"))
								{
									//check for existence of  attributes
								//	clientAtrributeName= attributeNode.getProperty("name").toString();
									serverAttributeName= otherNode.getProperty("name").toString();
									Node clientAttributeNode=null;
									String clientAttributeNodeType=null;
									 Iterable<Relationship> relationClient= clientNode.getRelationships(RelTypes.CONNECTING);
										for (Relationship rClient: relationClient)
										{
											//check each attribute of server for existence in client
											clientAttributeNode=rClient.getOtherNode(clientNode);
											clientAttributeNodeType= clientAttributeNode.getProperty("nodeType").toString();
											if (clientAttributeNodeType.equals("ATTRIBUTE"))
											{
												if (serverAttributeName.equals(clientAttributeNode.getProperty("name").toString()))
												{
													found = true;
													break;
												}
											}
										}
										
										if (!found)
										{
											//attribute does not exist
											communicator.informDeletionOfClassAttributeCase2(clientNode, otherNode, graphDbServer);
										}
								}
							}
					 }
					  
				}
		    }
		 
	    public void compareDeletionOfMethods()
		  {
	   Node clientClassNode= null;
	   Node clientNode=null;
	   Node serverClassNode= null;
	   boolean found=false;
	   //assumes find all class nodes.... only one such exist
	   ResourceIterator<Node> clientClassNodes= graphDbClient.findNodes(dGraphNodeType.CLASS);

		while (clientClassNodes.hasNext() )
		{
			clientNode= clientClassNodes.next();
						
			found = false;
			
			//get the corresponding class node at the server
			  String serverMethodName=null;
			  serverClassNode= graphDbServer.findNode(dGraphNodeType.CLASS, "name", clientNode.getProperty("name").toString());
			 Node otherNode;
			 String otherNodeType;
		
			 if (serverClassNode !=null)
			 {
				 //servernode found
				 Iterable<Relationship> relations= serverClassNode.getRelationships(RelTypes.CONNECTING);
					for (Relationship r: relations)
					{
						found=false;
						otherNode=r.getOtherNode(serverClassNode);
						otherNodeType= otherNode.getProperty("nodeType").toString();
						if (otherNodeType.equals("METHOD"))
						{
							//check for existence of  attributes
						//	clientAtrributeName= attributeNode.getProperty("name").toString();
							serverMethodName= otherNode.getProperty("name").toString();
							Node clientMethodNode=null;
							String clientMethodNodeType=null;
							 Iterable<Relationship> relationClient= clientNode.getRelationships(RelTypes.CONNECTING);
								for (Relationship rClient: relationClient)
								{
									//check each attribute of server for existence in client
									clientMethodNode=r.getOtherNode(clientNode);
									clientMethodNodeType= clientMethodNode.getProperty("nodeType").toString();
									if (clientMethodNodeType.equals("METHOD"))
									{
										if (serverMethodName.equals(clientMethodNode.getProperty("name").toString()))
										{
											found = true;
											//method exists
											//check for existence of all attributes of the method
											compareDeletionOfMethodAttributes(clientMethodNode,otherNode);
											break;
										}
									}
								}
								
								if (!found)
								{
									//attribute does not exist
									//invokeDeletionOfClassAttribute();
									communicator.informDeletionOfClassMethodCase2(clientNode, otherNode, graphDbServer);
								}
						}
					}
			 }
			  
		}
  }
	    
	    
	    public void compareDeletionOfMethodAttributes(Node clientMethodNode,Node serverMethodNode)
	    {
	    	//servernode found
	    	boolean found=false;
	    	Node otherNode=null;
	    	String otherNodeType=null;
	    	String serverMethodAttributeName=null;
			 Iterable<Relationship> relations= serverMethodNode.getRelationships(methodRelTypes.BODY);
				for (Relationship r: relations)
				{
					found=false;
					otherNode=r.getOtherNode(serverMethodNode);
					otherNodeType= otherNode.getProperty("nodeType").toString();
					if (otherNodeType.equals("METHOD-ATTRIBUTE"))
					{
						//check for existence of  attributes
					
						serverMethodAttributeName= otherNode.getProperty("name").toString();
						Node clientMethodAttributeNode=null;
						String clientMethodAttributeNodeType=null;
						 Iterable<Relationship> relationClient= clientMethodNode.getRelationships(RelTypes.CONNECTING);
							for (Relationship rClient: relationClient)
							{
								//check each attribute of server for existence in client
								clientMethodAttributeNode=r.getOtherNode(clientMethodNode);
								clientMethodAttributeNodeType= clientMethodNode.getProperty("nodeType").toString();
								if (clientMethodAttributeNodeType.equals("METHOD-ATTRIBUTE"))
								{
									if (serverMethodAttributeName.equals(clientMethodAttributeNode.getProperty("name").toString()))
									{
										found = true;
										break;
									}
								}
							}
							
							if (!found)
							{
								//method attribute does not exist
								communicator.informDeletionOfMethodAttributeCase2(clientMethodAttributeNode, otherNode, graphDbServer);
							}
					}
				}
	    	
	    }
	    
	    
	   public void compareMainClassNode()
	    {
		   Node clientNode= null;
		   Node serverNode= null;
		   boolean found=false;
		   //assumes find all class nodes.... only one such exist
		   ResourceIterator<Node> clientClassNodes= graphDbClient.findNodes(dGraphNodeType.CLASS);

			while (clientClassNodes.hasNext() )
			{
				clientNode= clientClassNodes.next();
							
				found = false;
				System.out.println("clientNode:: "+clientNode.getProperty("name"));
			   //assumes find all class nodes.... only one such exist
			   ResourceIterator<Node> serverClassNodes= graphDbServer.findNodes(dGraphNodeType.CLASS);
			  
				while (serverClassNodes.hasNext())
				{
					serverNode= serverClassNodes.next();
					System.out.println("serverNode:: "+serverNode.getProperty("name"));
					if (clientNode.getProperty("name").toString().equals(serverNode.getProperty("name").toString()))
					{
						//class exists
						System.out.println("Class Node exists");
						checkConnectingNodesExist(clientNode); // class exits
						invokeCheckClassProperties(clientNode, serverNode);
						found = true;
						break;
					}										
				}
			}
			
			if (!found)
			{
				//class does not exist
				invokeCase1(clientNode, graphDbServer);//addition of a new class
			}
		/*	else
			{
				checkConnectingNodesExist(clientNode); // class exits
				invokeCheckClassProperties(clientNode, serverNode);

			}*/
	    }
	   
	  public void invokeCheckClassProperties(Node clientNode, Node serverNode)
	   {
		  System.out.println(" invokeCheckClassProperties-- still to be implemented");
		  //modifier, imports and packageName, extends, implements (may be null)
		  
		  if (!(clientNode.getProperty("modifier").toString().equals(serverNode.getProperty("modifier").toString())))
		  {
			  //different modifier
			  System.out.println("Send to Client::"+ clientNode.getProperty("name")+"has a different modifier");
			  //sendMessage(clientAttributeNode, msg, caseNo);
		  }
		  
		  if (!(clientNode.getProperty("imports").toString().equals(serverNode.getProperty("imports").toString())))
		  {
			  //different modifier
			  System.out.println("Send to Client::"+ clientNode.getProperty("name")+"has a different imports");
			  //sendMessage(clientAttributeNode, msg, caseNo);
		  }
		  
		  if (!(clientNode.getProperty("packageName").toString().equals(serverNode.getProperty("packageName").toString())))
		  {
			  //different modifier
			  System.out.println("Send to Client::"+ clientNode.getProperty("name")+"has a different packageName");
			  //sendMessage(clientAttributeNode, msg, caseNo);
		  }
		  
		  if (!(clientNode.getProperty("extends").toString().equals(serverNode.getProperty("extends").toString())))
		  {
			  //different modifier
			  System.out.println("Send to Client::"+ clientNode.getProperty("name")+"has a different extends");
			  //sendMessage(clientAttributeNode, msg, caseNo);
		  }
		  
		  if (!(clientNode.getProperty("implements").toString().equals(serverNode.getProperty("implements").toString())))
		  {
			  //different modifier
			  System.out.println("Send to Client::"+ clientNode.getProperty("name")+"has a different implements");
			  //sendMessage(clientAttributeNode, msg, caseNo);
		  }
		  
	   }
	  
	  public void invokeCase1(Node clientNode, GraphDatabaseService graphDbServer)
	   {
		  System.out.println("Invoking case1:: Addition of classNode");
		  //new node has been added 
		  communicator.informAdditionClassNodeCase1(clientNode, graphDbServer);
	   }
	  
	  public void checkConnectingNodesExist(Node clientNode)
	  {
		  
		  if (DEBUG) System.out.println("In checkConnectingNodesExist");
		  Node otherNode;
		  String otherNodeType;
		Iterable<Relationship> relations;
		  relations= clientNode.getRelationships(RelTypes.CONNECTING);
			for (Relationship r: relations)
			{
				otherNode=r.getOtherNode(clientNode);
				otherNodeType= otherNode.getProperty("nodeType").toString();
				if (otherNodeType.equals("ATTRIBUTE"))
				{
					  if (DEBUG) System.out.println("Going to checkAttributesExist");
					//check for existence of all attributes
					checkAttributesExist(otherNode, clientNode);				
				}
				else if (otherNodeType.equals("METHOD"))
				{
					  if (DEBUG) System.out.println("Going to checkMethodsExist");
					//check for existence of all methods
					checkMethodsExist(otherNode, clientNode);
				}
					
			}
	  }
	  
	  public void checkAttributesExist(Node attributeNode, Node clientNode)
	  {
		
		  if (DEBUG) System.out.println("In checkAttributesExist");
		  //get the corresponding class node at the server
		  String clientAtrributeName=null;
		String serverAttributeName=null;
		 Node serverClassNode= graphDbServer.findNode(dGraphNodeType.CLASS, "name", clientNode.getProperty("name").toString());
		 Node otherNode;
		 String otherNodeType;
		 boolean found = false;
		 if (serverClassNode !=null)
		 {
			 //servernode found
			 Iterable<Relationship> relations= serverClassNode.getRelationships(RelTypes.CONNECTING);
				for (Relationship r: relations)
				{
					otherNode=r.getOtherNode(serverClassNode);
					otherNodeType= otherNode.getProperty("nodeType").toString();
					if (otherNodeType.equals("ATTRIBUTE"))
					{
						//check for existence of  attributes
						clientAtrributeName= attributeNode.getProperty("name").toString();
						serverAttributeName= otherNode.getProperty("name").toString();
						if 	(clientAtrributeName.equals(serverAttributeName))	
								{
									//atribute found
									//check for other propoerties of the attribute
									invokeCheckAttributeProperties(attributeNode,otherNode );
									found = true;
									break;
								}
					}
				}
				
				if (!found)
				{
					//attribute does not exist
					//addition of attribute node in client
					invokeDependencyEdgeCreation(attributeNode);
					communicator.informAdditionAttributeNodeCase1(attributeNode, serverClassNode, graphDbServer);
					
				}
		 }
		  
	  }
	  
	  public void invokeCheckAttributeProperties(Node clientAttributeNode, Node serverAttributeNode )
	  {		  
		  //modifier, datatype and initializer (may be null)
		  
		  if (!(clientAttributeNode.getProperty("modifier").toString().equals(serverAttributeNode.getProperty("modifier").toString())))
		  {
			  //different modifier
			  System.out.println("Send to Client::"+ clientAttributeNode.getProperty("name")+"has s different modifier");
			  //sendMessage(clientAttributeNode, msg, caseNo);
		  }
		  
		  if (!(clientAttributeNode.getProperty("dataType").toString().equals(serverAttributeNode.getProperty("dataType").toString())))
		  {
			  //different modifier
			  System.out.println("Send to Client::"+ clientAttributeNode.getProperty("name")+"has s different datatype");
			  //sendMessage(clientAttributeNode, msg, caseNo);
			  //this also leads to creation of a dependency edge
			  invokeDependencyEdgeCreation(clientAttributeNode);
		  }
		  
		  if (!(clientAttributeNode.getProperty("initializer").toString().equals(serverAttributeNode.getProperty("initializer").toString())))
		  {
			  //different modifier
			  System.out.println("Send to Client::"+ clientAttributeNode.getProperty("name")+"has s different initializer");
			  //sendMessage(clientAttributeNode, msg, caseNo);
		  }
	  }
	  
	  
	  public void invokeDependencyEdgeCreation(Node clientAttributeNode)
	  {
		  String dataType= clientAttributeNode.getProperty("dataType").toString();
		  //search this datatype class in server, if exists... 
		  Node serverClassNode = searchDatatypeClassInServerGraph(dataType);
		  
		  //invoke creation of dependency edge
		 if (serverClassNode !=null) communicator.informAdditionOfDependencyEdge(clientAttributeNode, serverClassNode, graphDbServer);
	  }
	  
	  public Node searchDatatypeClassInServerGraph(String dataType)
	  {
		  
		 Node serverClassNode= graphDbServer.findNode(dGraphNodeType.CLASS, "name", dataType);

		 if (serverClassNode != null)
		 {
			return serverClassNode;
		 }
		 else return null;

	  }
	  
	  public void checkMethodsExist(Node methodNode, Node clientNode)
	  {
		  //get the corresponding class node at the server
		  String clientMethodName=null;
		String serverMethodName=null;
		 Node serverClassNode= graphDbServer.findNode(dGraphNodeType.CLASS, "name", clientNode.getProperty("name").toString());
		 Node otherNode;
		 String otherNodeType;
		 boolean found = false;
		 if (serverClassNode !=null)
		 {
			 //servernode found
			 Iterable<Relationship> relations= serverClassNode.getRelationships(RelTypes.CONNECTING);
				for (Relationship r: relations)
				{
					otherNode=r.getOtherNode(serverClassNode);
					otherNodeType= otherNode.getProperty("nodeType").toString();
					if (otherNodeType.equals("METHOD"))
					{
						//check for existence of  attributes
						clientMethodName= methodNode.getProperty("name").toString();
						serverMethodName= otherNode.getProperty("name").toString();
						if 	(clientMethodName.equals(serverMethodName))	
								{
									//atribute found
									//check for other propoerties of the attribute
									invokeCheckMethodProperties(methodNode,otherNode );
									found = true;
									break;
								}
					}
				}
				
				if (!found)
				{
					//method does not exist
					//addition of attribute node in client
					 communicator.informAdditionMethodNodeCase1(clientNode, serverClassNode, graphDbServer);
				}
		 }
	  }
	  
	  
	  public void invokeCheckMethodProperties(Node clientMethodNode, Node serverMethodNode )
	  {		  
	    	//modifier, returnType and parameterList, body (may be null)
		  
		  if (!(clientMethodNode.getProperty("modifier").toString().equals(serverMethodNode.getProperty("modifier").toString())))
		  {
			  //different modifier
			  System.out.println("Send to Client::"+ clientMethodNode.getProperty("name")+"has s different modifier");
			  //sendMessage(clientAttributeNode, msg, caseNo);
		  }
		  
		  if (!(clientMethodNode.getProperty("returnType").toString().equals(serverMethodNode.getProperty("returnType").toString())))
		  {
			  //different modifier
			  System.out.println("Send to Client::"+ clientMethodNode.getProperty("name")+"has s different returnType");
			  //sendMessage(clientAttributeNode, msg, caseNo);
		  }
		  
		  if (!(clientMethodNode.getProperty("parameterList").toString().equals(serverMethodNode.getProperty("parameterList").toString())))
		  {
			  //different modifier
			  System.out.println("Send to Client::"+ clientMethodNode.getProperty("name")+"has s different parameterList");
			  //sendMessage(clientAttributeNode, msg, caseNo);
		  }
		  
		  //check for body
		  /*important*/
		  
		  //check for attributes inside methods
		  checkVariableDeclarationNodes(clientMethodNode, serverMethodNode);
	  }
	  
	  public void checkVariableDeclarationNodes(Node clientMethodNode, Node serverMethodNode)
	  {
		  Node otherClientNode;
		  Node otherServerNode;
		  boolean found=false;
		  //also check all properties of these methods
		  Iterable<Relationship> relationClient= clientMethodNode.getRelationships(methodRelTypes.BODY);
		  for (Relationship r: relationClient)
			{
			  found=false;
			  otherClientNode=r.getOtherNode(clientMethodNode);
			  //compare this node with every such other node in the server
			  //if found checkProperties
			  //else addition of a new attribute
			  Iterable<Relationship> relationServer= serverMethodNode.getRelationships(methodRelTypes.BODY);
			  for (Relationship rServer: relationClient)
				{
				  otherServerNode=r.getOtherNode(serverMethodNode);
				  if (otherClientNode.getProperty("name").toString().equals(otherServerNode.getProperty("name").toString()))
				  {
					  //found
					  found=true;
					  invokeCheckMethodAttributeProperties(otherClientNode, otherServerNode);
					  break;
				  }
				}
			  
				if (!found)
				{
					//method does not exist
					//addition of attribute node in client
					 communicator.informAdditionMethodAttributeNodeCase1(clientMethodNode, serverMethodNode, graphDbServer);
				}
			}
	  }
	  
	  
	  public void invokeCheckMethodAttributeProperties(Node clientMethodAttributeNode, Node serverMethodAttributeNode )
	  {		  
	    	//modifier, returnType and parameterList, body (may be null)
		  
		  if (!(clientMethodAttributeNode.getProperty("modifier").toString().equals(serverMethodAttributeNode.getProperty("modifier").toString())))
		  {
			  //different modifier
			  System.out.println("Send to Client::"+ clientMethodAttributeNode.getProperty("name")+"has s different modifier");
			  //sendMessage(clientAttributeNode, msg, caseNo);
		  }
		  
		  if (!(clientMethodAttributeNode.getProperty("dataType").toString().equals(serverMethodAttributeNode.getProperty("dataType").toString())))
		  {
			  //different modifier
			  System.out.println("Send to Client::"+ clientMethodAttributeNode.getProperty("name")+"has s different dataType");
			  //sendMessage(clientAttributeNode, msg, caseNo);
			  invokeDependencyEdgeCreation(clientMethodAttributeNode);
		  }
		  
		  if (!(clientMethodAttributeNode.getProperty("initializer").toString().equals(serverMethodAttributeNode.getProperty("initializer").toString())))
		  {
			  //different modifier
			  System.out.println("Send to Client::"+ clientMethodAttributeNode.getProperty("name")+"has s different initializer");
			  //sendMessage(clientAttributeNode, msg, caseNo);
		  }

	  }
	  
	  
	    void shutDown(GraphDatabaseService graphDb)
	    {
	        System.out.println( "Shutting down database ..." );
	        graphDb.shutdown();
	        System.out.println( "DB server shuting down complete" );
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
	    
}
