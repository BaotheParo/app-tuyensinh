package com.sgu.tuyensinh.admin.ui.common;

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
        // Ví dụ giả lập tiến trình import // này chỉ là mô phỏng, cần thay bằng logic thực tế để import file Excel
        for (int i = 0; i <= 100; i++) {
            Thread.sleep(50); // giả lập thời gian xử lý
            publish(i);       // gửi tiến trình
            setProgress(i);   // cập nhật progress
        }
        // TODO: gọi API backend để import file Excel thật sự
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