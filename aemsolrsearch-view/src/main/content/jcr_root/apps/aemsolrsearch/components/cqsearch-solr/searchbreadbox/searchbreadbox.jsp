<%@ include file="/apps/aemsolrsearch/components/global.jspx" %>
<%@ taglib prefix="cqsearch" uri="http://aemsolrsearch.headwire.com/taglibs/aemsolrsearch-taglib" %>
<c:set var="results" value="${solrSearchResults}"/>
<c:set var="solrQuery" value="${solrSearchResults.solrQuery}"/>
<c:if test="${breadboxEnabled}">
	<script type="text/javascript">
	if (cqsearch === undefined) {
		  var cqsearch = {};	
	}
	
	cqsearch.removeAllSolrQueries = function() {
	    $("#solr\\.query").val("*:*");
	    $("#solr\\.fq").empty();
	    $("#solr\\.form").submit();
	};

	cqsearch.removeSolrQuery = function () {
	    $("#solr\\.query").val("*:*");
	    $("#solr\\.form").submit();
	};
	
	cqsearch.removeSolrFq = function (value) {
	    var inputElement = $( "#solr\\.fq > input[value='" + value + "']" );
	    if (inputElement) {
	        $(inputElement).remove();        
	        $("#solr\\.form").submit();
	    }
	};
	</script>
	<ul id="currentsearchselection" class="breadcrumb">
	    <c:if test="${not empty solrQuery}">
	        <c:set var="numberOfVisibleRemoveLinks" value="0"/>
	        <c:if test="${solrQuery.query ne '*:*'}">
	            <c:set var="numberOfVisibleRemoveLinks" value="${numberOfVisibleRemoveLinks + 1}"/>
	            <li><a href="javascript:void(0);" onclick="cqsearch.removeSolrQuery();" class="badge">${solrQuery.query}</a></li>
	        </c:if>
	        <c:forEach var="filterQuery" items="${solrQuery.filterQueries}"> 
	            <c:if test="${not empty filterQuery}">
	                <c:choose>
	                    <c:when test="${advancedFilterQueriesEnabled and advancedFilterQueriesInBreadboxEnabled}">
	                        <c:set var="numberOfVisibleRemoveLinks" value="${numberOfVisibleRemoveLinks + 1}"/>
	                        <li><a href="javascript:void(0);" onclick="cqsearch.removeSolrFq('${filterQuery}');">[x]&nbsp;${fn:substringAfter(filterQuery, ':')}</a></li>
	                    </c:when>
	                    <c:when test="${advancedFilterQueriesEnabled and not advancedFilterQueriesInBreadboxEnabled and not cqsearch:contains(advancedFilterQueries, filterQuery)}">
	                        <c:set var="numberOfVisibleRemoveLinks" value="${numberOfVisibleRemoveLinks + 1}"/>
	                        <li><a href="javascript:void(0);" onclick="cqsearch.removeSolrFq('${filterQuery}');" class="badge">${fn:substringAfter(filterQuery, ':')}</a></li>
	                    </c:when>
	                    <c:when test="${not advancedFilterQueriesEnabled}">
	                        <c:set var="numberOfVisibleRemoveLinks" value="${numberOfVisibleRemoveLinks + 1}"/>
	                        <li><a href="javascript:void(0);" onclick="cqsearch.removeSolrFq('${filterQuery}');" class="badge">${fn:substringAfter(filterQuery, ':')}</a></li>
	                    </c:when>
	                    <c:otherwise>
	                    </c:otherwise>
	                </c:choose>
	            </c:if>
	        </c:forEach>
	        <c:if test="${numberOfVisibleRemoveLinks ge 2}">
	            <%-- If there are more than two li to remove, show the remove all. --%>
	            <script type="text/javascript">
	                $('<li><a href="javascript:void(0);" onclick="cqsearch.removeAllSolrQueries();">Remove all</a></li>').prependTo('#currentsearchselection');
	            </script>
	        </c:if>
	    </c:if>
	</ul>
</c:if>
