package com.st.trilobyte.models;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Flow implements Serializable {

    public final static int FLOW_VERSION = 1;

    private int version = FLOW_VERSION;

    private transient File file;

    private String id;

    private String category;

    private List<String> board_compatibility = new ArrayList<>();

    private String description;

    private String notes;

    private List<Sensor> sensors = new ArrayList<>();

    private List<Function> functions = new ArrayList<>();

    private List<Flow> flows = new ArrayList<>();

    private List<Output> outputs = new ArrayList<>();

    public Flow() {
        generateId();
    }

    public String getId() {
        return id;
    }

    public void generateId() {
        id = UUID.randomUUID().toString();
    }

    public int getVersion() {
        return version;
    }

    public String getCategory() { return category; }

    public List<String> getBoard_compatibility() { return board_compatibility; }

    public void setBoard_compatibility(String board){
        board_compatibility.add(board);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(final String notes) {
        this.notes = notes;
    }

    public File getFile() {
        return file;
    }

    public void setFile(final File file) {
        this.file = file;
    }

    public List<Sensor> getSensors() {
        return sensors;
    }

    public void setSensors(final List<Sensor> sensors) {
        this.sensors = sensors;
    }

    public List<Function> getFunctions() {
        return functions;
    }

    public List<Flow> getFlows() {
        return flows;
    }

    public void setFlows(final List<Flow> flows) {
        this.flows = flows;
    }

    public void setFunctions(final List<Function> functions) {
        this.functions = functions;
    }

    public List<Output> getOutputs() {
        return outputs;
    }

    public void setOutputs(final List<Output> outputs) {
        this.outputs = outputs;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Flow flow = (Flow) o;
        return Objects.equals(id, flow.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public boolean canBeUploaded() {
        if (getOutputs() == null || getOutputs().size() == 0) {
            return false;
        }
        for (Output output : getOutputs()) {
            if (output.getCanUpload()) {
                return true;
            }
        }
        return false;
    }

    public boolean canBeUsedAsInput() {
        if (getOutputs() == null || getOutputs().size() == 0) {
            return false;
        }
        for (Output output : getOutputs()) {
            if (output.getId().equals(Output.OUTPUT_AS_INPUT_ID)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "Flow{" +
                "version=" + version +
                ", file=" + file +
                ", id='" + id + '\'' +
                ", board_compatibility="+ board_compatibility +
                ", description='" + description + '\'' +
                ", notes='" + notes + '\'' +
                ", sensors=" + sensors +
                ", functions=" + functions +
                ", flows=" + flows +
                ", outputs=" + outputs +
                '}';
    }
}
