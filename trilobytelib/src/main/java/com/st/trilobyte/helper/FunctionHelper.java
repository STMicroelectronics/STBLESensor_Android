package com.st.trilobyte.helper;

import com.st.trilobyte.models.Flow;
import com.st.trilobyte.models.Function;

import java.util.List;

public class FunctionHelper {

    public static void filterFunctionsByMandatoryInputs(List<Function> availableFunctions, List<String> inputs) {
        for (int i = availableFunctions.size() - 1; i >= 0; i--) {
            Function function = availableFunctions.get(i);
            if (!FunctionHelper.hasFunctionMandatoryInput(function, inputs)) {
                availableFunctions.remove(function);
            }
        }
    }

    private static boolean hasFunctionMandatoryInput(Function function, List<String> inputs) {

        if (function.getMandatoryInputs().isEmpty()) {
            return true;
        }

        for (List<String> mandatoryInputs : function.getMandatoryInputs()) {
            if (inputs.containsAll(mandatoryInputs)) {
                return true;
            }
        }

        return false;
    }

    public static void filterFunctionsByInputs(List<Function> availableFunctions, List<String> inputs) {
        for (int i = availableFunctions.size() - 1; i >= 0; i--) {
            Function function = availableFunctions.get(i);

            boolean hasInput = false;
            for (String input : inputs) {
                if (function.getInputs().contains(input)) {
                    hasInput = true;
                    break;
                }
            }

            if (!hasInput) {
                availableFunctions.remove(function);
            }
        }
    }

    public static void filterFunctionsByParameterCount(List<Function> availableFunctions, List<String> inputs) {
        for (int i = availableFunctions.size() - 1; i >= 0; i--) {
            Function function = availableFunctions.get(i);

            int paramCount = 0;
            for (String inputId : inputs) {
                if (function.getInputs().contains(inputId)) {
                    paramCount += 1;
                }
            }

            if (paramCount != function.getParametersCount()) {
                availableFunctions.remove(function);
            }
        }
    }

    public static void filterFunctionByRepeatCount(Flow flow, List<Function> availableFunctions) {

        if (!flow.getFunctions().isEmpty()) {
            Function lastFunction = flow.getFunctions().get(flow.getFunctions().size() - 1);
            filterFunctionByRepeatCount(flow, lastFunction, 0, availableFunctions);
        } else {
            for (Flow parentFlow : flow.getFlows()) {
                filterFunctionByRepeatCount(parentFlow, availableFunctions);
            }
        }
    }

    private static void filterFunctionByRepeatCount(Flow flow, Function lastFunction, int repeatCount, List<Function> availableFunctions) {

        if (!flow.getFunctions().isEmpty()) {
            boolean scanParentsFlow = true;
            for (int i = flow.getFunctions().size() - 1; i >= 0; i--) {
                Function function = flow.getFunctions().get(i);
                if (!function.getId().equals(lastFunction.getId())) {
                    scanParentsFlow = false;
                    break;
                }

                repeatCount += 1;
            }

            if (scanParentsFlow && FlowHelper.isCompositeFlow(flow)) {
                for (Flow parentFlow : flow.getFlows()) {
                    filterFunctionByRepeatCount(parentFlow, lastFunction, repeatCount, availableFunctions);
                }
            } else {
                for (Function availableFunction : availableFunctions) {
                    if (availableFunction.getId().equals(lastFunction.getId())) {
                        if (availableFunction.getMaxRepeatCount() != null) {
                            if (repeatCount >= availableFunction.getMaxRepeatCount()) {
                                availableFunctions.remove(availableFunction);
                                break;
                            }
                        }
                    }
                }
            }
        } else {
            for (Flow parentFlow : flow.getFlows()) {
                filterFunctionByRepeatCount(parentFlow, lastFunction, repeatCount, availableFunctions);
            }
        }
    }

    /**
     * Filtra le funzioni disponibili dato l'outputs delle funzioni dei flow utilizzati come input.
     * Introdotta per esigenze FW.
     *
     * @param availableFunctions
     * @param parentFlows
     */
    public static void filterFunctionsByParentFlowsFunctionOutput(List<Function> availableFunctions, List<Flow> parentFlows) {

        for (Flow parentFlow : parentFlows) {
            if (parentFlow.getFunctions().isEmpty()) {
                filterFunctionsByParentFlowsFunctionOutput(availableFunctions, parentFlow.getFlows());
                continue;
            }

            Function lastParentFunction = parentFlow.getFunctions().get(parentFlow.getFunctions().size() - 1);
            for (int i = availableFunctions.size() - 1; i >= 0; i--) {
                Function function = availableFunctions.get(i);
                if (!function.getInputs().contains(lastParentFunction.getId())) {
                    availableFunctions.remove(function);
                }
            }
        }
    }

    public static Function findFunctionById(List<Function> functions, String id) {

        for (final Function function : functions) {
            if (function.getId().equals(id)) {
                return function;
            }
        }

        return null;
    }
}
