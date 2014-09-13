/**
 * @class CQSearch.MultiFieldKeyAndValueWidget
 * @extends CQ.form.CompositeField
 * This is a custom widget based on {@link CQ.form.CompositeField}.
 * @constructor
 * Creates a new MultiFieldKeyAndValueWidget.
 * @param {Object} config The config object
 */
CQSearch.MultiFieldKeyAndValueWidget = CQ.Ext.extend(CQ.form.CompositeField, {

    /**
     * @private
     * @type CQ.Ext.form.TextField
     */
    hiddenField: null,

    /**
     * @private
     * @type CQ.Ext.form.Selection
     */
    allowField: null,

    /**
     * @private
     * @type CQ.Ext.form.TextField
     */
    otherField: null,

    constructor: function(config) {
        config = config || { };
        var defaults = {
            bodyStyle: 'padding: 5px',
            border: true
        };
        config = CQ.Util.applyDefaults(config, defaults);
        CQSearch.MultiFieldKeyAndValueWidget.superclass.constructor.call(this, config);
    },

    // overriding CQ.Ext.Component#initComponent
    initComponent: function() {
        CQSearch.MultiFieldKeyAndValueWidget.superclass.initComponent.call(this);

        this.hiddenField = new CQ.Ext.form.Hidden({
            name: this.name
        });
        this.add(this.hiddenField);

        this.allowField = new CQ.form.Selection({
            cls: "cqsearch-mf-widget1",
            type: "select",
            fieldLabel: this.selectionLabel,
            listeners: {
                selectionchanged: {
                    scope:this,
                    fn: this.updateHidden
                }
            },
            optionsProvider: this.optionsProvider

        });
        this.add(this.allowField);

        this.otherField = new CQ.Ext.form.TextField({
            cls: "cqsearch-mf-widget2",
            fieldLabel: this.textfieldLabel,
            listeners: {
                change: {
                    scope:this,
                    fn:this.updateHidden
                }
            }
        });
        this.add(this.otherField);

    },

    // overriding CQ.form.CompositeField#processPath
    processPath: function(path) {
        console.log("CustomWidget#processPath", path);
        this.allowField.processPath(path);
    },

    // overriding CQ.form.CompositeField#processRecord
    processRecord: function(record, path) {
        console.log("MultiFieldKeyAndValueWidget#processRecord", path, record);
        this.allowField.processRecord(record, path);
    },

    // overriding CQ.form.CompositeField#setValue
    setValue: function(value) {
        var parts = value.split(":");
        this.allowField.setValue(parts[0]);
        this.otherField.setValue(parts[1]);
        this.hiddenField.setValue(value);
    },

    // overriding CQ.form.CompositeField#getValue
    getValue: function() {
        return this.getRawValue();
    },

    // overriding CQ.form.CompositeField#getRawValue
    getRawValue: function() {
        if (!this.allowField) {
            return null;
        }
        return this.allowField.getValue() + ":" +
               this.otherField.getValue();
    },

    // private
    updateHidden: function() {
        this.hiddenField.setValue(this.getValue());
    },

    updateSelectionOptions: function(options) {
        this.allowField.setOptions(options);
    }
});

// register xtype
CQ.Ext.reg('cqsearchmultifield', CQSearch.MultiFieldKeyAndValueWidget);
