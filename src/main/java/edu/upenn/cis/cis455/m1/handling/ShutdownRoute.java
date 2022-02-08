package edu.upenn.cis.cis455.m1.handling;

import edu.upenn.cis.cis455.m1.interfaces.Request;
import edu.upenn.cis.cis455.m1.interfaces.Response;
import edu.upenn.cis.cis455.m1.interfaces.Route;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ShutdownRoute implements Route {

    static final Logger logger = LogManager.getLogger(ShutdownRoute.class);

    public Object handle(Request request, Response response) {
        logger.info("Attempting to shut down");
        return true;
    }
}
