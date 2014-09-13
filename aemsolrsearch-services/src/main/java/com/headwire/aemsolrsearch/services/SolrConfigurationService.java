/*
 * Author  : Gaston Gonzalez
 * Date    : 6/30/13
 * Version : $Id$
 */
package com.headwire.aemsolrsearch.services;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.felix.scr.annotations.*;
import org.apache.felix.scr.annotations.Properties;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Component(
        name = "com.headwire.aemsolrsearch.services.SolrConfigurationService",
        label = "AEM Solr Search - Solr Configuration Service",
        description = "A service for configuring Solr",
        immediate = true,
        metatype = true)
@Service(SolrConfigurationService.class)
@Properties({
        @Property(
                name = Constants.SERVICE_VENDOR,
                value = "headwire.com, Inc."),
        @Property(
                name = Constants.SERVICE_DESCRIPTION,
                value = "Solr configuration service"),
        @Property(
                name = SolrConfigurationServiceAdminConstants.PROTOCOL,
                value = "http",
                label = "Protocol",
                description = "Either 'http' or 'https'"),
        @Property(
            name = SolrConfigurationServiceAdminConstants.SERVER_NAME,
            value = "localhost",
            label = "Server Name",
            description = "Server name or IP address"),
        @Property(
            name = SolrConfigurationServiceAdminConstants.SERVER_PORT,
            value = "8983",
            label = "Server Port",
            description = "Server port"),
        @Property(
            name = SolrConfigurationServiceAdminConstants.CONTEXT_PATH,
            value = "/solr",
            label = "Context Path",
            description = "Solr application context path"),
        @Property(
            name = SolrConfigurationServiceAdminConstants.PROXY_ENABLED,
            value = "true",
            label = "Enable Proxy",
            description = "Enable Proxy. Must be either 'true' or 'false'"),
        @Property(
                name = SolrConfigurationServiceAdminConstants.PROXY_URL,
                value = "http://localhost:4502/apps/solr/proxy",
                label = "Proxy URL",
                description = "Absolute proxy URL")
})
/**
 * SolrConfigurationService provides a services for setting and getting Solr configuration information.
 */
public class SolrConfigurationService {

    private static final Logger LOG = LoggerFactory.getLogger(SolrConfigurationService.class);
    private String solrEndPoint;
    private String protocol;
    private String serverName;
    private String serverPort;
    private String contextPath;
    private String proxyUrl;
    private boolean proxyEnabled;

    public static final String DEFAULT_PROTOCOL = "http";
    public static final String DEFAULT_SERVER_NAME = "localhost";
    public static final String DEFAULT_SERVER_PORT = "8993";
    public static final String DEFAULT_CONTEXT_PATH = "/solr";
    public static final String DEFAULT_PROXY_URL = "http://localhost:4502/apps/solr/proxy";

    /**
     * Returns the Solr endpoint as a full URL.
     * @return
     */
    public String getSolrEndPoint() {
        return solrEndPoint;
    }

    public String getProxyUrl() {
        return proxyUrl;
    }

    public boolean isProxyEnabled() {
        return proxyEnabled;
    }

    /**
     * Retrieves a list of available Solr cores.
     *
     * @return a list of available cores on success, and an empty list otherwise.
     */
    public List<String> getCores() {

        List<String> cores = new ArrayList<String>();

        // TODO: FogBugz #14 - Refactor HTTP implementation.
        HttpClient httpClient = new HttpClient();
        HttpMethod method = new GetMethod(getSolrEndPoint() + "/admin/cores?action=STATUS&wt=json");

        try {
            int statusCode = httpClient.executeMethod(method);

            if (statusCode != HttpStatus.SC_OK) {
                LOG.error("Method failed: {}", method.getStatusLine());
            }

            // TODO: Need a more efficent way. Does this support UTF-8 encoding properly?
            byte[] responseBody = method.getResponseBody();
            String solrReponse = new String(responseBody);

            JSONParser parser = new JSONParser();
            Object jsonData = parser.parse(solrReponse);
            JSONObject obj = (JSONObject) jsonData;

            JSONObject coreStatus = (JSONObject)obj.get("status");
            Set<String> coreNames = coreStatus.keySet();
            for (String coreName: coreNames) {
                cores.add(coreName);
            }

        } catch (Exception e) {
            LOG.error("Error fetching Solr cores", e);
        } finally {
            method.releaseConnection();
        }

        return cores;
    }

    /**
     * Returns all Solr fields that have <code>stored</code> set to <code>true</code>.
     *
     * @param  solrCore Solr core name
     * @return a list of stored fields on success, and an empty list otherwise.
     */
    public List<String> getStoredFields(String solrCore) {

        List<String> storedFields = new ArrayList<String>();

        StringBuilder schemaFieldsEndPoint = new StringBuilder();
        schemaFieldsEndPoint.append(getSolrEndPoint());
        schemaFieldsEndPoint.append("/");
        schemaFieldsEndPoint.append(solrCore);
        schemaFieldsEndPoint.append("/schema/fields");

        LOG.info("Looking up available stored fields {}", schemaFieldsEndPoint.toString());

        // TODO: Avoid code duplication. Refactor. Create an HTTP utility class.
        HttpClient httpClient = new HttpClient();
        HttpMethod method = new GetMethod(schemaFieldsEndPoint.toString());

        try {
            int statusCode = httpClient.executeMethod(method);

            if (statusCode != HttpStatus.SC_OK) {
                LOG.error("RESTful call to {} failed: {}", schemaFieldsEndPoint.toString(),
                        method.getStatusLine());
            }

            // TODO: Need a more efficent way. Does this support UTF-8 encoding properly?
            byte[] responseBody = method.getResponseBody();
            String solrReponse = new String(responseBody);

            JSONParser parser = new JSONParser();
            Object jsonData = parser.parse(solrReponse);
            JSONObject obj = (JSONObject) jsonData;

            JSONArray schemaFields = (JSONArray)obj.get("fields");
            Iterator fields = schemaFields.iterator();
            while (fields.hasNext()) {
                JSONObject facetField = (JSONObject)fields.next();
                Boolean stored = (Boolean) facetField.get("stored");

                if (stored) {
                    String fieldName = (String)facetField.get("name");
                    storedFields.add(fieldName);
                }
            }

            LOG.info("Found stored fields [{}] for {}", storedFields, schemaFieldsEndPoint);

        } catch (Exception e) {
            LOG.error("Error fetching stored fields from {}", schemaFieldsEndPoint.toString(), e);
        } finally {
            method.releaseConnection();
        }

        return storedFields;
    }

    /**
     * Returns all Solr fields that have <code>indexed</code> set to <code>true</code>
     *
     * @param  solrCore Solr core name
     * @return a list of indexed fields on success, and an empty list otherwise.
     */
    public List<String> getIndexedFields(String solrCore) {

        List<String> availableIndexedFields = new ArrayList<String>();

        StringBuilder schemaFieldsEndPoint = new StringBuilder();
        schemaFieldsEndPoint.append(getSolrEndPoint());
        schemaFieldsEndPoint.append("/");
        schemaFieldsEndPoint.append(solrCore);
        schemaFieldsEndPoint.append("/schema/fields");

        LOG.info("Looking up available indexed fields {}", schemaFieldsEndPoint.toString());

        // TODO: Avoid code duplication. Refactor. Create an HTTP utility class.
        HttpClient httpClient = new HttpClient();
        HttpMethod method = new GetMethod(schemaFieldsEndPoint.toString());

        try {
            int statusCode = httpClient.executeMethod(method);

            if (statusCode != HttpStatus.SC_OK) {
                LOG.error("RESTful call to {} failed: {}", schemaFieldsEndPoint.toString(),
                        method.getStatusLine());
            }

            // TODO: Need a more efficent way. Does this support UTF-8 encoding properly?
            byte[] responseBody = method.getResponseBody();
            String solrReponse = new String(responseBody);

            JSONParser parser = new JSONParser();
            Object jsonData = parser.parse(solrReponse);
            JSONObject obj = (JSONObject) jsonData;

            JSONArray schemaFields = (JSONArray)obj.get("fields");
            Iterator facetFields = schemaFields.iterator();
            while (facetFields.hasNext()) {
                JSONObject facetField = (JSONObject)facetFields.next();
                Boolean indexed = (Boolean) facetField.get("indexed");

                if (indexed) {
                    String fieldName = (String)facetField.get("name");
                    availableIndexedFields.add(fieldName);
                }
            }

            LOG.info("Found indexed fields [{}] for {}", availableIndexedFields, schemaFieldsEndPoint);

        } catch (Exception e) {
            LOG.error("Error fetching available indexed fields from {}", schemaFieldsEndPoint.toString(), e);
        } finally {
            method.releaseConnection();
        }

        return availableIndexedFields;
    }

    /**
     * Returns all Solr fields that have <code>indexed</code> set to <code>true</code>. This implementation uses
     * the <code>LukeRequestHandler</code>, as such this method will return dynamic fields as well.
     *
     * @param  solrCore Solr core name
     * @return a list of indexed fields on success, and an empty list otherwise.
     */
    public List<String> getIndexedFieldsFromLuke(String solrCore) {

        List<String> availableIndexedFields = new ArrayList<String>();

        StringBuilder schemaFieldsEndPoint = new StringBuilder();
        schemaFieldsEndPoint.append(getSolrEndPoint());
        schemaFieldsEndPoint.append("/");
        schemaFieldsEndPoint.append(solrCore);
        schemaFieldsEndPoint.append("/admin/luke?wt=json");

        LOG.info("Looking up available indexed fields using Luke: {}", schemaFieldsEndPoint.toString());

        // TODO: Avoid code duplication. Refactor. Create an HTTP utility class.
        HttpClient httpClient = new HttpClient();
        HttpMethod method = new GetMethod(schemaFieldsEndPoint.toString());

        try {
            int statusCode = httpClient.executeMethod(method);

            if (statusCode != HttpStatus.SC_OK) {
                LOG.error("RESTful call to {} failed: {}", schemaFieldsEndPoint.toString(),
                        method.getStatusLine());
            }

            // TODO: Need a more efficent way. Does this support UTF-8 encoding properly?
            byte[] responseBody = method.getResponseBody();
            String solrReponse = new String(responseBody);

            JSONParser parser = new JSONParser();
            Object jsonData = parser.parse(solrReponse);
            JSONObject obj = (JSONObject) jsonData;


            JSONObject fields = (JSONObject)obj.get("fields");
            Set<String> schemaFields = fields.keySet();
            for (String fieldName: schemaFields) {
                JSONObject field = (JSONObject)fields.get(fieldName);
                // It seems that the output for Luke changed in Solr 4.6. "index" does not seem to be a reliable
                // field to extract information about the field as there are instances where "(unstored field)" is
                // defined. "schema" seems to be the preferred option.
                String indexFlags = (String)field.get("schema");
                if (indexFlags != null && indexFlags.indexOf("I") != -1) {
                    LOG.debug("Found indexable field name: {}", fieldName);
                    availableIndexedFields.add(fieldName.toString());
                }

            }

            LOG.info("Found indexed fields [{}] for {}", availableIndexedFields, schemaFieldsEndPoint);

        } catch (Exception e) {
            LOG.error("Error fetching available indexed fields from {}", schemaFieldsEndPoint.toString(), e);
        } finally {
            method.releaseConnection();
        }

        return availableIndexedFields;
    }

    @Activate
    protected void activate(final Map<String, String> config) {
        resetService(config);
    }

    @Modified
    protected void modified(final Map<String, String> config) {
        resetService(config);
    }

    private synchronized void resetService(final Map<String, String> config) {
        LOG.info("Resetting Solr configuration service using configuration: " + config);

        protocol = config.containsKey(SolrConfigurationServiceAdminConstants.PROTOCOL) ?
            config.get(SolrConfigurationServiceAdminConstants.PROTOCOL) : DEFAULT_PROTOCOL;

        serverName = config.containsKey(SolrConfigurationServiceAdminConstants.SERVER_NAME) ?
                config.get(SolrConfigurationServiceAdminConstants.SERVER_NAME) : DEFAULT_SERVER_NAME;

        serverPort = config.containsKey(SolrConfigurationServiceAdminConstants.SERVER_PORT) ?
                config.get(SolrConfigurationServiceAdminConstants.SERVER_PORT) : DEFAULT_SERVER_PORT;

        contextPath = config.containsKey(SolrConfigurationServiceAdminConstants.CONTEXT_PATH) ?
                config.get(SolrConfigurationServiceAdminConstants.CONTEXT_PATH) : DEFAULT_CONTEXT_PATH;

        solrEndPoint = formatSolrEndPoint();

        proxyUrl = config.containsKey(SolrConfigurationServiceAdminConstants.PROXY_URL) ?
                config.get(SolrConfigurationServiceAdminConstants.PROXY_URL) : DEFAULT_PROXY_URL;

        proxyEnabled = config.containsKey(SolrConfigurationServiceAdminConstants.PROXY_ENABLED) ?
                Boolean.parseBoolean(config.get(SolrConfigurationServiceAdminConstants.PROXY_ENABLED)) : true;
    }

    private String formatSolrEndPoint() {

        StringBuilder url = new StringBuilder();
        url.append(protocol).append("://").append(serverName);
        if (!"80".equals(serverPort)) url.append(":").append(serverPort);
        url.append(contextPath);

        return url.toString();
    }

}
