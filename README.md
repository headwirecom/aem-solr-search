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
    * Solr: [http://localhost:8888/solr/](http://localhost:8888/solr/)


Security
--------
TODO

For More Information
--------------------

Send an email to <aemsolr@headwire.com>.
