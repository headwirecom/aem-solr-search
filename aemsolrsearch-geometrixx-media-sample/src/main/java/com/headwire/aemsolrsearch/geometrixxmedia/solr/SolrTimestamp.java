package com.headwire.aemsolrsearch.geometrixxmedia.solr;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author <a href="mailto:gg@headwire.com">Gaston Gonzalez</a>
 */
public class SolrTimestamp {


    private SolrTimestamp() {
        // Prevent instantiation
    }

    /**
     * Converts a date to the UTC DateField format that Solr expects.
     *
     * @param date
     * @return
     */
    public static String convertToUtc(Date date) {

        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:00.00'Z'");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        return formatter.format(date);
    }

    public static String convertToUtcAndUseNowIfNull(Date date) {

        return date != null ? convertToUtc(date) : convertToUtc(new Date());
    }
}
