<%@ include file="/apps/aemsolrsearch/components/global.jspx"%>
<%@ taglib prefix="cqsearch"
    uri="http://aemsolrsearch.headwire.com/taglibs/aemsolrsearch-taglib"%>   
<c:set var="results" value="${solrSearchResults}"/>     
<c:if test="${resultsEnabled}">
    <script type="text/javascript">
    if (cqsearch === undefined) {
        var cqsearch = {};    
    }

    cqsearch.showTeaser = function (target) {
        if ($(target).text().trim() == 'more') {        
            $(target).siblings('.showme').show();
            $(target).text('less');
        } else {        
            $(target).siblings('.showme').hide();
            $(target).text('more');
        }
    };
    </script>   
    <div id="searchresults">
        <c:forEach items="${results.solrDocumentList}" var="doc">
            <div class="result-card">
                <c:url var="url" value="${doc.id}" />
                <a href="${url}" class="bootstrap-title"> <c:out
                        value="${doc.title}" />
                </a>
                <div class="bootstrap-url">
                    <c:url value="${doc.url}" />
                </div>
                <div>
                   <c:set var="maxTeaserLength" value="250"/>
                   <c:choose>
                       <c:when test="${not empty results.queryResponse.highlighting[doc.id]['teaser']}">
                          <%-- Warning! Do not substring or escape the following, as there are embedded tags. --%>
                          <%-- Maybe use a span and css the width or overflow with ellipse instead? --%>
                          ${results.queryResponse.highlighting[doc.id]['teaser'][0]}
                       </c:when>
                       <c:when test="${fn:length(doc.teaser) gt maxTeaserLength}">
                            <c:out value="${fn:substring(doc.teaser, 0, maxTeaserLength)}"/><span class="showme" style="display:none"><c:out value="${fn:substring(doc.teaser, maxTeaserLength, -1)}" /></span>
                            <a href="javascript:void(0);" onclick="cqsearch.showTeaser(this);" class="more">more</a>
                       </c:when>
                       <c:otherwise>
                            <c:out value="${doc.teaser}" />
                       </c:otherwise>
                   </c:choose>
                </div>
            </div>
        </c:forEach>
    </div>
</c:if>
