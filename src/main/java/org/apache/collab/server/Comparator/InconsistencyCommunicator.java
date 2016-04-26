package org.apache.collab.server.Comparator;

import org.apache.collab.server.Comparator.CompareGraphs.RelTypes;
import org.apache.collab.server.Comparator.CompareGraphs.dGraphNodeType;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.ResourceIterator;

public class InconsistencyCommunicator {

	 public  static enum dGraphNodeType implements Label {
	    	PROJECT, PACKAGE, CLASS, INTERFACE, METHOD, ATTRIBUTE;
	    } 
	 
	String msg_add_class=" Message: Addition of new class: ";
	String msg_add_attribute=" Message: Addition of new attribute: ";
	String msg_add_method=" Message: Addition of new method: ";
	String msg_add_method_attribute=" Message: Addition of new attribute to method: ";
	
	
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
	
	public void informAdditionAttributeNodeCase1(Node clientNode, Node serverClassNode, GraphDatabaseService graphDbServer)
	{
		// inform parent of N1 
		sendInfo(serverClassNode, msg_add_attribute+clientNode.getProperty("name"));
		
		//inform all dependencies on parent of N1
		  Node otherNode;
				Iterable<Relationship> relations;
				  relations= serverClassNode.getRelationships(RelTypes.DEPENDENCY);
					for (Relationship r: relations)
					{
						otherNode=r.getOtherNode(serverClassNode);
						 sendInfo(otherNode, msg_add_attribute+clientNode.getProperty("name"));
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
	
	 public void sendInfo(Node node, String message)
	 {
		 System.out.println("Send Info to::"+ node.getProperty("name")+ message);
	 }
}
