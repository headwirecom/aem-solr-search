/*
 *  Copyright 2014 Adobe Systems Incorporated
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.headwire.aemsolrsearch.geometrixx.listeners;

import com.day.cq.wcm.api.PageEvent;
import com.day.cq.wcm.api.PageModification;
import com.headwire.aemsolrsearch.geometrixx.model.GeometrixxContentType;
import com.headwire.aemsolrsearch.geometrixx.model.GeometrixxPage;
import com.headwire.aemsolrsearch.search.services.DefaultSolrSearchService;
import com.headwire.aemsolrsearch.services.SolrConfigurationService;
import org.apache.felix.scr.annotations.*;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.solr.client.solrj.SolrClient;
import org.osgi.framework.Constants;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

//import com.day.cq.dam.api.DamEvent;
//import com.day.cq.replication.ReplicationAction;

/**
 * A service to demonstrate how changes in the resource tree
 * can be listened for. It registers an event handler service.
 * The component is activated immediately after the bundle is
 * started through the immediate flag.
 * Please note, that apart from EventHandler services,
 * the immediate flag should not be set on a service.
 */
@Component(immediate = true, metatype = true)
@Service(value = EventHandler.class)
@Properties({
    @Property(name = Constants.SERVICE_VENDOR, value = "headwire.com, Inc."),
    @Property(name = Constants.SERVICE_DESCRIPTION, value = "Provides a service listening changes in pages to update solr each change"),
	@Property(name = EventConstants.EVENT_TOPIC, value = {PageEvent.EVENT_TOPIC}),
    @Property(name = "listener.disabled", boolValue = true),
    @Property(name = "solr.core", value = "collection1"),
    @Property(name = "listener.paths", value = {""}, cardinality = Integer.MAX_VALUE)
})
@SuppressWarnings("PMD.LoggerIsNotStaticFinal")
public class SolrGeometrixxPageListener extends DefaultSolrSearchService implements EventHandler {
    private static final Logger LOG = LoggerFactory.getLogger(SolrGeometrixxPageListener.class);
	@Reference
	private ResourceResolverFactory resolverFactory;

	@Reference
	private SolrConfigurationService solrConfigurationService;

    public void handleEvent(final Event event) {
		if (disabled) return;

		SolrClient solr = getSolrIndexClient();

		PageEvent pageEvent = PageEvent.fromEvent(event);
		if (pageEvent == null) return;
		
		ResourceResolver resourceResolver = null;
		try {
			resourceResolver = resolverFactory.getAdministrativeResourceResolver(null);
			for (Iterator<PageModification> iter = pageEvent.getModifications(); iter.hasNext(); )
				handlePageModification(iter.next(), solr, resourceResolver);
		} catch (Exception e) {
			LOG.error("Could not get ResourceResolver instance or handle page modification", e);
			return;
		} finally {
			if (resourceResolver != null && resourceResolver.isLive())
				resourceResolver.close();
		}
    }
	
	protected void handlePageModification(PageModification mod, SolrClient solr, ResourceResolver resourceResolver) {
		String pagePath = mod.getPath();
		boolean isAllowedPath = false;
		for (String basePath : basePaths)
			isAllowedPath |= pagePath.startsWith(basePath);
		if (!isAllowedPath) {
			LOG.debug("Page event not on one of the base paths. Ignoring event.");
			return;
		}
		Resource pageRes = resourceResolver.getResource(pagePath);
		
		LOG.info("Handling valid page modification " + mod);
		switch (mod.getType()) {
			case CREATED:
			case MODIFIED:
			case RESTORED:
				addOrUpdatePage(pageRes, solr);
				break;
			case DELETED:
				removePage(pagePath, solr);
				break;
			case MOVED:
				removePage(pagePath, solr);
				pageRes = resourceResolver.getResource(mod.getDestination());
				addOrUpdatePage(pageRes, solr);
				break;
			//need version created to help with deletion on children, since only the parent receives a deletion event. however,
			//as the parent resource no longer exists to iterate through, the only way to remove the children is to assume if a
			//version is created but the resource doesn't exist, this must be either the deletion of a parent or a the source
			//of a move (same result either way)
			//hmm, still doesn't work (usually. it's asynchronous so sometimes it is deleted, sometimes it isn't)
			//TODO known bug when deleting a page with child pages, child pages not removed from solr
/*			case VERSION_CREATED:
				if (pageRes == null)
					removePage(pagePath, solr);
				break;*/
		}
	}
	
	protected void addOrUpdatePage(Resource pageRes, SolrClient solr) {
		if (pageRes == null) {
			LOG.error("Page does not exist to add/update in solr");
			return;
		}
    GeometrixxContentType dataPage = pageRes.adaptTo(GeometrixxPage.class);
		try {
			LOG.info("Adding/updating page " + pageRes.getPath());
			solr.add(core, dataPage.getSolrDoc());
			solr.commit(core);
		} catch (Exception e) {
			LOG.error("Failure to add/update page " + pageRes.getPath(), e);
		}
	}
	
	protected void removePage(String id, SolrClient solr) {
		try {
			LOG.info("Removing page " + id);
			solr.deleteById(core, id);
			solr.commit(core);
		} catch (Exception e) {
			LOG.error("Failure to remove page " + id, e);
		}
	}

	//having to rename activate/modified because parent class incorrectly uses Map<String, String>, despite other types existing such as boolean
	@Activate
	protected void activate2(Map<String, Object> params) {
		super.activate(getMapAsStringValues(params));
		setup(params);
	}

	@Modified
	protected void modified2(Map<String, Object> params) {
		super.modified(getMapAsStringValues(params));
		setup(params);
	}

	protected boolean disabled = false;
	protected String core = "collection1";
	protected String[] basePaths = new String[0];
	protected void setup(Map<String, Object> params) {
		if (params.get("listener.disabled") instanceof Boolean)
			disabled = (Boolean) params.get("listener.disabled");
		else
			disabled = params.get("listener.disabled").toString().equals("true");
		core = params.get("solr.core").toString();
		
		Object tempPaths = params.get("listener.paths");
		if (tempPaths instanceof String[])
			basePaths = (String[]) tempPaths;
		else
			basePaths = new String[] {tempPaths.toString()};
		
		LOG.debug("Service disabled? " + disabled);
		LOG.debug("Using core: " + core);
		LOG.debug("Base paths:");
		for (String path : basePaths)
			LOG.debug("  " + path);
	}
	
	private Map<String, String> getMapAsStringValues(Map<String, Object> original) {
		Map<String, String> retVal = new HashMap<String, String>();
		for (Map.Entry<String, Object> entry : original.entrySet())
			retVal.put(entry.getKey(), entry.getValue().toString());
		return retVal;
	}
}

