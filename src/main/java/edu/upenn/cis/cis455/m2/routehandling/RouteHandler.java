package edu.upenn.cis.cis455.m2.routehandling;

import edu.upenn.cis.cis455.m2.interfaces.Route;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RouteHandler {

    private final List<PathToRoutePair> routeList;

    public RouteHandler() {
        this.routeList = new ArrayList<>();
    }

    public Route getRoute(String path, Map<String, String> pathParams, List<String> splat) {
        synchronized (routeList) {
            for (PathToRoutePair pathToRoutePair : routeList) {
                pathParams.clear();
                splat.clear();
                if (pathToRoutePair.matchPathToSteps(path, pathParams, splat)) {
                    return pathToRoutePair.getRoute();
                }
            }
        }
        return null;
    }

    public void addRoute(String path, Route route) {
        PathToRoutePair pathToRoutePair = new PathToRoutePair(path, route);
        synchronized (routeList) {
            routeList.add(pathToRoutePair);
        }
    }
}
