package edu.upenn.cis.cis455.m1.handling;

import edu.upenn.cis.cis455.exceptions.HaltException;
import edu.upenn.cis.cis455.m2.core.HttpRequest;
import edu.upenn.cis.cis455.m2.core.HttpResponse;
import edu.upenn.cis.cis455.m2.filterHandling.FilterHandler;
import edu.upenn.cis.cis455.m2.interfaces.*;
import edu.upenn.cis.cis455.m2.routehandling.GetFileRoute;
import edu.upenn.cis.cis455.m2.routehandling.RouteHandler;
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

	private static final FilterHandler beforeFilterHandler;
	private static final FilterHandler afterFilterHandler;

	private static final Map<String, RouteHandler> routeHandlersByMethod;

	private static final GetFileRoute getFileRoute = new GetFileRoute();

	static {
		beforeFilters = new ArrayList<>();
		beforeFilters.add(((request, response) -> {
			if (request.requestMethod().equals("HEAD")) {
				((HttpResponse) response).setHeadResponse(true);
			}
		}));

		afterFilters = new ArrayList<>();
		afterFilters.add(((request, response) -> {
			Session session = request.session(false);
			if (session != null) {
				response.cookie("JSESSIONID", session.id());
			}
		}));

		beforeFilterHandler = new FilterHandler();
		afterFilterHandler = new FilterHandler();

		routeHandlersByMethod = new HashMap<>();
		routeHandlersByMethod.put("GET", new RouteHandler());
		routeHandlersByMethod.put("HEAD", new RouteHandler());
	}

	public static void setRootDirectory(String root) {
		synchronized (getFileRoute) {
			getFileRoute.setRoot(root);
		}
	}

	//TODO replace map of routes with route tree
	public static void applyRoutes(Request req, Response res) throws Exception {
		logger.info(String.format("Requested %s", req.uri()));

		checkProtocolSupported(req);
		if (isCompliant(req, res)) {
			applyBeforeFilters(req, res);

			String requestMethod = req.requestMethod().equals("HEAD") ? "GET" : req.requestMethod();

			synchronized (routeHandlersByMethod) {
				if (!routeHandlersByMethod.containsKey(req.requestMethod())) throw new HaltException(501);
			}

			Map<String, String> pathParams = new HashMap<>();
			List<String> splat = new ArrayList<>();
			Route requestedRoute = getRoute(requestMethod, req.pathInfo(), pathParams, splat);
			((HttpRequest) req).setPathParams(pathParams);
			try {
				((HttpRequest) req).setSplat((String[]) splat.toArray());
			} catch (ClassCastException e) {
				logger.error(e);
				throw new HaltException(500);
			}


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
		synchronized (getFileRoute) {
			getFileRoute.handle(req, res);
		}
	}

	public static void addRoute(String httpMethod, String path, Route route) {
		synchronized (routeHandlersByMethod) {
			RouteHandler routeHandler;
			if (!routeHandlersByMethod.containsKey(httpMethod)) {
				routeHandler = new RouteHandler();
				routeHandlersByMethod.put(httpMethod, routeHandler);
			} else  {
				routeHandler = routeHandlersByMethod.get(httpMethod);
			}
			routeHandler.addRoute(path, route);
		}
	}

	public static Route getRoute(String httpMethod, String path, Map<String, String> params, List<String> splat) {
		RouteHandler routeHandler;

		synchronized (routeHandlersByMethod) {
			if (!routeHandlersByMethod.containsKey(httpMethod)) {
				throw new HaltException(501);
			}
			routeHandler = routeHandlersByMethod.get(httpMethod);
		}

		if (routeHandler != null) {
			return routeHandler.getRoute(path, params, splat);
		}

		return null;
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

	public static void addBeforeFilterByPath(String path, Filter filter) {
		synchronized (beforeFilterHandler) {
			beforeFilterHandler.addFilter(path, filter);
		}
	}

	public static void addAfterFilterByPath(String path, Filter filter) {
		synchronized (afterFilterHandler) {
			afterFilterHandler.addFilter(path, filter);
		}
	}

	private static void applyBeforeFilters(Request request, Response response) throws Exception {
		synchronized (beforeFilters) {
			applyFiltersList(beforeFilters, new HashMap<>(), request, response);
		}

		Map<String, List<Filter>> matchingFilters;
		synchronized (beforeFilterHandler) {
			matchingFilters = beforeFilterHandler.getMatchingRegisteredPaths(request.pathInfo());
		}

		for (Map.Entry<String, List<Filter>> filterList : matchingFilters.entrySet()) {
			Map<String, String> pathParamsForRegisteredPath = new HashMap<>();
			FilterHandler.matchPathToStepsAndPopulateParams(request.pathInfo(), filterList.getKey(), pathParamsForRegisteredPath);
			((HttpRequest) request).setPathParams(pathParamsForRegisteredPath);

			applyFiltersList(filterList.getValue(), pathParamsForRegisteredPath, request, response);
		}
	}

	private static void applyAfterFilters(Request request, Response response) throws Exception {
		synchronized (afterFilters) {
			applyFiltersList(afterFilters, new HashMap<>(), request, response);
		}

		Map<String, List<Filter>> matchingFilters;
		synchronized (afterFilterHandler) {
			matchingFilters = afterFilterHandler.getMatchingRegisteredPaths(request.pathInfo());
		}

		for (Map.Entry<String, List<Filter>> filterList : matchingFilters.entrySet()) {
			Map<String, String> pathParamsForRegisteredPath = new HashMap<>();
			FilterHandler.matchPathToStepsAndPopulateParams(request.pathInfo(), filterList.getKey(), pathParamsForRegisteredPath);
			((HttpRequest) request).setPathParams(pathParamsForRegisteredPath);

			applyFiltersList(filterList.getValue(), pathParamsForRegisteredPath, request, response);
		}
	}

	private static void checkProtocolSupported(Request req) {
		if (!(req.protocol().equals("HTTP/1.0") || req.protocol().equals("HTTP/1.1"))) {
			throw new HaltException(505);
		}
	}

	private static void applyFiltersList(
			List<Filter> filtersList,
			Map<String, String> pathParams,
			Request request,
			Response response) throws Exception {

		if (filtersList == null) {
			return;
		}

		((HttpRequest) request).setPathParams(pathParams);
		for (Filter filter : filtersList) {
			filter.handle(request, response);
		}
	}
}
