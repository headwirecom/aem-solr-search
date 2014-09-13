/*
 * Author  : Gaston Gonzalez
 * Date    : 7/11/13
 * Version : $Id$
 */
package com.headwire.aemsolrsearch.taglib;

/**
 * Facet is responsible for...
 *
 * @author <a href="mailto:gg@headwire.com">Gaston Gonzalez</a>
 */
public class Facet {

    private String key;
    private String name;

    public Facet(String key, String name) {
        this.key = key;
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Facet{");
        sb.append("key='").append(key).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
