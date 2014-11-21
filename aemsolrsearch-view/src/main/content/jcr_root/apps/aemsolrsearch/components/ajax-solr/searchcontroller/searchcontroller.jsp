<%@ include file="/apps/aemsolrsearch/components/global.jspx" %>
<%@ taglib prefix="cqsearch" uri="http://aemsolrsearch.headwire.com/taglibs/aemsolrsearch-taglib" %>

<cqsearch:solrController />
<cqsearch:wcmMode />

<c:choose>
    <c:when test="${solrConfigured}">

        <script type="text/javascript">
        var Manager;

        (function ($) {

          $(function () {
            Manager = new AjaxSolr.Manager({
              <c:choose>
                <c:when test="${solrProxyEnabled}">
                  proxyUrl: '${solrProxyUrl}'
                </c:when>
                <c:otherwise>
                  solrUrl: '${solrEndPoint}'
                </c:otherwise>
              </c:choose>

            });

            <cq:include script="widgets/searchresults_widget.jsp" />
            <cq:include script="widgets/searchstatistics_widget.jsp" />
            <cq:include script="widgets/searchpagination_widget.jsp" />
            <cq:include script="widgets/searchfacets_widget.jsp" />
            <cq:include script="widgets/searchbreadbox_widget.jsp" />
            <cq:include script="widgets/searchinput_widget.jsp" />
            <cq:include script="widgets/registerwidget_hook.jsp" />
            Manager.setStore(new AjaxSolr.ParameterHistoryStore());
            Manager.store.exposed = [ 'q', 'fq', 'start' ];
            Manager.init();

            <%-- Pass the Solr core name as a request parameter to the proxy. --%>
            <c:if test="${solrProxyEnabled}">
              Manager.store.addByValue('corename', '${solrCore}');
            </c:if>

            <c:choose>
                <c:when test="${empty param.q}">
                     Manager.store.addByValue('q', '*:*');
                </c:when>
                <c:otherwise>
                    console.log('${param.q}');
                    Manager.store.addByValue('q', '${param.q}');
                </c:otherwise>
            </c:choose>

            var params = {
              <c:if test="${facetsEnabled}">
              'facet': true,
              'facet.field': <cqsearch:json attribute="facetsAvailableFacetKeys"/>,
              'facet.sort': '${facetsFacetSort}',
              'facet.limit': 20,
              'facet.mincount': 1,
              </c:if>

              <%-- Build filter query request parameter list. --%>
              <c:if test="${advancedFilterQueriesEnabled}">
                <c:forEach  var="filterQuery" items="${advancedFilterQueries}">
                    'fq': '${filterQuery}',
                </c:forEach>
              </c:if>

              <c:if test="${not empty searchHandler}">
              'qt': '${searchHandler}',
              </c:if>

              'fl': '${fn:join(resultsAvailableFields, ',')}',
              <c:if test="${highlightingEnabled}">
              'hl': true,
              'hl.fl': '${fn:join(highlightingFields, ',')}',
              'hl.simple.pre': '${highlightingPre}',
              'hl.simple.post': '${highlightingPost}',
              'hl.snippets': '${highlightingSnippets}',
              'hl.fragsize': '${highlightingFragSize}',
              </c:if>
              'rows': ${resultsPerPage},
              'json.nl': 'map'
            };
            for (var name in params) {
              Manager.store.addByValue(name, params[name]);
            }
                Manager.doRequest();
              });

            })(jQuery);
        </script>

        <c:choose>
          <c:when test="${wcmAuthor}">
            <cq:includeClientLib js="ajax-solr,ajax-solr-dialog" />
            <cq:includeClientLib css="ajax-solr,ajax-solr-dialog" />
          </c:when>
          <c:otherwise>
            <cq:includeClientLib js="ajax-solr" />
            <cq:includeClientLib css="ajax-solr" />
          </c:otherwise>
        </c:choose>
    </c:when>
    <c:otherwise>
        Please configure the Search Controller.
    </c:otherwise>
</c:choose>
