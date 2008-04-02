<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    <title>UIPanel</title>
    <link rel="stylesheet" type="text/css"
       href='<%= request.getContextPath() + "/stylesheet.css" %>'>
  </head>

  <body>

    <h1>UIPanel</h1>

    <h3>$Id: UIPanel.jsp,v 1.7 2002/11/12 22:51:41 jhorwat Exp $</h3>

    <%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
    <%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt" %>
    <%@ taglib uri="http://java.sun.com/jsf/html_basic" prefix="h" %>

     <fmt:setBundle basename="basic.Resources" scope="session" 
                    var="basicBundle"/>

     <jsp:useBean id="LoginBean" class="basic.LoginBean" scope="session" />
     <jsp:useBean id="ListBean" class="basic.ListBean" scope="session" />

     <f:use_faces>  

       <p>Form is rendered after this.</p>
     
       <h:form id="standardRenderKitForm" 
                   formName="standardRenderKitForm">

         <h:command_button id="standardRenderKitSubmit" 
             commandName="standardRenderKitSubmit"
             key="standardRenderKitSubmitLabel"
             bundle="basicBundle">
         </h:command_button>

         <table width="100%" border="1" cellpadding="3" cellspacing="3">

<!-- Each included page should have table rows for the appropriate widget. -->

           <%@ include file="table_header.jsp" %>

           <%@ include file="panel_list_row.jsp" %>

           <%@ include file="panel_grid_row.jsp" %>

         </table>

         <h:command_button id="standardRenderKitSubmit" 
             commandName="standardRenderKitSubmit"
             key="standardRenderKitSubmitLabel"
             bundle="basicBundle">
         </h:command_button>

       </h:form>

     </f:use_faces>   


  </body>
</html>
