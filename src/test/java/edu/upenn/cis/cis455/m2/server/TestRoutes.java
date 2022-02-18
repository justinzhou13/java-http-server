package edu.upenn.cis.cis455.m2.server;

import edu.upenn.cis.cis455.m2.routehandling.GetFileRoute;
import org.apache.logging.log4j.Level;
import org.junit.Before;
import org.junit.Test;

import static edu.upenn.cis.cis455.m1.handling.RequestHandler.addRouteToTree;
import static edu.upenn.cis.cis455.m1.handling.RequestHandler.getRoute;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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

        assertTrue(wwwTest.getRoot().equals("./www"));
        assertTrue(orgTest.getRoot().equals("./org"));
        assertNull(getRoute("GET", "/"));
    }
}
