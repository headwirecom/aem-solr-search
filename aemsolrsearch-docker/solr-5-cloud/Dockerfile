FROM centos:7

MAINTAINER headwire.com, Inc. <info@headwire.com>

ENV SOLR_VERSION 5.4.0
ENV SOLR_TGZ_URL https://archive.apache.org/dist/lucene/solr/$SOLR_VERSION/solr-$SOLR_VERSION.tgz

WORKDIR /opt

# Download Solr separately to allow for a separate FS layer. This download can take some time,
RUN curl -O "$SOLR_TGZ_URL"

# Also download the OS packages separately.
RUN yum -y install curl lsof net-tools java-1.8.0-openjdk-devel 

ENV SOLR_HOME /opt/solr-home
ENV SOLR_INSTALL_DIR /opt/solr

COPY solr-home $SOLR_HOME

RUN set -x \
	&& tar -xzf solr-$SOLR_VERSION.tgz \
	&& ln -s /opt/solr-$SOLR_VERSION $SOLR_INSTALL_DIR \
	&& rm solr-$SOLR_VERSION.tgz 

WORKDIR $SOLR_INSTALL_DIR

RUN set -x \
	&& bin/solr start -c -p 8983 -s /opt/solr-home/node1/solr \
	&& server/scripts/cloud-scripts/zkcli.sh -zkhost 127.0.0.1:9983 -cmd upconfig -confname geometrixx -confdir /opt/solr-home/configsets/geomtrixx/conf \
	&& bin/solr start -c -p 7574 -z localhost:9983 -s /opt/solr-home/node2/solr \
	&& curl "http://localhost:8983/solr/admin/collections?action=CREATE&name=collection1&numShards=2&replicationFactor=2&maxShardsPerNode=2&collection.configName=geometrixx"

EXPOSE 7574 8983 9983
CMD bin/solr start -c -p 8983 -s /opt/solr-home/node1/solr && bin/solr start -c -p 7574 -z localhost:9983 -s /opt/solr-home/node2/solr&& bash
