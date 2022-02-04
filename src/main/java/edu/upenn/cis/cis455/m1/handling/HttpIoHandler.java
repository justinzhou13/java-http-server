package edu.upenn.cis.cis455.m1.handling;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.upenn.cis.cis455.exceptions.HaltException;
import edu.upenn.cis.cis455.m1.interfaces.Request;
import edu.upenn.cis.cis455.m1.interfaces.Response;

/**
 * Handles marshaling between HTTP Requests and Responses
 */
public class HttpIoHandler {
    final static Logger logger = LogManager.getLogger(HttpIoHandler.class);
    
    final static Map<Integer, String> statusCodeToDescription;
    
    static {
    	statusCodeToDescription = new HashMap<>();
    	statusCodeToDescription.put(200, "OK");
    	statusCodeToDescription.put(404, "Not Found");
    	statusCodeToDescription.put(400, "Bad Request");
    }
    
    public static Request parseRequest(Socket socket) throws IOException {
    	InputStreamReader reader = new InputStreamReader(socket.getInputStream());
		BufferedReader in = new BufferedReader(reader);
		
		Map<String, String> pre = new HashMap<>();
		Map<String, List<String>> parms = new HashMap<>();
		Map<String, String> headers = new HashMap<>();
		HttpParsing.decodeHeader(in, pre, parms, headers);
		
		return new HttpRequest(pre, parms, headers);
    }

    /**
     * Sends an exception back, in the form of an HTTP response code and message.
     * Returns true if we are supposed to keep the connection open (for persistent
     * connections).
     */
    public static boolean sendException(Socket socket, Request request, HaltException except) {
        return true;
    }

    /**
     * Sends data back. Returns true if we are supposed to keep the connection open
     * (for persistent connections).
     * @throws IOException 
     */
    public static boolean sendResponse(Socket socket, Request request, Response response) throws IOException {
    	String protocol = request.protocol();
    	int responseStatus = response.status();
    	String statusDescription = statusCodeToDescription.get(responseStatus);
    	String firstResponseLine = String.format("%s %d %s \r\n", protocol, responseStatus, statusDescription);
    	
    	DateTimeFormatter httpDateFormatter = DateTimeFormatter
				.ofPattern("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH)
				.withZone(ZoneId.of("GMT"));
    	LocalDateTime cur = LocalDateTime.now();
    	String dateString = httpDateFormatter.format(cur);
    	String dateResponseLine = String.format("Date: %s \r\n", dateString);
    	
    	String contentTypeLine = "";
    	String contentLengthLine = "";
    	String bodyTextLine = "";
    	if (response.bodyRaw().length > 0) {
    		contentTypeLine = String.format("Content-Type: %s \r\n", response.type());
    		contentLengthLine = String.format("Content-Length: %s \r\n", response.bodyRaw().length);
    		if (response.type().contains("text")) {
    			bodyTextLine = response.body();
    		}
    	}
    	
    	String responseString = String.format("%s%s%s%s%s", 
    			firstResponseLine, 
    			dateResponseLine, 
    			contentTypeLine, 
    			contentLengthLine,
    			bodyTextLine);
    	PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
    	out.println(responseString);
    	
        return request.persistentConnection();
    }
}
