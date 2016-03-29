<%@page contentType="text/html;charset=UTF-8"%>
<%@ include file="/apps/aemsolrsearch/components/global.jspx"%>
<%@ taglib prefix="cqsearch" uri="http://aemsolrsearch.headwire.com/taglibs/aemsolrsearch-taglib"%>

<%-- This will provides the configs from the dialog as request attributes--%>
<cqsearch:solrController />

<%-- This allows to branch login on author mode --%>
<cqsearch:wcmMode />

<%-- This allows to use author ui mode --%>
<cqsearch:authoringUIMode />

<cqsearch:solrSearch
        var="solrSearch"
        varResults="results"
        solrCoreName="${properties['solr-core']}"
        query="${(inputEnabled and not empty param.q) ? param.q : '*:*'}"
        filterQueries="${(facetsEnabled and not empty param.fq)
        ? paramValues.fq
        : (facetsEnabled and advancedFilterQueriesEnabled and advancedFilterQueriesInBreadboxEnabled)
            ? advancedFilterQueries
            : null}"
        advancedFilterQueries="${(advancedFilterQueriesEnabled and not advancedFilterQueriesInBreadboxEnabled) ? advancedFilterQueries : null}"
        start="${(empty param.start) ? 0 : param.start}"
        rows="${resultsPerPage}"
        fieldNames="${resultsAvailableFields}"
        facetEnabled="${facetsEnabled}"
        facetSort="${facetsFacetSort}"
        facetFieldNames="${facetsAvailableFacetKeys}"
        facetLimit="20"
        facetMinCount="1"
        highlightEnabled="${highlightingEnabled}"
        highlightRequireFieldMatchEnabled="false"
        highlightSimplePre="${highlightingPre}"
        highlightSimplePost="${highlightingPost}"
        highlightNumberSnippets="${highlightingSnippets}"
        highlightFragsize="${highlightingFragSize}"
        highlightingFields="${highlightingFields}"
        searchHandler="${searchHandler}"
        />
<%-- Make the results available through the request to the other components. --%>
<c:set var="solrSearchResults" value="${results}" scope="request"/>

<%-- Form thats manipulated by other components to construct query. --%>
<form id="solr.form" method="get" action="${slingRequest.requestURL}">
    <input type="hidden" id="solr.query" name="q" value="${solrSearch.query}">
    <fieldset id="solr.fq">
        <c:if test="${facetsEnabled}">
            <c:forEach var="filterQuery" items="${solrSearch.filterQueries}">
                <c:if test="${not empty filterQuery}">
                    <input type="hidden" name="fq" value="${filterQuery}">
                </c:if>
            </c:forEach>
        </c:if>
    </fieldset>
</form>
<script>
    $("#solr\\.form").submit(function(event) {
        // remove any blank values
        $("#solr\\.form > input[value='']").remove();
    });
</script>

<c:if test="${wcmAuthor && touchMode}">
    Edit the Search Controller.
</c:if>

<c:choose>

    <c:when test="${wcmAuthor && classicMode}">
        <%-- Libs needed for setting the available fields while in author mode. --%>
        <cq:includeClientLib js="ajax-solr,ajax-solr-dialog" />
        <cq:includeClientLib css="ajax-solr,ajax-solr-dialog" />
    </c:when>
</c:choose>
