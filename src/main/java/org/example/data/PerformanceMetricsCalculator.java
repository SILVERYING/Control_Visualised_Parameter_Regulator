package org.example.data;

import java.util.List;

public class PerformanceMetricsCalculator {

    public static PerformanceMetrics calculate(List<DataPoint> data, double initialValue, double finalValue) {
        if (data == null || data.size() < 2) {
            return new PerformanceMetrics(0, 0, 0, 0);
        }

        double stepHeight = finalValue - initialValue;
        if (Math.abs(stepHeight) < 1e-6) { // Avoid division by zero if setpoint doesn't change
            return new PerformanceMetrics(0, 0, 0, 0);
        }

        double riseTime = calculateRiseTime(data, initialValue, stepHeight);
        double overshoot = calculateOvershoot(data, finalValue, stepHeight);
        double settlingTime = calculateSettlingTime(data, finalValue, stepHeight);
        double iae = calculateIAE(data);

        return new PerformanceMetrics(riseTime, overshoot, settlingTime, iae);
    }

    private static double calculateRiseTime(List<DataPoint> data, double initialValue, double stepHeight) {
        double timeAt10 = -1, timeAt90 = -1;
        double target10 = initialValue + 0.1 * stepHeight;
        double target90 = initialValue + 0.9 * stepHeight;

        for (DataPoint p : data) {
            if (timeAt10 == -1 && p.pv() >= target10) {
                timeAt10 = p.time();
            }
            if (timeAt90 == -1 && p.pv() >= target90) {
                timeAt90 = p.time();
                break; // Found both, can exit
            }
        }
        return (timeAt10 != -1 && timeAt90 != -1) ? timeAt90 - timeAt10 : 0;
    }

    private static double calculateOvershoot(List<DataPoint> data, double finalValue, double stepHeight) {
        double maxPv = data.stream().mapToDouble(DataPoint::pv).max().orElse(finalValue);
        if (maxPv <= finalValue)
            return 0;
        return ((maxPv - finalValue) / Math.abs(stepHeight)) * 100.0;
    }

    private static double calculateSettlingTime(List<DataPoint> data, double finalValue, double stepHeight) {
        double tolerance = 0.02 * Math.abs(stepHeight); // 2% tolerance band
        double lastTimeOutsideBand = 0;

        for (DataPoint p : data) {
            if (Math.abs(p.pv() - finalValue) > tolerance) {
                lastTimeOutsideBand = p.time();
            }
        }
        return lastTimeOutsideBand;
    }

    private static double calculateIAE(List<DataPoint> data) {
        double iae = 0.0;
        for (int i = 1; i < data.size(); i++) {
            double error = Math.abs(data.get(i).setpoint() - data.get(i).pv());
            double dt = data.get(i).time() - data.get(i - 1).time();
            iae += error * dt;
        }
        return iae;
    }
}
