package com.sgu.tuyensinh.service;

import com.sgu.tuyensinh.admin.ui.MainFrame;
import com.sgu.tuyensinh.entity.DiemCong;
import com.sgu.tuyensinh.entity.ThiSinh;
import com.sgu.tuyensinh.repository.DiemCongRepository;
import com.sgu.tuyensinh.repository.ThiSinhRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
public class BonusPointIntegrationTest {

    @MockitoBean
    private MainFrame mainFrame; // Mock UI để tránh HeadlessException

    @Autowired
    private BonusPointService bonusPointService;

    @Autowired
    private DiemCongRepository diemCongRepository;

    @Autowired
    private ThiSinhRepository thiSinhRepository;

    private final String TEST_CCCD = "123456789012";

    @BeforeEach
    void setUp() {
        // Tạo thí sinh mẫu
        ThiSinh ts = new ThiSinh();
        ts.setId(TEST_CCCD);
        ts.setHoTen("Thí sinh Test Bonus");
        thiSinhRepository.save(ts);
    }

    @Test
    void testCalculateRawBonusPoint_MaxCertificateAndHSG() {
        // 1. Thêm 2 chứng chỉ Ngoại ngữ (Lấy cái cao nhất: IELTS 6.5 -> 1.5đ)
        saveDiemCong(TEST_CCCD, "NGOAINGU", 1.0, 0.0, "01/01/2025"); // IELTS 5.0
        saveDiemCong(TEST_CCCD, "NGOAINGU", 1.5, 0.0, "15/05/2025"); // IELTS 6.5 (Cao nhất)
        
        // 2. Thêm 2 giải HSG (Cộng dồn: 1.0 + 0.5 = 1.5đ)
        saveDiemCong(TEST_CCCD, "HSG_QUOCGIA", 0.0, 1.0, null);
        saveDiemCong(TEST_CCCD, "HSG_TINH", 0.0, 0.5, null);

        // Act
        Double totalBonus = bonusPointService.calculateRawBonusPoint(TEST_CCCD);

        // Assert: 1.5 (Cert) + 1.5 (HSG) = 3.0
        assertEquals(3.0, totalBonus, "Tổng điểm cộng thô phải là 3.0 (1.5 cert + 1.5 HSG)");
    }

    @Test
    void testCalculateRawBonusPoint_ExpiredCertificate() {
        // Chứng chỉ 2.0 điểm nhưng cấp năm 2023 (Trước hạn 30/06/2024 trong AppConstants)
        saveDiemCong(TEST_CCCD, "NGOAINGU", 2.0, 0.0, "01/06/2023"); 
        
        // Giải HSG 1.0 điểm
        saveDiemCong(TEST_CCCD, "HSG", 0.0, 1.0, null);

        // Act
        Double totalBonus = bonusPointService.calculateRawBonusPoint(TEST_CCCD);

        // Assert: Chỉ còn 1.0 điểm HSG vì chứng chỉ hết hạn
        assertEquals(1.0, totalBonus, "Chứng chỉ hết hạn không được tính điểm.");
    }

    @Test
    void testCalculateRawBonusPoint_InvalidDateNoCrash() {
        // Chứng chỉ có ngày cấp lỗi "Unknown" hoặc sai format
        saveDiemCong(TEST_CCCD, "NGOAINGU", 2.0, 0.0, "Unknown");
        saveDiemCong(TEST_CCCD, "NGOAINGU", 1.0, 0.0, "2025/12/31"); // Sai format dd/MM/yyyy

        // Act
        Double totalBonus = bonusPointService.calculateRawBonusPoint(TEST_CCCD);

        // Assert: Không bị crash, kết quả trả về 0.0 (hoặc điểm HSG nếu có)
        assertEquals(0.0, totalBonus, "App không được crash khi gặp ngày lỗi, chỉ cần bỏ qua chứng chỉ đó.");
    }

    private void saveDiemCong(String cccd, String pt, Double dCC, Double dUT, String ngayCap) {
        DiemCong dc = new DiemCong();
        dc.setTsCccd(cccd);
        dc.setPhuongthuc(pt);
        dc.setDiemCC(dCC);
        dc.setDiemUtxt(dUT);
        dc.setNgayCap(ngayCap);
        dc.setDcKeys("TEST_" + System.nanoTime());
        diemCongRepository.save(dc);
    }
}
