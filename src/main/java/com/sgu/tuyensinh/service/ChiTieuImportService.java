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
import java.util.Optional;

/**
 * Import Chỉ tiêu tuyển sinh từ file "Chi tieu 2025.xlsx".
 *
 * Cấu trúc file:
 *   - Dòng 1: Tiêu đề (DỰKIẾN CHỈ TIÊU TUYỂN SINH NĂM 2025) → bỏ qua
 *   - Dòng 2: Header (TT | MÃ CTĐT | TÊN CTĐT | Chỉ tiêu chốt) → bỏ qua
 *   - Dòng 3+: Dữ liệu
 *     + Col 0: TT (số thứ tự)
 *     + Col 1: Mã CTĐT (mã ngành)
 *     + Col 2: Tên CTĐT (tên ngành)
 *     + Col 3: Chỉ tiêu chốt (số nguyên)
 *
 * Logic:
 *   - Nếu ngành đã tồn tại → cập nhật chỉ tiêu + tên ngành.
 *   - Nếu ngành chưa tồn tại → tạo mới với thông tin cơ bản.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChiTieuImportService implements IImportService {

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
            if (sheet == null || sheet.getLastRowNum() < 2) {
                result.addError("File không có dữ liệu (cần ít nhất 3 dòng: tiêu đề, header, dữ liệu)");
                return result;
            }

            int total   = sheet.getLastRowNum();
            int current = 0;

            // BẮT ĐẦU TỪ DÒNG 3 (index 2): bỏ qua row 0 (title) và row 1 (header)
            for (int i = 2; i <= total; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                current++;

                String maNganh  = ExcelReaderUtil.getSafeString(row.getCell(1));
                String tenNganh = ExcelReaderUtil.getSafeString(row.getCell(2));
                Integer chiTieu = ExcelReaderUtil.getSafeInteger(row.getCell(3));

                // Nếu là dòng chú thích (ví dụ bắt đầu bằng dấu *) hoặc dòng trống hoàn toàn → bỏ qua không báo lỗi
                if (maNganh == null || maNganh.isBlank() || maNganh.startsWith("*")) {
                    continue;
                }
                maNganh = maNganh.trim();

                if (chiTieu == null || chiTieu <= 0) {
                    result.addError(i + 1, maNganh, "INVALID_CHI_TIEU", "Dòng " + (i + 1) + ": Chỉ tiêu không hợp lệ (" + chiTieu + ")");
                    result.incrementSkip();
                    if (callback != null) callback.onProgress(current, total);
                    continue;
                }

                try {
                    Optional<Nganh> existing = nganhRepository.findById(maNganh);
                    if (existing.isPresent()) {
                        // Cập nhật chỉ tiêu cho ngành đã tồn tại
                        Nganh nganh = existing.get();
                        nganh.setChiTieu(chiTieu);
                        if (tenNganh != null && !tenNganh.isBlank()) {
                            nganh.setTenNganh(tenNganh.trim());
                        }
                        nganhRepository.save(nganh);
                        log.debug("Cập nhật chỉ tiêu ngành {}: {} chỉ tiêu", maNganh, chiTieu);
                    } else {
                        // Tạo mới ngành với thông tin cơ bản
                        Nganh nganh = new Nganh();
                        nganh.setMaNganh(maNganh);
                        nganh.setTenNganh(tenNganh != null ? tenNganh.trim() : maNganh);
                        nganh.setChiTieu(chiTieu);
                        nganhRepository.save(nganh);
                        log.debug("Tạo mới ngành {}: {}, {} chỉ tiêu", maNganh, tenNganh, chiTieu);
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
            log.error("Lỗi import chỉ tiêu", e);
            result.addError("Lỗi hệ thống: " + e.getMessage());
        }

        return result;
    }
}
