// package com.sgu.tuyensinh.service;

// import com.sgu.tuyensinh.admin.ui.MainFrame;
// import com.sgu.tuyensinh.entity.DiemThi;
// import com.sgu.tuyensinh.entity.ThiSinh;
// import com.sgu.tuyensinh.repository.DiemThiRepository;
// import com.sgu.tuyensinh.repository.ThiSinhRepository;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.data.domain.Page;
// import org.springframework.test.context.bean.override.mockito.MockitoBean;
// import org.springframework.transaction.annotation.Transactional;

// import java.util.Map;

// import static org.junit.jupiter.api.Assertions.*;

// @SpringBootTest
// @Transactional
// public class DiemThiIntegrationTest {

//     @MockitoBean
//     private MainFrame mainFrame; // Bỏ qua UI (HeadlessException)

//     @Autowired
//     private DiemThiService diemThiService;

//     @Autowired
//     private ThiSinhRepository thiSinhRepository;

//     @Autowired
//     private DiemThiRepository diemThiRepository;

//     private String ts1_id = "TEST_" + System.currentTimeMillis() + "_1";
//     private String ts2_id = "TEST_" + System.currentTimeMillis() + "_2";
//     private String ts3_id = "TEST_" + System.currentTimeMillis() + "_3";
//     private String ts4_id = "TEST_" + System.currentTimeMillis() + "_4";

//     @BeforeEach
//     void setUp() {
//         // KHÔNG dùng deleteAll() ở đây vì nếu dev chạy test trúng CSDL thật sẽ xóa sạch data gốc!
//         // Xóa data test cũ (nếu có tồn đọng) dựa trên test prefix
//         diemThiRepository.deleteById(-999L); // Không cần thiết xoá tất cả, spring @Transactional sẽ tự rollback sau test

//         // Tạo 4 thí sinh với CCCD động để tránh đụng độ Khóa chính khi chạy nhiều lần
//         saveThiSinh(ts1_id, "Nguyễn Văn A");
//         saveThiSinh(ts2_id, "Trần Thị B");
//         saveThiSinh(ts3_id, "Lê Văn C");
//         saveThiSinh(ts4_id, "Phạm Thị D");

//         // Nhập điểm
//         saveDiemThi(ts1_id, 4.0, 7.0);   // Kém Toán, Khá Văn
//         saveDiemThi(ts2_id, 5.5, 8.5);   // TB Toán, Giỏi Văn
//         saveDiemThi(ts3_id, 7.5, 6.0);   // Khá Toán, TB Văn
//         saveDiemThi(ts4_id, 9.0, null);  // Giỏi Toán, Vắng thi Văn (null)
//     }

//     private void saveThiSinh(String cccd, String hoTen) {
//         ThiSinh ts = new ThiSinh();
//         ts.setId(cccd);
//         ts.setHoTen(hoTen);
//         thiSinhRepository.save(ts);
//     }

//     private void saveDiemThi(String cccd, Double toan, Double van) {
//         DiemThi dt = new DiemThi();
//         dt.setCccd(cccd);
//         dt.setToan(toan);
//         dt.setVan(van);
//         diemThiRepository.save(dt);
//     }

//     @Test
//     void testGetDanhSachDiemThi() {
//         // Tìm kiếm giới hạn bằng ts_id để tránh vướng data thật gốc của User
//         Page<DiemThi> searchResult = diemThiService.getDanhSachDiemThi(ts1_id, 0, 10);
//         assertEquals(1, searchResult.getTotalElements(), "Tìm theo Keyword CCCD động");
        
//         Page<DiemThi> searchTsnName = diemThiService.getDanhSachDiemThi("Nguyễn Văn A", 0, 10);
//         assertTrue(searchTsnName.getTotalElements() >= 1, "Tìm kiếm theo tên có tồn tại");
//     }

//     @Test
//     void testUpdateDiemThi() {
//         DiemThi updateData = new DiemThi();
//         updateData.setToan(8.8);
//         updateData.setVan(7.7);

//         // Update điểm thi của thí sinh 1
//         DiemThi updated = diemThiService.updateDiemThi(ts1_id, updateData);
//         assertNotNull(updated);
//         assertEquals(8.8, updated.getToan());
//         assertEquals(7.7, updated.getVan());

//         // Kiểm tra trong DB
//         DiemThi inDb = diemThiRepository.findByCccd(ts1_id).orElseThrow();
//         assertEquals(8.8, inDb.getToan());
//         assertEquals(7.7, inDb.getVan());
//     }

//     @Test
//     void testClearDiemThi() {
//         diemThiService.clearDiemThi(ts2_id);

//         DiemThi cleared = diemThiRepository.findByCccd(ts2_id).orElseThrow();
//         assertNull(cleared.getToan());
//         assertNull(cleared.getVan());
//         assertNull(cleared.getLy());
//     }

//     @Test
//     void testThongKePhoDiem() {
//         // Thống kê môn Toán
//         Map<String, Long> tkToan = diemThiService.thongKePhoDiem("toan");

//         // Do chạy trên DB thật có sẵn data, số lượng có thể >= 1
//         assertTrue(tkToan.get("Kém (< 5.0)") >= 1L, "Phải có ít nhất 1 bạn Kém Toán (<5.0)");
//         assertTrue(tkToan.get("Trung bình (5.0 - 6.5)") >= 1L, "Phải có ít nhất 1 bạn TB Toán");
//         assertTrue(tkToan.get("Khá (6.5 - 8.0)") >= 1L, "Phải có ít nhất 1 bạn Khá Toán");
//         assertTrue(tkToan.get("Giỏi (> 8.0)") >= 1L, "Phải có ít nhất 1 bạn Giỏi Toán");

//         // Thống kê môn Văn
//         Map<String, Long> tkVan = diemThiService.thongKePhoDiem("van");

//         assertTrue(tkVan.get("Trung bình (5.0 - 6.5)") >= 1L); // 6.0
//         assertTrue(tkVan.get("Khá (6.5 - 8.0)") >= 1L); // 7.0
//         assertTrue(tkVan.get("Giỏi (> 8.0)") >= 1L); // 8.5
//     }
// }
