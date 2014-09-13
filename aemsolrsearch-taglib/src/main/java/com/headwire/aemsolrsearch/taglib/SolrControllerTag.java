/*
 * Author  : Gaston Gonzalez
 * Date    : 6/30/13
 * Version : $Id: SolrControllerTag.java 1680 2014-04-06 02:35:01Z gaston $
 */
package com.headwire.aemsolrsearch.taglib;

import com.cqblueprints.taglib.CqSimpleTagSupport;
import com.headwire.aemsolrsearch.services.SolrConfigurationService;
import com.headwire.aemsolrsearch.services.util.StringUtil;
import com.squeakysand.jsp.tagext.annotations.JspTag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.jsp.JspException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * SolrControllerTag is responsible for gathering search configuration information and making it available
 * to the JSP.
 *
 * @author <a href="mailto:gg@headwire.com">Gaston Gonzalez</a>
 */
@JspTag
public class SolrControllerTag extends CqSimpleTagSupport {

    private static final Logger LOG = LoggerFactory.getLogger(SolrControllerTag.class);

    /** JCR property holding Solr core name. */
    public static final String PROPERTY_SOLR_CORE = "solr-core";

    /** JCR property holding pagination enabled state. */
    public static final String PROPERTY_PAGINATION_ENABLE = "pagination-enable";
    /** JCR property holding pagination previous label. */
    public static final String PROPERTY_PAGINATION_PREV_LABEL = "pagination-prev-label";
    /** JCR property holding pagination next label. */
    public static final String PROPERTY_PAGINATION_NEXT_LABEL = "pagination-next-label";
    /** JCR property holding the HTML ID targeting pagination. */
    public static final String PROPERTY_PAGINATION_HTML_ID = "pagination-id";
    /** JCR property holding the HTML target for pagination. */
    public static final String PROPERTY_PAGINATION_HTML_TARGET = "pagination-target";

    /** JCR property holding results enabled state. */
    public static final String PROPERTY_RESULTS_ENABLE = "results-enable";
    /** JCR property holding results enabled state. */
    public static final String PROPERTY_RESULTS_PER_PAGE = "results-per-page";
    /** JCR property holding HTML ID targeting search results. */
    public static final String PROPERTY_RESULTS_HTML_ID = "results-id";
    /** JCR property holding  HTML target for search results. */
    public static final String PROPERTY_RESULTS_HTML_TARGET = "results-target";
    /** JCR property holding available result fields. */
    public static final String PROPERTY_RESULTS_AVAILABLE_FIELDS = "solr-result-fields";

    /** JCR property holding the facet enabled state. */
    public static final String PROPERTY_FACETS_ENABLE = "facets-enable";
    /** JCR property holding the facet sort order. */
    public static final String PROPERTY_FACETS_SORT = "solr-facet-sort";
    /** JCR property holding available facets. */
    public static final String PROPERTY_FACETS_AVAILABLE_FIELDS = "solr-facet-fields";
    /** JCR property holding show more facet label. */
    public static final String PROPERTY_FACETS_SHOW_MORE = "solr-facet-show-more";
    /** JCR property holding show less facet label. */
    public static final String PROPERTY_FACETS_SHOW_LESS = "solr-facet-show-less";
    /** JCR property holding number of facets to show. */
    public static final String PROPERTY_FACETS_SHOW_NUM_FACETS = "solr-facet-show-num-facets";

    /** JCR property holding search input enabled state. */
    public static final String PROPERTY_INPUT_ENABLE = "input-enable";
    /** JCR property holding the HTML ID targeting the search input field. */
    public static final String PROPERTY_INPUT_HTML_ID = "input-id";
    /** JCR property holding the HTML target for search input. */
    public static final String PROPERTY_INPUT_HTML_TARGET = "input-target";

    /** JCR property holding breadbox enabled state. */
    public static final String PROPERTY_BREADBOX_ENABLE = "breadbox-enable";
    /** JCR property holding the HTML ID targeting the breadbox input field. */
    public static final String PROPERTY_BREADBOX_HTML_ID = "breadbox-id";
    /** JCR property holding the HTML target for the breadbox. */
    public static final String PROPERTY_BREADBOX_HTML_TARGET = "breadbox-target";

    /** JCR property holding hit highlighting enabled state. */
    public static final String PROPERTY_HITHIGHLIGHTING_ENABLE = "highlighting-enable";
    /** JCR property holding the hit highlighting pre formatter. */
    public static final String PROPERTY_HITHIGHLIGHTING_PRE_FORMATTER = "highlighting-pre";
    /** JCR property holding the hit highlighting snippets option. */
    public static final String PROPERTY_HITHIGHLIGHTING_SNIPPETS = "highlighting-snippets";
    /** JCR property holding available highlighting fragment size. */
    public static final String PROPERTY_HITHIGHLIGHTING_FRAGMENT_SIZE = "highlighting-fragsize";

    /** JCR property holding the hit highlighting post formatter. */
    public static final String PROPERTY_HITHIGHLIGHTING_POST_FORMATTER = "highlighting-post";
    /** JCR property holding available hit highlighting fields. */
    public static final String PROPERTY_HITHIGHLIGHTING_AVAILABLE_FIELDS = "highlighting-fields";

    /** JCR property holding the state on whether filter queries are allowed in the breadbox. */
    public static final String PROPERTY_ADVANCED_FILTER_QUERIES_IN_BREADBOX_ENABLED = "solr-filter-queries-in-breadbox-enabled";
    /** JCR property holding the filter queries. */
    public static final String PROPERTY_ADVANCED_FILTER_QUERIES = "solr-filter-queries";
    /** JCR property holding the search handler */
    public static final String PROPERTY_ADVANCED_SEARCH_HANDLER = "search-handler";

    /** JSP attribute name holding Solr core. */
    public static final String JSP_ATTR_SOLR_END_POINT = "solrEndPoint";
    /** JSP attribute name holding Solr core. */
    public static final String JSP_ATTR_SOLR_CORE = "solrCore";
    /** JSP attribute name holding the state variable for the component. If <code>true</code>
     *  the component has been configured, otherwise it is not. */
    public static final String JSP_ATTR_SOLR_CONFIGURED = "solrConfigured";
    /** JSP attribute name holding proxy state. */
    public static final String JSP_ATTR_SOLR_PROXY_ENABLED = "solrProxyEnabled";
    /** JSP attribute name holding proxy state. */
    public static final String JSP_ATTR_SOLR_PROXY_URL = "solrProxyUrl";

    /** JSP attribute name holding the state variable for pagination. If <code>true</code>
     *  pagination is enabled, otherwise it is not. */
    public static final String JSP_ATTR_PAGINATION_ENABLED = "paginationEnabled";
    /** JSP attribute name holding pagination previous label. */
    public static final String JSP_ATTR_PAGINATION_PREV_LABEL = "paginationPrevLabel";
    /** JSP attribute name holding pagination next label. */
    public static final String JSP_ATTR_PAGINATION_NEXT_LABEL = "paginationNextLabel";
    /** JSP attribute name holding the pagination HTML ID. */
    public static final String JSP_ATTR_PAGINATION_HTML_ID = "paginationId";
    /** JSP attribute name holding the pagination HTML target. */
    public static final String JSP_ATTR_PAGINATION_HTML_TARGET = "paginationTarget";

    /** JSP attribute name holding the state variable for results. If <code>true</code>
     *  results are enabled, otherwise it is not. */
    public static final String JSP_ATTR_RESULTS_ENABLED = "resultsEnabled";
    /** JSP attribute name holding the number of results per page. */
    public static final String JSP_ATTR_RESULTS_PER_PAGE = "resultsPerPage";
    /** JSP attribute name holding the search results HTML ID. */
    public static final String JSP_ATTR_RESULTS_HTML_ID = "resultsId";
    /** JSP attribute name holding the search results HTML target. */
    public static final String JSP_ATTR_RESULTS_HTML_TARGET = "resultsTarget";
    /** JSP attribute name holding the available result field. */
    public static final String JSP_ATTR_RESULTS_AVAILABLE_FILEDS = "resultsAvailableFields";

    /** JSP attribute name holding the state variable for facets. If <code>true</code>
     *  facets are enabled, otherwise it is not. */
    public static final String JSP_ATTR_FACETS_ENABLED = "facetsEnabled";
    /** JSP attribute name holding the facet sort order. */
    public static final String JSP_ATTR_FACETS_FACET_SORT = "facetsFacetSort";
    /** JSP attribute name holding the available facets. */
    public static final String JSP_ATTR_FACETS_AVAILABLE_FACETS = "facetsAvailableFacets";
    /** JSP attribute name holding the available facet keys. */
    public static final String JSP_ATTR_FACETS_AVAILABLE_FACET_KEYS = "facetsAvailableFacetKeys";
    /** JSP attribute name holding the show more label. */
    public static final String JSP_ATTR_FACETS_SHOW_MORE = "facetsShowMore";
    /** JSP attribute name holding the show less label. */
    public static final String JSP_ATTR_FACETS_SHOW_LESS = "facetsShowLess";
    /** JSP attribute name holding the number of facets to show. */
    public static final String JSP_ATTR_FACETS_SHOW_NUM_FACETS = "facetsShowNumFacets";

    /** JSP attribute name holding the state variable for search input. If <code>true</code>
     *  search input is enabled, otherwise it is not. */
    public static final String JSP_ATTR_INPUT_ENABLED = "inputEnabled";
    /** JSP attribute name holding the search input HTML ID. */
    public static final String JSP_ATTR_INPUT_HTML_ID = "inputId";
    /** JSP attribute name holding the search input HTML target. */
    public static final String JSP_ATTR_INPUT_HTML_TARGET = "inputTarget";

    /** JSP attribute name holding the state variable for hit highlighting. If <code>true</code>
     *  the breadbox is enabled, otherwise it is not. */
    public static final String JSP_ATTR_HITHIGHLIGHTING_ENABLED = "highlightingEnabled";
    /** JSP attribute name holding the hit highlighting pre formatter. */
    public static final String JSP_ATTR_HITHIGHLIGHTING_PRE_FORMATTER = "highlightingPre";
    /** JSP attribute name holding the hit highlighting post formatter. */
    public static final String JSP_ATTR_HITHIGHLIGHTING_POST_FORMATTER = "highlightingPost";
    /** JSP attribute name holding the available hit highlighting fields. */
    public static final String JSP_ATTR_HITHIGHLIGHTING_AVAILABLE_FIELDS = "highlightingFields";
    /** JSP attribute name holding the snippet hit highlighting option. */
    public static final String JSP_ATTR_HITHIGHLIGHTING_SNIPPETS = "highlightingSnippets";
    /** JSP attribute name holding the fragment size highlighting option. */
    public static final String JSP_ATTR_HITHIGHLIGHTING_FRAGMENT_SIZE = "highlightingFragSize";

    /** JSP attribute name holding the state variable for the breadbox. If <code>true</code>
     *  the breadbox is enabled, otherwise it is not. */
    public static final String JSP_ATTR_BREADBOX_ENABLED = "breadboxEnabled";
    /** JSP attribute name holding the breadbox input HTML ID. */
    public static final String JSP_ATTR_BREADBOX_HTML_ID = "breadboxId";
    /** JSP attribute name holding the breadbox HTML target. */
    public static final String JSP_ATTR_BREADBOX_HTML_TARGET = "breadboxTarget";

    /** JSP attribute name holding the flag on whether filter queries should be displayed in the breadbox. */
    public static final String JSP_ATTR_ADVANCED_FILTER_QUERIES_IN_BREADBOX_ENABLED = "advancedFilterQueriesInBreadboxEnabled";
    /** JSP attribute name holding the filter queries. */
    public static final String JSP_ATTR_ADVANCED_FILTER_QUERIES = "advancedFilterQueries";
    /** JSP attribute name holding the enabled state for filter queries. */
    public static final String JSP_ATTR_ADVANCED_FILTER_QUERIES_ENABLED = "advancedFilterQueriesEnabled";
    /** JSP attribute name holding the search handler. */
    public static final String JSP_ATTR_ADVANCED_SEARCH_HANDLER = "searchHandler";

    private String solrCore = "";
    private boolean configured = false;

    @Override
    public void doTag() throws JspException, IOException {

        // Solr configuration
        solrCore = getProperties().get(PROPERTY_SOLR_CORE, String.class);
        getRequest().setAttribute(JSP_ATTR_SOLR_CORE, solrCore);
        getRequest().setAttribute(JSP_ATTR_SOLR_CONFIGURED, isConfigured());
        getRequest().setAttribute(JSP_ATTR_SOLR_END_POINT, formatSolrEndPointAndCore());
        getRequest().setAttribute(JSP_ATTR_SOLR_PROXY_ENABLED, isSolrProxyEnabled());
        getRequest().setAttribute(JSP_ATTR_SOLR_PROXY_URL, getSolrProxyURL());

        // Pagination
        getRequest().setAttribute(JSP_ATTR_PAGINATION_ENABLED, solrCore);
        getRequest().setAttribute(JSP_ATTR_PAGINATION_ENABLED,  getProperty(PROPERTY_PAGINATION_ENABLE, false));
        getRequest().setAttribute(JSP_ATTR_PAGINATION_PREV_LABEL,  getProperty(PROPERTY_PAGINATION_PREV_LABEL, "&lt;"));
        getRequest().setAttribute(JSP_ATTR_PAGINATION_NEXT_LABEL,  getProperty(PROPERTY_PAGINATION_NEXT_LABEL, "&gt;"));
        getRequest().setAttribute(JSP_ATTR_PAGINATION_HTML_ID,  getProperty(PROPERTY_PAGINATION_HTML_ID, "pager"));
        getRequest().setAttribute(JSP_ATTR_PAGINATION_HTML_TARGET,  getProperty(PROPERTY_PAGINATION_HTML_TARGET, "#pager"));

        // Results
        getRequest().setAttribute(JSP_ATTR_RESULTS_ENABLED,  getProperty(PROPERTY_RESULTS_ENABLE, false));
        getRequest().setAttribute(JSP_ATTR_RESULTS_PER_PAGE,  getProperty(PROPERTY_RESULTS_PER_PAGE, "10"));
        getRequest().setAttribute(JSP_ATTR_RESULTS_HTML_ID,  getProperty(PROPERTY_RESULTS_HTML_ID, "docs"));
        getRequest().setAttribute(JSP_ATTR_RESULTS_HTML_TARGET,  getProperty(PROPERTY_RESULTS_HTML_TARGET, "#search"));
        getRequest().setAttribute(JSP_ATTR_RESULTS_AVAILABLE_FILEDS,  getProperty(PROPERTY_RESULTS_AVAILABLE_FIELDS, new String[]{}));

        // Facets
        getRequest().setAttribute(JSP_ATTR_FACETS_ENABLED, getProperty(PROPERTY_FACETS_ENABLE, false));
        getRequest().setAttribute(JSP_ATTR_FACETS_FACET_SORT, getProperty(PROPERTY_FACETS_SORT, "count"));
        getRequest().setAttribute(JSP_ATTR_FACETS_SHOW_MORE, getProperty(PROPERTY_FACETS_SHOW_MORE, "+ Show More"));
        getRequest().setAttribute(JSP_ATTR_FACETS_SHOW_LESS, getProperty(PROPERTY_FACETS_SHOW_LESS, "- Show Less"));
        getRequest().setAttribute(JSP_ATTR_FACETS_SHOW_NUM_FACETS, getProperty(PROPERTY_FACETS_SHOW_NUM_FACETS, "10"));

        List<Facet> facets = convertToFacets(PROPERTY_FACETS_AVAILABLE_FIELDS);
        getRequest().setAttribute(JSP_ATTR_FACETS_AVAILABLE_FACETS, facets);
        getRequest().setAttribute(JSP_ATTR_FACETS_AVAILABLE_FACET_KEYS,  convertFacetsToKeys(facets));

        // Input
        getRequest().setAttribute(JSP_ATTR_INPUT_ENABLED,  getProperty(PROPERTY_INPUT_ENABLE, false));
        getRequest().setAttribute(JSP_ATTR_INPUT_HTML_ID,  getProperty(PROPERTY_INPUT_HTML_ID, "result"));
        getRequest().setAttribute(JSP_ATTR_INPUT_HTML_TARGET,  getProperty(PROPERTY_INPUT_HTML_TARGET, "#searchresults"));

        // Breadbox
        getRequest().setAttribute(JSP_ATTR_BREADBOX_ENABLED,  getProperty(PROPERTY_BREADBOX_ENABLE, false));
        getRequest().setAttribute(JSP_ATTR_BREADBOX_HTML_ID,  getProperty(PROPERTY_BREADBOX_HTML_ID, "currentsearch"));
        getRequest().setAttribute(JSP_ATTR_BREADBOX_HTML_TARGET,  getProperty(PROPERTY_BREADBOX_HTML_TARGET, "#currentsearchselection"));

        // Hit Highlighting
        getRequest().setAttribute(JSP_ATTR_HITHIGHLIGHTING_ENABLED,  getProperty(PROPERTY_HITHIGHLIGHTING_ENABLE, false));
        getRequest().setAttribute(JSP_ATTR_HITHIGHLIGHTING_PRE_FORMATTER,  getProperty(PROPERTY_HITHIGHLIGHTING_PRE_FORMATTER, "<strong>"));
        getRequest().setAttribute(JSP_ATTR_HITHIGHLIGHTING_POST_FORMATTER,  getProperty(PROPERTY_HITHIGHLIGHTING_POST_FORMATTER, "</strong>"));
        getRequest().setAttribute(JSP_ATTR_HITHIGHLIGHTING_FRAGMENT_SIZE,  getProperty(PROPERTY_HITHIGHLIGHTING_FRAGMENT_SIZE, "300"));
        getRequest().setAttribute(JSP_ATTR_HITHIGHLIGHTING_SNIPPETS,  getProperty(PROPERTY_HITHIGHLIGHTING_SNIPPETS, "3"));
        getRequest().setAttribute(JSP_ATTR_HITHIGHLIGHTING_AVAILABLE_FIELDS,  getProperty(PROPERTY_HITHIGHLIGHTING_AVAILABLE_FIELDS, new String[]{}));

        // Advanced settings  - Filter Queries
        String filterQueries[] = getProperty(PROPERTY_ADVANCED_FILTER_QUERIES, new String[]{});
        getRequest().setAttribute(JSP_ATTR_ADVANCED_FILTER_QUERIES, filterQueries);
        getRequest().setAttribute(JSP_ATTR_ADVANCED_FILTER_QUERIES_ENABLED, hasFilterQueries(filterQueries));
        getRequest().setAttribute(JSP_ATTR_ADVANCED_FILTER_QUERIES_IN_BREADBOX_ENABLED,  getProperty(PROPERTY_ADVANCED_FILTER_QUERIES_IN_BREADBOX_ENABLED, false));
        getRequest().setAttribute(JSP_ATTR_ADVANCED_SEARCH_HANDLER,  getProperty(PROPERTY_ADVANCED_SEARCH_HANDLER, ""));

    }

    private String formatSolrEndPointAndCore() {

        SolrConfigurationService solrConfigService =
                (SolrConfigurationService) getService(SolrConfigurationService.class);

        if (null == solrConfigService) {
            LOG.error("Can't get SolrConfigurationService. Check that all OSGi bundles are active");
        }

        StringBuilder endPointAndCore = new StringBuilder();
        endPointAndCore.append(solrConfigService.getSolrEndPoint())
                .append("/")
                .append(solrCore)
                .append("/");
        return endPointAndCore.toString();
    }

    private String getSolrProxyURL() {

        SolrConfigurationService solrConfigService =
                (SolrConfigurationService) getService(SolrConfigurationService.class);

        if (null == solrConfigService) {
            LOG.error("Can't get SolrConfigurationService. Check that all OSGi bundles are active");
        }

        return solrConfigService.getProxyUrl();
    }

    private boolean isSolrProxyEnabled() {

        SolrConfigurationService solrConfigService =
                (SolrConfigurationService) getService(SolrConfigurationService.class);

        if (null == solrConfigService) {
            LOG.error("Can't get SolrConfigurationService. Check that all OSGi bundles are active");
        }

        return solrConfigService.isProxyEnabled();
    }

    private List<Facet> convertToFacets(String propertyName) {

        List<Facet> facets = new ArrayList<Facet>();

        String[] values =  (String[]) getProperties().get(propertyName, String[].class);
        if (values != null) {
            for (String value: values) {
                String keyAndPair[] = value.split(":");
                if (keyAndPair != null && keyAndPair.length == 2) {
                    facets.add(new Facet(keyAndPair[0], keyAndPair[1]));
                }  else {
                    LOG.warn("Can't add available facet '{}' to list. Either facet key and/or name is missing", value);
                }
            }
        }
        return facets;
    }

    private String[] convertFacetsToKeys(List<Facet> facets) {

       if (null == facets) {
        return StringUtil.EMPTY_STRING_ARRAY;
       }

        String[] facetKeys = new String[facets.size()];
        for (int i = 0; i < facets.size(); i++) {
            facetKeys[i] =  facets.get(i).getKey();
        }

        return facetKeys;
    }

    private boolean hasFilterQueries(String filterQueries[]) {
        return (filterQueries != null && filterQueries.length > 0);
    }

    // TODO: Refactor: Extract getProperty() methods to class

    private Boolean getProperty(String propertyName, Boolean defaultValue) {
        Boolean value = getProperties().get(propertyName, Boolean.class);
        return (null == value) ? defaultValue : value;
    }

    private String getProperty(String propertyName, String defaultValue) {
        String value = getProperties().get(propertyName, String.class);
        return (null == value) ? defaultValue : value;
    }

    private String[] getProperty(String propertyName, String[] defaultValue) {
        String[] values =  (String[]) getProperties().get(propertyName, String[].class);
        return (null == values) ? defaultValue : values;
    }

    private boolean isConfigured() {
        return hasSolrCoreDefined();
    }

    private boolean hasSolrCoreDefined() {
        return solrCore != null;
    }
}
