package org.apache.collab.server.Comparator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;



import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.neo4j.logging.Log;
import org.neo4j.logging.LogProvider;
import org.neo4j.logging.NullLogProvider;
import org.apache.collab.server.dependencyGraphNodes;
import org.neo4j.graphdb.Direction;
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
	String collabName=null;
	String ipAddSQL=null;
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
	 private String DB_PATH_CLIENT = "neo4jDB/Client/";
	// private static final String DB_PATH_SERVER = "neo4jDB/Server";
	 private dependencyGraphNodes dpGraph;
	 GraphDatabaseService graphDbServer;
	 GraphDatabaseService graphDbClient;

	 HashMap<Long, String> nodeCanonicalHashMap=null;
	 
	 Node rootNodeSever=null;
	 Node rootNodeClient =null;
	 
/*	  public static void main(String args[]) {

		  CompareGraphs db= new CompareGraphs();
		  DB_PATH_CLIENT = DB_PATH_CLIENT + "CollabClient";
	       	db.initializeDB("CollabClient");
				
		}*/
 
	    public void initializeDB(String cName, String ipSQL,  GraphDatabaseService graphServer) {

	    	 graphDbServer =  graphServer;
	    	nodeCanonicalHashMap= new HashMap<Long, String>();
	    	readFromFileHashMap(nodeCanonicalHashMap);
	    
	    	collabName =cName;
	    	ipAddSQL = ipSQL;
	    	try {
	    		  DB_PATH_CLIENT = DB_PATH_CLIENT + collabName;
	    		dpGraph = new dependencyGraphNodes();	    		
	    	//	File dbDirServer = new File(DB_PATH_SERVER);
	    		File dbDirClient = new File(DB_PATH_CLIENT);
	    		
	    		
	    		//for server DB
	    	//	GraphDatabaseFactory graphFactoryServer = new GraphDatabaseFactory();
	    	//	GraphDatabaseBuilder graphBuilderServer = graphFactoryServer.newEmbeddedDatabaseBuilder(dbDirServer);
	    	//	 graphDbServer = graphBuilderServer.newGraphDatabase();  	    			    		 
	        //    registerShutdownHook( graphDbServer );

	            communicator = new InconsistencyCommunicator(collabName, ipAddSQL, graphDbServer, nodeCanonicalHashMap);
	        	//for client DB
	    		GraphDatabaseFactory graphFactoryClient = new GraphDatabaseFactory();
	    		GraphDatabaseBuilder graphBuilderClient = graphFactoryClient.newEmbeddedDatabaseBuilder(dbDirClient);
	    		 graphDbClient = graphBuilderClient.newGraphDatabase();                  
	            registerShutdownHook( graphDbClient );
	            
	            compareDB();
				
	            
			//	shutDown(graphDbServer);	
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
	        	 compareMainInterfaceNode();
	        	compareDeletionOfNodes();
	        	compareDeletionOfMethods();
        	System.out.println("created graph");
            // START SNIPPET: transaction
        	txClient.success();
        	txServer.success();
        }
        catch (Exception e)
        {
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
			//   Node serverClassNode= null;
			   boolean found=false;
			   //assumes find all class nodes.... only one such exist
			   ResourceIterator<Node> clientClassNodes= graphDbClient.findNodes(dGraphNodeType.CLASS);

				while (clientClassNodes.hasNext() )
				{
					clientNode= clientClassNodes.next();
							
					 long serverNodeID= getCanonicalClassNodeID(clientNode.getProperty("canonicalName").toString());
					 System.out.println("serverNodeID::"+serverNodeID);
					if (serverNodeID == -1)
					{
						//node does not exist
						 System.out.println("Node does not exist");
					}
					else
					{
					 Node serverClassNode= graphDbServer.getNodeById(serverNodeID);
					found = false;
					
					//get the corresponding class node at the server
					  String clientAtrributeName=null;
					  String serverAttributeName=null;
					//  serverClassNode= graphDbServer.findNode(dGraphNodeType.CLASS, "name", clientNode.getProperty("name").toString());
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
											//add line no of deleted attribute
											String lineNumber= otherNode.getProperty("lineNumber").toString();
											communicator.informDeletionOfClassAttributeCase2(clientNode, otherNode, lineNumber);
										}
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
	 //  Node serverClassNode= null;
	   boolean found=false;
	   //assumes find all class nodes.... only one such exist
	   ResourceIterator<Node> clientClassNodes= graphDbClient.findNodes(dGraphNodeType.CLASS);

		while (clientClassNodes.hasNext() )
		{
			clientNode= clientClassNodes.next();
						
			found = false;
			
			 long serverNodeID= getCanonicalClassNodeID(clientNode.getProperty("canonicalName").toString());
			 System.out.println("serverNodeID::"+serverNodeID);
			 Node serverClassNode= null;
			 if (serverNodeID != -1)
			 {
			 serverClassNode= graphDbServer.getNodeById(serverNodeID);
			 }
			//get the corresponding class node at the server
			  String serverMethodName=null;
			//  serverClassNode= graphDbServer.findNode(dGraphNodeType.CLASS, "name", clientNode.getProperty("name").toString());
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
									clientMethodNode=rClient.getOtherNode(clientNode);
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
									String lineNumber= otherNode.getProperty("lineNumber").toString();
									communicator.informDeletionOfClassMethodCase2(clientNode, otherNode, lineNumber);
								}
						}
					}
			 }
			  
		}
  }
	    
	    
	    public void compareDeletionOfMethodAttributes(Node clientMethodNode,Node serverMethodNode)
	    {
	    	if (DEBUG) System.out.println("In compareDeletionOfMethodAttributes");
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
						 Iterable<Relationship> relationClient= clientMethodNode.getRelationships(methodRelTypes.BODY);
							for (Relationship rClient: relationClient)
							{
								//check each attribute of server for existence in client															
								clientMethodAttributeNode=rClient.getOtherNode(clientMethodNode);
								clientMethodAttributeNodeType= clientMethodAttributeNode.getProperty("nodeType").toString();
							//	if (DEBUG) System.out.println("clientMethodAttributeNode: "+ clientMethodAttributeNode.getProperty("name").toString());
							//	if (DEBUG) System.out.println("serverMethodAttributeName: "+ serverMethodAttributeName);
							//	if (DEBUG) System.out.println("clientMethodAttributeNodeType: "+ clientMethodAttributeNodeType);
								
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
								String lineNumber= otherNode.getProperty("lineNumber").toString();
								communicator.informDeletionOfMethodAttributeCase2(otherNode, clientMethodNode, serverMethodNode, lineNumber);
								found=false;
							}
					}
				}
	    	
	    }
	    
	    
	    public void compareMainInterfaceNode()
	    {
	    //to be implemented
	    }
	    
	   public void compareMainClassNode()
	    {
		   Node clientNode= null;
		   Node serverNode= null;
		   boolean found=false;
		   
		   ResourceIterator<Node> clientClassNodes= graphDbClient.findNodes(dGraphNodeType.CLASS);
		 //assumes find all class nodes.... only one such exist
			while (clientClassNodes.hasNext() )
			{
				clientNode= clientClassNodes.next();
			   //get class node id from HashMap
			   long serverNodeID= getCanonicalClassNodeID(clientNode.getProperty("canonicalName").toString());
			   System.out.println("serverNodeID::"+serverNodeID);
			   
			   if (serverNodeID== -1)
			   {
				   //inform addition of node
				 //class does not exist on server
				   //message new class
				   invokeCase1(clientNode, graphDbServer);//addition of a new class
			   }
			   
			   else
			   {
				   Node classNodeFoundOnServer= graphDbServer.getNodeById(serverNodeID);
				   if (classNodeFoundOnServer == null)
				   {
					   //class does not exist on server
					   //message new class
					   invokeCase1(clientNode, graphDbServer);//addition of a new class
				   }
				   else
				   {
					   System.out.println("Class Node exists");
						checkConnectingNodesExist(clientNode, classNodeFoundOnServer); // class exits
						invokeCheckClassProperties(clientNode, classNodeFoundOnServer);
				   }
			   }
			}
	    }
	
	   long getCanonicalClassNodeID(String className)
	   {
			System.out.println("className::"+className);
		   //get id of class name in nodeHashMap else return -1
			Long id = (long) -1;

				    for (Map.Entry<Long, String> entry : nodeCanonicalHashMap.entrySet()) {
				        if (Objects.equals(className, entry.getValue())) {
				            id= entry.getKey();
				            break;
				        }
				    }
					return id;
	   }
	/*	   ResourceIterator<Node> clientClassNodes= graphDbClient.findNodes(dGraphNodeType.CLASS);

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
	 //   }
	   
	  public void invokeCheckClassProperties(Node clientNode, Node serverNode)
	   {
		 // System.out.println(" invokeCheckClassProperties-- still to be implemented");
		  //modifier, imports and packageName, extends, implements (may be null)
		  String lineNumber= clientNode.getProperty("lineNumber").toString();
		  if (!(clientNode.getProperty("modifier").toString().equals(serverNode.getProperty("modifier").toString())))
		  {
			  //different modifier
			  //System.out.println("Send to Client::"+ clientNode.getProperty("name")+"has a different modifier");
			  communicator.informPropertyChangeClassNodeCase3(clientNode, serverNode, "modifier", lineNumber);			  
		  }
		  
		  if (!(clientNode.getProperty("imports").toString().equals(serverNode.getProperty("imports").toString())))
		  {
			  //different imports
			 // System.out.println("Send to Client::"+ clientNode.getProperty("name")+"has a different imports");
			  //invoke change in dependency edges for imports
			  communicator.informPropertyChangeClassNodeCase3(clientNode, serverNode, "imports", lineNumber);
		  }
		  
		  if (!(clientNode.getProperty("packageName").toString().equals(serverNode.getProperty("packageName").toString())))
		  {
			  //different modifier
			  //System.out.println("Send to Client::"+ clientNode.getProperty("name")+"has a different packageName");
			  communicator.informPropertyChangeClassNodeCase3(clientNode, serverNode, "packageName", lineNumber);
		  }
		  
		  if (!(clientNode.getProperty("extends").toString().equals(serverNode.getProperty("extends").toString())))
		  {
			  //different modifier
			//  System.out.println("Send to Client::"+ clientNode.getProperty("name")+"has a different extends");
			  communicator.informPropertyChangeClassNodeCase3(clientNode, serverNode, "extends", lineNumber);
		  }
		  
		  if (!(clientNode.getProperty("implements").toString().equals(serverNode.getProperty("implements").toString())))
		  {
			  //different modifier
			  //System.out.println("Send to Client::"+ clientNode.getProperty("name")+"has a different implements");
			  communicator.informPropertyChangeClassNodeCase3(clientNode, serverNode, "implements", lineNumber);
		  }
		  
	   }
	  
	  public void invokeCase1(Node clientNode, GraphDatabaseService graphDbServer)
	   {
		  System.out.println("Invoking case1:: Addition of classNode");
		  //new node has been added 
		  communicator.informAdditionClassNodeCase1(clientNode);
		  
		  //check for addition of dependency edges here
		  String extendsProperty= clientNode.getProperty("extends").toString();
		  if (!extendsProperty.equalsIgnoreCase("null"))
		  {
			  //extends not null
			  //check it extends a class existing in the server
			  //then means addition/modification of a dependency edge
			  System.out.println("extendsProperty:: "+extendsProperty);
			  
			  //search class node in server
			  long serverNodeID= getCanonicalClassNodeID(extendsProperty);
			  if (serverNodeID==-1)
			  {
				  //no such node exists on the server
			  }
			  else
			  {
				  //inform communicator
				  communicator.informAdditionOfExtendsDependencyEdge(clientNode, serverNodeID, "EXTENDS");
				  
			  }
			  
		  }
	   }
	  
	  public void checkConnectingNodesExist(Node clientNode, Node serverClassNode)
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
					checkAttributesExist(otherNode, clientNode, serverClassNode);				
				}
				else if (otherNodeType.equals("METHOD"))
				{
					  if (DEBUG) System.out.println("Going to checkMethodsExist");
					//check for existence of all methods
					checkMethodsExist(otherNode, clientNode, serverClassNode);
				}
					
			}
	  }
	  
	  public void checkAttributesExist(Node attributeNode, Node clientNode, Node serverClassNode)
	  {
		
		  if (DEBUG) System.out.println("In checkAttributesExist");
		  //get the corresponding class node at the server
		  String clientAtrributeName=null;
		String serverAttributeName=null;
		// Node serverClassNode= graphDbServer.findNode(dGraphNodeType.CLASS, "name", clientNode.getProperty("name").toString());
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
					invokeAttributeConnectingEdgeCreation(attributeNode);
					 String lineNumber= attributeNode.getProperty("lineNumber").toString();
					communicator.informAdditionAttributeNodeCase1(attributeNode, serverClassNode, lineNumber);
					
				}
		 }
		  
	  }
	  
	  public void invokeCheckAttributeProperties(Node clientAttributeNode, Node serverAttributeNode )
	  {		  
		  //modifier, datatype and initializer (may be null)
		  String lineNumber= clientAttributeNode.getProperty("lineNumber").toString();
		  
		  if (!(clientAttributeNode.getProperty("modifier").toString().equals(serverAttributeNode.getProperty("modifier").toString())))
		  {
			  //different modifier
			 // System.out.println("Send to Client::"+ clientAttributeNode.getProperty("name")+"has s different modifier");
			  communicator.informPropertyChangeAttributeNodeCase3(clientAttributeNode, serverAttributeNode, "modifier", lineNumber);
		  }
		  
		  if (!(clientAttributeNode.getProperty("dataType").toString().equals(serverAttributeNode.getProperty("dataType").toString())))
		  {
			  //different modifier
			  //System.out.println("Send to Client::"+ clientAttributeNode.getProperty("name")+"has s different datatype");
			  communicator.informPropertyChangeAttributeNodeCase3(clientAttributeNode, serverAttributeNode, "dataType", lineNumber);
			  //this also leads to creation of a dependency edge
			  invokeAttributeConnectingEdgeCreation(clientAttributeNode);
		  }
		  
		  if (!(clientAttributeNode.getProperty("initializer").toString().equals(serverAttributeNode.getProperty("initializer").toString())))
		  {
			  //different modifier
			  //System.out.println("Send to Client::"+ clientAttributeNode.getProperty("name")+"has s different initializer");
			  communicator.informPropertyChangeAttributeNodeCase3(clientAttributeNode, serverAttributeNode, "initializer", lineNumber);
		  }
	  }
	  
	  
	  public void invokeAttributeConnectingEdgeCreation(Node clientAttributeNode)
	  {
		  String dataType= clientAttributeNode.getProperty("dataType").toString();
		  //search this datatype class in server, if exists... 
		  Node serverClassNode = searchDatatypeClassInServerGraph(dataType);
		  
		  //invoke creation of connecting edge
		 if (serverClassNode !=null) communicator.informAdditionOfAttributeConnectingEdge(clientAttributeNode, serverClassNode);
	  }
	  
	  public Node searchDatatypeClassInServerGraph(String dataType)
	  {
		  
		/* Node serverClassNode= graphDbServer.findNode(dGraphNodeType.CLASS, "name", dataType);

		 if (serverClassNode != null)
		 {
			return serverClassNode;
		 }
		 else return null;*/
		 Node serverNode=null;
		  ResourceIterator<Node>  serverClassNodes= graphDbServer.findNodes(dGraphNodeType.CLASS);

		 while (serverClassNodes.hasNext())
			{
				serverNode= serverClassNodes.next();
				System.out.println("serverNode:: "+serverNode.getProperty("name"));
				
				 if (serverNode.getProperty("name").toString().equals(dataType))
				 {
					return serverNode;
				 }				 
			}
		return serverNode;		
	  }
	  
	  public void checkMethodsExist(Node methodNode, Node clientNode, Node serverClassNode)
	  {
		  //get the corresponding class node at the server
		  String lineNumber= methodNode.getProperty("lineNumber").toString();
		  
		  if (DEBUG) System.out.println("In checkMethodsExist");
		  String clientMethodName=null;
		String serverMethodName=null;
		// Node serverClassNode= graphDbServer.findNode(dGraphNodeType.CLASS, "name", clientNode.getProperty("name").toString());
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
							  if (DEBUG) System.out.println("Method exists:: "+clientMethodName);
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
					//invokeMethodConnectingEdgeCreation(methodNode);
					  if (DEBUG) System.out.println("Method does not exists:: "+methodNode);
					 communicator.informAdditionMethodNodeCase1(methodNode, clientNode, serverClassNode, lineNumber);
				}
		 }
	  }
	  
	  
	  public void invokeCheckMethodProperties(Node clientMethodNode, Node serverMethodNode )
	  {		  
	    	//modifier, returnType and parameterList, body (may be null)
		  String lineNumber= serverMethodNode.getProperty("lineNumber").toString();
		  
		  if (!(clientMethodNode.getProperty("modifier").toString().equals(serverMethodNode.getProperty("modifier").toString())))
		  {
			  //different modifier
			  System.out.println("Send to Client::"+ clientMethodNode.getProperty("name")+"has a different modifier");
			  communicator.informPropertyChangeMethodNodeCase3(clientMethodNode, serverMethodNode, "modifier", lineNumber);
		  }
		  
		  if (!(clientMethodNode.getProperty("returnType").toString().equals(serverMethodNode.getProperty("returnType").toString())))
		  {
			  //different modifier
			  System.out.println("Send to Client::"+ clientMethodNode.getProperty("name")+"has a different returnType");
			  communicator.informPropertyChangeMethodNodeCase3(clientMethodNode, serverMethodNode, "returnType", lineNumber);
		  }
		  
		  if (!(clientMethodNode.getProperty("parameterList").toString().equals(serverMethodNode.getProperty("parameterList").toString())))
		  {
			  //different modifier
			  System.out.println("Send to Client::"+ clientMethodNode.getProperty("name")+"has a different parameterList");
			  communicator.informPropertyChangeMethodNodeCase3(clientMethodNode, serverMethodNode, "parameterList", lineNumber);
		  }
		  
		  //check for body
		  /*important*/
		  System.out.println("Check for method body here");
		  //check for attributes inside methods
		  String clientMethodBody= (String) clientMethodNode.getProperty("body"); 
		  String serverMethodBody= (String) serverMethodNode.getProperty("body");
		  
		  if (!clientMethodBody.equals(serverMethodBody))
		  {
			  System.out.println("ClientMethodBody:: "+clientMethodBody);
			  System.out.println("serverMethodBody:: "+serverMethodBody);
			  findDifferenceInMethodBody(clientMethodBody,serverMethodBody, clientMethodNode, serverMethodNode);
		  }
		  checkVariableDeclarationNodes(clientMethodNode, serverMethodNode);
	  }
	  
	  public void findDifferenceInMethodBody(String clientMethodBody, String serverMethodBody, Node clientMethodNode, Node serverMethodNode)
	  {

		  //get client body in an array
		  
		  String lineNumber= clientMethodNode.getProperty("lineNumber").toString();
		  String clientBody[]= clientMethodBody.split(";");
		  String serverBody[]= serverMethodBody.split(";");
		  int i;
		  for (i=0; i< clientBody.length && i < serverBody.length; i++)
		  {
			  System.out.println("ClientBody[i]:: "+i + " "+ clientBody[i]);
			  System.out.println("serverBody[i]:: "+i + " "+ serverBody[i]);
			  
			  if (!clientBody[i].equals(serverBody[i]))
			  {
				  // inform communicator
				  System.out.println("Body modified at:: "+ i + " "+ clientBody[i]);
				  communicator.informMethodBodyChange(clientMethodNode, serverMethodNode, clientBody[i], "BODY MODIFIED", i+2+Integer.parseInt(lineNumber));
			  }
		  }
		  
		  if (clientBody.length > serverBody.length)
		  {
			  //added content to client body
			  // inform communicator
			 communicator.informMethodBodyChange(clientMethodNode, serverMethodNode, clientBody[i], "BODY ADDED", i+2+Integer.parseInt(lineNumber));
		  }
		  else
		  {
			  if (clientBody.length < serverBody.length){
			  //deleted content from method
			  communicator.informMethodBodyChange(clientMethodNode, serverMethodNode, serverBody[i], "BODY DELETED", i+2+Integer.parseInt(lineNumber));
			  }
		  }
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
			  for (Relationship rServer: relationServer)
				{
				  otherServerNode=rServer.getOtherNode(serverMethodNode);
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
					invokeMethodConnectingEdgeCreation(otherClientNode, clientMethodNode, serverMethodNode);
					String lineNumber= otherClientNode.getProperty("lineNumber").toString();
					 communicator.informAdditionMethodAttributeNodeCase1(otherClientNode, clientMethodNode, serverMethodNode, lineNumber);
				}
			}
	  }
	  
	  
	  public void invokeCheckMethodAttributeProperties(Node clientMethodAttributeNode, Node serverMethodAttributeNode )
	  {		  
	    	//modifier, returnType and parameterList, body (may be null)
		  
		  // Get clientMethodNode and serverMethodNode here
		  Node clientMethodNode=null;
		  Node serverMethodNode=null;
		  
		  Relationship r= clientMethodAttributeNode.getSingleRelationship(methodRelTypes.BODY, Direction.OUTGOING);
		  clientMethodNode= r.getOtherNode(clientMethodAttributeNode); 
		  
		  r= serverMethodAttributeNode.getSingleRelationship(methodRelTypes.BODY, Direction.OUTGOING);
		  serverMethodNode= r.getOtherNode(serverMethodAttributeNode);
		  
		  String lineNumber= clientMethodAttributeNode.getProperty("lineNumber").toString();
		  
		  if (!(clientMethodAttributeNode.getProperty("modifier").toString().equals(serverMethodAttributeNode.getProperty("modifier").toString())))
		  {
			  //different modifier
			  System.out.println("Send to Client::"+ clientMethodAttributeNode.getProperty("name")+"has a different modifier");
			  communicator.informPropertyChangeMethodAttributeNodeCase3(clientMethodNode, serverMethodNode, "modifier", lineNumber);
		  }
		  
		  if (!(clientMethodAttributeNode.getProperty("dataType").toString().equals(serverMethodAttributeNode.getProperty("dataType").toString())))
		  {
			  //different modifier
			  System.out.println("Send to Client::"+ clientMethodAttributeNode.getProperty("name")+"has a different dataType");			  
			  communicator.informPropertyChangeMethodAttributeNodeCase3(clientMethodNode, serverMethodNode, "dataType", lineNumber);
			 
			  invokeMethodConnectingEdgeCreation(clientMethodAttributeNode, clientMethodNode, serverMethodNode);
		  }
		  
		  if (!(clientMethodAttributeNode.getProperty("initializer").toString().equals(serverMethodAttributeNode.getProperty("initializer").toString())))
		  {
			  //different modifier
			  System.out.println("Send to Client::"+ clientMethodAttributeNode.getProperty("name")+"has a different initializer");
			  communicator.informPropertyChangeMethodAttributeNodeCase3(clientMethodNode, serverMethodNode, "initializer", lineNumber);
		  }

	  }
	  
	  public void invokeMethodConnectingEdgeCreation(Node clientMethodAttributeNode, Node clientMethodNode, Node serverMethodNode)
	  {
		  String dataType= clientMethodAttributeNode.getProperty("dataType").toString();
		  //search this datatype class in server, if exists... 
		  Node serverClassNode = searchDatatypeClassInServerGraph(dataType);
		  
		  //invoke creation of dependency edge
		 if (serverClassNode !=null) communicator.informAdditionOfMethodAttributeConnectingEdge(clientMethodAttributeNode, clientMethodNode, serverClassNode);
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
		 
		 private void readFromFileHashMap(HashMap<Long, String> nodeCanonicalMap)
		 {
		
			 
			 BufferedReader br = null;
			 
				try {
					int index=0;
					String sCurrentLine;
					String  key=null;
					String value = null;
							
					br = new BufferedReader(new FileReader("neo4jDB/HashMap.txt"));
					while ((sCurrentLine = br.readLine()) != null) {
					//	System.out.println("sCurrentLine:"+sCurrentLine);
						index = sCurrentLine.indexOf(":");
					//	System.out.println("index:"+index);
						key = sCurrentLine.substring(0, index);
					//	System.out.println("key:"+key);
						long l = Long.parseLong(key);
						value = sCurrentLine.substring(index+1, sCurrentLine.length());
					//	System.out.println("key:"+l+" Value:"+value);
						nodeCanonicalMap.put(l, value);
									
					}
		 
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						if (br != null)br.close();
					} catch (IOException ex) {
						ex.printStackTrace();
					}
				}					
		 }	    
}
