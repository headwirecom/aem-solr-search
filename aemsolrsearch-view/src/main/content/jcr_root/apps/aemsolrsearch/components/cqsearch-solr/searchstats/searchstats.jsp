<%@ include file="/apps/aemsolrsearch/components/global.jspx" %>
<%@ taglib prefix="cqsearch" uri="http://aemsolrsearch.headwire.com/taglibs/aemsolrsearch-taglib" %>
<c:if test="${paginationEnabled}">
    <c:set var="results" value="${solrSearchResults}"/>
	<c:set var="total" value="${results.solrDocumentListNumFound}"/>
	<c:set var="qTime" value="${results.responseHeaderMap['QTime']}"/>
	<c:set var="resultIndexStart" value="${cqsearch:min(total, results.solrDocumentListStart + 1)}"/>
	<c:set var="resultIndexEnd" value="${cqsearch:min(total, results.solrDocumentListStart + results.solrDocumentListSize)}"/>
	<div id="search-statistics" class="alert alert-success">
	    <span>Found ${total} results in ${qTime} seconds. Displaying results ${resultIndexStart} to ${resultIndexEnd}.</span>
	</div>
</c:if>
