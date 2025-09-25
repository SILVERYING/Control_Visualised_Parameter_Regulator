package org.example.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.TitledBorder;

/**
 * 实时数据显示面板 (数字、光柱、状态)
 */
public class RealTimeDisplayPanel extends JPanel {
    private final JLabel pvValueLabel;
    private final JLabel svValueLabel;
    private final JLabel outValueLabel;
    private final JProgressBar pvBar;
    private final JProgressBar svBar;
    private final JProgressBar outBar;
    private final JLabel statusLabel;

    public RealTimeDisplayPanel() {
        super(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 5, 2, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // --- PV ---
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(new JLabel("PV:"), gbc);
        pvValueLabel = new JLabel("0.000");
        gbc.gridx = 1;
        add(pvValueLabel, gbc);
        pvBar = new JProgressBar(-10, 10); // 范围可以根据实际情况调整
        gbc.gridx = 2;
        gbc.weightx = 1.0;
        add(pvBar, gbc);

        // --- SV ---
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        add(new JLabel("SV:"), gbc);
        svValueLabel = new JLabel("0.000");
        gbc.gridx = 1;
        add(svValueLabel, gbc);
        svBar = new JProgressBar(-10, 10);
        gbc.gridx = 2;
        gbc.weightx = 1.0;
        add(svBar, gbc);

        // --- OUT ---
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        add(new JLabel("OUT:"), gbc);
        outValueLabel = new JLabel("0.000");
        gbc.gridx = 1;
        add(outValueLabel, gbc);
        outBar = new JProgressBar(-100, 100); // 输出通常是 -100 到 100
        gbc.gridx = 2;
        gbc.weightx = 1.0;
        add(outBar, gbc);

        // --- Status ---
        statusLabel = new JLabel("Ready");
        statusLabel.setBorder(BorderFactory.createTitledBorder(null, "Status",
                TitledBorder.LEFT, TitledBorder.TOP));
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(10, 0, 0, 0);
        add(statusLabel, gbc);
    }

    /**
     * 更新显示的值
     * 
     * @param pv  过程量
     * @param sv  设定值
     * @param out 输出量
     */
    public void updateValues(double pv, double sv, double out) {
        pvValueLabel.setText(String.format("%.3f", pv));
        svValueLabel.setText(String.format("%.3f", sv));
        outValueLabel.setText(String.format("%.3f", out));

        // JProgressBar 只接受整数，需要转换
        pvBar.setValue((int) (pv * 10)); // 假设范围是-10到10，乘以10以提高精度
        svBar.setValue((int) (sv * 10));
        outBar.setValue((int) out);
    }

    /**
     * 设置状态栏文本
     * 
     * @param text 状态文本
     */
    public void setStatus(String text) {
        statusLabel.setText(text);
    }
}
