package com.sgu.tuyensinh.service;

import com.sgu.tuyensinh.repository.DiemCongRepository;
import com.sgu.tuyensinh.repository.DiemThiRepository;
import com.sgu.tuyensinh.service.dto.ImportResultDTO;
import com.sgu.tuyensinh.service.interfaces.IImportService;
import com.sgu.tuyensinh.service.interfaces.ProgressCallback;
import com.sgu.tuyensinh.util.ExcelReaderUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiemQuyDoiNgoaiNguImportService implements IImportService {

    private final JdbcTemplate jdbcTemplate;

    private static final String SQL_UPDATE_DIEM_THI = 
        "UPDATE diem_thi SET anh = ? WHERE cccd = ?";

    private static final String SQL_UPSERT_DIEM_CONG = 
        "INSERT INTO xt_diemcongxetuyen (ts_cccd, diemCC, dc_keys, phuongthuc, ghichu) " +
        "VALUES (?, ?, ?, 'NGOAINGU', 'Quy doi tieng Anh') " +
        "ON DUPLICATE KEY UPDATE diemCC = VALUES(diemCC)";

    @Override
    @Transactional
    public ImportResultDTO importFromExcel(InputStream inputStream, ProgressCallback callback) {
        ImportResultDTO result = new ImportResultDTO();
        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            int totalRows = sheet.getLastRowNum();
            log.info("🚀 Bắt đầu import quy đổi Tiếng Anh cho {} dòng...", totalRows);

            List<Object[]> diemThiArgs = new ArrayList<>();
            List<Object[]> diemCongArgs = new ArrayList<>();

            for (int i = 1; i <= totalRows; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String cccd = ExcelReaderUtil.getSafeString(row.getCell(1));
                Double diemQuyDoi = ExcelReaderUtil.getSafeDouble(row.getCell(4));
                Double diemCong = ExcelReaderUtil.getSafeDouble(row.getCell(5));

                if (cccd != null && !cccd.isEmpty()) {
                    if (diemQuyDoi != null) {
                        diemThiArgs.add(new Object[]{diemQuyDoi, cccd});
                    }
                    if (diemCong != null && diemCong > 0) {
                        diemCongArgs.add(new Object[]{cccd, diemCong, cccd + "_ENGLISH"});
                    }
                }

                if (i % 1000 == 0 || i == totalRows) {
                    executeBatch(diemThiArgs, diemCongArgs);
                    if (callback != null) {
                        callback.onProgress(i, totalRows);
                    }
                }
            }

            result.setSuccessCount(totalRows);
            log.info("✅ Hoàn tất import quy đổi Tiếng Anh.");
        } catch (Exception e) {
            log.error("❌ Lỗi import quy đổi Tiếng Anh", e);
            result.addError("Lỗi hệ thống: " + e.getMessage());
        }
        return result;
    }

    private void executeBatch(List<Object[]> diemThiArgs, List<Object[]> diemCongArgs) {
        if (!diemThiArgs.isEmpty()) {
            jdbcTemplate.batchUpdate(SQL_UPDATE_DIEM_THI, diemThiArgs);
            diemThiArgs.clear();
        }
        if (!diemCongArgs.isEmpty()) {
            jdbcTemplate.batchUpdate(SQL_UPSERT_DIEM_CONG, diemCongArgs);
            diemCongArgs.clear();
        }
    }
}
