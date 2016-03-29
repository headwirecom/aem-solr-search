/*
 * Author  : Gaston Gonzalez
 * Date    : 6/30/13
 * Version : $Id$
 */
package com.headwire.aemsolrsearch.services;

import com.headwire.aemsolrsearch.exception.AEMSolrSearchException;
import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.*;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.impl.LBHttpSolrClient;
import org.apache.solr.client.solrj.impl.XMLResponseParser;
import org.apache.solr.client.solrj.request.CollectionAdminRequest;
import org.apache.solr.client.solrj.request.CoreAdminRequest;
import org.apache.solr.client.solrj.request.LukeRequest;
import org.apache.solr.client.solrj.request.schema.SchemaRequest;
import org.apache.solr.client.solrj.response.CollectionAdminResponse;
import org.apache.solr.client.solrj.response.CoreAdminResponse;
import org.apache.solr.client.solrj.response.LukeResponse;
import org.apache.solr.client.solrj.response.schema.SchemaResponse;
import org.apache.solr.common.luke.FieldFlag;
import org.apache.solr.common.params.CoreAdminParams;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
        label = "Solr Mode",
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

    private static final Map<String, SolrClient> solrClientByOperation = new ConcurrentHashMap<String, SolrClient>();
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

        if (isStandaloneMode()) {

            return fetchStandloneCores();

        } else if (isCloudMode()) {

            return fetchCloudCores();

        } else {

            return new ArrayList<String>();
        }

    }

    public List<String> fetchCollectionShards() {
        return fetchStandloneCores();
    }

    private List<String> fetchStandloneCores() {

        SolrClient client = getQueryingSolrClient();

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

    private List<String> fetchCloudCores() {

        SolrClient client = getQueryingSolrClient();
        CollectionAdminRequest request = new CollectionAdminRequest.List();
        List<String> collections = null;

        try {
            CollectionAdminResponse response = (CollectionAdminResponse) request.process(client);

            if (response != null) {
                collections = (List) response.getResponse().get("collections");

                if (LOG.isDebugEnabled()) {
                    for (String collection : collections) {
                        LOG.debug("Fetched Collection '{}' of Solr Cloud", collection);
                    }
                }
            }
        } catch (SolrServerException e) {
            LOG.error("Error fetching Cloud Cores.", e);
        } catch (IOException e) {
            LOG.error("Error fetching Cloud Cores.", e);
        }

        return collections;

    }

    /**
     * Returns all Solr fields that have <code>stored</code> set to <code>true</code>.
     *
     * @param  solrCore Solr core name
     * @return a list of stored fields on success, and an empty list otherwise.
     */
    public List<String> getStoredFields(String solrCore) {

        List<String> storedFields = new ArrayList<String>();

        SolrClient solrClient = getQueryingSolrClient();
        final SchemaRequest.Fields fieldsSchemaRequest = new SchemaRequest.Fields();

        try {
            SchemaResponse.FieldsResponse fieldsResponse =
                fieldsSchemaRequest.process(solrClient, solrCore);

            List<Map<String, Object>> fields = fieldsResponse.getFields();

            for (Map map : fields) {

                if (map.containsKey("stored")) {
                    Boolean stored = (Boolean) map.get("stored");
                    if (stored) {
                        String fieldName = (String) map.get("name");
                        storedFields.add(fieldName);
                    }
                }
            }

        } catch (SolrServerException e) {
            LOG.error("Error fetching stored fields of schema '{}'. ", solrCore, e);
        } catch (IOException e) {
            LOG.error("Error fetching stored fields of schema '{}'. ", solrCore, e);
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
        final SolrClient solrClient = getQueryingSolrClient();
        final SchemaRequest.Fields fieldsSchemaRequest = new SchemaRequest.Fields();

        try {
            SchemaResponse.FieldsResponse fieldsResponse =
                fieldsSchemaRequest.process(solrClient, solrCore);

            List<Map<String, Object>> fields = fieldsResponse.getFields();

            for (Map map : fields) {
                if (map.containsKey("indexed")) {
                    Boolean stored = (Boolean) map.get("indexed");
                    if (stored) {
                        String fieldName = (String) map.get("name");
                        availableIndexedFields.add(fieldName);
                    }
                }
            }

        } catch (SolrServerException e) {
            LOG.error("Error fetching indexed fields of schema '{}'.", solrCore, e);
        } catch (IOException e) {
            LOG.error("Error fetching indexed fields of schema '{}'. ", solrCore, e);
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

        SolrClient solrClient = getQueryingSolrClient();
        final LukeRequest request = new LukeRequest();
        request.setShowSchema(true);

        LOG.info("Looking up available indexed fields using Luke");

        try {

            LukeResponse response = request.process(solrClient, solrCore);
            Map<String, LukeResponse.FieldInfo> fields = response.getFieldInfo();
            for (LukeResponse.FieldInfo field : fields.values()) {
                Set<FieldFlag> indexFlags = field.getFlags();

                if (indexFlags != null && indexFlags.contains(FieldFlag.INDEXED)) {

                    availableIndexedFields.add(field.getName());
                }
            }

        } catch (SolrServerException e) {
            LOG.error("Error fetching indexed fields of schema '{}'. ", solrCore, e);
        } catch (IOException e) {
            LOG.error("Error fetching indexed fields of schema '{}'. ", solrCore, e);
        }

        LOG.info("Found indexed fields [{}]", availableIndexedFields);

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
            LOG.info("Returning existing instance of Solr Server {} for operation '{}'", existingSolrServer, solrOperation);
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

                    LOG.info("Returning NEW instance of Solr Server {} for operation '{}'", client, solrOperation);
                    return client;
                }
            }
        }
    }

    private SolrClient getCloudSolrClient() {

        LOG.debug("Creating CloudSolrClient using ZooKeeper: '{}'", solrZKHost);

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
            LOG.error("Error for malformed URL.", e);
        } catch (AEMSolrSearchException e) {
            LOG.error("Solr client initialization failed.", e);
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
                LOG.error("Solr client initialization failed.", e);
            }
        }

        LOG.debug("Creating HttpSolrClient using solrMaster '{}'", solrMaster);
        HttpSolrClient client =  new HttpSolrClient(solrMaster);
        client.setParser(new XMLResponseParser());

        return client;
    }

    public Boolean isCloudMode() {

        return SOLR_MODE_SOLRCLOUD.equals(solrMode);

    }

    public Boolean isStandaloneMode() {

        return SOLR_MODE_STANDALONE.equals(solrMode);

    }


    public void clearSolrClient() {

        for (SolrClient obj : solrClientByOperation.values()) {
            try {
                LOG.info("Releasing SolrClient: '{}'", obj.getClass().getName());
                obj.close();
            } catch (IOException e) {
                LOG.error("Failed to close the Solr Client.", e);
            }
        }

        solrClientByOperation.clear();
    }

}