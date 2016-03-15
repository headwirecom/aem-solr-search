package com.headwire.aemsolrsearch.geometrixx.model;

import com.day.cq.wcm.api.NameConstants;
import com.headwire.aemsolrsearch.geometrixx.config.ComponentDataConfig;
import com.headwire.aemsolrsearch.geometrixx.model.exceptions.SlingModelsException;
import com.headwire.aemsolrsearch.geometrixx.solr.SolrTimestamp;
import com.headwire.aemsolrsearch.geometrixx.util.SolrIndexingUtil;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Optional;
import org.apache.solr.common.SolrInputDocument;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.Map;

import static com.headwire.aemsolrsearch.geometrixx.solr.GeometrixxSchema.*;

@Model(adaptables = Resource.class)
public class GeometrixxPageContent {

    private static final Logger LOG =
        LoggerFactory.getLogger(GeometrixxPageContent.class);

    private String id;

    private String body;

    private String url;

    @Inject @Named("jcr:description") @Default
    private String description;

    @Inject @Named("sling:resourceType")
    private String slingResourceType;

    @Inject @Named("jcr:title") @Default
    private String title;

    @Inject @Named(NameConstants.PN_PAGE_LAST_MOD) @Optional
    private Date lastUpdate;

    private Resource resource;

    @PostConstruct
    public void init() throws SlingModelsException {

        Map<String, ComponentDataConfig>
            componentDataConfigMap = SolrIndexingUtil.getComponentDataConfigs();
        Map<String, String> mainParsysProps = SolrIndexingUtil.extractDataFromParsys(resource, "par", componentDataConfigMap);

        id = resource.getParent().getPath();
        url = getId() + ".html";
        body = mainParsysProps.containsKey("bodyText") ? mainParsysProps.get("bodyText") : "";

    }

    public GeometrixxPageContent(Resource resource) {
        this.resource = resource;
    }

    public String toString() {

        final StringBuilder sb = new StringBuilder("");
        sb.append(", body=").append(body);
        sb.append(", description='").append(description).append('\'');
        sb.append(", id='").append(id).append('\'');
        sb.append(", title='").append(title).append('\'');
        sb.append(", url='").append(url).append('\'');
        sb.append(", slingResourceType='").append(slingResourceType).append('\'');
        sb.append(", lastUpdate=").append(lastUpdate);
        return sb.toString();
    }

    public String getId() {
        return id;
    }

    public String getBody() {
        return body;
    }

    public String getUrl() {
        return url;
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

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public JSONObject getJson() {
        JSONObject json = new JSONObject();
        json.put(ID, getId());
        json.put(TITLE, getTitle());
        json.put(DESCRIPTION, getDescription());
        json.put(LAST_MODIFIED, SolrTimestamp.convertToUtcAndUseNowIfNull(getLastUpdate()));
        json.put(SLING_RESOUCE_TYPE, getSlingResourceType());
        json.put(URL, getUrl());
        json.put(BODY, getBody());

        return json;
    }

    public SolrInputDocument getSolrDoc() {

        SolrInputDocument doc = new SolrInputDocument();

        doc.addField(ID, getId());
        doc.addField(TITLE, getTitle());
        doc.addField(DESCRIPTION, getDescription());
        doc.addField(LAST_MODIFIED, SolrTimestamp.convertToUtcAndUseNowIfNull(getLastUpdate()));
        doc.addField(SLING_RESOUCE_TYPE, getSlingResourceType());
        doc.addField(URL, getUrl());
        doc.addField(BODY, getBody());

        return doc;
    }
}
