<%@ include file="/apps/aemsolrsearch/components/global.jspx" %>
<%@ taglib prefix="cqsearch" uri="http://aemsolrsearch.headwire.com/taglibs/aemsolrsearch-taglib" %>

<%--
  | Note: If you wish to extend this JSP, only include valid JavaScript in the response, as this
  |       JSP is included by searchcontroller.jsp.
  --%>

<c:if test="${paginationEnabled}">
    Manager.addWidget(new AjaxSolr.StatisticsWidget({
      id: 'search-statistics',
      target: '#search-statistics',
      renderHeader: function (perPage, offset, total, qTime) {
        $('#search-statistics').html($('<span></span>').text('Found ' + total + ' results in ' + qTime + ' seconds. Displaying results ' + Math.min(total, offset + 1) + ' to ' + Math.min(total, offset + perPage) + '.'));
      }
    }));
</c:if>
