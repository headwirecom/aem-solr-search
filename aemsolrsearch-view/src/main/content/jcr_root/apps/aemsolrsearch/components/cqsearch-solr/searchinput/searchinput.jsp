<%@ include file="/apps/aemsolrsearch/components/global.jspx" %>
<c:set var="solrQuery" value="${solrSearchResults.solrQuery}"/>
<c:if test="${inputEnabled}">
	<script type="text/javascript">
    if (cqsearch === undefined) {
        var cqsearch = {};    
    }

    cqsearch.setSolrQuery = function(queryString) {
	    $("#solr\\.query").val(queryString);
	    $("#solr\\.form").submit();
	};
	</script>
	<form action="javascript:cqsearch.setSolrQuery($('#solr\\.queryInput').val());">
	    <div id="search" class="input-group">
	        <span class="input-group-addon" style="cursor:pointer;" onclick="$(this).closest('form').submit();">Go!</span>
	        <input class="form-control" type="text" id="solr.queryInput" name="q" value="${(solrQuery.query eq '*:*') ? '' : solrQuery.query}" size="48">
	    </div>
	</form>
</c:if>
