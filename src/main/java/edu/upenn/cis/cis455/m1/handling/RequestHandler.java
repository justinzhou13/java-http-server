package edu.upenn.cis.cis455.m1.handling;

import edu.upenn.cis.cis455.exceptions.HaltException;
import edu.upenn.cis.cis455.m1.interfaces.Request;
import edu.upenn.cis.cis455.m1.interfaces.Response;
import edu.upenn.cis.cis455.m1.interfaces.Route;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

import static edu.upenn.cis.cis455.m1.handling.HttpComplianceHandler.isCompliant;

public class RequestHandler {

	static final Logger logger = LogManager.getLogger(RequestHandler.class);

	private static final Map<String, Map<String, Route>> routes;

	private static final GetFileRoute getFileRoute = new GetFileRoute();

	static {
		routes = new HashMap<>();
		routes.put("GET", new HashMap<>());
		routes.put("HEAD", new HashMap<>());
	}

	public static void setRootDirectory(String root) {
		getFileRoute.setRoot(root);
	}

	public static void applyRoutes(Request req, Response res) throws HaltException {
		logger.info(String.format("Requested %s", req.uri()));
		if (isCompliant(req, res)) {
			String requestMethod = req.requestMethod().equals("HEAD") ? "GET" : req.requestMethod();
			boolean lookingForFile = requestMethod.equals("GET")
					&& !routes.get("GET").containsKey(req.pathInfo());
			if (lookingForFile) {
				handleFileRequest(req, res);
				return;
			}

			Map<String, Route> routesForMethod = routes.get(requestMethod);
			if (routesForMethod == null) throw new HaltException(501);

			Route route = routesForMethod.get(req.pathInfo());
			if (route == null) throw new HaltException(404);

			try {
				route.handle(req, res);
			} catch (Exception e){
				logger.error(e.getMessage());
				throw new HaltException(500);
			}
		}
	}

	public static void handleFileRequest(Request req, Response res) {
		getFileRoute.handle(req, res);
	}

	public static void addRoute(String httpMethod, String uri, Route route) {
		try {
			logger.info(String.format("attempting to add new route to worker for method %s and uri %s",
							httpMethod,
							uri));
			synchronized (routes) {
				routes.get(httpMethod).put(uri, route);
			}
		} catch (NullPointerException e) {
			logger.error("Attempted to add a route that wasn't a valid HTTP method");
		}
	}
}
