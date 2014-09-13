/*
 * Author  : Gaston Gonzalez
 * Date    : 7/4/13
 * Version : $Id$
 */
package com.headwire.aemsolrsearch.services.util;

import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.io.JSONWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringWriter;

/**
 * StringUtil provides a number of string utility methods.
 *
 * @author <a href="mailto:gg@headwire.com">Gaston Gonzalez</a>
 */
public class StringUtil {

    private static final Logger LOG = LoggerFactory.getLogger(StringUtil.class);
    public static final String[] EMPTY_STRING_ARRAY = new String[]{};
    public static final String EMPTY_JSON_ARRAY= "[]";

    /**
     * Converts an array into a JSON array.
     *
     * @param array
     * @return A compatible JSON array on success, and an empty JSON array otherwise.
     */
    public static String arrayToJson(String[] array) {

        if (null == array) {return EMPTY_JSON_ARRAY;}

        StringWriter stringWriter = new StringWriter();
        JSONWriter jsonWriter = new JSONWriter(stringWriter);

        try {
            jsonWriter.array();
            for (String item: array) {
                jsonWriter.value(item);
            }
            jsonWriter.endArray();

        } catch (JSONException e) {
            LOG.error("Can't convert array '{}' to JSON", array, e);
            return EMPTY_JSON_ARRAY;
        }

        return stringWriter.toString();
    }
}
