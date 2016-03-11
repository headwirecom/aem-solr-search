package com.headwire.aemsolrsearch.geometrixxmedia.model;

import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.Rendition;
import com.headwire.aemsolrsearch.geometrixxmedia.model.exceptions.SlingModelsException;
import com.headwire.aemsolrsearch.geometrixxmedia.util.HtmlUtils;
import com.headwire.aemsolrsearch.geometrixxmedia.util.MarkdownUtils;
import org.apache.commons.io.IOUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.StringWriter;

/**
 * Represents a Geometrixx Media article body.
 *
 * @author <a href="mailto:gg@headwire.com">Gaston Gonzalez</a>
 */
@Model(adaptables = Resource.class)
public class GeometrixxMediaArticleBody {

    private static final Logger LOG = LoggerFactory.getLogger(GeometrixxMediaArticleBody.class);

    @Default(values = "")
    private String body;

    private Resource resource;

    @PostConstruct public void init() throws SlingModelsException {

        Asset asset = resource.adaptTo(Asset.class);
        if(null == asset){
            return;
        }
        Rendition rendition = (asset.getRendition("plain") != null) ?
            asset.getRendition("plain") :
            asset.getOriginal();

        StringWriter writer = new StringWriter();
        try {
            IOUtils.copy(rendition.getStream(), writer, "UTF8");
            this.body = writer.toString();
        } catch (IOException e) {
            LOG.error("Error reading rendition: {}", rendition.getPath(), e);
        }

    }

    public GeometrixxMediaArticleBody(Resource resource) throws SlingModelsException {

        if (null == resource) {
            LOG.info("Resource is null");
            throw new SlingModelsException("Resource is null");
        }

        if (ResourceUtil.isNonExistingResource(resource)) {
            LOG.warn("Can't adapt non existent resource: '{}'", resource.getPath());
            throw new SlingModelsException(
                "Can't adapt non existent resource." + resource.getPath());
        }

        this.resource = resource;

    }

    public GeometrixxMediaArticleBody(String body){
        this.body = body;
    }

    public String getBody() {
        return body;
    }

    public String getBodyAsHtml() {
        return MarkdownUtils.markdownToHtml(body);
    }

    public String getBodyAsText() {
        // Markdown to HTML to plain text.
        return HtmlUtils.htmlToText(getBodyAsHtml());
    }

    @Override public String toString() {
        final StringBuilder sb = new StringBuilder("MediaArticleBody{");
        sb.append("body='").append(body).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
