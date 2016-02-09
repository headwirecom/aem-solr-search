/*
 * Author  : Gaston Gonzalez
 * Date    : 8/26/13
 * Version : $Id$
 */
package com.headwire.aemsolrsearch.geometrixxmedia.solr.index;

import com.headwire.aemsolrsearch.services.SolrConfigurationService;
import org.apache.felix.scr.annotations.*;
import org.apache.solr.client.solrj.SolrClient;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Component(
        name = "com.headwire.aemsolrsearch.index.services.GeometrixxMediaSolrIndexService",
        label = "AEM Solr Search - GeometrixxMedia Solr Index Service",
        description = "A service for indexing Solr",
        immediate = true,
        metatype = true)
@Service(GeometrixxMediaSolrIndexService.class)
@Properties({
        @Property(
                name = Constants.SERVICE_VENDOR,
                value = "headwire.com, Inc."),
        @Property(
                name = Constants.SERVICE_DESCRIPTION,
                value = "An index service for the Geometrixx Media Site")
})
/**
 * GeometrixxMediaSolrIndexService is responsible for performing index operations against Solr.
 *
 * @author <a href="mailto:gg@headwire.com">Gaston Gonzalez</a>
 */
public class GeometrixxMediaSolrIndexService extends AbstractSolrIndexService {

    private static final Logger LOG = LoggerFactory.getLogger(GeometrixxMediaSolrIndexService.class);
    // TODO: Make this an OSGi property
    private static final String GEOMETRIXX_MEDIA_CORE = "collection1";

    @Reference
    SolrConfigurationService solrConfigService;

    @Activate
    protected void activate(final Map<String, String> config) {
        resetService(config);
    }

    @Modified
    protected void modified(final Map<String, String> config) {
        resetService(config);
    }

    @Override
    protected String getSolrServerURI() {
        assertSolrConfigService();
        return formatSolrEndPointAndCore(solrConfigService.getSolrEndPoint(), GEOMETRIXX_MEDIA_CORE);
    }

    @Override
    protected String getSolrServerURI(String solrCore) {
        assertSolrConfigService();
        return formatSolrEndPointAndCore(solrConfigService.getSolrEndPoint(), solrCore);
    }

    @Override
    protected SolrClient getSolrIndexClient() {
        return solrConfigService.getIndexingSolrClient();
    }

    @Override
    protected SolrClient getSolrQueryClient() {
        return solrConfigService.getQueryingSolrClient();
    }

    private void resetService(final Map<String, String> config) {
        LOG.info("Resetting Solr index service using configuration: " + config);
    }
    
    private void assertSolrConfigService() throws IllegalStateException {        
        if (null == solrConfigService) {
            LOG.error("Can't get SolrConfigurationService. Check that all OSGi bundles are active");
            throw new IllegalStateException("No solr configuration service.");
        }
    }

    @Override
    public String getCoreName() {
        return GEOMETRIXX_MEDIA_CORE;
    }
}
