package com.sgu.tuyensinh.service;

import com.sgu.tuyensinh.entity.DiemThi;
import com.sgu.tuyensinh.entity.NganhToHop;
import com.sgu.tuyensinh.entity.ThiSinh;
import com.sgu.tuyensinh.service.dto.DiemXetTuyenDTO;
import com.sgu.tuyensinh.util.AppConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ScoringService {


    @Autowired
    private BonusPointService bonusPointService;

    /**
     * Tính điểm Xét Tuyển (ĐXT) dựa trên chuẩn 6 bước xét tuyển.
     * Cấu trúc viết đơn giản cho sinh viên năm 3 tham khảo dễ hiểu.
     */
    public Double calculateFinalScore(ThiSinh ts, NganhToHop nth) {
        double dcRaw = 0.0;
        if (ts.getId() != null) {
            dcRaw = bonusPointService.calculateRawBonusPoint(ts.getId());
        }
        return calculateFinalScore(ts, nth, dcRaw);
    }

    public Double calculateFinalScore(ThiSinh ts, NganhToHop nth, double dcRaw) {
        // Bước 1: Tính Điểm Tổ Hợp Gốc (ĐTHGXT)
        Double dtHxt = calculateComboScore(ts, nth, "THPT");
        double dolech = (nth.getDoLech() != null) ? nth.getDoLech() : 0.0;
        double dthgxt = dtHxt - dolech;

        // Bước 3: Chặn trần Điểm Cộng (Cap ĐC)
        double capDc = Math.min(dcRaw, 3.0);

        // Bước 4: Tính Base Score & Chặn trần 30
        double baseScore = Math.min(dthgxt + capDc, AppConstants.MAX_SCORE);

        // Bước 5: Tính Điểm Ưu Tiên (ĐƯT) có giảm dần
        double dutGoc = calculateBaseUT(ts);
        double dutThucTe;

        if (baseScore >= AppConstants.THRESHOLD_UUTIEN) {
            dutThucTe = ((AppConstants.MAX_SCORE - baseScore) / 7.5) * dutGoc;
        } else {
            dutThucTe = dutGoc;
        }

        // Bước 6: Chốt Điểm Xét Tuyển cuối cùng (ĐXT)
        double finalScore = Math.min(baseScore + dutThucTe, AppConstants.MAX_SCORE);

        return Math.round(finalScore * 100.0) / 100.0;
    }

    /**
     * Tính toán và trả về chi tiết các thành phần điểm.
     */
    public DiemXetTuyenDTO calculateDetailedScore(ThiSinh ts, NganhToHop nth, String phuongThuc) {
        double dcRaw = 0.0;
        if (ts.getId() != null) {
            dcRaw = bonusPointService.calculateRawBonusPoint(ts.getId());
        }
        return calculateDetailedScore(ts, nth, phuongThuc, dcRaw);
    }

    public DiemXetTuyenDTO calculateDetailedScore(ThiSinh ts, NganhToHop nth, String phuongThuc, double dcRaw) {
        Double dtHxt = calculateComboScore(ts, nth, phuongThuc);
        double dolech = (nth.getDoLech() != null) ? nth.getDoLech() : 0.0;
        double dthgxt = dtHxt - dolech;

        double capDc = Math.min(dcRaw, 3.0);
        double baseScore = Math.min(dthgxt + capDc, AppConstants.MAX_SCORE);

        double dutGoc = calculateBaseUT(ts);
        double dutThucTe;

        if (baseScore >= AppConstants.THRESHOLD_UUTIEN) {
            dutThucTe = ((AppConstants.MAX_SCORE - baseScore) / 7.5) * dutGoc;
        } else {
            dutThucTe = dutGoc;
        }

        double finalScore = Math.min(baseScore + dutThucTe, AppConstants.MAX_SCORE);

        return DiemXetTuyenDTO.builder()
                .diemThxt(Math.round(dtHxt * 100.0) / 100.0)
                .diemCong(Math.round(capDc * 100.0) / 100.0)
                .diemUtqd(Math.round(dutThucTe * 100.0) / 100.0)
                .diemXetTuyen(Math.round(finalScore * 100.0) / 100.0)
                .ttThm(nth.getMaToHop())
                .phuongThuc(phuongThuc)
                .build();
    }

    /**
     * Hàm phụ: Tính tổng 3 môn của tổ hợp từ đối tượng ThiSinh.
     */
    private Double calculateComboScore(ThiSinh ts, NganhToHop nth, String type) {
        DiemThi diemThi = ts.getDiemThi();
        if (diemThi == null) {
            return 0.0;
        }

        // Đọc điểm thô từ 3 môn (null thì tính là 0.0)
        double m1 = getScoreBySubject(diemThi, nth.getThMon1());
        double m2 = getScoreBySubject(diemThi, nth.getThMon2());
        double m3 = getScoreBySubject(diemThi, nth.getThMon3());

        // CHỐT THEO PRD 2.10.1: Nếu có bất kỳ môn nào = 0.0 hoặc không có điểm -> Loại tổ hợp (Trả về 0.0)
        if (m1 <= 0.0 || m2 <= 0.0 || m3 <= 0.0) {
            return 0.0;
        }

        // Nếu tham gia thi ĐGNL hoặc V-SAT, gọi hàm quy đổi mock
        if ("VSAT".equalsIgnoreCase(type) || "DGNL".equalsIgnoreCase(type)) {
            m1 = mockConvertScore(m1, type);
            m2 = mockConvertScore(m2, type);
            m3 = mockConvertScore(m3, type);
        }

        // Tính công thức (điểm 1*hs1 + điểm 2*hs2 + điểm 3*hs3)
        double w1 = (nth.getHsMon1() != null) ? nth.getHsMon1() : 1.0;
        double w2 = (nth.getHsMon2() != null) ? nth.getHsMon2() : 1.0;
        double w3 = (nth.getHsMon3() != null) ? nth.getHsMon3() : 1.0;

        double tongHeSo = w1 + w2 + w3;
        double tongDiem = (m1 * w1) + (m2 * w2) + (m3 * w3);

        // Quy về thang đo 30 (tongDiem / tongHeSo * 3)
        return (tongDiem / tongHeSo) * 3;
    }

    @Autowired
    private com.sgu.tuyensinh.repository.BangQuyDoiRepository bangQuyDoiRepository;

    /**
     * Hàm quy đổi điểm V-SAT / ĐGNL sử dụng bảng quy đổi.
     */
    private Double mockConvertScore(Double rawScore, String type) {
        if (rawScore == null)
            return 0.0;
            
        // Nếu điểm đã nằm trong thang 10 (do mock data), giữ nguyên để tránh bị chia nhỏ
        if (rawScore <= 10.0) {
            return rawScore;
        }

        // Lấy danh sách quy đổi từ DB
        java.util.List<com.sgu.tuyensinh.entity.BangQuyDoi> dsQuyDoi = bangQuyDoiRepository.findByPhuongThuc(type);
        
        if (dsQuyDoi != null && !dsQuyDoi.isEmpty()) {
            // Tìm quy tắc có điểm gốc gần nhất hoặc bao hàm rawScore
            // Sắp xếp theo điểm gốc A giảm dần (từ cao xuống thấp)
            dsQuyDoi.sort((a, b) -> Double.compare(
                b.getDiemGocA() != null ? b.getDiemGocA() : 0.0, 
                a.getDiemGocA() != null ? a.getDiemGocA() : 0.0
            ));
            
            for (com.sgu.tuyensinh.entity.BangQuyDoi rule : dsQuyDoi) {
                double minScore = rule.getDiemGocA() != null ? rule.getDiemGocA() : 0.0;
                double maxScore = rule.getDiemGocB() != null ? rule.getDiemGocB() : Double.MAX_VALUE;
                
                // Nếu rawScore nằm trong khoảng (hoặc lớn hơn minScore nếu maxScore không có)
                if (rawScore >= minScore && (rule.getDiemGocB() == null || rawScore <= maxScore)) {
                    return rule.getDiemQuyDoiC() != null ? rule.getDiemQuyDoiC() : 0.0;
                }
            }
        }

        // Fallback nội suy tuyến tính nếu không có luật trong bảng
        if ("VSAT".equalsIgnoreCase(type)) {
            // VSAT thang 450 -> Thang 10: raw / 45
            return (rawScore / 45.0);
        } else if ("DGNL".equalsIgnoreCase(type)) {
            // ĐGNL thang 1200 -> Thang 10: raw / 120
            return (rawScore / 120.0);
        }
        return rawScore;
    }

    /**
     * Hàm hỗ trợ rẽ nhánh tìm điểm môn dựa trên chuỗi cấu hình.
     */
    private double getScoreBySubject(DiemThi dt, String subject) {
        if (subject == null)
            return 0.0;
        String sub = subject.toUpperCase().trim();
        Double sc = null;
        
        if (sub.equals("TO") || sub.equals("TOAN") || sub.equals("TOÁN")) sc = dt.getToan();
        else if (sub.equals("VA") || sub.equals("VAN") || sub.equals("VĂN")) sc = dt.getVan();
        else if (sub.equals("LI") || sub.equals("LY") || sub.equals("LÝ")) sc = dt.getLy();
        else if (sub.equals("HO") || sub.equals("HOA") || sub.equals("HÓA")) sc = dt.getHoa();
        else if (sub.equals("SI") || sub.equals("SINH")) sc = dt.getSinh();
        else if (sub.equals("SU") || sub.equals("SỬ")) sc = dt.getSu();
        else if (sub.equals("DI") || sub.equals("DIA") || sub.equals("ĐỊA")) sc = dt.getDia();
        else if (sub.equals("AN") || sub.equals("ANH") || sub.equals("N1") || sub.equals("N1_CC") || sub.equals("NN1")) sc = dt.getAnh();
        else if (sub.startsWith("NK1")) sc = dt.getNk1();
        else if (sub.startsWith("NK2")) sc = dt.getNk2();

        return (sc != null) ? sc : 0.0;
    }

    /**
     * Tính ĐƯT gốc từ ĐTƯT và KVƯT (theo quy định nhà nước cấp sẵn).
     */
    private double calculateBaseUT(ThiSinh ts) {
        double ut = 0.0;

        // Mức điểm ưu tiên theo Đối tượng
        if (ts.getDoiTuongUt() != null) {
            String dt = ts.getDoiTuongUt().trim();
            if (dt.equals("01") || dt.equals("04"))
                ut += 2.0;
            else if (dt.equals("02"))
                ut += 1.5;
            else if (dt.equals("03"))
                ut += 1.0;
            else if (dt.equals("05") || dt.equals("06a"))
                ut += 0.5;
            else if (dt.equals("07"))
                ut += 0.25;
        }

        // Mức điểm ưu tiên theo Khu vực
        if (ts.getKhuVucUt() != null) {
            String kv = ts.getKhuVucUt().trim().toUpperCase();
            if (kv.equals("KV1"))
                ut += 0.75;
            else if (kv.equals("KV2NT"))
                ut += 0.5;
            else if (kv.equals("KV2"))
                ut += 0.25;
            // KV3 là 0
        }

        return ut;
    }
}
