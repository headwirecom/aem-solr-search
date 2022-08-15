package com.headwire.aemsolrsearch.proxy.controller;

import com.headwire.aemsolrsearch.proxy.service.SolrProxyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController @RequestMapping("/solrproxy")
public class SolrProxyController extends ProxyController {

    private static final Logger LOG = LoggerFactory.getLogger(SolrProxyController.class);

    @Value("${solr.allowed.rows.max}") public int MAX_ALLOWED_ROWS;

    private static final String CORE_NAME_PARAM = "corename";
    private static final String SOLR_ROWS_PARAM = "rows";

    @Value("${solr.endpoint.url}")
    private String solrEndPoint;

    @Autowired
    private SolrProxyService solrProxyService;

    @Override public String getTargetURI(HttpServletRequest request) {

        final String searchHandler =
            (request.getParameter("qt") != null) ? request.getParameter("qt") : "/select";
        final String logicalCoreName = request.getParameter(CORE_NAME_PARAM);

        if(StringUtils.isEmpty(logicalCoreName)){
            LOG.info("The request missing the required Solr core name.");
        }

        String core = "";
        if (solrProxyService.isCloudMode()) {
            List<String> shards = solrProxyService.fetchCollectionShards();
            for (String shard : shards) {
                if (shard.startsWith(logicalCoreName)) {
                    core = shard;
                    break;
                }
            }
        } else {

            core = logicalCoreName;
        }

        final String targetUri = solrEndPoint + "/" + core + searchHandler;

        LOG.debug("Using '{}' for proxy search", targetUri);

        return targetUri;
    }

    @Override public String sanitizeQueryString(String queryString, HttpServletRequest request) {

        String rows = request.getParameter(SOLR_ROWS_PARAM);
        if (rows != null && hasIllegalRowValue(rows)) {
            queryString = queryString.replace("rows=" + rows, "rows=" + MAX_ALLOWED_ROWS);
            LOG.info("Modified query string to {}", queryString);
        }

        //Query Parameter
        String query = request.getParameter("q");
        if (query == null) {
            queryString = StringUtils.isEmpty(queryString) ?
                queryString.concat("?q=*") :
                queryString.concat("&q=*");
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
    public boolean isValidRequest(HttpServletRequest request) {
        final String searchHandler =
            (request.getParameter("qt") != null) ? request.getParameter("qt") : "/select";
        return solrProxyService.isRequestHandlerAllowed(searchHandler);
    }


}
