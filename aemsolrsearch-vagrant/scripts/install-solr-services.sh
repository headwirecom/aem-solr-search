#!/bin/bash

echo "Copying System V start up scripts..."
cp /home/vagrant/sync/artifacts/solr1 /etc/init.d
cp /home/vagrant/sync/artifacts/solr2 /etc/init.d
chmod 755 /etc/init.d/solr*

echo "Copying environment settings..."
cp /home/vagrant/sync/artifacts/solr*.in.sh /etc/default

echo "Enabling Solr services..."
chkconfig solr1 on
chkconfig solr2 on
