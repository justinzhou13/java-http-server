package edu.upenn.cis.cis455.m1.handling;

import edu.upenn.cis.cis455.m1.interfaces.Request;
import edu.upenn.cis.cis455.m1.interfaces.Response;
import edu.upenn.cis.cis455.m1.interfaces.Route;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

import static edu.upenn.cis.cis455.m1.handling.HttpComplianceHandler.isCompliant;

public class RouteOrchestrator {

	static final Logger logger = LogManager.getLogger(RouteOrchestrator.class);

	private final Map<String, Map<String, Route>> routes;

	private final GetFileRoute getFileRoute;

	public RouteOrchestrator(String root) {
		this.getFileRoute = new GetFileRoute(root);
		this.routes = new HashMap<>();
		routes.put("GET", new HashMap<>());
		routes.put("POST", new HashMap<>());
		routes.put("PUT", new HashMap<>());
		routes.put("HEAD", new HashMap<>());
		routes.put("DELETE", new HashMap<>());
		routes.put("OPTIONS", new HashMap<>());
	}

	public void applyRoutes(Request req, Response res) {
		logger.info(String.format("Requested %s", req.uri()));
		if (isCompliant(req, res)) {
			boolean lookingForFile = req.requestMethod().equals("GET")
					&& !routes.get("GET").containsKey(req.uri());
			if (lookingForFile) {
				handleFileRequest(req, res);
			} else {
				boolean routeExists = routes.get(req.requestMethod()).containsKey(req.uri());
				if (routeExists) {
					Route route = routes.get(req.requestMethod()).get(req.uri());
					try {
						route.handle(req, res);
					} catch (Exception e){
						logger.error(e.getMessage());
					}
				} else {
					//TODO 404 here
				}
			}
		}
	}

	public void handleFileRequest(Request req, Response res) {
		getFileRoute.handle(req, res);
	}

	public void addRoute(String httpMethod, String uri, Route route) {
		try {
			logger.info(String.format("attempting to add new route to worker for method %s and uri %s",
							httpMethod,
							uri));
			routes.get(httpMethod).put(uri, route);
		} catch (NullPointerException e) {
			logger.error("Attempted to add a route that wasn't a valid HTTP method");
		}
	}
}
