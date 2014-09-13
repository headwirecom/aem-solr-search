var CQSearch = {};
CQSearch.controller = {};

/**
 * Finds the name of the Solr core for this dialog.

 * @param {Object} scope
 * @return {String} The Solr Core
 */
CQSearch.controller.getCore = function(scope) {

    tabPanel = scope.findParentByType('tabpanel');
    generalPanel = tabPanel.findByType('panel')[0];
    solrCore = generalPanel.findByType('selection')[0];
    solrCoreName = solrCore.getValue();

    return solrCoreName;
};

/**
 * A simple health check to ensure that Solr is up and running. Alerts the user if Solr is unavailable.
 */
CQSearch.controller.solrHealthCheck = function(field) {

    var solrCores = [];

    // Use the Solr core lookup service for our health check.
    $.ajax({
        url: "/apps/solr/core",
        async: false
    }).done(function (data) {
        solrCores =  data;
    });

    if (solrCores.length == 0) {
        var message = [];
        message.push(
          "The Solr server seems to be unavailable. Check that your Solr server has been configured in the Felix console. ",
          "The service pid is 'com.headwire.cqsearch.services.SolrConfigurationService'. Also make sure that the Solr ",
          "server is running and that there are no network connection problems between CQ and Solr. ",
          "The functionality in this dialog is dependent on an available Solr server."
        );
        CQ.Ext.Msg.alert('Solr Server Unavailable', message.join(""));
    }
};

/**
 * Dynamically populates the list of available facet fields. This is intended to be invoked
 * by an optionsProvider.
 */
CQSearch.controller.provideFacetOptions = function(path, record) {

    var coreName = CQSearch.controller.getCore(this);

    return CQSearch.controller.getFacetOptions(coreName);
};

/**
 * An options provider that returns the supported list of facet sort options.
 */
CQSearch.controller.provideFacetSortOptions = function(path, record) {

    var options = [
        {
            text:"Count",
            value:"count"
        },
        {
            text:"Index",
            value:"index"
        }
    ];

    return options;
};

/**
 * Dynamically builds the list of available stored fields. This is intended to be invoked
 * by an optionsProvider.
 */
CQSearch.controller.provideStoredFieldsOptions = function(path, record) {

    var coreName = CQSearch.controller.getCore(this);

    return CQSearch.controller.getSearchResultOptions(coreName);
};

/**
 * This function is intended to be invoked by a listener when the Solr core selection option
 * changes. The function will update the list of available facet fields and result fields.
 *
 * @param {@link CQ.Ext.form.Field} field
 */
CQSearch.controller.updateSolrOptions = function(field) {

    // Get the currently selected core name.
    var coreName = field.getValue();
    console.log("Solr core name has changed to: '" + coreName + "'");

    CQSearch.controller.updateFacetOptions(field, coreName);
    CQSearch.controller.updateSearchResultOptions(field, coreName);
}

/**
 * The function will update the list of available facet fields for all active
 * facets in the multifield.
 *
 * @param {@link CQ.Ext.form.Field} field
 * @param {String} Solr core
 */
CQSearch.controller.updateFacetOptions = function(field, coreName) {

    var parentPanel = field.findParentByType('tabpanel');
    var activeFacets = parentPanel.findByType('cqsearchmultifield');

    // For each cqsearch multifield widget, update the list of available facet fields on
    // all active facets.
    for(var i = 0; i < activeFacets.length; i++){
        var name = activeFacets[i].name;
        if (name.indexOf("solr-facet-fields") > 0) {
            activeFacets[i].updateSelectionOptions(CQSearch.controller.getFacetOptions(coreName));
        }
    }
};

/**
 * The function will update the list of available search result fields.
 *
 * @param {@link CQ.Ext.form.Field} field
 * @param {String} Solr core
 */
CQSearch.controller.updateSearchResultOptions = function(field, coreName) {

    var parentPanel = field.findParentByType('tabpanel');
    var searchResultOptions = parentPanel.findByType('selection');

    for(var i = 0; i < searchResultOptions.length; i++){
        var name = searchResultOptions[i].name;
        if (( typeof name != 'undefined') && (name.indexOf("solr-result-fields") > 0) ) {
            searchResultOptions[i].setOptions(CQSearch.controller.getSearchResultOptions(coreName));
        }
    }
};

/**
 * Queries a Solr core for its list of available facetable fields (i.e., indexed fields).
 *
 * @param {String} the Solr core to query
 */
CQSearch.controller.getFacetOptions = function(coreName) {

    var facetOptions = '[]';

    // Note: We need a blocking call, otherwise this method may return before the
    // data is received.
    $.ajax({
        url: "/apps/solr/schema/fields/indexed",
        data: { core: coreName },
        async: false
    }).done(function (data) {
        facetOptions =  data;
        console.log("Available Facets:" + CQ.Ext.util.JSON.encode(data));
    });

    return facetOptions;
};

/**
 * Queries a Solr core for its list of available search result fields (i.e., stored fields).
 *
 * @param {String} the Solr core to query
 */
CQSearch.controller.getSearchResultOptions = function(coreName) {

    var facetOptions = '[]';

    // Note: We need a blocking call, otherwise this method may return before the
    // data is received.
    $.ajax({
        url: "/apps/solr/schema/fields/stored",
        data: { core: coreName },
        async: false
    }).done(function (data) {
        facetOptions =  data;
        console.log("Available stored fields:" + CQ.Ext.util.JSON.encode(data));
    });

    return facetOptions;
};
