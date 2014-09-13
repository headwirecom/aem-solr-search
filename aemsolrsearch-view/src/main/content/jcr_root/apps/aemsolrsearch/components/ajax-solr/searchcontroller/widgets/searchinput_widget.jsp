<%@ include file="/apps/aemsolrsearch/components/global.jspx" %>
<%@ taglib prefix="cqsearch" uri="http://aemsolrsearch.headwire.com/taglibs/aemsolrsearch-taglib" %>

<%--
  | Note: If you wish to extend this JSP, only include valid JavaScript in the response, as this
  |       JSP is included by searchcontroller.jsp.
  --%>
<c:if test="${inputEnabled}">

    AjaxSolr.TextWidget = AjaxSolr.AbstractTextWidget.extend({
      init: function () {
        var self = this;
        $(this.target).find('input').bind('keydown', function(e) {
          if (e.which == 13) {
            var value = $(this).val();
            if (value && self.set(value)) {
              self.doRequest();
            }
          }
        });
      },

      afterRequest: function () {
        $(this.target).find('input').val('');
      }
    });

    Manager.addWidget(new AjaxSolr.TextWidget({
      id: '${inputId}',
      target: '${inputTarget}'
    }));
</c:if>
