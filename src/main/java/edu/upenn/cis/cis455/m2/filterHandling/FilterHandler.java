package edu.upenn.cis.cis455.m2.filterHandling;

import edu.upenn.cis.cis455.m2.interfaces.Filter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FilterHandler {
    
    private final Map<String, List<Filter>> pathsToFilters;

    public FilterHandler() {
        this.pathsToFilters = new HashMap<>();
    }

    public Map<String, List<Filter>> getMatchingRegisteredPaths(String path) {
        Map<String, List<Filter>> registeredPathsToFilterLists = new HashMap<>();
        synchronized (pathsToFilters) {
            for (String registeredPath : pathsToFilters.keySet()) {
                if (matchPathToStepsAndPopulateParams(path, registeredPath, new HashMap<>())) {
                    registeredPathsToFilterLists.put(registeredPath, pathsToFilters.get(registeredPath));
                }
            }
        }
        return registeredPathsToFilterLists;
    }

    public void addFilter(String path, Filter filter) {
        synchronized (pathsToFilters) {
            List<Filter> filtersList;
            if (pathsToFilters.get(path) != null) {
                filtersList = pathsToFilters.get(path);
            } else {
                filtersList = new ArrayList<>();
                pathsToFilters.put(path, filtersList);
            }
            filtersList.add(filter);
        }
    }

    public static boolean matchPathToStepsAndPopulateParams(String pathToMatch, String registeredPath, Map<String, String> pathParams) {
        String[] pathStepsToMatch = convertPathToSteps(pathToMatch);
        String[] registeredPathSteps = convertPathToSteps(registeredPath);

        if (pathStepsToMatch.length != registeredPathSteps.length) {
            return false;
        }

        for (int i = 0; i < registeredPathSteps.length; i++) {
            String registeredPathStep = registeredPathSteps[i];
            String stepToMatch = pathStepsToMatch[i];
            if (isVariablePathStep(registeredPathStep)) {
                if (registeredPathStep.startsWith(":")) pathParams.put(registeredPathStep, stepToMatch);
            } else {
                if (!registeredPathStep.equals(stepToMatch)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static String[] convertPathToSteps(String path) {
        String[] pathSteps = path.split("/");
        return pathSteps.length > 0 ? pathSteps : new String[]{""};
    }

    private static boolean isVariablePathStep(String step) {
        return step.startsWith(":") || step.equals("*");
    }
}
