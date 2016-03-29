About AEM Solr Search
=====================

AEM Solr Search provides an intergration between AEM (CQ) and Apache Solr. It includes the following features:

* SolrJ OSGi bundle
* Suite of search UI components (i.e., facets, search input fields, pagination, etc.)
* Server-side proxy
* Quick start distribution of Apache Solr 4.10.4 (intended for development use only).
* Geometrixx Media sample intergration with Solr.

Note: This README uses Google Analytics for tracking site visits using: [![Analytics](https://ga-beacon.appspot.com/UA-72395016-2/headwirecom/aem-solr-search/readme)](https://github.com/igrigorik/ga-beacon)

Requirements
------------

* Java 7 or greater
* Adobe AEM 6.1 or greater (with the Geometrixx Media Site)
* Maven 3.2.x

Getting Started
---------------

These instructions assume that AEM is running on localhost on port 4502 with the default admin/admin credentials.

1. Start AEM/CQ.

2. Deploy AEM Solr Search by running the following from the root project.

        $ mvn clean install -Pauto-deploy-all
        
3. Deploy the Geometrixx Media sample bundles. 

        $ mvn install -Pauto-deploy-geo
        $ mvn install -Pauto-deploy-sample

4. Start Jetty. This will take sometime the first time, as Solr will be fetched from a Maven repository.

        $ cd aemsolrsearch-quickstart
        $ mvn clean resources:resources jetty:run
    
5. In another terminal window run the index script.

        $ cd ../aemsolrsearch-geometrixx-media-sample
        $ ./index-geometrixx-media-articles.sh

6. Open a browser and visit:
    * Sample Geometrixx Media Search Page: [http://localhost:4502/content/aemsolrsearch/aem-solr-search.html](http://localhost:4502/content/aemsolrsearch/aem-solr-search.html)
    * Solr: [http://localhost:8888/solr/](http://localhost:8888/solr/)


SolrCloud 5.x Demo
------------------

AEM Solr Search 2.0 now supports multiple deployment options. The _Getting Started_ section described the traditional standalone Solr deployment for for Solr 4.x.

If you would like to try Solr 5 in SolrCloud mode, perform the following:

1. Ensure that steps 1-3 are complete in the _Getting Started_ section.

2. Since Solr 5 is no longer packaged as a WAR file, we can no longer use the AEM Solr Search Quickstart project (`aemsolrsearch-quickstart`). 
   However, we wanted to provided a working runtime. We opted for Vagrant and VirtualBox as our virtualization solution. Simply refer to
   `aemsolrsearch-vagrant/README.md`, perform the steps described, and then return to these instructions.
   
3. In a browser, visit http://localhost:4502/system/console/configMgr and edit the _AEM Solr Search - Solr Configuration Service_ and set 
   `solr.mode` to _SolrCloud_ and set `solr.master` to one of the nodes in the cluster (i.e., `http://localhost:8983/solr`). 
   Currently, the proxy does not detect the set of active nodes in Zookeeper.
   
4. Trigger a real-time index by creating a Geometrixx Media Article page or by running:

        $ cd aemsolrsearch-geometrixx-media-sample
        $ ./index-geometrixx-media-articles-solrcloud.sh

For More Information
--------------------

Send an email to <aemsolr@headwire.com>.
