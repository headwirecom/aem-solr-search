FROM centos:7

MAINTAINER headwire.com, Inc. <info@headwire.com>

ENV SOLR_VERSION 5.4.0
ENV SOLR_TGZ_URL https://archive.apache.org/dist/lucene/solr/$SOLR_VERSION/solr-$SOLR_VERSION.tgz

WORKDIR /tmp

# Download Solr separately to allow for a separate FS layer. This download can take some time,
RUN curl -O "$SOLR_TGZ_URL"

ENV SOLR_HOME /opt/solr-home

# Deploy Solr 5 following https://cwiki.apache.org/confluence/display/solr/Taking+Solr+to+Production
RUN set -x \
	&& yum -y install curl lsof java-1.8.0-openjdk-devel \
	&& tar -xzf solr-$SOLR_VERSION.tgz solr-$SOLR_VERSION/bin/install_solr_service.sh --strip-components=2 \
	&& useradd solr \
	&& ./install_solr_service.sh solr-$SOLR_VERSION.tgz -d $SOLR_HOME -p 8080 -u solr \
	&& chkconfig solr on \
	&& rm solr-$SOLR_VERSION.tgz

# Deploy sample Solr home directory
COPY solr-home $SOLR_HOME/data

RUN set -x \
	&& chown -R solr:solr $SOLR_HOME

WORKDIR $SOLR_HOME
   
EXPOSE 8080
CMD /etc/init.d/solr start && tail -f /opt/solr-home/logs/solr.log
