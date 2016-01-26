package com.headwire.aemsolrsearch.geometrixx.adapters;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.adapter.AdapterFactory;
import org.apache.sling.api.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.headwire.aemsolrsearch.geometrixx.adapters.pages.GeometrixxContentPage;
import com.headwire.aemsolrsearch.geometrixx.adapters.pages.GeometrixxHomePage;
import com.headwire.aemsolrsearch.geometrixx.adapters.pages.GeometrixxWidePage;

/**
 * An adapter factory for the Geometrixx site.
 *
 * @author <a href="mailto:gg@headwire.com">Gaston Gonzalez</a>
 */
@Component(metatype=false)
@Service(AdapterFactory.class)
public class GeometrixxFactory implements  AdapterFactory {

    private static final Logger LOG = LoggerFactory.getLogger(GeometrixxFactory.class);
    private static final Class<GeometrixxContentPage> GEOMETRIXX_CONTENT_PAGE_CLASS = GeometrixxContentPage.class;
    private static final Class<GeometrixxHomePage> GEOMETRIXX_HOME_PAGE_CLASS = GeometrixxHomePage.class;
    private static final Class<GeometrixxWidePage> GEOMETRIXX_WIDE_PAGE_CLASS = GeometrixxWidePage.class;

    @Property(name = "adapters")
    public static final String[] ADAPTER_CLASSES = {
            GEOMETRIXX_CONTENT_PAGE_CLASS.getName(),
            GEOMETRIXX_HOME_PAGE_CLASS.getName(),
            GEOMETRIXX_WIDE_PAGE_CLASS.getName()
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
            if (adapterType == GEOMETRIXX_CONTENT_PAGE_CLASS) {
                return (AdapterType) GeometrixxContentPage.adaptFromResource(resource);
            } else if (adapterType == GEOMETRIXX_HOME_PAGE_CLASS) {
                return (AdapterType) GeometrixxHomePage.adaptFromResource(resource);
            } else if (adapterType == GEOMETRIXX_WIDE_PAGE_CLASS) {
                return (AdapterType) GeometrixxWidePage.adaptFromResource(resource);
            }

        } catch (Exception e) {
            LOG.error("Unable to adapt to {} from resource {}", adapterType, resource.getPath());
        }

        return null;
    }
}