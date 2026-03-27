package com.sgu.tuyensinh.worker;

import com.sgu.tuyensinh.dto.ThiSinhImportDTO;
import com.sgu.tuyensinh.entity.ThiSinh;
import com.sgu.tuyensinh.service.ThiSinhImportService;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ThiSinhImportWorker extends SwingWorker<Void, Integer> {

    private final File file;
    private final ThiSinhImportService service;
    private final JProgressBar progressBar;

    public ThiSinhImportWorker(File file, ThiSinhImportService service, JProgressBar progressBar) {
        this.file = file;
        this.service = service;
        this.progressBar = progressBar;
    }

    @Override
    protected Void doInBackground() {
        List<ThiSinhImportDTO> dtos = com.sgu.tuyensinh.util.ThiSinhExcelReaderUtil.read(file);

        List<ThiSinh> validList = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        int total = dtos.size();

        for (int i = 0; i < total; i++) {
            ThiSinhImportDTO dto = dtos.get(i);

            List<String> err = service.validate(dto);

            if (err.isEmpty()) {
                validList.add(service.toEntity(dto));
            } else {
                errors.add("Dòng " + (i + 1) + ": " + err.toString());
            }
            publish(i);
        }

        // SAVE
        service.saveAll(validList);

        // HIỂN THỊ LỖI
        if (!errors.isEmpty()) {
            JOptionPane.showMessageDialog(null, String.join("\n", errors));
        }

        return null;
    }

    @Override
    protected void process(List<Integer> chunks) {
        int i = chunks.get(chunks.size() - 1);
        progressBar.setValue(i);
    }
}