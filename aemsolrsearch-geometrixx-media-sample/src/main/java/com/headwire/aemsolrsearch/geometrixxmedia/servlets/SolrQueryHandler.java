package com.headwire.aemsolrsearch.geometrixxmedia.servlets;

import com.day.cq.search.QueryBuilder;
import com.headwire.aemsolrsearch.services.SolrConfigurationService;
import org.apache.felix.scr.annotations.*;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.jcr.api.SlingRepository;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.json.simple.JSONArray;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;

@Component(
        name = "com.headwire.aemsolrsearch.geometrixxmedia.servlets.SolrQueryHandler",
        label = "AEM Solr Search - Geometrixx Media Solr Query Handler",
        description = "Serializes content to Solr's JSON Query handler format",
        immediate = true,
        metatype = true)
@Service(Servlet.class)
@Properties({
    @Property(name = Constants.SERVICE_VENDOR, value = "headwire.com, Inc."),
    @Property(name = Constants.SERVICE_DESCRIPTION, value = "Serializes content to Solr's JSON Query handler format"),
    @Property(name = "sling.servlet.methods", value = "GET"),
    @Property(name = "solr.core", value = "collection1"),
    @Property(name = "sling.servlet.paths", value = "/apps/geometrixx-media/solr/queryhandler")
})
public class SolrQueryHandler extends SlingSafeMethodsServlet {

    private static final Logger LOG = LoggerFactory.getLogger(SolrQueryHandler.class);
    public static final String REQ_PARAM_TYPE = "type"; // sling:resourceType

    @Reference
    private QueryBuilder queryBuilder;

    @Reference
    private SlingRepository repository;

    @Reference
    private SolrConfigurationService solrConfigurationService;

    private String core = "collection1";
    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");

        JSONArray solrDocs = new JSONArray();

        SolrClient solr = solrConfigurationService.getQueryingSolrClient();

        try {

            QueryResponse result = solr.query(core, new SolrQuery("*:*"));
            SolrDocumentList list=result.getResults();
            response.getWriter().write(list.toString());

        } catch (SolrServerException e) {
            e.printStackTrace();
        }


    }

}