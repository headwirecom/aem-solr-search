# About This Project

This is a Vagrant image specfication for Solr 5 running in cloud mode. Additionatly, it provides 
the configuration set used by the Geometrixx Sample.

It provides two Solr nodes using an embedded ZooKeeper instance.

# Requirements

* VirtualBox
* Vagrant


# Provisioning Solr 5


1. Change into the root of this directory: `aem-solr-search/aemsolrsearch-vagrant`

2. Provision the VM with Vagrant:

        $ vagrant up

3. In order for the SolrJ client to work nicely with ZooKeeper, you will need to add the following
   entries to your host operating system's hosts file:

        127.0.0.1 solrnode1
        127.0.0.1 solrnode2 

4. Once the provisioning is done, you can access Solr:

    * http://solrnode1:8983/solr
    * http://solrnode2:7574/solr
