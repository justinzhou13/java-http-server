package edu.upenn.cis.cis455.m1.handling;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

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
	    statusCodeToDescription.put(304, "Not Modified");
		statusCodeToDescription.put(412, "Precondition Failed");
    	statusCodeToDescription.put(404, "Not Found");
    	statusCodeToDescription.put(400, "Bad Request");
	    statusCodeToDescription.put(501, "Not Implemented");
	    statusCodeToDescription.put(505, "HTTP Version Not Supported");
    }
    
    public static Request parseRequest(Socket socket) throws IOException, HaltException {
    	InputStreamReader reader = new InputStreamReader(socket.getInputStream());
		BufferedReader in = new BufferedReader(reader);
		
		Map<String, String> pre = new HashMap<>();
		Map<String, List<String>> parms = new HashMap<>();
		Map<String, String> headers = new HashMap<>();
		HttpParsing.decodeHeader(in, pre, parms, headers);
	    String host = socket.getInetAddress().getCanonicalHostName();
		int port = socket.getLocalPort();
		String ip = socket.getInetAddress().getHostAddress();
		
	    return buildRequest(
				host,
			    port,
			    ip,
			    pre,
			    parms,
			    headers);
    }

    /**
     * Sends an exception back, in the form of an HTTP response code and message.
     * Returns true if we are supposed to keep the connection open (for persistent
     * connections).
     */
    public static boolean sendException(Socket socket, Request request, HaltException except) {
		String firstResponseLine = firstResponseLine(request, except.statusCode());
		String dateResponseLine = dateResponseLine();

		String responseString = String.format("%s%s", firstResponseLine, dateResponseLine);

		try {
			OutputStream outputStream = socket.getOutputStream();
			outputStream.write(responseString.getBytes(StandardCharsets.UTF_8));
		} catch (IOException e) {
			logger.error("Error writing exception to socket");
		}

	    return request != null && request.persistentConnection();
    }

    /**
     * Sends data back. Returns true if we are supposed to keep the connection open
     * (for persistent connections).
     * @throws IOException 
     */
    public static boolean sendResponse(Socket socket, Request request, Response response) {
    	int responseStatus = response.status();
    	String firstResponseLine = firstResponseLine(request, responseStatus);
    	String dateResponseLine = dateResponseLine();
    	
    	String contentTypeLine = "";
    	String contentLengthLine = "";
    	String bodyTextLine = "";
    	if (response.bodyRaw() != null && response.bodyRaw().length > 0) {
    		contentTypeLine = String.format("Content-Type: %s \r\n", response.type());
    		contentLengthLine = String.format("Content-Length: %s \r\n", response.bodyRaw().length);
    	}
    	
    	String responseString = String.format("%s%s%s%s\r\n%s",
    			firstResponseLine, 
    			dateResponseLine, 
    			contentTypeLine, 
    			contentLengthLine,
    			bodyTextLine);

		try {
			OutputStream outputStream = socket.getOutputStream();
			outputStream.write(responseString.getBytes(StandardCharsets.UTF_8));

			if (response.bodyRaw() != null && !request.requestMethod().equals("HEAD")) {
				outputStream.write(response.bodyRaw());
			}
		} catch (IOException e) {
			logger.error("Error writing http response to socket");
		}

	    return request != null && request.persistentConnection();
    }

	private static Request buildRequest(
			String host,
			int port,
			String ip,
			Map<String, String> pre,
			Map<String, List<String>> parms,
			Map<String, String> headers) {
		return new HttpRequest(
				pre.get("method"),
				pre.get("protocolVersion"),
				pre.get("uri"),
				host,
				port,
				headers.get("user-agent"),
				headers,
				pre.get("queryString"),
				parms,
				ip
		);
	}

	private static String firstResponseLine(Request request, int statusCode) {
		String protocol = request != null ? request.protocol() : "HTTP/1.1";
		String statusDescription = statusCodeToDescription.get(statusCode);
		return String.format("%s %d %s \r\n", protocol, statusCode, statusDescription);
	}

	private static String dateResponseLine() {
		DateTimeFormatter httpDateFormatter = DateTimeFormatter
				.ofPattern("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH)
				.withZone(ZoneId.of("GMT"));
		LocalDateTime cur = LocalDateTime.now();
		String dateString = httpDateFormatter.format(cur);
		return String.format("Date: %s \r\n", dateString);
	}
}
