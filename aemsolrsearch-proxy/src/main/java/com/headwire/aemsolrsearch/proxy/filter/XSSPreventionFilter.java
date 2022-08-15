package com.headwire.aemsolrsearch.proxy.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.text.Normalizer;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Filters Http requests and removes malicious characters/strings
 * (i.e. XSS) from the Query String
 */

@Component
public class XSSPreventionFilter implements Filter {

    private static final Logger LOG = LoggerFactory.getLogger(XSSPreventionFilter.class);


    class XSSRequestWrapper extends HttpServletRequestWrapper {

        private Map<String, String[]> sanitizedQueryString;

        public XSSRequestWrapper(HttpServletRequest request) {
            super(request);
        }

        //QueryString overrides

        @Override public String getParameter(String name) {
            String parameter = null;
            String[] vals = getParameterMap().get(name);
            if (vals != null && vals.length > 0) {
                parameter = vals[0];
            }

            return parameter;
        }

        @Override public String[] getParameterValues(String name) {
            return getParameterMap().get(name);
        }

        @Override public Enumeration<String> getParameterNames() {
            return Collections.enumeration(getParameterMap().keySet());
        }

        @SuppressWarnings("unchecked")
        @Override public Map<String, String[]> getParameterMap() {
            if (sanitizedQueryString == null) {
                Map<String, String[]> res = new HashMap<String, String[]>();
                Map<String, String[]> originalQueryString = super.getParameterMap();
                if (originalQueryString != null) {
                    for (String key : (Set<String>) originalQueryString.keySet()) {
                        String[] rawVals = originalQueryString.get(key);
                        String[] snzVals = new String[rawVals.length];
                        for (int i = 0; i < rawVals.length; i++) {
                            snzVals[i] = stripXSS(rawVals[i]);
                            LOG.debug("Sanitized: '{}', to '{}'", rawVals[i], snzVals[i]);
                        }
                        res.put(stripXSS(key), snzVals);
                    }
                }
                sanitizedQueryString = res;
            }
            return sanitizedQueryString;
        }

        /**
         * Removes all the potentially malicious characters from a string
         *
         * @param value the raw string
         * @return the sanitized string
         */
        private String stripXSS(String value) {
            String cleanValue = null;
            if (value != null) {
                cleanValue = Normalizer.normalize(value, Normalizer.Form.NFD);

                // Avoid null characters
                cleanValue = cleanValue.replaceAll("\0", "");

                // Avoid anything between script tags
                Pattern scriptPattern =
                    Pattern.compile("<script>(.*?)</script>", Pattern.CASE_INSENSITIVE);
                cleanValue = scriptPattern.matcher(cleanValue).replaceAll("");

                // Avoid anything in a src='...' type of expression
                scriptPattern = Pattern.compile("src[\r\n]*=[\r\n]*\\\'(.*?)\\\'",
                    Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
                cleanValue = scriptPattern.matcher(cleanValue).replaceAll("");

                scriptPattern = Pattern.compile("src[\r\n]*=[\r\n]*\\\"(.*?)\\\"",
                    Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
                cleanValue = scriptPattern.matcher(cleanValue).replaceAll("");

                // Remove any lonesome </script> tag
                scriptPattern = Pattern.compile("</script>", Pattern.CASE_INSENSITIVE);
                cleanValue = scriptPattern.matcher(cleanValue).replaceAll("");

                // Remove any lonesome <script ...> tag
                scriptPattern = Pattern.compile("<script(.*?)>",
                    Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
                cleanValue = scriptPattern.matcher(cleanValue).replaceAll("");

                // Avoid eval(...) expressions
                scriptPattern = Pattern.compile("eval\\((.*?)\\)",
                    Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
                cleanValue = scriptPattern.matcher(cleanValue).replaceAll("");

                // Avoid expression(...) expressions
                scriptPattern = Pattern.compile("expression\\((.*?)\\)",
                    Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
                cleanValue = scriptPattern.matcher(cleanValue).replaceAll("");

                // Avoid javascript:... expressions
                scriptPattern = Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE);
                cleanValue = scriptPattern.matcher(cleanValue).replaceAll("");

                // Avoid vbscript:... expressions
                scriptPattern = Pattern.compile("vbscript:", Pattern.CASE_INSENSITIVE);
                cleanValue = scriptPattern.matcher(cleanValue).replaceAll("");

                // Avoid onload= expressions
                scriptPattern = Pattern.compile("onload(.*?)=",
                    Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
                cleanValue = scriptPattern.matcher(cleanValue).replaceAll("");
            }
            return cleanValue;
        }
    }

    @Override public void destroy() {
        LOG.info("XSSPreventionFilter: destroy()");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {
        XSSRequestWrapper wrapper = new XSSRequestWrapper((HttpServletRequest) request);
        chain.doFilter(wrapper, response);
    }

    @Override public void init(FilterConfig filterConfig) throws ServletException {
        LOG.info("XSSPreventionFilter: init()");
    }
}
