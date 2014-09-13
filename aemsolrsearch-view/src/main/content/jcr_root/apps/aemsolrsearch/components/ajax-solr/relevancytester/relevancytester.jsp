<%@ include file="/apps/aemsolrsearch/components/global.jspx" %>
<%@ taglib prefix="cqsearch" uri="http://aemsolrsearch.headwire.com/taglibs/aemsolrsearch-taglib" %>

<%
    String searchPageA = (String) properties.get("search-page-a");
    String searchPageB = (String) properties.get("search-page-b");
    pageContext.setAttribute("searchPageA", searchPageA);
    pageContext.setAttribute("searchPageB", searchPageB);
%>

<div class="container">
    <form class="relevancy-search" action="" method="get">
        <div id="search"  class="input-group">
            <span class="input-group-addon">Go!</span>
            <input type="text" id="query" name="q" autocomplete="off" class="form-control" placeholder="${param.q}">
        </div>
    </form>
</div>

<div class="container">
    <div class="row">
        <div class="col-md-6">
            <h2>Search A</h2>
                <iframe id="searchIFrame1" src="${searchPageA}.html?wcmmode=disabled&q=${param.q}" width="550" height="1000" frameborder="0"></iframe>
        </div>
        <div class="col-md-6">
            <h2>Search B</h2>
            <iframe id="searchIFrame2" src="${searchPageB}.html?wcmmode=disabled&q=${param.q}" width="550" height="1000" frameborder="0"></iframe>
        </div>
    </div>
</div>
