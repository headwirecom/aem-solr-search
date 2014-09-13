package com.headwire.aemsolrsearch.geometrixxmedia.solr.index;

import com.headwire.aemsolrsearch.services.AbstractSolrService;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * AbstractSolrIndexService provides basic support for indexing Solr documents. This implementation creates
 * a shared instance of SolrServer. Extend this class and override getSolrServer(getCoreName()) if you need to
 * communicate with multiple Solr servers or cores.
 */
public abstract class AbstractSolrIndexService extends AbstractSolrService {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractSolrIndexService.class);

    /**
     * Adds a document to the index, then commits it.
     *
     * @param doc
     */
    public void addAndCommit(SolrInputDocument doc) {
        add(doc);
        commit();
    }

    /**
     * Deletes a document by ID and performs commit.
     *
     * @param docId Solr document ID
     */
    public void deleteAndCommit(String docId) {
        delete(docId);
        commit();
    }

    /**
     * Adds a document to the index. This method does not perform a commit. You may call commit()
     * yourself after add(), or you may use the addAndCommit() method.
     *
     * @param doc
     */
    public void add(SolrInputDocument doc) {

        try {
            LOG.info("Adding document to Solr index: {}", doc);

            getSolrServer(getCoreName()).add(doc);

        } catch (SolrServerException e) {
            LOG.error("Error indexing: {}", doc, e);
        } catch (IOException e) {
            LOG.error("Error indexing: {}", doc, e);
        }
    }

    /**
     * Deletes a document by ID.
     *
     * @param docId Solr document ID
     */
    public void delete(String docId) {

        try {
            LOG.info("Deleting document from Solr index: {}", docId);

            getSolrServer(getCoreName()).deleteById(docId);

        } catch (SolrServerException e) {
            LOG.error("Error deleting document: {}", docId, e);
        } catch (IOException e) {
            LOG.error("Error deleting document: {}", docId, e);
        }
    }

    /**
     * Commits any pending index changes.
     */
    public void commit() {

        try {
            getSolrServer(getCoreName()).commit();
        } catch (SolrServerException e) {
            LOG.error("Error committing change to index", e);
        } catch (IOException e) {
            LOG.error("Error committing change to index", e);
        }
    }

    public abstract String getCoreName();
}
