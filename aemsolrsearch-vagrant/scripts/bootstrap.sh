#!/bin/bash

SOLR_VERSION=5.4.1
SOLR_TGZ_URL=https://archive.apache.org/dist/lucene/solr/$SOLR_VERSION/solr-$SOLR_VERSION.tgz

SOLR_HOST_NODE1="solrnode1"
SOLR_HOST_NODE2="solrnode2"

curl -O "$SOLR_TGZ_URL"

yum -y install curl lsof net-tools java-1.8.0-openjdk-devel 

SOLR_HOME=/opt/solr-home
SOLR_INSTALL_DIR=/opt/solr

CWD=`pwd`

echo "Updating /etc/hosts..."
echo "127.0.0.1 $SOLR_HOST_NODE1" >> /etc/hosts
echo "127.0.0.1 $SOLR_HOST_NODE2" >> /etc/hosts

echo "Installing Solr $SOLR_VERSION..."
cd /opt
tar -xzf /home/vagrant/solr-$SOLR_VERSION.tgz
ln -s /opt/solr-$SOLR_VERSION $SOLR_INSTALL_DIR
cp -r /home/vagrant/sync/solr-home /opt

cd $SOLR_INSTALL_DIR

echo "Creating Solr user and fixing permissions..."
useradd solr
chown -R solr:solr $SOLR_HOME $SOLR_INSTALL_DIR /opt/solr-$SOLR_VERSION

# Install System V scripts
/home/vagrant/sync/scripts/install-solr-services.sh

echo "Starting node 1..."
#su solr -c "bin/solr start -c -p 8983 -h $SOLR_HOST_NODE1 -s /opt/solr-home/node1/solr"
service solr1 start

echo "Uploading AEM Solr Search sample Geometrixx config set to Zookeeper"
server/scripts/cloud-scripts/zkcli.sh -zkhost 127.0.0.1:9983 -cmd upconfig -confname geometrixx -confdir /opt/solr-home/configsets/geomtrixx/conf

echo "Starting node 2..."
#su solr -c "bin/solr start -c -p 7574 -h $SOLR_HOST_NODE2 -z localhost:9983 -s /opt/solr-home/node2/solr" 
service solr2 start

echo "Creating collection..."
curl "http://localhost:8983/solr/admin/collections?action=CREATE&name=collection1&numShards=2&replicationFactor=2&maxShardsPerNode=2&collection.configName=geometrixx"

cat << EOF

*****************************************************************************************
* On your host OS, please add the following host entries for ZooKeeper to work correctly:
* 
* 127.0.0.1 $SOLR_HOST_NODE1
* 127.0.0.1 $SOLR_HOST_NODE2
*****************************************************************************************
EOF
