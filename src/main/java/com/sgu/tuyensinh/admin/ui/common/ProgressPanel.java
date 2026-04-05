package com.sgu.tuyensinh.admin.ui.common;

import javax.swing.*;
import java.awt.*;

public class ProgressPanel extends JPanel {
    private JProgressBar progressBar;
    private JLabel statusLabel;

    public ProgressPanel(int total) {
        setLayout(new BorderLayout());

        progressBar = new JProgressBar(0, total);
        progressBar.setStringPainted(true);

        statusLabel = new JLabel("Chưa bắt đầu");

        add(progressBar, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);
    }

    // Hàm cập nhật tiến trình
    public void updateProgress(int current, int total) {
        progressBar.setValue(current);
        statusLabel.setText("Đang tính điểm cho thí sinh " + current + "/" + total);
    }

    // Hàm khi hoàn tất
    public void finish(int total) {
        statusLabel.setText("Hoàn tất tính điểm cho " + total + " thí sinh!");
    }

    // Getter để SwingWorker gọi
    public JProgressBar getProgressBar() {
        return progressBar;
    }

    public JLabel getStatusLabel() {
        return statusLabel;
    }
}