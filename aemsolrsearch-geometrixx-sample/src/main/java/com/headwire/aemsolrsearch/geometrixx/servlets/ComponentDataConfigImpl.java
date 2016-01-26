package com.headwire.aemsolrsearch.geometrixx.servlets;

import com.headwire.aemsolrsearch.geometrixx.config.ComponentDataConfig;
import org.apache.felix.scr.annotations.*;
import org.apache.felix.scr.annotations.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by headwire on 6/23/2015.
 */


@Component(
		configurationFactory = true,
		policy = ConfigurationPolicy.REQUIRE,
		metatype = true,
		immediate = true)
@Service()
@Properties({
		@Property(
				name = ComponentDataConfig.COMPONENT_PATHS,
				label = "Component Paths",
				description = "Paths to the components that this configuration is bound to.",
				cardinality = Integer.MAX_VALUE,
				value = {""}
		),
		@Property(
				name = ComponentDataConfig.COMPONENT_FIELDS,
				label = "Fields",
				description = "Mapping of solr schema fields to one or more JCR Properties, in the format solrFieldName=jcr:field1,jcr:field2",
				cardinality = Integer.MAX_VALUE,
				value = {""}
		)
})
public class ComponentDataConfigImpl implements ComponentDataConfig{
	private static final Logger LOG = LoggerFactory.getLogger(ComponentDataConfigImpl.class);

	private String[] componentPaths;
	private Map<String, Collection<String>> fields;

	@Activate
	protected void activate(final Map<String, Object> config) throws Exception {
		resetService(config);
	}

	@Modified
	protected void modified(final Map<String, Object> config) {
		resetService(config);
	}

	private synchronized void resetService(final Map<String, Object> config) {
		componentPaths = config.containsKey(COMPONENT_PATHS) ? (String[])config.get(COMPONENT_PATHS) : new String[0];
		String[] fieldConfigs = config.containsKey(COMPONENT_FIELDS) ? (String[])config.get(COMPONENT_FIELDS) : new String[0];
		fields = createFieldMap(fieldConfigs);
	}

	private Map<String, Collection<String>> createFieldMap(String[] fieldConfigs)
	{
		Map<String, Collection<String>> fields = new HashMap<String, Collection<String>>();

		if(fieldConfigs == null || fieldConfigs.length <= 0)
		{
			LOG.warn("No field config found for component configuration for {}; this config won't be able to do anything!", componentPaths);
			return fields;
		}

		for(String fieldConfig : fieldConfigs)
		{
			String[] splitConfig = fieldConfig.split("=");
			if(splitConfig.length != 2)
			{
				LOG.error("Configuration {} is invalid", fieldConfig);
				continue;
			}

			String solrField = splitConfig[0].trim();
			String jcrFieldsString = splitConfig[1].trim();

			String[] jcrFields = jcrFieldsString.split(",");

			Collection<String> jcrFieldSet = new HashSet<String>();
			for(String jcrField : jcrFields)
			{
				String trimmedField = jcrField.trim();
				if(!trimmedField.isEmpty())
				{
					jcrFieldSet.add(trimmedField);
				}
			}

			if(jcrFieldSet.isEmpty())
			{
				LOG.warn("Field set for {} is empty", solrField);
				continue;
			}

			fields.put(solrField, jcrFieldSet);
		}

		return fields;
	}

	@Override
	public String[] getComponentPaths()
	{
		return componentPaths;
	}

	@Override
	public Map<String, Collection<String>> getFields()
	{
		return fields;
	}

	@Override
	public String toString()
	{
		final StringBuffer sb = new StringBuffer("ComponentDataConfigImpl{");
		sb.append("componentPaths='").append(componentPaths).append('\'');
		sb.append(", fields=").append(fields);
		sb.append('}');
		return sb.toString();
	}
}
