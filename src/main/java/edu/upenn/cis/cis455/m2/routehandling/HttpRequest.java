package edu.upenn.cis.cis455.m2.routehandling;

import edu.upenn.cis.cis455.m2.interfaces.Request;
import edu.upenn.cis.cis455.m2.interfaces.Session;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class HttpRequest extends Request {
    @Override
    public String requestMethod() {
        return null;
    }

    @Override
    public String host() {
        return null;
    }

    @Override
    public String userAgent() {
        return null;
    }

    @Override
    public int port() {
        return 0;
    }

    @Override
    public String pathInfo() {
        return null;
    }

    @Override
    public String url() {
        return null;
    }

    @Override
    public String uri() {
        return null;
    }

    @Override
    public String protocol() {
        return null;
    }

    @Override
    public String contentType() {
        return null;
    }

    @Override
    public String ip() {
        return null;
    }

    @Override
    public String body() {
        return null;
    }

    @Override
    public int contentLength() {
        return 0;
    }

    @Override
    public String headers(String name) {
        return null;
    }

    @Override
    public Set<String> headers() {
        return null;
    }

    @Override
    public Session session() {
        return null;
    }

    @Override
    public Session session(boolean create) {
        return null;
    }

    @Override
    public Map<String, String> params() {
        return null;
    }

    @Override
    public String queryParams(String param) {
        return null;
    }

    @Override
    public List<String> queryParamsValues(String param) {
        return null;
    }

    @Override
    public Set<String> queryParams() {
        return null;
    }

    @Override
    public String queryString() {
        return null;
    }

    @Override
    public void attribute(String attrib, Object val) {

    }

    @Override
    public Object attribute(String attrib) {
        return null;
    }

    @Override
    public Set<String> attributes() {
        return null;
    }

    @Override
    public Map<String, String> cookies() {
        return null;
    }
}
