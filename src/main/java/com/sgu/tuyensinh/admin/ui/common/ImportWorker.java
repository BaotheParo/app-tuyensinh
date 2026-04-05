package com.sgu.tuyensinh.admin.ui.common;

import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import org.apache.poi.ss.usermodel.*; //phải thêm thư viện âpche vào pom.xml
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import javax.swing.*;

public class ImportWorker extends SwingWorker<Void, Integer> {
    private String filePath;
    private ProgressPanel progressPanel;
    private int totalRows;

    public ImportWorker(String filePath, ProgressPanel progressPanel) {
        this.filePath = filePath;
        this.progressPanel = progressPanel;
    }

    @Override
    protected Void doInBackground() throws Exception {
        try (FileInputStream fis = new FileInputStream(filePath);
                Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            totalRows = sheet.getPhysicalNumberOfRows();

            // Lấy dòng đầu tiên làm header (tên cột)
            Row headerRow = sheet.getRow(0);
            int totalCols = headerRow.getPhysicalNumberOfCells();

            for (int i = 1; i < totalRows; i++) { // bắt đầu từ dòng 1, bỏ header
                Row row = sheet.getRow(i);
                if (row == null)
                    continue;

                // Xây dựng JSON từ tất cả các cột
                StringBuilder jsonBuilder = new StringBuilder("{");
                for (int j = 0; j < totalCols; j++) {
                    Cell headerCell = headerRow.getCell(j);
                    Cell dataCell = row.getCell(j);

                    if (headerCell == null || dataCell == null)
                        continue;

                    String key = headerCell.getStringCellValue();
                    String value;

                    switch (dataCell.getCellType()) {
                        case STRING:
                            value = dataCell.getStringCellValue();
                            break;
                        case NUMERIC:
                            if (DateUtil.isCellDateFormatted(dataCell)) {
                                value = new SimpleDateFormat("yyyy-MM-dd").format(dataCell.getDateCellValue());
                            } else {
                                value = String.valueOf(dataCell.getNumericCellValue());
                            }
                            break;
                        case BOOLEAN:
                            value = String.valueOf(dataCell.getBooleanCellValue());
                            break;
                        default:
                            value = "";
                    }

                    jsonBuilder.append("\"")
                            .append(key)
                            .append("\":\"")
                            .append(value)
                            .append("\"");

                    if (j < totalCols - 1) {
                        jsonBuilder.append(", ");
                    }
                }
                jsonBuilder.append("}");

                String jsonInputString = jsonBuilder.toString();

                // --- Gọi API backend ---
                try {
                    URL url = new URL("http://localhost:8080/api/import");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json; utf-8");
                    conn.setDoOutput(true);

                    try (OutputStream os = conn.getOutputStream()) {
                        byte[] input = jsonInputString.getBytes("utf-8");
                        os.write(input, 0, input.length);
                    }

                    int code = conn.getResponseCode();
                    if (code != HttpURLConnection.HTTP_OK && code != HttpURLConnection.HTTP_CREATED) {
                        System.err.println("Import thất bại cho dòng " + i + ": " + code);
                    }

                    conn.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // --- Cập nhật tiến trình ---
                publish(i); // gửi số dòng hiện tại
                setProgress((int) ((i + 1) * 100.0 / totalRows)); // vẫn có thể set % nếu muốn
            }
        }
        return null;
    }

    @Override
    protected void process(java.util.List<Integer> chunks) {
        // Cập nhật thanh tiến trình
        int currentRow = chunks.get(chunks.size() - 1);
        progressPanel.updateProgress(currentRow, totalRows);

    }

    @Override
    protected void done() {
        progressPanel.finish(totalRows);
        JOptionPane.showMessageDialog(progressPanel.getParent(), "Import hoàn tất!");
    }
}