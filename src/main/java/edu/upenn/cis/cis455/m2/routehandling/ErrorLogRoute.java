package edu.upenn.cis.cis455.m2.routehandling;

import edu.upenn.cis.cis455.m2.interfaces.Request;
import edu.upenn.cis.cis455.m2.interfaces.Response;
import edu.upenn.cis.cis455.m2.interfaces.Route;

public class ErrorLogRoute implements Route {

    private static final String ERROR_LOG_PATH = "./errors.log";

    @Override
    public Object handle(Request request, Response response) throws Exception {
        GetFileRoute.populateResponseWithFile(ERROR_LOG_PATH, "/", request, response);
        return null;
    }
}
