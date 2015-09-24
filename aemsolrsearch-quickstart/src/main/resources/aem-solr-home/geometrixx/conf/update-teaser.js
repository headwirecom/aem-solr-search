/*
 * Author      : Gaston Gonzalez
 * Date        : 17 February 2014
 * Description : Script update processor for building a basic teaser.
 */

function processAdd(cmd) {

    doc = cmd.solrDoc; 
    content = doc.getFieldValue("body");

    teaser = '';
    
    doc.setField("teaser", teaser);
}

function processDelete(cmd) {
  // no-op
}

function processMergeIndexes(cmd) {
  // no-op
}

function processCommit(cmd) {
  // no-op
}

function processRollback(cmd) {
  // no-op
}

function finish() {
  // no-op
}
