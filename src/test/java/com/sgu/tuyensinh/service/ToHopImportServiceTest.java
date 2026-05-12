package com.sgu.tuyensinh;

import com.sgu.tuyensinh.entity.ToHop;
import com.sgu.tuyensinh.service.ToHopImportService;
import com.sgu.tuyensinh.repository.ToHopRepository;
import com.sgu.tuyensinh.service.dto.ImportResultDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;


import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


@SpringBootTest
class ToHopServiceImplTest {

    @Autowired
    private ToHopImportService toHopService;

    @Autowired
    private ToHopRepository toHopRepository;

    // QUAN TRỌNG: MockBean này sẽ "lừa" Spring không khởi tạo giao diện thật, tránh lỗi Headless
    @MockBean
    private com.sgu.tuyensinh.admin.ui.MainFrame mainFrame;

    @Test
    @Transactional
    @Rollback(false) // Giữ lại dữ liệu trong MySQL để bạn dùng lệnh SELECT kiểm tra
    void testImportFromExcel_Success() {
        try {
            // Đường dẫn file (Bạn nhớ đổi tên file .xlsx cho đúng với máy bạn nhé)
            File file = new File("D:\\Phanmemphanlop\\app-tuyensinh\\src\\test\\resources\\importToHop.xlsx");
            
            if (!file.exists()) {
                System.err.println("❌ File không tồn tại rồi bạn ơi! Kiểm tra lại đường dẫn.");
                return;
            }

            FileInputStream is = new FileInputStream(file);
            
            // Thực hiện import
            ImportResultDTO result = toHopService.importFromExcel(is, null);

            // Kiểm tra kết quả
            assertNotNull(result);
            System.out.println("✅ Import thành công: " + result.getSuccessCount());
            System.out.println("⚠️ Bỏ qua (Trùng/Lỗi): " + result.getSkipCount());

            // Kiểm tra thực tế trong DB
            List<ToHop> listInDb = toHopRepository.findAll();
            System.out.println("📊 Tổng số dòng trong bảng xt_tohop_monthi: " + listInDb.size());
            
        } catch (Exception e) {
            System.err.println("🔥 Có lỗi xảy ra trong quá trình Test:");
            e.printStackTrace();
        }
    }
}