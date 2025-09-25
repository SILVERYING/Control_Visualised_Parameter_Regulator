package org.example.data;

import java.util.List;
import java.util.Map;

public class SimulationRun {
    private String name;
    private final long timestamp;
    private final Map<String, Double> parameters;
    private final List<DataPoint> data;
    private final PerformanceMetrics metrics;

    public SimulationRun(String name, Map<String, Double> parameters, List<DataPoint> data,
            PerformanceMetrics metrics) {
        this.name = name;
        this.timestamp = System.currentTimeMillis();
        this.parameters = parameters;
        this.data = data;
        this.metrics = metrics;
    }

    // Getters
    public String getName() {
        return name;
    }

    public Map<String, Double> getParameters() {
        return parameters;
    }

    public List<DataPoint> getData() {
        return data;
    }

    public PerformanceMetrics getMetrics() {
        return metrics;
    }

    @Override
    public String toString() {
        return name; // JList will use this to display the run
    }
}
