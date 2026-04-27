package com.sgu.tuyensinh.service;

import com.sgu.tuyensinh.dto.NganhImportDTO;
import com.sgu.tuyensinh.entity.Nganh;
import com.sgu.tuyensinh.repository.NganhRepository;
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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Import Ngành từ Excel.
 * Đặt tại: service/impl/NganhImportService.java
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NganhImportService implements IImportService {

    private final NganhRepository nganhRepository;

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
                log.warn("File Excel Ngành không có dữ liệu");
                return result;
            }

            List<Nganh> validList = new ArrayList<>();

            // Bước 1: Đọc toàn bộ → validate từng dòng → gom lỗi
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                NganhImportDTO dto = new NganhImportDTO();
                dto.setMaNganh(ExcelReaderUtil.getSafeString(row.getCell(1)));
                dto.setTenNganh(ExcelReaderUtil.getSafeString(row.getCell(2)));
                dto.setToHopGoc(ExcelReaderUtil.getSafeString(row.getCell(3)));
                dto.setChiTieu(ExcelReaderUtil.getSafeInteger(row.getCell(4)));
                dto.setDiemSan(ExcelReaderUtil.getSafeBigDecimal(row.getCell(5)));

                String error = validate(dto, i + 1);
                if (error != null) {
                    result.addError(error);
                    result.incrementSkip();
                    continue;
                }

                validList.add(convertToEntity(dto));
            }

            // Bước 2: saveAll danh sách hợp lệ
            if (!validList.isEmpty()) {
                nganhRepository.saveAll(validList);
                validList.forEach(n -> result.incrementSuccess());
                log.info("Import Ngành: {}", result);
            }

        } catch (Exception e) {
            log.error("Lỗi import Ngành từ Excel", e);
            result.addError("Lỗi hệ thống: " + e.getMessage());
        }

        return result;
    }

    private String validate(NganhImportDTO dto, int rowNum) {
        if (dto.getMaNganh() == null || dto.getMaNganh().isBlank())
            return "Dòng " + rowNum + ": Mã ngành không được trống";
        if (dto.getTenNganh() == null || dto.getTenNganh().isBlank())
            return "Dòng " + rowNum + ": Tên ngành không được trống (maNganh=" + dto.getMaNganh() + ")";
        if (dto.getChiTieu() == null || dto.getChiTieu() <= 0)
            return "Dòng " + rowNum + ": Chỉ tiêu phải > 0 (maNganh=" + dto.getMaNganh() + ")";
        if (dto.getDiemSan() == null || dto.getDiemSan().compareTo(BigDecimal.ZERO) < 0)
            return "Dòng " + rowNum + ": Điểm sàn không được âm (maNganh=" + dto.getMaNganh() + ")";
        return null;
    }

    private Nganh convertToEntity(NganhImportDTO dto) {
        Nganh nganh = new Nganh();
        nganh.setMaNganh(dto.getMaNganh().trim());
        nganh.setTenNganh(dto.getTenNganh().trim());
        nganh.setToHopGoc(dto.getToHopGoc() != null ? dto.getToHopGoc().trim() : null);
        nganh.setChiTieu(dto.getChiTieu());
        nganh.setDiemSan(dto.getDiemSan());
        return nganh;
    }

    // =========================================================
    // CÁC HÀM CRUD PHỤC VỤ TRỰC TIẾP CHO GIAO DIỆN (JAVA SWING)
    // =========================================================

    /**
     * Lấy danh sách ngành có phân trang
     */
    public org.springframework.data.domain.Page<Nganh> layDanhSachPhanTrang(int page, int size, String keyword) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        if (keyword != null && !keyword.trim().isEmpty()) {
            return nganhRepository.findByMaNganhContainingOrTenNganhContainingIgnoreCase(keyword, keyword, pageable);
        }
        return nganhRepository.findAll(pageable);
    }

    /**
     * Lưu mới hoặc Cập nhật Ngành
     */
    @Transactional
    public Nganh luuNganh(Nganh nganh) {
        // Validate cơ bản
        if (nganh.getMaNganh() == null || nganh.getMaNganh().isBlank()) {
            throw new IllegalArgumentException("Mã ngành không được để trống!");
        }
        if (nganh.getTenNganh() == null || nganh.getTenNganh().isBlank()) {
            throw new IllegalArgumentException("Tên ngành không được để trống!");
        }
        return nganhRepository.save(nganh);
    }

    /**
     * Xóa Ngành theo Mã
     */
    @Transactional
    public void xoaNganh(String maNganh) {
        if (!nganhRepository.existsById(maNganh)) {
            throw new IllegalArgumentException("Không tìm thấy mã ngành: " + maNganh);
        }
        nganhRepository.deleteById(maNganh);
    }
}