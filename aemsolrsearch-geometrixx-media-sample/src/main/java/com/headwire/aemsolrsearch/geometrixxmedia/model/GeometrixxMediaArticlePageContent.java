package com.headwire.aemsolrsearch.geometrixxmedia.model;

import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagManager;
import com.day.cq.wcm.api.NameConstants;
import com.headwire.aemsolrsearch.geometrixxmedia.model.exceptions.SlingModelsException;
import com.headwire.aemsolrsearch.geometrixxmedia.solr.SolrTimestamp;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Optional;
import org.apache.solr.common.SolrInputDocument;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Arrays;
import java.util.Date;

import static com.headwire.aemsolrsearch.geometrixxmedia.solr.GeometrixxMediaSchema.*;

@Model(adaptables = Resource.class)
public class GeometrixxMediaArticlePageContent {

    private static final Logger LOG =
        LoggerFactory.getLogger(GeometrixxMediaArticlePageContent.class);

    private GeometrixxMediaAuthorSummary author;

    private GeometrixxMediaArticleBody body;

    private String id;

    private String url;

    @Inject @Named("jcr:description") @Default(values = "")
    private String description;

    @Inject @Named("sling:resourceType")
    private String slingResourceType;

    @Inject @Named("jcr:title") @Default(values = "")
    private String title;

    @Inject @Named("author-summary/articleAuthor") @Default(values = "")
    private String authorRef;

    @Inject @Named("article-content-par/article/fileReference") @Optional
    private GeometrixxMediaArticleBody articleBody;

    @Inject @Named(NameConstants.PN_PAGE_LAST_MOD) @Optional
    private Date lastUpdate;

    @Inject @Named("author-summary/publishedDate") @Optional
    private Date publishDate;

    private Tag[] tags;

    private Resource resource;

    @PostConstruct
    public void init() throws SlingModelsException {

        final Resource authorResource =
            resource.getResourceResolver().resolve(authorRef + "/profile");
        final GeometrixxMediaAuthorSummary authorSummary =
            authorResource.adaptTo(GeometrixxMediaAuthorSummary.class);

        final TagManager tagManager = resource.getResourceResolver().adaptTo(TagManager.class);

        id = resource.getParent().getPath();
        url = id + ".html";
        author = authorSummary != null ? authorSummary : new GeometrixxMediaAuthorSummary();
        body = articleBody != null ? articleBody : new GeometrixxMediaArticleBody("");
        tags = tagManager.getTags(resource);

    }

    public GeometrixxMediaArticlePageContent(Resource resource) {
        this.resource = resource;
    }

    public GeometrixxMediaAuthorSummary getAuthor() {
        return author;
    }

    public GeometrixxMediaArticleBody getBody() {
        return body;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getSlingResourceType() {
        return slingResourceType;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
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

    public String toString() {

        final StringBuilder sb = new StringBuilder("");
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
        return sb.toString();
    }

    protected JSONObject getJson() {
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

    protected SolrInputDocument getSolrDoc() {

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
