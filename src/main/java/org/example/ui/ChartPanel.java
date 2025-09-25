package org.example.ui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import org.example.data.DataPoint;
import org.example.data.SimulationRun;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

public class ChartPanel extends JPanel {
    private final JFreeChart chart;
    private final org.jfree.chart.ChartPanel jfreeChartPanel;
    private final TimeSeries pvSeries;
    private final TimeSeries setpointSeries;
    private final TimeSeries outSeries;

    private final List<DataPoint> currentRunData = new ArrayList<>();
    private final List<TimeSeries> comparisonSeries = new ArrayList<>();
    private int comparisonColorIndex = 0;
    private static final Color[] COMPARISON_COLORS = {
            new Color(255, 128, 0), // Orange
            new Color(128, 0, 128), // Purple
            new Color(0, 128, 128), // Teal
            new Color(153, 153, 0) // Olive
    };

    public ChartPanel() {
        super(new BorderLayout());

        pvSeries = new TimeSeries("Live PV");
        setpointSeries = new TimeSeries("Live Setpoint");
        outSeries = new TimeSeries("Live Output");

        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(pvSeries);
        dataset.addSeries(setpointSeries);
        dataset.addSeries(outSeries);

        chart = ChartFactory.createTimeSeriesChart(
                "System Response", "Time", "Value", dataset, true, true, false);

        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);

        XYItemRenderer renderer = plot.getRenderer();
        renderer.setSeriesPaint(0, Color.BLUE);
        renderer.setSeriesStroke(0, new BasicStroke(2.0f));
        renderer.setSeriesPaint(1, Color.RED);
        renderer.setSeriesStroke(1, new BasicStroke(2.0f, BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND, 1.0f, new float[] { 6.0f, 6.0f }, 0.0f));
        renderer.setSeriesPaint(2, new Color(0, 153, 51));
        renderer.setSeriesStroke(2, new BasicStroke(1.5f));

        chart.getPlot().setInsets(new RectangleInsets(5.0, 5.0, 5.0, 20.0));

        this.jfreeChartPanel = new org.jfree.chart.ChartPanel(chart);
        this.jfreeChartPanel.setMouseWheelEnabled(true);
        add(this.jfreeChartPanel, BorderLayout.CENTER);
    }

    public void addDataPoint(double time, double pv, double setpoint, double out) {
        Millisecond now = new Millisecond();
        pvSeries.addOrUpdate(now, pv);
        setpointSeries.addOrUpdate(now, setpoint);
        outSeries.addOrUpdate(now, out);

        currentRunData.add(new DataPoint(time, pv, setpoint, out));
    }

    public List<Double> getPvDataForFFT() {
        return currentRunData.stream().map(DataPoint::pv).toList();
    }

    public List<DataPoint> getCurrentRunData() {
        return new ArrayList<>(currentRunData);
    }

    public void reset() {
        pvSeries.clear();
        setpointSeries.clear();
        outSeries.clear();
        currentRunData.clear();
        clearComparisonRuns();
    }

    public void displayComparisonRun(SimulationRun run) {
        TimeSeries series = new TimeSeries("PV: " + run.getName());
        long startTimeMillis = System.currentTimeMillis();
        for (DataPoint dp : run.getData()) {
            // We use Millisecond to align with the live chart's time axis type
            long timeOffset = (long) (dp.time() * 1000);
            series.add(new Millisecond(new java.util.Date(startTimeMillis + timeOffset)), dp.pv());
        }

        comparisonSeries.add(series);
        XYPlot plot = chart.getXYPlot();
        ((TimeSeriesCollection) plot.getDataset()).addSeries(series);

        int seriesIndex = plot.getDataset().getSeriesCount() - 1;
        XYItemRenderer renderer = plot.getRenderer();
        renderer.setSeriesPaint(seriesIndex, COMPARISON_COLORS[comparisonColorIndex % COMPARISON_COLORS.length]);
        renderer.setSeriesStroke(seriesIndex,
                new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 3, 3 }, 0));
        comparisonColorIndex++;
    }

    public void clearComparisonRuns() {
        XYPlot plot = chart.getXYPlot();
        TimeSeriesCollection dataset = (TimeSeriesCollection) plot.getDataset();
        for (TimeSeries series : comparisonSeries) {
            dataset.removeSeries(series);
        }
        comparisonSeries.clear();
        comparisonColorIndex = 0;
    }
}
