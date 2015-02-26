package com.headwire.aemsolrsearch.taglib;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.jsp.JspException;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cqblueprints.taglib.CqSimpleTagSupport;
import com.headwire.aemsolrsearch.search.services.AbstractSolrSearchService;
import com.headwire.aemsolrsearch.search.services.DefaultSolrSearchService;
import com.squeakysand.jsp.tagext.annotations.JspTag;
import com.squeakysand.jsp.tagext.annotations.JspTagAttribute;

/**
 * Tag used to build SolrQuery, call the SolrSearchService, and store results. 
 */
@JspTag
public class SolrSearchTag extends CqSimpleTagSupport {

	private static final Logger LOG = LoggerFactory
			.getLogger(SolrSearchTag.class);

	private SolrQuery solrQuery;

	/** Name to retrieve this object in JSP/Servlet in page scope. */
	private String var;

	/** Name to retrieve the SolrSearchResult object in JSP/Servlet in page scope. */
	private String varResults;
		
	// Set by user
	private String query;
	private int start = 0;
	private String[] filterQueries;
	
	// Set by author
	private String solrCoreName;
	private int rows = 0;
	private String[] fieldNames;
	private String[] advancedFilterQueries;
	private boolean facetEnabled;
	private String facetSort;
	private int facetLimit;
	private int facetMinCount;
	private String[] facetFieldNames;	
	private boolean highlightEnabled;
	private boolean highlightRequireFieldMatchEnabled;
	private String highlightSimplePre;
	private String highlightSimplePost;
	private int highlightNumberSnippets;
	private int highlightFragsize;
	private String[] highlightingFields;
    private String searchHandler;

	@Override
	public void doTag() throws JspException, IOException {

		// Make this tag object available to the JSP/Servlet scope.
		if (StringUtils.isNotBlank(getVar())) {				
			getPageContext().setAttribute(getVar(), this);
//			getRequest().setAttribute(getVar(), this);
		}

		// Get Solr Service
		AbstractSolrSearchService searchService = null;
		try {
			searchService = (AbstractSolrSearchService) getService(DefaultSolrSearchService.class);
		} catch (Exception e) {
			LOG.error("Cannot load DefaultSolrSearchService.", e);

			// Is this right?
			getRequest().setAttribute("exception", e);
		}

		if (null == searchService) {
			LOG.error("Can't get AbstractSolrSearchService. Check that all OSGi bundles are active");
			return;
		}

		try {
			// Build query.
			final SolrQuery query = buildSolrQuery();

			// Query Solr service.
			final QueryResponse queryResponse = searchService.query(getSolrCoreName(), query);
			
			// Make the query and results available to the JSP
			if (StringUtils.isNotBlank(getVarResults())) {	
				final SolrSearchResults results = new SolrSearchResults(query, queryResponse);
				getPageContext().setAttribute(getVarResults(), results);
			}
		} catch (SolrServerException e) {
			LOG.error("SolrServerException while trying to query.", e);
			// Is this right?
			getRequest().setAttribute("exception", e);
		} catch (Exception e) {
			LOG.error("Error while trying to query : " + e.getMessage(), e);
			// Is this right?
			getRequest().setAttribute("exception", e);
		}
	}

	/** Builds solr query from this tags attributes. */
	private SolrQuery buildSolrQuery() {
		
		// Use the solr query passed in, or create a new one.
		final SolrQuery query = (null != getSolrQuery()) ? getSolrQuery()
				: new SolrQuery();
		
		// Set query (q).
		if (StringUtils.isNotBlank(this.getQuery())) {
			query.setQuery(getQuery());
		}
		
		// Assert query not blank.
		if (StringUtils.isBlank(query.getQuery())) {
			query.setQuery("*:*");
		}
		
		// Set (start) or starting index.
		if (0 < this.getStart()) {
			query.setStart(getStart());
		}
		
		// Set (rows) or limit of results returned.
		if (0 < this.getRows()) {
			query.setRows(getRows());
		}
		
		// Set (fl) or available fields.
		if (null != getFieldNames()) {
			for (String field : getFieldNames()) {					
				query.addField(field);
			}
		}
		
		// Set (fq) or selected filtered queries form selected facets or adv. query / 
		Set<String> uniquefilterQueries = new HashSet<String>();
		if (null != getFilterQueries()) {
			uniquefilterQueries.addAll(Arrays.asList(getFilterQueries()));
		}
		if (null != getAdvancedFilterQueries()) {
			uniquefilterQueries.addAll(Arrays.asList(getAdvancedFilterQueries()));
		}
		uniquefilterQueries = cleanFilterQueries(uniquefilterQueries);
		if (!uniquefilterQueries.isEmpty()) {			
			query.setFilterQueries(uniquefilterQueries.toArray(new String[uniquefilterQueries.size()]));
		}
		
		// Set enable of (facets)
		query.setFacet(isFacetEnabled());
		if (isFacetEnabled()) {

			// Set (facet.sort)
			if (StringUtils.isNotBlank(this.getFacetSort())) {
				query.setFacetSort(this.getFacetSort());
			}

			// Set (facet.limit)
			if (0 < this.getFacetLimit()) {
				query.setFacetLimit(getFacetLimit());
			}
			// Set (facet.mincount)
			if (0 < this.getFacetMinCount()) {
				query.setFacetMinCount(getFacetMinCount());
			}
			// Set (facet.field)			
			if (null != getFacetFieldNames() && 0 < getFacetFieldNames().length) {
				query.addFacetField(getFacetFieldNames());
			}
		}
		
		if (isHighlightEnabled()) {			
			
			// Set (hl) enable
			query.setHighlight(true);
			
			// Set (hl.requireFieldMatch)
			query.setHighlightRequireFieldMatch(isHighlightRequireFieldMatchEnabled());
			
			// Set (hl.simple.pre)
			query.setHighlightSimplePre(getHighlightSimplePre());
			
			// Set (hl.simple.post)
			query.setHighlightSimplePost(getHighlightSimplePost());
			
			// Set (hl.snippets)
			query.setHighlightSnippets(getHighlightNumberSnippets());
			
			// Set (hl.fragsize)
			query.setHighlightFragsize(getHighlightFragsize());
			
			// Set (hl.fl)
			if (null != getHighlightingFields()) {
				for (String field : getHighlightingFields()) {					
					query.addHighlightField(field);
				}
			}

            // Set (qt)
            if (null != getSearchHandler()) {
                query.setRequestHandler(getSearchHandler());
            }
		}
		
		return query;
	}
	
	private Set<String> cleanFilterQueries(Set<String> queries) {
		Set<String> retVal = new HashSet<String>();
		for (String query : queries)
			retVal.add(cleanFilterQuery(query));
		return retVal;
	}
	
	private String cleanFilterQuery(String query) {
		StringBuffer retVal = new StringBuffer();
		for (String queryPart : query.split(":")) {
			if (retVal.length() > 0)
				retVal.append(":");
			
			if ((queryPart.contains(" ") ||
					queryPart.contains("%20") ||
					queryPart.contains("+")) &&
					!queryPart.contains("\""))
				retVal.append("\"" + queryPart + "\"");
			else
				retVal.append(queryPart);
		}
		return retVal.toString();
	}
	
	/** Return name to retrieve this object in JSP/Servlet page scope. */
	public String getVar() {
		return var;
	}

	/** Set name to retrieve this object in JSP/Servlet page scope. */
	@JspTagAttribute(required = false, rtexprvalue = true)
	public void setVar(String var) {
		this.var = var;
	}

	/** Return name to retrieve SolrSearchResult object in JSP/Servlet page scope. */
	public String getVarResults() {
		return varResults;
	}
	
	/** Set name to retrieve the SolrSearchResult object in JSP/Servlet page scope. */
	@JspTagAttribute(required = false, rtexprvalue = true)
	public void setVarResults(String varResults) {
		this.varResults = varResults;
	}

	/** Return solr core to use. */
	public String getSolrCoreName() {
		return solrCoreName;
	}

	/** Set solr core to use. */
	@JspTagAttribute(required = true, rtexprvalue = true)
	public void setSolrCoreName(String solrCoreName) {
		this.solrCoreName = solrCoreName;
	}

	/** Return SolrQuery to used to query the solr service. */
	public SolrQuery getSolrQuery() {
		return solrQuery;
	}

	/**
	 * Set SolrQuery to use, useful for programmatically initializing the query.
	 * Note that attributes set on this tag will override properties set on this
	 * SolrQuery. E.g. if you set mySolrQuery.setQuery("foo") and then pass it
	 * to this tag, and have <namespace:solrSearch query="bar"/>, then the
	 * resulting query will execute with "bar".
	 */
	@JspTagAttribute(required = false, rtexprvalue = true)
	public void setSolrQuery(SolrQuery solrQuery) {
		this.solrQuery = solrQuery;
	}

	/** Return query string. (q). */
	public String getQuery() {
		return query;
	}

	/** Set query string. (q). */
	@JspTagAttribute(required = false, rtexprvalue = true)
	public void setQuery(String query) {
		this.query = query;
	}

	/** Return start (start). */
	public int getStart() {
		return start;
	}

	/** Set start (start). */
	@JspTagAttribute(required = false, rtexprvalue = true)
	public void setStart(int offset) {
		this.start = offset;
	}

	/** Return rows (rows). */
	public int getRows() {
		return rows;
	}

	/** Set rows (rows). */
	@JspTagAttribute(required = false, rtexprvalue = true)
	public void setRows(int limit) {
		this.rows = limit;
	}
	
	/** Return fieldNames (fl). */
	public String[] getFieldNames() {
		return fieldNames;
	}

	/** Set fieldNames (fl). */
	@JspTagAttribute(required = false, rtexprvalue = true)
	public void setFieldNames(String[] fieldNames) {
		this.fieldNames = fieldNames;
	}

	/** Return filterQueries (fq). */
	public String[] getFilterQueries() {
		return filterQueries;
	}

	/** Set filterQueries (fq). */
	@JspTagAttribute(required = false, rtexprvalue = true)
	public void setFilterQueries(String[] filterQueries) {
		this.filterQueries = filterQueries;
	}
	
	/** Return filterQueries hidden from user. (fq). */
	public String[] getAdvancedFilterQueries() {
		return advancedFilterQueries;
	}

	/** Set filterQueries hidden from user. (fq). */
	@JspTagAttribute(required = false, rtexprvalue = true)
	public void setAdvancedFilterQueries(String[] advancedFilterQueries) {
		this.advancedFilterQueries = advancedFilterQueries;
	}

	/** Return facet enabled (facet). */
	public boolean isFacetEnabled() {
		return facetEnabled;
	}

	/** Set facet enabled (facet). */
	@JspTagAttribute(required = false, rtexprvalue = true)
	public void setFacetEnabled(boolean facetEnabled) {
		this.facetEnabled = facetEnabled;
	}

	/** Return facet sort (facet.sort). */
	public String getFacetSort() {
		return facetSort;
	}

	/** Set facet sort (facet.sort). */
	@JspTagAttribute(required = false, rtexprvalue = true)
	public void setFacetSort(String facetSort) {
		this.facetSort = facetSort;
	}

	/** Return facet limit (facet.limit). */
	public int getFacetLimit() {
		return facetLimit;
	}

	/** Set facet limit (facet.limit). */
	@JspTagAttribute(required = false, rtexprvalue = true)
	public void setFacetLimit(int facetLimit) {
		this.facetLimit = facetLimit;
	}

	/** Return facet min count (facet.mincount). */
	public int getFacetMinCount() {
		return facetMinCount;
	}

	/** Set facet min count (facet.mincount). */
	@JspTagAttribute(required = false, rtexprvalue = true)
	public void setFacetMinCount(int facetMinCount) {
		this.facetMinCount = facetMinCount;
	}

	/** Return facet fields (facet.field). */
	public String[] getFacetFieldNames() {
		return facetFieldNames;
	}

	/** Set facet fields (facet.field). */
	@JspTagAttribute(required = false, rtexprvalue = true)
	public void setFacetFieldNames(String[] facetKeys) {
		this.facetFieldNames = facetKeys;
	}

	/** Return highlight enabled fields (hl). */
	public boolean isHighlightEnabled() {
		return highlightEnabled;
	}

	/** Set highlight enabled fields (hl). */
	@JspTagAttribute(required = false, rtexprvalue = true)
	public void setHighlightEnabled(boolean highlightEnabled) {
		this.highlightEnabled = highlightEnabled;
	}

	/** Return highlight require field match enabled fields (hl.requireFieldMatch). */
	public boolean isHighlightRequireFieldMatchEnabled() {
		return highlightRequireFieldMatchEnabled;
	}

	/** Set highlight require field match enabled fields (hl.requireFieldMatch). */
	@JspTagAttribute(required = false, rtexprvalue = true)
	public void setHighlightRequireFieldMatchEnabled(
			boolean highlightRequireFieldMatchEnabled) {
		this.highlightRequireFieldMatchEnabled = highlightRequireFieldMatchEnabled;
	}

	/** Return highlight simple pre (hl.simple.pre). */
	public String getHighlightSimplePre() {
		return highlightSimplePre;
	}

	/** Set highlight simple pre (hl.simple.pre). */
	@JspTagAttribute(required = false, rtexprvalue = true)
	public void setHighlightSimplePre(String highlightSimplePre) {
		this.highlightSimplePre = highlightSimplePre;
	}

	/** Return highlight simple post (hl.simple.post). */
	public String getHighlightSimplePost() {
		return highlightSimplePost;
	}

	/** Set highlight simple post (hl.simple.post). */
	@JspTagAttribute(required = false, rtexprvalue = true)
	public void setHighlightSimplePost(String highlightSimplePost) {
		this.highlightSimplePost = highlightSimplePost;
	}

	/** Return highlight number snippets (hl.snippets). */
	public int getHighlightNumberSnippets() {
		return highlightNumberSnippets;
	}

	/** Set highlight number snippets (hl.snippets). */
	@JspTagAttribute(required = false, rtexprvalue = true)
	public void setHighlightNumberSnippets(int highlightNumberSnippets) {
		this.highlightNumberSnippets = highlightNumberSnippets;
	}

	/** Return highlight frag size (hl.fragsize). */
	public int getHighlightFragsize() {
		return highlightFragsize;
	}

	/** Set highlight frag size (hl.fragsize). */
	@JspTagAttribute(required = false, rtexprvalue = true)
	public void setHighlightFragsize(int highlightFragsize) {
		this.highlightFragsize = highlightFragsize;
	}

	/** Return highlight fields (hl.fl). */
	public String[] getHighlightingFields() {
		return highlightingFields;
	}

	/** Set highlight fields (hl.fl). */
	@JspTagAttribute(required = false, rtexprvalue = true)
	public void setHighlightingFields(String[] highlightingFields) {
		this.highlightingFields = highlightingFields;
	}

    /** Return search handler (qt). */
    public String getSearchHandler() {
        return searchHandler;
    }

    /** Set search handler (qt). */
    @JspTagAttribute(required = false, rtexprvalue = true)
    public void setSearchHandler(String searchHandler) {
        this.searchHandler = searchHandler;
    }
}
