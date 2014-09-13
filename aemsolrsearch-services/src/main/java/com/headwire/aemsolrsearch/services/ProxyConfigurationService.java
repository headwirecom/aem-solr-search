/*
 * Author  : Gaston Gonzalez
 * Date    : 8/13/13
 * Version : $Id$
 */
package com.headwire.aemsolrsearch.services;

import org.apache.felix.scr.annotations.*;
import org.apache.felix.scr.annotations.Properties;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Component(
        name = "com.headwire.aemsolrsearch.services.ProxyConfigurationService",
        label = "AEM Solr Search - Solr Proxy Service",
        description = "A service for configuring the search proxy",
        immediate = true,
        metatype = true)
@Service(ProxyConfigurationService.class)
@Properties({
        @Property(
                name = Constants.SERVICE_VENDOR,
                value = "headwire.com, Inc."),
        @Property(
                name = Constants.SERVICE_DESCRIPTION,
                value = "CQ Search proxy configuration service"),
        @Property(
                name = ProxyConfigurationServiceAdminConstants.HTTP_CONN_TIMEOUT,
                intValue = 10000,
                label = "Connection Timeout",
                description = "Connection timeout in ms"),
        @Property(
                name = ProxyConfigurationServiceAdminConstants.HTTP_SO_TIMEOUT,
                intValue = 30000,
                label = "Socket Timeout",
                description = "Socket timeout in ms")
})
/**
 * ProxyConfigurationService provides services for setting and getting proxy configuration information.
 */
public class ProxyConfigurationService {

    private static final Logger LOG = LoggerFactory.getLogger(ProxyConfigurationService.class);
    private Integer httpConnTimeout;
    private Integer httpSoTimeout;

    public static final Integer DEFAULT_HTTP_CONN_TIMEOUT = 10000;
    public static final Integer DEFAULT_HTTP_SO_TIMEOUT= 30000;

    public Integer getHttpConnTimeout() {
        return httpConnTimeout;
    }

    public Integer getHttpSoTimeout() {
        return httpSoTimeout;
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
        LOG.info("Resetting CQ Search proxy configuration service using configuration: " + config);

        httpConnTimeout = config.containsKey(ProxyConfigurationServiceAdminConstants.HTTP_CONN_TIMEOUT) ?
                (Integer)config.get(ProxyConfigurationServiceAdminConstants.HTTP_CONN_TIMEOUT) : DEFAULT_HTTP_CONN_TIMEOUT;

        httpSoTimeout = config.containsKey(ProxyConfigurationServiceAdminConstants.HTTP_SO_TIMEOUT) ?
                (Integer)config.get(ProxyConfigurationServiceAdminConstants.HTTP_SO_TIMEOUT) : DEFAULT_HTTP_SO_TIMEOUT;
    }
}
