package com.headwire.aemsolrsearch.geometrixx.servlets;

import com.day.cq.dam.api.DamConstants;
import com.day.cq.wcm.api.NameConstants;
import com.headwire.aemsolrsearch.geometrixx.model.GeometrixxContentType;
import com.headwire.aemsolrsearch.geometrixx.model.GeometrixxPage;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;

/**
 * Renders supported Sling resource types using Solr's JSON update handler format. The following Sling resource
 * types are supported:
 *
 * <ul>
 *     <li><code>geometrixx/components/page</code></li>
 * </ul>
 *
 * <em>Note: *.solr.json should be blocked at dispatcher</em>
 *
 * @author <a href="mailto:gg@headwire.com">Gaston Gonzalez</a>
 */
@Component(immediate = true, metatype = true)
@Service(Servlet.class)
@Properties({
        @Property(name = Constants.SERVICE_VENDOR, value = "headwire.com, Inc."),
        @Property(name = Constants.SERVICE_DESCRIPTION, value = "Renders content using the Solr JSON update handler format "),
        @Property(name = "sling.servlet.methods", value = "GET"),
        @Property(name = "sling.servlet.resourceTypes", value = {NameConstants.NT_PAGE, DamConstants.NT_DAM_ASSET}),
        @Property(name = "sling.servlet.selectors", value = "solr"),
        @Property(name = "sling.servlet.extensions", value = "json")
})
public class SolrUpdateHandler extends SlingSafeMethodsServlet {

    private static final Logger LOG = LoggerFactory.getLogger(SolrUpdateHandler.class);

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");

        Resource resource = request.getResource();
        GeometrixxContentType contentType = resource.adaptTo(GeometrixxPage.class);

        response.getWriter().write(contentType.getJson().toJSONString());
    }
}
