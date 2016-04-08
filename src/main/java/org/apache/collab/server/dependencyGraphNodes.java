package org.apache.collab.server;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Node;
import java.util.HashMap;

public class dependencyGraphNodes {

    public  static enum dGraphNodeType implements Label {
    	PROJECT, PACKAGE, CLASS, INTERFACE, METHOD, ATTRIBUTE;
    }  
    
    Relationship relationship;
    
    public static enum RelTypes implements RelationshipType
    {
    	CONNECTING, DEPENDENCY;
    }
    
    HashMap<Long, String> nodeHashMap = new HashMap<Long, String>();
    HashMap<Long, String> edgeHashMap = new HashMap<Long, String>();
    
    public Node addConnectingClassNode(GraphDatabaseService graphDb, Node pNode, String smallClassName, String className, String imports, String packageName, String modifier)
    {
//    	System.out.println("Creating Class Node::"+className);
    	Node classNode = graphDb.createNode(dGraphNodeType.CLASS);
    	classNode.addLabel(dGraphNodeType.CLASS);
    	classNode.setProperty( "name", smallClassName );
    	classNode.setProperty( "canonicalName", className );
    	classNode.setProperty( "nodeType", "CLASS" );
    	classNode.setProperty( "modifier", modifier );
    	classNode.setProperty( "imports", imports );
    	classNode.setProperty( "packageName", packageName );
//    	System.out.println("cNode Id:"+classNode.getId());
    	nodeHashMap.put(classNode.getId(), className);
    	 
     	relationship = classNode.createRelationshipTo( pNode, RelTypes.CONNECTING );
        relationship.setProperty( "edgeType", "OWNER" ); 
        
        //later set this something else if required //summation of class name and package node name
        relationship.setProperty( "name", classNode.getProperty("canonicalName").toString());
 //       System.out.println("relationship Id:"+relationship.getId());
//        System.out.println("relationship Name:"+relationship.getProperty("name").toString());
        edgeHashMap.put(relationship.getId(), relationship.getProperty("name").toString());
        
        return classNode;
    }
    
    public Node addConnectingInterfaceNode(GraphDatabaseService graphDb, Node pNode, String smallClassName, String interfaceName, String imports, String packageName, String modifier)
    {
//    	System.out.println("Creating Class Node::"+className);
    	Node interfaceNode = graphDb.createNode(dGraphNodeType.INTERFACE);
    	interfaceNode.addLabel(dGraphNodeType.INTERFACE);
    	interfaceNode.setProperty( "name", smallClassName );
    	interfaceNode.setProperty( "canonicalName", interfaceName );
    	interfaceNode.setProperty( "nodeType", "INTERFACE" );
    	interfaceNode.setProperty( "modifier", modifier );
    	interfaceNode.setProperty( "imports", imports );
    	interfaceNode.setProperty( "packageName", packageName );
 //   	System.out.println("cNode Id:"+classNode.getId());
    	nodeHashMap.put(interfaceNode.getId(), interfaceName);
    	 
     	relationship = interfaceNode.createRelationshipTo( pNode, RelTypes.CONNECTING );
        relationship.setProperty( "edgeType", "OWNER" ); 
        
        //later set this something else if required //summation of class name and package node name
        relationship.setProperty( "name", interfaceNode.getProperty("canonicalName").toString());
//        System.out.println("relationship Id:"+relationship.getId());
//        System.out.println("relationship Name:"+relationship.getProperty("name").toString());
        edgeHashMap.put(relationship.getId(), relationship.getProperty("name").toString());
        
        return interfaceNode;
    }
    
    public Node addMethodNode(GraphDatabaseService graphDb, Node cNode, String smallMethodName, String methodName, String modifier, String returnType, String parameterList)
    {
 //   	System.out.println("Creating Method Node::"+methodName);
    	Node mNode = graphDb.createNode(dGraphNodeType.METHOD);
    	mNode.addLabel(dGraphNodeType.METHOD);
    	mNode.setProperty( "name", smallMethodName );
    	mNode.setProperty( "canonicalName", methodName );
    	mNode.setProperty( "nodeType", "METHOD" );
    	mNode.setProperty( "modifier", modifier );
    	mNode.setProperty( "returnType", returnType );  	
    	mNode.setProperty( "parameterList", parameterList );

//    	System.out.println("mNode Id:"+mNode.getId());
    	nodeHashMap.put(mNode.getId(), methodName);
    	
     	relationship = mNode.createRelationshipTo( cNode, RelTypes.CONNECTING );
        relationship.setProperty( "edgeType", "OWNER" );
        relationship.setProperty( "name", methodName+"::"+cNode.getProperty("canonicalName").toString());
        
//        System.out.println("relationship Id:"+relationship.getId());
//        System.out.println("relationship Name:"+relationship.getProperty("name").toString());
    	edgeHashMap.put(relationship.getId(), relationship.getProperty("name").toString());
    	
        return mNode;
    }
    
}
