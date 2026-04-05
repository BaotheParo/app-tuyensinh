package com.sgu.tuyensinh.worker;

import com.sgu.tuyensinh.service.ThiSinhImportService;

import javax.swing.*;
import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Senior Backend Architect: Tái cấu trúc Tầng UI (Worker)
 * Worker này chỉ đóng vai trò cầu nối, gọi Service và hiển thị kết quả.
 * Mọi logic nghiệp vụ và đọc file đã được chuyển xuống tầng Service.
 */
public class ThiSinhImportWorker extends SwingWorker<List<String>, Void> {

    private final File file;
    private final ThiSinhImportService service;
    private final JProgressBar progressBar;

    public ThiSinhImportWorker(File file, ThiSinhImportService service, JProgressBar progressBar) {
        this.file = file;
        this.service = service;
        this.progressBar = progressBar;
    }

    @Override
    protected List<String> doInBackground() {
        if (progressBar != null) {
            progressBar.setIndeterminate(true); // Hiển thị trạng thái đang xử lý
        }
        
        // UI chỉ gửi đường dẫn file xuống Service
        return service.importThiSinhFromExcel(file.getAbsolutePath());
    }

    @Override
    protected void done() {
        if (progressBar != null) {
            progressBar.setIndeterminate(false);
            progressBar.setValue(100);
        }

        try {
            List<String> errors = get();

            if (errors.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Thành công: Đã nạp dữ liệu hoàn tất!", 
                        "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            } else {
                // Hiển thị danh sách lỗi nếu có
                String errorMsg = String.join("\n", errors.subList(0, Math.min(errors.size(), 10)));
                if (errors.size() > 10) {
                    errorMsg += "\n... và " + (errors.size() - 10) + " lỗi khác.";
                }
                JOptionPane.showMessageDialog(null, "Import hoàn tất nhưng có lỗi:\n" + errorMsg, 
                        "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            }
        } catch (InterruptedException | ExecutionException e) {
            JOptionPane.showMessageDialog(null, "Lỗi hệ thống khi import: " + e.getMessage(), 
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}