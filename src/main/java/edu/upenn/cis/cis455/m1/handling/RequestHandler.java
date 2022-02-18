package edu.upenn.cis.cis455.m1.handling;

import edu.upenn.cis.cis455.exceptions.HaltException;
import edu.upenn.cis.cis455.m2.interfaces.Request;
import edu.upenn.cis.cis455.m2.interfaces.Response;
import edu.upenn.cis.cis455.m2.interfaces.Route;
import edu.upenn.cis.cis455.m2.routehandling.GetFileRoute;
import edu.upenn.cis.cis455.m2.routehandling.PathStep;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

import static edu.upenn.cis.cis455.m1.handling.HttpComplianceHandler.isCompliant;

public class RequestHandler {

	static final Logger logger = LogManager.getLogger(RequestHandler.class);

	private static final Map<String, Map<String, Route>> routes;
	private static final Map<String, PathStep> routeTrees;

	private static final GetFileRoute getFileRoute = new GetFileRoute();

	static {
		routes = new HashMap<>();
		routes.put("GET", new HashMap<>());
		routes.put("HEAD", new HashMap<>());

		routeTrees = new HashMap<>();
		routeTrees.put("GET", new PathStep(""));
		routeTrees.put("HEAD", new PathStep(""));
	}

	public static void setRootDirectory(String root) {
		getFileRoute.setRoot(root);
	}

	//TODO replace map of routes with route tree
	public static void applyRoutes(Request req, Response res) throws Exception {
		logger.info(String.format("Requested %s", req.uri()));

		checkProtocolSupported(req);
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

	public static void handleFileRequest(Request req, Response res) throws Exception {
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

	public static void addRouteToTree(String httpMethod, String path, edu.upenn.cis.cis455.m2.interfaces.Route route) {
		synchronized (routeTrees) {
			PathStep root;
			if (!routeTrees.containsKey(httpMethod)) {
				root = new PathStep("");
				routeTrees.put(httpMethod, root);
			} else {
				root = routeTrees.get(httpMethod);
			}
			String[] pathSteps = path.split("/");
			pathSteps = pathSteps.length > 0 ? pathSteps : new String[]{""};
			root.addRoutePath(pathSteps, 0, route);
		}
	}

	public static edu.upenn.cis.cis455.m2.interfaces.Route getRoute(String httpMethod, String path, Map<String, String> params) {
		synchronized (routeTrees) {
			if (!routeTrees.containsKey(httpMethod)) {
				throw new HaltException(501);
			}
			PathStep root = routeTrees.get(httpMethod);
			String[] pathSteps = path.split("/");
			pathSteps = pathSteps.length > 0 ? pathSteps : new String[]{""};
			return root.getRoutePath(pathSteps, 0, params);
		}
	}

	private static void checkProtocolSupported(Request req) {
		if (!(req.protocol().equals("HTTP/1.0") || req.protocol().equals("HTTP/1.1"))) {
			throw new HaltException(505);
		}
	}
}
