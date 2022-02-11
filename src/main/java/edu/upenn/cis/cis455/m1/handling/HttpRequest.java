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
	private final String uri;
	private final String host;
	private final String userAgent;
	private final Map<String, String> headers;
	private final Map<String, List<String>> queryStringParms;
	
	
	public HttpRequest(Map<String, String> pre, Map<String, List<String>> parms, Map<String, String> headers) {
		this.requestMethod = pre.getOrDefault("method", null);
		this.protocol = pre.getOrDefault("protocolVersion", "");
		if (!(this.protocol.equals("HTTP/1.1") || this.protocol.equals("HTTP/1.0"))) {
			throw new HaltException(505);
		}

		this.uri = pre.getOrDefault("uri", null);
		this.host = headers.getOrDefault("host", null);
		this.userAgent = headers.getOrDefault("user-agent", null);
		this.headers = Collections.unmodifiableMap(headers);
		this.queryStringParms = parms;
	}

	@Override
	public String requestMethod() {
		return requestMethod;
	}

	@Override
	public String host() {
		return host;
	}

	@Override
	public String userAgent() {
		return userAgent;
	}

	@Override
	public int port() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String pathInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String url() {
		return host + uri;
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
		return headers.getOrDefault("content-type", null);
	}

	@Override
	public String ip() {
		// TODO Auto-generated method stub
		return null;
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
