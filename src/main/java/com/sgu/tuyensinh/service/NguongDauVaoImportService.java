package com.sgu.tuyensinh.service;

import com.sgu.tuyensinh.entity.Nganh;
import com.sgu.tuyensinh.repository.NganhRepository;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Optional;

/**
 * Import Ngưỡng đầu vào từ file "Nguong dau vao 2025.xlsx".
 *
 * Cấu trúc file:
 *   - Dòng 1: Header (STT | Mã xét tuyển | Tên ngành... | Ngưỡng đầu vào) → bỏ qua
 *   - Dòng 2+: Dữ liệu
 *     + Col 0: STT
 *     + Col 1: Mã xét tuyển (mã ngành)
 *     + Col 2: Tên ngành, chương trình đào tạo
 *     + Col 3: Ngưỡng đầu vào (số thực, thang 30)
 *
 * Logic:
 *   - Nếu ngành đã tồn tại → cập nhật diemSan.
 *   - Nếu ngành chưa tồn tại → tạo mới với thông tin cơ bản.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NguongDauVaoImportService implements IImportService {

    private final NganhRepository nganhRepository;

    @Override
    @Transactional
    public ImportResultDTO importFromExcel(InputStream inputStream, ProgressCallback callback) {
        ImportResultDTO result = new ImportResultDTO();

        if (inputStream == null) {
            result.addError("InputStream không được để null");
            return result;
        }

        if (callback != null) callback.onProgress(0, 0);

        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null || sheet.getLastRowNum() < 1) {
                result.addError("File không có dữ liệu");
                return result;
            }

            int total   = sheet.getLastRowNum();
            int current = 0;

            // BẮT ĐẦU TỪ DÒNG 2 (index 1): bỏ qua row 0 (header)
            for (int i = 1; i <= total; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                current++;

                String maNganh  = ExcelReaderUtil.getSafeString(row.getCell(1));
                String tenNganh = ExcelReaderUtil.getSafeString(row.getCell(2));
                BigDecimal nguong = ExcelReaderUtil.getSafeBigDecimal(row.getCell(3));

                // Validate
                if (maNganh == null || maNganh.isBlank()) {
                    result.addError(i + 1, "N/A", "MISSING_MA_NGANH", "Dòng " + (i + 1) + ": Mã ngành trống, bỏ qua");
                    result.incrementSkip();
                    if (callback != null) callback.onProgress(current, total);
                    continue;
                }
                maNganh = maNganh.trim();

                if (nguong == null || nguong.compareTo(BigDecimal.ZERO) < 0) {
                    result.addError(i + 1, maNganh, "INVALID_NGUONG", "Dòng " + (i + 1) + ": Ngưỡng đầu vào không hợp lệ (" + nguong + ")");
                    result.incrementSkip();
                    if (callback != null) callback.onProgress(current, total);
                    continue;
                }

                try {
                    Optional<Nganh> existing = nganhRepository.findById(maNganh);
                    if (existing.isPresent()) {
                        // Cập nhật ngưỡng điểm sàn cho ngành đã tồn tại
                        Nganh nganh = existing.get();
                        nganh.setDiemSan(nguong);
                        if (tenNganh != null && !tenNganh.isBlank() &&
                                (nganh.getTenNganh() == null || nganh.getTenNganh().isBlank())) {
                            nganh.setTenNganh(tenNganh.trim());
                        }
                        nganhRepository.save(nganh);
                        log.debug("Cập nhật ngưỡng ngành {}: {} điểm", maNganh, nguong);
                    } else {
                        // Tạo mới ngành nếu chưa có
                        Nganh nganh = new Nganh();
                        nganh.setMaNganh(maNganh);
                        nganh.setTenNganh(tenNganh != null ? tenNganh.trim() : maNganh);
                        nganh.setDiemSan(nguong);
                        nganh.setChiTieu(0); // Chỉ tiêu sẽ được cập nhật sau khi import Chi tieu
                        nganhRepository.save(nganh);
                        log.debug("Tạo mới ngành {}: {}, ngưỡng={}", maNganh, tenNganh, nguong);
                    }
                    result.incrementSuccess();
                } catch (Exception e) {
                    result.addError(i + 1, maNganh, "SYSTEM_ERROR", e.getMessage());
                    result.incrementSkip();
                    log.warn("Lỗi xử lý dòng {} (ngành {}): {}", i + 1, maNganh, e.getMessage());
                }

                if (callback != null) callback.onProgress(current, total);
            }

        } catch (Exception e) {
            log.error("Lỗi import ngưỡng đầu vào", e);
            result.addError("Lỗi hệ thống: " + e.getMessage());
        }

        return result;
    }
}
