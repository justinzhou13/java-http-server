package edu.upenn.cis.cis455.m1.interfaces;

/**
 * A Route Handler is called when an HTTP request maps to the assigned route. It
 * is given Request info.
 */
@FunctionalInterface
public interface Route {

    /**
     * A route handler for a given HTTP request.
     * 
     */
    Object handle(Request request, Response response) throws Exception;
}
