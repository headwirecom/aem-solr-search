/*
 * Author  : Gaston Gonzalez
 * Date    : 7/7/13
 * Version : $Id$
 */
package com.headwire.aemsolrsearch.services.servlets;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.jcr.api.SlingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.PrintWriter;

@Component(componentAbstract = true)
/**
 * SimpleJSONServlet is responsible for providing a basic template for returning schema information
 * in the form of a JSON response.
 *
 * @author <a href="mailto:gg@headwire.com">Gaston Gonzalez</a>
 */
public abstract class SimpleJSONServlet extends SlingSafeMethodsServlet {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleJSONServlet.class);
    private String solrCore;

    @Reference
    protected SlingRepository repository;

    public static final String REQUEST_PARAM_CORE_NAME = "core";

    /**
     * Implementing classes must write a valid JSON response to the passed in writer.
     * @param writer
     */
    public abstract void getJSONWriter(PrintWriter writer);

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException, IOException {

        PrintWriter out = response.getWriter();

        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");

        solrCore = request.getParameter(REQUEST_PARAM_CORE_NAME);

        getJSONWriter(response.getWriter());
    }

    public String getSolrCore() {
        return solrCore;
    }
}
