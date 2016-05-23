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

2. Enter the Solr Core Name

        solr.core=collection1

3. Enter the allowerd request handlers, seperated by commas:

        solr.allowed.request.handlers=/select,/update,/delete
        
4. Enter the maximum number of allowed rows
        
        solr.allowed.rows.max=30
        
5. If using the Solr Cloud mode, then configure the following:        

        solr.cloud.mode=true

6. If using the Solr Cloud mode, then configure the zookeeper configuration:         

        solr.zkHost=localhost:8983
        
   NOTE: The zkHost has no /solr handler.     
        
7. For HTTP Requests, configure the http connection timeout        

        http.connection.timeout=1000
        
8. For HTTP Requests, configure the http socket timeout
         
         http.socket.timeout=1000

9. Enter the Available Server port, where you want to run this Solr Proxy

        server.port=8899


Run Solr Proxy
----------------

1. For rapid application development, you can get started with Solr Proxy with the following command:

        $ cd ../aemsolrsearch-proxy
        $ mvn spring-boot:run
    
    The above maven command will run the Solr Proxy locally, at the server port you provided in the application.properties
    
    Now, you can send the HTTP requests to the Solr Proxy:
    
        GET http://localhost:8899/solrproxy?corename=collection1&qt=/select&q=*    


2. Or if you want to build this proxy application and run on some other application server, then use the following command:

         $ mvn clean package
         
         
For More Information
--------------------

Send an email to <aemsolr@headwire.com>.         
