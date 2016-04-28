package org.apache.collab.server.Comparator;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.collab.server.LoadDriver;
import org.apache.collab.server.Comparator.CompareGraphs.RelTypes;
import org.apache.collab.server.Comparator.CompareGraphs.dGraphNodeType;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.ResourceIterator;

public class InconsistencyCommunicator {

	String collabName=null;
	String ipAddSQL=null;
	boolean DEBUG= true;
	Connection conn=null;
	
	public InconsistencyCommunicator(String cName, String ipSQL)
	{
		collabName=cName;
		if (DEBUG) System.out.println("In InconsistencyCommunicator::Collab::"+collabName);
		ipAddSQL= ipSQL;
		conn= LoadDriver.connect;
	}
	
	 public  static enum dGraphNodeType implements Label {
	    	PROJECT, PACKAGE, CLASS, INTERFACE, METHOD, ATTRIBUTE;
	    } 
	 
	 String msg_collaborator= "|by Collaborator:: ";
	 String msg_add_class="| Message: Addition of new class: ";
	String msg_add_attribute="| Message: Addition of new attribute to: ";
	String msg_add_method="| Message: Addition of new method: ";
	String msg_add_method_attribute="| Message: Addition of new attribute to method: ";
	
	String msg_del_method="| Message: Deletion of a method: ";
	String msg_del_attribute="| Message: Deletion of an attribute: ";
	String msg_del_method_attribute="| Message: Deletion of a method attribute: ";
	
	String msg_add_attribute_dependency= "| Message: Addition/Modification of attribute dependency : |";
	String msg_add_method_attribute_dependency= "| Message: Addition/Modification of attribute dependency to the method : ";
			
	public void informAdditionClassNodeCase1(Node clientNode, GraphDatabaseService graphDbServer)
	{
		if (DEBUG) System.out.println("In informAdditionClassNodeCase1");
		//1)
		//get package info from client
		//search that package in server
		//inform package node
		String packageName= null;
		
		if (clientNode.getProperty("packageName") != null)
		{
			packageName= clientNode.getProperty("packageName").toString();
		}
		
		Node packageNode=null;
		 ResourceIterator<Node> serverPackageNodes= graphDbServer.findNodes(dGraphNodeType.PACKAGE);
			while (serverPackageNodes.hasNext() )
			{
				packageNode= serverPackageNodes.next();
				 if (packageNode.getProperty("name").toString().equals(packageName))
				 {
					 //package node found
					 sendInfo(packageNode, msg_add_class+clientNode.getProperty("name"));
					 break;
				 }
			}
		
		//2)
		//inform dependencies (including transitive of parent of N1)
		//inform all nodes that dependent on parent node
		// inform all nodes that parent node depends upon
		//inform all transitive dependencies-- important not done
		if (packageNode !=null)	
		{
			//this the parent node
			  Node otherNode;
			Iterable<Relationship> relations;
			  relations= packageNode.getRelationships(RelTypes.DEPENDENCY);
				for (Relationship r: relations)
				{
					otherNode=r.getOtherNode(packageNode);
					 sendInfo(otherNode, msg_add_class+clientNode.getProperty("name"));
				}
		}
			
	}
	
	public void informAdditionAttributeNodeCase1(Node clientAttributeNode, Node serverClassNode, GraphDatabaseService graphDbServer)
	{
		if (DEBUG) System.out.println("In informAdditionAttributeNodeCase1");
		
		// inform parent of N1 
		sendInfo(serverClassNode, msg_add_attribute+serverClassNode.getProperty("canonicalName")+"| Attribute Added::"+ clientAttributeNode.getProperty("name") );
		
		//inform all dependencies on parent of N1
		  Node otherNode;
				Iterable<Relationship> relations;
				  relations= serverClassNode.getRelationships(RelTypes.DEPENDENCY);
					for (Relationship r: relations)
					{
						otherNode=r.getOtherNode(serverClassNode);
						 sendInfo(otherNode, msg_add_attribute+serverClassNode.getProperty("canonicalName")+"| Attribute Added::"+ clientAttributeNode.getProperty("name") );
					}
		
	}
	
	public void informAdditionMethodNodeCase1(Node clientNode, Node serverClassNode, GraphDatabaseService graphDbServer)
	{
		if (DEBUG) System.out.println("In informAdditionMethodNodeCase1");
		// inform parent of N1 
		sendInfo(serverClassNode, msg_add_method+clientNode.getProperty("name"));
		
		//inform all dependencies on parent of N1
		  Node otherNode;
				Iterable<Relationship> relations;
				  relations= serverClassNode.getRelationships(RelTypes.DEPENDENCY);
					for (Relationship r: relations)
					{
						otherNode=r.getOtherNode(serverClassNode);
						 sendInfo(otherNode, msg_add_method+clientNode.getProperty("name"));
					}
		
	}
	
	public void informAdditionMethodAttributeNodeCase1(Node clientNode, Node serverClassNode, GraphDatabaseService graphDbServer)
	{
		if (DEBUG) System.out.println("In informAdditionMethodAttributeNodeCase1");
		// inform parent of N1 
		sendInfo(serverClassNode, msg_add_method_attribute+clientNode.getProperty("name"));
		
		//inform all dependencies on parent of N1
		  Node otherNode;
				Iterable<Relationship> relations;
				  relations= serverClassNode.getRelationships(RelTypes.DEPENDENCY);
					for (Relationship r: relations)
					{
						otherNode=r.getOtherNode(serverClassNode);
						 sendInfo(otherNode, msg_add_method_attribute+clientNode.getProperty("name"));
					}
		
	}
	
	public void informDeletionOfClassAttributeCase2(Node clientClassNode, Node serverAttributeNode, GraphDatabaseService graphDbServer)
	{
		if (DEBUG) System.out.println("In informDeletionOfClassAttributeCase2");
		// inform parent of N1 
		Node serverClassNode =null;
		Relationship r1= serverAttributeNode.getSingleRelationship(RelTypes.CONNECTING, Direction.OUTGOING);
		if (r1!=null) serverClassNode = r1.getOtherNode(serverAttributeNode);
		
		sendInfo(serverClassNode, msg_del_attribute+serverAttributeNode.getProperty("name"));
		
		//inform all dependencies on parent of N1
		  Node otherNode;
				Iterable<Relationship> relations;
				  relations= serverClassNode.getRelationships(RelTypes.DEPENDENCY);
					for (Relationship r: relations)
					{
						otherNode=r.getOtherNode(serverClassNode);
						 sendInfo(otherNode, msg_del_attribute+serverAttributeNode.getProperty("name"));
					}
		
	}
	
	public void informDeletionOfClassMethodCase2(Node clientClassNode, Node serverMethodNode, GraphDatabaseService graphDbServer)
	{
		if (DEBUG) System.out.println("In informDeletionOfClassMethodCase2");
		// inform parent of N1 
		Node serverClassNode =null;
		Relationship r1= serverMethodNode.getSingleRelationship(RelTypes.CONNECTING, Direction.OUTGOING);
		if (r1!=null) serverClassNode = r1.getOtherNode(serverMethodNode);
		
		sendInfo(serverClassNode, msg_del_method+serverMethodNode.getProperty("name"));
		
		//inform all dependencies on parent of N1
		  Node otherNode;
				Iterable<Relationship> relations;
				  relations= serverClassNode.getRelationships(RelTypes.DEPENDENCY);
					for (Relationship r: relations)
					{
						otherNode=r.getOtherNode(serverClassNode);
						 sendInfo(otherNode, msg_del_method+serverMethodNode.getProperty("name"));
					}
		
	}
	
	public void informDeletionOfMethodAttributeCase2(Node clientMethodAttributeNode, Node serverMethodNode, GraphDatabaseService graphDbServer)
	{
		if (DEBUG) System.out.println("In informDeletionOfMethodAttributeCase2");
		// inform parent of N1 		
		sendInfo(serverMethodNode, msg_del_method_attribute+clientMethodAttributeNode.getProperty("name"));
		
		//inform all dependencies on parent of N1
		  Node otherNode;
				Iterable<Relationship> relations;
				  relations= serverMethodNode.getRelationships(RelTypes.DEPENDENCY);
					for (Relationship r: relations)
					{
						otherNode=r.getOtherNode(serverMethodNode);
						 sendInfo(otherNode,msg_del_method_attribute+clientMethodAttributeNode.getProperty("name"));
					}
		
	}
	
	public void informAdditionOfAttributeDependencyEdge(Node clientAttributeNode, Node serverClassNode, GraphDatabaseService graphDbServer)
	{
		if (DEBUG) System.out.println("In informAdditionOfAttributeDependencyEdge");
		// N1- clientAttributeNode
		// N2 - serverAttributeNode
		
	    // get clientClassNode					
		Node clientClassNode=null;
		if (clientAttributeNode!=null)
		{
			Relationship r1= clientAttributeNode.getSingleRelationship(RelTypes.CONNECTING, Direction.OUTGOING);
			if (r1!=null) clientClassNode = r1.getOtherNode(clientAttributeNode);			
		}
		
		//get node N2
		Node serverAttributeNode=null;
		Node otherNode=null;
		Iterable<Relationship> relations= serverClassNode.getRelationships(RelTypes.CONNECTING);
		for (Relationship r: relations)
		{
			otherNode=r.getOtherNode(serverClassNode);
			if (otherNode.getProperty("name").toString().equals(clientAttributeNode.getProperty("name").toString())) 
			{
				serverAttributeNode= otherNode;
			}
		}
		
		//1) Inform node N2: serverAttributeNode
		if (serverAttributeNode !=null) sendInfo(serverAttributeNode ,msg_add_attribute_dependency+clientAttributeNode.getProperty("name")+ "| to the class: |"+ clientClassNode.getProperty("name"));
		
		//2) inform all dependencies of N2
		 
		if (serverAttributeNode !=null)
			{
			otherNode =null;
			
			relations=null;
			  relations= serverAttributeNode.getRelationships(RelTypes.DEPENDENCY);
				for (Relationship r: relations)
				{
					otherNode=r.getOtherNode(serverAttributeNode);
					 sendInfo(otherNode,msg_add_attribute_dependency+clientAttributeNode.getProperty("name")+ "| to the class: |"+ clientClassNode.getProperty("name"));
				}
			}
				
		//3) inform parent of N2
		sendInfo(serverClassNode ,msg_add_attribute_dependency+clientAttributeNode.getProperty("name")+ "| to the class: |"+ clientClassNode.getProperty("name"));		
		
		// 4) inform all dependencies on parent of N2
		  otherNode =null;
				relations=null;
				  relations= serverClassNode.getRelationships(RelTypes.DEPENDENCY);
					for (Relationship r: relations)
					{
						otherNode=r.getOtherNode(serverClassNode);
						sendInfo(otherNode, msg_add_attribute_dependency+clientAttributeNode.getProperty("name")+ "| to the class: |"+ clientClassNode.getProperty("name"));
					}
		
					
		//5) Inform node N1: clientAttributeNode
		if (clientAttributeNode!=null) sendInfo(clientAttributeNode, msg_add_attribute_dependency+clientAttributeNode.getProperty("name")+ "| to the class: |"+ clientClassNode.getProperty("name"));
			
	    //6) Inform all dependencies of N1
		if (clientAttributeNode!=null)
		{
		otherNode =null;
				relations=null;
				  relations= clientAttributeNode.getRelationships(RelTypes.DEPENDENCY);
					for (Relationship r: relations)
					{
						otherNode=r.getOtherNode(serverClassNode);
						sendInfo(otherNode, msg_add_attribute_dependency+clientAttributeNode.getProperty("name")+ "| to the class: |"+ clientClassNode.getProperty("name"));
					}
		}
					
	    //7) Inform parent of N1 : clientClassNode					

		if (clientClassNode !=null)
				sendInfo(clientClassNode ,msg_add_attribute_dependency+clientAttributeNode.getProperty("name")+ "| to the class: |"+ clientClassNode.getProperty("name"));

		
		  //8) Inform all dependencies of parent of N1-clientClassNode 
		// -dependency of parent exist in the serverGrpah
		// get the same node as this classNode in the server Graph
		Node clientClassNodeInServer=null;
		
		clientClassNodeInServer= graphDbServer.findNode(dGraphNodeType.CLASS, "name", clientClassNode.getProperty("name").toString());
		if (clientClassNodeInServer !=null)
			{
			otherNode =null;
			
				relations=null;
				  relations= clientClassNodeInServer.getRelationships(RelTypes.DEPENDENCY);
					for (Relationship r: relations)
					{
						otherNode=r.getOtherNode(clientClassNodeInServer);
						sendInfo(otherNode, msg_add_attribute_dependency+clientAttributeNode.getProperty("name")+ "| to the class: |"+ clientClassNode.getProperty("name"));
					}
			}
	}
	
	public void informAdditionOfMethodAttributeDependencyEdge(Node clientMethodAttributeNode, Node clientMethodNode, Node serverClassNode)
	{
		if (DEBUG) System.out.println("In informAdditionOfMethodAttributeDependencyEdge");
		// N1- clientMethodNode
		// N2 - serverMethodNode
		
		//get node N2
		Node serverMethodNode=null;
		Node otherNode=null;
		Iterable<Relationship> relations= serverClassNode.getRelationships(RelTypes.CONNECTING);
		for (Relationship r: relations)
		{
			otherNode=r.getOtherNode(serverClassNode);
			if (otherNode.getProperty("name").toString().equals(clientMethodNode.getProperty("name").toString())) 
			{
				serverMethodNode= otherNode;
			}
		}
		
		//1) Inform node N2: serverMethodNode
		if (serverMethodNode !=null) sendInfo(serverMethodNode ,msg_add_method_attribute_dependency+clientMethodAttributeNode.getProperty("name"));
		
		//2) inform all dependencies of N2
		 
		if (serverMethodNode !=null)
			{
			otherNode =null;
			
			relations=null;
			  relations= serverMethodNode.getRelationships(RelTypes.DEPENDENCY);
				for (Relationship r: relations)
				{
					otherNode=r.getOtherNode(serverMethodNode);
					 sendInfo(otherNode,msg_add_method_attribute_dependency+clientMethodAttributeNode.getProperty("name"));
				}
			}
				
		//3) inform parent of N2
		sendInfo(serverClassNode ,msg_add_method_attribute_dependency+clientMethodAttributeNode.getProperty("name"));		
		
		// 4) inform all dependencies on parent of N2
		  otherNode =null;
				relations=null;
				  relations= serverClassNode.getRelationships(RelTypes.DEPENDENCY);
					for (Relationship r: relations)
					{
						otherNode=r.getOtherNode(serverClassNode);
						sendInfo(otherNode, msg_add_method_attribute_dependency+clientMethodAttributeNode.getProperty("name"));
					}
		
					
		//5) Inform node N1: clientAttributeNode
		if (clientMethodNode!=null) sendInfo(clientMethodNode, msg_add_method_attribute_dependency+clientMethodAttributeNode.getProperty("name"));
			
	    //6) Inform all dependencies of N1
		if (clientMethodNode!=null)
		{
		otherNode =null;
				relations=null;
				  relations= clientMethodNode.getRelationships(RelTypes.DEPENDENCY);
					for (Relationship r: relations)
					{
						otherNode=r.getOtherNode(serverClassNode);
						sendInfo(otherNode, msg_add_method_attribute_dependency+clientMethodAttributeNode.getProperty("name"));
					}
		}
					
	    //7) Inform parent of N1 : clientClassNode					
		Node clientClassNode=null;
		if (clientMethodNode!=null)
		{
			Relationship r1= clientMethodNode.getSingleRelationship(RelTypes.CONNECTING, Direction.OUTGOING);
			if (r1!=null) clientClassNode = r1.getOtherNode(clientMethodNode);
			if (clientClassNode !=null)
				sendInfo(clientClassNode ,msg_add_method_attribute_dependency+clientMethodAttributeNode.getProperty("name")+ "to the class: "+ clientClassNode.getProperty("name"));
		}
		
		  //8) Inform all dependencies of parent of N1-clientClassNode
		if (clientClassNode !=null)
			{
			otherNode =null;
			
				relations=null;
				  relations= clientClassNode.getRelationships(RelTypes.DEPENDENCY);
					for (Relationship r: relations)
					{
						otherNode=r.getOtherNode(clientClassNode);
						sendInfo(otherNode, msg_add_method_attribute_dependency+clientMethodAttributeNode.getProperty("name"));
					}
			}
	}
	
	 public void sendInfo(Node node, String message)
	 {
		 message= message + msg_collaborator+collabName;
		 System.out.println("Send Info to::"+ node.getProperty("canonicalName")+ message);
		//filterMessage(node, "Send Info to::"+ node.getProperty("canonicalName")+ message);
	 }
	 
	 public void filterMessage(Node node, String message)
	 {
		 if (!messageAlreadySent(node, message))
		 {
			 if ((node.getProperty("nodeType").toString().equals("ATTRIBUTE")) || (node.getProperty("nodeType").toString().equals("METHOD")))
			 {
				 //get enclosing class
				 Node ClassNode=null;				
				Relationship r1= node.getSingleRelationship(RelTypes.CONNECTING, Direction.OUTGOING);
				if (r1!=null) ClassNode = r1.getOtherNode(node);
				sendInfoToDB(ClassNode, message);									 
			 }
			 else
			 {
				 //send message to DB
				 sendInfoToDB(node, message);
			 }			 
		 }
	 }
	 
	 public boolean messageAlreadySent(Node node, String message)
	 {
		 boolean exists= false;
		 //searchDB for existence of message
		  Statement statement = null;
 		  String sql = null;
 		 ResultSet resultSet =null;
 		  try {
	       if (conn !=null) 
	    	   { 
				statement = conn.createStatement();				    	   
	    	   // Result set get the result of the SQL query
	    	   resultSet = statement.executeQuery("select * from conflictMessages where sentNode="+ node.getProperty("canonicalName").toString() + " AND message="+message);
	    	   
	    	   if (resultSet!=null)  exists=true;
	    	   }	       
 		 } catch (SQLException e) {				
				e.printStackTrace();
			}
		 return exists;
	 }
	 
	 public void sendInfoToDB(Node node, String message)
	 {
	        if (DEBUG) System.out.println("ipAddress SQL: "+ipAddSQL);
	        
	        //make connection to DB
	      
	        
		       if (conn !=null) 
		    	   {
		    	   	//updateUserActivityTable(con);
		    	   Statement statement = null;
		 		  String sql = null;

		 			 //insert data here
		 			 try {
		 		        	
		 				 if (DEBUG) System.out.println("inserting data in conflictMessages table in given database...");
		 				 statement = conn.createStatement();
		 				
		 				 			 
		 				 sql = "INSERT INTO conflictMessages"+
		 		                   " (sentNode, message) VALUES  ('"+node.getProperty("canonicalName").toString()+"','"+message+"');";
		 				 if (DEBUG) System.out.println("SQL: "+sql);
		 				 statement.executeUpdate(sql);
		 			   	   	
		 				
		 				  if (DEBUG)  System.out.println("inserted data in conflictMessages...");
		 			      
		 		          
		 		        } catch (SQLException ex) {
		 		            // handle any errors
		 		            System.out.println("SQLException: " + ex.getMessage());
		 		            System.out.println("SQLState: " + ex.getSQLState());
		 		            System.out.println("VendorError: " + ex.getErrorCode());		 		           
		 		        }
		    	   }
		       else 
		       {
		    	   System.out.println("No connection exists:");//need to forward this error
		       }
		       //do not close this connection
	 }
	 
	 
	 public void informTransitiveDependencyNodes(Node node, int radius, String msg, String name)
	 {
		
		 if (radius == 0)
			return;
		else
		{
		 Iterable<Relationship> relations;
		 Node otherNode=null;
		  relations= node.getRelationships(RelTypes.DEPENDENCY);
			for (Relationship r: relations)
			{
				otherNode=r.getOtherNode(node);
				 sendInfo(otherNode,msg+name);
				 informTransitiveDependencyNodes(otherNode, radius-1, msg, name);
			}
		}
	 }
}
