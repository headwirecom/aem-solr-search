package com.headwire.aemsolrsearch.geometrixxmedia.adapters;

import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.Rendition;
import com.headwire.aemsolrsearch.geometrixxmedia.util.HtmlUtils;
import com.headwire.aemsolrsearch.geometrixxmedia.util.MarkdownUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;

/**
 * Represents a Geometrixx Media article body.
 *
 * @author <a href="mailto:gg@headwire.com">Gaston Gonzalez</a>
 */
public class GeometrixxMediaArticleBody {

    private static final Logger LOG = LoggerFactory.getLogger(GeometrixxMediaArticleBody.class);
    private String body;

    public static final GeometrixxMediaArticleBody NULL = new GeometrixxMediaArticleBody("");

    public GeometrixxMediaArticleBody(String body) {
        this.body = body;
    }

    public static GeometrixxMediaArticleBody adaptFromResource(Resource resource) {

        if (null == resource) {
            return null;
        }

        if (ResourceUtil.isNonExistingResource(resource)) {
            LOG.warn("Can't adapt non existent resource: '{}'", resource.getPath());
            return null;
        }

        Asset asset = resource.adaptTo(Asset.class);
        Rendition rendition = (asset.getRendition("plain") != null)
                ? asset.getRendition("plain")
                : asset.getOriginal();

        StringWriter writer = new StringWriter();
        try {
            IOUtils.copy(rendition.getStream(), writer, "UTF8");
            return new GeometrixxMediaArticleBody(writer.toString());
        } catch (IOException e) {
            LOG.error("Error reading rendition: {}", rendition.getPath(), e);
            return null;
        }
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

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MediaArticleBody{");
        sb.append("body='").append(body).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
