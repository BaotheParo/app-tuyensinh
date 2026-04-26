package com.sgu.tuyensinh.service;

import com.sgu.tuyensinh.dto.NguyenVongImportDTO;
import com.sgu.tuyensinh.entity.NguyenVong;
import com.sgu.tuyensinh.repository.NguyenVongRepository;
import com.sgu.tuyensinh.service.dto.ImportResultDTO;
import com.sgu.tuyensinh.util.NguyenVongExcelReaderUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sgu.tuyensinh.service.interfaces.ProgressCallback;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
    
@Service
public class NguyenVongImportService {

    private final NguyenVongRepository repository;

    public NguyenVongImportService(NguyenVongRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ImportResultDTO importNguyenVongFromExcel(InputStream inputStream, ProgressCallback callback) {
        ImportResultDTO result = new ImportResultDTO();

        // 0. Check inputStream
        if (inputStream == null) {
            result.addError("InputStream null (không có dữ liệu file)");
            return result;
        }

        try {
            // 1. Đọc Excel → DTO
            List<NguyenVongImportDTO> dtos = NguyenVongExcelReaderUtil.read(inputStream);
            List<NguyenVong> entitiesToSave = new ArrayList<>();

            if (dtos == null || dtos.isEmpty()) {
                result.addError("File Excel không có dữ liệu");
                return result;
            }

            // 2. Validate + map
            for (int i = 0; i < dtos.size(); i++) {
                NguyenVongImportDTO dto = dtos.get(i);

                List<String> validationErrors = validate(dto);

                if (validationErrors.isEmpty()) {
                    entitiesToSave.add(toEntity(dto));
                    result.incrementSuccess(); // hợp lệ
                } else {
                    result.incrementSkip(); // bỏ qua
                    result.addError("Dòng " + (i + 1) + ": " + String.join(", ", validationErrors));
                }
            }

            // 3. Chunking save DB
            int chunkSize = 1000;

            for (int i = 0; i < entitiesToSave.size(); i += chunkSize) {
                int end = Math.min(i + chunkSize, entitiesToSave.size());
                List<NguyenVong> chunk = entitiesToSave.subList(i, end);

                repository.saveAll(chunk);
            }

        } catch (Exception e) {
            e.printStackTrace();
            result.addError("Lỗi hệ thống: " + e.getMessage());
        }

        return result;
    }

    // ===== VALIDATE =====
    public List<String> validate(NguyenVongImportDTO dto) {
        List<String> errors = new ArrayList<>();

        if (dto.getCccd() == null || dto.getCccd().isEmpty()) {
            errors.add("CCCD trống");
        }

        if (dto.getMaNganh() == null || dto.getMaNganh().isEmpty()) {
            errors.add("Mã ngành trống");
        }

        if (dto.getThuTu() == null) {
            errors.add("Thứ tự NV trống");
        }

        return errors;
    }
    // ===== MAP DTO -> ENTITY =====
    public NguyenVong toEntity(NguyenVongImportDTO dto) {
        NguyenVong nv = new NguyenVong();

        nv.setNnCccd(dto.getCccd());
        nv.setNvManganh(dto.getMaNganh());
        nv.setNvTt(dto.getThuTu());

        nv.setTtPhuongthuc(dto.getPhuongThuc());
        nv.setTtThm(dto.getToHopMon());

        nv.setDiemThxt(dto.getDiemThxt());
        nv.setDiemUtqd(dto.getDiemUtqd());
        nv.setDiemCong(dto.getDiemCong());
        nv.setDiemXetTuyen(dto.getDiemXetTuyen());

        nv.setNvKetQua(null); // chưa xét

        // key unique (tùy bạn định nghĩa)
        nv.setNvKeys(dto.getCccd() + "_" + dto.getMaNganh() + "_" + dto.getThuTu());

        return nv;
    }

    // ===== SAVE =====
    public void saveAll(List<NguyenVong> list) {
        repository.saveAll(list);
    }
}