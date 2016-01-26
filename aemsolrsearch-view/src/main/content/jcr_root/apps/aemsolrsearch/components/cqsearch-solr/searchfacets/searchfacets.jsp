<%@ include file="/apps/aemsolrsearch/components/global.jspx" %>
<%@ taglib prefix="cqsearch" uri="http://aemsolrsearch.headwire.com/taglibs/aemsolrsearch-taglib" %>
<%@ page import="org.apache.solr.client.solrj.response.QueryResponse,org.apache.solr.client.solrj.response.FacetField,com.headwire.aemsolrsearch.taglib.Facet" %>
<c:set var="results" value="${solrSearchResults}"/>
<c:set var="solrQuery" value="${solrSearchResults.solrQuery}"/>
<c:if test="${facetsEnabled}">    
	<script type="text/javascript">
    if (cqsearch === undefined) {
          var cqsearch = {};    
    }

    cqsearch.addSolrFq = function (fq) {
	    if (null == fq) return;
	    $("<input type='hidden'/>")
	      .attr("name", "fq")
	      .val(fq)
	      .appendTo("#solr\\.fq");
	
	    $("#solr\\.form").submit();
	};
	
    cqsearch.showMoreFacets = function (target) {
	    if ($(target).text().trim() == '<c:out value="${facetsShowMore}" />') {
			$(target).closest('ul').children(':hidden').each(
				function() {
					$(this).addClass('hideme').show();
				}
			); 
	        $(target).text('<c:out value="${facetsShowLess}" />');
	    } else {
	    	$(target).closest('ul').children('.hideme').each(
	   			function() {
	   				$(this).removeClass('hideme').hide();
	   			}
	    	); 
	    	$(target).text('<c:out value="${facetsShowMore}" />');
		}
	};
	</script>
	<c:forEach var="curFacet" items="${facetsAvailableFacets}">
	    <div id="facet-section-${curFacet.key}" class="facet_outer_container">
	        <span class="facet-heading hidden"><c:out value="${curFacet.name}"/></span>
	        <%-- style="visibility: hidden;" --%>
	        <ul id="${curFacet.key}" class="facet_container list-group">
	            <%-- initially hide the facet section. --%>
	            <script type="text/javascript">
	                $('#facet-section-${curFacet.key}').hide();
	            </script>
	            <li class="facet_item list-group-item active"><c:out value="${curFacet.name}"/></li>
	            <c:if test="${not empty results.queryResponse}">
	               <c:set var="queryResponse" value="${results.queryResponse}"/>
	                <%
	                    final QueryResponse qr = (QueryResponse) pageContext.getAttribute("queryResponse");
	                    final Facet curFacet = (Facet)pageContext.getAttribute("curFacet");
	                    if (null != curFacet) {
	                        FacetField facetField = qr.getFacetField(curFacet.getKey());
	                        if (null != facetField) {
	                            pageContext.setAttribute("facetField", facetField);
	                        }
	                    }
	                %>
	                <c:if test="${not empty facetField.values}">
	                    <%-- show the facet section if there are facet field values. --%>
	                    <script type="text/javascript">
	                        $('#facet-section-${curFacet.key}').show();
	                    </script>
	                    <c:forEach var="count" items="${facetField.values}"
	                        varStatus="status">
	                        <c:set var="styleDisplay">
	                            <c:if test="${status.count gt facetsShowNumFacets}">style='display: none;'</c:if>
	                        </c:set>
	                        <li ${styleDisplay} class="facet_item list-group-item"><c:set
	                                var="facetValue" value="${curFacet.key}:${count.name}" /> <c:choose>
	                                <c:when test="${cqsearch:contains(solrQuery.filterQueries, facetValue)}">
	                                    <span class="disabled"><c:out value="${count.name}" />&nbsp;(<c:out value="${count.count}" />)</span>
	                                </c:when>
	                                <c:otherwise>
	                                    <a href="javascript:void(0);"
	                                        onclick="cqsearch.addSolrFq('${facetValue}');">
	                                        <c:out value="${count.name}" />&nbsp;(<c:out value="${count.count}" />)
	                                    </a>
	                                </c:otherwise>
	                            </c:choose></li>
	                    </c:forEach>
	                    <c:if test="${not empty styleDisplay}">
	                        <li class="list-group-item list-group-item-info"><a
	                            href="javascript:void(0);"
	                            onclick="cqsearch.showMoreFacets(this);">
	                            <c:out value="${facetsShowMore}" /></a>
	                        </li>
	                    </c:if>
	                </c:if>
	            </c:if>
	        </ul>
	    </div>
	</c:forEach>
</c:if>
