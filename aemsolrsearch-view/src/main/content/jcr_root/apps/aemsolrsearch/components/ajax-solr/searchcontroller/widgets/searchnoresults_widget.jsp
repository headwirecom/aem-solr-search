<%@ include file="/apps/aemsolrsearch/components/global.jspx" %>
<%@ taglib prefix="cqsearch" uri="http://aemsolrsearch.headwire.com/taglibs/aemsolrsearch-taglib" %>

<%--
  | Note: If you wish to extend this JSP, only include valid JavaScript in the response, as this
  |       JSP is included by searchcontroller.jsp.
  --%>

<c:if test="${noResultsEnabled}">

    AjaxSolr.NoResultWidget = AjaxSolr.AbstractWidget.extend({

      beforeRequest: function () {
        $("${noResultsTarget}").empty();
      },

      afterRequest: function () {

        if (this.manager.response.response.numFound === 0) {
          $.get("${noParsysUrl}", function(data) {
            $("${noResultsTarget}").html(data);
          });
        }
      },

      init: function () {
        // do nothing
      }
    });

    Manager.addWidget(new AjaxSolr.NoResultWidget({
      id: '${noResultsId}',
      target: '${noResultsTarget}'
    }));
</c:if>
