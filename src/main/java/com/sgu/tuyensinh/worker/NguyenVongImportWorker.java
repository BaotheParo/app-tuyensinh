package com.sgu.tuyensinh.worker;

import com.sgu.tuyensinh.dto.NguyenVongImportDTO;
import com.sgu.tuyensinh.entity.NguyenVong;
import com.sgu.tuyensinh.service.NguyenVongImportService;
import com.sgu.tuyensinh.util.NguyenVongExcelReaderUtil;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class NguyenVongImportWorker extends SwingWorker<Void, Integer> {

    private final File file;
    private final NguyenVongImportService service;
    private final JProgressBar progressBar;

    public NguyenVongImportWorker(File file,
            NguyenVongImportService service,
            JProgressBar progressBar) {
        this.file = file;
        this.service = service;
        this.progressBar = progressBar;
    }

    @Override
    protected Void doInBackground() {

        List<NguyenVongImportDTO> dtos = NguyenVongExcelReaderUtil.read(file);

        List<NguyenVong> validList = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        int total = dtos.size();

        for (int i = 0; i < total; i++) {
            NguyenVongImportDTO dto = dtos.get(i);

            List<String> err = service.validate(dto);

            if (err.isEmpty()) {
                validList.add(service.toEntity(dto));
            } else {
                errors.add("Dòng " + (i + 1) + ": " + err);
            }

            publish(i * 100 / total); // progress %
        }

        // SAVE
        service.saveAll(validList);

        // SHOW ERROR
        if (!errors.isEmpty()) {
            JOptionPane.showMessageDialog(null, String.join("\n", errors));
        }

        return null;
    }

    @Override
    protected void process(List<Integer> chunks) {
        int value = chunks.get(chunks.size() - 1);
        progressBar.setValue(value);
    }
}