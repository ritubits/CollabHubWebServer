package org.apache.collab.server.Comparator;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CompareGraphsServlet extends HttpServlet {

	 public void doGet(HttpServletRequest request, HttpServletResponse response)
			    throws IOException, ServletException
			    {
			    
		 CompareGraphs db= new CompareGraphs();
		 db.initializeDB("CollabClient");
			    }
}
