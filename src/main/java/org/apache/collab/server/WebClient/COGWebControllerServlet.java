package org.apache.collab.server.WebClient;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class COGWebControllerServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    String DEBUG="TRUE";

    public COGWebControllerServlet() {
        super();

    }


	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

	 	response.setContentType("text/html");
        PrintWriter out = response.getWriter();
  
        if (DEBUG.equals("TRUE")) System.out.println(" In the COGWebControllerServlet ");  
        
		String requestName = request.getParameter("requestName");

		RequestDispatcher rd = null;
        if (DEBUG.equals("TRUE")) System.out.println("requestName:: "+requestName);  
        
		if (requestName.equals("AllCollaboratorsView"))
		{
	        if (DEBUG.equals("TRUE")) System.out.println("Dispatching request to userDetails");  
	        
	        rd = request.getRequestDispatcher("userDetails.jsp");
		}
		else
		{
			if (requestName.equals("AllConflictsView"))
			{
		        if (DEBUG.equals("TRUE")) System.out.println("Dispatching request to userDetails");  
		        
		        rd = request.getRequestDispatcher("conflictMessages.jsp");
			}
		}
		rd.forward(request, response);
			
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

	}

}
