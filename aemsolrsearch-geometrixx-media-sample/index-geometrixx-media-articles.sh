#!/bin/bash
# Author: Gaston Gonzalez <gg@headwire.com>
# Description: Performs a full re-index of Geometrixx Media Articles.
(set -o igncr) 2>/dev/null && set -o igncr; # this comment is needed
CQ_USER=admin
CQ_PASS=admin
CQ_HOST=localhost
CQ_PORT=4502

SOLR_HOST=localhost
SOLR_PORT=8888
SOLR_CORE=collection1

SLING_RESOURCE_TYPE=geometrixx-media/components/page/article
SAVE_FILE=article.json

# Request from CQ a dump of the content in the Solr JSON update handler format
curl -s -u ${CQ_USER}:${CQ_PASS} -o ${SAVE_FILE} http://${CQ_HOST}:${CQ_PORT}/apps/geometrixx-media/solr/updatehandler?type=${SLING_RESOURCE_TYPE}

# This will delete all documents in your Solr core. Adjust according, perhaps, by content type.
curl http://${SOLR_HOST}:${SOLR_PORT}/solr/${SOLR_CORE}/update?commit=true -H "Content-Type: application/json" --data-binary '{"delete": { "query":"*:*" }}'

# Post the local JSON file that was saved in step 1 to Solr and commit
curl http://${SOLR_HOST}:${SOLR_PORT}/solr/${SOLR_CORE}/update?commit=true -H "Content-Type: application/json" --data-binary @${SAVE_FILE}
