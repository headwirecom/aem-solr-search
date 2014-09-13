<%@ include file="/apps/aemsolrsearch/components/global.jspx" %>
<%@ taglib prefix="cqsearch" uri="http://aemsolrsearch.headwire.com/taglibs/aemsolrsearch-taglib" %>

<%--
  | Note: If you wish to extend this JSP, only include valid JavaScript in the response, as this
  |       JSP is included by searchcontroller.jsp.
  --%>
<c:if test="${facetsEnabled}">

    var showNumFacets = ${facetsShowNumFacets} - 1;
    $(".facet_container").delegate(".show-more", "click", function() {

        if ($(this).hasClass('show-me')) {
            $(this).parent().find('li:gt(' + showNumFacets + ')').hide();
            $(this).text('${facetsShowMore}');
        } else {
            $(this).parent().find('li:gt(' + showNumFacets + ')').show();
            $(this).text('${facetsShowLess}');
        }

        $(this).toggleClass('show-me');
    });

    AjaxSolr.SimpleFacetWidget = AjaxSolr.AbstractFacetWidget.extend({
      afterRequest: function () {
        if (this.manager.response.facet_counts.facet_fields[this.field] === undefined) {
          $(this.target).html('no items found in current selection');
          return;
        }

        var maxCount = 0;
        var objectedItems = [];
        for (var facet in this.manager.response.facet_counts.facet_fields[this.field]) {
          var count = parseInt(this.manager.response.facet_counts.facet_fields[this.field][facet]);
          if (count > maxCount) {
            maxCount = count;
          }
          objectedItems.push({ facet: facet, count: count });
        }

        $(this.target).empty();
        for (var i = 0, l = objectedItems.length; i < l; i++) {
          var facet = objectedItems[i].facet;
          $(this.target).append(
            $('<li><a href="#"></a></li>')
            .text(facet + ' [' + objectedItems[i].count + ']')
            .addClass('facet_item')
            .click(this.clickHandler(facet))
          );
        }

        $(this.target).find('li:gt(' + showNumFacets + ')').hide();
        if ($(this.target).find('li').length > showNumFacets) {
            $(this.target).find('li').last().after('<a href="javascript:void(0)" class="show-more">${facetsShowMore}</a>');
        }

        <%-- Hide any facet containers with no values. --%>
        if ( $(this.target).find('li').length <= 1) {
          $(this.target).hide();
        }
      }
    });

    var fields = <cqsearch:json attribute="facetsAvailableFacetKeys"/>;

    for (var i = 0, l = fields.length; i < l; i++) {
        Manager.addWidget(new AjaxSolr.SimpleFacetWidget({
            id: fields[i],
            target: '#' + fields[i],
            field: fields[i]
        }));
    }
</c:if>
