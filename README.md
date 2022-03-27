# Java HTTP Server Framework
## Overview
This repository holds an HTTP-compliant, Java-based web server framework, modeled after the Spark Java framework. Using this framework, developers can write and deploy RESTful API endpoints.
## Getting Started
To run this repository, you must first clone the repo and run:
```
mvn clean install exec:java -Dexec.args="[desired port number] [static file directory root]"
```
The web server will default to port `45555` and static fill directory `./www `
## Defining API Routes
###### Query Parameters
Here is an example of an API GET call that you can define. Note you can pull in query parameters with `request.queryParam(paramName)`. API routes can also contain single-step wildcards using the `*` character.
```
get("/add", ((request, respoonse) -> {
  int x = Integer.parseInt(request.queryParam("x"));
  int y = Integer.parseInt(request.queryParam("y));
  
  int sum = x + y;
  response.type("text/plain");
  return "The sum of the two numbers is " + sum;
  }));
```
In this route, if we send an HTTP GET to http://localhost:45555/add?x=1&y=2, the endpoint would return `The sum of the two numbers is 3`.
###### Path Parameters
The same functionality of the above can be replicated with path parameters.
```
get("/add/:x/:y", ((request, respoonse) -> {
  int x = Integer.parseInt(request.params(":x"));
  int y = Integer.parseInt(request.params(":y"));
  
  int sum = x + y;
  response.type("text/plain");
  return "The sum of the two numbers is " + sum;
  }));
```
## The Request Object
The Request object supports the following common web operations:
```
request.attributes();             // the attributes list
request.attribute("foo");         // value of foo attribute
request.attribute("A", "V");      // sets value of attribute A to V
request.body();                   // request body sent by the client
request.contentLength();          // length of request body
request.contentType();            // content type of request.body
request.cookies();                // request cookies sent by the client
request.headers();                // the HTTP header list
request.headers("BAR");           // value of BAR header
request.host();                   // the host, e.g. "example.com"
request.ip();                     // client IP address
request.params("foo");            // value of foo path parameter
request.params();                 // map with all parameters
request.pathInfo();               // the path info
request.port();                   // the server port
request.protocol();               // the protocol, e.g. HTTP/1.1
request.queryParams();            // the query param list
request.queryParams("FOO");       // value of FOO query param
request.queryParamsValues("FOO")  // all values of FOO query param
request.requestMethod();          // The HTTP method (GET, ..etc)
request.session();                // session management
request.splat();                  // splat (*) parameters
request.uri();                    // the uri, e.g. "http://example.com/foo"
request.url();                    // the url. e.g. "http://example.com/foo"
request.userAgent();              // user agent
```
## The Response Object
The Response object supports the following common web operations:
```
response.body();               // get response content
response.body("Hello");        // sets content to Hello
response.header("FOO", "bar"); // sets header FOO with value bar
response.raw();                // raw response handed in by Jetty
response.redirect("/example"); // browser redirect to /example
response.status();             // get the response status
response.status(401);          // set status code to 401
response.type();               // get the content type
response.type("text/xml");     // set content type to text/xml
```
## Web Sessions
This framework supports sessions for users. Sessions can store values and can be automatically generated for new clients.
```
request.session(true);                     // create and return session
request.session().attribute("user");       // Get session attribute 'user'
request.session().attribute("user","foo"); // Set session attribute 'user'
request.session().removeAttribute("user"); // Remove session attribute 'user'
request.session().attributes();            // Get all session attributes
request.session().id();                    // Get session id    
```
## Error Handling
Upon an error in one of your API routes, you can call the `halt` method to return an error code and error message to the client. By default, halt wil return a 500 error.
```
halt();                // halt 
halt(401);             // halt with status
halt("Body Message");  // halt with message
halt(401, "Go away!"); // halt with status and message
```
## Before and After Filters
You can also specify Routes that execute globally before or after all API requests. Such routes can do things like handle authentication, user permissions, and response formatting. Here's an example of an authentication filter:
```
before((request, response) -> {
    boolean authenticated;
    // ... check if authenticated
    if (!authenticated) {
        halt(401, "You are not welcome here");
    }
});
```
Alternatively, you can specify specific paths that a filter will execute before or after. Note filters execute before all matching request paths, regardless of request type.
```
before("/gandalf/*", (request, response) -> {
    boolean isFrodo;
    // ... check if authenticated
    if (!isFrodo) {
        halt(401, "You shall not pass!");
    }
});
```
