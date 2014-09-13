package com.headwire.aemsolrsearch.geometrixxmedia.adapters;

import static com.headwire.aemsolrsearch.geometrixxmedia.solr.GeometrixxMediaSchema.*;

import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagManager;
import com.day.cq.wcm.api.NameConstants;
import com.headwire.aemsolrsearch.geometrixxmedia.solr.SolrTimestamp;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.solr.common.SolrInputDocument;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Date;

/**
 * Data model representing a Geometrixx Media Article page.
 *
 * @author <a href="mailto:gg@headwire.com">Gaston Gonzalez</a>
 */
public class GeometrixxMediaArticlePage extends GeometrixxMediaContentType {

    private static final Logger LOG = LoggerFactory.getLogger(GeometrixxMediaArticlePage.class);

    private GeometrixxMediaAuthorSummary author;
    private GeometrixxMediaArticleBody body;
    private String description;
    private String id;
    private String title;
    private String url;
    private String slingResourceType;
    private Date lastUpdate;
    private Date publishDate;
    private Tag[] tags;

    public GeometrixxMediaArticlePage(String id) {
        this.id = id;
    }

    public static GeometrixxMediaArticlePage adaptFromResource(Resource resource) {

        final String id = resource.getPath();
        final Resource jcrResource = resource.getChild("jcr:content");
        final ValueMap valueMap = jcrResource.adaptTo(ValueMap.class);
        final String title = valueMap.get("jcr:title", "");
        final String description = valueMap.get("jcr:description", "");
        final String slingResourceType = valueMap.get("sling:resourceType", "");

        final Date lastUpdate = valueMap.get(NameConstants.PN_PAGE_LAST_MOD, Date.class);

        final String articleRef = valueMap.get("article-content-par/article/fileReference", "");
        final Resource articleAsset = resource.getResourceResolver().resolve(articleRef);
        final GeometrixxMediaArticleBody body = articleAsset.adaptTo(GeometrixxMediaArticleBody.class);

        final String authorRef = valueMap.get("author-summary/articleAuthor", "");
        final Resource authorResource = resource.getResourceResolver().resolve(authorRef + "/profile");
        final GeometrixxMediaAuthorSummary authorSummary = authorResource.adaptTo(GeometrixxMediaAuthorSummary.class);

        final Date publishDate = valueMap.get("author-summary/publishedDate", Date.class);

        final TagManager tagManager = resource.getResourceResolver().adaptTo(TagManager.class);
        final Tag[] tags = tagManager.getTags(jcrResource);

        GeometrixxMediaArticlePage article = new GeometrixxMediaArticlePage(id)
                .withTitle(title)
                .withDescription(description)
                .withBody(body != null ? body : GeometrixxMediaArticleBody.NULL)
                .withAuthor(authorSummary != null ? authorSummary : GeometrixxMediaAuthorSummary.NULL)
                .withUrl(resource.getPath() + ".html")
                .withLastUpdate(lastUpdate)
                .withPublishDate(publishDate)
                .withTags(tags)
                .withSlingResourceType(slingResourceType);

        if (publishDate != null) {
            article.withPublishDate(publishDate);
        }

        return article;
    }

    public GeometrixxMediaArticlePage withAuthor(GeometrixxMediaAuthorSummary author) {
        this.author = author;
        return this;
    }

    public GeometrixxMediaArticlePage withBody(GeometrixxMediaArticleBody body) {
        this.body = body;
        return this;
    }

    public GeometrixxMediaArticlePage withDescription(String description) {
        this.description = description;
        return this;
    }

    public GeometrixxMediaArticlePage withLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
        return this;
    }

    public GeometrixxMediaArticlePage withPublishDate(Date publishDate) {
        this.publishDate = publishDate;
        return this;
    }

    public GeometrixxMediaArticlePage withSlingResourceType(String slingResourceType) {
        this.slingResourceType = slingResourceType;
        return this;
    }

    public GeometrixxMediaArticlePage withTags(Tag[] tags) {
        this.tags = tags;
        return this;
    }

    public GeometrixxMediaArticlePage withTitle(String title) {
        this.title = title;
        return this;
    }

    public GeometrixxMediaArticlePage withUrl(String url) {
        this.url = url;
        return this;
    }

    public GeometrixxMediaAuthorSummary getAuthor() {
        return author;
    }

    public GeometrixxMediaArticleBody getBody() {
        return body;
    }

    public String getDescription() {
        return description;
    }

    public String getId() {
        return id;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public Date getPublishDate() {
        return publishDate;
    }

    public Tag[] getTags() {
        return tags;
    }

    public String getTitle() {
        return title;
    }

    public String getSlingResourceType() {
        return slingResourceType;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("GeometrixxMediaArticlePage{");
        sb.append("author=").append(author);
        sb.append(", body=").append(body);
        sb.append(", description='").append(description).append('\'');
        sb.append(", id='").append(id).append('\'');
        sb.append(", title='").append(title).append('\'');
        sb.append(", url='").append(url).append('\'');
        sb.append(", slingResourceType='").append(slingResourceType).append('\'');
        sb.append(", lastUpdate=").append(lastUpdate);
        sb.append(", publishDate=").append(publishDate);
        sb.append(", tags=").append(Arrays.toString(tags));
        sb.append('}');
        return sb.toString();
    }

    @Override
    public JSONObject getJson() {
        JSONObject json = new JSONObject();
        json.put(ID, getId());
        json.put(TITLE, getTitle());
        json.put(DESCRIPTION, getDescription());
        json.put(BODY, getBody().getBodyAsText());
        json.put(AUTHOR, getAuthor().displayName());
        json.put(LAST_MODIFIED, SolrTimestamp.convertToUtcAndUseNowIfNull(getLastUpdate()));
        json.put(PUBLISH_DATE, SolrTimestamp.convertToUtcAndUseNowIfNull(getPublishDate()));
        json.put(SLING_RESOUCE_TYPE, getSlingResourceType());
        json.put(URL, getUrl());

        JSONArray tags = new JSONArray();
        for (Tag tag : getTags()) {
            tags.add(tag.getTitle());
        }
        json.put(TAGS, tags);

        return json;
    }

    @Override
    public SolrInputDocument getSolrDoc() {

        SolrInputDocument doc = new SolrInputDocument();

        doc.addField(ID, getId());
        doc.addField(TITLE, getTitle());
        doc.addField(DESCRIPTION, getDescription());
        doc.addField(BODY, getBody().getBodyAsText());
        doc.addField(AUTHOR, getAuthor().displayName());
        doc.addField(LAST_MODIFIED, SolrTimestamp.convertToUtcAndUseNowIfNull(getLastUpdate()));
        doc.addField(PUBLISH_DATE, SolrTimestamp.convertToUtcAndUseNowIfNull(getPublishDate()));
        doc.addField(SLING_RESOUCE_TYPE, getSlingResourceType());
        doc.addField(URL, getUrl());

        for (Tag tag : getTags()) {
            doc.addField(TAGS, tag.getTitle());
        }

        return doc;
    }
}
