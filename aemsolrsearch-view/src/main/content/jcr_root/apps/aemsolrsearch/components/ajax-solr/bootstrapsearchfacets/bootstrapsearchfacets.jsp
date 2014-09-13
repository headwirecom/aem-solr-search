<%@ include file="/apps/aemsolrsearch/components/global.jspx" %>
<%@ taglib prefix="cqsearch" uri="http://aemsolrsearch.headwire.com/taglibs/aemsolrsearch-taglib" %>

<c:if test="${facetsEnabled}">
    <c:forEach  var="curFacet" items="${facetsAvailableFacets}">
        <div class="facet_outer_container">
            <span class="facet-heading hidden">${curFacet.name}</span>
            <ul id="${curFacet.key}" class="facet_container list-group">
            </ul>
        </div>
    </c:forEach>
</c:if>
