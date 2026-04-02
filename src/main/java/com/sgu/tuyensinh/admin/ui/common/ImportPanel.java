package com.sgu.tuyensinh.admin.ui.common;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;

public class ImportPanel extends JPanel {
    private JTextField filePathField;
    private JButton browseButton;
    private JButton importButton;
    private JProgressBar progressBar;

    public ImportPanel() {
        setLayout(new BorderLayout());

        // Panel chọn file
        JPanel filePanel = new JPanel(new BorderLayout());
        filePathField = new JTextField();
        browseButton = new JButton("Browse...");
        filePanel.add(filePathField, BorderLayout.CENTER);
        filePanel.add(browseButton, BorderLayout.EAST);

        // Nút Import
        importButton = new JButton("Import");

        // Thanh tiến trình
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);

        // Thêm vào panel chính
        add(filePanel, BorderLayout.NORTH);
        add(importButton, BorderLayout.CENTER);
        add(progressBar, BorderLayout.SOUTH);

        // Sự kiện chọn file
        browseButton.addActionListener((ActionEvent e) -> {
            JFileChooser chooser = new JFileChooser();
            int result = chooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = chooser.getSelectedFile();
                filePathField.setText(selectedFile.getAbsolutePath());
            }
        });

        // Sự kiện Import
        importButton.addActionListener((ActionEvent e) -> {
            String path = filePathField.getText();
            if (path == null || path.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn file Excel!");
                return;
            }
            // Chạy worker
            ImportWorker worker = new ImportWorker(path, progressBar);
            worker.execute();
        });
    }
}