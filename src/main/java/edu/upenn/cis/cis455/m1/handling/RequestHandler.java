package edu.upenn.cis.cis455.m1.handling;

import edu.upenn.cis.cis455.exceptions.HaltException;
import edu.upenn.cis.cis455.m2.interfaces.Filter;
import edu.upenn.cis.cis455.m2.interfaces.Request;
import edu.upenn.cis.cis455.m2.interfaces.Response;
import edu.upenn.cis.cis455.m2.interfaces.Route;
import edu.upenn.cis.cis455.m2.routehandling.GetFileRoute;
import edu.upenn.cis.cis455.m2.routehandling.PathStep;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static edu.upenn.cis.cis455.m1.handling.HttpComplianceHandler.isCompliant;

public class RequestHandler {

	static final Logger logger = LogManager.getLogger(RequestHandler.class);

	private static final List<Filter> beforeFilters;
	private static final List<Filter> afterFilters;
	private static final Map<String, PathStep> routeTrees;

	private static final GetFileRoute getFileRoute = new GetFileRoute();

	static {
		routeTrees = new HashMap<>();
		routeTrees.put("GET", new PathStep(""));
		routeTrees.put("HEAD", new PathStep(""));

		beforeFilters = new ArrayList<>();
		afterFilters = new ArrayList<>();
		afterFilters.add(((request, response) -> {
			if (request.requestMethod().equals("HEAD")) {
				response.bodyRaw(null);
			}
		}));
	}

	public static void setRootDirectory(String root) {
		getFileRoute.setRoot(root);
	}

	//TODO replace map of routes with route tree
	public static void applyRoutes(Request req, Response res) throws Exception {
		logger.info(String.format("Requested %s", req.uri()));

		checkProtocolSupported(req);
		if (isCompliant(req, res)) {
			applyBeforeFilters(req, res);

			String requestMethod = req.requestMethod().equals("HEAD") ? "GET" : req.requestMethod();
			PathStep routeTreeRoot = routeTrees.get(requestMethod);
			if (routeTreeRoot == null) throw new HaltException(501);

			Map<String, String> pathParams = new HashMap<>();
			Route requestedRoute = getRoute("GET", req.pathInfo(), pathParams);
			((HttpRequest) req).setPathParams(pathParams);

			boolean lookingForFile = requestMethod.equals("GET") && requestedRoute == null;
			if (lookingForFile) {
				handleFileRequest(req, res);
			} else {
				if (requestedRoute == null) throw new HaltException(404);

				try {
					Object routeResult = requestedRoute.handle(req, res);
					if (routeResult != null) {
						res.body(routeResult.toString());
					}
				} catch (Exception e){
					logger.error(e.getMessage());
					throw new HaltException(500);
				}
			}
		}
		//TODO figure out if this is actually the desired behavior
		applyAfterFilters(req, res);
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

	private static void applyBeforeFilters(Request request, Response response) throws Exception {
		synchronized (beforeFilters) {
			for (Filter filter : beforeFilters) {
				filter.handle(request, response);
			}
		}
	}

	private static void applyAfterFilters(Request request, Response response) throws Exception {
		synchronized (afterFilters) {
			for (Filter filter : afterFilters) {
				filter.handle(request, response);
			}
		}
	}

	public static void addBeforeFilter(Filter filter) {
		synchronized (beforeFilters) {
			beforeFilters.add(filter);
		}
	}

	public static void addAfterFilter(Filter filter) {
		synchronized (afterFilters) {
			afterFilters.add(filter);
		}
	}
}
