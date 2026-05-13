package com.sgu.tuyensinh.service;

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
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service import Thí sinh và Điểm thi tối ưu (High Performance).
 * Sử dụng JdbcTemplate Batch Update và Multi-threading để xử lý 50,000+ dòng nhanh chóng.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ThiSinhImportService implements IImportService {

    private final JdbcTemplate jdbcTemplate;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // SQL sử dụng INSERT ... ON DUPLICATE KEY UPDATE để hỗ trợ Upsert nhanh cho Thí sinh
    private static final String SQL_UPSERT_THISINH = 
        "INSERT INTO thi_sinh (cccd, ho_ten, ngay_sinh, gioi_tinh, doi_tuong_ut, khu_vuc_ut, ma_truong, ma_tinh) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
        "ON DUPLICATE KEY UPDATE ho_ten=VALUES(ho_ten), ngay_sinh=VALUES(ngay_sinh), " +
        "gioi_tinh=VALUES(gioi_tinh), doi_tuong_ut=VALUES(doi_tuong_ut), khu_vuc_ut=VALUES(khu_vuc_ut), " +
        "ma_truong=VALUES(ma_truong), ma_tinh=VALUES(ma_tinh)";

    // SQL cho Điểm thi (Sử dụng UPSERT với Unique Index trên cccd)
    private static final String SQL_UPSERT_DIEMTHI = 
        "INSERT INTO diem_thi (cccd, toan, van, ly, hoa, sinh, su, dia, anh, nk1, nk2, nk3, nk4, nk5, nk6, nk7, nk8) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
        "ON DUPLICATE KEY UPDATE toan=VALUES(toan), van=VALUES(van), ly=VALUES(ly), hoa=VALUES(hoa), " +
        "sinh=VALUES(sinh), su=VALUES(su), dia=VALUES(dia), anh=VALUES(anh), " +
        "nk1=VALUES(nk1), nk2=VALUES(nk2), nk3=VALUES(nk3), nk4=VALUES(nk4), " +
        "nk5=VALUES(nk5), nk6=VALUES(nk6), nk7=VALUES(nk7), nk8=VALUES(nk8)";

    @Override
    public ImportResultDTO importFromExcel(InputStream inputStream, ProgressCallback callback) {
        ImportResultDTO result = new ImportResultDTO();
        long startTime = System.currentTimeMillis();

        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            int totalRows = sheet.getLastRowNum();
            log.info("🚀 Bắt đầu import {} thí sinh...", totalRows);

            List<RowData> allData = new ArrayList<>();
            for (int i = 1; i <= totalRows; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                RowData data = parseRow(row);
                if (data != null) {
                    allData.add(data);
                }
            }

            // Sắp xếp theo CCCD để tránh Deadlock khi chạy đa luồng (Ordered Lock Acquisition)
            allData.sort((a, b) -> a.cccd.compareTo(b.cccd));

            int batchSize = 1000;
            java.util.concurrent.atomic.AtomicInteger processedCount = new java.util.concurrent.atomic.AtomicInteger(0);
            for (int i = 0; i < allData.size(); i += batchSize) {
                final List<RowData> batch = allData.subList(i, Math.min(i + batchSize, allData.size()));
                processBatch(batch);
                int currentProgress = processedCount.addAndGet(batch.size());
                if (callback != null) {
                    callback.onProgress(currentProgress, allData.size());
                }
            }

            result.setSuccessCount(allData.size());
            long endTime = System.currentTimeMillis();
            log.info("✅ Hoàn tất import {} thí sinh trong {} ms", allData.size(), (endTime - startTime));

        } catch (Exception e) {
            log.error("❌ Lỗi import thí sinh", e);
            result.addError("Lỗi hệ thống: " + e.getMessage());
        }

        return result;
    }

    private void processBatch(List<RowData> batch) {
        // 1. Batch Update Thí sinh (Upsert)
        List<Object[]> thiSinhArgs = new ArrayList<>();
        List<Object[]> deleteDiemThiArgs = new ArrayList<>();
        List<Object[]> insertDiemThiArgs = new ArrayList<>();

        for (RowData d : batch) {
            thiSinhArgs.add(new Object[]{
                d.cccd, d.hoTen, 
                d.ngaySinh != null ? Date.valueOf(d.ngaySinh) : null, 
                d.gioiTinh, d.doiTuongUt, d.khuVucUt, d.maTruong, d.maTinh
            });
            
            deleteDiemThiArgs.add(new Object[]{d.cccd});
            
            insertDiemThiArgs.add(new Object[]{
                d.cccd, d.toan, d.van, d.ly, d.hoa, d.sinh, d.su, d.dia, d.anh,
                d.nk1, d.nk2, d.nk3, d.nk4, d.nk5, d.nk6, d.nk7, d.nk8
            });
        }

        int retryCount = 3;
        while (retryCount > 0) {
            try {
                jdbcTemplate.batchUpdate(SQL_UPSERT_THISINH, thiSinhArgs);
                jdbcTemplate.batchUpdate(SQL_UPSERT_DIEMTHI, insertDiemThiArgs);
                break; // Thành công thì thoát
            } catch (org.springframework.dao.TransientDataAccessException e) {
                retryCount--;
                if (retryCount == 0) throw e;
                log.warn("🔄 Deadlock detected, retrying batch ({} attempts left)...", retryCount);
                try { Thread.sleep(500); } catch (InterruptedException ignored) {}
            }
        }
    }

    private RowData parseRow(Row row) {
        String cccd = ExcelReaderUtil.getSafeString(row.getCell(1));
        if (cccd == null || cccd.isBlank()) return null;

        RowData d = new RowData();
        d.cccd = cccd.trim();
        d.hoTen = ExcelReaderUtil.getSafeString(row.getCell(2));
        
        String dobStr = ExcelReaderUtil.getSafeDateString(row.getCell(3));
        if (dobStr != null) {
            try {
                // Thử parse theo dd/MM/yyyy
                d.ngaySinh = LocalDate.parse(dobStr, dateFormatter);
            } catch (Exception e) {
                log.warn("⚠️ Không thể parse ngày sinh: {} ở dòng {}", dobStr, row.getRowNum());
            }
        }
        
        d.gioiTinh = ExcelReaderUtil.getSafeString(row.getCell(4));
        d.doiTuongUt = ExcelReaderUtil.getSafeString(row.getCell(5));
        d.khuVucUt = ExcelReaderUtil.getSafeString(row.getCell(6));
        
        // Bổ sung mã trường/tỉnh từ các cột phụ
        d.maTruong = ExcelReaderUtil.getSafeString(row.getCell(21)); // Chương trình học
        d.maTinh = ExcelReaderUtil.getSafeString(row.getCell(35));   // Nơi sinh

        // Điểm thi
        d.toan = ExcelReaderUtil.getSafeDouble(row.getCell(7));
        d.van = ExcelReaderUtil.getSafeDouble(row.getCell(8));
        d.ly = ExcelReaderUtil.getSafeDouble(row.getCell(9));
        d.hoa = ExcelReaderUtil.getSafeDouble(row.getCell(10));
        d.sinh = ExcelReaderUtil.getSafeDouble(row.getCell(11));
        d.su = ExcelReaderUtil.getSafeDouble(row.getCell(12));
        d.dia = ExcelReaderUtil.getSafeDouble(row.getCell(13));
        // GDCD ở cột 14 bỏ qua nếu không dùng
        d.anh = ExcelReaderUtil.getSafeDouble(row.getCell(15)); // Cột NN

        // Năng khiếu
        d.nk1 = ExcelReaderUtil.getSafeDouble(row.getCell(22));
        d.nk2 = ExcelReaderUtil.getSafeDouble(row.getCell(23));
        d.nk3 = ExcelReaderUtil.getSafeDouble(row.getCell(24));
        d.nk4 = ExcelReaderUtil.getSafeDouble(row.getCell(25));
        d.nk5 = ExcelReaderUtil.getSafeDouble(row.getCell(26));
        d.nk6 = ExcelReaderUtil.getSafeDouble(row.getCell(27));
        d.nk7 = ExcelReaderUtil.getSafeDouble(row.getCell(28));
        d.nk8 = ExcelReaderUtil.getSafeDouble(row.getCell(29));

        return d;
    }

    private static class RowData {
        String cccd, hoTen, gioiTinh, doiTuongUt, khuVucUt, maTruong, maTinh;
        LocalDate ngaySinh;
        Double toan, van, ly, hoa, sinh, su, dia, anh;
        Double nk1, nk2, nk3, nk4, nk5, nk6, nk7, nk8;
    }
}
