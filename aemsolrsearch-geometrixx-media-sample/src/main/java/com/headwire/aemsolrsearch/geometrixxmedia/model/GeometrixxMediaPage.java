package com.headwire.aemsolrsearch.geometrixxmedia.model;

import com.headwire.aemsolrsearch.geometrixxmedia.model.exceptions.SlingModelsException;
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
 * Sling model representing a Geometrixx Media Page.
 *
 */
@Model(adaptables = Resource.class, adapters = {GeometrixxMediaContentType.class, GeometrixxMediaPage.class})
public class GeometrixxMediaPage implements GeometrixxMediaContentType{

    private static final Logger LOG = LoggerFactory.getLogger(GeometrixxMediaPage.class);

    private GeometrixxMediaPageContent pageContent;

    @Inject @Named("jcr:content")
    private Resource jcrResource;

    @PostConstruct
    public void init() throws SlingModelsException {

        pageContent = jcrResource.adaptTo(GeometrixxMediaPageContent.class);

    }

    public GeometrixxMediaPage(Resource resource) throws SlingModelsException {

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

        final StringBuilder sb = new StringBuilder("GeometrixxMediaPage{");
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
