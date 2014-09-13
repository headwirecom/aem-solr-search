/*
 * Author  : Gaston Gonzalez
 * Date    : 7/4/13
 * Version : $Id$
 */
package com.headwire.aemsolrsearch.taglib;

import com.cqblueprints.taglib.CqSimpleTagSupport;
import com.google.gson.Gson;
import com.squeakysand.jsp.tagext.annotations.JspTag;
import com.squeakysand.jsp.tagext.annotations.JspTagAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.jsp.JspException;
import java.io.IOException;

/**
 * JsonTag is responsible for taking JSP attributes and converting them to the equivalent
 * JSON representation.
 *
 * @author <a href="mailto:gg@headwire.com">Gaston Gonzalez</a>
 */
@JspTag
public class JsonTag extends CqSimpleTagSupport {

    private static final Logger LOG = LoggerFactory.getLogger(JsonTag.class);
    private String attribute;

    @Override
    public void doTag() throws JspException, IOException {

        Object attributeValue = getRequest().getAttribute(attribute);
        if (attributeValue != null) {
            getJspWriter().write(new Gson().toJson(attributeValue));
        } else {
            LOG.warn("Can't produce JSON for null attribute '{}'", attribute);
        }
    }

    public String getAttribute() {
        return attribute;
    }

    @JspTagAttribute(required = true, rtexprvalue = true)
    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }
}
