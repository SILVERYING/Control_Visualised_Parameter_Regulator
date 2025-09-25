package org.example.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.example.algorithm.ControlAlgorithm;
import org.example.algorithm.PIDController;
import org.example.data.DataPoint;
import org.example.data.PerformanceMetrics;
import org.example.data.PerformanceMetricsCalculator;
import org.example.data.SimulationRun;
import org.example.plant.FirstOrderPlant;
import org.example.plant.Plant;

public class ControlVisualizer extends JFrame {
    private static final double SIMULATION_DT = 0.05;

    // 输入模式枚举
    private enum InputMode {
        STEP, SINE
    }

    private InputMode inputMode = InputMode.STEP;

    // UI Components
    private ChartPanel chartPanel;
    private ParameterPanel parameterPanel;
    private RealTimeDisplayPanel realTimeDisplayPanel;
    private JButton startButton, stopButton, resetButton, fftButton, manageRunsButton;
    private JComboBox<String> inputModeBox;
    private JTextField amplitudeField, freqField, offsetField;

    // Simulation State
    private boolean isRunning = false;
    private java.util.Timer simulationTimer;
    private Plant plant;
    private ControlAlgorithm currentAlgorithm;
    private double currentTime = 0.0;

    // Data Management
    private List<SimulationRun> savedRuns = new ArrayList<>();
    private double initialValueForMetrics = 0.0;
    private double setpointForMetrics = 0.0;

    public ControlVisualizer() {
        setupUI();
        initializeSystem();
    }

    public ChartPanel getChartPanel() {
        return chartPanel;
    }

    public List<SimulationRun> getSavedRuns() {
        return savedRuns;
    }

    public void setSavedRuns(List<SimulationRun> runs) {
        this.savedRuns = runs;
    }

    // ---------------- UI ----------------
    private void setupUI() {
        setTitle("Control System Visualizer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(5, 5));

        chartPanel = new ChartPanel();
        add(chartPanel, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel(new BorderLayout());
        controlPanel.setBorder(BorderFactory.createTitledBorder("Control Panel"));

        // --- 按钮面板 ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        startButton = new JButton("Start");
        stopButton = new JButton("Stop");
        resetButton = new JButton("Reset");
        fftButton = new JButton("Frequency Analysis");
        manageRunsButton = new JButton("Manage Runs");

        startButton.addActionListener(e -> startSimulation());
        stopButton.addActionListener(e -> stopSimulation());
        resetButton.addActionListener(e -> resetSystem());
        fftButton.addActionListener(e -> showFrequencyAnalysis());
        manageRunsButton.addActionListener(e -> showRunManager());

        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);
        buttonPanel.add(resetButton);
        buttonPanel.add(fftButton);
        buttonPanel.add(manageRunsButton);

        // --- 输入模式与参数 ---
        JPanel inputConfigPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        inputConfigPanel.add(new JLabel("Input Mode:"));
        inputModeBox = new JComboBox<>(new String[] { "Step", "Sine" });
        inputModeBox.addActionListener(e -> updateInputMode());
        inputConfigPanel.add(inputModeBox);

        inputConfigPanel.add(new JLabel("Amplitude:"));
        amplitudeField = new JTextField("2.0", 4);
        inputConfigPanel.add(amplitudeField);

        inputConfigPanel.add(new JLabel("Freq(Hz):"));
        freqField = new JTextField("0.5", 4);
        inputConfigPanel.add(freqField);

        inputConfigPanel.add(new JLabel("Offset:"));
        offsetField = new JTextField("5.0", 4);
        inputConfigPanel.add(offsetField);

        // 初始禁用正弦参数（因为默认是阶跃模式）
        amplitudeField.setEnabled(false);
        freqField.setEnabled(false);
        offsetField.setEnabled(false);

        // 参数输入面板
        parameterPanel = new ParameterPanel(this);
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(buttonPanel, BorderLayout.NORTH);
        topPanel.add(inputConfigPanel, BorderLayout.SOUTH);

        controlPanel.add(topPanel, BorderLayout.NORTH);
        controlPanel.add(parameterPanel, BorderLayout.CENTER);

        add(controlPanel, BorderLayout.EAST);

        realTimeDisplayPanel = new RealTimeDisplayPanel();
        add(realTimeDisplayPanel, BorderLayout.SOUTH);

        pack();
        setMinimumSize(getSize());
        setLocationRelativeTo(null);
    }

    private void updateInputMode() {
        if (inputModeBox.getSelectedIndex() == 0) {
            inputMode = InputMode.STEP;
            amplitudeField.setEnabled(false);
            freqField.setEnabled(false);
            offsetField.setEnabled(false);
        } else {
            inputMode = InputMode.SINE;
            amplitudeField.setEnabled(true);
            freqField.setEnabled(true);
            offsetField.setEnabled(true);
        }
    }

    // ---------------- 初始化系统 ----------------
    private void initializeSystem() {
        plant = new FirstOrderPlant(1.0, 1.0, SIMULATION_DT);
        currentAlgorithm = new PIDController();
        parameterPanel.setAlgorithm(currentAlgorithm);

        if (currentAlgorithm instanceof PIDController) {
            ((PIDController) currentAlgorithm).setAutoTuneListener(() -> SwingUtilities.invokeLater(() -> {
                parameterPanel.updateParameterFields();
                stopSimulation();
                realTimeDisplayPanel.setStatus("Auto-tuning complete.");
                parameterPanel.setButtonsEnabled(true);
            }));
        }
        resetSystem();
    }

    // ---------------- 开始仿真 ----------------
    private void startSimulation() {
        if (isRunning)
            return;

        plant.reset();
        chartPanel.reset();
        currentTime = 0.0;

        if (currentAlgorithm.isAutoTuning()) {
            parameterPanel.setButtonsEnabled(false);
        } else {
            currentAlgorithm.reset();
        }

        isRunning = true;
        initialValueForMetrics = plant.getState();
        setpointForMetrics = parameterPanel.getSetpoint();

        simulationTimer = new java.util.Timer();
        simulationTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!isRunning)
                    return;

                currentTime += SIMULATION_DT;
                double setpoint;
                if (inputMode == InputMode.STEP) {
                    setpoint = parameterPanel.getSetpoint();
                } else {
                    double amplitude = Double.parseDouble(amplitudeField.getText());
                    double freq = Double.parseDouble(freqField.getText());
                    double offset = Double.parseDouble(offsetField.getText());
                    setpoint = amplitude * Math.sin(2 * Math.PI * freq * currentTime) + offset;
                }

                double pv = plant.getState();
                double controlOutput = currentAlgorithm.calculate(setpoint, pv, currentTime);
                plant.update(controlOutput);
                double newPv = plant.getState();

                SwingUtilities.invokeLater(() -> {
                    chartPanel.addDataPoint(currentTime, newPv, setpoint, controlOutput);
                    realTimeDisplayPanel.updateValues(newPv, setpoint, controlOutput);
                    String status = currentAlgorithm.isAutoTuning()
                            ? ((PIDController) currentAlgorithm).getAutoTuneStatus()
                            : "Running...";
                    realTimeDisplayPanel.setStatus(status);
                });
            }
        }, 0, (long) (SIMULATION_DT * 1000));

        setTitle("Control System Visualizer [RUNNING]");
    }

    // ---------------- 停止仿真 ----------------
    private void stopSimulation() {
        if (!isRunning)
            return;
        isRunning = false;
        if (simulationTimer != null) {
            simulationTimer.cancel();
            simulationTimer = null;
        }
        setTitle("Control System Visualizer [STOPPED]");
        realTimeDisplayPanel.setStatus("Stopped. Ready to save run.");
        parameterPanel.setButtonsEnabled(true);
        captureCurrentRun();
    }

    private void resetSystem() {
        if (isRunning)
            stopSimulation();
        plant.reset();
        currentAlgorithm.reset();
        chartPanel.reset();
        currentTime = 0.0;
        realTimeDisplayPanel.updateValues(plant.getState(), parameterPanel.getSetpoint(), 0);
        realTimeDisplayPanel.setStatus("System Reset. Ready.");
        parameterPanel.setButtonsEnabled(true);
    }

    public void startAutoTune(double setpoint, PIDController.TuningRule rule) {
        if (!(currentAlgorithm instanceof PIDController))
            return;
        if (isRunning)
            stopSimulation();
        plant.reset();
        chartPanel.reset();
        currentTime = 0.0;
        realTimeDisplayPanel.updateValues(0, 0, 0);
        realTimeDisplayPanel.setStatus("Preparing for Auto-Tune...");
        ((PIDController) currentAlgorithm).startAutoTune(setpoint, rule);
        startSimulation();
    }

    private void showFrequencyAnalysis() {
        if (isRunning)
            stopSimulation();
        List<Double> pvData = chartPanel.getPvDataForFFT();
        if (pvData.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No data available for analysis. Please run a simulation first.",
                    "FFT Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        FrequencyAnalysisDialog dialog = new FrequencyAnalysisDialog(this, pvData, SIMULATION_DT);
        dialog.setVisible(true);
    }

    private void showRunManager() {
        if (isRunning) {
            int choice = JOptionPane.showConfirmDialog(this,
                    "Simulation is running. Stop and open manager?",
                    "Confirm Stop", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION)
                stopSimulation();
            else
                return;
        }
        RunManagerDialog dialog = new RunManagerDialog(this, savedRuns);
        dialog.setVisible(true);
    }

    private void captureCurrentRun() {
        List<DataPoint> data = chartPanel.getCurrentRunData();
        if (data == null || data.size() < 10)
            return;
        String defaultName = "Run @ " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String runName = JOptionPane.showInputDialog(this, "Enter run name:", defaultName);
        if (runName != null && !runName.trim().isEmpty()) {
            Map<String, Double> params = currentAlgorithm.getCurrentParameters();
            PerformanceMetrics metrics = PerformanceMetricsCalculator.calculate(data, initialValueForMetrics,
                    setpointForMetrics);
            SimulationRun run = new SimulationRun(runName, params, data, metrics);
            savedRuns.add(run);
            realTimeDisplayPanel.setStatus("'" + runName + "' saved successfully!");
        } else {
            realTimeDisplayPanel.setStatus("Run finished but not saved.");
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> new ControlVisualizer().setVisible(true));
    }
}
