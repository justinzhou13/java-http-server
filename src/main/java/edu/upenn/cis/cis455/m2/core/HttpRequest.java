package edu.upenn.cis.cis455.m2.core;

import edu.upenn.cis.cis455.exceptions.HaltException;
import edu.upenn.cis.cis455.m2.interfaces.Request;
import edu.upenn.cis.cis455.m2.interfaces.Session;
import edu.upenn.cis.cis455.m2.session.SessionManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class HttpRequest extends Request {
	
    final static Logger logger = LogManager.getLogger(HttpRequest.class);
	
	private final String requestMethod;
	private final String protocol;
	private final String pathInfo;
	private final String host;
	private final int port;
	private final String uri;
	private final String url;
	private final String queryString;

	private final String userAgent;
	private final Map<String, String> headers;
	private final Map<String, List<String>> queryStringParms;
	private final String ip;
	private final String body;

	private Map<String, String> pathParams;
	private String[] splat = new String[0];
	private final Map<String, Object> attributes;

	private Session session;
	private final Map<String, String> cookies;

	public HttpRequest(String requestMethod,
	                   String protocol,
	                   String pathInfo,
	                   String host,
					   int port,
	                   String userAgent,
	                   Map<String, String> headers,
	                   String queryString,
	                   Map<String, List<String>> queryStringParms,
	                   String ip,
	                   String body) {
		if (requestMethod == null || protocol == null || pathInfo == null || host == null) {
			logger.error("Request with null values for request method received");
			throw new HaltException(400);
		}
		this.requestMethod = requestMethod;
		this.protocol = protocol;

		if (pathInfo.startsWith("http")) {
			//counting forward from "http://" to the first backslash, handing absolute paths
			pathInfo = pathInfo.substring(pathInfo.indexOf('/', 8));
		}
		this.pathInfo = pathInfo;
		this.host = host;
		this.port = port;
		this.uri = String.format("http://%s:%s%s", this.host, this.port, this.pathInfo);
		this.url = queryString != null && queryString.length() > 0 ? String.format("%s?%s", uri, queryString) : uri;
		this.queryString = queryString;
		this.userAgent = userAgent;
		this.headers = headers;
		this.queryStringParms = queryStringParms;
		if (headers.containsKey("content-type") && headers.get("content-type").equals("application/x-www-form-urlencoded")) {
			String[] params = body.split("&");
			for (String param : params) {
				int equalsIndex = param.indexOf("=");
				if (equalsIndex != -1) {
					String key = param.substring(0, equalsIndex);
					String value = param.substring(equalsIndex + 1);
					List<String> queryParamsForKey = this.queryStringParms.get(key);
					if (queryParamsForKey == null) {
						queryParamsForKey = new ArrayList<>();
					}
					queryParamsForKey.add(value);
					this.queryStringParms.put(key, queryParamsForKey);
				}
			}
		}

		this.ip = ip;
		this.body = body;
		this.pathParams = new HashMap<>();
		this.attributes = new HashMap<>();

		this.cookies = new HashMap<>();
		if (headers.containsKey("cookie")) {
			String[] cookies = headers.get("cookie").split(";");
			for (String cookie : cookies) {
				int equalsIndex = cookie.indexOf('=');
				if (equalsIndex != -1 && equalsIndex < cookie.length() - 1) {
					String cookieName = cookie.substring(0, equalsIndex);
					String cookieValue = cookie.substring(equalsIndex + 1);
					this.cookies.put(cookieName, cookieValue);

					if (cookieName.equals("JSESSIONID")) {
						this.session = SessionManager.getSession(cookieValue);
					}
				}
			}
		}
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
		return headers.getOrDefault("content-type", "");
	}

	@Override
	public String ip() {
		return this.ip;
	}

	@Override
	public String body() {
		return this.body;
	}

	@Override
	public int contentLength() {
		return this.body.length();
	}

	@Override
	public String headers(String name) {
		return headers.get(name);
	}

	@Override
	public Set<String> headers() {
		return headers.keySet();
	}

	@Override
	public Session session() {
		if (this.session == null) {
			this.session = new WebSession();
		}
		return this.session;
	}

	@Override
	public Session session(boolean create) {
		if (create) {
			this.session = new WebSession();
		}
		return this.session;
	}

	public void setPathParams(Map<String, String> pathParams) {
		this.pathParams = pathParams;
	}

	@Override
	public Map<String, String> params() {
		return this.pathParams;
	}

	@Override
	public String queryParams(String param) {
		List<String> paramValues = queryStringParms.get(param);
		if (paramValues != null && paramValues.size() > 0) {
			return paramValues.get(0);
		}
		return null;
	}

	@Override
	public List<String> queryParamsValues(String param) {
		return this.queryStringParms.get(param);
	}

	@Override
	public Set<String> queryParams() {
		return this.queryStringParms.keySet();
	}

	@Override
	public String queryString() {
		return this.queryString;
	}

	@Override
	public void attribute(String attrib, Object val) {
		this.attributes.put(attrib, val);
	}

	@Override
	public Object attribute(String attrib) {
		return this.attributes.get(attrib);
	}

	@Override
	public Set<String> attributes() {
		return this.attributes.keySet();
	}

	@Override
	public Map<String, String> cookies() {
		return this.cookies;
	}

	public String[] splat() {
		return this.splat;
	}

	public void setSplat(String[] splat) {
		this.splat = splat;
	}
}
