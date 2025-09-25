package org.example.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.example.algorithm.ControlAlgorithm;
import org.example.algorithm.PIDController;

public class ParameterPanel extends JPanel {
    private final ControlVisualizer controlVisualizer;
    private ControlAlgorithm algorithm;
    private final Map<String, JTextField> parameterFields = new HashMap<>();
    private JTextField setpointField;
    private JButton autoTuneButton;
    private JButton applyButton;

    // --- NEW: JComboBox for tuning rules ---
    private JComboBox<PIDController.TuningRule> tuningRuleComboBox;

    public ParameterPanel(ControlVisualizer visualizer) {
        this.controlVisualizer = visualizer;
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        int y = 0;

        // --- Setpoint ---
        gbc.gridx = 0;
        gbc.gridy = y;
        add(new JLabel("Setpoint:"), gbc);
        gbc.gridx = 1;
        setpointField = new JTextField("5.0", 10);
        add(setpointField, gbc);
        y++;

        // --- Apply Button ---
        gbc.gridy = y;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        applyButton = new JButton("Apply Parameters");
        applyButton.addActionListener(e -> applyParameters());
        add(applyButton, gbc);
        y++;

        // --- NEW: Tuning Rule ComboBox ---
        gbc.gridy = y;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        add(new JLabel("Tuning Rule:"), gbc);
        gbc.gridx = 1;
        tuningRuleComboBox = new JComboBox<>(PIDController.TuningRule.values());
        tuningRuleComboBox.setSelectedItem(PIDController.TuningRule.NO_OVERSHOOT); // Set default
        add(tuningRuleComboBox, gbc);
        y++;

        // --- Auto-Tune Button ---
        gbc.gridy = y;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        autoTuneButton = new JButton("Start Auto-Tuning");
        autoTuneButton.addActionListener(e -> startAutoTuning());
        add(autoTuneButton, gbc);
    }

    public void setAlgorithm(ControlAlgorithm algorithm) {
        this.algorithm = algorithm;
        // ... (rest of the method is the same, no changes needed here)
        for (JTextField field : parameterFields.values()) {
            int componentIndex = getComponentZOrder(field);
            if (componentIndex > 0) {
                remove(componentIndex - 1);
            }
            remove(field);
        }
        parameterFields.clear();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Start adding param fields below the buttons and combo box
        int row = 5;
        for (String paramName : algorithm.getParameterNames()) {
            gbc.gridx = 0;
            gbc.gridy = row;
            add(new JLabel(paramName + ":"), gbc);
            gbc.gridx = 1;
            JTextField field = new JTextField(10);
            parameterFields.put(paramName, field);
            add(field, gbc);
            row++;
        }
        updateParameterFields();
        revalidate();
        repaint();
    }

    // --- MODIFIED: Pass the selected rule to the visualizer ---
    private void startAutoTuning() {
        if (algorithm instanceof PIDController && controlVisualizer != null) {
            try {
                double setpoint = Double.parseDouble(setpointField.getText());
                // Get the selected rule from the combo box
                PIDController.TuningRule selectedRule = (PIDController.TuningRule) tuningRuleComboBox.getSelectedItem();
                // Pass both setpoint and the rule
                controlVisualizer.startAutoTune(setpoint, selectedRule);
            } catch (NumberFormatException e) {
                // Handle invalid setpoint
            }
        }
    }

    public void updateParameterFields() {
        if (algorithm == null)
            return;
        Map<String, Double> params = algorithm.getCurrentParameters();
        for (Map.Entry<String, JTextField> entry : parameterFields.entrySet()) {
            String paramName = entry.getKey();
            if (params.containsKey(paramName)) {
                entry.getValue().setText(String.format("%.3f", params.get(paramName)));
            }
        }
    }

    private void applyParameters() {
        if (algorithm == null)
            return;
        Map<String, Double> params = new HashMap<>();
        for (Map.Entry<String, JTextField> entry : parameterFields.entrySet()) {
            try {
                double value = Double.parseDouble(entry.getValue().getText());
                params.put(entry.getKey(), value);
            } catch (NumberFormatException ex) {
                /* Ignore invalid input */ }
        }
        algorithm.setParameters(params);
    }

    public double getSetpoint() {
        try {
            return Double.parseDouble(setpointField.getText());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    public void setButtonsEnabled(boolean enabled) {
        autoTuneButton.setEnabled(enabled);
        applyButton.setEnabled(enabled);
        tuningRuleComboBox.setEnabled(enabled); // Also disable/enable the combo box
    }
}
