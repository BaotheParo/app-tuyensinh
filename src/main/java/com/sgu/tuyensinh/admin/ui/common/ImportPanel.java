package com.sgu.tuyensinh.admin.ui.common;

import javax.swing.*;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.usermodel.Sheet;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;

public class ImportPanel extends JPanel {
    private JTextField filePathField;
    private JButton browseButton;
    private JButton importButton;
    private ProgressPanel progressPanel;

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
        progressPanel = new ProgressPanel(100); // khởi tẹo mặc địng :)))

        // Thêm vào panel chính
        add(filePanel, BorderLayout.NORTH);
        add(importButton, BorderLayout.CENTER);
        add(progressPanel, BorderLayout.SOUTH);

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

            // Đếm số dòng trong file Excel
            int totalRows = ExcelUtils.countRows(path);

            // Khởi tạo lại ProgressPanel với số dòng thực tế
            progressPanel = new ProgressPanel(totalRows);
            add(progressPanel, BorderLayout.SOUTH);
            revalidate();
            repaint();

            // Chạy worker
            ImportWorker worker = new ImportWorker(path, progressPanel);
            worker.execute();
        });
    }
}
