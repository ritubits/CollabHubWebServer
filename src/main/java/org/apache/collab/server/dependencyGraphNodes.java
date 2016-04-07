package org.apache.collab.server;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Node;

public class dependencyGraphNodes {

    public  static enum dGraphNodeType implements Label {
    	PROJECT, PACKAGE, CLASS, METHOD, ATTRIBUTE;
    }  
    
    Relationship relationship;
    
    public static enum RelTypes implements RelationshipType
    {
    	CONNECTING, DEPENDENCY;
    }
    
    public Node addConnectingClassNode(GraphDatabaseService graphDb, Node pNode, String className, String accessSpecifier, String finalType, String implType, String staticType)
    {
    	Node classNode = graphDb.createNode(dGraphNodeType.CLASS);
    	classNode.addLabel(dGraphNodeType.CLASS);
    	classNode.setProperty( "name", className );
    	classNode.setProperty( "nodeType", "CLASS" );
    	classNode.setProperty( "accessSpecifier", accessSpecifier );
    	classNode.setProperty( "finalType", finalType );
    	classNode.setProperty( "implType", implType );
    	classNode.setProperty( "staticType", staticType );
     	relationship = classNode.createRelationshipTo( pNode, RelTypes.CONNECTING );
        relationship.setProperty( "edgeType", "OWNER" );        
        return classNode;
    }
    
    public Node addMethodNode(GraphDatabaseService graphDb, Node cNode, String methodName, String modifier, String returnType, String parameterList)
    {
    	Node mNode = graphDb.createNode(dGraphNodeType.METHOD);
    	mNode.addLabel(dGraphNodeType.METHOD);
    	mNode.setProperty( "name", methodName );
    	mNode.setProperty( "nodeType", "METHOD" );
    	mNode.setProperty( "modifier", modifier );
    	mNode.setProperty( "returnType", returnType );  	
    	mNode.setProperty( "parameterList", parameterList );
     	relationship = mNode.createRelationshipTo( cNode, RelTypes.CONNECTING );
        relationship.setProperty( "edgeType", "OWNER" );
        return mNode;
    }
    
}
