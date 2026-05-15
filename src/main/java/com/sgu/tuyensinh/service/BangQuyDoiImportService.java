package com.sgu.tuyensinh.service;

import com.sgu.tuyensinh.dto.QuyDoiNNImportDTO;
import com.sgu.tuyensinh.entity.BangQuyDoi;
import com.sgu.tuyensinh.entity.Nganh;
import com.sgu.tuyensinh.repository.BangQuyDoiRepository;
import com.sgu.tuyensinh.service.dto.ImportResultDTO;
import com.sgu.tuyensinh.service.interfaces.IImportService;
import com.sgu.tuyensinh.service.interfaces.ProgressCallback;

import com.sgu.tuyensinh.util.ExcelReaderUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sgu.tuyensinh.dto.NganhImportDTO;


import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;

/**
 * Service import bảng quy đổi (overwrite + tối ưu batch)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BangQuyDoiImportService implements IImportService {

    private final BangQuyDoiRepository bangQuyDoiRepository;

    @Override
    @Transactional
    public ImportResultDTO importFromExcel(InputStream inputStream, ProgressCallback callback) {
        ImportResultDTO result = new ImportResultDTO();

        if (inputStream == null) {
            result.addError("InputStream không được để null");
            return result;
        }

        try (Workbook workbook = WorkbookFactory.create(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null || sheet.getLastRowNum() < 1) {
                log.warn("File Excel không có dữ liệu");
                return result;
            }

            List<BangQuyDoi> validList = new ArrayList<>();

            // ===== 1. READ + VALIDATE =====
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                QuyDoiNNImportDTO dto = readRowToDto(row);

                String error = validate(dto, i + 1);
                if (error != null) {
                    result.addError(error);
                    result.incrementSkip();
                    continue;
                }

                validList.add(convertToEntity(dto));
            }

            if (validList.isEmpty()) {
                return result;
            }

            // ===== 2. LOAD DATA CŨ =====
            List<BangQuyDoi> existingList = bangQuyDoiRepository.findAll();

            Map<String, BangQuyDoi> existingMap = new HashMap<>();
            for (BangQuyDoi e : existingList) {
                String key = buildKey(
                        e.getPhuongThuc(),
                        e.getToHop(),
                        e.getMon(),
                        e.getMaQuyDoi()
                );
                existingMap.put(key, e);
            }

            // ===== 3. MERGE (UPDATE / INSERT) =====
            List<BangQuyDoi> toSave = new ArrayList<>();

            for (BangQuyDoi newEntity : validList) {

                String key = buildKey(
                        newEntity.getPhuongThuc(),
                        newEntity.getToHop(),
                        newEntity.getMon(),
                        newEntity.getMaQuyDoi()
                );

                if (existingMap.containsKey(key)) {
                    // 🔥 UPDATE
                    BangQuyDoi old = existingMap.get(key);

                    old.setDiemGocA(newEntity.getDiemGocA());
                    old.setDiemGocB(newEntity.getDiemGocB());
                    old.setDiemQuyDoiC(newEntity.getDiemQuyDoiC());
                    old.setDiemQuyDoiD(newEntity.getDiemQuyDoiD());
                    old.setPhanVi(newEntity.getPhanVi());

                    toSave.add(old);
                } else {
                    // 🆕 INSERT
                    toSave.add(newEntity);
                }

                result.incrementSuccess();
            }

            // ===== 4. SAVE BATCH =====
            bangQuyDoiRepository.saveAll(toSave);

            log.info("Import thành công: {}", result);

        } catch (Exception e) {
            log.error("Lỗi import Excel", e);
            result.addError("Lỗi hệ thống: " + e.getMessage());
        }

        return result;
    }

    // ================= HELPER =================

    private String buildKey(String phuongThuc, String toHop, String mon, String maQuyDoi) {
        return (safe(phuongThuc) + "|" +
                safe(toHop) + "|" +
                safe(mon) + "|" +
                safe(maQuyDoi)).toLowerCase();
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }

    private QuyDoiNNImportDTO readRowToDto(Row row) {
        QuyDoiNNImportDTO dto = new QuyDoiNNImportDTO();

        dto.setPhuongThuc(ExcelReaderUtil.getSafeString(row.getCell(1)));
        dto.setToHop(ExcelReaderUtil.getSafeString(row.getCell(2)));
        dto.setMon(ExcelReaderUtil.getSafeString(row.getCell(3)));
        dto.setDiemA(ExcelReaderUtil.getSafeDouble(row.getCell(4)));
        dto.setDiemB(ExcelReaderUtil.getSafeDouble(row.getCell(5)));
        dto.setDiemC(ExcelReaderUtil.getSafeDouble(row.getCell(6)));
        dto.setDiemD(ExcelReaderUtil.getSafeDouble(row.getCell(7)));
        dto.setMaQuyDoi(ExcelReaderUtil.getSafeString(row.getCell(8)));
        dto.setPhanVi(ExcelReaderUtil.getSafeString(row.getCell(9)));

        return dto;
    }

    private String validate(QuyDoiNNImportDTO dto, int rowNum) {
        if (dto.getPhuongThuc() == null || dto.getPhuongThuc().isBlank()) {
            return "Dòng " + rowNum + ": Phương thức không được trống";
        }

        if (dto.getMaQuyDoi() == null || dto.getMaQuyDoi().isBlank()) {
            return "Dòng " + rowNum + ": Mã quy đổi không được trống";
        }

        if (dto.getDiemA() == null) {
            return "Dòng " + rowNum + ": Điểm A không được trống";
        }

        return null;
    }

    private BangQuyDoi convertToEntity(QuyDoiNNImportDTO dto) {
        BangQuyDoi entity = new BangQuyDoi();

        entity.setPhuongThuc(dto.getPhuongThuc().trim());
        entity.setToHop(dto.getToHop() != null ? dto.getToHop().trim() : null);
        entity.setMon(dto.getMon() != null ? dto.getMon().trim() : null);
        entity.setDiemGocA(dto.getDiemA());
        entity.setDiemGocB(dto.getDiemB());
        entity.setDiemQuyDoiC(dto.getDiemC());
        entity.setDiemQuyDoiD(dto.getDiemD());
        entity.setMaQuyDoi(dto.getMaQuyDoi());
        entity.setPhanVi(dto.getPhanVi() != null ? dto.getPhanVi().trim() : null);

        return entity;
    }

    public Page<BangQuyDoi> layDanhSachPhanTrang(int page, int size, String keyword) {
        Pageable pageable = PageRequest.of(page, size);

        if (keyword != null && !keyword.trim().isEmpty()) {
            return bangQuyDoiRepository
                    .findByPhuongThucContainingIgnoreCaseOrMonContainingIgnoreCase(
                            keyword, keyword, pageable);
        }

        return bangQuyDoiRepository.findAll(pageable);
    }

    public boolean isEmpty() {
        return bangQuyDoiRepository.count() == 0;
    }

    @Transactional
    public void saveSampleData() {
        List<BangQuyDoi> samples = new ArrayList<>();

        // IELTS conversion
        samples.add(createSample("CERT", null, "IELTS", 5.0, null, 8.0, null, "I50", "Quy đổi IELTS 5.0 -> 8.0"));
        samples.add(createSample("CERT", null, "IELTS", 5.5, null, 8.5, null, "I55", "Quy đổi IELTS 5.5 -> 8.5"));
        samples.add(createSample("CERT", null, "IELTS", 6.0, null, 9.0, null, "I60", "Quy đổi IELTS 6.0 -> 9.0"));
        samples.add(createSample("CERT", null, "IELTS", 6.5, null, 9.5, null, "I65", "Quy đổi IELTS 6.5 -> 9.5"));
        samples.add(createSample("CERT", null, "IELTS", 7.0, null, 10.0, null, "I70", "Quy đổi IELTS 7.0 -> 10.0"));

        // V-SAT conversion (Sample steps)
        samples.add(createSample("VSAT", null, "TOAN", 300.0, null, 7.0, null, "V300", "V-SAT Toán 300 -> 7.0"));
        samples.add(createSample("VSAT", null, "TOAN", 350.0, null, 8.0, null, "V350", "V-SAT Toán 350 -> 8.0"));
        samples.add(createSample("VSAT", null, "TOAN", 400.0, null, 9.0, null, "V400", "V-SAT Toán 400 -> 9.0"));
        samples.add(createSample("VSAT", null, "TOAN", 450.0, null, 10.0, null, "V450", "V-SAT Toán 450 -> 10.0"));

        bangQuyDoiRepository.saveAll(samples);
        log.info("Đã nạp {} dòng dữ liệu quy đổi mẫu.", samples.size());
    }

    private BangQuyDoi createSample(String pt, String th, String mon, Double a, Double b, Double c, Double d, String code, String note) {
        BangQuyDoi entity = new BangQuyDoi();
        entity.setPhuongThuc(pt);
        entity.setToHop(th);
        entity.setMon(mon);
        entity.setDiemGocA(a);
        entity.setDiemGocB(b);
        entity.setDiemQuyDoiC(c);
        entity.setDiemQuyDoiD(d);
        entity.setMaQuyDoi(code);
        entity.setPhanVi(note);
        return entity;
    }
}