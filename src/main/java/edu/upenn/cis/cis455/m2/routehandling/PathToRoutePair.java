package edu.upenn.cis.cis455.m2.routehandling;

import edu.upenn.cis.cis455.m2.interfaces.Route;

import java.util.Map;

public class PathToRoutePair {

    private final String[] pathSteps;
    private final Route route;

    public PathToRoutePair(String path, Route route) {
        this.pathSteps = convertPathToSteps(path);
        this.route = route;
    }

    public Route getRoute() {
        return route;
    }

    public boolean matchPathToSteps(String path, Map<String, String> pathParams) {
        String[] pathStepsToMatch = convertPathToSteps(path);

        if (this.pathSteps.length != pathStepsToMatch.length) {
            return false;
        }

        for (int i = 0; i < this.pathSteps.length; i++) {
            String step = this.pathSteps[i];
            String stepToMatch = pathStepsToMatch[i];
            if (isVariablePathStep(step)) {
                if (step.startsWith(":")) pathParams.put(step, stepToMatch);
            } else {
                if (!step.equals(stepToMatch)) {
                    return false;
                }
            }
        }
        return true;
    }

    private String[] convertPathToSteps(String path) {
        String[] pathSteps = path.split("/");
        return pathSteps.length > 0 ? pathSteps : new String[]{""};
    }

    private boolean isVariablePathStep(String step) {
        return step.startsWith(":") || step.equals("*");
    }
}
