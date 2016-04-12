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
    
    public  static enum dMethodNodeType implements Label {
    	VariableDeclarationNode, PACKAGE;
    }  
    
    Relationship relationship;
    
    public static enum RelTypes implements RelationshipType
    {
    	CONNECTING, DEPENDENCY;
    }
    
    public static enum methodRelTypes implements RelationshipType
    {
    	BODY;
    }
    
    HashMap<Long, String> nodeHashMap = new HashMap<Long, String>();
    HashMap<Long, String> edgeHashMap = new HashMap<Long, String>();
    
    HashMap<Long, String> methodHashMap = new HashMap<Long, String>();
    HashMap<Long, String> methodEdgeHashMap = new HashMap<Long, String>();
    
    public Node addConnectingClassNode(GraphDatabaseService graphDb, Node pNode, String smallClassName, String className, String imports, String packageName, String modifier, String extend, String implemented)
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
    	classNode.setProperty( "extends", extend );
    	classNode.setProperty( "implements", implemented );
//    	System.out.println("cNode Id:"+classNode.getId());
    	nodeHashMap.put(classNode.getId(), smallClassName);//adding canonical name
    	 
     	relationship = classNode.createRelationshipTo( pNode, RelTypes.CONNECTING );
        relationship.setProperty( "edgeType", "OWNER" ); 
        
        //later set this something else if required //summation of class name and package node name
        relationship.setProperty( "name", classNode.getProperty("canonicalName").toString());
 //       System.out.println("relationship Id:"+relationship.getId());
//        System.out.println("relationship Name:"+relationship.getProperty("name").toString());
        edgeHashMap.put(relationship.getId(), relationship.getProperty("name").toString());//adding canonical name
        
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
    	nodeHashMap.put(interfaceNode.getId(), smallClassName);
    	 
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
    	nodeHashMap.put(mNode.getId(), smallMethodName);
    	
     	relationship = mNode.createRelationshipTo( cNode, RelTypes.CONNECTING );
        relationship.setProperty( "edgeType", "OWNER" );
        relationship.setProperty( "name", methodName+"::"+cNode.getProperty("canonicalName").toString());
        
//        System.out.println("relationship Id:"+relationship.getId());
//        System.out.println("relationship Name:"+relationship.getProperty("name").toString());
    	edgeHashMap.put(relationship.getId(), relationship.getProperty("name").toString());
    	
        return mNode;
    }
    
    public Node addAttributeNode(GraphDatabaseService graphDb, Node cNode, String smallAttributeName, String attributeName, String modifier, String dataType, String initializer)
    {
 //   	System.out.println("Creating Method Node::"+methodName);
    	Node aNode = graphDb.createNode(dGraphNodeType.ATTRIBUTE);
    	aNode.addLabel(dGraphNodeType.ATTRIBUTE);
    	aNode.setProperty( "name", smallAttributeName );
    	aNode.setProperty( "canonicalName", attributeName );
    	aNode.setProperty( "nodeType", "ATTRIBUTE" );
    	aNode.setProperty( "modifier", modifier );
    	aNode.setProperty( "dataType", dataType );  	
    	aNode.setProperty( "initializer", initializer );
    	
    	nodeHashMap.put(aNode.getId(), smallAttributeName);//adding canonical name
    	
     	relationship = aNode.createRelationshipTo( cNode, RelTypes.CONNECTING );
     
        relationship.setProperty( "edgeType", "OWNER" );
        relationship.setProperty( "name", attributeName+"::"+cNode.getProperty("canonicalName").toString());
        
//        System.out.println("relationship Id:"+relationship.getId());
//        System.out.println("relationship Name:"+relationship.getProperty("name").toString());
    	edgeHashMap.put(relationship.getId(), relationship.getProperty("name").toString());
    	
        return aNode;
    }
    
    public Node addVariableDeclarationNode(GraphDatabaseService graphDb, Node mNode, String smallAttributeName, String attributeName, String modifier, String dataType, String initializer)
    {
 //   	System.out.println("Creating Method Node::"+methodName);
    	Node aNode = graphDb.createNode(dMethodNodeType.VariableDeclarationNode);
    	aNode.addLabel(dMethodNodeType.VariableDeclarationNode);
    	aNode.setProperty( "name", smallAttributeName );
    	aNode.setProperty( "canonicalName", attributeName );
    	aNode.setProperty( "nodeType", "METHOD-ATTRIBUTE" );
    	aNode.setProperty( "modifier", modifier );
    	aNode.setProperty( "dataType", dataType );  	
    	aNode.setProperty( "initializer", initializer );
    	
    	methodHashMap.put(aNode.getId(), smallAttributeName);//adding canonical name
    	
     	relationship = aNode.createRelationshipTo( mNode, methodRelTypes.BODY );
     
        relationship.setProperty( "edgeType", "BODY" );
        relationship.setProperty( "name", attributeName+"::"+mNode.getProperty("canonicalName").toString());
        
//        System.out.println("relationship Id:"+relationship.getId());
//        System.out.println("relationship Name:"+relationship.getProperty("name").toString());
    	methodEdgeHashMap.put(relationship.getId(), relationship.getProperty("name").toString());
    	
        return aNode;
    }
    
    public void addExtendsDependencyEdge(GraphDatabaseService graphDb, Long superClassID, Long subClassID)    
    {
    	//adding edge from superclass id to sub class it
    	Node subClassNode =	graphDb.getNodeById(subClassID);
    	Node superClassNode =	graphDb.getNodeById(superClassID);
   // 	System.out.println("Adding edge from::"+subClassNode.getProperty("name")+"to"+superClassNode.getProperty("name"));
    	    	
     	relationship = subClassNode.createRelationshipTo(superClassNode, RelTypes.DEPENDENCY );
        relationship.setProperty( "edgeType", "EXTENDS" );
        relationship.setProperty( "name", subClassNode.getProperty("canonicalName").toString()+"::"+superClassNode.getProperty("canonicalName").toString());        
    	edgeHashMap.put(relationship.getId(), relationship.getProperty("name").toString());
    	
    }
    
    public void addImplementsDependencyEdge(GraphDatabaseService graphDb, Long interfaceID, Long subClassID)    
    {
    	//adding edge from superclass id to sub class it
    	Node subClassNode =	graphDb.getNodeById(subClassID);
    	Node interfaceNode =	graphDb.getNodeById(interfaceID);
    //	System.out.println("Adding edge from::"+subClassNode.getProperty("name")+"to"+interfaceNode.getProperty("name"));
    	    	
     	relationship = subClassNode.createRelationshipTo(interfaceNode, RelTypes.DEPENDENCY );
        relationship.setProperty( "edgeType", "IMPLEMENTS" );
        relationship.setProperty( "name", subClassNode.getProperty("canonicalName").toString()+"::"+interfaceNode.getProperty("canonicalName").toString());        
    	edgeHashMap.put(relationship.getId(), relationship.getProperty("name").toString());
    	
    }
    
    public void addUsesDependencyEdge(GraphDatabaseService graphDb, Long superClassID, Long subClassID)    
    {
    	//adding edge from superclass id to sub class it
    	Node subClassNode =	graphDb.getNodeById(subClassID);
    	Node superClassNode =	graphDb.getNodeById(superClassID);
   // 	System.out.println("Adding edge from::"+subClassNode.getProperty("name")+"to"+superClassNode.getProperty("name"));
    	    	
     	relationship = subClassNode.createRelationshipTo(superClassNode, RelTypes.DEPENDENCY );
        relationship.setProperty( "edgeType", "USES" );
        relationship.setProperty( "name", subClassNode.getProperty("canonicalName").toString()+"::"+superClassNode.getProperty("canonicalName").toString());        
    	edgeHashMap.put(relationship.getId(), relationship.getProperty("name").toString());
    	
    }
    
    public HashMap getNodeHashMap()
    {
    	return nodeHashMap;
    }
    
    public void addToNodeHashMap()
    {
    	
    }
    
    public HashMap getEdgeHashMap()
    {
    	return edgeHashMap;
    }
    
    public void addToEdgeHashMap()
    {
    	
    }
    
}
