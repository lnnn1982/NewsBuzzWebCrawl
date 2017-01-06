<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<jsp:directive.page import="com.NewBuzz.Spider.*"/>    
<jsp:directive.page import="java.util.ArrayList"/> 

<% 
    ResultUrlCollection clintonRst = (ResultUrlCollection)request.getAttribute("clintonRst");
    ResultUrlCollection trumpRst = (ResultUrlCollection)request.getAttribute("trumpRst");
%>  
    
<!DOCTYPE html>
<html lang="en">
<head>
  <title>Clinton VS Trump</title>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">
  <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.1.1/jquery.min.js"></script>
  <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js"></script>
  <style>
    /* Add a gray background color and some padding to the footer */
    body {
      font: 17px "Montserrat", sans-serif;
      line-height: 2.5;
      color: #CC0033;
    }

    a {
      color: #CC6600;
    }
    
    p {
      font: 23px "Montserrat", sans-serif;
    }
    
    .bg-1 { 
      background-image: url(res/trump-vs-clinton-crazy-face.jpg);
      
      background-repeat: no-repeat;
      background-attachment: fixed;
      background-size: contain;



    }
    
    .bg-2 { 
      background-image: url(res/white-house1.jpg);
      
      background-repeat: no-repeat;
      background-attachment: fixed;
      background-size: cover;
    }
    

  </style>
</head>

<body>
<div class="container-fluid">   
  <div class="row  bg-1">
    <br/><br/>
    <h1 class="text-center">Trump VS Clinton</h1>
    <br/><br/>
  </div>
  
  <div class="row bg-2">
    <div class="col-sm-6">
        <p>
          <br/>
          Trump CNN News
        </p>
        
       <% 
        ArrayList<ResultUrlCollection.ResultUrl> trumpUrls = trumpRst.getResultUrls();
        for (ResultUrlCollection.ResultUrl oneTrumpUrl : trumpUrls) {
        	String url = oneTrumpUrl.getFullUrl();
        	String title = oneTrumpUrl.getTitle();
       %>
       	
        <div> 
            <a href="<%= url %>"><%= title %></a>
        </div>
        	
       <% 	
        }
       %>

    </div>
    <div class="col-sm-6"> 
        <p>
          <br/>
          Clinton CNN News
        </p>
        <% 
        ArrayList<ResultUrlCollection.ResultUrl> clintonUrls = clintonRst.getResultUrls();
        for (ResultUrlCollection.ResultUrl oneClitonUrl : clintonUrls) {
            String url = oneClitonUrl.getFullUrl();
            String title = oneClitonUrl.getTitle();
       %>
        
        <div> 
            <a class="text-left" href="<%= url %>"><%= title %></a>
        </div>
            
       <%   
        }
       %>
        
        <br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/>
   
    </div>
  </div>
</div>

</body>
</html>
