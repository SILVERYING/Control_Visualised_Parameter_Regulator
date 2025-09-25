package org.example.data;

public record PerformanceMetrics(
        double riseTime, // 上升时间 (10% -> 90%)
        double overshoot, // 超调量 (%)
        double settlingTime, // 稳定时间 (进入 ±2% 误差带)
        double iae // 积分绝对误差
) {
    @Override
    public String toString() {
        return String.format(
                "Rise Time (10-90%%): %.2f s " + "Overshoot: %.2f %% " + "Settling Time (±2%%): %.2f s "
                        + "Integral Absolute Error (IAE): %.2f",
                riseTime, overshoot, settlingTime, iae);
    }
}
