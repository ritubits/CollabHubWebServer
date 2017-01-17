<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<script type="text/javascript">
window.setTimeout(function(){ document.location.reload(true); }, 15000);
</script>

<title>All Collabortors View</title>
</head>
<body>

<b><font face="Arial, Helvetica, sans-serif" ALIGN=CENTER><font size=+2><center>Collaboration Over GitHub: AllCollaborators View</center></font></font></b>

<%@ page language="java" import="org.apache.collab.server.WebClient.*" %>
<%@ page language="java" import="java.util.Enumeration" %>
<%@ page language="java" import="java.util.Vector" %>
<jsp:useBean id="userB" scope="session" class="org.apache.collab.server.WebClient.CollaborationBean" />


<%
String projectName =null;
String ownerName = null;
String userName = null;
String collabName=null;
Vector uNames= new Vector();


projectName= userB.getProjectDetails();

userName = userB.getUserDetails();

if (userName !=null)
{
String[] temp1;
String delimiter1 = "[,]";
temp1 = userName .split(delimiter1);
for(int i =0; i < temp1.length ; i++)
{
System.out.println("i=" + i + temp1[i]);
uNames.add(temp1[i]);
}
				
}	
  Enumeration uName = uNames.elements();
       if (uName.hasMoreElements())
		{
		    	   ownerName = userB.getOwnerName();
		}

 %>

 
     

<!-- active header table -->
<CENTER>
<TABLE WIDTH=30% CELLPADDING=15>
<TR VALIGN=TOP>
<TD VALIGN=TOP > </A>
<TD VALIGN=TOP > 
Project Name:   <% 
System.out.println("ProjectName::"+projectName);
if (projectName==null)
{
	out.print("No active project");
}
else
out.print(projectName); %>
 </TD>
<TD VALIGN=TOP > </A>
</TR>

<TR VALIGN=TOP>
<TD VALIGN=TOP > </A>
<TD VALIGN=TOP > 
Owner Name:   <% 
if (ownerName==null)
{
	out.print("No active collaborator/owner");
}
else
	out.print(ownerName); %>
 </TD>
<TD VALIGN=TOP > </A>
</TR>

</TABLE>

<!-- the main active table -->
<TABLE BORDER=0 WIDTH=20%  BORDER=1 CELLPADDING=10>
<TR VALIGN=TOP>
<TH> Collaborator Names </TH>
</TR>
<FORM METHOD=POST ACTION=displayProjectList.jsp>
<%
	        
	        while (uName.hasMoreElements())
	        {
	        	//System.out.println(uName.nextElement());
%>
    <TR>
    <TD>
    <% 

	collabName= uName.nextElement().toString();
	out.print(collabName); %>

    </TD>
 <% if (!collabName.equals("No data to display"))
 {%>
<td WIDTH="30%"><a href="collabInfo.jsp?pName=<%=projectName%>&cName=<%=collabName%>">View</a></td>

    </TR>
<% } %>
<%
    }
%>
</FORM>
</TABLE>
<BR>



</FORM>
</TABLE>


</body>
</html>