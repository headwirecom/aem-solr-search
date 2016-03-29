package com.headwire.aemsolrsearch.services;

import org.apache.solr.client.solrj.SolrClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AbstractSolrSearchService provides basic support for searching Solr documents. This implementation creates
 * a map of shared instance of SolrServer by core.
 */
public abstract class AbstractSolrService {
	
    private static final Logger LOG = LoggerFactory.getLogger(AbstractSolrService.class);
    
    /**
     * Returns the absolute URL to the Solr server.
     * @return
     */
    protected abstract String getSolrServerURI();
    
    /**
     * Returns the absolute URL to the Solr server and core.
     */
    protected abstract String getSolrServerURI(String solrCore);

    protected String formatSolrEndPointAndCore(String solrEndPoint, String solrCore) {
        StringBuilder endPointAndCore = new StringBuilder();
        endPointAndCore.append(solrEndPoint)
                .append("/")
                .append(solrCore)
                .append("/");
        return endPointAndCore.toString();
    }

    /** Retrieve a particular instance of SolrClient for Indexing. */
    protected abstract SolrClient getSolrIndexClient();

    /** Retrieve a particular instance of SolrClient for Querying. */
    protected abstract SolrClient getSolrQueryClient();

}
