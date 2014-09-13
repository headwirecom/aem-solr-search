package com.headwire.aemsolrsearch.geometrixxmedia.util;

import com.petebevin.markdown.MarkdownProcessor;
import org.apache.commons.lang.StringUtils;

/**
 * A simple utility class for handling Markdown.
 *
 * @author <a href="mailto:gg@headwire.com">Gaston Gonzalez</a>
 */
public class MarkdownUtils {

    /**
     * Converts Markdown to HTML.
     *
     * @param markdown
     * @return Markdown on success and an empty string otherwise.
     */
    public static String markdownToHtml(String markdown) {

        if (StringUtils.isBlank(markdown)) {
            return "";
        }

        MarkdownProcessor proc = new MarkdownProcessor();
        return proc.markdown(markdown);
    }
}
