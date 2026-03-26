package com.sgu.tuyensinh.admin.ui.common;

import java.io.FileInputStream;
import org.apache.poi.ss.usermodel.*;   //phải thêm thư viện âpche vào pom.xml
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import javax.swing.*;

public class ImportWorker extends SwingWorker<Void, Integer> {
    private String filePath;
    private JProgressBar progressBar;

    public ImportWorker(String filePath, JProgressBar progressBar) {
        this.filePath = filePath;
        this.progressBar = progressBar;
    }

    @Override
    protected Void doInBackground() throws Exception {
        try (FileInputStream fis = new FileInputStream(filePath);
            Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            int totalRows = sheet.getPhysicalNumberOfRows();

            for (int i = 0; i < totalRows; i++) {
                Row row = sheet.getRow(i);
                Cell cell = row.getCell(0);
                String value = cell.getStringCellValue();
                // gọi API backend để lưu dữ liệu...

                // cập nhật tiến trình
                int progress = (int) ((i + 1) * 100.0 / totalRows);
                publish(progress);
                setProgress(progress);
            }
        }
        return null;
    }


    @Override
    protected void process(java.util.List<Integer> chunks) {
        // Cập nhật thanh tiến trình
        int latest = chunks.get(chunks.size() - 1);
        progressBar.setValue(latest);
    }

    @Override
    protected void done() {
        JOptionPane.showMessageDialog(progressBar.getParent(), "Import hoàn tất!");
    }
}