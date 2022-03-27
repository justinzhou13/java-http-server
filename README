#Java HTTP Server Framework
##Overview
This repository holds an HTTP-compliant, Java-based web server framework, modeled after the Spark Java framework. Using this framework, developers can write and deploy RESTful API endpoints.
##Getting Started
To run this repository, you must first clone the repo and run:
```
mvn clean install exec:java -Dexec.args="[desired port number] [static file directory root]"
```
The web server will default to port `45555` and static fill directory `./www `
##Defining Routes
Here is an example of an API GET call that you can define
```
get("/add", ((request, respoonse) -> {
  int x = Integer.parseInt(request.queryParam("x"));
  int y = Integer.parseInt(request.queryParam("y));
  
  int sum = x + y;
  response.type("text/plain");
  return "The sum of the two numbers is " + sum;
  }));
```
