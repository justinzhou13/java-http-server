package edu.upenn.cis.cis455.m2.server;

import edu.upenn.cis.cis455.m2.routehandling.GetFileRoute;
import org.apache.logging.log4j.Level;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static edu.upenn.cis.cis455.m1.handling.RequestHandler.addRoute;
import static edu.upenn.cis.cis455.m1.handling.RequestHandler.getRoute;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TestRoutes {

    @Before
    public void setUp() {
        org.apache.logging.log4j.core.config.Configurator.setLevel("edu.upenn.cis.cis455", Level.DEBUG);
    }

    @Test
    public void testRoutesAddedSuccessfully() {
        edu.upenn.cis.cis455.m2.routehandling.GetFileRoute wwwRoute = new edu.upenn.cis.cis455.m2.routehandling.GetFileRoute();
        wwwRoute.setRoot("./www");
        addRoute("GET", "/www", wwwRoute);
        edu.upenn.cis.cis455.m2.routehandling.GetFileRoute orgRoute = new edu.upenn.cis.cis455.m2.routehandling.GetFileRoute();
        orgRoute.setRoot("./org");
        addRoute("GET", "/org", orgRoute);
        GetFileRoute wwwTest = (GetFileRoute) getRoute("GET", "/www", new HashMap<>());
        GetFileRoute orgTest = (GetFileRoute) getRoute("GET", "/org",new HashMap<>());

        assertEquals("./www", wwwTest.getRoot());
        assertEquals("./org", orgTest.getRoot());
        assertNull(getRoute("GET", "/", new HashMap<>()));
    }

    @Test
    public void testRoutesWithWildCardAddedSuccessfully() {
        edu.upenn.cis.cis455.m2.routehandling.GetFileRoute wwwRoute = new edu.upenn.cis.cis455.m2.routehandling.GetFileRoute();
        wwwRoute.setRoot("./www");
        addRoute("GET", "/*/www/*/hello", wwwRoute);
        GetFileRoute wwwTest = (GetFileRoute) getRoute("GET", "/randomvalue/www/someothervalue/hello", new HashMap<>());

        assertEquals("./www", wwwTest.getRoot());
        assertNull(getRoute("GET", "/", new HashMap<>()));
    }

    @Test
    public void testRoutesWithParamsAddedSuccessfully() {
        edu.upenn.cis.cis455.m2.routehandling.GetFileRoute wwwRoute = new edu.upenn.cis.cis455.m2.routehandling.GetFileRoute();
        wwwRoute.setRoot("./blah");
        addRoute("GET", "/:value1/www/:value2/hello/:value3", wwwRoute);
        Map<String, String> params = new HashMap<>();
        GetFileRoute wwwTest = (GetFileRoute) getRoute("GET", "/randomvalue/www/someothervalue/hello/finalvalue", params);

        assertEquals("./blah", wwwTest.getRoot());
        assertEquals("randomvalue", params.get(":value1"));
        assertEquals("someothervalue", params.get(":value2"));
        assertEquals("finalvalue", params.get(":value3"));
        assertNull(getRoute("GET", "/", new HashMap<>()));
    }

    @Test
    public void testRoutesWithOverlappingPathsAddedSuccessfully() {
        edu.upenn.cis.cis455.m2.routehandling.GetFileRoute wwwRoute = new edu.upenn.cis.cis455.m2.routehandling.GetFileRoute();
        wwwRoute.setRoot("./www");
        addRoute("GET", "/route/www", wwwRoute);
        edu.upenn.cis.cis455.m2.routehandling.GetFileRoute orgRoute = new edu.upenn.cis.cis455.m2.routehandling.GetFileRoute();
        orgRoute.setRoot("./org");
        addRoute("GET", "/route/org", orgRoute);
        GetFileRoute wwwTest = (GetFileRoute) getRoute("GET", "/route/www", new HashMap<>());
        GetFileRoute orgTest = (GetFileRoute) getRoute("GET", "/route/org",new HashMap<>());

        assertEquals("./www", wwwTest.getRoot());
        assertEquals("./org", orgTest.getRoot());
        assertNull(getRoute("GET", "/", new HashMap<>()));
    }
}
