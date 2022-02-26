package edu.upenn.cis.cis455.m1.handling;

import edu.upenn.cis.cis455.exceptions.HaltException;
import edu.upenn.cis.cis455.m2.core.HttpRequest;
import edu.upenn.cis.cis455.m2.core.HttpResponse;
import edu.upenn.cis.cis455.m2.interfaces.Request;
import edu.upenn.cis.cis455.m2.interfaces.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Handles marshaling between HTTP Requests and Responses
 */
public class HttpIoHandler {
    final static Logger logger = LogManager.getLogger(HttpIoHandler.class);
    
    final static Map<Integer, String> statusCodeToDescription;
    
    static {
    	statusCodeToDescription = new HashMap<>();
    	statusCodeToDescription.put(200, "OK");
	    statusCodeToDescription.put(300, "Multiple Choice");
	    statusCodeToDescription.put(301, "Moved Permanently");
	    statusCodeToDescription.put(302, "Found");
	    statusCodeToDescription.put(303, "See Other");
	    statusCodeToDescription.put(304, "Not Modified");
	    statusCodeToDescription.put(307, "Temporary Redirect");
	    statusCodeToDescription.put(308, "Permanent Redirect");
		statusCodeToDescription.put(412, "Precondition Failed");
    	statusCodeToDescription.put(404, "Not Found");
	    statusCodeToDescription.put(403, "Forbidden");
    	statusCodeToDescription.put(400, "Bad Request");
	    statusCodeToDescription.put(500, "Internal Server Error");
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

	    String host = socket.getInetAddress().getHostName();
		int port = socket.getLocalPort();
		String ip = socket.getInetAddress().getHostAddress();

		String body = "";
	    char[] bodyChars;
		if (headers.get("content-length") != null) {
			try {
				int bodyLength = Integer.parseInt(headers.get("content-length"));
				bodyChars = new char[bodyLength];
				in.read(bodyChars, 0, bodyLength);
				body = String.valueOf(bodyChars);
			} catch (NumberFormatException e) {
				logger.error("Content length passed was not a valid number", e);
			}
		}


	    return buildRequest(
				host,
			    port,
			    ip,
				body,
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
		Response response = new HttpResponse(new HashMap<>());
		response.status(except.statusCode());

		String responseString = generateResponse(response);

		try {
			OutputStream outputStream = socket.getOutputStream();
			outputStream.write(responseString.getBytes(StandardCharsets.UTF_8));
		} catch (IOException e) {
			logger.error("Error writing exception to socket", e);
		}

	    return request != null && request.persistentConnection();
    }

    /**
     * Sends data back. Returns true if we are supposed to keep the connection open
     * (for persistent connections).
     * @throws IOException 
     */
    public static boolean sendResponse(Socket socket, Request request, Response response) {
		String responseString = generateResponse(response);

		try {
			OutputStream outputStream = socket.getOutputStream();
			outputStream.write(responseString.getBytes(StandardCharsets.UTF_8));
			if (response.bodyRaw() != null) outputStream.write(response.bodyRaw());
		} catch (IOException e) {
			logger.error("Error writing http response to socket", e);
		}

	    return request != null && request.persistentConnection();
    }

	private static Request buildRequest(
			String host,
			int port,
			String ip,
			String body,
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
				ip,
				body
		);
	}

	private static String generateResponse(Response res) {
		if (res == null) {
			logger.error("Attempted to generate a response for a null Response object");
			return "";
		}
		String out = "";
		out += firstResponseLine(res.status());
		out += dateResponseLine();
		out += res.getHeaders();
		out += "\r\n";
		return out;
	}

	private static String firstResponseLine(int statusCode) {
		String protocol = "HTTP/1.1";
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
