package com.headwire.aemsolrsearch.geometrixxmedia.model;

import org.apache.solr.common.SolrInputDocument;
import org.json.simple.JSONObject;

public interface GeometrixxMediaContentType {

    JSONObject getJson();

    SolrInputDocument getSolrDoc();

}
