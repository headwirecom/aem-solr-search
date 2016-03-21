(function () {
    var DATA_EAEM_NESTED = "data-ajax-eaem-nested",
        CFFW = ".coral-Form-fieldwrapper",
        CORE = "./solr-core",
        FACET_FIELDS = "./solr-field-select",
        SOLR_FACET_MULTI = "./solr-facets_multi",
        SOLR_FACET_FIELDS = "./solr-facet-fields",
        facetOptions = '[]',
        MULTI_FIELDS_NAMES = ["solr-field-select", "solr-facet-name"];


    function setSelect($field, value){
        var select = $field.closest(".coral-Select").data("select");

        if(select){
            select.setValue(value);
        }
    }

    function setCheckBox($field, value){
        $field.prop( "checked", $field.attr("value") == value);
    }

    //reads multifield data from server, creates the nested composite multifields and fills them
    function addDataInFields() {
        function getMultiFieldNames($multifields){
            var mNames = {}, mName;

            $multifields.each(function (i, multifield) {
                mName = $(multifield).children("[name$='@Delete']").attr("name");

                mName = mName.substring(0, mName.indexOf("@"));

                mName = mName.substring(2);

                mNames[mName] = $(multifield);
            });

            return mNames;
        }

        function buildMultiField(data, $multifield, mName){
            if (_.isEmpty(mName) || _.isEmpty(data)) {
                return;
            }

            var dataArr = [];
            if (!$.isArray(data) && (typeof data == "string")) {
                dataArr = $.makeArray(data);
            } else {
                dataArr = data;
            }

            _.each(dataArr, function(value, key){
                if(key == "jcr:primaryType"){
                    return;
                }

                $multifield.find(".js-coral-Multifield-add").click();

                var values = [];
                if(value.indexOf(':') != -1){
                    values = value.split(":")
                }

                _.each(values, function(fValue, index) {

                    var $field = $multifield.find("[name='./" + MULTI_FIELDS_NAMES[index] + "']").last(),
                        type = $field.prop("type");


                    if(_.isEmpty($field)){
                        return;
                    }

                    //handle single selection dropdown
                    if( type == "select-one"){
                        setSelect($field, fValue);
                    }else if( type == "checkbox"){
                        setCheckBox($field, fValue);
                    }else{
                        $field.val(fValue);
                    }


                });
            });
        }

        $(document).on("dialog-ready", function() {
            var $multifields = $("[" + DATA_EAEM_NESTED + "]");

            if(_.isEmpty($multifields)){
                return;
            }

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

            //listener on coreSelect select for dynamically filling the related Fields
            coreSelect._selectList.on('selected.select', function (event) {

                if (_.isEmpty(event.selectedValue)) {
                    console.log('coreName is empty');
                    return;
                }

                addDataToFields(event.selectedValue);
            });

            // Add Listener for - Add Field event
            $multifields.on("click", ".js-coral-Multifield-add", function (e) {
                console.log("js-coral-Multifield-add");
                updateFacetOptions();
            });

            var mNames = getMultiFieldNames($multifields),
                $form = $(".cq-dialog"),
                actionUrl = $form.attr("action") + ".infinity.json";

            $.ajax(actionUrl).done(postProcess);

            var solrfieldname = SOLR_FACET_FIELDS.substring(2);

            function postProcess(data){
                _.each(mNames, function($multifield, mName){
                    buildMultiField(data[solrfieldname], $multifield, mName);
                });
            }
        });
    }

    //collect data from widgets in multifield and POST them to CRX
    function collectDataFromFields(){
        function fillValue($form, fieldSetName, $field, counter, valToVal){
            var name = $field.attr("name");

            if (!name) {
                return;
            }

            //strip ./
            if (name.indexOf("./") == 0) {
                name = name.substring(2);
            }

            var value = $field.val();

            if( $field.prop("type") == "checkbox" ){
                value = $field.prop("checked") ? $field.val() : "";
            }

            $('<input />').attr('type', 'hidden')
                .attr('name', fieldSetName + "/" + counter + "/" + name)
                .attr('value', value )
                .appendTo($form);


            //remove the field, so that individual values are not POSTed
            $field.remove();
        }

        $(document).on("click", ".cq-dialog-submit", function () {
            var $multifields = $("[" + DATA_EAEM_NESTED + "]");

            if(_.isEmpty($multifields)){
                return;
            }

            var $form = $(this).closest("form.foundation-form"),
                $fieldSets, $fields;

            $multifields.each(function(i, multifield){
                $fieldSets = $(multifield).find("[class='coral-Form-fieldset']");

                $fieldSets.each(function (counter, fieldSet) {
                    $fields = $(fieldSet).children().children(CFFW);
                    //TO Store as Child Notes
                    var valToVal = '';
                    $fields.each(function (j, field) {

                        if(valToVal == ''){
                            valToVal = $(field).find("[name]").val();

                        } else {
                            var str = $(field).find("[name]").val();
                            valToVal = valToVal + ":" + str;
                        }

                        fillValue($form, $(fieldSet).data("name"), $(field).find("[name]"), (counter + 1));
                    });

                    //add the record JSON in a hidden field as string - support classic UI
                    $('<input />').attr('type', 'hidden')
                        .attr('name', SOLR_FACET_FIELDS)
                        .attr('value', valToVal)
                        .appendTo($form);


                });
            });
        });
    }

    function updateFacetOptions() {

        var ft_fields = new CUI.Select({
            element: $("[name='" + FACET_FIELDS + "']").last().closest(".coral-Select")
        });

        if (_.isEmpty(ft_fields)) {
            return;
        }

        ft_fields._nativeSelect.children().remove();
        ft_fields._selectList.children().remove();

        $("<option>").appendTo(ft_fields._nativeSelect)
            .val('').html('Select');

        _.each(facetOptions, function (value, field) {
            if ((field.value == "*")) {
                return;
            }

            $("<option>").appendTo(ft_fields._nativeSelect)
                .val(value.value).html(value.text);
        });

        ft_fields = new CUI.Select({
            element: $("[name='" + FACET_FIELDS + "']").last().closest(".coral-Select")
        });
    }

    function addDataToFields(coreName) {

        facetOptions = getFacetOptions(coreName);

        updateFacetOptions();

    }

    function getFacetOptions(coreName) {

        var facetOptions = '[]';

        // Note: We need a blocking call, otherwise this method may return before the
        // data is received.
        $.ajax({
            url: "/apps/solr/schema/fields/indexed",
            data: {core: coreName},
            async: false
        }).done(function (data) {
            facetOptions = data;
            console.log("Ajax-Nested:Available Facets:" + JSON.stringify(data));
        });

        return facetOptions;
    }

    $(document).ready(function () {
        addDataInFields();
        collectDataFromFields();
    });
})();