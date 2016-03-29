(function ($, $document) {
    "use strict";

    var CORE = "./solr-core",
        RESULT_FIELDS = "./solr-result-fields",
        HIGHLIGHT_FIELDS = "./highlighting-fields", storedFields = {};


    function adjustLayoutHeight() {
        $(".coral-FixedColumn-column").css("height", "20rem");
    }


    $document.on("dialog-ready", function () {

        adjustLayoutHeight();

        var coreSelect = new CUI.Select({
            element: $("[name='" + CORE + "']").closest(".coral-Select")
        });

        if (_.isEmpty(coreSelect)) {
            return;
        }

        //workaround to remove the options getting added twice, using CUI.Select()
        var $coreOptions = coreSelect._selectList.children();
        var seen = {};
        $($coreOptions.each(function () {
            var txt = $(this).text();
            if (seen[txt])
                $(this).remove();
            else
                seen[txt] = true;
        }));

        addDataToFields(coreSelect.getValue());

        function addDataToFields(coreName) {

            storedFields = getSearchResultOptions(event.selectedValue);
            updateCoreRelatedFields()

        }

        //listener on coreSelect select for dynamically filling the related Fields
        coreSelect._selectList.on('selected.select', function (event) {

            if (_.isEmpty(event.selectedValue)) {
                console.log('coreName is empty');
                return;
            }

            storedFields = getSearchResultOptions(event.selectedValue);
            updateCoreRelatedFields();
        });

    });

    function updateCoreRelatedFields() {
        // It will update all the dependent fields based on the coreName
        updateHightlightFields(storedFields);
        updateResultFields(storedFields);

    }

    function updateHightlightFields(storedFields) {

        var hl_fields = new CUI.Select({
            element: $("[name='" + HIGHLIGHT_FIELDS + "']").closest(".coral-Select")
        });

        if (_.isEmpty(hl_fields)) {
            return;
        }

        hl_fields._nativeSelect.children().remove();
        hl_fields._selectList.children().remove();

        _.each(storedFields, function (value, field) {
            if ((field.value == "*")) {
                return;
            }

            $("<option>").appendTo(hl_fields._nativeSelect)
                .val(value.value).html(value.text);
        });

        hl_fields = new CUI.Select({
            element: $("[name='" + HIGHLIGHT_FIELDS + "']").closest(".coral-Select")
        });

        var $form = hl_fields.$element.closest("form");

        //removing ./
        var fieldName = HIGHLIGHT_FIELDS.slice(2);

        $.getJSON($form.attr("action") + ".json").done(function (data) {
            if (_.isEmpty(data)) {
                return;
            }

            hl_fields._nativeSelect.val(data['' + fieldName + '']).trigger('change');
        })

    }

    function updateResultFields(storedFields) {

        var al_fields = new CUI.Select({
            element: $("[name='" + RESULT_FIELDS + "']").closest(".coral-Select")
        });

        if (_.isEmpty(al_fields)) {
            return;
        }

        al_fields._nativeSelect.children().remove();
        al_fields._selectList.children().remove();

        _.each(storedFields, function (value, field) {
            if ((field.value == "*")) {
                return;
            }

            $("<option>").appendTo(al_fields._nativeSelect)
                .val(value.value).html(value.text);
        });

        al_fields = new CUI.Select({
            element: $("[name='" + RESULT_FIELDS + "']").closest(".coral-Select")
        });

        var $form = al_fields.$element.closest("form");

        //removing ./
        var fieldName = RESULT_FIELDS.slice(2);

        $.getJSON($form.attr("action") + ".json").done(function (data) {
            if (_.isEmpty(data)) {
                return;
            }

            al_fields._nativeSelect.val(data['' + fieldName + '']).trigger('change');
        })

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
            data: {core: coreName},
            async: false
        }).done(function (data) {
            facetOptions = data;
            console.log("Available stored fields:" + JSON.stringify(data));
        });

        return facetOptions;
    };

})($, $(document));