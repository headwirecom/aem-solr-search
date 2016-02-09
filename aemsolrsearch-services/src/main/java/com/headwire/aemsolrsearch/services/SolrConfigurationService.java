/*
 * Author  : Gaston Gonzalez
 * Date    : 6/30/13
 * Version : $Id$
 */
package com.headwire.aemsolrsearch.services;

import com.headwire.aemsolrsearch.exception.AEMSolrSearchException;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.*;
import org.apache.felix.scr.annotations.Properties;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.impl.LBHttpSolrClient;
import org.apache.solr.client.solrj.impl.XMLResponseParser;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
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
        name = SolrConfigurationServiceAdminConstants.PROXY_ENABLED,
        value = "true",
        label = "Enable Proxy",
        description = "Enable Proxy. Must be either 'true' or 'false'"),
    @Property(
        name = SolrConfigurationServiceAdminConstants.PROXY_URL,
        value = "http://localhost:4502/apps/solr/proxy",
        label = "Proxy URL",
        description = "Absolute proxy URL"),
    @Property(
        name = SolrConfigurationServiceAdminConstants.ALLOWED_REQUEST_HANDLERS,
        value = { "/select", "/geometrixx-media-search" },
        label = "Allowed request handlers",
        description = "Whitelist of allowed request handlers"),
    @Property(
        name = SolrConfigurationServiceAdminConstants.SOLR_MODE,
        value = "Standalone",
        options = {
            @PropertyOption(name = "Standalone", value = "Standalone"),
            @PropertyOption(name = "SolrCloud", value = "SolrCloud")
        }
    ),
    @Property(
        name = SolrConfigurationServiceAdminConstants.SOLR_ZKHOST,
        value = {"localhost:9983"},
        label = "SOLR ZooKeeper Hosts",
        description = "A comma delimited list of ZooKeeper hosts "
            + "using the same format expected by CloudSolrClient"),
    @Property(
        name = SolrConfigurationServiceAdminConstants.SOLR_MASTER,
        value = { "http://localhost:8888/solr" },
        label = "Master Server",
        description = "The master Solr server is formatted as follows: <scheme>://<host>:<port>/<solr context>"
            + " (i.e., http://solr1.example.com:8080/solr)."),
    @Property(
        name = SolrConfigurationServiceAdminConstants.SOLR_SLAVES,
        value = { "" },
        label = "An optional set of zero or more Solr slave servers.",
        description = "This property will be considered for Solr mode = 'Standalone' only. "
            + "An optional set of zero or more Solr slave servers. "
            + "Each Solr Slave is formatted as: <scheme>://<host>:<port>/<solr context> "
            + "(i.e., http://solr2.example.com:8080/solr)."),
    @Property(
        name = SolrConfigurationServiceAdminConstants.ALLOWED_SOLR_MASTER_QUERIES,
        boolValue = false,
        label = "Allowed Solr Master Queries",
        description = "This property will be considered for Solr mode = 'Standalone'. "
            + "A boolean flag indicating if the Solr master should receive queries. "
            + "Default is false. If true, the master and slaves will both receive queries "
            + "using Solr's software load balancer.")
})
/**
 * SolrConfigurationService provides a services for setting and getting Solr configuration information.
 */
public class SolrConfigurationService {

    private static final Logger LOG = LoggerFactory.getLogger(SolrConfigurationService.class);
    private String solrEndPoint;
    private String proxyUrl;
    private boolean proxyEnabled;
    private String[] allowedRequestHandlers;
    private String solrMode;
    private String solrZKHost;
    private String solrMaster;
    private String solrSlaves;
    private boolean solrAllowMasterQueriesEnabled;

    public static final String DEFAULT_PROXY_URL = "http://localhost:4502/apps/solr/proxy";
    public static final String[] DEFAULT_ALLOWED_REQUEST_HANDLERS = new String[] { "/select", "/geometrixx-media-search" };
    public static final String DEFAULT_SOLR_MODE = "Standalone";
    public static final String DEFAULT_SOLR_MASTER = "http://localhost:8888/solr";
    public static final String DEFAULT_SOLR_ZKHOST = "localhost:9983";
    public static final boolean DEFAULT_ALLOW_MASTER_QUERIES = false;

    public static final String SOLR_MODE_STANDALONE = "Standalone";
    public static final String SOLR_MODE_SOLRCLOUD = "SolrCloud";

    private static final Map<String, SolrClient> solrClientByOperation = new HashMap<String, SolrClient>();
    public static final String SOLR_INDEX_OPERATION = "INDEX";
    public static final String SOLR_QUERY_OPERATION = "QUERY";

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

    public boolean isRequestHandlerAllowed(String requestHandler) {

        if (StringUtils.isBlank(requestHandler)) { return false; }

        for (String whitelist: allowedRequestHandlers) {
            if (whitelist.equalsIgnoreCase(requestHandler)) { return true; }
        }

        return false;
    }

    @Activate
    protected void activate(final Map<String, Object> config) {
        resetService(config);
    }

    @Modified
    protected void modified(final Map<String, Object> config) {
        resetService(config);
    }

    private synchronized void resetService(final Map<String, Object> config) {
        LOG.info("Resetting Solr configuration service using configuration: " + config);

        proxyUrl = config.containsKey(SolrConfigurationServiceAdminConstants.PROXY_URL) ?
            (String)config.get(SolrConfigurationServiceAdminConstants.PROXY_URL) : DEFAULT_PROXY_URL;

        proxyEnabled = config.containsKey(SolrConfigurationServiceAdminConstants.PROXY_ENABLED) ?
            Boolean.parseBoolean((String)config.get(SolrConfigurationServiceAdminConstants.PROXY_ENABLED)) : true;

        allowedRequestHandlers = config.containsKey(SolrConfigurationServiceAdminConstants.ALLOWED_REQUEST_HANDLERS)
            ? (String[])config.get(SolrConfigurationServiceAdminConstants.ALLOWED_REQUEST_HANDLERS)
            : DEFAULT_ALLOWED_REQUEST_HANDLERS;
        solrMode = config.containsKey(SolrConfigurationServiceAdminConstants.SOLR_MODE) ?
            (String) config.get(SolrConfigurationServiceAdminConstants.SOLR_MODE) :
            DEFAULT_SOLR_MODE;

        solrMaster = config.containsKey(SolrConfigurationServiceAdminConstants.SOLR_MASTER) ?
            (String) config.get(SolrConfigurationServiceAdminConstants.SOLR_MASTER) :
            DEFAULT_SOLR_MASTER;

        solrEndPoint = solrMaster;

        if (SOLR_MODE_SOLRCLOUD.equals(solrMode)) {

            solrZKHost = config.containsKey(SolrConfigurationServiceAdminConstants.SOLR_ZKHOST) ?
                (String) config.get(SolrConfigurationServiceAdminConstants.SOLR_ZKHOST) :
                DEFAULT_SOLR_ZKHOST;
        }

        if (SOLR_MODE_STANDALONE.equals(solrMode)) {
            //support MASTER/SLAVE
            configureMasterSlaveInt(config);
        }

        clearSolrClient();

    }

    private void configureMasterSlaveInt(Map<String, Object> config) {

        solrSlaves = config.containsKey(SolrConfigurationServiceAdminConstants.SOLR_SLAVES) ?
            (String) config.get(SolrConfigurationServiceAdminConstants.SOLR_SLAVES) :
            null;

        solrAllowMasterQueriesEnabled =
            config.containsKey(SolrConfigurationServiceAdminConstants.ALLOWED_SOLR_MASTER_QUERIES) ?
                (Boolean) config
                    .get(SolrConfigurationServiceAdminConstants.ALLOWED_SOLR_MASTER_QUERIES) :
                DEFAULT_ALLOW_MASTER_QUERIES;

    }

    public SolrClient getIndexingSolrClient() {

        return getSolrClient(SOLR_INDEX_OPERATION);

    }

    public SolrClient getQueryingSolrClient() {

        return getSolrClient(SOLR_QUERY_OPERATION);
    }

    /** Retrieve a particular instance of SolrClient identified by the Operation. */
    private SolrClient getSolrClient(String solrOperation) {

        final SolrClient existingSolrServer = solrClientByOperation.get(solrOperation);
        if (null != existingSolrServer) {
            LOG.info("Returning existing instance of Solr Server {} for operation {}", existingSolrServer, solrOperation);
            return existingSolrServer;
        } else {
            synchronized (solrClientByOperation) {
                // Double check existence while in synchronized block.
                if (solrClientByOperation.containsKey(solrOperation)) {
                    return solrClientByOperation.get(solrOperation);
                } else {
                    SolrClient client = null;
                    if (SOLR_MODE_SOLRCLOUD.equals(solrMode)) {
                        client = getCloudSolrClient();
                    } else {
                        if (SOLR_INDEX_OPERATION.equals(solrOperation)) {
                            client = getStandaloneIndexSolrClient();
                        } else if (SOLR_QUERY_OPERATION.equals(solrOperation)) {
                            client = getStandaloneQuerySolrClient();
                        }

                    }
                    solrClientByOperation.put(solrOperation, client);
                    LOG.info("Returning NEW instance of Solr Server {} for operation {}", client, solrOperation);
                    return client;
                }
            }
        }
    }

    private SolrClient getCloudSolrClient() {

        LOG.debug("Creating CloudSolrClient using solrMaster {}", solrMaster);
        CloudSolrClient client =  new CloudSolrClient(solrZKHost);
        client.setParser(new XMLResponseParser());

        return client;
    }

    private SolrClient getStandaloneQuerySolrClient() {

        LBHttpSolrClient lbHttpSolrClient = null;

        try {
            if (StringUtils.isEmpty(solrSlaves) && StringUtils.isNotEmpty(solrMaster)) {

                LOG.debug("Creating LBHttpSolrClient using solrMaster {}", solrMaster);
                lbHttpSolrClient = new LBHttpSolrClient(solrMaster);

            } else if (StringUtils.isNotEmpty(solrSlaves)) {

                LOG.debug("Creating LBHttpSolrClient using solrSlaves {}", solrSlaves);
                lbHttpSolrClient = new LBHttpSolrClient(solrSlaves);

                if (solrAllowMasterQueriesEnabled && StringUtils.isNotEmpty(solrMaster)) {

                    LOG.debug("Adding solrMaster {} to the LBHttpSolrClient", solrSlaves);
                    lbHttpSolrClient.addSolrServer(solrMaster);
                }

            } else if (StringUtils.isEmpty(solrSlaves) && StringUtils.isEmpty(solrMaster)) {
                // unexpected
                throw new AEMSolrSearchException("Initialization failed. "
                    + "Either 'solr.master' or 'solr.slaves' properties are missing for Standalone mode.");

            } else {
                // Do nothing
            }
        } catch (MalformedURLException e) {
            LOG.error("Error for malformed URL:  {}", e);
        } catch (AEMSolrSearchException e) {
            LOG.error("Solr client initialization failed. {}", e);
        }

        lbHttpSolrClient.setParser(new XMLResponseParser());
        return lbHttpSolrClient;

    }

    private SolrClient getStandaloneIndexSolrClient() {

        if (StringUtils.isEmpty(solrMaster)) {
            try {
                throw new AEMSolrSearchException(
                    "Initialization failed. The property 'solr-master' is missing for Standalone mode.");
            } catch (AEMSolrSearchException e) {
                LOG.info("Solr client initialization failed. {}", e);
            }
        }

        LOG.debug("Creating HttpSolrClient using solrMaster {}", solrMaster);
        return  new HttpSolrClient(solrMaster, null, new XMLResponseParser());
    }

    public void clearSolrClient(){
        solrClientByOperation.clear();
    }

}