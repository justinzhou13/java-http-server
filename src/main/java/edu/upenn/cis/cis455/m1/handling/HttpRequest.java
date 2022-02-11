package edu.upenn.cis.cis455.m1.handling;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import edu.upenn.cis.cis455.exceptions.HaltException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.upenn.cis.cis455.m1.interfaces.Request;

public class HttpRequest extends Request {
	
    final static Logger logger = LogManager.getLogger(HttpIoHandler.class);
	
	private final String requestMethod;
	private final String protocol;
	private final String pathInfo;
	private final String host;
	private final int port;
	private final String uri;
	private final String url;

	private final String userAgent;
	private final Map<String, String> headers;
	private final String queryString;
	private final Map<String, List<String>> queryStringParms;
	private final String ip;

	public HttpRequest(String requestMethod,
	                   String protocol,
	                   String pathInfo,
	                   String host,
					   int port,
	                   String userAgent,
	                   Map<String, String> headers,
	                   String queryString,
	                   Map<String, List<String>> queryStringParms,
	                   String ip) {
		if (requestMethod == null || protocol == null || pathInfo == null || host == null) {
			throw new HaltException(400);
		}
		this.requestMethod = requestMethod;
		this.protocol = protocol;
		this.pathInfo = pathInfo;
		this.host = host;
		this.port = port;
		this.queryString = queryString;
		this.uri = String.format("http://%s:%s%s", this.host, this.port, this.pathInfo);
		this.url = queryString != null && queryString.length() > 0 ? String.format("%s?%s", uri, queryString) : uri;
		this.userAgent = userAgent;
		this.headers = headers;
		this.queryStringParms = queryStringParms;
		this.ip = ip;
	}

	@Override
	public String requestMethod() {
		return this.requestMethod;
	}

	@Override
	public String host() {
		return this.host;
	}

	@Override
	public String userAgent() {
		return this.userAgent;
	}

	@Override
	public int port() {
		return this.port;
	}

	@Override
	public String pathInfo() {
		return this.pathInfo;
	}

	@Override
	public String url() {
		return this.url;
	}

	@Override
	public String uri() {
		return this.uri;
	}

	@Override
	public String protocol() {
		return this.protocol;
	}

	@Override
	public String contentType() {
		return headers.get("content-type");
	}

	@Override
	public String ip() {
		return this.ip;
	}

	@Override
	public String body() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int contentLength() {
		try {
			return Integer.parseInt(headers.get("content-length"));
		} catch (Exception e) {
			logger.info("No content length found for request");
		}
		return 0;
	}

	@Override
	public String headers(String name) {
		return headers.getOrDefault(name, null);
	}

	@Override
	public Set<String> headers() {
		return headers.keySet();
	}

}
