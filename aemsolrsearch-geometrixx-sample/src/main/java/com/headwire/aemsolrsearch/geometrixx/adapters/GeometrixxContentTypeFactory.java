package com.headwire.aemsolrsearch.geometrixx.adapters;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.headwire.aemsolrsearch.geometrixx.adapters.pages.GeometrixxContentPage;
import com.headwire.aemsolrsearch.geometrixx.adapters.pages.GeometrixxHomePage;
import com.headwire.aemsolrsearch.geometrixx.adapters.pages.GeometrixxWidePage;

/**
 * A factory for creating {@link com.headwire.aemsolrsearch.geometrixx.adapters.GeometrixxContentType}
 * instances.
 *
 * @author <a href="mailto:gg@headwire.com">Gaston Gonzalez</a>
 */
public class GeometrixxContentTypeFactory {

    private static final Logger LOG = LoggerFactory.getLogger(GeometrixxContentTypeFactory.class);
    /** geometrixx/components/page */
    public static final String SLING_RESOURCE_TYPE_GEOMETRIXX_CONTENT_PAGE = "geometrixx/components/contentpage";
    public static final String SLING_RESOURCE_TYPE_GEOMETRIXX_HOME_PAGE = "geometrixx/components/homepage";
    public static final String SLING_RESOURCE_TYPE_GEOMETRIXX_WIDE_PAGE = "geometrixx/components/widepage";

    /**
     * Adapts a resource to the appropriate Geometrixx content type.
     *
     * @param resource
     * @return A sub class of GeometrixxContentType on success and <code>null</code> otherwise.
     */
    public static GeometrixxContentType getInstance(Resource resource) {

        if (null == resource) {
            LOG.error("Can't adapt a null resource");
            return null;
        }

        final String resourceType = getSlingResourceType(resource);

        GeometrixxContentType contentType = null;
        if (SLING_RESOURCE_TYPE_GEOMETRIXX_CONTENT_PAGE.equals(resourceType)) {
            contentType = resource.adaptTo(GeometrixxContentPage.class);
        } else if (SLING_RESOURCE_TYPE_GEOMETRIXX_HOME_PAGE.equals(resourceType)) {
            contentType = resource.adaptTo(GeometrixxHomePage.class);
        } else if (SLING_RESOURCE_TYPE_GEOMETRIXX_WIDE_PAGE.equals(resourceType)) {
            contentType = resource.adaptTo(GeometrixxWidePage.class);
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
