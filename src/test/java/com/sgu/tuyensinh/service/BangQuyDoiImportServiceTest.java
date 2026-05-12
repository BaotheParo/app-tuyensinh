package com.sgu.tuyensinh;

import com.sgu.tuyensinh.service.dto.ImportResultDTO;
import com.sgu.tuyensinh.service.BangQuyDoiImportService;
import com.sgu.tuyensinh.repository.BangQuyDoiRepository;
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
class BangQuyDoiServiceImplTest {

    private static final Logger log = LoggerFactory.getLogger(BangQuyDoiServiceImplTest.class);

    @Autowired
    private BangQuyDoiImportService bangQuyDoiService;

    @Autowired
    private BangQuyDoiRepository bangQuyDoiRepository;

    @MockBean
    private com.sgu.tuyensinh.admin.ui.MainFrame mainFrame;

    @Test
    @Transactional 
    @Rollback(false) // Lưu ý: Nếu có lỗi DB bên trong Service, lệnh này sẽ bị lỗi UnexpectedRollback
    void testImportExcel() {
        File file = new File("D:\\Phanmemphanlop\\app-tuyensinh\\src\\test\\resources\\importBangQuyDoi.xlsx");
        
        if (!file.exists()) {
            log.error("❌ FILE KHÔNG TỒN TẠI TẠI ĐƯỜNG DẪN: {}", file.getAbsolutePath());
            fail("File test không tồn tại!");
        }

        // Dùng try-with-resources để tự động đóng stream
        try (InputStream is = new FileInputStream(file)) {
            log.info("🚀 Bắt đầu import file Excel...");
            
            ImportResultDTO result = bangQuyDoiService.importFromExcel(is, null);

            assertNotNull(result, "Kết quả import không được null");
            log.info("✅ Import hoàn tất!");
            log.info("📊 Số dòng thành công: {}", result.getSuccessCount());
            
            if (result.getErrors() != null && !result.getErrors().isEmpty()) {
                log.warn("⚠️ Có lỗi trong nội dung file: {}", result.getErrors());
            }
            
        } catch (Exception e) {
            log.error("💥 LỖI THỰC SỰ GÂY RA ROLLBACK: ", e);
            // Quan trọng: Phải ném lỗi ra để JUnit và Spring biết mà xử lý, không được nuốt lỗi
            throw new RuntimeException(e);
        }
    }
}