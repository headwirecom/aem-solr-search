About AEM Solr Search
=====================

AEM Solr Search provides an intergration between AEM (CQ) and Apache Solr. It includes the following features:

* SolrJ OSGi bundle
* Suite of search UI components (i.e., facets, search input fields, pagination, etc.)
* Server-side proxy
* Quick start distribution of Apache Solr 4.9 (intended for development use only).
* Geometrixx Media sample intergration with Solr.

Requirements
------------

* Java 7 or greater
* Adobe AEM (CQ) 5.6.1 (with the Geometrixx Media Site)
* Maven 3

Getting Started
---------------

These instructions assume that AEM (CQ) is running on localhost on port 4502 with the default admin/admin credentials.

1. Start AEM/CQ.

2. Deploy AEM Solr Search by running the following from the root project.

        $ mvn clean install -Pauto-deploy-all
        
3. Deploy Geometrixx Media sample and Geomextrixx Sample

        $ mvn install -Pauto-deploy-sample
        $ mvn install -Pauto-deploy-geo
		$ mvn install -Pauto-deploy-geo-sample

4. Start Jetty. This will take sometime the first time, as Solr will be fetched from a Maven repository.

        $ cd aemsolrsearch-quickstart
        $ mvn clean resources:resources jetty:run
    
5. In another terminal window run the index script.

        $ cd ../aemsolrsearch-geometrixx-media-sample
        $ ./index-geometrixx-media-articles.sh
        $ cd ../aemsolrsearch-geometrixx-sample
        $ ./index-geometrixx-content.sh 

6. Open a browser and visit:
    * Sample Geometrixx Media Search Page: [http://localhost:4502/content/aemsolrsearch/aem-solr-search.html](http://localhost:4502/content/aemsolrsearch/aem-solr-search.html)
	* Sample Geometrixx Search Page: [http://localhost:4502/content/aemsolrsearch/aem-solr-search-geo.html](http://localhost:4502/content/aemsolrsearch/aem-solr-search-geo.html)
    * Solr: [http://localhost:8080/solr/](http://localhost:8080/solr/)


How AEM Solr works ?
--------------------

AEM Solr considers the two main area which works completely independently: indexing the content and then searching the content.

1. Indexing the content: If any new page is created, or any existing page is being modified, it needs to be indexed. The event handlers are being configured to listen to the Page events and Replication events using the Sling Eventing. These event handlers use the SolrJ API to update the Solr index.
To learn more, refer to the event handler 'com.headwire.aemsolrsearch.geometrixx.listeners.SolrGeometrixxPageListener'.

2. Searching the content: Once your content is indexed in Solr, you will need a search interface. This integration offers support for building search interfaces using search components built on ajax-solr as well as a configurable CQ Listeners for real-time Solr indexing.
To learn more, refer to 'aemsolrsearch/cqsearch-solr' and 'aemsolrsearch/ajax-solr' components.


How to setup the environment manually ?
---------------------------------------

The steps in 'Getting Started' covers how to setup the environment locally. Following are the steps that you can follow to setup any higher environment like the testing, integration or the production environment.

1. Setup Solr: Considering you have installed the Solr on your target environment, and having the Solr Home Directory, let's name it SOLR_HOME_DIR. The steps below will add the new Core to Solr. The newly added Solr Core can be named as per your requirement.  
        
        $ cd aemsolrsearch-quickstart/src/main/resources
        $ cp -r aem-solr-home/* SOLR_HOME_DIR/
        
2. Deploy AEM Solr Search by running the following from the root project by replacing the host 'YOUR_TEST_INSTANCE' and the port 'YOUR_TEST_PORT'
        
        $ mvn clean install -Pauto-deploy-all -Dcq.host=<YOUR_TEST_INSTANCE> -Dcq.port=<YOUR_TEST_PORT>
              
3. Deploy Geometrixx Media sample and Geomextrixx Sample

        $ mvn install -Pauto-deploy-sample -Dcq.host=<YOUR_TEST_INSTANCE> -Dcq.port=<YOUR_TEST_PORT>
        $ mvn install -Pauto-deploy-geo -Dcq.host=<YOUR_TEST_INSTANCE> -Dcq.port=<YOUR_TEST_PORT>
		$ mvn install -Pauto-deploy-geo-sample -Dcq.host=<YOUR_TEST_INSTANCE> -Dcq.port=<YOUR_TEST_PORT>

4. Configure the Solr Update Handler in the configurations
        id="sling.servlet.paths", value = "/apps/geometrixx-media/solr/updatehandler"

5. Copy the script from the following location to the location where you want to index the text and then configure the script according to your environment details.
        Edit the file /aemsolrsearch-geometrixx-media-sample/index-geometrixx-media-articles.sh
        and replace the the following fields:
        
        CQ_USER=<CQ_USER>
        CQ_PASS=<CQ_PASS>
        CQ_HOST=<CQ_HOST>
        CQ_PORT=<CQ_POST>
        SOLR_HOST=<SOLR_HOST>
        SOLR_PORT=<SOLR_PORT>
        SOLR_CORE=<SOLR_CORE_NAME>
        SLING_RESOURCE_TYPE=<RESOURCE_TYPE_TO_INDEX_FOR_SEARCHING>


Security
--------
TODO

For More Information
--------------------

Send an email to <aemsolr@headwire.com>.
