/*
 * Author  : Gaston Gonzalez
 * Date    : 6/30/13
 * Version : $Id$
 */
package com.headwire.aemsolrsearch.services.servlets;

import com.headwire.aemsolrsearch.services.SolrConfigurationService;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.io.JSONWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;

/**
 * SolrCoreServlet is responsible for...
 *
 * @author <a href="mailto:gg@headwire.com">Gaston Gonzalez</a>
 */
@Service
@Component(
        name = "com.headwire.aemsolrsearch.services.servlets.SolrCoreServlet",
        metatype = true,
        immediate = true,
        label = "Solr Core Servlet",
        description = "Solr Core Servlet"
)
@Properties({
        @Property(
                name = "sling.servlet.paths",
                value = { "/apps/solr/core" },
                propertyPrivate = true
        ),
        @Property(
                name = "sling.servlet.methods",
                value = { "GET", "POST" },
                propertyPrivate = true
        ),
        @Property(
                name = "service.description",
                value = "Provides Solr core services",
                propertyPrivate = true
        )
})
public class SolrCoreServlet extends SimpleJSONServlet {

    private static final Logger LOG = LoggerFactory.getLogger(SolrCoreServlet.class);
    @Reference
    private SolrConfigurationService solrConfigurationService;

    @Override
    public void getJSONWriter(PrintWriter writer)  {

        JSONWriter jsonWriter = new JSONWriter(writer);

        try {
            jsonWriter.array();
            for(String core: solrConfigurationService.getCores()) {
                jsonWriter.object();
                jsonWriter.key("text");
                jsonWriter.value(core);
                jsonWriter.key("value");
                jsonWriter.value(core);
                jsonWriter.endObject();
            }
            jsonWriter.endArray();

        } catch (JSONException e) {
            LOG.error("Error creating solr cores JSON", e);
        }
    }
}
