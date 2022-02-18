package edu.upenn.cis.cis455.m1.handling;

import edu.upenn.cis.cis455.m2.interfaces.Response;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class HttpResponse extends Response {

	private Map<String, String> headers;

	public HttpResponse(Map<String, String> headers) {
		this.headers = headers;
	}

	public void addHeader(String header, String value) {
		headers.put(header, value);
	}

	public void removeHeader(String header) {
		headers.remove(header);
	}

	@Override
	public void type(String contentType) {
		if (contentType != null) addHeader("Content-Type", contentType);
		else removeHeader("Content-Type");
		super.type(contentType);
	}

	@Override
	public void bodyRaw(byte[] b) {
		if (b != null) addHeader("Content-Length", String.valueOf(b.length));
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
		if (headers != null) {
			StringBuilder stringBuilder = new StringBuilder();
			for (Map.Entry<String, String> entry : headers.entrySet()) {
				stringBuilder.append(entry.getKey());
				stringBuilder.append(": ");
				stringBuilder.append(entry.getValue());
				stringBuilder.append("\r\n");
			}
			return stringBuilder.toString();
		}
		return "";
	}

	@Override
	public void header(String header, String value) {

	}

	@Override
	public void redirect(String location) {

	}

	@Override
	public void redirect(String location, int httpStatusCode) {

	}

	@Override
	public void cookie(String name, String value) {

	}

	@Override
	public void cookie(String name, String value, int maxAge) {

	}

	@Override
	public void cookie(String name, String value, int maxAge, boolean secured) {

	}

	@Override
	public void cookie(String name, String value, int maxAge, boolean secured, boolean httpOnly) {

	}

	@Override
	public void cookie(String path, String name, String value) {

	}

	@Override
	public void cookie(String path, String name, String value, int maxAge) {

	}

	@Override
	public void cookie(String path, String name, String value, int maxAge, boolean secured) {

	}

	@Override
	public void cookie(String path, String name, String value, int maxAge, boolean secured, boolean httpOnly) {

	}

	@Override
	public void removeCookie(String name) {

	}

	@Override
	public void removeCookie(String path, String name) {

	}
}
