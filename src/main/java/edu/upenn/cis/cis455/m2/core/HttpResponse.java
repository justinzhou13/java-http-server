package edu.upenn.cis.cis455.m2.core;

import edu.upenn.cis.cis455.m2.interfaces.Response;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpResponse extends Response {

	private final Map<String, String> headers;
	private final Map<String, Cookie> nameToCookie;

	public HttpResponse(Map<String, String> headers) {
		this.headers = headers != null ? headers : new HashMap<>();
		nameToCookie = new HashMap<>();
	}

	public void removeHeader(String header) {
		headers.remove(header);
	}

	@Override
	public void type(String contentType) {
		if (contentType != null) header("Content-Type", contentType);
		else removeHeader("Content-Type");
		super.type(contentType);
	}

	@Override
	public void bodyRaw(byte[] b) {
		if (b != null) header("Content-Length", String.valueOf(b.length));
		else removeHeader("Content-Length");
		super.bodyRaw(b);
	}

	@Override
	public void body(String body) {
		if (body != null) bodyRaw(body.getBytes(StandardCharsets.UTF_8));
		else bodyRaw(null);
	}

	@Override
	public String getHeaders() {
		StringBuilder headerLines = new StringBuilder();
		for (Map.Entry<String, String> entry : headers.entrySet()) {
			headerLines.append(entry.getKey());
			headerLines.append(": ");
			headerLines.append(entry.getValue());
			headerLines.append("\r\n");
		}
		headerLines.append(getCookieHeaders());
		return headerLines.toString();
	}

	@Override
	public void header(String header, String value) {
		headers.put(header, value);
	}

	@Override
	public void redirect(String location) {
		status(301);
		header("Location", location);
	}

	@Override
	public void redirect(String location, int httpStatusCode) {
		status(httpStatusCode);
		header("Location", location);
	}

	@Override
	public void cookie(String name, String value) {
		Cookie cookie = new Cookie(name, value);
		nameToCookie.put(name, cookie);
	}

	@Override
	public void cookie(String name, String value, int maxAge) {
		Cookie cookie = new Cookie(name, value);
		cookie.setMaxAge(maxAge);
		nameToCookie.put(name, cookie);
	}

	@Override
	public void cookie(String name, String value, int maxAge, boolean secured) {
		Cookie cookie = new Cookie(name, value);
		cookie.setMaxAge(maxAge);
		cookie.setSecured(secured);
		nameToCookie.put(name, cookie);
	}

	@Override
	public void cookie(String name, String value, int maxAge, boolean secured, boolean httpOnly) {
		Cookie cookie = new Cookie(name, value);
		cookie.setMaxAge(maxAge);
		cookie.setSecured(secured);
		cookie.setHttpOnly(httpOnly);
		nameToCookie.put(name, cookie);
	}

	@Override
	public void cookie(String path, String name, String value) {
		Cookie cookie = new Cookie(name, value);
		cookie.setPath(path);
		nameToCookie.put(name, cookie);
	}

	@Override
	public void cookie(String path, String name, String value, int maxAge) {
		Cookie cookie = new Cookie(name, value);
		cookie.setMaxAge(maxAge);
		cookie.setPath(path);
		nameToCookie.put(name, cookie);
	}

	@Override
	public void cookie(String path, String name, String value, int maxAge, boolean secured) {
		Cookie cookie = new Cookie(name, value);
		cookie.setMaxAge(maxAge);
		cookie.setSecured(secured);
		cookie.setPath(path);
		nameToCookie.put(name, cookie);
	}

	@Override
	public void cookie(String path, String name, String value, int maxAge, boolean secured, boolean httpOnly) {
		Cookie cookie = new Cookie(name, value);
		cookie.setMaxAge(maxAge);
		cookie.setSecured(secured);
		cookie.setPath(path);
		cookie.setHttpOnly(httpOnly);
		nameToCookie.put(name, cookie);
	}

	@Override
	public void removeCookie(String name) {
		nameToCookie.remove(name);
	}

	@Override
	public void removeCookie(String path, String name) {
		Cookie cookie = nameToCookie.get(name);
		if (cookie != null && cookie.getPath().equals(path)) nameToCookie.remove(name);
	}

	private String getCookieHeaders() {
		StringBuilder cookieHeaders = new StringBuilder();
		for (Cookie cookie : nameToCookie.values()) {
			cookieHeaders.append("Set-Cookie: ");
			cookieHeaders.append(cookie.toString());
			cookieHeaders.append("\r\n");
		}
		return cookieHeaders.toString();
	}
}
