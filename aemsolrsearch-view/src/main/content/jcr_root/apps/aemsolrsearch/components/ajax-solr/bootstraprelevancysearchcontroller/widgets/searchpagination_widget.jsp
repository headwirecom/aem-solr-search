<%@ include file="/apps/aemsolrsearch/components/global.jspx" %>
<%@ taglib prefix="cqsearch" uri="http://aemsolrsearch.headwire.com/taglibs/aemsolrsearch-taglib" %>

<%--
  | Note: If you wish to extend this JSP, only include valid JavaScript in the response, as this
  |       JSP is included by searchcontroller.jsp.
  --%>

<c:if test="${paginationEnabled}">
    Manager.addWidget(new AjaxSolr.BootstrapPagerWidget({
      id: '${paginationId}',
      target: '${paginationTarget}',
      prevLabel: '${paginationPrevLabel}',
      nextLabel: '${paginationNextLabel}',
      innerWindow: 1,
      renderHeader: function (perPage, offset, total) {
        $('#pager-header').html($('<span></span>').text('Displaying ' + Math.min(total, offset + 1) + ' to ' + Math.min(total, offset + perPage) + ' of ' + total));
      }
    }));
</c:if>
