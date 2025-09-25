package org.example.plant;

/**
 * 一阶惯性系统实现A
 * 标准被控对象模型
 */
public class FirstOrderPlant implements Plant {
    private double state = 0;
    private final double timeConstant; // 时间常数
    private final double gain; // 系统增益
    private final double dt; // 仿真步长

    public FirstOrderPlant(double timeConstant, double gain, double dt) {
        this.timeConstant = timeConstant;
        this.gain = gain;
        this.dt = dt;
    }

    @Override
    public double update(double input) {
        // 一阶系统离散化模型: x(k+1) = x(k) + dt*(-x(k) + K*u)/T
        state = state + dt * (-state + gain * input) / timeConstant;
        return state;
    }

    @Override
    public double getState() {
        return state;
    }

    @Override
    public void reset() {
        state = 0;
    }

    @Override
    public String getName() {
        return String.format("1st Order (T=%.1f, K=%.1f)", timeConstant, gain);
    }
}
