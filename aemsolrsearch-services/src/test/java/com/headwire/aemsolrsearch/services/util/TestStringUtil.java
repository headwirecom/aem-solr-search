/*
 * Author  : Gaston Gonzalez
 * Date    : 7/4/13
 * Version : $Id$
 */
package com.headwire.aemsolrsearch.services.util;

import static org.junit.Assert.*;

import com.headwire.aemsolrsearch.services.util.StringUtil;
import org.junit.*;

public class TestStringUtil {

    @Test
    public void testArrayToJsonWithEmptyArray() {

        assertEquals("[]", StringUtil.arrayToJson(new String[]{}));
    }

    @Test
    public void testArrayToJsonWithNull() {

        assertEquals("[]", StringUtil.arrayToJson(null));
    }

    @Test
    public void testArrayToJsonWithValid() {

        assertEquals("[\"item1\",\"item2\"]", StringUtil.arrayToJson(new String[]{"item1","item2"}));
    }
}
