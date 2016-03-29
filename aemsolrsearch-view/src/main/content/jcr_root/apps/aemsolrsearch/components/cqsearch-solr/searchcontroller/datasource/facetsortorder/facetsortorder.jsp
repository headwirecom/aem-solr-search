<%@include file="/libs/granite/ui/global.jsp"%>

<%@page session="false" import="
                  com.adobe.granite.ui.components.ds.DataSource,
                  com.adobe.granite.ui.components.ds.EmptyDataSource,
                  com.adobe.granite.ui.components.ds.SimpleDataSource,
                  com.adobe.granite.ui.components.ds.ValueMapResource,
                  com.headwire.aemsolrsearch.services.SolrConfigurationService,
                  org.apache.commons.collections.Transformer,
                  org.apache.commons.collections.iterators.TransformIterator,
                  org.apache.sling.api.resource.*,
				  org.apache.sling.api.wrappers.ValueMapDecorator,
				  java.util.Arrays,
                  java.util.HashMap,
                  java.util.List"%>

<cq:defineObjects/><%

    // set fallback
    request.setAttribute(DataSource.class.getName(), EmptyDataSource.instance());

    List<String> facetSortOrders = Arrays.asList("Count", "Index");

    final ResourceResolver resolver = resourceResolver;

    DataSource ds = new SimpleDataSource(new TransformIterator(facetSortOrders.iterator(), new Transformer() {
        public Object transform(Object o) {
            String facetSortOrder = (String) o;
            ValueMap vm = new ValueMapDecorator(new HashMap<String, Object>());

            vm.put("value", facetSortOrder.toLowerCase());
            vm.put("text", facetSortOrder);

            return new ValueMapResource(resolver, new ResourceMetadata(), "nt:unstructured", vm);
        }
    }));

    request.setAttribute(DataSource.class.getName(), ds);
%>