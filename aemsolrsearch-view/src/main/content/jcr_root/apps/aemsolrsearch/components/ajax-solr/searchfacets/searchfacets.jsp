<%@ include file="/apps/aemsolrsearch/components/global.jspx" %>
<%@ taglib prefix="cqsearch" uri="http://aemsolrsearch.headwire.com/taglibs/aemsolrsearch-taglib" %>

<c:if test="${facetsEnabled}">
    <c:forEach  var="curFacet" items="${facetsAvailableFacets}">
        <div class="facet_outer_container">
            <h2 class="facet-heading">${curFacet.name}</h2>
            <ul id="${curFacet.key}" class="facet_container"></ul>
        </div>
    </c:forEach>
</c:if>
