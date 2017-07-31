package org.apache.collab.server.Comparator;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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
	boolean DEBUG= false;
	Connection conn=null;
	GraphDatabaseService graphDbServer=null;
	HashMap<Long, String> nodeCanonicalHashMap=null;
	int radius=4;
	public InconsistencyCommunicator(String cName, String ipSQL , GraphDatabaseService server, HashMap<Long, String> nodeMap )
	{
		collabName=cName;
		if (DEBUG) System.out.println("In InconsistencyCommunicator::Collab::"+collabName);
		ipAddSQL= ipSQL;
		conn= LoadDriver.connect;
		createTableConflictMessages(conn);
		graphDbServer= server;
		nodeCanonicalHashMap = nodeMap;
		
		
		//added here only for checking... remove afterwards
/*			try {
		conn = LoadDriver.createConnection(ipAddSQL);
		createTableConflictMessages(conn);

	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}*/
		
	}
	
	 public  static enum dGraphNodeType implements Label {
	    	PROJECT, PACKAGE, CLASS, INTERFACE, METHOD, ATTRIBUTE;
	    } 
	 
	String msg_collaborator= "|by Collaborator:: ";
	String msg_add_class="| Message: Addition of new class: ";
	String msg_add_interface="| Message: Addition of new interface: ";
	String msg_add_attribute="| Message: Addition of new attribute to: ";
	String msg_add_method="| Message: Addition of new method : |";
	String msg_add_method_attribute="| Message: Addition of new attribute: |";
	
	String msg_del_method="| Message: Deletion of a method: ";
	String msg_del_attribute="| Message: Deletion of an attribute: ";
	String msg_del_method_attribute="| Message: Deletion of a method attribute: ";
	
	String msg_add_attribute_dependency= "| Message: Addition/Modification of attribute dependency : |";
	String msg_add_method_attribute_dependency= "| Message: Addition/Modification of attribute dependency to the method : ";
	String msg_add_class_dependency= "| Message: Addition of dependency edge: ";
	
	String msg_change_class_properties= "| Message: Change in class property: |";
	String msg_change_attribute_properties= "| Message: Change in attribute property: |";
	String msg_change_method_properties= "| Message: Change in method property: |";
	String msg_change_method_body= "| Message: Change in method body: |";
	
	String msg_change_method_attribute_properties= "| Message: Change in method attribute property: |";
	

	public void informAdditionClassNodeCase1(Node clientNode)
	{
		if (DEBUG) System.out.println("In informAdditionClassNodeCase1");
		//1)
		//get package info from client
		//search that package in server
		//inform package node- parent of N1
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
					 sendInfo(packageNode, msg_add_class+clientNode.getProperty("name")+"|to the package:|"+packageName, " ");
					 informTransitiveSubNodes(packageNode, radius, msg_add_class+clientNode.getProperty("name")+"|to the package:|"+packageName, " ");
					 break;
				 }
			}
		
		//2)
		//inform dependencies (including transitive of parent of N1)
		//inform all nodes that dependent on parent node
		// inform all nodes that parent node depends upon
		//inform all transitive dependencies
		if (packageNode !=null)	
		{
			//this the parent node
			  Node otherNode;
			Iterable<Relationship> relations;
			  relations= packageNode.getRelationships(RelTypes.DEPENDENCY);
				for (Relationship r: relations)
				{
					otherNode=r.getOtherNode(packageNode);
					 sendInfo(otherNode, msg_add_class+clientNode.getProperty("name")+"|to the package:|"+packageName, " ");
					 informTransitiveDependencyNodes(packageNode, radius, msg_add_class+clientNode.getProperty("name")+"|to the package:|"+packageName, " ");
				}
		}
			
	}
	
	public void informAdditionInterfaceNodeCase1(Node clientNode)
	{
		//to be implemented
		if (DEBUG) System.out.println("In informAdditionInterfaceNodeCase1");
		//1)
		//get package info from client
		//search that package in server
		//inform package node- parent of N1
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
					 sendInfo(packageNode, msg_add_interface+clientNode.getProperty("name")+"|to the package:|"+packageName, " ");
					 informTransitiveSubNodes(packageNode, radius, msg_add_interface+clientNode.getProperty("name")+"|to the package:|"+packageName, " ");
					 break;
				 }
			}
		
		//2)
		//inform dependencies (including transitive of parent of N1)
		//inform all nodes that dependent on parent node
		// inform all nodes that parent node depends upon
		//inform all transitive dependencies
		if (packageNode !=null)	
		{
			//this the parent node
			  Node otherNode;
			Iterable<Relationship> relations;
			  relations= packageNode.getRelationships(RelTypes.DEPENDENCY);
				for (Relationship r: relations)
				{
					otherNode=r.getOtherNode(packageNode);
					 sendInfo(otherNode, msg_add_interface+clientNode.getProperty("name")+"|to the package:|"+packageName, " ");
					 informTransitiveDependencyNodes(packageNode, radius, msg_add_interface+clientNode.getProperty("name")+"|to the package:|"+packageName, " ");
				}
		}
			
	}
	
	public void informAdditionAttributeNodeCase1(Node clientAttributeNode, Node serverClassNode, String lineNumber)
	{
		if (DEBUG) System.out.println("In informAdditionAttributeNodeCase1");
		
		// inform parent of N1 
		sendInfo(serverClassNode, msg_add_attribute+serverClassNode.getProperty("canonicalName")+"| Attribute Added::"+ clientAttributeNode.getProperty("name"), lineNumber );
		informTransitiveSubNodes(serverClassNode, radius, msg_add_attribute+serverClassNode.getProperty("canonicalName")+"| Attribute Added::"+ clientAttributeNode.getProperty("name"), lineNumber );
		
		//inform all dependencies on parent of N1
		  Node otherNode;
				Iterable<Relationship> relations;
				  relations= serverClassNode.getRelationships(RelTypes.DEPENDENCY);
					for (Relationship r: relations)
					{
						otherNode=r.getOtherNode(serverClassNode);
						 sendInfo(otherNode, msg_add_attribute+serverClassNode.getProperty("canonicalName")+"| Attribute Added::"+ clientAttributeNode.getProperty("name"), lineNumber );
						 informTransitiveDependencyNodes(serverClassNode, radius, msg_add_attribute+serverClassNode.getProperty("canonicalName")+"| Attribute Added::"+ clientAttributeNode.getProperty("name"),lineNumber );
					}
		
	}
	
	public void informAdditionMethodNodeCase1(Node clientMethodNode, Node clientClassNode, Node serverClassNode, String lineNumber)
	{
		if (DEBUG) System.out.println("In informAdditionMethodNodeCase1");
		// inform parent of N1 
		sendInfo(serverClassNode, msg_add_method+clientMethodNode.getProperty("name")+ "| to the class :|"+ clientClassNode.getProperty("name"), lineNumber);
		informTransitiveSubNodes(serverClassNode, radius, msg_add_method+clientMethodNode.getProperty("name")+"| to the class :|"+ clientClassNode.getProperty("name"), lineNumber );	
		//inform all dependencies on parent of N1
		  Node otherNode;
				Iterable<Relationship> relations;
				  relations= serverClassNode.getRelationships(RelTypes.DEPENDENCY);
					for (Relationship r: relations)
					{
						otherNode=r.getOtherNode(serverClassNode);
						 sendInfo(otherNode, msg_add_method+clientMethodNode.getProperty("name")+ "| to the class :|"+ clientClassNode.getProperty("name"), lineNumber);
						 informTransitiveDependencyNodes(serverClassNode, radius, msg_add_method+clientMethodNode.getProperty("name")+"| to the class :|"+ clientClassNode.getProperty("name"), lineNumber );
					}
		
	}
	
	public void informAdditionMethodAttributeNodeCase1(Node clientMethodAttributeNode, Node clientMethodNode, Node serverMethodNode, String lineNumber)
	{
		//also same as modification of a method 
		//need to be implemented here
		
		//get classnod
		Node serverClassNode =null;
		Relationship r1= serverMethodNode.getSingleRelationship(RelTypes.CONNECTING, Direction.OUTGOING);
		if (r1!=null) 
			{
			serverClassNode = r1.getOtherNode(serverMethodNode);				   
			}
		
		//1) inform N1
		if (DEBUG) System.out.println("In informAdditionMethodAttributeNodeCase1");
		// inform parent of N1 
		sendInfo(clientMethodNode, msg_add_method_attribute+clientMethodAttributeNode.getProperty("name")+"| to method: |"+clientMethodNode.getProperty("name")+ "| of class|"+serverClassNode.getProperty("name"),lineNumber);
		informTransitiveSubNodes(clientMethodNode, radius, msg_add_method_attribute+clientMethodAttributeNode.getProperty("name")+"| to method :|"+ clientMethodNode.getProperty("name")+ "| of class|"+serverClassNode.getProperty("name"), lineNumber );
		
		//2) inform all dependencies of N1
		 Node otherNode;
			Iterable<Relationship> relations;
			  relations= serverMethodNode.getRelationships(RelTypes.DEPENDENCY);
				for (Relationship r: relations)
				{
					otherNode=r.getOtherNode(serverMethodNode);
					sendInfo(otherNode, msg_add_method_attribute+clientMethodAttributeNode.getProperty("name")+"| to method: |"+serverMethodNode.getProperty("name")+ "| of class|"+serverClassNode.getProperty("name"), lineNumber);;
					informTransitiveDependencyNodes(clientMethodNode, radius, msg_add_method_attribute+clientMethodAttributeNode.getProperty("name")+"| to method :|"+ clientMethodNode.getProperty("name")+ "| of class|"+serverClassNode.getProperty("name"), lineNumber );
				}
				
		//3) inform parent of N1		
		sendInfo(serverClassNode, msg_add_method_attribute+clientMethodAttributeNode.getProperty("name")+"| to method: |"+clientMethodNode.getProperty("name")+ "| of class|"+serverClassNode.getProperty("name"),lineNumber);
		
		
		//4) inform dependencies of parent of N1
		 otherNode = null;
			  relations= serverClassNode.getRelationships(RelTypes.DEPENDENCY);
				for (Relationship r: relations)
				{
					otherNode=r.getOtherNode(serverClassNode);
					 sendInfo(otherNode, msg_add_method_attribute+clientMethodAttributeNode.getProperty("name")+"| to method: |"+serverMethodNode.getProperty("name")+ "| of class|"+serverClassNode.getProperty("name"), lineNumber);
				}		
	}
	
	public void informDeletionOfClassAttributeCase2(Node clientClassNode, Node serverAttributeNode, String lineNumber)
	{
		if (DEBUG) System.out.println("In informDeletionOfClassAttributeCase2");
		// inform parent of N1 
		Node serverClassNode =null;
		Relationship r1= serverAttributeNode.getSingleRelationship(RelTypes.CONNECTING, Direction.OUTGOING);
		if (r1!=null) serverClassNode = r1.getOtherNode(serverAttributeNode);
		
		sendInfo(serverClassNode, msg_del_attribute+serverAttributeNode.getProperty("name")+"| of class |"+clientClassNode.getProperty("name"), lineNumber);
		informTransitiveSubNodes(serverClassNode, radius, msg_del_attribute+serverAttributeNode.getProperty("name")+"| of class |"+ clientClassNode.getProperty("name"), lineNumber );
		
		//inform all dependencies on parent of N1
		  Node otherNode;
				Iterable<Relationship> relations;
				  relations= serverClassNode.getRelationships(RelTypes.DEPENDENCY);
					for (Relationship r: relations)
					{
						otherNode=r.getOtherNode(serverClassNode);
						sendInfo(otherNode, msg_del_attribute+serverAttributeNode.getProperty("name")+"| of class |"+clientClassNode.getProperty("name"), lineNumber);
						informTransitiveDependencyNodes(serverClassNode, radius, msg_del_attribute+serverAttributeNode.getProperty("name")+"| of class |"+ clientClassNode.getProperty("name"), lineNumber );
					}
		
	}
	
	public void informDeletionOfClassMethodCase2(Node clientClassNode, Node serverMethodNode, String lineNumber)
	{
		if (DEBUG) System.out.println("In informDeletionOfClassMethodCase2");
		// inform parent of N1 
		Node serverClassNode =null;
		Relationship r1= serverMethodNode.getSingleRelationship(RelTypes.CONNECTING, Direction.OUTGOING);
		if (r1!=null) serverClassNode = r1.getOtherNode(serverMethodNode);
		
		sendInfo(serverClassNode, msg_del_method+serverMethodNode.getProperty("name")+"|of class: |"+clientClassNode.getProperty("name"), lineNumber);
		informTransitiveSubNodes(serverClassNode, radius, msg_del_method+serverMethodNode.getProperty("name")+"| of class |"+ clientClassNode.getProperty("name"), lineNumber );
		
		//inform all dependencies on parent of N1
		  Node otherNode;
				Iterable<Relationship> relations;
				  relations= serverClassNode.getRelationships(RelTypes.DEPENDENCY);
					for (Relationship r: relations)
					{
						otherNode=r.getOtherNode(serverClassNode);
						sendInfo(otherNode, msg_del_method+serverMethodNode.getProperty("name")+"|of class: |"+clientClassNode.getProperty("name"), lineNumber);
						informTransitiveDependencyNodes(serverClassNode, radius, msg_del_method+serverMethodNode.getProperty("name")+"| of class |"+ clientClassNode.getProperty("name"), lineNumber );
					}
		
	}
	
	public void informDeletionOfMethodAttributeCase2(Node clientMethodAttributeNode, Node clientMethodNode, Node serverMethodNode, String lineNumber)
	{
		if (DEBUG) System.out.println("In informDeletionOfMethodAttributeCase2");
		//get parent of serverMethodNode
		//get the server classNode
		Node serverClassNode =null;
		Relationship r1= serverMethodNode.getSingleRelationship(RelTypes.CONNECTING, Direction.OUTGOING);
		if (r1!=null) serverClassNode = r1.getOtherNode(serverMethodNode);
		
		// inform parent of N1 		
		sendInfo(serverClassNode, msg_del_method_attribute+clientMethodAttributeNode.getProperty("name")+"| of the method: |"+serverMethodNode.getProperty("name")+"| of the class: |"+serverClassNode.getProperty("name"), lineNumber);
		informTransitiveSubNodes(serverClassNode, radius, msg_del_method_attribute+clientMethodAttributeNode.getProperty("name")+"| of the method: |"+ serverMethodNode.getProperty("name")+"| of the class: |"+serverClassNode.getProperty("name"), lineNumber );
		
		//inform all dependencies on parent of N1
		  Node otherNode;
				Iterable<Relationship> relations;
				  relations= serverClassNode.getRelationships(RelTypes.DEPENDENCY);
					for (Relationship r: relations)
					{
						otherNode=r.getOtherNode(serverClassNode);
						sendInfo(otherNode,msg_del_method_attribute+clientMethodAttributeNode.getProperty("name")+"| of the method: |"+serverMethodNode.getProperty("name")+"| of the class: |"+serverClassNode.getProperty("name"), lineNumber);
						informTransitiveDependencyNodes(serverClassNode, radius, msg_del_method_attribute+clientMethodAttributeNode.getProperty("name")+"| of the method: |"+ serverMethodNode.getProperty("name")+"| of the class: |"+serverClassNode.getProperty("name"), lineNumber );
					}
		
	}
	
	public void informAdditionOfMethodConnectingEdge(Node clientAttributeNode, Node serverClassNode)
	{
		
	}
	
	public void informAdditionOfAttributeConnectingEdge(Node clientAttributeNode, Node serverClassNode)
	{
		if (DEBUG) System.out.println("In informAdditionOfAttributeConnectingEdge");
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
		if (serverAttributeNode !=null) 
			sendInfo(serverAttributeNode ,msg_add_attribute_dependency+clientAttributeNode.getProperty("name")+ "| to the class: |"+ clientClassNode.getProperty("name"), " ");
		
		//2) inform all dependencies of N2
		 
		if (serverAttributeNode !=null)
			{
			otherNode =null;
			
			relations=null;
			  relations= serverAttributeNode.getRelationships(RelTypes.DEPENDENCY);
				for (Relationship r: relations)
				{
					otherNode=r.getOtherNode(serverAttributeNode);
					 sendInfo(otherNode,msg_add_attribute_dependency+clientAttributeNode.getProperty("name")+ "| to the class: |"+ clientClassNode.getProperty("name"), " ");
				}
			}
				
		//3) inform parent of N2
		sendInfo(serverClassNode ,msg_add_attribute_dependency+clientAttributeNode.getProperty("name")+ "| to the class: |"+ clientClassNode.getProperty("name"), " ");		
		
		// 4) inform all dependencies on parent of N2
		  otherNode =null;
				relations=null;
				  relations= serverClassNode.getRelationships(RelTypes.DEPENDENCY);
					for (Relationship r: relations)
					{
						otherNode=r.getOtherNode(serverClassNode);
						sendInfo(otherNode, msg_add_attribute_dependency+clientAttributeNode.getProperty("name")+ "| to the class: |"+ clientClassNode.getProperty("name"), " ");
					}
		
					
		//5) Inform node N1: clientAttributeNode
		if (clientAttributeNode!=null) sendInfo(clientAttributeNode, msg_add_attribute_dependency+clientAttributeNode.getProperty("name")+ "| to the class: |"+ clientClassNode.getProperty("name"), " ");
			
	    //6) Inform all dependencies of N1
		if (clientAttributeNode!=null)
		{
		otherNode =null;
				relations=null;
				  relations= clientAttributeNode.getRelationships(RelTypes.DEPENDENCY);
					for (Relationship r: relations)
					{
						otherNode=r.getOtherNode(serverClassNode);
						sendInfo(otherNode, msg_add_attribute_dependency+clientAttributeNode.getProperty("name")+ "| to the class: |"+ clientClassNode.getProperty("name"), " ");
					}
		}
					
	    //7) Inform parent of N1 : clientClassNode					

		if (clientClassNode !=null)
				sendInfo(clientClassNode ,msg_add_attribute_dependency+clientAttributeNode.getProperty("name")+ "| to the class: |"+ clientClassNode.getProperty("name"), " ");

		
		  //8) Inform all dependencies of parent of N1-clientClassNode 
		// -dependency of parent exist in the serverGrpah
		// get the same node as this classNode in the server Graph
	//	Node clientClassNodeInServer=null;
		
		 long serverNodeID= getCanonicalClassNodeID(clientClassNode.getProperty("canonicalName").toString());
		   System.out.println("serverNodeID::"+serverNodeID);
		   if (graphDbServer== null) System.out.println("graphDbSetver Is NULL");  
		   Node clientClassNodeInServer= graphDbServer.getNodeById(serverNodeID);
		   
	//	clientClassNodeInServer= graphDbServer.findNode(dGraphNodeType.CLASS, "name", clientClassNode.getProperty("name").toString());
		if (clientClassNodeInServer !=null)
			{
			otherNode =null;
			
				relations=null;
				  relations= clientClassNodeInServer.getRelationships(RelTypes.DEPENDENCY);
					for (Relationship r: relations)
					{
						otherNode=r.getOtherNode(clientClassNodeInServer);
						sendInfo(otherNode, msg_add_attribute_dependency+clientAttributeNode.getProperty("name")+ "| to the class: |"+ clientClassNode.getProperty("name"), " ");
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
	   
	public void informAdditionOfMethodAttributeConnectingEdge(Node clientMethodAttributeNode, Node clientMethodNode, Node serverClassNode)
	{
		if (DEBUG) System.out.println("In informAdditionOfMethodAttributeConnectingEdge");
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
		if (serverMethodNode !=null) sendInfo(serverMethodNode ,msg_add_method_attribute_dependency+clientMethodNode.getProperty("name"), " ");
		
		//2) inform all dependencies of N2
		 
		if (serverMethodNode !=null)
			{
			otherNode =null;
			
			relations=null;
			  relations= serverMethodNode.getRelationships(RelTypes.DEPENDENCY);
				for (Relationship r: relations)
				{
					otherNode=r.getOtherNode(serverMethodNode);
					 sendInfo(otherNode,msg_add_method_attribute_dependency+clientMethodNode.getProperty("name"), " ");
				}
			}
				
		//3) inform parent of N2
		sendInfo(serverClassNode ,msg_add_method_attribute_dependency+clientMethodNode.getProperty("name"), " ");		
		
		// 4) inform all dependencies on parent of N2
		  otherNode =null;
				relations=null;
				  relations= serverClassNode.getRelationships(RelTypes.DEPENDENCY);
					for (Relationship r: relations)
					{
						otherNode=r.getOtherNode(serverClassNode);
						sendInfo(otherNode, msg_add_method_attribute_dependency+clientMethodNode.getProperty("name"), " ");
					}
		
					
		//5) Inform node N1: clientAttributeNode
		if (clientMethodNode!=null) sendInfo(clientMethodNode, msg_add_method_attribute_dependency+clientMethodNode.getProperty("name"), " ");
			
	    //6) Inform all dependencies of N1
		if (clientMethodNode!=null)
		{
		otherNode =null;
				relations=null;
				  relations= clientMethodNode.getRelationships(RelTypes.DEPENDENCY);
					for (Relationship r: relations)
					{
						otherNode=r.getOtherNode(serverClassNode);
						sendInfo(otherNode, msg_add_method_attribute_dependency+clientMethodNode.getProperty("name"), " ");
					}
		}
					
	    //7) Inform parent of N1 : clientClassNode					
		Node clientClassNode=null;
		if (clientMethodNode!=null)
		{
			Relationship r1= clientMethodNode.getSingleRelationship(RelTypes.CONNECTING, Direction.OUTGOING);
			if (r1!=null) clientClassNode = r1.getOtherNode(clientMethodNode);
			if (clientClassNode !=null)
				sendInfo(clientClassNode ,msg_add_method_attribute_dependency+clientMethodNode.getProperty("name")+ " |to the class: "+ clientClassNode.getProperty("name"), " ");
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
						sendInfo(otherNode, msg_add_method_attribute_dependency+clientMethodNode.getProperty("name"), " ");
					}
			}
	}
	
	public void informAdditionOfImplementsDependencyEdge(Node clientClassNode,long serverNodeID, String dependencyType)
	{
		
	}
	
	public void informAdditionOfImportsDependencyEdge(Node clientClassNode,long serverNodeID, String dependencyType)
	{
		
	}
	
	public void informAdditionOfUsesDependencyEdge(Node clientClassNode,long serverNodeID, String dependencyType)
	{
		
	}
	
	public void informAdditionOfCallsDependencyEdge(Node clientClassNode,long serverNodeID, String dependencyType)
	{
		
	}
	
	public void informAdditionOfExtendsDependencyEdge(Node clientClassNode,long serverNodeID, String dependencyType)
	{
		if (DEBUG) System.out.println("In informAdditionOfExtendsDependencyEdge");
		// N1- clientClassNode
		// N2 - serverClassNode

		//get serverClassNode to which extends edge would be created
		
		//get node N2
		System.out.println("serverNodeID::"+serverNodeID);
		if (graphDbServer== null) System.out.println("graphDbSetver Is NULL");  
		Node serverClassNode= graphDbServer.getNodeById(serverNodeID);
		   
		   
		
		//1) Inform node N2: serverClassNode
		if (serverClassNode !=null) sendInfo(serverClassNode ,msg_add_class_dependency+"|"+dependencyType+"|from|"+clientClassNode.getProperty("name")+ "| to the class: |"+ serverClassNode.getProperty("name"), " ");
		
		//2) inform all dependencies of N2
		 if (serverClassNode !=null)
			{
			Node otherNode =null;
			
			Iterable<Relationship> relations=null;
			  relations= serverClassNode.getRelationships(RelTypes.DEPENDENCY);
				for (Relationship r: relations)
				{
					otherNode=r.getOtherNode(serverClassNode);
					sendInfo(otherNode ,msg_add_class_dependency+"|"+dependencyType+"|from|"+clientClassNode.getProperty("name")+ "| to the class: |"+ serverClassNode.getProperty("name"), " ");
				}
			}
				
		//3) inform parent of N2	
		Node serverParent=null;
		// get parent of N1 from serverGraph
		Relationship r1= serverClassNode.getSingleRelationship(RelTypes.CONNECTING, Direction.OUTGOING);
		if (r1!=null) serverParent = r1.getOtherNode(serverClassNode);
		if (serverParent !=null)
					sendInfo(serverParent ,msg_add_class_dependency+"|"+dependencyType+"|from|"+clientClassNode.getProperty("name")+ "| to the class: |"+ serverClassNode.getProperty("name"), " ");
		
		
		// 4) inform all dependencies on parent of N2
		  Node otherNode =null;
			Iterable<Relationship> relations=null;
				  relations= serverParent.getRelationships(RelTypes.DEPENDENCY);
					for (Relationship r: relations)
					{
						otherNode=r.getOtherNode(serverParent);						
						sendInfo(otherNode ,msg_add_class_dependency+"|"+dependencyType+"|from|"+clientClassNode.getProperty("name")+ "| to the class: |"+ serverClassNode.getProperty("name"), " ");
					}
		
					
		//5) Inform node N1: clientAttributeNode
		if (clientClassNode!=null) 
			sendInfo(clientClassNode ,msg_add_class_dependency+"|"+dependencyType+"|from|"+clientClassNode.getProperty("name")+ "| to the class: |"+ serverClassNode.getProperty("name"), " ");
		

							

		// -dependency of parent exist in the serverGrpah
		// get the same node as this classNode in the server Graph
		// to get the parent 
		// and the dependencies of this node
		// and get the dependencies of parent of this node
		
	    //6) Inform all dependencies of N1
		//get them from the server graph

		
		 serverNodeID= getCanonicalClassNodeID(clientClassNode.getProperty("canonicalName").toString());
		   System.out.println("serverNodeID::"+serverNodeID);
		   if (graphDbServer== null) System.out.println("graphDbSetver Is NULL");  
		   Node clientClassNodeInServer= graphDbServer.getNodeById(serverNodeID);
		   
		if (clientClassNodeInServer !=null)
			{
			otherNode =null;
			
				relations=null;
				  relations= clientClassNodeInServer.getRelationships(RelTypes.DEPENDENCY);
					for (Relationship r: relations)
					{
						otherNode=r.getOtherNode(clientClassNodeInServer);
						sendInfo(otherNode ,msg_add_class_dependency+"|"+dependencyType+"|from|"+clientClassNode.getProperty("name")+ "| to the class: |"+ serverClassNode.getProperty("name"), " ");
					}
			}
		
		
	    //7) Inform parent of N1 : clientClassNode	
		Node clientParent=null;
		// get parent of N1 from serverGraph
		Relationship r2= clientClassNodeInServer.getSingleRelationship(RelTypes.CONNECTING, Direction.OUTGOING);
		if (r2!=null) clientParent = r2.getOtherNode(clientClassNodeInServer);
		if (serverParent !=null)
					sendInfo(clientParent ,msg_add_class_dependency+"|"+dependencyType+"|from|"+clientClassNode.getProperty("name")+ "| to the class: |"+ serverClassNode.getProperty("name"), " ");
		
		
		//8) Inform all dependencies of parent of N1-clientClassNode 
		   
		if (clientParent !=null)
			{
			otherNode =null;
			
				relations=null;
				  relations= clientParent.getRelationships(RelTypes.DEPENDENCY);
					for (Relationship r: relations)
					{
						otherNode=r.getOtherNode(clientParent);
						sendInfo(otherNode ,msg_add_class_dependency+"|"+dependencyType+"|from|"+clientClassNode.getProperty("name")+ "| to the class: |"+ serverClassNode.getProperty("name"), " ");
					}
			}
	}
	
	public void informPropertyChangeClassNodeCase3(Node clientClassNode, Node serverClassNode, String propertyChanged, String lineNumber)
	{
		// 1) inform node N1
		sendInfo(clientClassNode ,msg_change_class_properties+propertyChanged+"|of class |"+ clientClassNode.getProperty("name"), lineNumber);
		informTransitiveSubNodes(clientClassNode, radius, msg_change_class_properties+propertyChanged+"|of class |"+clientClassNode.getProperty("name"),lineNumber);
		 
		// 2) inform dependencies of N1
		//get dependencies of N1 from server graph
		Node otherNode =null;
		Iterable<Relationship> relations= serverClassNode.getRelationships(RelTypes.DEPENDENCY);
			for (Relationship r: relations)
			{
				otherNode=r.getOtherNode(serverClassNode);
				sendInfo(otherNode ,msg_change_class_properties+propertyChanged+"|of class |"+ clientClassNode.getProperty("name"), lineNumber);
				informTransitiveDependencyNodes(clientClassNode, radius, msg_change_class_properties+propertyChanged+"|of class |"+clientClassNode.getProperty("name"),lineNumber);
			}
		
		// 3) inform parent of N1
			Node serverParent=null;
			// get parent of N1 from serverGraph
			Relationship r1= serverClassNode.getSingleRelationship(RelTypes.CONNECTING, Direction.OUTGOING);
			if (r1!=null) serverParent = r1.getOtherNode(serverClassNode);
			if (serverParent !=null)
			{
				sendInfo(serverParent ,msg_change_class_properties+propertyChanged+"|of class |"+ clientClassNode.getProperty("name"), lineNumber);
				informTransitiveSubNodes(serverParent, radius, msg_change_class_properties+propertyChanged+"|of class |"+clientClassNode.getProperty("name"),lineNumber);
			}
			
			
		// 4) inform dependencies of parent of N1
			if (serverParent !=null)
				{
					otherNode =null;
				
					relations= serverParent.getRelationships(RelTypes.DEPENDENCY);
					for (Relationship r: relations)
					{
						otherNode=r.getOtherNode(serverParent);
						sendInfo(otherNode ,msg_change_class_properties+propertyChanged+"|of class |"+ clientClassNode.getProperty("name"), lineNumber);
						informTransitiveDependencyNodes(clientClassNode, radius, msg_change_class_properties+propertyChanged+"|of class |"+clientClassNode.getProperty("name"),lineNumber);
					}
				}
			
	}
	
	public void informPropertyChangeAttributeNodeCase3(Node clientAttributeNode, Node serverAttributeNode, String propertyChanged, String lineNumber)
	{
		//get clientClassNode
		Node serverClassNode=null;

		// get parent of N1 from serverGraph
		Relationship r1= serverAttributeNode.getSingleRelationship(RelTypes.CONNECTING, Direction.OUTGOING);
		if (r1!=null) serverClassNode = r1.getOtherNode(serverAttributeNode);				
		
		if (serverClassNode !=null)
			// 1) inform node N1
			{
				sendInfo(clientAttributeNode ,msg_change_attribute_properties+propertyChanged+"|of attribute |"+clientAttributeNode.getProperty("name")+"|of class |"+ serverClassNode.getProperty("name"), lineNumber);
				informTransitiveSubNodes(clientAttributeNode, radius, msg_change_attribute_properties+propertyChanged+"|of attribute |"+clientAttributeNode.getProperty("name")+"|of class |"+ serverClassNode.getProperty("name"), lineNumber);
			}
		
		// 2) inform dependencies of N1
		//get dependencies of N1 from server graph
		Node otherNode =null;
		Iterable<Relationship> relations= serverAttributeNode.getRelationships(RelTypes.DEPENDENCY);
			for (Relationship r: relations)
			{
				otherNode=r.getOtherNode(serverAttributeNode);
				sendInfo(otherNode , msg_change_attribute_properties+propertyChanged+"|of attribute |"+clientAttributeNode.getProperty("name")+"|of class |"+ serverClassNode.getProperty("name"),lineNumber);
				informTransitiveDependencyNodes(otherNode, radius, msg_change_attribute_properties+propertyChanged+"|of attribute |"+clientAttributeNode.getProperty("name")+"|of class |"+ serverClassNode.getProperty("name"), lineNumber);
			}
		
		// 3) inform parent of N1
		// inform serverClassNode;
			// get parent of N1 from serverGraph		
			if (serverClassNode !=null)
			{
				sendInfo(serverClassNode , msg_change_attribute_properties+propertyChanged+"|of attribute |"+clientAttributeNode.getProperty("name")+"|of class |"+ serverClassNode.getProperty("name"), lineNumber);
			informTransitiveSubNodes(serverClassNode, radius, msg_change_attribute_properties+propertyChanged+"|of attribute |"+clientAttributeNode.getProperty("name")+"|of class |"+ serverClassNode.getProperty("name"), lineNumber);
			}
			
			
		// 4) inform dependencies of parent of N1
			if (serverClassNode !=null)
				{
					otherNode =null;
				
					relations= serverClassNode.getRelationships(RelTypes.DEPENDENCY);
					for (Relationship r: relations)
					{
						otherNode=r.getOtherNode(serverClassNode);
						sendInfo(otherNode ,msg_change_attribute_properties+propertyChanged+"|of attribute |"+clientAttributeNode.getProperty("name")+"|of class |"+ serverClassNode.getProperty("name"),lineNumber);
						informTransitiveDependencyNodes(otherNode, radius, msg_change_attribute_properties+propertyChanged+"|of attribute |"+clientAttributeNode.getProperty("name")+"|of class |"+ serverClassNode.getProperty("name"), lineNumber);
					}
				}			
	}
	
	
	public void informPropertyChangeMethodNodeCase3(Node clientMethodNode, Node serverMethodNode, String propertyChanged, String lineNumber)
	{
		//get clientClassNode
		Node serverClassNode=null;

		// get parent of N1 from serverGraph
		Relationship r1= serverMethodNode.getSingleRelationship(RelTypes.CONNECTING, Direction.OUTGOING);
		if (r1!=null) serverClassNode = r1.getOtherNode(serverMethodNode);				
		
		if (serverClassNode !=null)
		{
			// 1) inform node N1
			sendInfo(clientMethodNode ,msg_change_method_properties+propertyChanged+"|of method |"+clientMethodNode.getProperty("name")+"|of class |"+ serverClassNode.getProperty("name"), lineNumber);
			informTransitiveSubNodes(clientMethodNode, radius, msg_change_method_properties+propertyChanged+"|of method |"+clientMethodNode.getProperty("name")+"|of class |"+ serverClassNode.getProperty("name"), lineNumber);
		}
		
		// 2) inform dependencies of N1
		//get dependencies of N1 from server graph
		Node otherNode =null;
		Iterable<Relationship> relations= serverMethodNode.getRelationships(RelTypes.DEPENDENCY);
			for (Relationship r: relations)
			{
				otherNode=r.getOtherNode(serverMethodNode);
				sendInfo(otherNode , msg_change_method_properties+propertyChanged+"|of method |"+clientMethodNode.getProperty("name")+"|of class |"+ serverClassNode.getProperty("name"), lineNumber);
				informTransitiveDependencyNodes(clientMethodNode, radius, msg_change_method_properties+propertyChanged+"|of method |"+clientMethodNode.getProperty("name")+"|of class |"+ serverClassNode.getProperty("name"), lineNumber);
			}
		
		// 3) inform parent of N1
		// inform serverClassNode;
			// get parent of N1 from serverGraph		
			if (serverClassNode !=null)
			{
				sendInfo(serverClassNode , msg_change_method_properties+propertyChanged+"|of method |"+clientMethodNode.getProperty("name")+"|of class |"+ serverClassNode.getProperty("name"), lineNumber);
				informTransitiveSubNodes(clientMethodNode, radius, msg_change_method_properties+propertyChanged+"|of method |"+clientMethodNode.getProperty("name")+"|of class |"+ serverClassNode.getProperty("name"), lineNumber);
			}
			
			
		// 4) inform dependencies of parent of N1
			if (serverClassNode !=null)
				{
					otherNode =null;
				
					relations= serverClassNode.getRelationships(RelTypes.DEPENDENCY);
					for (Relationship r: relations)
					{
						otherNode=r.getOtherNode(serverClassNode);
						sendInfo(otherNode ,msg_change_method_properties+propertyChanged+"|of method |"+clientMethodNode.getProperty("name")+"|of class |"+ serverClassNode.getProperty("name"),lineNumber);
						informTransitiveDependencyNodes(clientMethodNode, radius, msg_change_method_properties+propertyChanged+"|of method |"+clientMethodNode.getProperty("name")+"|of class |"+ serverClassNode.getProperty("name"), lineNumber);
					}
				}			
	}
	
	public void informMethodBodyChange(Node clientMethodNode, Node serverMethodNode, String methodBody, String typeOfChange, int i )
	{
		//get clientClassNode
		Node serverClassNode=null;
		String lineNumber= Integer.toString(i);
		// get parent of N1 from serverGraph
		Relationship r1= serverMethodNode.getSingleRelationship(RelTypes.CONNECTING, Direction.OUTGOING);
		if (r1!=null) serverClassNode = r1.getOtherNode(serverMethodNode);				
		
		if (serverClassNode !=null)
		{// 1) inform node N1
			sendInfo(clientMethodNode ,msg_change_method_body+typeOfChange+"|of method |"+clientMethodNode.getProperty("name")+"|of class |"+ serverClassNode.getProperty("name"), lineNumber);
			informTransitiveSubNodes(clientMethodNode, radius, msg_change_method_body+typeOfChange+"|of method |"+clientMethodNode.getProperty("name")+"|of class |"+ serverClassNode.getProperty("name"), lineNumber);
		}
		
		// 2) inform dependencies of N1
		//get dependencies of N1 from server graph
		Node otherNode =null;
		Iterable<Relationship> relations= serverMethodNode.getRelationships(RelTypes.DEPENDENCY);
			for (Relationship r: relations)
			{
				otherNode=r.getOtherNode(serverMethodNode);
				sendInfo(otherNode , msg_change_method_body+typeOfChange+"|of method |"+clientMethodNode.getProperty("name")+"|of class |"+ serverClassNode.getProperty("name"),lineNumber);
				informTransitiveDependencyNodes(clientMethodNode, radius, msg_change_method_body+typeOfChange+"|of method |"+clientMethodNode.getProperty("name")+"|of class |"+ serverClassNode.getProperty("name"), lineNumber);
			}
		
		// 3) inform parent of N1
		// inform serverClassNode;
			// get parent of N1 from serverGraph		
			if (serverClassNode !=null)
			{
				sendInfo(serverClassNode , msg_change_method_body+typeOfChange+"|of method |"+clientMethodNode.getProperty("name")+"|of class |"+ serverClassNode.getProperty("name"), lineNumber);
				informTransitiveSubNodes(clientMethodNode, radius, msg_change_method_body+typeOfChange+"|of method |"+clientMethodNode.getProperty("name")+"|of class |"+ serverClassNode.getProperty("name"), lineNumber);
			}
			
			
		// 4) inform dependencies of parent of N1
			if (serverClassNode !=null)
				{
					otherNode =null;
				
					relations= serverClassNode.getRelationships(RelTypes.DEPENDENCY);
					for (Relationship r: relations)
					{
						otherNode=r.getOtherNode(serverClassNode);
						sendInfo(otherNode ,msg_change_method_body+typeOfChange+"|of method |"+clientMethodNode.getProperty("name")+"|of class |"+ serverClassNode.getProperty("name"), lineNumber);
						informTransitiveDependencyNodes(clientMethodNode, radius, msg_change_method_body+typeOfChange+"|of method |"+clientMethodNode.getProperty("name")+"|of class |"+ serverClassNode.getProperty("name"), lineNumber);
					}
				}			
	}
	
	public void informPropertyChangeMethodAttributeNodeCase3(Node clientMethodNode, Node serverMethodNode, Node clientMethodAttributeNode, String propertyChanged, String lineNumber)
	{
		//get clientClassNode
		Node serverClassNode=null;

		// get parent of N1 from serverGraph
		Relationship r1= serverMethodNode.getSingleRelationship(RelTypes.CONNECTING, Direction.OUTGOING);
		if (r1!=null) serverClassNode = r1.getOtherNode(serverMethodNode);				
		
		if (serverClassNode !=null)
		{
			// 1) inform node N1
			sendInfo(clientMethodNode ,msg_change_method_attribute_properties+propertyChanged+"|of attribute |"+clientMethodAttributeNode.getProperty("name")+"|of method |"+clientMethodNode.getProperty("name")+"|of class |"+ serverClassNode.getProperty("name"),lineNumber);
			informTransitiveSubNodes(clientMethodNode, radius, msg_change_method_attribute_properties+propertyChanged+"|of attribute |"+clientMethodAttributeNode.getProperty("name")+"|of method |"+clientMethodNode.getProperty("name")+"|of class |"+ serverClassNode.getProperty("name"), lineNumber);
		}
		
		// 2) inform dependencies of N1
		//get dependencies of N1 from server graph
		Node otherNode =null;
		Iterable<Relationship> relations= serverMethodNode.getRelationships(RelTypes.DEPENDENCY);
			for (Relationship r: relations)
			{
				otherNode=r.getOtherNode(serverMethodNode);
				sendInfo(otherNode , msg_change_method_attribute_properties+propertyChanged+"|of attribute |"+clientMethodAttributeNode.getProperty("name")+"|of method |"+clientMethodNode.getProperty("name")+"|of class |"+ serverClassNode.getProperty("name"),lineNumber);
				informTransitiveDependencyNodes(clientMethodNode, radius, msg_change_method_attribute_properties+propertyChanged+"|of attribute |"+clientMethodAttributeNode.getProperty("name")+"|of method |"+clientMethodNode.getProperty("name")+"|of class |"+ serverClassNode.getProperty("name"), lineNumber);
			}
		
		// 3) inform parent of N1
		// inform serverClassNode;
			// get parent of N1 from serverGraph		
			if (serverClassNode !=null)
				{
				sendInfo(serverClassNode , msg_change_method_attribute_properties+propertyChanged+"|of attribute |"+clientMethodAttributeNode.getProperty("name")+"|of method |"+clientMethodNode.getProperty("name")+"|of class |"+ serverClassNode.getProperty("name"),lineNumber);
				informTransitiveSubNodes(clientMethodNode, radius, msg_change_method_attribute_properties+propertyChanged+"|of attribute |"+clientMethodAttributeNode.getProperty("name")+"|of method |"+clientMethodNode.getProperty("name")+"|of class |"+ serverClassNode.getProperty("name"), lineNumber);
				}
			
			
		// 4) inform dependencies of parent of N1
			if (serverClassNode !=null)
				{
					otherNode =null;
				
					relations= serverClassNode.getRelationships(RelTypes.DEPENDENCY);
					for (Relationship r: relations)
					{
						otherNode=r.getOtherNode(serverClassNode);
						sendInfo(otherNode ,msg_change_method_attribute_properties+propertyChanged+"|of attribute |"+clientMethodAttributeNode.getProperty("name")+"|of method |"+clientMethodNode.getProperty("name")+"|of class |"+ serverClassNode.getProperty("name"),lineNumber);
						informTransitiveDependencyNodes(clientMethodNode, radius, msg_change_method_attribute_properties+propertyChanged+"|of attribute |"+clientMethodAttributeNode.getProperty("name")+"|of method |"+clientMethodNode.getProperty("name")+"|of class |"+ serverClassNode.getProperty("name"), lineNumber);
					}
				}			
	}
	 public void sendInfo(Node node, String message, String lineNo)
	 {
		
		 if (!lineNo.equalsIgnoreCase(" "))
		 {
			 message= message + msg_collaborator+collabName + "@LineNo::"+lineNo;
		 }
		 else
			 message= message + msg_collaborator+collabName;
		 if (DEBUG) System.out.println("Send Info to::"+ node.getProperty("canonicalName")+ message);
		// sendInfoToDB(node, message);
		//filterMessage(node, "Send Info to::"+ node.getProperty("canonicalName")+ message);
		filterMessage(node, message);
	 }
	 
	 public void filterMessage(Node node, String message)
	 {
		 
			 if ((node.getProperty("nodeType").toString().equals("ATTRIBUTE")) || (node.getProperty("nodeType").toString().equals("METHOD")))
			 {
				 //get enclosing class
				 Node ClassNode=null;				
				Relationship r1= node.getSingleRelationship(RelTypes.CONNECTING, Direction.OUTGOING);
				if (r1!=null) ClassNode = r1.getOtherNode(node);
				{
					 if (!messageAlreadySent(ClassNode, message)) 
					 {
						// System.out.println("In filterMessage::1::");
						 sendInfoToDB(ClassNode, message);
					 }
				}
			 }
			 else
			 {
				 //send message to DB
				 if (!messageAlreadySent(node, message)) 
				 { //System.out.println("In filterMessage::2::");
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
				sql= "select sentNode from conflictMessages where sentNode='"+ node.getProperty("canonicalName").toString() + "' AND message='"+message+"';";
				if (DEBUG) System.out.println("SQL::from filterMessage::"+sql);
	    	   resultSet = statement.executeQuery(sql);
	    	   resultSet.last();
	    	   if (DEBUG)  System.out.println("Resultset is ::"+resultSet.getRow());
	    	   if (resultSet.getRow() == 0) 
	    	   {
	    		   exists=false;
	    		   if (DEBUG)  System.out.println("Resultset is null");
	    	   }
	    	   else 
	    		   {
	    		   exists =true;
	    		   if (DEBUG)  System.out.println("Resultset is not null");
	    		   }
	    	   
	    	   resultSet.close();

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

		 		//= addLineNo(message);
		 			 //insert data here
		 			 try {
		 		        	
		 			//	 if (DEBUG) System.out.println("inserting data in conflictMessages table in given database...");
		 				 statement = conn.createStatement();
		 				
		 				 			 
		 				 sql = "INSERT INTO conflictMessages"+
		 		                   " (sentNode, message,collabName) VALUES  ('"+node.getProperty("canonicalName").toString()+"','"+message+"','"+collabName+"');";
		 			//	 if (DEBUG) System.out.println("SQL: "+sql);
		 				 statement.executeUpdate(sql);
		 			   	   	
		 				
		 			//	  if (DEBUG)  System.out.println("inserted data in conflictMessages...");
		 			      
		 		          
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
	 
	 public String addLineNo(String message)
	 {
		 String newMsg=null;
		 String eName=null;
		 int line=0;
	       if (conn !=null) 
    	   {
    	   	//updateUserActivityTable(con);
    	   Statement statement = null;
 		  String sql = null;
 		
 			 try { 		        	
 				 statement = conn.createStatement();
 				
 				 			 
 				 sql = "select filename, elementName, lineNo from useractivity_"+collabName+" where activitytype='EDIT' and activitytime = (select max(activitytime) from useractivity_"+collabName+");";
 				ResultSet resultSet = statement.executeQuery(sql);
 			   	   	int index=0;
 				while (resultSet.next())
				{
 					eName= resultSet.getString("elementName");
 					line = resultSet.getInt("lineNo");
 					eName= eName.substring(1, eName.length());
 					index= eName.indexOf("(");
 					if (index !=-1) eName= eName.substring(0,index);
 					System.out.println("Index from Conflcit Messages@@@:: "+ eName);
 					if (message.matches("(.*)"+eName+"(.*)"))
 					{
 						if (line !=0 ) newMsg= message+"@LineNo::"+line;
 					}
 					else newMsg= message;
 					
				}
 			      
 		          
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

		 return newMsg;
	 }
	 public void informTransitiveDependencyNodes(Node node, int radiusNode, String msg, String lineNumber)
	 {
		
		 if (radiusNode == 0)
			return;
		else
		{
		 Iterable<Relationship> relations;
		 Node otherNode=null;
		  relations= node.getRelationships(RelTypes.DEPENDENCY);
			for (Relationship r: relations)
			{
				otherNode=r.getOtherNode(node);
				 sendInfo(otherNode,msg, lineNumber);
				 informTransitiveDependencyNodes(otherNode, radiusNode-1, msg, lineNumber);
			}
		}
	 }
	 
	 public void informTransitiveSubNodes(Node node, int radiusNode, String msg, String lineNumber)
	 {
		
		 if (radiusNode == 0)
			return;
		else
		{
		 Iterable<Relationship> relations;
		 Node otherNode=null;
		  relations= node.getRelationships(RelTypes.CONNECTING);
			for (Relationship r: relations)
			{
				otherNode=r.getOtherNode(node);
				 sendInfo(otherNode,msg, lineNumber);
				 informTransitiveSubNodes(otherNode, radiusNode-1, msg, lineNumber);
			}
		}
	 }
	 
	 public void createTableConflictMessages(Connection conn)
	 {
		  Statement statement = null;
	  
		 //create table here
		 try {
	        	
			 if (DEBUG) System.out.println("Creating conflictMessages table in given database...");
			 statement = conn.createStatement();
		      
		      String sql = "CREATE TABLE IF NOT EXISTS conflictMessages"+
		                   "(sentNode VARCHAR(200) not NULL, " +
		                   " message VARCHAR(300), " + " collabName VARCHAR(30), " + 
		                   " messagetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP) "; 
		      
		    //  if (DEBUG) System.out.println("SQL: "+sql);

		      statement.executeUpdate(sql);
		      if (DEBUG) System.out.println("Created table in given database...");
		      
	          
	        } catch (SQLException ex) {
	            // handle any errors
	            System.out.println("SQLException: " + ex.getMessage());
	            System.out.println("SQLState: " + ex.getSQLState());
	            System.out.println("VendorError: " + ex.getErrorCode());
	           
	        }
	    	
	 }
}
