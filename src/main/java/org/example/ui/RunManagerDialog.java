package org.example.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.example.data.SimulationRun;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class RunManagerDialog extends JDialog {
    private final ControlVisualizer owner;
    private final DefaultListModel<SimulationRun> listModel;
    private final JList<SimulationRun> runList;
    private final JTextArea detailsArea;

    public RunManagerDialog(ControlVisualizer owner, List<SimulationRun> runs) {
        super(owner, "Run Manager", true);
        this.owner = owner;
        setSize(800, 500);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        listModel = new DefaultListModel<>();
        runs.forEach(listModel::addElement);
        runList = new JList<>(listModel);
        runList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        runList.addListSelectionListener(e -> updateDetails());

        detailsArea = new JTextArea();
        detailsArea.setEditable(false);
        detailsArea.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 12));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(runList), new JScrollPane(detailsArea));
        splitPane.setDividerLocation(250);
        add(splitPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton displayButton = new JButton("Display on Chart");
        displayButton.addActionListener(e -> displaySelectedRun());
        JButton deleteButton = new JButton("Delete");
        deleteButton.addActionListener(e -> deleteSelectedRun());
        JButton clearComparisonsButton = new JButton("Clear Comparisons");
        clearComparisonsButton.addActionListener(e -> owner.getChartPanel().clearComparisonRuns());
        JButton saveToFileButton = new JButton("Save to File...");
        saveToFileButton.addActionListener(e -> saveRunsToFile());
        JButton loadFromFileButton = new JButton("Load from File...");
        loadFromFileButton.addActionListener(e -> loadRunsFromFile());

        buttonPanel.add(loadFromFileButton);
        buttonPanel.add(saveToFileButton);
        buttonPanel.add(clearComparisonsButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(displayButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void updateDetails() {
        SimulationRun selected = runList.getSelectedValue();
        if (selected == null) {
            detailsArea.setText("");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(" ===== ").append(selected.getName()).append(" ===== ");
        sb.append("--- Parameters ---");
        selected.getParameters().forEach((key, value) -> sb.append(String.format("%s: %.3f ", key, value)));
        sb.append(" --- Performance Metrics ---\n");
        sb.append(selected.getMetrics().toString());

        detailsArea.setText(sb.toString());
    }

    private void displaySelectedRun() {
        SimulationRun selected = runList.getSelectedValue();
        if (selected != null) {
            owner.getChartPanel().displayComparisonRun(selected);
        }
    }

    private void deleteSelectedRun() {
        SimulationRun selected = runList.getSelectedValue();
        if (selected != null) {
            owner.getSavedRuns().remove(selected);
            listModel.removeElement(selected);
        }
    }

    private void saveRunsToFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Runs to JSON File");
        chooser.setFileFilter(new FileNameExtensionFilter("JSON Files", "json"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            try (FileWriter writer = new FileWriter(chooser.getSelectedFile())) {
                gson.toJson(owner.getSavedRuns(), writer);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error saving file: " + e.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void loadRunsFromFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Load Runs from JSON File");
        chooser.setFileFilter(new FileNameExtensionFilter("JSON Files", "json"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            Gson gson = new Gson();

            // ==================== THE FIX IS HERE ====================
            // Use the fully qualified name to avoid ambiguity with java.awt.Window.Type
            java.lang.reflect.Type runListType = new TypeToken<List<SimulationRun>>() {
            }.getType();
            // =========================================================

            try (FileReader reader = new FileReader(chooser.getSelectedFile())) {
                List<SimulationRun> loadedRuns = gson.fromJson(reader, runListType);
                owner.setSavedRuns(loadedRuns);
                listModel.clear();
                loadedRuns.forEach(listModel::addElement);
            } catch (IOException | com.google.gson.JsonSyntaxException e) {
                JOptionPane.showMessageDialog(this, "Error loading or parsing file: " + e.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
