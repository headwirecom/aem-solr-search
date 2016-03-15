package com.headwire.aemsolrsearch.geometrixx.model;

import com.headwire.aemsolrsearch.geometrixx.model.exceptions.SlingModelsException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.models.annotations.Model;
import org.apache.solr.common.SolrInputDocument;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * Sling model representing a Geometrixx Page.
 *
 */
@Model(adaptables = Resource.class, adapters = {GeometrixxContentType.class, GeometrixxPage.class})
public class GeometrixxPage implements GeometrixxContentType {

    private static final Logger LOG = LoggerFactory.getLogger(GeometrixxPage.class);

    private GeometrixxPageContent pageContent;

    @Inject @Named("jcr:content")
    private Resource jcrResource;

    @PostConstruct
    public void init() throws SlingModelsException {

        pageContent = jcrResource.adaptTo(GeometrixxPageContent.class);

    }

    public GeometrixxPage(Resource resource) throws SlingModelsException {

        if (null == resource) {
            LOG.debug("resource is null");
            throw new SlingModelsException("Resource is null");
        }

        if (ResourceUtil.isNonExistingResource(resource)) {
            LOG.warn("Can't adapt non existent resource: '{}'", resource.getPath());
            throw new SlingModelsException(
                "Can't adapt non existent resource." + resource.getPath());
        }

    }

    @Override
    public String toString() {

        final StringBuilder sb = new StringBuilder("GeometrixxPage{");
        if(null != pageContent){
            sb.append(pageContent.toString());
        }
        sb.append('}');
        return sb.toString();
    }

    @Override
    public JSONObject getJson() {

        return pageContent != null ? pageContent.getJson() : new JSONObject();
    }

    @Override
    public SolrInputDocument getSolrDoc() {

        return pageContent != null ? pageContent.getSolrDoc() : null;
    }
}
