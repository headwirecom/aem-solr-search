
(function ($, $document) {
    "use strict";

    var CORE = "./solr-core", AVAIL_FIELDS = "./";

    function getCore() {
        //get the country widget
        var core = new CUI.Select({
            element: $("[name='" + CORE + "']").closest(".coral-Select")
        });

        return core;
    };


    function pullStoredFields(core){

        //
        return getSearchResultOptions(core);

    }

    /**
     * Queries a Solr core for its list of available search result fields (i.e., stored fields).
     *
     * @param {String} the Solr core to query
     */
    function getSearchResultOptions(coreName) {

        var facetOptions = '[]';

        // Note: We need a blocking call, otherwise this method may return before the
        // data is received.
        $.ajax({
            url: "/apps/solr/schema/fields/stored",
            data: { core: coreName },
            async: false
        }).done(function (data) {
            facetOptions =  data;
            console.log("Available stored fields:" + data);
        });

        return facetOptions;
    };



    //http://docs.adobe.com/docs/en/aem/6-0/develop/ref/granite-ui/api/index.html
    $document.on("dialog-ready", function() {
  //      $(window).adaptTo("foundation-ui").alert("Open", "Dialog now open, event [dialog-ready]");


    });

    $(document).on("click", ".cq-dialog-submit", function (e) {
        $(window).adaptTo("foundation-ui").alert("Close", "Dialog closed, selector [.cq-dialog-submit]");
    });

    $document.on("dialog-ready", function() {
        document.querySelector('form.cq-dialog').addEventListener('submit', function(){
            $(window).adaptTo("foundation-ui").alert("Close", "Dialog closed, selector [form.cq-dialog]");
        }, true);
    });

    $document.on("dialog-success", function() {
        $(window).adaptTo("foundation-ui").alert("Save", "Dialog content saved, event [dialog-success]");
    });

    $document.on("dialog-closed", function() {
      //  $(window).adaptTo("foundation-ui").alert("Close", "Dialog closed, event [dialog-closed]");
    });


})($, $(document));
