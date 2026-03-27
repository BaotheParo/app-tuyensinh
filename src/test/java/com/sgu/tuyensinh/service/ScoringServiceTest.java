package com.sgu.tuyensinh.service;

import com.sgu.tuyensinh.entity.DiemThi;
import com.sgu.tuyensinh.entity.NganhToHop;
import com.sgu.tuyensinh.entity.ThiSinh;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScoringServiceTest {

    private final ScoringService scoringService = new ScoringService();

    @Test
    void testCalculateFinalScore_PriorityDropoff_CapAt30() {
        // Chuẩn bị: Thí sinh thuộc KV1 (+0.75), đạt tổng 3 môn là 28.0 điểm
        ThiSinh ts = new ThiSinh();
        ts.setKhuVucUt("KV1"); // 0.75 ưu tiên gốc

        DiemThi dt = new DiemThi();
        dt.setToan(10.0);
        dt.setVan(9.0);
        dt.setAnh(9.0); // 10 + 9 + 9 = 28
        ts.setDiemThi(dt);

        NganhToHop nth = new NganhToHop();
        nth.setThMon1("TOAN");
        nth.setThMon2("VAN");
        nth.setThMon3("ANH");
        nth.setDoLech(0.0);
        nth.setHsMon1(1.0);
        nth.setHsMon2(1.0);
        nth.setHsMon3(1.0);

        // Action
        Double finalScore = scoringService.calculateFinalScore(ts, nth);

        // Kiểm tra logic giảm dần điểm ưu tiên:
        // Cần bù: (30 - 28) / 7.5 * 0.75 = 0.2
        // Tổng: 28.0 + 0.2 = 28.2
        assertEquals(28.20, finalScore, "Trần điểm ưu tiên giảm dần phải được tính đúng");
        assertTrue(finalScore <= 30.0, "Điểm tuyệt đối không bao giờ được phép vỡ trần 30");
    }
}
