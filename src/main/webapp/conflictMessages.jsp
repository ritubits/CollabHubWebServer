<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<script type="text/javascript">
window.setTimeout(function(){ document.location.reload(true); }, 15000);
</script>
<title>All Conflicts View </title>
</head>
<body>
<b><font face="Arial, Helvetica, sans-serif" ALIGN=CENTER><font size=+2><center>Collaboration Over GitHub: AllConflicts View</center></font></font></b>

<%@ page language="java" import="org.apache.collab.server.WebClient.*" %>
<%@ page language="java" import="java.util.Enumeration" %>
<%@ page language="java" import="java.util.Vector" %>
<jsp:useBean id="userConflict" scope="session" class="org.apache.collab.server.WebClient.ConflictsBean" />


<%
String artifactName =null;
Vector aNames= new Vector();

String messageName = null;
Vector mNames= new Vector();

userConflict.getConflictMessageDetails();
artifactName = userConflict.getArtifactNames();
messageName = userConflict.getConflictMessages();

if (messageName !=null)
{
String[] temp1;
String delimiter1 = "[,]";
temp1 = messageName.split(delimiter1);
for(int i =0; i < temp1.length ; i++)
{
System.out.println("i=" + i + temp1[i]);
mNames.add(temp1[i]);
}
				
}	
  Enumeration mName = mNames.elements();

if (artifactName !=null)
{
String[] temp1;
String delimiter1 = "[,]";
temp1 = artifactName .split(delimiter1);
for(int i =0; i < temp1.length ; i++)
{
System.out.println("i=" + i + temp1[i]);
aNames.add(temp1[i]);
}
				
}	
  Enumeration aName = aNames.elements();


 %>

 
     

<!-- active header table -->
<CENTER>
<TABLE WIDTH=30% CELLPADDING=15>

<TR VALIGN=TOP>
<TD VALIGN=TOP > </A>
<TD VALIGN=TOP > 
<center>Conflict Messages  </center> 
 </TD>
<TD VALIGN=TOP > </A>
</TR>

</TABLE>

<!-- the main active table -->
<TABLE WIDTH=100%  BORDER=1 CELLPADDING=10>
<TR VALIGN=TOP>
<td WIDTH="30%">Artifact Name</td>
<td WIDTH="70%">Message</td>
</TR>

<%
	        
	        while (aName.hasMoreElements() && mName.hasMoreElements())
	        {
%>
    <TR>
    <TD WIDTH="30%">
    <% out.print(aName.nextElement()); %>

    </TD>
	
 <td WIDTH="70%">

<FONT COLOR="#990000">   <% out.print(mName.nextElement()); %>
</FONT>
   </td>

    </TR>

<%
    }
%>

</TABLE>
<BR>




</BODY>

</html>