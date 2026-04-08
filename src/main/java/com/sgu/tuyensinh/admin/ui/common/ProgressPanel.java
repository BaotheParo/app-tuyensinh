package com.sgu.tuyensinh.admin.ui.common;

import javax.swing.*;
import java.awt.*;

public class ProgressPanel extends JPanel {
    private JProgressBar progressBar;
    private JLabel statusLabel;

    public ProgressPanel(int total) {
        setLayout(new BorderLayout());

        // Thanh tiến trình theo % (0-100)
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);

        statusLabel = new JLabel("Chưa bắt đầu");

        add(progressBar, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);
    }

    // Hàm cập nhật tiến trình
    public void updateProgress(int current, int total) {
        if (total > 0) {
            int percent = (int) ((current * 100.0) / total); // tính %
            progressBar.setValue(percent); // thanh hiển thị %
        }
        statusLabel.setText("Đang tính điểm cho thí sinh " + current + "/" + total); // label hiển thị X/Y
    }

    // Hàm khi hoàn tất
    public void finish(int total) {
        progressBar.setValue(100); // đảm bảo lên 100%
        statusLabel.setText("Hoàn tất tính điểm cho " + total + " thí sinh!");
    }

    public JProgressBar getProgressBar() {
        return progressBar;
    }

    public JLabel getStatusLabel() {
        return statusLabel;
    }
}