package com.sgu.tuyensinh.service;

import com.sgu.tuyensinh.admin.ui.MainFrame;
import com.sgu.tuyensinh.entity.BangQuyDoi;
import com.sgu.tuyensinh.repository.BangQuyDoiRepository;
import com.sgu.tuyensinh.service.dto.ImportResultDTO;
import com.sgu.tuyensinh.service.impl.BangQuyDoiServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class DanhMucImportIntegrationTest {

    @MockitoBean
    private MainFrame mainFrame;

    @Autowired
    private BangQuyDoiServiceImpl bangQuyDoiService;

    @Autowired
    private BangQuyDoiRepository bangQuyDoiRepository;

    @Test
    void testImportBangQuyDoi_Success() throws IOException {
        // Arrange (Chuẩn bị): Lấy đường dẫn file Excel test
        String filePath = "src/test/resources/test_data/Ds_quy_doi_tieng_Anh_test.xlsx";
        
        // Act (Thực thi): Gọi hàm Import từ Service của Vinh
        try (InputStream is = new FileInputStream(filePath)) {
            ImportResultDTO result = bangQuyDoiService.importFromExcel(is);
            System.out.println("Kết quả import: " + result);
        }

        // Assert (Kiểm chứng): Dùng Repository tìm lại dữ liệu vừa lưu
        List<BangQuyDoi> allData = bangQuyDoiRepository.findAll();
        assertFalse(allData.isEmpty(), "Dữ liệu bảng quy đổi không được trống sau khi import.");

        // Kiểm tra xem có record nào lưu đúng chữ 'NGOAINGU' không (Theo HOTFIX)
        boolean hasNgoaiNgu = allData.stream()
                .anyMatch(b -> "NGOAINGU".equals(b.getPhuongThuc()));
        
        assertTrue(hasNgoaiNgu, "Cột phuongthuc phải được lưu đúng chữ 'NGOAINGU' theo PRD và HOTFIX.");
    }
}
