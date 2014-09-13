(function (callback) {
  if (typeof define === 'function' && define.amd) {
    define(['core/AbstractWidget'], callback);
  }
  else {
    callback();
  }
}(function () {

(function ($) {

/**
 * Displays search statistics.
 *
 * @expects this.target to be a list.
 * @class StatisticsWidget
 * @augments AjaxSolr.AbstractWidget
 * @todo Don't use the manager to send the request. Request only the results,
 * not the facets. Update only itself and the results widget.
 */
AjaxSolr.StatisticsWidget = AjaxSolr.AbstractWidget.extend(
  /** @lends AjaxSolr.StatisticsWidget.prototype */
  {
  /**
   * @param {Object} [attributes]
   * @param {Number} [attributes.innerWindow] How many links are shown around
   *   the current page. Defaults to 4.
   * @param {Number} [attributes.outerWindow] How many links are around the
   *   first and the last page. Defaults to 1.
   * @param {String} [attributes.separator] Separator between pagination links.
   *   Defaults to " ".
   */
  constructor: function (attributes) {
    AjaxSolr.StatisticsWidget.__super__.constructor.apply(this, arguments);
    AjaxSolr.extend(this, {
      separator: ' ',
      // The current page number.
      currentPage: null,
      // The total number of pages.
      totalPages: null
    }, attributes);
  },

  /**
   * An abstract hook for child implementations.
   *
   * @param {Number} perPage The number of items shown per results page.
   * @param {Number} offset The index in the result set of the first document to render.
   * @param {Number} total The total number of documents in the result set.
   */
  renderHeader: function (perPage, offset, total, qTime) {},

  /**
   * @returns {Number} The number of results to display per page.
   */
  perPage: function () {
    return parseInt(this.manager.response.responseHeader && this.manager.response.responseHeader.params && this.manager.response.responseHeader.params.rows || this.manager.store.get('rows').val() || 10);
  },

  /**
   * @returns {Number} The Solr offset parameter's value.
   */
  getOffset: function () {
    return parseInt(this.manager.response.responseHeader && this.manager.response.responseHeader.params && this.manager.response.responseHeader.params.start || this.manager.store.get('start').val() || 0);
  },

  afterRequest: function () {
    var perPage = this.perPage();
    var offset  = this.getOffset();
    var total   = parseInt(this.manager.response.response.numFound);
    var qTime   = parseInt(this.manager.response.responseHeader.QTime) / 1000;

    // Normalize the offset to a multiple of perPage.
    offset = offset - offset % perPage;

    this.currentPage = Math.ceil((offset + 1) / perPage);
    this.totalPages = Math.ceil(total / perPage);

    $(this.target).empty();

    this.renderHeader(perPage, offset, total, qTime);
  }
});

})(jQuery);

}));
