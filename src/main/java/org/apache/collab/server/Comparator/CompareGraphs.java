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
	        	// createDependencyGraph(files);
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
			else
			{
				checkConnectingNodesExist(clientNode);

			}
	    }
	   
	  public void invokeCase1(Node clientNode, Node serverNode)
	   {
		  System.out.println("Invoking case1:: Addition of classNode");
		  //new node has been added 
		  communicator.informAdditionClassNodeCase1(clientNode, serverNode);
	   }
	  
	  public void checkConnectingNodesExist(Node clientNode)
	  {
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
					//check for existence of all attributes
					checkAttributesExist(otherNode, clientNode);				
				}
				else if (otherNodeType.equals("METHOD"))
				{
					//check for existence of all methods
					checkMethodsExist(otherNode, clientNode);
				}
					
			}
	  }
	  
	  public void checkAttributesExist(Node attributeNode, Node clientNode)
	  {
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
					//invokeCase1();
					//
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
		  }
		  
		  if (!(clientAttributeNode.getProperty("initializer").toString().equals(serverAttributeNode.getProperty("initializer").toString())))
		  {
			  //different modifier
			  System.out.println("Send to Client::"+ clientAttributeNode.getProperty("name")+"has s different initializer");
			  //sendMessage(clientAttributeNode, msg, caseNo);
		  }
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
					//invokeCase1();
					//
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
			  //node not found
			  //invoke(not found);
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
	    
	    
	    private void clearDBServer() {
			try {
				FileUtils.deleteRecursively(new File(DB_PATH_SERVER));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	    
	    private void clearDBClient() {
			try {
				FileUtils.deleteRecursively(new File(DB_PATH_CLIENT));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
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
