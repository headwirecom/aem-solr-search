package com.headwire.aemsolrsearch.geometrixx.util;

import com.day.cq.tagging.Tag;
import com.day.cq.wcm.api.Page;
import com.headwire.aemsolrsearch.geometrixx.config.ComponentDataConfig;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.jsoup.Jsoup;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by headwire on 6/24/2015.
 */
public class SolrIndexingUtil{
	private static final Logger LOG = LoggerFactory.getLogger(SolrIndexingUtil.class);

	public static Map<String, String> extractDataFromPage(Page page, Map<String, ComponentDataConfig> componentDataConfigMap)
	{
		Map<String, String> extractedData = new LinkedHashMap<String, String>();
		Resource contentResource = page.getContentResource();

		dataExtractionHelper(contentResource, componentDataConfigMap, extractedData);

		return extractedData;
	}

	private static void dataExtractionHelper(Resource resource, Map<String, ComponentDataConfig> componentDataConfigMap, Map<String, String> extractedData)
	{
		if(resource == null)
		{
			return;
		}

		String slingResourceType = resource.getResourceType();
		ComponentDataConfig config = componentDataConfigMap.get(slingResourceType);

		extractDataFromResource(resource, extractedData, config);

		Iterable<Resource> children = resource.getChildren();
		if(children != null)
		{
			for(Resource child : resource.getChildren())
			{
				dataExtractionHelper(child, componentDataConfigMap, extractedData);
			}
		}
	}

	private static void extractDataFromResource(Resource resource, Map<String, String> extractedData, ComponentDataConfig config)
	{
		if(config != null)
		{
			Map<String, Collection<String>> fieldMap = config.getFields();
			ValueMap componentProperties = resource.adaptTo(ValueMap.class);
			for(Map.Entry<String, Collection<String>> entry : fieldMap.entrySet())
			{
				String solrFieldName = entry.getKey();
				Collection<String> jcrFields = entry.getValue();

				if(!extractedData.containsKey(solrFieldName))
				{
					extractedData.put(solrFieldName, "");
				}

				for (String jcrFieldName : jcrFields) {
					String jcrValue = componentProperties.get(jcrFieldName, "");
					jcrValue = htmlToText(jcrValue);
					if (!jcrValue.isEmpty()) {
						String newString = extractedData.get(solrFieldName) + " " + jcrValue;
						extractedData.put(solrFieldName, newString);
					}
				}
			}
		}
	}

	public static Map<String, String> extractDataFromParsys(Resource resource, String parsysName, Map<String, ComponentDataConfig> componentDataConfigMap)
	{
		Map<String, String> parsysData = new LinkedHashMap<String, String>();
		Resource parsysResource = resource.getChild(parsysName);
		if(parsysResource == null)
		{
			LOG.info("No parsys resource found for page {}", resource.getPath());
			return parsysData;
		}

		for(Resource componentResource : parsysResource.getChildren())
		{
			String slingResourceType = componentResource.getResourceType();
			ComponentDataConfig config = componentDataConfigMap.get(slingResourceType);

			extractDataFromResource(componentResource, parsysData, config);
		}

		return parsysData;
	}

	//should cache? probably not needed, as is not going to be called very often on publish and this way there's
	//no need to wait out the cache after a configuration change
	public static Map<String, ComponentDataConfig> getComponentDataConfigs()
	{
		Map<String, ComponentDataConfig> componentDataConfigMap = new HashMap<String, ComponentDataConfig>();

		ServiceReference[] configReferences;
		BundleContext bundleContext = FrameworkUtil.getBundle(SolrIndexingUtil.class).getBundleContext();
		try
		{
			configReferences = bundleContext.getServiceReferences(ComponentDataConfig.class.getName(), null);
		} catch(InvalidSyntaxException e)
		{
			configReferences = new ServiceReference[0];
			LOG.error("This is literally impossible", e);
		}

		if(configReferences == null)
		{
			LOG.info("There are no component data configuration methods.");
			return componentDataConfigMap;
		}

		for(ServiceReference configReference : configReferences)
		{
			ComponentDataConfig config = (ComponentDataConfig)bundleContext.getService(configReference);
			String[] componentPaths = config.getComponentPaths();
			for (String componentPath : componentPaths)
				componentDataConfigMap.put(componentPath, config);
			LOG.debug("Config reference: {}", config);
		}

		return componentDataConfigMap;
	}

	public static String htmlToText(String html) {

		if (StringUtils.isBlank(html)) {
			return "";
		}

		return Jsoup.parse(html).text();
	}

	public static Collection<String> getTagIdsFromTags(Tag[] tags)
	{
		Collection<String> tagIds = new ArrayList<String>();
		if(tags == null)
		{
			return tagIds;
		}
		for(Tag tag : tags)
		{
			tagIds.add(tag.getTagID());
		}
		return tagIds;
	}

	public static String convertToUtc(Date date) {
		if(date == null) return null;

        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:00.00'Z'");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        return formatter.format(date);
    }

    public static String convertToUtcAndUseNowIfNull(Date date) {

        return date != null ? convertToUtc(date) : convertToUtc(new Date());
    }


}
