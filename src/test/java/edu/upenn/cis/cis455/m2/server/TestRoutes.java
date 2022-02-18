package edu.upenn.cis.cis455.m2.server;

import edu.upenn.cis.cis455.m2.routehandling.GetFileRoute;
import org.apache.logging.log4j.Level;
import org.junit.Before;
import org.junit.Test;

import static edu.upenn.cis.cis455.m1.handling.RequestHandler.addRouteToTree;
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
        addRouteToTree("GET", "/www", wwwRoute);
        edu.upenn.cis.cis455.m2.routehandling.GetFileRoute orgRoute = new edu.upenn.cis.cis455.m2.routehandling.GetFileRoute();
        orgRoute.setRoot("./org");
        addRouteToTree("GET", "/org", orgRoute);
        GetFileRoute wwwTest = (GetFileRoute) getRoute("GET", "/www");
        GetFileRoute orgTest = (GetFileRoute) getRoute("GET", "/org");

        assertEquals("./www", wwwTest.getRoot());
        assertEquals("./org", orgTest.getRoot());
        assertNull(getRoute("GET", "/"));
    }

    @Test
    public void testRoutesWithWildCardAddedSuccessfully() {
        edu.upenn.cis.cis455.m2.routehandling.GetFileRoute wwwRoute = new edu.upenn.cis.cis455.m2.routehandling.GetFileRoute();
        wwwRoute.setRoot("./www");
        addRouteToTree("GET", "/*/www/*/hello", wwwRoute);
        GetFileRoute wwwTest = (GetFileRoute) getRoute("GET", "/randomvalue/www/someothervalue/hello");

        assertEquals("./www", wwwTest.getRoot());
        assertNull(getRoute("GET", "/"));
    }

    @Test
    public void testRoutesWithParamsAddedSuccessfully() {
        edu.upenn.cis.cis455.m2.routehandling.GetFileRoute wwwRoute = new edu.upenn.cis.cis455.m2.routehandling.GetFileRoute();
        wwwRoute.setRoot("./blah");
        addRouteToTree("GET", "/:value1/www/:value2/hello/:value3", wwwRoute);
        GetFileRoute wwwTest = (GetFileRoute) getRoute("GET", "/randomvalue/www/someothervalue/hello/finalvalue");

        assertEquals("./blah", wwwTest.getRoot());
        assertNull(getRoute("GET", "/"));
    }
}
