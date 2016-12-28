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
   HashMap<Long, String> nodeCanonicalHashMap = new HashMap<Long, String>();
   HashMap<Long, String> nodeClassCanonicalHashMap = new HashMap<Long, String>();
    HashMap<Long, String> edgeHashMap = new HashMap<Long, String>();
    
    HashMap<Long, String> methodHashMap = new HashMap<Long, String>();
    HashMap<Long, String> methodEdgeHashMap = new HashMap<Long, String>();
    
    public Node addPackageNode(GraphDatabaseService graphDb, Node rootNode, String packName)
    {
    
//    	System.out.println("Creating Class Node::"+className);
    	Node existingPackageNode= checkPackageNodeExists(graphDb, rootNode,  packName);
    	
    	if (existingPackageNode == null)//package node does not exist- create new
    	{
    	Node packageNode = graphDb.createNode(dGraphNodeType.PACKAGE);
    	packageNode.addLabel(dGraphNodeType.PACKAGE);
    	packageNode.setProperty( "name", packName );
    	packageNode.setProperty( "canonicalName", packName );
    	packageNode.setProperty( "nodeType", "PACKAGE" );


    	nodeHashMap.put(packageNode.getId(), packName);//adding canonical name
    	 
     	relationship = packageNode.createRelationshipTo( rootNode, RelTypes.CONNECTING );
        relationship.setProperty( "edgeType", "OWNER" ); 
        
        //later set this something else if required //summation of class name and package node name
        relationship.setProperty( "name", packageNode.getProperty("canonicalName").toString());
 //       System.out.println("relationship Id:"+relationship.getId());
//        System.out.println("relationship Name:"+relationship.getProperty("name").toString());
        edgeHashMap.put(relationship.getId(), relationship.getProperty("name").toString());//adding canonical name
        
        return packageNode;
    	}
    	else return existingPackageNode;
    }
    
    public Node addConnectingClassNode(GraphDatabaseService graphDb, Node pNode, String smallClassName, String className, String imports, String packageName, String modifier, String extend, String implemented, int lineNumber)
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
    	classNode.setProperty( "lineNumber", lineNumber );
    	if (extend!=null)  	classNode.setProperty( "extends", extend );
    	else classNode.setProperty( "extends", "null" );
    	classNode.setProperty( "implements", implemented );
//    	System.out.println("cNode Id:"+classNode.getId());
    	nodeHashMap.put(classNode.getId(), smallClassName);
    	nodeCanonicalHashMap.put(classNode.getId(), className);//adding canonical name
    	
    	//for classHashMap
    	nodeClassCanonicalHashMap.put(classNode.getId(), className);//adding canonical name
     	relationship = classNode.createRelationshipTo( pNode, RelTypes.CONNECTING );
        relationship.setProperty( "edgeType", "OWNER" ); 
        
        //later set this something else if required //summation of class name and package node name
        relationship.setProperty( "name", classNode.getProperty("canonicalName").toString());
 //       System.out.println("relationship Id:"+relationship.getId());
//        System.out.println("relationship Name:"+relationship.getProperty("name").toString());
        edgeHashMap.put(relationship.getId(), relationship.getProperty("name").toString());//adding canonical name
        
        return classNode;
    }
    
    public Node addConnectingInterfaceNode(GraphDatabaseService graphDb, Node pNode, String smallClassName, String interfaceName, String imports, String packageName, String modifier, int lineNumber)
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
    	interfaceNode.setProperty( "lineNumber", lineNumber );
 //   	System.out.println("cNode Id:"+classNode.getId());
    	nodeHashMap.put(interfaceNode.getId(), smallClassName);
    	nodeCanonicalHashMap.put(interfaceNode.getId(), interfaceName);
    	
    	//for classHashMap
    	nodeClassCanonicalHashMap.put(interfaceNode.getId(), interfaceName);//adding canonical name
    	
     	relationship = interfaceNode.createRelationshipTo( pNode, RelTypes.CONNECTING );
        relationship.setProperty( "edgeType", "OWNER" ); 
        
        //later set this something else if required //summation of class name and package node name
        relationship.setProperty( "name", interfaceNode.getProperty("canonicalName").toString());
//        System.out.println("relationship Id:"+relationship.getId());
//        System.out.println("relationship Name:"+relationship.getProperty("name").toString());
        edgeHashMap.put(relationship.getId(), relationship.getProperty("name").toString());
        
        return interfaceNode;
    }
    
    public Node addMethodNode(GraphDatabaseService graphDb, Node cNode, String smallMethodName, String methodName, String modifier, String returnType, String parameterList, String body, int lineNumber)
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
    	mNode.setProperty( "lineNumber", lineNumber );
    	//System.out.println("Method ")
    	mNode.setProperty( "body", body );
    	
//    	System.out.println("mNode Id:"+mNode.getId());
    	nodeHashMap.put(mNode.getId(), smallMethodName);
    	nodeCanonicalHashMap.put(mNode.getId(), methodName);
     	relationship = mNode.createRelationshipTo( cNode, RelTypes.CONNECTING );
        relationship.setProperty( "edgeType", "OWNER" );
        relationship.setProperty( "name", methodName+"::"+cNode.getProperty("canonicalName").toString());
        
//        System.out.println("relationship Id:"+relationship.getId());
//        System.out.println("relationship Name:"+relationship.getProperty("name").toString());
    	edgeHashMap.put(relationship.getId(), relationship.getProperty("name").toString());
    	
        return mNode;
    }
    
    public Node addAttributeNode(GraphDatabaseService graphDb, Node cNode, String smallAttributeName, String attributeName, String modifier, String dataType, String initializer, int lineno)
    {
 //   	System.out.println("Creating Method Node::"+methodName);
    	Node aNode = graphDb.createNode(dGraphNodeType.ATTRIBUTE);
    	aNode.addLabel(dGraphNodeType.ATTRIBUTE);
    	aNode.setProperty( "name", smallAttributeName );
    	aNode.setProperty( "canonicalName", attributeName );
    	aNode.setProperty( "nodeType", "ATTRIBUTE" );
    	aNode.setProperty( "modifier", modifier );
    	aNode.setProperty( "dataType", dataType );  
    	aNode.setProperty( "lineNumber", lineno );  
    	if (initializer !=null)    	aNode.setProperty( "initializer", initializer );
    	else aNode.setProperty( "initializer", "null" );
    	
    	nodeHashMap.put(aNode.getId(), smallAttributeName);//adding canonical name
    	nodeCanonicalHashMap.put(aNode.getId(), attributeName);//adding canonical name
    	
     	relationship = aNode.createRelationshipTo( cNode, RelTypes.CONNECTING );
     
        relationship.setProperty( "edgeType", "OWNER" );
        relationship.setProperty( "name", attributeName+"::"+cNode.getProperty("canonicalName").toString());
        
//        System.out.println("relationship Id:"+relationship.getId());
//        System.out.println("relationship Name:"+relationship.getProperty("name").toString());
    	edgeHashMap.put(relationship.getId(), relationship.getProperty("name").toString());
    	
        return aNode;
    }
    
    public Node addVariableDeclarationNode(GraphDatabaseService graphDb, Node mNode, String smallAttributeName, String attributeName, String modifier, String dataType, String initializer, int lineNo)
    {
 //   	System.out.println("Creating Method Node::"+methodName);
    	Node aNode = graphDb.createNode(dMethodNodeType.VariableDeclarationNode);
    	aNode.addLabel(dMethodNodeType.VariableDeclarationNode);
    	aNode.setProperty( "name", smallAttributeName );
    	aNode.setProperty( "canonicalName", attributeName );
    	aNode.setProperty( "nodeType", "METHOD-ATTRIBUTE" );
    	aNode.setProperty( "modifier", modifier );
    	aNode.setProperty( "dataType", dataType ); 
    	aNode.setProperty( "lineNumber", lineNo );
    	if (initializer !=null)    	aNode.setProperty( "initializer", initializer );
    	else aNode.setProperty( "initializer", "null" );
    //	aNode.setProperty( "initializer", initializer );
    	
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
    
    public void addImportsDependencyEdge(GraphDatabaseService graphDb, Long importID, Long classID)    
    {
    	//adding edge from superclass id to sub class it
    	Node classNode =	graphDb.getNodeById(classID);
    	Node importClassNode =	graphDb.getNodeById(importID);
    //	System.out.println("Adding edge from::"+subClassNode.getProperty("name")+"to"+interfaceNode.getProperty("name"));
    	    	
     	relationship = classNode.createRelationshipTo(importClassNode, RelTypes.DEPENDENCY );
        relationship.setProperty( "edgeType", "IMPORTS" );
        relationship.setProperty( "name", classNode.getProperty("canonicalName").toString()+"::"+importClassNode.getProperty("canonicalName").toString());        
    	edgeHashMap.put(relationship.getId(), relationship.getProperty("name").toString());
    	
    }
    
    public void addDependencyEdge(GraphDatabaseService graphDb, Long superClassID, Long subClassID, String edgeType)    
    {
    	//adding edge from superclass id to sub class it
    	Node subClassNode =	graphDb.getNodeById(subClassID);
    	Node superClassNode =	graphDb.getNodeById(superClassID);
    	
    	//returns true if node exists
    	Boolean exists= checkDependencyEdgeExists(graphDb, subClassNode,superClassNode, edgeType );
   	
    	 
    	if (!exists)
    	{
    	//	System.out.println("Adding "+ edgeType+" edge from:: "+subClassNode.getProperty("name")+" to "+superClassNode.getProperty("name"));
	     	relationship = subClassNode.createRelationshipTo(superClassNode, RelTypes.DEPENDENCY );
	        relationship.setProperty( "edgeType", edgeType );
	        relationship.setProperty( "name", subClassNode.getProperty("canonicalName").toString()+"::"+superClassNode.getProperty("canonicalName").toString());        
	    	edgeHashMap.put(relationship.getId(), relationship.getProperty("name").toString());
    	}
    	
    }
    
    public boolean checkDependencyEdgeExists(GraphDatabaseService graphDb, Node subClassNode, Node superClassNode, String edgeType )
    {
    	Iterable<Relationship> relations;
    	relations=subClassNode.getRelationships(RelTypes.DEPENDENCY);
    	Long otherClassID = (long) -1;
    	for (Relationship r: relations)
		{
    		otherClassID=r.getOtherNode(subClassNode).getId();
    		if (otherClassID == superClassNode.getId() && (r.getProperty("edgeType") == edgeType))
    		{
    			//edge exists
    			return true;
    		}
		}
    	return false;
    }
    
    public Node checkPackageNodeExists(GraphDatabaseService graphDb, Node rootNode,  String packName)
    {
    	Node pNode= graphDb.findNode(dGraphNodeType.PACKAGE, "name", packName);    	
    	 return pNode;
    }
    
    public Node createIndependentClassNode(GraphDatabaseService graphDb, String smallClassName, String className, String imports, String packageName, String modifier, String extend, String implemented, int lineNumber)
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
    	classNode.setProperty( "lineNumber", lineNumber );
    	if (extend!=null)  	classNode.setProperty( "extends", extend );
    	else classNode.setProperty( "extends", "null" );
    	classNode.setProperty( "implements", implemented );
//    	System.out.println("cNode Id:"+classNode.getId());
    	nodeHashMap.put(classNode.getId(), smallClassName);//not adding canonical name
  //  	nodeCanonicalHashMap.put(classNode.getId(), className);
        
        return classNode;
    }
    
    public Node createIndependentInterfaceNode(GraphDatabaseService graphDb, String smallClassName, String interfaceName, String imports, String packageName, String modifier, int lineNumber)
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
    	interfaceNode.setProperty( "lineNumber", lineNumber );
 //   	System.out.println("cNode Id:"+classNode.getId());
    	nodeHashMap.put(interfaceNode.getId(), smallClassName);
  //  	nodeCanonicalHashMap.put(interfaceNode.getId(), interfaceName);
        
        return interfaceNode;
    }
    public HashMap getNodeHashMap()
    {
    	return nodeHashMap;
    }
    
    
   public HashMap getCanonicalNodeHashMap()
    {
    	return nodeCanonicalHashMap;
    }
    
   public HashMap getClassCanonicalNodeHashMap()
   {
   	return nodeClassCanonicalHashMap;
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
