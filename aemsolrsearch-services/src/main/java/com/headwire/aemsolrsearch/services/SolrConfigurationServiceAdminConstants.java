/*
 * Author  : Gaston Gonzalez
 * Date    : 7/2/13
 * Version : $Id$
 */
package com.headwire.aemsolrsearch.services;

/**
 * SolrConfigurationServiceAdminConstants is responsible for defining the configurable properties for the
 * {@link SolrConfigurationService}.
 *
 * @author <a href="mailto:gg@headwire.com">Gaston Gonzalez</a>
 */
public class SolrConfigurationServiceAdminConstants {

    /** Solr protocol */
    public static final String PROTOCOL = "solr.protocol";
    /** Solr server name */
    public static final String SERVER_NAME = "solr.server.name";
    /** Solr server port */
    public static final String SERVER_PORT = "solr.server.port";
    /** Solr application context path. Including leading slash.*/
    public static final String CONTEXT_PATH = "solr.context.path";
    /** Proxy enabled state. */
    public static final String PROXY_ENABLED = "solr.proxy.enabled";
    /** Proxy URL. */
    public static final String PROXY_URL = "solr.proxy.url";
    /** Allowed request handlers. */
    public static final String ALLOWED_REQUEST_HANDLERS = "solr.proxy.allowed.handlers";
    /** Solr Mode. */
    public static final String SOLR_MODE = "solr.mode";
    /** Solr ZooKeeper host. */
    public static final String SOLR_ZKHOST = "solr.zkhost";
    /** Solr Master. */
    public static final String SOLR_MASTER = "solr.master";
    /** Solr Slaves. */
    public static final String SOLR_SLAVES = "solr.slaves";
    /** Solr Allow Master Queries. */
    public static final String ALLOWED_SOLR_MASTER_QUERIES = "solr.allow.master.queries";

}
