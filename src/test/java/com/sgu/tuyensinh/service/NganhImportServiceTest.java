package com.sgu.tuyensinh;

import com.sgu.tuyensinh.service.dto.ImportResultDTO;
import com.sgu.tuyensinh.service.NganhImportService;
import com.sgu.tuyensinh.repository.NganhRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class NganhServiceImplTest {

    // 1. Khai báo các Bean ở đây (NGOÀI hàm test)
    @Autowired
    private NganhImportService nganhService;

    @Autowired // Dùng Autowired để lưu vào DB thật
    private NganhRepository nganhRepository;

    @MockBean // Giữ MockBean cho UI để tránh lỗi Headless
    private com.sgu.tuyensinh.admin.ui.MainFrame mainFrame;

    @Test
    @Transactional // Để quản lý transaction
    @Rollback(false) // QUAN TRỌNG: Giữ lại dữ liệu trong DB sau khi test xong
    void testImportExcel() {
        try {
            File file = new File("D:\\Phanmemphanlop\\app-tuyensinh\\src\\test\\resources\\importNganh.xlsx"); // Đảm bảo file tồn tại ở đây
            if (!file.exists()) {
                System.out.println("File không tồn tại rồi bạn ơi!");
                return;
            }
            
            FileInputStream is = new FileInputStream(file);
            ImportResultDTO result = nganhService.importFromExcel(is);

            assertNotNull(result);
            System.out.println("SUCCESS: " + result.getSuccessCount());
            System.out.println("ERRORS: " + result.getErrors());
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}