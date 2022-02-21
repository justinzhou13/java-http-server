package edu.upenn.cis.cis455.m2.server;

import edu.upenn.cis.cis455.m2.routehandling.PathToRoutePair;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestPathMatching {

    @Test
    public void testNoWildCards() {
        String definedPath = "/hello";
        String requestedPath = "/hello";

        assertTrue(PathToRoutePair.matchPathToMultistepWildcard(requestedPath, definedPath));
    }

    @Test
    public void testNoWildCardsMultiStepMatches() {
        String definedPath = "/hello/world/path";
        String requestedPath = "/hello/world/path";

        assertTrue(PathToRoutePair.matchPathToMultistepWildcard(requestedPath, definedPath));
    }

    @Test
    public void testNoWildCardsMultiStepNoMatch() {
        String definedPath = "/hello/world/path";
        String requestedPath = "/hello/world";

        assertFalse(PathToRoutePair.matchPathToMultistepWildcard(requestedPath, definedPath));
    }

    @Test
    public void testNoWildCardsMultiStepNoMatchDefinedShorter() {
        String definedPath = "/hello/world";
        String requestedPath = "/hello/world/path";

        assertFalse(PathToRoutePair.matchPathToMultistepWildcard(requestedPath, definedPath));
    }

    @Test
    public void testWildCardPathMatches() {
        String definedPath = "/*";
        String requestedPath = "/hello/world/path";

        assertTrue(PathToRoutePair.matchPathToMultistepWildcard(requestedPath, definedPath));
    }

    @Test
    public void testWildCardPathMultistepMatches() {
        String definedPath = "/*/world/*";
        String requestedPath = "/hello/spacer/world/path/spacer/";

        assertTrue(PathToRoutePair.matchPathToMultistepWildcard(requestedPath, definedPath));
    }

    @Test
    public void testWildCardNoMatch() {
        String definedPath = "/*/hello";
        String requestedPath = "/hello";

        assertFalse(PathToRoutePair.matchPathToMultistepWildcard(requestedPath, definedPath));
    }

    @Test
    public void testMultiWildCardNoMatch() {
        String definedPath = "/*/*/hello/*";
        String requestedPath = "/hello/hello/hello";

        assertFalse(PathToRoutePair.matchPathToMultistepWildcard(requestedPath, definedPath));
    }
}
