package org.apache.collab.server.Comparator;

import org.apache.collab.server.Comparator.CompareGraphs.dGraphNodeType;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.ResourceIterator;

public class InconsistencyCommunicator {

	 public  static enum dGraphNodeType implements Label {
	    	PROJECT, PACKAGE, CLASS, INTERFACE, METHOD, ATTRIBUTE;
	    } 
	 
	String msg_add_class=" Message: Addition of new class: ";
	
	
	
	
	
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
					 sendInfo(packageNode, msg_add_class+clientNode.getProperty("packageName"));
				 }
			}
		
		//2)
		//inform dependencies (including transitive of parent of N1)
	}
	
	 public void sendInfo(Node node, String message)
	 {
		 System.out.println("Send Info to::"+ node.getProperty("name")+ message);
	 }
}
