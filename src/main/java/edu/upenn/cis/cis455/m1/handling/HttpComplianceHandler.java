package edu.upenn.cis.cis455.m1.handling;

import edu.upenn.cis.cis455.m1.interfaces.Request;
import edu.upenn.cis.cis455.m1.interfaces.Response;

public class HttpComplianceHandler {

    private static final String NO_HOST_RESPONSE =
        "<html><body>\n" +
        "<h2>No Host: header received</h2>\n" +
        "HTTP 1.1 requests must include the Host: header.\n" +
        "</body></html>";

    public static boolean isCompliant(Request request, Response response) {
        String protocolVersion = request.protocol();
        if (protocolVersion.equals("HTTP/1.1") && request.headers("host") == null) {
            response.status(400);
            response.type("text/html");
            response.body(NO_HOST_RESPONSE);
            return false;
        }
        return true;
    }
}
