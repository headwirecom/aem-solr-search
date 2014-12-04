/*
 * Author  : Gaston Gonzalez
 * Date    : 8/12/13
 * Version : $Id$
 */
package com.headwire.aemsolrsearch.services.servlets;

import com.headwire.aemsolrsearch.services.SolrConfigurationService;
import org.apache.felix.scr.annotations.*;
import org.apache.sling.api.SlingHttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

@Service
@Component(
        name = "com.headwire.aemsolrsearch.services.servlets.SolrProxyServlet",
        metatype = true,
        immediate = true,
        label = "Solr Proxy Servlet",
        description = "Solr Proxy Servlet"
)
@Properties({
        @Property(
                name = "sling.servlet.paths",
                value = { "/apps/solr/proxy" },
                propertyPrivate = true
        ),
        @Property(
                name = "sling.servlet.methods",
                value = { "GET" },
                propertyPrivate = true
        ),
        @Property(
                name = "service.description",
                value = "Provides a proxy for AJAX Solr",
                propertyPrivate = true
        )
})
/**
 * SolrProxyServlet provides a basic proxy for Ajax Solr.
 *
 * @author <a href="mailto:gg@headwire.com">Gaston Gonzalez</a>
 */
public class SolrProxyServlet extends ProxyServlet {

    private static final Logger LOG = LoggerFactory.getLogger(ProxyServlet.class);
    @Reference
    private SolrConfigurationService solrConfigurationService;

    public static final int MAX_ALLOWED_ROWS = 30; // TODO configure via Solr Configuration Service
    public static final String CORE_NAME_PARAM = "corename";
    public static final String SOLR_ROWS_PARAM = "rows";

    @Override
    public String getTargetURI(SlingHttpServletRequest request) {

        String searchHandler = (request.getParameter("qt") != null) ? request.getParameter("qt") : "/select";
        return solrConfigurationService.getSolrEndPoint() + "/" +
                request.getParameter(CORE_NAME_PARAM) + searchHandler;
    }

    @Override
    public String sanitizeQueryString(String queryString, HttpServletRequest request) {

        String rows = request.getParameter(SOLR_ROWS_PARAM);
        if (rows != null && hasIllegalRowValue(rows)) {
            queryString = queryString.replace("rows=" + rows, "rows=" + MAX_ALLOWED_ROWS);
            LOG.info("Modified query string to {}", queryString);
        }
        return queryString;
    }

    private boolean hasIllegalRowValue(String rows) {

        try {
            int numRows = Integer.parseInt(rows);

            if (numRows > MAX_ALLOWED_ROWS) {
                LOG.warn("Illegal 'rows' parameter detected. Value was '{}', but cannot exceed '{}",
                        numRows, MAX_ALLOWED_ROWS);
                return true;
            }
        } catch (Exception e) {
            LOG.error("Illegal 'rows' parameter detected.", e);
            return true;
        }

        return false;

    }

    @Override
    public boolean isValidRequest(SlingHttpServletRequest request) {
        final String searchHandler = (request.getParameter("qt") != null) ? request.getParameter("qt") : "/select";
        return solrConfigurationService.isRequestHandlerAllowed(searchHandler);
    }
}
