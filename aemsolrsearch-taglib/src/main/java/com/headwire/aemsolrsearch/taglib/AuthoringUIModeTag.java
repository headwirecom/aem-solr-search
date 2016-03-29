/*
 * Author  : Gaston Gonzalez
 * Date    : 17 February 2014
 * Version : $Id$
 */
package com.headwire.aemsolrsearch.taglib;

import com.cqblueprints.taglib.CqSimpleTagSupport;
import com.day.cq.wcm.api.AuthoringUIMode;
import com.squeakysand.jsp.tagext.annotations.JspTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.jsp.JspException;
import java.io.IOException;

/**
 * AuthoringUiMode is responsible for determining the current Authoring UI mode.
 *
 */

@JspTag
public class AuthoringUIModeTag extends CqSimpleTagSupport {

    private static final Logger LOG = LoggerFactory.getLogger(AuthoringUIModeTag.class);

    private boolean classicMode;
    private boolean touchMode;

    /** JSP attribute name holding Author TOUCH UI mode. */
    public static final String JSP_ATTR_AUTHOR_TOUCHI_UI = "touchMode";
    /** JSP attribute name holding Author Classic UI Mode. */
    public static final String JSP_ATTR_AUTHOR_CLASSIC_UI = "classicMode";

    @Override
    public void doTag() throws JspException, IOException {

        classicMode = AuthoringUIMode.fromRequest(getRequest()) == AuthoringUIMode.CLASSIC;
        touchMode = AuthoringUIMode.fromRequest(getRequest()) == AuthoringUIMode.TOUCH;

        getPageContext().setAttribute(JSP_ATTR_AUTHOR_TOUCHI_UI, touchMode);
        getPageContext().setAttribute(JSP_ATTR_AUTHOR_CLASSIC_UI, classicMode);
    }
}
