package com.headwire.aemsolrsearch.geometrixxmedia.adapters;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A factory for creating {@link com.headwire.aemsolrsearch.geometrixxmedia.adapters.GeometrixxMediaContentType}
 * instances.
 *
 * @author <a href="mailto:gg@headwire.com">Gaston Gonzalez</a>
 */
public class GeometrixxMediaContentTypeFactory {

    private static final Logger LOG = LoggerFactory.getLogger(GeometrixxMediaContentTypeFactory.class);
    /** geometrixx-media/components/page/article */
    public static final String SLING_RESOURCE_TYPE_GEOMETRIXX_MEDIA_ARTICLE_PAGE = "geometrixx-media/components/page/article";

    /**
     * Adapts a resource to the appropriate Geometrixx Media content type.
     *
     * @param resource
     * @return A sub class of GeometrixxMediaContentType on success and <code>null</code> otherwise.
     */
    public static GeometrixxMediaContentType getInstance(Resource resource) {

        if (null == resource) {
            LOG.error("Can't adapt a null resource");
            return null;
        }

        final String resourceType = getSlingResourceType(resource);

        GeometrixxMediaContentType contentType = null;
        if (SLING_RESOURCE_TYPE_GEOMETRIXX_MEDIA_ARTICLE_PAGE.equals(resourceType)) {
            contentType = resource.adaptTo(GeometrixxMediaArticlePage.class);
        } else {
            LOG.warn("Unsupported sling:resourceType detected: '{}'", resourceType);
        }

        return contentType;
    }

    public static String getSlingResourceType(Resource resource) {

        if (null == resource) { return null; }

        final ValueMap valueMap = resource.adaptTo(ValueMap.class);
        return valueMap.get("jcr:content/sling:resourceType", String.class);
    }
}
