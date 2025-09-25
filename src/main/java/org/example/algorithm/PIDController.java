package org.example.algorithm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class PIDController implements ControlAlgorithm {
    public enum TuningRule {
        CLASSIC_ZN("Classic Z-N (Aggressive)"),
        SOME_OVERSHOOT("Some Overshoot (Balanced)"),
        NO_OVERSHOOT("No Overshoot (Smooth)");

        private final String displayName;

        TuningRule(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    // PID Parameters
    private double Kp = 1.0;
    private double Ki = 0.1;
    private double Kd = 0.05;

    // Internal state
    private double integral = 0;
    private double previousError = 0;
    private double dt = 0.05;

    // Auto-Tuning State
    private boolean autoTuning = false;
    private double tuneSetpoint;
    private double relayOutputAmplitude = 80.0;
    private double hysteresis = 0.5;
    private double currentOutput;
    private List<Double> peakAmplitudes = new ArrayList<>();
    private List<Double> peakTimes = new ArrayList<>();
    private double lastPeakTime = 0;
    private double lastProcessValue = Double.NaN;
    private int cycleCount = 0;
    private static final int REQUIRED_CYCLES = 3;
    private Runnable autoTuneListener;
    private TuningRule selectedTuneRule = TuningRule.NO_OVERSHOOT; // Default rule

    public PIDController() {
    }

    @Override
    public String getDisplayName() {
        return "PID Controller";
    }

    @Override
    public double calculate(double setpoint, double pv, double currentTime) {
        if (!this.autoTuning) {
            return calculatePidOutput(setpoint, pv);
        }

        autoTuneStep(pv, currentTime);

        if (cycleCount >= REQUIRED_CYCLES * 2) {
            calculateAndApplyTuning();
            return calculatePidOutput(setpoint, pv); // Start using new params immediately
        } else {
            return currentOutput;
        }
    }

    private double calculatePidOutput(double setpoint, double pv) {
        double error = setpoint - pv;
        this.integral += error * dt;
        double derivative = (error - this.previousError) / dt;
        this.previousError = error;
        return (Kp * error) + (Ki * this.integral) + (Kd * derivative);
    }

    private void autoTuneStep(double pv, double currentTime) {
        if (Double.isNaN(lastProcessValue)) {
            lastProcessValue = pv;
            lastPeakTime = currentTime;
            return;
        }

        double error = tuneSetpoint - pv;
        if (error > hysteresis) {
            currentOutput = relayOutputAmplitude;
        } else if (error < -hysteresis) {
            currentOutput = -relayOutputAmplitude;
        }

        boolean crossedSetpoint = (lastProcessValue - tuneSetpoint) * (pv - tuneSetpoint) < 0;

        if (crossedSetpoint) {
            if (lastPeakTime > 0) {
                double halfPeriod = currentTime - lastPeakTime;
                peakTimes.add(halfPeriod);
                peakAmplitudes.add(Math.abs(lastProcessValue - tuneSetpoint));
                cycleCount++;
            }
            lastPeakTime = currentTime;
        }
        lastProcessValue = pv;
    }

    // --- MODIFIED: Use a switch statement for clarity ---
    private void calculateAndApplyTuning() {
        if (peakTimes.size() < REQUIRED_CYCLES * 2) {
            finishAutoTune(false, "Auto-tune failed: Not enough oscillation cycles detected.");
            return;
        }

        double avgPeriod = peakTimes.stream().mapToDouble(t -> t).average().orElse(0) * 2.0;
        double avgAmplitude = peakAmplitudes.stream().mapToDouble(a -> a).average().orElse(0);

        if (avgAmplitude <= 0 || avgPeriod <= 0) {
            finishAutoTune(false, "Auto-tune failed: Invalid oscillation data.");
            return;
        }

        double Ku = (4.0 * relayOutputAmplitude) / (Math.PI * avgAmplitude);
        double Tu = avgPeriod;

        // --- NEW: Apply the selected tuning rule ---
        switch (selectedTuneRule) {
            case CLASSIC_ZN:
                this.Kp = 0.6 * Ku;
                this.Ki = (1.2 * Ku) / Tu;
                this.Kd = (0.6 * Ku * Tu) / 8.0;
                break;
            case SOME_OVERSHOOT:
                this.Kp = 0.33 * Ku;
                this.Ki = (0.66 * Ku) / Tu;
                this.Kd = (0.33 * Ku * Tu) / 8.0;
                break;
            case NO_OVERSHOOT:
            default: // Fallback to the safest option
                this.Kp = 0.2 * Ku;
                this.Ki = (0.4 * Ku) / Tu;
                this.Kd = (0.2 * Ku * Tu) / 8.0;
                break;
        }

        String successMessage = String.format(
                "Auto-tuning successful! Rule: %s Ku=%.2f, Tu=%.2f New Params: Kp=%.2f, Ki=%.2f, Kd=%.2f",
                selectedTuneRule, Ku, Tu, this.Kp, this.Ki, this.Kd);
        finishAutoTune(true, successMessage);
    }

    // --- MODIFIED: Accept a TuningRule as a parameter ---
    public void startAutoTune(double setpoint, TuningRule rule) {
        reset();
        this.autoTuning = true;
        this.tuneSetpoint = setpoint;
        this.selectedTuneRule = rule; // Store the chosen rule

        this.peakAmplitudes.clear();
        this.peakTimes.clear();
        this.lastPeakTime = 0;
        this.cycleCount = 0;
        this.lastProcessValue = Double.NaN;
        this.currentOutput = relayOutputAmplitude;
    }

    private void finishAutoTune(boolean success, String message) {
        this.autoTuning = false;
        if (autoTuneListener != null) {
            autoTuneListener.run();
        }
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, message, "Auto-Tune Result",
                success ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE));
    }

    public void setAutoTuneListener(Runnable listener) {
        this.autoTuneListener = listener;
    }

    @Override
    public void reset() {
        if (this.autoTuning) {
            finishAutoTune(false, "Tuning process was manually reset.");
        }
        this.integral = 0;
        this.previousError = 0;
    }

    public String getAutoTuneStatus() {
        if (!autoTuning)
            return "Not in Auto-Tune";
        return String.format("Tuning (%s)... Cycle %d of %d", selectedTuneRule, (cycleCount / 2) + 1, REQUIRED_CYCLES);
    }

    @Override
    public boolean isAutoTuning() {
        return this.autoTuning;
    }

    @Override
    public String[] getParameterNames() {
        return new String[] { "Kp", "Ki", "Kd" };
    }

    @Override
    public Map<String, Double> getCurrentParameters() {
        return Map.of("Kp", this.Kp, "Ki", this.Ki, "Kd", this.Kd);
    }

    @Override
    public void setParameters(Map<String, Double> params) {
        this.Kp = params.getOrDefault("Kp", this.Kp);
        this.Ki = params.getOrDefault("Ki", this.Ki);
        this.Kd = params.getOrDefault("Kd", this.Kd);
    }
}
