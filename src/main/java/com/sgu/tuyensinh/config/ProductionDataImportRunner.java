package com.sgu.tuyensinh.config;

import com.sgu.tuyensinh.service.*;
import com.sgu.tuyensinh.service.dto.ImportResultDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * Runner import dữ liệu thực tế từ thư mục docs/ vào Database.
 *
 * Thứ tự import:
 *   1. tohopmon.xlsx      → Tổ hợp môn (bảng xt_tohop_monthi)
 *   2. Chi tieu 2025.xlsx → Chỉ tiêu theo ngành (upsert bảng xt_nganh)
 *   3. Nguong dau vao 2025.xlsx → Ngưỡng điểm sàn (update diemSan bảng xt_nganh)
 *   4. Ds thi sinh.xlsx   → Thí sinh & điểm thi (giới hạn 50 bản ghi)
 *   5. Nguyenvong.xlsx    → Nguyện vọng (giới hạn 200 bản ghi)
 *
 * Ghi chú:
 *   - File "Nganh.xlsx" và "Nguong dau vao 2025.xlsx" đã không còn dùng NganhImportService cũ nữa.
 *   - ChiTieuImportService  : đọc Chi tieu 2025.xlsx (bỏ 2 dòng đầu)
 *   - NguongDauVaoImportService: đọc Nguong dau vao 2025.xlsx (bỏ 1 dòng header)
 *   - ToHopImportService    : đọc tohopmon.xlsx, parse "B03(TO-3,VA-3,SI-1)" → mã + môn
 */
//@Component
@Order(10)
@RequiredArgsConstructor
@Slf4j
public class ProductionDataImportRunner implements CommandLineRunner {

    private final ToHopImportService toHopImportService;
    private final ChiTieuImportService chiTieuImportService;
    private final NguongDauVaoImportService nguongDauVaoImportService;
    private final BangQuyDoiImportService bangQuyDoiImportService;
    private final DiemQuyDoiNgoaiNguImportService diemQuyDoiNgoaiNguImportService;
    private final ThiSinhImportService thiSinhImportService;
    private final NguyenVongServiceImpl nguyenVongImportService;

    // Đường dẫn cơ sở tới thư mục docs — dùng System.getProperty("docs.path") nếu muốn cấu hình linh hoạt
    private static final String DOCS_PATH = "d:\\Project CV\\app-tuyensinh\\docs\\";

    @Override
    public void run(String... args) {
        log.info("🚀 BẮT ĐẦU IMPORT DỮ LIỆU THỰC TẾ TỪ EXCEL...");

        try {
            // 0. Seed Bảng Quy Đổi (Nếu trống)
            seedBangQuyDoi();

            // 1. Chỉ tiêu tuyển sinh: Chi tieu 2025.xlsx
            importFile("Chi tieu 2025.xlsx", chiTieuImportService);

            // 2. Tổ hợp môn: tohopmon.xlsx
            importFile("tohopmon.xlsx", toHopImportService);

            // 3. Ngưỡng đầu vào: Nguong dau vao 2025.xlsx
            importFile("Nguong dau vao 2025.xlsx", nguongDauVaoImportService);

            // 4. Quy đổi Tiếng Anh (Cá nhân thí sinh)
            importFile("Ds quy doi tieng Anh.xlsx", diemQuyDoiNgoaiNguImportService);

            // 5. Danh sách thí sinh & điểm thi
            importFile("Ds thi sinh.xlsx", thiSinhImportService);

            // 6. Nguyện vọng xét tuyển
            importFile("Nguyenvong.xlsx", nguyenVongImportService);

            log.info("✅ HOÀN TẤT IMPORT TOÀN BỘ DỮ LIỆU THỰC TẾ.");

        } catch (Exception e) {
            log.error("❌ LỖI TRONG QUÁ TRÌNH IMPORT TỔNG THỂ: {}", e.getMessage());
        }
    }

    private void seedBangQuyDoi() {
        if (bangQuyDoiImportService.isEmpty()) {
            log.info("--- Đang nạp dữ liệu mẫu cho Bảng Quy Đổi ---");
            bangQuyDoiImportService.saveSampleData();
        }
    }

    private void importFile(String fileName, Object service) {
        File file = new File(DOCS_PATH + fileName);
        if (!file.exists()) {
            log.warn("⚠️ File không tồn tại, bỏ qua: {}", fileName);
            return;
        }

        log.info("--- Đang import file: {} ---", fileName);
        try (InputStream is = new FileInputStream(file)) {
            ImportResultDTO result = null;

            if (service instanceof com.sgu.tuyensinh.service.interfaces.IImportService s) {
                result = s.importFromExcel(is, (curr, tot) -> {
                    if (tot > 0 && (curr % 50 == 0 || curr == tot)) {
                        log.debug("   Progress: {}/{}", curr, tot);
                    }
                });
            }

            if (result != null) {
                log.info("   Kết quả {}: Thành công={}, Bỏ qua={}", fileName, result.getSuccessCount(), result.getSkipCount());
                if (!result.getErrors().isEmpty()) {
                    log.warn("   Có {} dòng bị lỗi. Lỗi đầu tiên: {}", result.getErrors().size(),
                            result.getErrors().get(0));
                }
            }
        } catch (Exception e) {
            log.error("   Lỗi khi xử lý file {}: {}", fileName, e.getMessage());
        }
    }
}
