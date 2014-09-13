/*
 * Author  : Gaston Gonzalez
 * Date    : 6/30/13
 * Version : $Id$
 */
package com.headwire.aemsolrsearch.services.servlets;

import com.headwire.aemsolrsearch.services.SolrConfigurationService;
import org.apache.felix.scr.annotations.*;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.io.JSONWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;

/**
 * SolrSchemaIndexedFieldsServlet is responsible for <em>auto-discovering</em> the list of available indexed fields.
 *
 * @author <a href="mailto:gg@headwire.com">Gaston Gonzalez</a>
 */
@Service
@Component(
        name = "com.headwire.aemsolrsearch.services.servlets.SolrSchemaIndexedFieldsServlet",
        metatype = true,
        immediate = true,
        label = "Solr schema indexed fields servlet",
        description = "Auto-discovers the list of 'indexed' fields."
)
@Properties({
        @Property(
                name = "sling.servlet.paths",
                value = { "/apps/solr/schema/fields/indexed" },
                propertyPrivate = true
        ),
        @Property(
                name = "sling.servlet.methods",
                value = { "GET", "POST" },
                propertyPrivate = true
        ),
        @Property(
                name = "service.description",
                value = "Auto-discovers the list of 'indexed' fields.",
                propertyPrivate = true
        )
})
public class SolrSchemaIndexedFieldsServlet extends SimpleJSONServlet {

    private static final Logger LOG = LoggerFactory.getLogger(SolrSchemaIndexedFieldsServlet.class);
    @Reference
    private SolrConfigurationService solrConfigurationService;

    @Override
    public void getJSONWriter(PrintWriter writer)  {

        JSONWriter jsonWriter = new JSONWriter(writer);

        try {
            jsonWriter.array();
            for(String field: solrConfigurationService.getIndexedFieldsFromLuke(getSolrCore())) {
                jsonWriter.object();
                jsonWriter.key("text");
                jsonWriter.value(field);
                jsonWriter.key("value");
                jsonWriter.value(field);
                jsonWriter.endObject();
            }
            jsonWriter.endArray();
        } catch (JSONException e) {
            LOG.error("Error creating indexed fields JSON", e);
        }
    }

}
