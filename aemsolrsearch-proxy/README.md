Solr Search Proxy
=====================

Solr Search Proxy provides the capability to the users to proxy the Apache Solr from any non-AEM applications.

Requirements
------------

* Java 7 or greater
* Maven 3.2.x


Getting Started
---------------

These instructions assume that Apache Solr is actually running for which we are setting up this proxy.

Set up the following configurations inside resources/application.properties file:

1. Enter the Solr Endpoint URL
    
        solr.endpoint.url=http://localhost:8983/solr
    
    For example in my case, it is 'http://localhost:8983/solr'

2. Enter the allowed request handlers, seperated by commas:

        solr.allowed.request.handlers=/select
        
3. Enter the maximum number of allowed rows
        
        solr.allowed.rows.max=30
        
4. If using the Solr Cloud mode, then configure the following:        

        solr.cloud.mode=true
           
5. For HTTP Requests, configure the http connection timeout        

        http.connection.timeout=1000
        
6. For HTTP Requests, configure the http socket timeout
         
         http.socket.timeout=1000

7. Enter the Available Server port, where you want to run this Solr Proxy

        server.port=8899


Run Solr Proxy
----------------

1. For rapid application development, you can get started with Solr Proxy with the following command:

        $ cd ../aemsolrsearch-proxy
        $ mvn spring-boot:run
    
    The above maven command will run the Solr Proxy locally, at the server port you provided in the application.properties
    
    Now, you can send the HTTP requests to the Solr Proxy:
    
        GET http://localhost:8899/solrproxy?corename=collection1&qt=/select&q=*
            
    NOTE: In the request above, the proxy needs the corename in order to provide the results.     


Build the Solr Proxy
----------------------

   By using the following command, the proxy application can be build into the Web application ARchive (WAR) :

         $ mvn clean package
         
   It will generate the target/proxy.war, that can be deployed on any Java Application Server.       
         
         
For More Information
--------------------

Send an email to <aemsolr@headwire.com>.         
