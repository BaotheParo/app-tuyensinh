package com.sgu.tuyensinh.service;

import com.sgu.tuyensinh.dto.ThiSinhImportDTO;
import com.sgu.tuyensinh.entity.ThiSinh;
import com.sgu.tuyensinh.repository.ThiSinhRepository;
import com.sgu.tuyensinh.util.ThiSinhExcelReaderUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class ThiSinhImportService {

    private final ThiSinhRepository repository;

    public ThiSinhImportService(ThiSinhRepository repository) {
        this.repository = repository;
    }

    /**
     * Senior Backend Architect: Refactor MVC & Performance
     * Hàm này xử lý logic import, tách biệt hoàn toàn với UI.
     * Sử dụng kỹ thuật Batch Insert (Chunking) để xử lý 43,000 dòng mà không làm treo máy.
     */
    @Transactional
    public List<String> importThiSinhFromExcel(String filePath) {
        List<String> errors = new ArrayList<>();
        File file = new File(filePath);

        if (!file.exists()) {
            errors.add("File không tồn tại: " + filePath);
            return errors;
        }

        // 1. Đọc toàn bộ DTO từ Excel qua Util của Lead
        List<ThiSinhImportDTO> dtos = ThiSinhExcelReaderUtil.read(file);
        List<ThiSinh> entitiesToSave = new ArrayList<>();

        // 2. Validate và chuyển đổi sang Entity
        for (int i = 0; i < dtos.size(); i++) {
            ThiSinhImportDTO dto = dtos.get(i);
            List<String> validationErrors = validate(dto);
            
            if (validationErrors.isEmpty()) {
                entitiesToSave.add(toEntity(dto));
            } else {
                errors.add("Dòng " + (i + 1) + ": " + String.join(", ", validationErrors));
            }
        }

        // 3. Kỹ thuật Chunking (Chia để trị):
        // Thay vì gọi saveAll(43000), ta chia thành từng nhóm 1000 dòng.
        // Việc này giúp Hibernate và DB handle transaction mượt mà hơn, tránh lỗi tràn bộ nhớ đệm (Cache).
        int chunkSize = 1000;
        for (int i = 0; i < entitiesToSave.size(); i += chunkSize) {
            int end = Math.min(i + chunkSize, entitiesToSave.size());
            List<ThiSinh> chunk = entitiesToSave.subList(i, end);
            
            repository.saveAll(chunk);
            // repository.flush(); // Có thể dùng nếu cần giải phóng persist context ngay lập tức
        }

        return errors;
    }

    // ====== VALIDATE ======
    public List<String> validate(ThiSinhImportDTO dto) {
        List<String> errors = new ArrayList<>();
        if (dto.getCccd() == null || dto.getCccd().isEmpty()) {
            errors.add("CCCD bị trống");
        }
        if (dto.getHoTen() == null || dto.getHoTen().isEmpty()) {
            errors.add("Họ tên bị trống");
        }
        return errors;
    }   

    // ====== MAP DTO -> ENTITY ======
    public ThiSinh toEntity(ThiSinhImportDTO dto) {
        ThiSinh ts = new ThiSinh();
        ts.setId(dto.getCccd());
        ts.setHoTen(dto.getHoTen());
        ts.setGioiTinh(dto.getGioiTinh());
        ts.setMaTinh(dto.getMaTinh());
        ts.setMaTruong(dto.getMaTruong());
        ts.setDoiTuongUt(dto.getDoiTuongUt());
        ts.setKhuVucUt(dto.getKhuVucUt());
        ts.setNgaySinh(parseDate(dto.getNgaySinh()));
        return ts;
    }

    private LocalDate parseDate(String date) {
        try {
            return LocalDate.parse(date, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (Exception e) {
            return null;
        }
    }
}