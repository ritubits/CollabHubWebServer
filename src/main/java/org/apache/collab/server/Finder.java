package org.apache.collab.server;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.*;

import static java.nio.file.FileVisitResult.*;

public class Finder extends SimpleFileVisitor<Path> {
    private int numMatches = 0;
    CreateDependencyGraph dSererGraph=null;

    Finder(CreateDependencyGraph dGraph)
    {
    	dSererGraph= dGraph;
    }
    
    void find(Path file) {
        Path name = file.getFileName();
     //   System.out.println(file);
        if (name != null && name.toString().contains(".java")) {
            numMatches++;
            System.out.println(file);
            try {
				dSererGraph.createConnectingGraph(file.toFile());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    }

    // Prints the total number of
    // matches to standard out.
    void done() {
        System.out.println("Matched: "+ numMatches);
    }

    // Invoke the pattern matching
    // method on each file.
    @Override
    public FileVisitResult visitFile(Path file,
            BasicFileAttributes attrs) {
        find(file);
        return CONTINUE;
    }

    // Invoke the pattern matching
    // method on each directory.

    public FileVisitResult preVisitDirectory(Path dir,
            BasicFileAttributes attrs) {
        find(dir);
        return CONTINUE;
    }

    public FileVisitResult visitFileFailed(Path file,
            IOException exc) {
        System.err.println(exc);
        return CONTINUE;
    }
}