package com.sgu.tuyensinh;

import com.sgu.tuyensinh.service.dto.ImportResultDTO;
import com.sgu.tuyensinh.service.impl.NganhToHopServiceImpl;
import com.sgu.tuyensinh.repository.NganhToHopRepository;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest
class NganhToHopServiceImplTest {

    private static final Logger log = LoggerFactory.getLogger(NganhToHopServiceImplTest.class);

    @Autowired
    private NganhToHopServiceImpl nganhToHopService;

    @Autowired
    private NganhToHopRepository nganhToHopRepository;

    @MockBean
    private com.sgu.tuyensinh.admin.ui.MainFrame mainFrame;

    @Test
    @Transactional
    @Rollback(false) // Giữ dữ liệu trong DB để bạn kiểm tra sau khi chạy test
    void testImportNganhToHopExcel() {
        // Đảm bảo bạn đã có file này trong thư mục resources của test
        File file = new File("D:\\Phanmemphanlop\\app-tuyensinh\\src\\test\\resources\\importNganhToHop.xlsx");
        
        if (!file.exists()) {
            log.error("❌ KHÔNG TÌM THẤY FILE: {}", file.getAbsolutePath());
            fail("File test không tồn tại tại đường dẫn đã chỉ định!");
        }

        try (InputStream is = new FileInputStream(file)) {
            log.info("🚀 Bắt đầu import file Ngành Tổ Hợp...");
            
            ImportResultDTO result = nganhToHopService.importFromExcel(is);

            assertNotNull(result, "Kết quả import không được null");
            log.info("✅ Hoàn tất quá trình gọi Service!");
            log.info("📊 Số dòng lưu thành công: {}", result.getSuccessCount());
            
            if (result.getErrors() != null && !result.getErrors().isEmpty()) {
                log.warn("⚠️ Các dòng bị lỗi/bỏ qua: {}", result.getErrors());
            }
            
        } catch (Exception e) {
            log.error("💥 LỖI PHÁT SINH TRONG QUÁ TRÌNH TEST: ", e);
            // Ném lỗi ra để thấy được Root Cause (như lỗi Regex hay lỗi Duplicate Key)
            throw new RuntimeException(e);
        }
    }
}