package com.headwire.aemsolrsearch.services;

import java.util.HashMap;
import java.util.Map;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AbstractSolrSearchService provides basic support for searching Solr documents. This implementation creates
 * a map of shared instance of SolrServer by core.
 */
public abstract class AbstractSolrService {
	
    private static final Logger LOG = LoggerFactory.getLogger(AbstractSolrService.class);
    
    private static final Map<String, HttpSolrClient> solrClientByCore = new HashMap<String, HttpSolrClient>();
    
    /**
     * Returns the absolute URL to the Solr server.
     * @return
     */
    protected abstract String getSolrServerURI();
    
    /**
     * Returns the absolute URL to the Solr server and core.
     */
    protected abstract String getSolrServerURI(String solrCore);

    /** Retrieve a particular instance of SolrClient identified by the core name. */
    protected SolrClient getSolrClient() {
        return getSolrClient(null);
    }

    /** Retrieve a particular instance of SolrClient identified by the core name. */
    protected SolrClient getSolrClient(String solrCore) {
        
        final HttpSolrClient existingSolrServer = solrClientByCore.get(solrCore);
        if (null != existingSolrServer) {
			LOG.info("Returning existing instance of Solr Server: {}", existingSolrServer.getBaseURL());
        	return existingSolrServer;
        } else {       
        	synchronized (solrClientByCore) {
        		// Double check existence while in synchronized block.
        		if (solrClientByCore.containsKey(solrCore)) {
        			return solrClientByCore.get(solrCore);
        		} else {
        			final String solrServerUri = getSolrServerURI(solrCore);
        			LOG.info("Initializing Solr Server: {}", solrServerUri);
        			HttpSolrClient newSolrServer = new HttpSolrClient(solrServerUri);
        			solrClientByCore.put(solrCore, newSolrServer);
        			return newSolrServer;
        		}
        	}
        }
    }
    
    /** Reset map of instance of SolrClient by the core name. */
    protected void resetSolrClients() {
    	synchronized (solrClientByCore) {   
    		for (HttpSolrClient server : solrClientByCore.values()) {
    			try {
    				server.shutdown();
    			} catch(Exception e) {    				
        			LOG.warn("Exception while shutting down Solr Server instance.", e);
    			}
    		}
    		solrClientByCore.clear();
    	}
    }

    protected String formatSolrEndPointAndCore(String solrEndPoint, String solrCore) {
        StringBuilder endPointAndCore = new StringBuilder();
        endPointAndCore.append(solrEndPoint)
                .append("/")
                .append(solrCore)
                .append("/");
        return endPointAndCore.toString();
    }

}
