/*
 * Author  : Gaston Gonzalez
 * Date    : 17 February 2014
 * Version : $Id$
 */
package com.headwire.aemsolrsearch.taglib;

import com.cqblueprints.taglib.CqSimpleTagSupport;
import com.day.cq.wcm.api.WCMMode;
import com.squeakysand.jsp.tagext.annotations.JspTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.jsp.JspException;
import java.io.IOException;

/**
 * WcmMode is responsible for determining the current WCM mode.
 *
 * @author <a href="mailto:gg@headwire.com">Gaston Gonzalez</a>
 */
@JspTag
public class WcmModeTag extends CqSimpleTagSupport {

    private static final Logger LOG = LoggerFactory.getLogger(WcmModeTag.class);

    private boolean wcmEdit;
    private boolean wcmDesign;
    private boolean wcmAuthor;

    /** JSP attribute name holding WCM Edit. */
    public static final String JSP_ATTR_WCM_EDIT = "wcmEdit";
    /** JSP attribute name holding WCM Design. */
    public static final String JSP_ATTR_WCM_DESIGN = "wcmDesign";
    /** JSP attribute name holding WCM Author. */
    public static final String JSP_ATTR_WCM_AUTHOR = "wcmAuthor";

    @Override
    public void doTag() throws JspException, IOException {

        wcmEdit = WCMMode.fromRequest(getRequest()) == WCMMode.EDIT;
        wcmDesign = WCMMode.fromRequest(getRequest()) == WCMMode.DESIGN;
        wcmAuthor = !(WCMMode.fromRequest(getRequest()) == WCMMode.DISABLED);

        getPageContext().setAttribute(JSP_ATTR_WCM_EDIT, wcmEdit);
        getPageContext().setAttribute(JSP_ATTR_WCM_DESIGN, wcmDesign);
        getPageContext().setAttribute(JSP_ATTR_WCM_AUTHOR, wcmAuthor);
    }
}
