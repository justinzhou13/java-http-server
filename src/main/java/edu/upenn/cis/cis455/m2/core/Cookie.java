package edu.upenn.cis.cis455.m2.core;

public class Cookie {

    private String name;
    private String value;
    private Integer maxAge;
    private Boolean secured;
    private Boolean httpOnly;
    private String path;

    public Cookie(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public void setMaxAge(Integer maxAge) {
        this.maxAge = maxAge;
    }

    public void setSecured(Boolean secured) {
        this.secured = secured;
    }

    public void setHttpOnly(Boolean httpOnly) {
        this.httpOnly = httpOnly;
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        String cookie = name + "=" + value;
        if (maxAge != null) cookie += "; Max-Age=" + maxAge;
        if (secured != null) cookie += "; Secure";
        if (httpOnly != null) cookie += "; HttpOnly";
        if (path != null) cookie += "; Path=" + path;
        return cookie;
    }
}
