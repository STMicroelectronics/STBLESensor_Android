package com.st.trilobyte.helper;

import com.st.trilobyte.models.Flow;
import com.st.trilobyte.models.Function;
import com.st.trilobyte.models.Output;
import com.st.trilobyte.models.Sensor;
import com.st.trilobyte.models.SensorConfiguration;

import java.util.ArrayList;
import java.util.List;

public class FlowHelper {

    /**
     * Restituisce tutti i sensori presenti in un flow
     *
     * @param flow
     * @param sensors
     */
    public static void extractAllSensorsFromCompositeFlow(Flow flow, List<Sensor> sensors) {

        sensors.addAll(flow.getSensors());

        for (final Flow parent : flow.getFlows()) {
            extractAllSensorsFromCompositeFlow(parent, sensors);
        }
    }

    /**
     * Restituisce l'insieme dei flow che compongono il flow corrente
     *
     * @param flow
     * @param flows
     */
    public static void extractAllFlowsFromCompositeFlow(Flow flow, List<Flow> flows) {

        flows.add(flow);

        for (Flow parent : flow.getFlows()) {
            extractAllFlowsFromCompositeFlow(parent, flows);
        }
    }

    /**
     * Restituisce i sensori presenti come input al flow corrente
     *
     * @param flow
     * @param inputs
     */
    public static void getFlowSensorInputs(Flow flow, List<String> inputs) {

        for (Sensor sensor : flow.getSensors()) {
            if (!inputs.contains(sensor.getId())) {
                inputs.add(sensor.getId());
            }
        }

        for (Flow parent : flow.getFlows()) {
            if (parent.getFunctions().isEmpty()) {
                getFlowSensorInputs(parent, inputs);
            }
        }
    }

    /**
     * Restituisce le funzioni presenti come input al flow corrente
     *
     * @param flow
     * @param inputs
     */
    public static void getFlowFunctionInputs(Flow flow, List<String> inputs) {

        for (Flow parent : flow.getFlows()) {
            if (parent.getFunctions().isEmpty()) {
                getFlowFunctionInputs(parent, inputs);
            } else {
                Function lastFunction = parent.getFunctions().get(parent.getFunctions().size() - 1);
                inputs.add(lastFunction.getId());
            }
        }
    }

    /**
     * Ricerca nel flow corrente la configurazione di un sensore
     *
     * @param flow
     * @param sensorId
     * @return
     */
    public static SensorConfiguration searchSensorConfigurationInFlow(Flow flow, String sensorId) {

        List<Sensor> sensors = new ArrayList<>();
        extractAllSensorsFromCompositeFlow(flow, sensors);

        for (int i = sensors.size() - 1; i >= 0; i--) {
            Sensor sensor = sensors.get(i);
            if (sensor.getId().equals(sensorId)) {
                return sensor.getConfiguration();
            }
        }

        return null;
    }

    public static int getCompositeInputFlowCount(Flow flow) {
        return flow.getFlows().size();
    }

    /**
     * @param flow
     * @return true if the flow has a parent flow as input
     */
    public static boolean isCompositeFlow(Flow flow) {
        return !flow.getFlows().isEmpty();
    }

    public static List<Flow> filterByFunction(List<Flow> flows, Function function) {
        List<Flow> filtered = new ArrayList<>();
        for (Flow flow : flows) {
            List<Function> functions = flow.getFunctions();
            List<Sensor> sensors = flow.getSensors();
            if (functions != null && !functions.isEmpty()) {
                if (function.getInputs().contains(functions.get(functions.size() - 1).getId())) {
                    filtered.add(flow);
                    continue;
                }
            }
            if (sensors != null && !sensors.isEmpty()) {
                for (Sensor sensor : sensors) {
                    if (function.getInputs().contains(sensor.getId())) {
                        filtered.add(flow);
                        continue;
                    }
                }
            }
        }
        return filtered;
    }

    public static boolean containsFunction(Flow flow, String funId) {
        if (flow.getFunctions() != null) {
            for (Function function : flow.getFunctions()) {
                if (function.getId().equals(funId)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean canBeUsedAsExp(Flow flow) {

        if (flow.getOutputs().size() == 1) {
            return flow.getOutputs().get(0).getId().equals(Output.OUTPUT_EXP_ID);
        }

        return false;
    }
}
