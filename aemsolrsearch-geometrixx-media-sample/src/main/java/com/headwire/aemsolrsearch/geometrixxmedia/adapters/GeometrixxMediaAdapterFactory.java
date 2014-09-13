package com.headwire.aemsolrsearch.geometrixxmedia.adapters;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.adapter.AdapterFactory;
import org.apache.sling.api.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An adapter factory for the Geometrixx Media site.
 *
 * @author <a href="mailto:gg@headwire.com">Gaston Gonzalez</a>
 */
@Component(metatype=false)
@Service(AdapterFactory.class)
public class GeometrixxMediaAdapterFactory implements  AdapterFactory {

    private static final Logger LOG = LoggerFactory.getLogger(GeometrixxMediaAdapterFactory.class);
    private static final Class<GeometrixxMediaArticlePage> ARTICLE_CLASS = GeometrixxMediaArticlePage.class;
    private static final Class<GeometrixxMediaArticleBody> MEDIA_ARTICLE_CLASS = GeometrixxMediaArticleBody.class;
    private static final Class<GeometrixxMediaAuthorSummary> AUTHOR_SUMMARY_CLASS = GeometrixxMediaAuthorSummary.class;

    @Property(name = "adapters")
    public static final String[] ADAPTER_CLASSES = {
            ARTICLE_CLASS.getName(),
            MEDIA_ARTICLE_CLASS.getName(),
            AUTHOR_SUMMARY_CLASS.getName()
    };

    @Property(name = "adaptables")
    public static final String[] ADAPTABLE_CLASSES = {Resource.class.getName()};

    @Override
    public <AdapterType> AdapterType getAdapter(Object adaptable, Class<AdapterType> adapterType) {

        if (adaptable instanceof Resource) {
            return getAdapter((Resource) adaptable, adapterType);
        }
        return null;
    }

    protected <AdapterType> AdapterType getAdapter(Resource resource, Class<AdapterType> adapterType) {

        if (null == resource) {
            LOG.error("Unable to adapt null resource {}");
            return null;
        }

        try {
            if (adapterType == ARTICLE_CLASS) {
                return (AdapterType) GeometrixxMediaArticlePage.adaptFromResource(resource);
            } else if (adapterType == MEDIA_ARTICLE_CLASS) {
                return (AdapterType) GeometrixxMediaArticleBody.adaptFromResource(resource);
            } else if (adapterType == AUTHOR_SUMMARY_CLASS) {
                return (AdapterType) GeometrixxMediaAuthorSummary.adaptFromResource(resource);
            }

        } catch (Exception e) {
            LOG.error("Unable to adapt to {} from resource {}", adapterType, resource.getPath());
        }

        return null;
    }
}