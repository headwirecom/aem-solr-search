<%@ include file="/apps/aemsolrsearch/components/global.jspx" %>
<%@ taglib prefix="cqsearch" uri="http://aemsolrsearch.headwire.com/taglibs/aemsolrsearch-taglib"%>
<c:set var="results" value="${solrSearchResults}"/>
<c:set var="solrQuery" value="${solrSearchResults.solrQuery}"/>
<c:if test="${paginationEnabled}">
	<div id="navigation">
	    <c:if test="${not empty results.solrDocumentList}">
	    
	        <c:set var="pagerStartIndex"      value="0"/>
	        <c:set var="pagerRemainder"       value="${results.solrDocumentListNumFound mod solrQuery.rows}"/>
	        <c:set var="pagerEndIndex"        value="${cqsearch:floor((results.solrDocumentListNumFound div solrQuery.rows) - (pagerRemainder eq 0 ? 1 : 0))}"/>
	        <c:set var="pagerSelectedIndex"   value="${((empty solrQuery.start) ? 0 : solrQuery.start) / solrQuery.rows }"/>
	        <c:set var="pagerPrev"            value="${solrQuery.start - solrQuery.rows}"/>
	        <c:set var="pagerNext"            value="${solrQuery.start + solrQuery.rows}"/>
	        <c:set var="pagerIsStart"         value="${empty solrQuery.start or solrQuery.start eq 0}"/>
	        <c:set var="pagerIsEnd"           value="${solrQuery.start + solrQuery.rows ge results.solrDocumentListNumFound}"/>        
	        <c:set var="searchRequestUrl"><cq:requestURL><cq:removeParam name='start'/></cq:requestURL></c:set>
	               
	        <ul id="pager" class="pagination">
	            <c:choose>
	                <c:when test="${pagerIsStart}">
	                    <li class="disabled pager-prev"><span class="disabled pager-prev">${paginationPrevLabel}</span></li>
	                </c:when>
	                <c:otherwise>
	                    <c:url var="url" value="${searchRequestUrl}"><c:param name="start" value="${pagerPrev}"/></c:url>
	                    <li><a rel="prev" href="${url}" class="pager-prev">${paginationPrevLabel}</a></li>
	                </c:otherwise>
	            </c:choose>
	            <c:forEach var="pagerCurrentIndex" begin="${pagerStartIndex}" end="${pagerEndIndex}" varStatus="status">
	                <c:set var="pagerCurrentLabel" value="${pagerCurrentIndex + 1}"/>
	                <c:choose>
	                    <c:when test="${pagerCurrentIndex eq pagerSelectedIndex}">
	                        <li class="active"><span class="active"><c:out value="${pagerCurrentLabel}"/></span></li>
	                    </c:when>
                        <%-- 
                        if the number of pages is greater than 9, 
                        show first 2, 
                        one one either side of the selected and
                        the last 2; 
                        Also prevent ellipsing a range of one between the selected and the boundries.
                        Eg. 1 2 ... 4 5 6 ... 8 9 where 'not' showing 3 and 7 is not desired.
                        Magic numbers:
                        2 = the number of pages to show on the start and the end.
                        4 = double the amount to show on either end (double the number just above).
                        9 = the number of pages to start to consider shrinking the pagination with ellipses (first 2 + 1 before selected + selected + 1 after selected + last 2 + 2 ellipses.)
                        --%>
                        <c:when test="${pagerEndIndex le 9 or
                                        pagerCurrentIndex lt 2 or 
                                        pagerCurrentIndex gt pagerEndIndex - 2 or  
                                        pagerCurrentIndex eq pagerSelectedIndex - 1 or
                                        pagerCurrentIndex eq pagerSelectedIndex + 1 or
                                        (pagerCurrentIndex eq 2 and pagerSelectedIndex eq 4) or
                                        (pagerCurrentIndex eq pagerEndIndex - 2 and pagerSelectedIndex eq pagerEndIndex - 4)
                                       }">
                            <c:url var="url" value="${searchRequestUrl}"><c:param name="start" value="${pagerCurrentIndex * solrQuery.rows}"/></c:url>
                            <li><a href="${url}"><c:out value="${pagerCurrentLabel}"/></a></li>
                        </c:when>
                        <%-- show gap between start, selected and end. --%>
                        <c:when test="${pagerCurrentIndex eq pagerSelectedIndex - 2 or pagerCurrentIndex eq pagerSelectedIndex + 2 }">
                            <li class="pager-gap"><span class="pager-gap">&hellip;</span></li>
                        </c:when>
	                    <c:otherwise>
                            <%-- hide any pagination elements between start, selected and end. --%>
	                    </c:otherwise>
	                </c:choose>
	            </c:forEach>
	            <c:choose>
	                <c:when test="${pagerIsEnd}">
	                    <li class="disabled pager-next"><span class="disabled pager-next">${paginationNextLabel}</span></li>
	                </c:when>
	                <c:otherwise>
	                    <c:url var="url" value="${searchRequestUrl}"><c:param name="start" value="${pagerNext}"/></c:url>
	                    <li><a rel="next" href="${url}" class="pager-next">${paginationNextLabel}</a></li>
	                </c:otherwise>
	            </c:choose>
	        </ul>
	     </c:if>
	</div>
	<div style="clear:both;"></div>
</c:if>
