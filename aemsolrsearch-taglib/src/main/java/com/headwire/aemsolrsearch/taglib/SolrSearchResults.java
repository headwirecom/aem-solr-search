package com.headwire.aemsolrsearch.taglib;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.util.NamedList;

/**
 * Delegate contain Solr QueryResponse and convenience attributes for the purpose of retrieving in a JSP.
 * 
 * This object will aid in named spacing for JSP, so multiple instance of a component can deal with their specific response.
 */ 
public class SolrSearchResults {
	
	private final SolrQuery solrQuery;
	private final QueryResponse queryResponse;
	
	private final Map responseHeaderMap;
	private final int solrDocumentListStart;
	private final int solrDocumentListNumFound;
	private final int solrDocumentListSize;
	
	SolrSearchResults(SolrQuery solrQuery, QueryResponse queryResponse) {
		
		this.solrQuery = solrQuery;
		this.queryResponse = queryResponse;

		if (null != queryResponse && null != queryResponse.getResponseHeader()) {
			// The NamedList thats returned by queryResponse.getResponseHeader() is not JSP friendly.
			// Convert it to a map to make it more accessible.
			final Map map = new LinkedHashMap();
			final NamedList<?> namedList = queryResponse.getResponseHeader();
			for (Map.Entry entry : namedList) {
				map.put(entry.getKey(), entry.getValue());
			}
			responseHeaderMap = Collections.unmodifiableMap(map);
		} else {			
			responseHeaderMap = Collections.emptyMap();
		}
		
		if (null != queryResponse && null != queryResponse.getResults()) {				
			final SolrDocumentList solrDocumentList = queryResponse.getResults();
			final Long numFound = solrDocumentList.getNumFound();
			final Long start = solrDocumentList.getStart();
			solrDocumentListNumFound = numFound.intValue();
			solrDocumentListStart = start.intValue();
			solrDocumentListSize = solrDocumentList.size();
		} else {
			solrDocumentListNumFound = 0;
			solrDocumentListStart = 0;
			solrDocumentListSize = 0;
		}
	}
	
	public SolrQuery getSolrQuery() {
		return solrQuery;
	}
	
	public QueryResponse getQueryResponse() {
		return this.queryResponse;
	}
	
	public SolrDocumentList getSolrDocumentList() {
		return queryResponse.getResults();
	}

	public Map<?, ?> getResponseHeaderMap() {		
		return responseHeaderMap;
	}

	public int getSolrDocumentListStart() {
		return solrDocumentListStart;
	}
	
	public int getSolrDocumentListNumFound() {
		return solrDocumentListNumFound;
	}
	
	public int getSolrDocumentListSize() {
		return solrDocumentListSize;
	}

}
