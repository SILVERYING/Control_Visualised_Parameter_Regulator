package org.example.ui;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JFrame;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * 频域分析对话框
 */
public class FrequencyAnalysisDialog extends JDialog {

    public FrequencyAnalysisDialog(JFrame owner, List<Double> pvData, double dt) {
        super(owner, "Frequency Domain Analysis (FFT of PV)", true);
        setSize(800, 500);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        // 准备数据进行FFT
        double[] data = prepareDataForFFT(pvData);
        if (data == null || data.length == 0) {
            // 处理数据不足的情况
            add(new javax.swing.JLabel("Not enough data for frequency analysis."), BorderLayout.CENTER);
            return;
        }

        // 执行FFT
        FastFourierTransformer transformer = new FastFourierTransformer(DftNormalization.STANDARD);
        Complex[] result = transformer.transform(data, TransformType.FORWARD);

        // 创建图表数据集
        XYSeries series = new XYSeries("Amplitude Spectrum");
        int n = data.length;
        double samplingFrequency = 1.0 / dt;

        // 频谱图只需要前半部分数据 (Nyquist 频率之前)
        for (int i = 0; i < n / 2; i++) {
            double frequency = i * samplingFrequency / n;
            double magnitude = result[i].abs() / n; // 归一化幅度
            series.add(frequency, magnitude);
        }

        XYSeriesCollection dataset = new XYSeriesCollection(series);
        JFreeChart chart = ChartFactory.createXYLineChart(
                "PV Frequency Spectrum",
                "Frequency (Hz)",
                "Amplitude",
                dataset,
                PlotOrientation.VERTICAL,
                false, true, false);

        ChartPanel chartPanel = new ChartPanel(chart);
        add(chartPanel, BorderLayout.CENTER);
    }

    /**
     * 准备FFT数据，确保长度为2的幂，并移除直流分量
     * 
     * @param originalData 原始数据列表
     * @return 处理后的 double 数组
     */
    private double[] prepareDataForFFT(List<Double> originalData) {
        if (originalData.size() < 16)
            return null; // 至少需要一些数据点

        // 计算平均值 (直流分量)
        double mean = originalData.stream().mapToDouble(d -> d).average().orElse(0.0);

        // 找到小于等于原始数据长度的最大的2的幂
        int n = 1;
        while (n <= originalData.size()) {
            n <<= 1; // n = n * 2
        }
        n >>= 1; // n = n / 2

        double[] data = new double[n];
        for (int i = 0; i < n; i++) {
            // 截取数据并移除直流分量
            data[i] = originalData.get(originalData.size() - n + i) - mean;
        }
        return data;
    }
}
