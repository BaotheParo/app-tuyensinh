package com.sgu.tuyensinh.service;

import com.sgu.tuyensinh.admin.ui.MainFrame;
import com.sgu.tuyensinh.entity.Nganh;
import com.sgu.tuyensinh.entity.NguyenVong;
import com.sgu.tuyensinh.entity.ThiSinh;
import com.sgu.tuyensinh.repository.NganhRepository;
import com.sgu.tuyensinh.repository.NguyenVongRepository;
import com.sgu.tuyensinh.repository.ThiSinhRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Transactional
public class AdmissionIntegrationTest {

    @MockitoBean
    private MainFrame mainFrame; // Tránh lỗi HeadlessException do UI

    @Autowired
    private AdmissionService admissionService;

    @Autowired
    private ThiSinhRepository thiSinhRepository;

    @Autowired
    private NganhRepository nganhRepository;

    @Autowired
    private NguyenVongRepository nguyenVongRepository;

    @BeforeEach
    void setUp() {
        // Xóa data cũ (dù @Transactional có tự rollback sau mỗi test nhưng để chắc chắn)
        nguyenVongRepository.deleteAll();
        nganhRepository.deleteAll();
        thiSinhRepository.deleteAll();

        // 1. Tạo 2 Ngành với chỉ tiêu là 1
        Nganh cntt = new Nganh();
        cntt.setMaNganh("CNTT");
        cntt.setTenNganh("Công nghệ thông tin");
        cntt.setChiTieu(1);
        nganhRepository.save(cntt);

        Nganh nna = new Nganh();
        nna.setMaNganh("NNA");
        nna.setTenNganh("Ngôn ngữ Anh");
        nna.setChiTieu(1);
        nganhRepository.save(nna);

        // 2. Tạo 2 Thí sinh
        ThiSinh tsA = new ThiSinh();
        tsA.setId("TS_A");
        tsA.setHoTen("Thí sinh A");
        thiSinhRepository.save(tsA);

        ThiSinh tsB = new ThiSinh();
        tsB.setId("TS_B");
        tsB.setHoTen("Thí sinh B");
        thiSinhRepository.save(tsB);

        // 3. Tạo Nguyện vọng mô phỏng kịch bản lỗi:
        // TS A: NV1 CNTT (25đ), NV2 NNA (28đ)
        saveNguyenVong("TS_A", "CNTT", 1, 25.0);
        saveNguyenVong("TS_A", "NNA", 2, 28.0);

        // TS B: NV1 CNTT (26đ) - Cạnh tranh trực tiếp rớt TS A ở NV1
        saveNguyenVong("TS_B", "CNTT", 1, 26.0);
    }

    private void saveNguyenVong(String cccd, String maNganh, int thuTu, double diem) {
        NguyenVong nv = new NguyenVong();
        nv.setNnCccd(cccd);
        nv.setNvManganh(maNganh);
        nv.setNvTt(thuTu);
        nv.setDiemXetTuyen(diem);
        nguyenVongRepository.save(nv);
    }

    @Test
    void testRunAdmissionProcess_GlobalSort_ResolvesConflict() {
        // Act: Chạy thuật toán lọc ảo (Global Sort)
        admissionService.runAdmissionProcess();

        // Lấy lại danh sách nguyện vọng từ DB để kiểm tra
        List<NguyenVong> results = nguyenVongRepository.findAll();
        assertEquals(3, results.size(), "Phải có đúng 3 nguyện vọng trong DB");

        // Assert: 
        for (NguyenVong nv : results) {
            if ("TS_B".equals(nv.getNnCccd()) && "CNTT".equals(nv.getNvManganh())) {
                // Thí sinh B (26đ) đấu với TS A (25đ) ở NV1 CNTT. Chỉ tiêu 1 -> TS B phải ĐẬU.
                assertEquals("TRUNG_TUYEN", nv.getNvKetQua(), "Thí sinh B phải đậu CNTT (NV1) vì điểm cao hơn");
            } 
            else if ("TS_A".equals(nv.getNnCccd()) && "CNTT".equals(nv.getNvManganh())) {
                // Thí sinh A rớt NV1 CNTT do thua TS B
                assertEquals("TRUOT", nv.getNvKetQua(), "Thí sinh A phải rớt CNTT (NV1) do hết chỉ tiêu");
            } 
            else if ("TS_A".equals(nv.getNnCccd()) && "NNA".equals(nv.getNvManganh())) {
                // Thí sinh A điểm rất cao ở NV2 (28đ), Ngành NNA chưa ai lấy -> TS A phải ĐẬU NV2.
                // Nếu thuật toán cũ lỗi (không Global Sort), nó có thể cho TS A đậu NNA ngay trước khi xét CNTT,
                // Nhưng với Global Sort, hệ thống xét TS A(28đ NNA) TƯỚC, lưu trạng thái ĐẬU. Rồi mới xét tiếp.
                assertEquals("TRUNG_TUYEN", nv.getNvKetQua(), "Thí sinh A phải đậu NNA (NV2) dù điểm rất cao");
            }
        }
    }
}
