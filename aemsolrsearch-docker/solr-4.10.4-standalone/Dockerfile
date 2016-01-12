FROM tomcat:8-jre8

ENV CATALINA_HOME /usr/local/tomcat
ENV SOLR_HOME /opt/solr-home
ENV PATH $CATALINA_HOME/bin:$PATH

RUN mkdir -p "$SOLR_HOME"
WORKDIR $SOLR_HOME

ENV SOLR_VERSION 4.10.4
ENV SOLR_TGZ_URL https://archive.apache.org/dist/lucene/solr/$SOLR_VERSION/solr-$SOLR_VERSION.tgz

# Deploy Solr 4 as a WAR file along with logging dependencies to Tomcat.
RUN set -x \
	&& curl -O "$SOLR_TGZ_URL" \
	&& tar -xzf solr-$SOLR_VERSION.tgz \
	&& cp solr-$SOLR_VERSION/dist/solr-$SOLR_VERSION.war $CATALINA_HOME/webapps/solr.war \
	&& cp solr-$SOLR_VERSION/example/lib/ext/*.jar $CATALINA_HOME/lib \
	&& cp solr-$SOLR_VERSION/example/resources/log4j.properties $CATALINA_HOME/lib \
	&& rm -rf solr-$SOLR_VERSION \
	&& rm solr-$SOLR_VERSION.tgz

# Deploy sample Solr home directory
COPY solr-home $SOLR_HOME

ENV SET_ENV_SH /usr/local/tomcat/bin/setenv.sh
RUN set -x \
	&& echo '#!/bin/bash' > $SET_ENV_SH \
	&& echo 'export JAVA_OPTS="-Dsolr.solr.home=/opt/solr-home"' >> $SET_ENV_SH \
	&& chmod 755 $SET_ENV_SH

EXPOSE 8080
CMD ["catalina.sh", "run"]
