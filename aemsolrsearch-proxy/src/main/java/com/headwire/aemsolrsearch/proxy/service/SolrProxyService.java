/*
 * Author  : Gaston Gonzalez
 * Date    : 6/30/13
 * Version : $Id$
 */
package com.headwire.aemsolrsearch.proxy.service;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.impl.XMLResponseParser;
import org.apache.solr.client.solrj.request.CoreAdminRequest;
import org.apache.solr.client.solrj.response.CoreAdminResponse;
import org.apache.solr.common.params.CoreAdminParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class SolrProxyService {

    private static final Logger LOG = LoggerFactory.getLogger(SolrProxyService.class);

    @Value("#{'${solr.allowed.request.handlers}'.split(',')}")
    private List<String> allowedRequestHandlers;

    @Value("${solr.zkHost}")
    private String solrZKHost;

    @Value("${solr.endpoint.url}")
    private String solrEndPoint;

    @Value("${solr.cloud.mode}")
    private Boolean solrCloudMode;

    private static SolrClient solrClient = null;

    public List<String> fetchCollectionShards() {

        SolrClient client = getSolrClient();

        CoreAdminRequest request = new CoreAdminRequest();
        request.setAction(CoreAdminParams.CoreAdminAction.STATUS);

        List<String> coreList = new ArrayList<String>();

        try {
            CoreAdminResponse cores = request.process(client);

            // List of the cores
            for (int i = 0; i < cores.getCoreStatus().size(); i++) {

                LOG.debug("Fetched Core {} of Standalone Solr", cores.getCoreStatus().getName(i));

                coreList.add(cores.getCoreStatus().getName(i));

            }

        } catch (SolrServerException e) {
            LOG.error("Error fetching Standalone Solr Core.", e);

        } catch (IOException e) {
            LOG.error("Error fetching Standalone Solr Core.", e);
        }

        return coreList;
    }

    public boolean isRequestHandlerAllowed(String requestHandler) {

        if (StringUtils.isBlank(requestHandler)) { return false; }

        for (String whitelist: allowedRequestHandlers) {
            if (whitelist.equalsIgnoreCase(requestHandler)) { return true; }
        }

        return false;
    }

    /**
     * Retrieve a particular instance of SolrClient.
     */
    private SolrClient getSolrClient() {

        if (null != solrClient) {
            LOG.info("Returning existing instance of Solr Server", solrClient);
        } else {
            solrClient = getCloudSolrClient();
        }
        return solrClient;
    }

    private synchronized SolrClient getCloudSolrClient() {

        // Double check existence while in synchronized method.
        if (solrClient != null) {
            return solrClient;
        }

        LOG.debug("Creating CloudSolrClient using ZooKeeper: '{}'", solrZKHost);

        if (StringUtils.isBlank(solrZKHost)) {
            solrZKHost = solrEndPoint;
        }

        CloudSolrClient client = new CloudSolrClient(solrZKHost);
        client.setParser(new XMLResponseParser());

        return client;
    }

    public Boolean isCloudMode() {

        return solrCloudMode;

    }

}
