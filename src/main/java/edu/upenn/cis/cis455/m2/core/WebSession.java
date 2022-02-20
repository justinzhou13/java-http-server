package edu.upenn.cis.cis455.m2.core;

import edu.upenn.cis.cis455.m2.interfaces.Session;
import edu.upenn.cis.cis455.m2.session.SessionManager;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class WebSession extends Session {

    private static final int DEFAULT_MAX_INACTIVE_INTERVAL = 3600;

    private final String id;
    private final long creationTime;
    private long lastAccessedTime;
    private int maxInactiveInterval;
    private final Map<String, Object> attributes;

    public WebSession() {
        this.id = UUID.randomUUID().toString();
        this.creationTime = Instant.now().toEpochMilli();
        this.lastAccessedTime = this.creationTime;
        this.maxInactiveInterval = DEFAULT_MAX_INACTIVE_INTERVAL;
        this.attributes = new HashMap<>();

        SessionManager.addSession(this);
    }

    @Override
    public String id() {
        return this.id;
    }

    @Override
    public long creationTime() {
        return this.creationTime;
    }

    @Override
    public long lastAccessedTime() {
        return this.lastAccessedTime;
    }

    @Override
    public void invalidate() {
        SessionManager.removeSession(this);
    }

    @Override
    public int maxInactiveInterval() {
        return this.maxInactiveInterval;
    }

    @Override
    public void maxInactiveInterval(int interval) {
        this.maxInactiveInterval = interval;
    }

    @Override
    public void access() {
        this.lastAccessedTime = Instant.now().toEpochMilli();
    }

    @Override
    public void attribute(String name, Object value) {
        this.attributes.put(name, value);
    }

    @Override
    public Object attribute(String name) {
        return this.attributes.get(name);
    }

    @Override
    public Set<String> attributes() {
        return this.attributes.keySet();
    }

    @Override
    public void removeAttribute(String name) {
        this.attributes.remove(name);
    }
}
