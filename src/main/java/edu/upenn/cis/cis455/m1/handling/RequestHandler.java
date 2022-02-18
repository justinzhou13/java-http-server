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

	private static final Map<String, PathStep> routeTrees;

	private static final GetFileRoute getFileRoute = new GetFileRoute();

	static {
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
			PathStep routeTreeRoot = routeTrees.get(requestMethod);
			if (routeTreeRoot == null) throw new HaltException(501);

			Map<String, String> pathParams = new HashMap<>();
			Route requestedRoute = getRoute("GET", req.pathInfo(), pathParams);
			boolean lookingForFile = requestMethod.equals("GET") && requestedRoute == null;
			if (lookingForFile) {
				handleFileRequest(req, res);
				return;
			}

			if (requestedRoute == null) throw new HaltException(404);

			try {
				requestedRoute.handle(req, res);
			} catch (Exception e){
				logger.error(e.getMessage());
				throw new HaltException(500);
			}
		}
	}

	public static void handleFileRequest(Request req, Response res) throws Exception {
		getFileRoute.handle(req, res);
	}

	public static void addRoute(String httpMethod, String path, Route route) {
		addRouteToTree(httpMethod, path, route);
	}

	public static void addRouteToTree(String httpMethod, String path, Route route) {
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

	public static Route getRoute(String httpMethod, String path, Map<String, String> params) {
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
