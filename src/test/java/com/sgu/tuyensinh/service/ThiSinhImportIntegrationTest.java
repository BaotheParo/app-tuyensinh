// package com.sgu.tuyensinh.service;

// import com.sgu.tuyensinh.admin.ui.MainFrame;
// import com.sgu.tuyensinh.entity.DiemThi;
// import com.sgu.tuyensinh.entity.ThiSinh;
// import com.sgu.tuyensinh.repository.ThiSinhRepository;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.test.context.bean.override.mockito.MockitoBean;
// import org.springframework.transaction.annotation.Transactional;

// import java.util.List;

// import static org.junit.jupiter.api.Assertions.*;

// @SpringBootTest
// @Transactional
// public class ThiSinhImportIntegrationTest {

//     @MockitoBean
//     private MainFrame mainFrame;

//     @Autowired
//     private ThiSinhImportService thiSinhImportService;

//     @Autowired
//     private ThiSinhRepository thiSinhRepository;

//     @Test
//     void testImportThiSinh_Success() {
//         // Arrange (Chuẩn bị): Lấy đường dẫn file Excel thi sinh test
//         String filePath = "src/test/resources/test_data/Ds_thi_sinh_test.xlsx";
        
//         // Act (Thực thi): Gọi hàm import của Service (đã được refactor với Chunking)
//         List<String> errors = thiSinhImportService.importThiSinhFromExcel(filePath);
        
//         // Assert (Kiểm chứng):
//         assertTrue(errors.isEmpty(), "Import không được có lỗi logic: " + errors);

//         // Kiểm chứng dữ liệu trong DB
//         List<ThiSinh> allTs = thiSinhRepository.findAll();
//         assertFalse(allTs.isEmpty(), "Phải có dữ liệu thí sinh trong DB sau khi import.");

//         ThiSinh savedTs = allTs.get(0);
//         assertNotNull(savedTs.getHoTen(), "Họ tên thí sinh không được null.");
        
//         System.out.println("Đã import thành công " + allTs.size() + " thí sinh.");

//         // Kiểm chứng logic Điểm thi (nếu có)
//         DiemThi diem = savedTs.getDiemThi();
//         if (diem != null) {
//             System.out.println("CCCD: " + savedTs.getId() + " - Điểm NK1: " + diem.getNk1());
//         }
//     }
// }
