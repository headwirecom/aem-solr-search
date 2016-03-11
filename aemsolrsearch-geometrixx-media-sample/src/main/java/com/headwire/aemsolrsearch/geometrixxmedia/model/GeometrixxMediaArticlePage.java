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
 * Data model representing a Geometrixx Media Article page.
 *
 * @author <a href="mailto:gg@headwire.com">Gaston Gonzalez</a>
 */
@Model(adaptables = Resource.class)
public class GeometrixxMediaArticlePage extends GeometrixxMediaContentType {

    private static final Logger LOG = LoggerFactory.getLogger(GeometrixxMediaArticlePage.class);

    private GeometrixxMediaArticlePageContent articlePageContent;

    @Inject @Named("jcr:content")
    private Resource jcrResource;

    @PostConstruct public void init() throws SlingModelsException {

        articlePageContent = jcrResource.adaptTo(GeometrixxMediaArticlePageContent.class);

    }

    public GeometrixxMediaArticlePage(Resource resource) throws SlingModelsException {

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

    @Override public String toString() {

        final StringBuilder sb = new StringBuilder("GeometrixxMediaArticlePage{");
        if(null != articlePageContent){
            sb.append(articlePageContent.toString());
        }
        sb.append('}');
        return sb.toString();
    }

    @Override public JSONObject getJson() {

        return articlePageContent != null ? articlePageContent.getJson() : null;
    }

    @Override public SolrInputDocument getSolrDoc() {

        return articlePageContent != null ? articlePageContent.getSolrDoc() : null;
    }
}
