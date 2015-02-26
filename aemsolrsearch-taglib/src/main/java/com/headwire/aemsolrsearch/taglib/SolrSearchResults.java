package com.headwire.aemsolrsearch.taglib;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField;
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
		
		cleanQueryAndResponseFacets();
	}
	
	private void cleanQueryAndResponseFacets() {
		String[] filterQueries = solrQuery.getFilterQueries();
		if (filterQueries != null) {
			for (int i=0; i<filterQueries.length; i++)
				filterQueries[i] = cleanFilterQuery(filterQueries[i]);
			solrQuery.setFilterQueries(filterQueries);
		}
		
		for (FacetField field : queryResponse.getFacetFields())
			for (FacetField.Count fieldCount : field.getValues())
				fieldCount.setName(cleanFilterQuery(fieldCount.getName()));
	}
	
	private String cleanFilterQuery(String query) {
		StringBuffer retVal = new StringBuffer();
		for (String queryPart : query.split(":")) {
			if (retVal.length() > 0)
				retVal.append(":");
			
			if (queryPart.startsWith("\"") && queryPart.endsWith("\""))
				retVal.append(queryPart.substring(1, queryPart.length()-1));
			else
				retVal.append(queryPart);
		}
		return retVal.toString();
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
