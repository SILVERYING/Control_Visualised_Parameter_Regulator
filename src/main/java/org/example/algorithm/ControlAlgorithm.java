package org.example.algorithm;

import java.util.Map;

public interface ControlAlgorithm {

    /**
     * Calculates the control output.
     * 
     * @param setpoint    The desired value.
     * @param pv          The current measured value (Process Variable).
     * @param currentTime The current simulation time.
     * @return The calculated control output.
     */
    double calculate(double setpoint, double pv, double currentTime); // <--- 主要变化在这里

    void reset();

    String getDisplayName();

    String[] getParameterNames();

    void setParameters(Map<String, Double> params);

    Map<String, Double> getCurrentParameters();

    boolean isAutoTuning();
}
