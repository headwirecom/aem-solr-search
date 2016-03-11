package com.headwire.aemsolrsearch.geometrixxmedia.model;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Optional;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author <a href="mailto:gg@headwire.com">Gaston Gonzalez</a>
 */
@Model(adaptables = Resource.class)
public class GeometrixxMediaAuthorSummary {

    @Inject @Named("givenName") @Default(values = "") @Optional
    private String firstName;

    @Inject @Named("familyName") @Default(values = "") @Optional
    private String lastName;

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String displayName() {
        return firstName + " " + lastName;
    }

    @Override public String toString() {
        final StringBuilder sb = new StringBuilder("GeometrixxMediaAuthorSummary{");
        sb.append("firstName='").append(firstName).append('\'');
        sb.append(", lastName='").append(lastName).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
