package edu.upenn.cis.cis455.m2.routehandling;

import edu.upenn.cis.cis455.m2.interfaces.Route;

import java.util.List;
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

    public boolean matchPathToSteps(String path, Map<String, String> pathParams, List<String> splat) {
        return matchPathToSteps(convertPathToSteps(path), this.pathSteps, pathParams, splat);
    }

    public static boolean matchPathToSteps(String pathToMatch, String definedPath, Map<String, String> pathParams, List<String> splat) {
        String[] pathStepsToMatch = convertPathToSteps(pathToMatch);
        String[] definedPathSteps = convertPathToSteps(definedPath);
        return matchPathToSteps(pathStepsToMatch, definedPathSteps, pathParams, splat);
    }

    private static boolean matchPathToSteps(
            String[] pathStepsToMatch, String[] definedPathSteps, Map<String, String> pathParams, List<String> splat) {

        if (definedPathSteps.length > pathStepsToMatch.length) {
            return false;
        }

        String definedStep = "";
        for (int i = 0; i < definedPathSteps.length; i++) {
            definedStep = definedPathSteps[i];
            String stepToMatch = pathStepsToMatch[i];
            if (isVariablePathStep(definedStep)) {
                if (definedStep.startsWith(":")) pathParams.put(definedStep, stepToMatch);
                if (definedStep.equals("*") && i < definedPathSteps.length - 1) splat.add(stepToMatch);
            } else {
                if (!definedStep.equals(stepToMatch)) {
                    return false;
                }
            }
        }

        if (definedStep.equals("*")) {
            StringBuilder lastSplat = new StringBuilder(pathStepsToMatch[definedPathSteps.length - 1]);
            for (int i = definedPathSteps.length; i < pathStepsToMatch.length; i++) {
                lastSplat.append("/").append(pathStepsToMatch[i]);
            }
            splat.add(lastSplat.toString());
        }

        return definedStep.equals("*") || definedPathSteps.length == pathStepsToMatch.length;
    }

    private static String[] convertPathToSteps(String path) {
        String[] pathSteps = path.split("/");
        return pathSteps.length > 0 ? pathSteps : new String[]{""};
    }

    private static boolean isVariablePathStep(String step) {
        return step.startsWith(":") || step.equals("*");
    }
}
