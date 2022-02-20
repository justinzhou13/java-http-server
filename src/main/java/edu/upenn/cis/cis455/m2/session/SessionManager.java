package edu.upenn.cis.cis455.m2.session;

import edu.upenn.cis.cis455.m2.interfaces.Session;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class SessionManager {

    static final Logger logger = LogManager.getLogger(SessionManager.class);

    private static final Map<String, Session> activeSessions;

    static {
        activeSessions = new HashMap<>();
    }

    public static Session getSession(String id) {
        Session session;
        synchronized (activeSessions) {
            session = activeSessions.get(id);
        }
        if (session != null) {
            if (session.lastAccessedTime() + session.maxInactiveInterval() * 1000L < Instant.now().toEpochMilli()) {
                removeSession(session);
                return null;
            }
            session.access();
        }
        return session;
    }

    public static void addSession(Session session) {
        if (session != null) {
            logger.info("Adding session " + session.id());
            synchronized (activeSessions) {
                activeSessions.put(session.id(), session);
            }
        }
    }

    public static void removeSession(Session session) {
        if (session != null) {
            synchronized (activeSessions) {
                activeSessions.remove(session.id());
            }
        }
    }
}
