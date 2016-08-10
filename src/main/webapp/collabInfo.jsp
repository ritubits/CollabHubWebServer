<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<script type="text/javascript">
window.setTimeout(function(){ document.location.reload(true); }, 15000);
</script>

<title>CollaboratorInfo View</title>
</head>
<BODY >
<b><font face="Arial, Helvetica, sans-serif" ALIGN=CENTER><font size=+2><center>Collaboration Over GitHub: CollaboratorInfo View</center></font></font></b>

<%@ page language="java" import="org.apache.collab.server.WebClient.*" %>
<%@ page language="java" import="java.util.Enumeration" %>
<%@ page language="java" import="java.util.Vector" %>
<jsp:useBean id="InfoBean" scope="session" class="org.apache.collab.server.WebClient.CollabInfoBean" />

<%
String projectName =request.getParameter("pName");
String collabName =request.getParameter("cName");


String artifactName =null;
Vector aNames= new Vector();

String messageName = null;
Vector mNames= new Vector();

InfoBean.getActivtyData(collabName);
artifactName = InfoBean.getActivityArtifact();
messageName = InfoBean.getActivityType();

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
Project Name:   <% 
System.out.println("ProjectName::"+projectName);
out.print(projectName); %>
 </TD>
<TD VALIGN=TOP > </A>
</TR>

<TR VALIGN=TOP>
<TD VALIGN=TOP > </A>
<TD VALIGN=TOP > 
Collaborator Name:   <% 
System.out.println("CollaboratorName::"+collabName);
out.print(collabName); %>
 </TD>
<TD VALIGN=TOP > </A>
</TR>

<TR VALIGN=TOP>
<TD VALIGN=TOP > </A>
<TD VALIGN=TOP > 
Collaborator Activity Data  
 </TD>
<TD VALIGN=TOP > </A>
</TR>

</TABLE>

<!-- the main active table -->
<TABLE WIDTH=30% BGCOLOR=#CCCCCC BORDER=1 CELLPADDING=10>
<TR VALIGN=TOP>
<td WIDTH="70%"><center>Activity Artifact Name</center></td>
<td WIDTH="30%"><center>Activity Mode</center></td>

</TR>

<FORM METHOD=POST ACTION=displayProjectList.jsp>
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
</FORM>
</TABLE>
<BR>



</FORM>
</TABLE>
<%
artifactName =null;
aNames= new Vector();

messageName = null;
mNames= new Vector();

InfoBean.getConflictMessageDetails(collabName);
artifactName = InfoBean.getArtifactNames();
messageName = InfoBean.getConflictMessages();

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
  mName = mNames.elements();

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
  aName = aNames.elements();


 %>

 
     

<!-- active header table -->
<CENTER>
<TABLE WIDTH=30% CELLPADDING=15>

<TR VALIGN=TOP>
<TD VALIGN=TOP > </A>
<TD VALIGN=TOP > 
<center>Targeted Conflict Messages  </center> 
 </TD>
<TD VALIGN=TOP > </A>
</TR>

</TABLE>

<!-- the main active table -->
<TABLE WIDTH=100% BGCOLOR=#FFFFCC BORDER=1 CELLPADDING=10>
<TR VALIGN=TOP>
<td WIDTH="30%"><center>Artifact Name</center></td>
<td WIDTH="70%"><center>Message</center></td>
</TR>

<FORM METHOD=POST ACTION=displayProjectList.jsp>
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
</FORM>
</TABLE>
<BR>



</FORM>
</TABLE>



</BODY>

</html>