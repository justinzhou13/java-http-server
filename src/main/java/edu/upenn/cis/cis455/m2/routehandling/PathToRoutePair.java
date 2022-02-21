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

    public static boolean matchPathToMultistepWildcard(String pathToMatch, String definedPath) {
        String[] pathToMatchSteps = convertPathToSteps(pathToMatch);
        String[] definedPathSteps = convertPathToSteps(definedPath);
        return matchPathStepsToWildcardPath(definedPathSteps, pathToMatchSteps);
    }

    private static boolean matchPathStepsToWildcardPath(String[] definedPathSteps, String[] pathToMatchSteps) {
        int definedPathIndex = 0;
        int pathToMatchIndex = 0;
        String definedPathStep = definedPathSteps[definedPathIndex];
        String pathToMatchStep = pathToMatchSteps[pathToMatchIndex];
        while (definedPathIndex < definedPathSteps.length - 1 && pathToMatchIndex < pathToMatchSteps.length - 1) {
            if (!definedPathStep.equals("*") && !definedPathStep.equals(pathToMatchStep)) {
                return false;
            } else if (!definedPathStep.equals("*")) {
                definedPathIndex++;
                definedPathStep = definedPathSteps[definedPathIndex];

                pathToMatchIndex++;
                pathToMatchStep = pathToMatchSteps[pathToMatchIndex];
            } else {
                do {
                    definedPathIndex++;
                    definedPathStep = definedPathSteps[definedPathIndex];

                    pathToMatchIndex++;
                    pathToMatchStep = pathToMatchSteps[pathToMatchIndex];
                } while (definedPathStep.equals("*") && definedPathIndex < definedPathSteps.length - 1);
                while (!definedPathStep.equals(pathToMatchStep) && pathToMatchIndex < pathToMatchSteps.length - 1) {
                    pathToMatchIndex++;
                    pathToMatchStep = pathToMatchSteps[pathToMatchIndex];
                }
            }
        }
        return definedPathIndex == definedPathSteps.length - 1 && (definedPathStep.equals("*") || (definedPathStep.equals(pathToMatchStep) && pathToMatchIndex == pathToMatchSteps.length - 1));
    }

    private static String[] convertPathToSteps(String path) {
        String[] pathSteps = path.split("/");
        return pathSteps.length > 0 ? pathSteps : new String[]{""};
    }

    private static boolean isVariablePathStep(String step) {
        return step.startsWith(":") || step.equals("*");
    }
}
