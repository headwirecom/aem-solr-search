package com.headwire.aemsolrsearch.geometrixx.model;

import org.apache.solr.common.SolrInputDocument;
import org.json.simple.JSONObject;

public interface GeometrixxContentType {

    JSONObject getJson();

    SolrInputDocument getSolrDoc();

}
