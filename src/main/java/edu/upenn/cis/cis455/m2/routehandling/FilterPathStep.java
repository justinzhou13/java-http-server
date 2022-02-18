package edu.upenn.cis.cis455.m2.routehandling;

import edu.upenn.cis.cis455.m2.interfaces.Filter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FilterPathStep {

    final static Logger logger = LogManager.getLogger(FilterPathStep.class);

    private String pathStepValue;
    private String[] fullPath;
    private List<Filter> filtersAssigned;
    private Map<String, FilterPathStep> children;
    private Map<String, FilterPathStep> variablePathChildren;

    public FilterPathStep(String pathStepValue) {
        this.pathStepValue = pathStepValue;
        this.fullPath = new String[0];
        this.filtersAssigned = new ArrayList<>();
        this.children = new HashMap<>();
        this.variablePathChildren = new HashMap<>();
    }

    public String getPathStepValue() {
        return pathStepValue;
    }

    public void setPathStepValue(String pathStepValue) {
        this.pathStepValue = pathStepValue;
    }


    public void addFilterByPath(String[] steps, int curIndex, Filter filter) {
        if (curIndex <= steps.length - 1) {
            String curPathValue = steps[curIndex];
            if (curIndex == steps.length - 1) {
                if (this.pathStepValue.equals(curPathValue)) {
                    this.filtersAssigned.add(filter);
                    this.fullPath = steps;
                    return;
                }
                logger.error("Stepped into Pathstep with incorrect pathvalue");
            }

            curIndex++;
            String nextPathValue = steps[curIndex];
            FilterPathStep nextPathStep;

            if (isVariablePathStep(nextPathValue)) {
                if (this.variablePathChildren.containsKey(nextPathValue) && this.variablePathChildren.get(nextPathValue) != null) {
                    nextPathStep = this.variablePathChildren.get(nextPathValue);
                } else {
                    nextPathStep = new FilterPathStep(nextPathValue);
                    this.variablePathChildren.put(nextPathValue, nextPathStep);
                }
            } else {
                if (this.children.containsKey(nextPathValue) && this.children.get(nextPathValue) != null) {
                    nextPathStep = this.children.get(nextPathValue);
                } else {
                    nextPathStep = new FilterPathStep(nextPathValue);
                    this.children.put(nextPathValue, nextPathStep);
                }
            }
            nextPathStep.addFilterByPath(steps, curIndex, filter);
        } else {
            logger.error("Trying to add a path at a depth beyond what was specified");
        }
    }

    public List<Filter> getFilterByPath(String[] steps, int curIndex, Map<String, String> params) {
        if (curIndex <= steps.length - 1) {
            String curPathValue = steps[curIndex];
            if (curIndex == steps.length - 1) {
                if (this.pathStepValue.equals(curPathValue) || isVariablePathStep(this.pathStepValue)) {
                    fillPathParameters(params, steps);
                    return this.filtersAssigned;
                }
                return null;
            }

            curIndex++;
            String nextPathValue = steps[curIndex];
            if (this.children.containsKey(nextPathValue) && this.children.get(nextPathValue) != null) {
                return this.children.get(nextPathValue).getFilterByPath(steps, curIndex, params);
            } else {
                for (FilterPathStep variableStep : this.variablePathChildren.values()) {
                    List<Filter> dfsFilters = variableStep.getFilterByPath(steps, curIndex, params);
                    if (dfsFilters != null) {
                        return dfsFilters;
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
