package com.headwire.aemsolrsearch.geometrixxmedia.solr.index;

import com.day.cq.replication.ReplicationAction;
import com.day.cq.wcm.api.PageEvent;
import com.day.cq.wcm.api.PageModification;
import com.headwire.aemsolrsearch.geometrixxmedia.adapters.GeometrixxMediaArticlePage;
import com.headwire.aemsolrsearch.geometrixxmedia.adapters.GeometrixxMediaContentType;
import com.headwire.aemsolrsearch.geometrixxmedia.adapters.GeometrixxMediaContentTypeFactory;
import org.apache.felix.scr.annotations.*;
import org.apache.sling.api.resource.*;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;

@Component (
        name = "com.headwire.aemsolrsearch.index.services.DefaultSolrIndexListenerService",
        label = "AEM Solr Search - Geometrixx Media Solr Index Listener",
        description = "Submits page modifications to Solr for indexing",
        immediate = true,
        metatype = true)
@Service
@Properties({
    @Property(
        name = DefaultSolrIndexListenerServiceAdminConstants.ENABLED,
        boolValue = true,
        label = "Enabled",
        description = "Enable the default Solr Index Listener"
    ),
    @Property(
        name = DefaultSolrIndexListenerServiceAdminConstants.OBSERVED_PATHS,
        value={"/content/geometrixx-media"},
        label = "Observed Repository Paths",
        description = "The paths on which this listener is active"
    ),
    @Property(
        name="event.topics",
        value={ReplicationAction.EVENT_TOPIC, PageEvent.EVENT_TOPIC})
})
/**
 * DefaultSolrIndexListenerService is responsible for listening for page modifications and submitting the
 * appropriate index operations to Solr.
 *
 * @author <a href="mailto:gg@headwire.com">Gaston Gonzalez</a>
 */
public class DefaultSolrIndexListenerService implements EventHandler {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultSolrIndexListenerService.class);

    private Boolean serviceEnabled;
    private String[] observedPaths;

    @Reference
    private GeometrixxMediaSolrIndexService indexService;

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    private ResourceResolver resourceResolver;

    @Override
    public void handleEvent(Event event) {

        if (pageIsNotIndexable(event)) { return; }

        PageEvent pageEvent = PageEvent.fromEvent(event);

        if (null == pageEvent) { return; }

        Iterator<PageModification> modifications = pageEvent.getModifications();
        while (modifications.hasNext()) {

            PageModification modification = modifications.next();
            String modificationPath = modification.getPath();

            if (pageIsNotInObservedPath(modificationPath)) { return; }

            PageModification.ModificationType type = modification.getType();
            if (type == PageModification.ModificationType.DELETED) {

                LOG.info("Page deleted {}", modificationPath);
                indexService.deleteAndCommit(modification.getPath());

                // Exit loop early on first page modification for deletes.
                return;

            } else if (type == PageModification.ModificationType.CREATED) {

                LOG.info("Page created {}", modificationPath);
                addOrUpdatePage(modification);

            } else if (type == PageModification.ModificationType.MODIFIED) {

                LOG.info("Page modified {}", modificationPath);
                addOrUpdatePage(modification);

            } else {
                LOG.info("Unsupported page modification detected: '{}'", type);
            }
        }
    }

    /**
     * Determines whether the page modification is a candidate for indexing.
     *
     * @param event  page event
     * @return  <code>true</code> if the page modification should be indexed, and <code>false</code> if
     *          the page should be excluded from indexing.
     *
     */
    protected boolean pageIsIndexable(Event event) {

        if (!serviceEnabled) {
            LOG.debug("DefaultSolrIndexListenerService not enabled. Ignoring indexing event");
            return false;
        }

        return true;
    }

    protected boolean pageIsNotIndexable(Event event) {
        return !pageIsIndexable(event);
    }


    /**
     * Determines whether the page modification is in the allowed list of observed paths.
     *
     * @param pagePath Path of the modified page.
     * @return <code>true</code> if the page modification is in the observed path, and <code>false</code>
     *         otherwise.
     */
    protected boolean pageIsInObservedPath(String pagePath) {

        if (null == pagePath || null == observedPaths) { return false; }

        for (String observedPath: observedPaths) {
            if (pagePath.startsWith(observedPath)) {
                LOG.info("Page '{}' is in observed path '{}'", pagePath, observedPath);
                return true;
            }
        }

        return false;
    }

    protected boolean pageIsNotInObservedPath(String pagePath) {
        return !pageIsInObservedPath(pagePath);
    }

    protected void addOrUpdatePage(PageModification modification) {

        final String modificationPath = modification.getPath();

        if (null == resourceResolver) {
            LOG.warn("Can't perform indexing operation for '{}'", modificationPath);
            return;
        }

        final Resource resource = resourceResolver.getResource(modificationPath);
        if (ResourceUtil.isNonExistingResource(resource)) {
            LOG.warn("Can't perform indexing operation for '{}'. Resource does not exist.", modificationPath);
            return;
        }

        GeometrixxMediaContentType contentPage = GeometrixxMediaContentTypeFactory.getInstance(resource);

        indexService.addAndCommit(contentPage.getSolrDoc());
    }

    @Activate
    protected void activate(final Map<String, Object> config) {
        resetService(config);
    }

    @Modified
    protected void modified(final Map<String, Object> config) {
        resetService(config);
    }

    @Deactivate
    protected void deactivate(final Map<String, Object> config) {

        if (resourceResolver != null && resourceResolver.isLive())  {
            resourceResolver.close();
        }
    }

    private synchronized void resetService(final Map<String, Object> config) {
        LOG.info("Resetting default Solr index listener service using configuration: " + config);

        serviceEnabled = config.containsKey(DefaultSolrIndexListenerServiceAdminConstants.ENABLED) ?
                (Boolean) config.get(DefaultSolrIndexListenerServiceAdminConstants.ENABLED) : false;

        if (resourceResolverFactory != null) {
            try {
                resourceResolver = resourceResolverFactory.getAdministrativeResourceResolver( null );
            } catch (LoginException e) {
                LOG.error("Can't get resource resolver. Solr indexing will not be available");
            }
        }

        if (config.containsKey(DefaultSolrIndexListenerServiceAdminConstants.OBSERVED_PATHS)) {

            Object observedPathsValue = config.get(DefaultSolrIndexListenerServiceAdminConstants.OBSERVED_PATHS);
            if (observedPathsValue instanceof String) {
                LOG.info("Observing single path");
                observedPaths = new String[] {(String)observedPathsValue};
            } else if (observedPathsValue instanceof String[]) {
                LOG.info("Observing multiple paths");
                observedPaths = (String[])observedPathsValue;
            } else {
                LOG.warn("Unexpected value assigned to observed paths");
                observedPaths = new String[]{};
            }

        } else {
            LOG.info("No observed paths defined for listener");
            observedPaths = new String[]{};
        }
    }
}
