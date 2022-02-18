package edu.upenn.cis.cis455.m2.routehandling;

import edu.upenn.cis.cis455.m2.interfaces.Route;
import edu.upenn.cis.cis455.m2.server.WebService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class PathStep {

    final static Logger logger = LogManager.getLogger(WebService.class);

    private String pathValue;
    private Route routeAssigned;
    private Map<String, PathStep> children;

    public PathStep(String pathValue) {
        this.pathValue = pathValue;
        this.children = new HashMap<>();
    }

    public String getPathValue() {
        return pathValue;
    }

    public void setPathValue(String pathValue) {
        this.pathValue = pathValue;
    }

    public Route getRouteAssigned() {
        return routeAssigned;
    }

    public void setRouteAssigned(Route routeAssigned) {
        this.routeAssigned = routeAssigned;
    }

    public void addRoutePath(String[] steps, int curIndex, Route route) {
        if (curIndex <= steps.length - 1) {
            String curPathValue = steps[curIndex];
            if (curIndex == steps.length - 1) {
                if (this.pathValue.equals(curPathValue)) {
                    this.routeAssigned = route;
                    return;
                }
                logger.error("Stepped into Pathstep with incorrect pathvalue");
            }

            curIndex++;
            String nextPathValue = steps[curIndex];
            PathStep nextPathStep;
            if (this.children.containsKey(nextPathValue) && this.children.get(nextPathValue) != null) {
                nextPathStep = this.children.get(nextPathValue);
            } else {
                nextPathStep = new PathStep(nextPathValue);
                this.children.put(nextPathValue, nextPathStep);
            }
            nextPathStep.addRoutePath(steps, curIndex, route);
        } else {
            logger.error("Trying to add a path at a depth beyond what was specified");
        }
    }

    public Route getRoutePath(String[] steps, int curIndex) {
        if (curIndex <= steps.length - 1) {
            String curPathValue = steps[curIndex];
            if (curIndex == steps.length - 1) {
                if (this.pathValue.equals(curPathValue)) {
                    return this.routeAssigned;
                }
                return null;
            }

            curIndex++;
            String nextPathValue = steps[curIndex];
            if (this.children.containsKey(nextPathValue) && this.children.get(nextPathValue) != null) {
                return this.children.get(nextPathValue).getRoutePath(steps, curIndex);
            }
        }
        return null;
    }

    @Override
    public int hashCode() {
        return pathValue.hashCode();
    }
}
