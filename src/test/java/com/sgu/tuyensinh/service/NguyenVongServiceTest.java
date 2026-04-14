

package com.sgu.tuyensinh.service;

import com.sgu.tuyensinh.entity.NguyenVong;
import com.sgu.tuyensinh.entity.ThiSinh;
import com.sgu.tuyensinh.repository.NguyenVongRepository;
import com.sgu.tuyensinh.repository.ThiSinhRepository;
import com.sgu.tuyensinh.service.dto.NguyenVongResultDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class NguyenVongServiceTest {

    @Autowired
    private NguyenVongService nguyenVongService;

    @Autowired
    private NguyenVongRepository nguyenVongRepository;

    @Autowired
    private ThiSinhRepository thiSinhRepository;

    /**
     * Test chính dùng saveAll()
     */
    @Test
    void capNhatKetQuaTuLead_ShouldUpdateSuccessfully() {

        // ===== GIVEN =====
        NguyenVong nv1 = createTestNguyenVong("012345678901", "CNTT");
        NguyenVong nv2 = createTestNguyenVong("012345678902", "QTKD");
        NguyenVong nv3 = createTestNguyenVong("012345678903", "LUAT");

        List<NguyenVongResultDTO> inputData = List.of(
                new NguyenVongResultDTO(nv1.getIdnv(), "TRUNG_TUYEN"),
                new NguyenVongResultDTO(nv2.getIdnv(), "TRUOT"),
                new NguyenVongResultDTO(nv3.getIdnv(), "TRUNG_TUYEN"),
                new NguyenVongResultDTO(99999, "TRUNG_TUYEN") // không tồn tại
        );

        // ===== WHEN =====
        nguyenVongService.capNhatKetQuaTuLead(inputData);

        // ===== THEN =====
        NguyenVong rs1 = nguyenVongRepository.findById(nv1.getIdnv()).orElse(null);
        NguyenVong rs2 = nguyenVongRepository.findById(nv2.getIdnv()).orElse(null);
        NguyenVong rs3 = nguyenVongRepository.findById(nv3.getIdnv()).orElse(null);

        assertNotNull(rs1);
        assertNotNull(rs2);
        assertNotNull(rs3);

        assertEquals("TRUNG_TUYEN", rs1.getNvKetQua());
        assertEquals("TRUOT", rs2.getNvKetQua());
        assertEquals("TRUNG_TUYEN", rs3.getNvKetQua());

        System.out.println("TEST UPDATE OK");
    }

    /**
     * Test null / empty
     */
    @Test
    void capNhatKetQuaTuLead_WithEmptyOrNullList_ShouldDoNothing() {

        nguyenVongService.capNhatKetQuaTuLead(null);
        nguyenVongService.capNhatKetQuaTuLead(List.of());

        System.out.println("TEST NULL/EMPTY OK");
    }

    // ================= HELPER =================

    private NguyenVong createTestNguyenVong(String cccd, String manganh) {

        createTestThiSinh(cccd);

        NguyenVong nv = new NguyenVong();

        nv.setNnCccd(cccd);
        nv.setNvManganh(manganh);
        nv.setNvTt(1);
        nv.setNvKetQua(null);

        return nguyenVongRepository.save(nv);
    }

    private void createTestThiSinh(String cccd) {
        if (thiSinhRepository.findById(cccd).isEmpty()) {
            ThiSinh ts = new ThiSinh();
            ts.setId(cccd);
            ts.setHoTen("Test User " + cccd);
            thiSinhRepository.save(ts);
        }
    }
}