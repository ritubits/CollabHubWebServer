<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<script type="text/javascript">
window.setTimeout(function(){ document.location.reload(true); }, 15000);
</script>
<title>Live Preview View</title>
</head>
<body>

<b><font face="Arial, Helvetica, sans-serif" ALIGN=CENTER><font size=+2><center>Collaboration Over GitHub: Live Preview View</center></font></font></b>

<%@ page language="java" import="org.apache.collab.server.WebClient.*" %>
<%@ page language="java" import="java.util.Enumeration" %>
<%@ page language="java" import="java.util.Vector" %>
<jsp:useBean id="liveP" scope="session" class="org.apache.collab.server.WebClient.LivePreviewBean" />


<%
String projectName =request.getParameter("pName");
String collabName =request.getParameter("cName");
liveP.setProjectName(projectName);
liveP.setCollabName(collabName);
String content=liveP.readCollaboratorEditFile();
%>

<!-- active header table -->
<CENTER>
<TABLE WIDTH=30% CELLPADDING=15>
<TR VALIGN=TOP>
<TD VALIGN=TOP > </A>
<TD VALIGN=TOP > 
Project Name:   <% 

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
Collaborator Name:   <% 
if (collabName==null)
{
	out.print("No active collaborator");
}
else
	out.print(collabName); %>
 </TD>
<TD VALIGN=TOP > </A>
</TR>

</TABLE>
</CENTER>

<br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
<br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
<br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;

<div  style="height:500px;width:500px;overflow:scroll;background-color:#F0F8FF;">

<pre><font size="2" face="Courier New" ><%=content %> 
</font></pre>
</div>




</body>
</html>