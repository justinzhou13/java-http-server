package edu.upenn.cis.cis455.m2.server;

import edu.upenn.cis.cis455.m2.routehandling.PathToRoutePair;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.Assert.*;

public class TestPathMatching {

    @Test
    public void testNoWildCards() {
        String definedPath = "/hello";
        String requestedPath = "/hello";

        assertTrue(PathToRoutePair.matchPathToSteps(requestedPath, definedPath, new HashMap<>(), new ArrayList<>()));
    }

    @Test
    public void testNoWildCardsMultiStepMatches() {
        String definedPath = "/hello/world/path";
        String requestedPath = "/hello/world/path";

        assertTrue(PathToRoutePair.matchPathToSteps(requestedPath, definedPath, new HashMap<>(), new ArrayList<>()));
    }

    @Test
    public void testNoWildCardsMultiStepNoMatch() {
        String definedPath = "/hello/world/path";
        String requestedPath = "/hello/world";

        assertFalse(PathToRoutePair.matchPathToSteps(requestedPath, definedPath, new HashMap<>(), new ArrayList<>()));
    }

    @Test
    public void testNoWildCardsMultiStepNoMatchDefinedShorter() {
        String definedPath = "/hello/world";
        String requestedPath = "/hello/world/path";

        assertFalse(PathToRoutePair.matchPathToSteps(requestedPath, definedPath, new HashMap<>(), new ArrayList<>()));
    }

    @Test
    public void testWildCardPathMatches() {
        String definedPath = "/*";
        String requestedPath = "/hello/world/path";

        assertTrue(PathToRoutePair.matchPathToSteps(requestedPath, definedPath, new HashMap<>(), new ArrayList<>()));
    }

    @Test
    public void testWildCardPathMultistepMatches() {
        String definedPath = "/*/world/*";
        String requestedPath = "/hello/world/path/spacer/lol/spacer";

        assertTrue(PathToRoutePair.matchPathToSteps(requestedPath, definedPath, new HashMap<>(), new ArrayList<>()));
    }

    @Test
    public void testWildCardNoMatch() {
        String definedPath = "/*/hello";
        String requestedPath = "/hello";

        assertFalse(PathToRoutePair.matchPathToSteps(requestedPath, definedPath, new HashMap<>(), new ArrayList<>()));
    }

    @Test
    public void testSplat() {
        String definedPath = "/*/*";
        String requestedPath = "/hello/1/2/3/4";

        ArrayList<String> splat = new ArrayList<>();
        assertTrue(PathToRoutePair.matchPathToSteps(requestedPath, definedPath, new HashMap<>(), splat));

        String[] expectedSplat = new String[]{"hello", "1/2/3/4"};
        assertArrayEquals(splat.toArray(), expectedSplat);
    }

    @Test
    public void testSplatSingle() {
        String definedPath = "/*";
        String requestedPath = "/hello/1/2/3/4";

        ArrayList<String> splat = new ArrayList<>();
        assertTrue(PathToRoutePair.matchPathToSteps(requestedPath, definedPath, new HashMap<>(), splat));

        String[] expectedSplat = new String[]{"hello/1/2/3/4"};
        assertArrayEquals(splat.toArray(), expectedSplat);
    }
}
