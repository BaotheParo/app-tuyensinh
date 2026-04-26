package com.sgu.tuyensinh.admin.ui.common;

import com.sgu.tuyensinh.service.dto.ImportResultDTO;
import com.sgu.tuyensinh.service.interfaces.ProgressCallback;

import javax.swing.*;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.function.BiFunction;

public class ImportWorker extends SwingWorker<ImportResultDTO, int[]> {

    private final String   filePath;
    private final ProgressPanel progressPanel;
    private final BiFunction<InputStream, ProgressCallback, ImportResultDTO> importFunction;

    private long lastPublish = 0; // throttle publish

    public ImportWorker(String filePath,
                        ProgressPanel progressPanel,
                        BiFunction<InputStream, ProgressCallback, ImportResultDTO> importFunction) {
        this.filePath      = filePath;
        this.progressPanel = progressPanel;
        this.importFunction = importFunction;
    }

    @Override
    protected ImportResultDTO doInBackground() throws Exception {
        try (FileInputStream fis = new FileInputStream(filePath)) {
            return importFunction.apply(fis, (current, total) -> {
                long now = System.currentTimeMillis();
                if (current == total || now - lastPublish >= 50) {
                    publish(new int[]{current, total});
                    lastPublish = now;
                }
            });
        }
    }

    @Override
    protected void process(java.util.List<int[]> chunks) {
        int[] latest = chunks.get(chunks.size() - 1);
        progressPanel.updateProgress(latest[0], latest[1]);
    }

    // done() được override bởi ImportPanel để hiển thị kết quả
    @Override
    protected void done() { }
}