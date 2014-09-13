<%@ include file="/apps/aemsolrsearch/components/global.jspx" %>

<body>

    <cq:include path="search-controller" resourceType="foundation/components/parsys"/>

    <div class="container">
      <div class="row">
        <div class="page-header">
          <h1>AEM Search <small>powered by <a href="http://www.headwire.com/">headwire.com, Inc.</a></small></h1>
       </div>
      </div>
      <div class="row">
        <div class="col-md-3">
            <cq:include path="par-left-main" resourceType="foundation/components/parsys"/>
        </div>
        <div class="col-md-7">
          <div class="search-spotlight">
            <cq:include path="par-main" resourceType="foundation/components/parsys"/>
          </div>
        </div>

        <div class="col-md-3">
          <cq:include path="par-left-rail" resourceType="foundation/components/parsys"/>
        </div>
        <div class="col-md-7">
          <cq:include path="par-center-well" resourceType="foundation/components/parsys"/>
       </div>
      </div>

      <hr>

      <footer>
        <p>&copy; headwire.com, Inc. 2014</p>
      </footer>
    </div> <!-- /container -->

</body>
