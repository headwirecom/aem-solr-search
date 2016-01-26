package com.headwire.aemsolrsearch.search.services;

import org.apache.solr.client.solrj.SolrQuery;

import java.io.IOException;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.headwire.aemsolrsearch.services.AbstractSolrService;

/** Extends AbstractSolrService and adds the ability to query. */
public abstract class AbstractSolrSearchService extends AbstractSolrService {
    
    private static final Logger LOG = LoggerFactory.getLogger(AbstractSolrSearchService.class);

    /**
     * Query a particular instance of SolrServer identified by the core name
     * with a given query.
     */
    public QueryResponse query(String solrCore, SolrQuery solrQuery)
            throws SolrServerException {
    	SolrClient server = getSolrClient(solrCore);
        LOG.info("Quering {} with '{}'", getSolrServerURI(solrCore), solrQuery);
        QueryResponse solrResponse;
		try {
			solrResponse = server.query(solrQuery);
			return solrResponse;
		} catch (IOException e) {
			e.printStackTrace();
		}
        return new QueryResponse();
    }

}
