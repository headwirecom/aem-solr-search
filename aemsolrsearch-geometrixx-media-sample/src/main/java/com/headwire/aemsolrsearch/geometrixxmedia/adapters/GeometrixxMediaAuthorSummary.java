package com.headwire.aemsolrsearch.geometrixxmedia.adapters;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:gg@headwire.com">Gaston Gonzalez</a>
 */
public class GeometrixxMediaAuthorSummary {

    private static final Logger LOG = LoggerFactory.getLogger(GeometrixxMediaAuthorSummary.class);
    public static final GeometrixxMediaAuthorSummary NULL = new GeometrixxMediaAuthorSummary("", "");

    private String firstName;
    private String lastName;

    public GeometrixxMediaAuthorSummary(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public static GeometrixxMediaAuthorSummary adaptFromResource(Resource resource) {

        if (null == resource) {
            return null;
        }

        if (ResourceUtil.isNonExistingResource(resource)) {
            LOG.warn("Can't adapt non existent resource: '{}'", resource.getPath());
            return null;
        }

        ValueMap valueMap = resource.adaptTo(ValueMap.class);
        final String firstName = valueMap.get("givenName", "");
        final String lastName = valueMap.get("familyName", "");
        return new GeometrixxMediaAuthorSummary(firstName, lastName);
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String displayName() {
        return firstName + " " + lastName;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("GeometrixxMediaAuthorSummary{");
        sb.append("firstName='").append(firstName).append('\'');
        sb.append(", lastName='").append(lastName).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
