package edu.upenn.cis.cis455.m2.routehandling;

import edu.upenn.cis.cis455.m2.interfaces.Route;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class PathStep {

    final static Logger logger = LogManager.getLogger(PathStep.class);

    private String pathStepValue;
    private String[] fullPath;
    private Route routeAssigned;
    private Map<String, PathStep> children;
    private Map<String, PathStep> variablePathChildren;

    public PathStep(String pathStepValue) {
        this.pathStepValue = pathStepValue;
        this.fullPath = new String[0];
        this.children = new HashMap<>();
        this.variablePathChildren = new HashMap<>();
    }

    public String getPathStepValue() {
        return pathStepValue;
    }

    public void setPathStepValue(String pathStepValue) {
        this.pathStepValue = pathStepValue;
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
                if (this.pathStepValue.equals(curPathValue)) {
                    this.routeAssigned = route;
                    this.fullPath = steps;
                    return;
                }
                logger.error("Stepped into Pathstep with incorrect pathvalue");
            }

            curIndex++;
            String nextPathValue = steps[curIndex];
            PathStep nextPathStep;

            if (isVariablePathStep(nextPathValue)) {
                if (this.variablePathChildren.containsKey(nextPathValue) && this.variablePathChildren.get(nextPathValue) != null) {
                    nextPathStep = this.variablePathChildren.get(nextPathValue);
                } else {
                    nextPathStep = new PathStep(nextPathValue);
                    this.variablePathChildren.put(nextPathValue, nextPathStep);
                }
            } else {
                if (this.children.containsKey(nextPathValue) && this.children.get(nextPathValue) != null) {
                    nextPathStep = this.children.get(nextPathValue);
                } else {
                    nextPathStep = new PathStep(nextPathValue);
                    this.children.put(nextPathValue, nextPathStep);
                }
            }
            nextPathStep.addRoutePath(steps, curIndex, route);
        } else {
            logger.error("Trying to add a path at a depth beyond what was specified");
        }
    }

    public Route getRoutePath(String[] steps, int curIndex, Map<String, String> params) {
        if (curIndex <= steps.length - 1) {
            String curPathValue = steps[curIndex];
            if (curIndex == steps.length - 1) {
                if (this.pathStepValue.equals(curPathValue) || isVariablePathStep(this.pathStepValue)) {
                    fillPathParameters(params, steps);
                    return this.routeAssigned;
                }
                return null;
            }

            curIndex++;
            String nextPathValue = steps[curIndex];
            if (this.children.containsKey(nextPathValue) && this.children.get(nextPathValue) != null) {
                return this.children.get(nextPathValue).getRoutePath(steps, curIndex, params);
            } else {
                for (PathStep variableStep : this.variablePathChildren.values()) {
                    Route dfsRoute = variableStep.getRoutePath(steps, curIndex, params);
                    if (dfsRoute != null) {
                        return dfsRoute;
                    }
                }
            }
        }
        return null;
    }

    private boolean isVariablePathStep(String step) {
        return step.startsWith(":") || step.equals("*");
    }

    private void fillPathParameters(Map<String, String> params, String[] steps) {
        for (int i = 0; i < this.fullPath.length; i++) {
            String curRoutePathStep = this.fullPath[i];
            if (curRoutePathStep.startsWith(":")) {
                params.put(curRoutePathStep, steps[i]);
            }
        }
    }

    @Override
    public int hashCode() {
        return pathStepValue.hashCode();
    }
}
