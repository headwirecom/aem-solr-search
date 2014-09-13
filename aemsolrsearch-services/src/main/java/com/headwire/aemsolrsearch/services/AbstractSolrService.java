package com.headwire.aemsolrsearch.services;

import java.util.HashMap;
import java.util.Map;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AbstractSolrSearchService provides basic support for searching Solr documents. This implementation creates
 * a map of shared instance of SolrServer by core.
 */
public abstract class AbstractSolrService {
	
    private static final Logger LOG = LoggerFactory.getLogger(AbstractSolrService.class);
    
    private static final Map<String, HttpSolrServer> solrServerByCore = new HashMap<String, HttpSolrServer>();
    
    /**
     * Returns the absolute URL to the Solr server.
     * @return
     */
    protected abstract String getSolrServerURI();
    
    /**
     * Returns the absolute URL to the Solr server and core.
     */
    protected abstract String getSolrServerURI(String solrCore);

    /** Retrieve a particular instance of SolrServer identified by the core name. */
    protected SolrServer getSolrServer() {
        return getSolrServer(null);
    }

        /** Retrieve a particular instance of SolrServer identified by the core name. */
    protected SolrServer getSolrServer(String solrCore) {
        
        final HttpSolrServer existingSolrServer = solrServerByCore.get(solrCore);
        if (null != existingSolrServer) {
			LOG.info("Returning existing instance of Solr Server: {}", existingSolrServer.getBaseURL());
        	return existingSolrServer;
        } else {       
        	synchronized (solrServerByCore) {
        		// Double check existence while in synchronized block.
        		if (solrServerByCore.containsKey(solrCore)) {
        			return solrServerByCore.get(solrCore);
        		} else {
        			final String solrServerUri = getSolrServerURI(solrCore);
        			LOG.info("Initializing Solr Server: {}", solrServerUri);
        			HttpSolrServer newSolrServer = new HttpSolrServer(solrServerUri);
        			solrServerByCore.put(solrCore, newSolrServer);
        			return newSolrServer;
        		}
        	}
        }
    }
    
    /** Reset map of instance of SolrServer by the core name. */
    protected void resetSolrServerClients() {
    	synchronized (solrServerByCore) {   
    		for (HttpSolrServer server : solrServerByCore.values()) {
    			try {
    				server.shutdown();
    			} catch(Exception e) {    				
        			LOG.warn("Exception while shutting down Solr Server instance.", e);
    			}
    		}
    		solrServerByCore.clear();
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
