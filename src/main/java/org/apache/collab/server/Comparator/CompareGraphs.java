package org.apache.collab.server.Comparator;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.neo4j.logging.Log;
import org.neo4j.logging.LogProvider;
import org.neo4j.logging.NullLogProvider;
import org.apache.collab.server.CreateDependencyGraph;
import org.apache.collab.server.dependencyGraphNodes;
import org.apache.collab.server.CreateDependencyGraph.dGraphNodeType;
import org.neo4j.cypher.ExecutionEngine;
import org.neo4j.cypher.ExecutionResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseBuilder;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.helpers.collection.IteratorUtil;
import org.neo4j.io.fs.FileUtils;
import org.neo4j.server.*;

public class CompareGraphs {

	
	 public  static enum dGraphNodeType implements Label {
	    	PROJECT, PACKAGE, CLASS, INTERFACE, METHOD, ATTRIBUTE;
	    }   
	    
	 
	 private static String DB_PATH_CLIENT = "neo4jDB/Client/";
	 private final String DB_PATH_SERVER = "neo4jDB/Server";
	 private dependencyGraphNodes dpGraph;
	 GraphDatabaseService graphDbServer;
	 GraphDatabaseService graphDbClient;
	 
	 Node rootNodeSever=null;
	 Node rootNodeClient =null;
	 
	  public static void main(String args[]) {

		  CompareGraphs db= new CompareGraphs();
		  DB_PATH_CLIENT = DB_PATH_CLIENT + "COllabClient";
	       	db.initializeDB("COllabClient");
				
		}
 
	    public void initializeDB(String collabName) {
			
	    	try {
	    		
	    		dpGraph = new dependencyGraphNodes();	    		
	    		clearDBServer();
	    		clearDBClient();
	    		File dbDirServer = new File(DB_PATH_SERVER);
	    		File dbDirClient = new File(DB_PATH_CLIENT);
	    		
	    		
	    		//for server DB
	    		GraphDatabaseFactory graphFactoryServer = new GraphDatabaseFactory();
	    		GraphDatabaseBuilder graphBuilderServer = graphFactoryServer.newEmbeddedDatabaseBuilder(dbDirServer);
	    		 graphDbServer = graphBuilderServer.newGraphDatabase();  
	    		 
	    		 
	            registerShutdownHook( graphDbServer );

	        	//for client DB
	    	//	GraphDatabaseFactory graphFactoryClient = new GraphDatabaseFactory();
	    	//	GraphDatabaseBuilder graphBuilderClient = graphFactoryClient.newEmbeddedDatabaseBuilder(dbDirClient);
	    	//	 graphDbClient = graphBuilderClient.newGraphDatabase();                  
	        //    registerShutdownHook( graphDbClient );
	            
	            ExecutionEngine engine = new ExecutionEngine(graphDbServer, new LogProvider() {
					
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

	                Iterator<Node> n_column = (Iterator<Node>) result.columnAs( "n" );
	                for ( Node node : IteratorUtil.asIterable( n_column ) )
	                {

	                    nodeResult = node + ": " + node.getProperty( "name" );

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
	            }
	            //compareDB();
				
	            
				shutDown(graphDbServer);	
				//shutDown(graphDbClient);
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
	        	 
	        	 compareConnectingGraph();
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
	    
	   public void compareConnectingGraph()
	    {
		   Node clientNode= null;
		   //assumes find all class nodes.... only one such exist
		   ResourceIterator<Node> clientClassNodes= graphDbClient.findNodes(dGraphNodeType.CLASS);
		   System.out.println("clientClassNodes:: "+clientClassNodes);
			while (clientClassNodes.hasNext() )
			{
				clientNode= clientClassNodes.next();
				System.out.println("clientNode:: "+clientNode.getProperty("name"));
				//clientNodeType= (String)clientNode.getProperty("dataType");
			}
			
			 Node serverNode= null;
			   //assumes find all class nodes.... only one such exist
			   ResourceIterator<Node> serverClassNodes= graphDbServer.findNodes(dGraphNodeType.CLASS);
			   System.out.println("serverClassNodes:: "+serverClassNodes);
				while (serverClassNodes.hasNext() )
				{
					serverNode= serverClassNodes.next();
					System.out.println("serverNode:: "+serverNode.getProperty("name"));
					//clientNodeType= (String)clientNode.getProperty("dataType");
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
