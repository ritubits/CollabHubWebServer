package org.apache.collab.server.Comparator;

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

	 public  static enum dGraphNodeType implements Label {
	    	PROJECT, PACKAGE, CLASS, INTERFACE, METHOD, ATTRIBUTE;
	    } 
	 
	String msg_add_class="| Message: Addition of new class: ";
	String msg_add_attribute="| Message: Addition of new attribute to: ";
	String msg_add_method="| Message: Addition of new method: ";
	String msg_add_method_attribute="| Message: Addition of new attribute to method: ";
	
	String msg_del_method="| Message: Deletion of a method: ";
	String msg_del_attribute="| Message: Deletion of an attribute: ";
	String msg_del_method_attribute="| Message: Deletion of a method attribute: ";
	
	String msg_add_attribute_dependency= "| Message: Addition/Modification of attribute dependency to the class : ";
	
	public void informAdditionClassNodeCase1(Node clientNode, GraphDatabaseService graphDbServer)
	{
		System.out.println("In informAdditionClassNodeCase1");
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
	
	public void informAdditionOfDependencyEdge(Node clientAttributeNode, Node serverClassNode, GraphDatabaseService graphDbServer)
	{

		//1) Inform node N2: serverClassNode
		sendInfo(serverClassNode ,msg_add_attribute_dependency+clientAttributeNode.getProperty("name"));
		
		//2) inform all dependencies of N2
		
		
		
		// 4) inform all dependencies on parent of N2
	/*	  Node otherNode;
				Iterable<Relationship> relations;
				  relations= serverClassNode.getRelationships(RelTypes.DEPENDENCY);
					for (Relationship r: relations)
					{
						otherNode=r.getOtherNode(serverClassNode);
						 sendInfo(otherNode, msg_add_method+clientNode.getProperty("name"));
					}*/
		
					
		//5) Inform node N1: clientClassNode
		Node clientClassNode=null;
		Relationship r1= clientAttributeNode.getSingleRelationship(RelTypes.CONNECTING, Direction.OUTGOING);
		if (r1!=null) clientClassNode = r1.getOtherNode(clientAttributeNode);
		if (clientClassNode !=null)
			sendInfo(clientClassNode ,msg_add_attribute_dependency+clientAttributeNode.getProperty("name")+ "to the class: "+ clientClassNode.getProperty("name"));
	}
	
	
	 public void sendInfo(Node node, String message)
	 {
		 System.out.println("Send Info to::"+ node.getProperty("canonicalName")+ message);
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
