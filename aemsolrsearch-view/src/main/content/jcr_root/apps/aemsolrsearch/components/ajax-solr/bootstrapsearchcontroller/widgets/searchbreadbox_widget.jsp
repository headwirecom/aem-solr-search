<%@ include file="/apps/aemsolrsearch/components/global.jspx" %>
<%@ taglib prefix="cqsearch" uri="http://aemsolrsearch.headwire.com/taglibs/aemsolrsearch-taglib" %>

<%--
  | Note: If you wish to extend this JSP, only include valid JavaScript in the response, as this
  |       JSP is included by searchcontroller.jsp.
  --%>
<c:if test="${breadboxEnabled}">

    AjaxSolr.CurrentSearchWidget = AjaxSolr.AbstractWidget.extend({
      start: 0,

      afterRequest: function () {
        var self = this;
        var links = [];

        var q = this.manager.store.get('q').val();
        if (q != '*:*') {
          links.push($('<a href="#" class="badge"></a>').text(q).click(function () {
            self.manager.store.get('q').val('*:*');
            self.doRequest();
            return false;
          }));
        }

        <%-- Create a lookup table: facet name-to-value --%>
        var facetLookup = <cqsearch:json attribute="facetsAvailableFacets"/>;

        <%-- Build an array containing the content author-defined filter queries. --%>
        var hiddenBreadboxItems = <cqsearch:json attribute="advancedFilterQueries"/>;

        var fq = this.manager.store.values('fq');
        for (var i = 0, l = fq.length; i < l; i++) {

          var facetName = findFacetName(fq[i], facetLookup);

          <c:choose>
            <c:when test="${advancedFilterQueriesInBreadboxEnabled}">
              links.push($('<a href="#"></a>').text('[x] ' + facetName).click(self.removeFacet(fq[i])));
            </c:when>
            <c:otherwise>
              <%-- Prevent facet queries from appearing in the breadbox. --%>
              if ($.inArray(fq[i], hiddenBreadboxItems) == -1) {
                links.push($('<a href="#" class="badge"></a>').text(facetName).click(self.removeFacet(fq[i])));
              }
            </c:otherwise>
          </c:choose>
        }

        if (links.length > 1) {
          links.unshift($('<a href="#"></a>').text('Remove all').click(function () {
            self.manager.store.get('q').val('*:*');
            self.manager.store.remove('fq');


            <%-- Re-add filter queries, if and only if, they are not allowed in the breadbox --%>
            <c:if test="${facetsEnabled and not advancedFilterQueriesInBreadboxEnabled}">
                <c:forEach  var="curFilterQuery" items="${advancedFilterQueries}">
                    self.manager.store.addByValue('fq', '${curFilterQuery}');
                </c:forEach>
            </c:if>

            self.doRequest();
            return false;
          }));
        }

        if (links.length) {
          var $target = $(this.target);
          $target.empty();
          for (var i = 0, l = links.length; i < l; i++) {
            $target.append($('<li></li>').append(links[i]));
          }
        }
        else {
          $(this.target).html('<li>Viewing all results</li>');
        }
      },

      removeFacet: function (facet) {
        var self = this;
        return function () {
          if (self.manager.store.removeByValue('fq', facet)) {
            self.doRequest();
          }
          return false;
        };
      }
    });

    Manager.addWidget(new AjaxSolr.CurrentSearchWidget({
      id: '${breadboxId}',
      target: '${breadboxTarget}'
    }));

    var findFacetName = function(facetKeyAndValue, facetArray) {

        var facetPair = facetKeyAndValue.split(':');
        for (var i = 0, len = facetArray.length; i < len; i++) {

            if (facetArray[i].key === facetPair[0])
                return facetPair[1];
        }

        return facetPair[1];
    }
</c:if>
