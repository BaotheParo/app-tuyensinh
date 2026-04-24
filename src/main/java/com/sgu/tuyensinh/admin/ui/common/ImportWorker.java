package com.sgu.tuyensinh.admin.ui.common;

import java.io.FileInputStream;
import javax.swing.*;
import com.sgu.tuyensinh.service.dto.ImportResultDTO;
import java.io.InputStream;
import java.util.function.Function;

public class ImportWorker extends SwingWorker<ImportResultDTO, Integer> {

    private final String filePath;
    private final ProgressPanel progressPanel;
    private final Function<InputStream, ImportResultDTO> importFunction;

    public ImportWorker(String filePath,
                        ProgressPanel progressPanel,
                        Function<InputStream, ImportResultDTO> importFunction) {
        this.filePath       = filePath;
        this.progressPanel  = progressPanel;
        this.importFunction = importFunction;
    }

    @Override
    protected ImportResultDTO doInBackground() throws Exception {
        try (FileInputStream fis = new FileInputStream(filePath)) {
            ImportResultDTO result = importFunction.apply(fis);
            int total = result.getSuccessCount() + result.getErrors().size();
            progressPanel.finish(total);
            return result;
        }
    }

    // done() được override bởi ImportPanel để cập nhật UI
    @Override
    protected void done() { }
}
