package edu.upenn.cis.cis455.m1.handling;

import edu.upenn.cis.cis455.m2.interfaces.Request;
import edu.upenn.cis.cis455.m2.interfaces.Response;
import edu.upenn.cis.cis455.m2.interfaces.Route;

import java.util.Map;

public class ControlPanelRoute implements Route {

    private final Map<String, String> workerThreadNameToStatus;
    private static final String OUTPUT_HTML_TABLE = "" +
            "<!DOCTYPE html>\n" +
            "<html>\n" +
            "<head>\n" +
            "    <title>Webserver Control Panel</title>\n" +
            "</head>\n" +
            "<body>\n" +
            "<h1>Webserver Worker Thread Statuses</h1>\n" +
            "<table>\n" +
                "<tr>\n" +
                    "<th>Worker Thread Name</th>\n" +
                    "<th>Current URL Handling</th\n" +
                "</tr>\n" +
                "%s" +
            "</table>\n" +
            "<a href=/shutdown><button>Shut down the server</button></a>\n" +
            "</body>\n" +
            "</html>";

    public ControlPanelRoute(Map<String, String> workerThreadNameToStatus) {
        this.workerThreadNameToStatus = workerThreadNameToStatus;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        String workerStatusTable = "";
        synchronized (workerThreadNameToStatus) {
            StringBuilder tableBuilder = new StringBuilder();
            for (Map.Entry<String, String> entry : workerThreadNameToStatus.entrySet()) {
                tableBuilder.append("<tr>");
                tableBuilder.append(String.format("<td>%s</td>", entry.getKey()));
                tableBuilder.append(String.format("<td>%s</td>", entry.getValue()));
                tableBuilder.append("</tr>\n");
            }
            workerStatusTable = tableBuilder.toString();
        }
        String fullControlPanel = String.format(OUTPUT_HTML_TABLE, workerStatusTable);
        response.body(fullControlPanel);
        response.type("text/html");
        return null;
    }
}
