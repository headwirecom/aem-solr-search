package com.headwire.aemsolrsearch.geometrixx.config;

import java.util.Collection;
import java.util.Map;

/**
 * Created by headwire on 6/23/2015.
 */
public interface ComponentDataConfig{

	public static final String COMPONENT_PATHS = "component.paths";
	public static final String COMPONENT_FIELDS = "component.fields";

	public String[] getComponentPaths();

	public Map<String, Collection<String>> getFields();

}
