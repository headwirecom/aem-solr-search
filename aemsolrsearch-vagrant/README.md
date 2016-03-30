# About This Project

This is a Vagrant image specification for Solr 5 running in SolrCloud mode. It provides 
the configuration set used by the Geometrixx Media Site sample. You can use this project for development to version your
Solr configuration as well as use to the Vagrant provisioning scripts as a model for non-development deployments.

It provides two Solr nodes using an embedded ZooKeeper instance.

Note: This project is intended a as replacement for all the Solr 4.x quickstart distribution: `aem-solr-search/aemsolrsearch-quickstart`. 


# Requirements

* VirtualBox - https://www.virtualbox.org/wiki/Downloads
* Vagrant - https://www.vagrantup.com/downloads.html


# Provisioning Solr 5

1. Change into the root of this directory: `aem-solr-search/aemsolrsearch-vagrant`

2. Provision the VM with Vagrant:

        $ vagrant up

3. In order for the SolrJ client to work nicely with ZooKeeper, you will need to add the following entries to your host 
   operating system's hosts file:

        127.0.0.1 solrnode1
        127.0.0.1 solrnode2 

4. Once the provisioning is done, you can access Solr:

    * [http://solrnode1:8983/solr](http://solrnode1:8983/solr)
    * [http://solrnode2:7574/solr](http://solrnode2:7574/solr)
  
    
# Development Recommendations

This project is intended to help your team version your Solr project. The typical development flow looks as follows:

1. Make your desired modification to `aemsolrsearch-vagrant/solr-home`.

2. Destroy the current VirtualBox VM and provision a new VM. This will rebuild Solr from scratch using the configuration
   in this project.

        $ vagrant destroy -f && vagrant up
        
3. Re-index your content.
