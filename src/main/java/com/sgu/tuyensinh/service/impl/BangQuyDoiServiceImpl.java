package com.sgu.tuyensinh.service.impl;

import com.sgu.tuyensinh.dto.QuyDoiNNImportDTO;
import com.sgu.tuyensinh.entity.BangQuyDoi;
import com.sgu.tuyensinh.repository.BangQuyDoiRepository;
import com.sgu.tuyensinh.service.dto.ImportResultDTO;
import com.sgu.tuyensinh.service.interfaces.IImportService;
import com.sgu.tuyensinh.util.ExcelReaderUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Import Bảng Quy đổi Tiếng Anh / Ngoại ngữ từ Excel.
 * Đặt tại: service/impl/QuyDoiTiengAnhServiceImpl.java
 *
 * Lưu ý: code gốc có lỗi cú pháp (thiếu method wrapper, dùng bangQuyDoiRepository.save()
 * từng dòng). Đã sửa thành saveAll(validList) theo chuẩn PRD v3.0.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BangQuyDoiServiceImpl implements IImportService {

    private final BangQuyDoiRepository bangQuyDoiRepository;

    @Override
    @Transactional
    public ImportResultDTO importFromExcel(InputStream inputStream) {
        ImportResultDTO result = new ImportResultDTO();

        if (inputStream == null) {
            result.addError("InputStream không được để null");
            return result;
        }

        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null || sheet.getLastRowNum() < 1) {
                log.warn("File Excel BảngQuyĐổi không có dữ liệu");
                return result;
            }

            List<BangQuyDoi> validList = new ArrayList<>();

            // Bước 1: Đọc toàn bộ → validate từng dòng → gom lỗi
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

            // Bước 2: saveAll danh sách hợp lệ (thay vì save() từng dòng)
            if (!validList.isEmpty()) {
                bangQuyDoiRepository.saveAll(validList);
                validList.forEach(b -> result.incrementSuccess());
                log.info("Import BảngQuyĐổi: {}", result);
            }

        } catch (Exception e) {
            log.error("Lỗi import BảngQuyĐổi từ Excel", e);
            result.addError("Lỗi hệ thống: " + e.getMessage());
        }

        return result;
    }

    private QuyDoiNNImportDTO readRowToDto(Row row) {
        QuyDoiNNImportDTO dto = new QuyDoiNNImportDTO();
        // Cột 0: STT (bỏ qua)
        dto.setPhuongThuc(ExcelReaderUtil.getSafeString(row.getCell(1)));
        dto.setToHop(ExcelReaderUtil.getSafeString(row.getCell(2)));
        dto.setMon(ExcelReaderUtil.getSafeString(row.getCell(3)));
        dto.setDiemA(ExcelReaderUtil.getSafeDouble(row.getCell(4)));
        dto.setDiemB(ExcelReaderUtil.getSafeDouble(row.getCell(5)));
        dto.setDiemC(ExcelReaderUtil.getSafeDouble(row.getCell(6)));
        dto.setDiemD(ExcelReaderUtil.getSafeDouble(row.getCell(7)));
        dto.setMaQuyDoi(ExcelReaderUtil.getSafeString(row.getCell(8)));
        dto.setPhanVi(ExcelReaderUtil.getSafeString(row.getCell(9)));
        // Lưu ý: code gốc set maQuyDoi = cell(8) rồi lại set phanVi = cell(8) — đã sửa:
        // maQuyDoi nên được sinh tự động hoặc lấy từ cột riêng nếu có
        return dto;
    }

    private String validate(QuyDoiNNImportDTO dto, int rowNum) {
        if (dto.getPhuongThuc() == null || dto.getPhuongThuc().isBlank())
            return "Dòng " + rowNum + ": Phương thức không được trống";
        if (dto.getMon() == null || dto.getMon().isBlank())
            return "Dòng " + rowNum + ": Môn không được trống (phuongThuc=" + dto.getPhuongThuc() + ")";
        if (dto.getDiemA() == null)
            return "Dòng " + rowNum + ": Điểm A không được để trống (mon=" + dto.getMon() + ")";
        return null;
    }

    private BangQuyDoi convertToEntity(QuyDoiNNImportDTO dto) {
        BangQuyDoi entity = new BangQuyDoi();
        // HOTFIX: Tuyệt đối không set giá trị cho trường Khóa chính (identity) khi import
        // entity.setMaQuyDoi(dto.getMaQuyDoi()); 

        // HOTFIX: Gán cứng phương thức là NGOAINGU cho module Quy đổi Tiếng Anh theo đúng PRD
        entity.setPhuongThuc("NGOAINGU"); 
        entity.setDToHop(dto.getToHop() != null ? dto.getToHop().trim() : null);
        entity.setMon(dto.getMon().trim());
        entity.setDiemGocA(dto.getDiemA());
        entity.setDiemGocB(dto.getDiemB());
        entity.setDiemQuyDoiC(dto.getDiemC());
        entity.setDiemQuyDoiD(dto.getDiemD());
        entity.setPhanVi(dto.getPhanVi() != null ? dto.getPhanVi().trim() : null);
        return entity;
    }
}