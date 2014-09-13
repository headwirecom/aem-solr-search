/*
 * Author  : Gaston Gonzalez
 * Date    : 8/12/13
 * Version : $Id$
 */
package com.headwire.aemsolrsearch.services.servlets;

import com.headwire.aemsolrsearch.services.ProxyConfigurationService;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.message.HeaderGroup;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.BitSet;
import java.util.Enumeration;
import java.util.Formatter;

@Component(componentAbstract = true)
/**
 * ProxyServlet provides a basic proxy. It is based on David Smiley's HTTP-Proxy-Servlet
 * (https://github.com/dsmiley/HTTP-Proxy-Servlet).
 */
public abstract class ProxyServlet extends SlingSafeMethodsServlet {

    private static final Logger LOG = LoggerFactory.getLogger(ProxyServlet.class);
    protected HttpClient proxyClient;
    protected URI targetUri;

    @Reference
    protected ProxyConfigurationService proxyConfigurationService;

    /**
     * Returns the full URL that will be proxied.
     * @param request
     * @return
     */
    public abstract String getTargetURI(SlingHttpServletRequest request);

    /**
     * A hook for sanitizing the query string before it is sent to the target by the proxy.
     *
     * @param queryString
     * @param request
     * @return The sanitized query string.
     */
    public abstract String sanitizeQueryString(String queryString, HttpServletRequest request);

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException, IOException {

        try {
            targetUri = new URI(getTargetURI(request));
            LOG.debug("Target URI: {}", targetUri);

        } catch (Exception e) {
            LOG.info("Error retrieving Solr endpoint for proxy");
            throw new RuntimeException("Error retrieving Solr endpoint for proxy", e);
        }

        HttpParams hcParams = new BasicHttpParams();
        proxyClient = createHttpClient(hcParams);
        proxyClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT,
                proxyConfigurationService.getHttpConnTimeout());
        proxyClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,
                proxyConfigurationService.getHttpSoTimeout());

        String method = request.getMethod();
        String proxyRequestUri = rewriteUrlFromRequest(request);
        LOG.debug("Proxy URI: {}", proxyRequestUri);
        HttpRequest proxyRequest;

        if (request.getHeader(HttpHeaders.CONTENT_LENGTH) != null ||
                request.getHeader(HttpHeaders.TRANSFER_ENCODING) != null) {
            HttpEntityEnclosingRequest eProxyRequest = new BasicHttpEntityEnclosingRequest(method, proxyRequestUri);
            eProxyRequest.setEntity(new InputStreamEntity(request.getInputStream(), request.getContentLength()));
            proxyRequest = eProxyRequest;
            LOG.info("Proxy request: {}", proxyRequest);
        } else {
            proxyRequest = new BasicHttpRequest(method, proxyRequestUri);
        }

        copyRequestHeaders(request, proxyRequest);

        HttpResponse proxyResponse = proxyClient.execute(URIUtils.extractHost(targetUri), proxyRequest);
        int statusCode = proxyResponse.getStatusLine().getStatusCode();

        if (doResponseRedirectOrNotModifiedLogic(request, response, proxyResponse, statusCode)) {
            //just to be sure, but is probably a no-op
            EntityUtils.consume(proxyResponse.getEntity());
            return;
        }

        // Pass the response code. This method with the "reason phrase" is deprecated but it's the only way to pass the
        //  reason along too.
        //noinspection deprecation
        response.setStatus(statusCode, proxyResponse.getStatusLine().getReasonPhrase());

        copyResponseHeaders(proxyResponse, response);

        // Send the content to the client
        copyResponseEntity(proxyResponse, response);

        proxyClient.getConnectionManager().shutdown();
    }

    protected HttpClient createHttpClient(HttpParams hcParams) {
        return new DefaultHttpClient(new PoolingClientConnectionManager(),hcParams);
    }

    protected boolean doResponseRedirectOrNotModifiedLogic(
            HttpServletRequest servletRequest, HttpServletResponse servletResponse,
            HttpResponse proxyResponse, int statusCode)
            throws ServletException, IOException {
        // Check if the proxy response is a redirect
        // The following code is adapted from org.tigris.noodle.filters.CheckForRedirect
        if (statusCode >= HttpServletResponse.SC_MULTIPLE_CHOICES /* 300 */
                && statusCode < HttpServletResponse.SC_NOT_MODIFIED /* 304 */) {
            Header locationHeader = proxyResponse.getLastHeader(HttpHeaders.LOCATION);
            if (locationHeader == null) {
                throw new ServletException("Received status code: " + statusCode
                        + " but no " + HttpHeaders.LOCATION + " header was found in the response");
            }
            // Modify the redirect to go to this proxy servlet rather that the proxied host
            String locStr = rewriteUrlFromResponse(servletRequest, locationHeader.getValue());

            servletResponse.sendRedirect(locStr);
            return true;
        }
        // 304 needs special handling.  See:
        // http://www.ics.uci.edu/pub/ietf/http/rfc1945.html#Code304
        // We get a 304 whenever passed an 'If-Modified-Since'
        // header and the data on disk has not changed; server
        // responds w/ a 304 saying I'm not going to send the
        // body because the file has not changed.
        if (statusCode == HttpServletResponse.SC_NOT_MODIFIED) {
            servletResponse.setIntHeader(HttpHeaders.CONTENT_LENGTH, 0);
            servletResponse.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            return true;
        }
        return false;
    }

    protected void closeQuietly(Closeable closeable) {
        try {
            closeable.close();
        } catch (IOException e) {
            LOG.error("Error closing HTTP proxy client", e);
        }
    }

    /** These are the "hop-by-hop" headers that should not be copied.
     * http://www.w3.org/Protocols/rfc2616/rfc2616-sec13.html
     * I use an HttpClient HeaderGroup class instead of Set<String> because this
     * approach does case insensitive lookup faster.
     */
    protected static final HeaderGroup hopByHopHeaders;
    static {
        hopByHopHeaders = new HeaderGroup();
        String[] headers = new String[] {
                "Connection", "Keep-Alive", "Proxy-Authenticate", "Proxy-Authorization",
                "TE", "Trailers", "Transfer-Encoding", "Upgrade" };
        for (String header : headers) {
            hopByHopHeaders.addHeader(new BasicHeader(header, null));
        }
    }

    /** Copy request headers from the servlet client to the proxy request. */
    protected void copyRequestHeaders(HttpServletRequest servletRequest, HttpRequest proxyRequest) {
        // Get an Enumeration of all of the header names sent by the client
        Enumeration enumerationOfHeaderNames = servletRequest.getHeaderNames();
        while (enumerationOfHeaderNames.hasMoreElements()) {
            String headerName = (String) enumerationOfHeaderNames.nextElement();
            //Instead the content-length is effectively set via InputStreamEntity
            if (headerName.equalsIgnoreCase(HttpHeaders.CONTENT_LENGTH))
                continue;
            if (hopByHopHeaders.containsHeader(headerName))
                continue;

            Enumeration headers = servletRequest.getHeaders(headerName);
            while (headers.hasMoreElements()) {//sometimes more than one value
                String headerValue = (String) headers.nextElement();
                // In case the proxy host is running multiple virtual servers,
                // rewrite the Host header to ensure that we get content from
                // the correct virtual server
                if (headerName.equalsIgnoreCase(HttpHeaders.HOST)) {
                    HttpHost host = URIUtils.extractHost(this.targetUri);
                    headerValue = host.getHostName();
                    if (host.getPort() != -1)
                        headerValue += ":"+host.getPort();
                }
                proxyRequest.addHeader(headerName, headerValue);
            }
        }
    }

    /** Copy proxied response headers back to the servlet client. */
    protected void copyResponseHeaders(HttpResponse proxyResponse, HttpServletResponse servletResponse) {
        for (Header header : proxyResponse.getAllHeaders()) {
            if (hopByHopHeaders.containsHeader(header.getName()))
                continue;
            servletResponse.addHeader(header.getName(), header.getValue());
        }
    }

    /** Copy response body data (the entity) from the proxy to the servlet client. */
    protected void copyResponseEntity(HttpResponse proxyResponse, HttpServletResponse servletResponse) throws IOException {
        HttpEntity entity = proxyResponse.getEntity();
        if (entity != null) {
            OutputStream servletOutputStream = servletResponse.getOutputStream();
            try {
                entity.writeTo(servletOutputStream);
            } finally {
                closeQuietly(servletOutputStream);
            }
        }
    }

    /** Reads the request URI from {@code servletRequest} and rewrites it, considering {@link
     * #targetUri}. It's used to make the new request.
     */
    protected String rewriteUrlFromRequest(HttpServletRequest servletRequest) {
        StringBuilder uri = new StringBuilder(500);
        uri.append(this.targetUri.toString());

        // Handle the query string
        String sanitizedString = sanitizeQueryString(servletRequest.getQueryString(), servletRequest);
        if (sanitizedString != null && sanitizedString.length() > 0) {
            uri.append('?');
            int fragIdx = sanitizedString.indexOf('#');
            String queryNoFrag = (fragIdx < 0 ? sanitizedString : sanitizedString.substring(0,fragIdx));
            uri.append(encodeUriQuery(queryNoFrag));
            if (fragIdx >= 0) {
                uri.append('#');
                uri.append(encodeUriQuery(sanitizedString.substring(fragIdx + 1)));
            }
        }
        return uri.toString();
    }

    /** For a redirect response from the target server, this translates {@code theUrl} to redirect to
     * and translates it to one the original client can use. */
    protected String rewriteUrlFromResponse(HttpServletRequest servletRequest, String theUrl) {
        //TODO document example paths
        if (theUrl.startsWith(this.targetUri.toString())) {
            String curUrl = servletRequest.getRequestURL().toString();//no query
            String pathInfo = servletRequest.getPathInfo();
            if (pathInfo != null) {
                assert curUrl.endsWith(pathInfo);
                curUrl = curUrl.substring(0,curUrl.length()-pathInfo.length());//take pathInfo off
            }
            theUrl = curUrl+theUrl.substring(this.targetUri.toString().length());
        }
        return theUrl;
    }

    /**
     * Encodes characters in the query or fragment part of the URI.
     *
     * <p>Unfortunately, an incoming URI sometimes has characters disallowed by the spec.  HttpClient
     * insists that the outgoing proxied request has a valid URI because it uses Java's {@link java.net.URI}.
     * To be more forgiving, we must escape the problematic characters.  See the URI class for the
     * spec.
     *
     * @param in example: name=value&foo=bar#fragment
     */
    protected static CharSequence encodeUriQuery(CharSequence in) {
        //Note that I can't simply use URI.java to encode because it will escape pre-existing escaped things.
        StringBuilder outBuf = null;
        Formatter formatter = null;
        for(int i = 0; i < in.length(); i++) {
            char c = in.charAt(i);
            boolean escape = true;
            if (c < 128) {
                if (asciiQueryChars.get((int)c)) {
                    escape = false;
                }
            } else if (!Character.isISOControl(c) && !Character.isSpaceChar(c)) {//not-ascii
                escape = false;
            }
            if (!escape) {
                if (outBuf != null)
                    outBuf.append(c);
            } else {
                //escape
                if (outBuf == null) {
                    outBuf = new StringBuilder(in.length() + 5*3);
                    outBuf.append(in,0,i);
                    formatter = new Formatter(outBuf);
                }
                //leading %, 0 padded, width 2, capital hex
                formatter.format("%%%02X",(int)c);//TODO
            }
        }
        return outBuf != null ? outBuf : in;
    }

    protected static final BitSet asciiQueryChars;
    static {
        char[] c_unreserved = "_-!.~'()*".toCharArray();//plus alphanum
        char[] c_punct = ",;:$&+=".toCharArray();
        char[] c_reserved = "?/[]@".toCharArray();//plus punct

        asciiQueryChars = new BitSet(128);
        for(char c = 'a'; c <= 'z'; c++) asciiQueryChars.set((int)c);
        for(char c = 'A'; c <= 'Z'; c++) asciiQueryChars.set((int)c);
        for(char c = '0'; c <= '9'; c++) asciiQueryChars.set((int)c);
        for(char c : c_unreserved) asciiQueryChars.set((int)c);
        for(char c : c_punct) asciiQueryChars.set((int)c);
        for(char c : c_reserved) asciiQueryChars.set((int)c);

        asciiQueryChars.set((int)'%');//leave existing percent escapes in place
    }
}
