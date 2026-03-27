package com.sgu.tuyensinh.service;

import com.sgu.tuyensinh.entity.DiemCong;
import com.sgu.tuyensinh.entity.DiemThi;
import com.sgu.tuyensinh.entity.NganhToHop;
import com.sgu.tuyensinh.entity.ThiSinh;
import com.sgu.tuyensinh.util.AppConstants;
import org.springframework.stereotype.Service;

@Service
public class ScoringService {

    /**
     * Tính điểm Xét Tuyển (ĐXT) dựa trên chuẩn 6 bước xét tuyển.
     * Cấu trúc viết đơn giản cho sinh viên năm 3 tham khảo dễ hiểu.
     */
    public Double calculateFinalScore(ThiSinh ts, NganhToHop nth) {
        // Bước 1: Tính Điểm Tổ Hợp Gốc (ĐTHGXT)
        // Cần truyền thêm 1 chuỗi giả định "PT4" (THPT) hoặc loại phương thức tùy theo tham số thực sau này
        Double dtHxt = calculateComboScore(ts, nth, "THPT");
        
        // Trừ đi độ lệch tổ hợp về gốc (ĐTHGXT = ĐTHXT - dolech)
        double dolech = (nth.getDoLech() != null) ? nth.getDoLech() : 0.0;
        double dthgxt = dtHxt - dolech;

        // Bước 2: Tính tổng Điểm Cộng thô (ĐC raw)
        double dcRaw = 0.0;
        if (ts.getDiemCongs() != null && !ts.getDiemCongs().isEmpty()) {
            for (DiemCong dc : ts.getDiemCongs()) {
                double diemCC = (dc.getDiemCC() != null) ? dc.getDiemCC() : 0.0;
                double diemUT = (dc.getDiemUtxt() != null) ? dc.getDiemUtxt() : 0.0;
                // Nếu DB đã tinh giản trước thì dùng diemTong
                double diemTong = (dc.getDiemTong() != null) ? dc.getDiemTong() : (diemCC + diemUT);
                dcRaw += diemTong;
            }
        }

        // Bước 3: Chặn trần Điểm Cộng (Cap ĐC)
        double capDc = Math.min(dcRaw, 3.0);

        // Bước 4: Tính Base Score & Chặn trần 30
        double baseScore = Math.min(dthgxt + capDc, AppConstants.MAX_SCORE);

        // Bước 5: Tính Điểm Ưu Tiên (ĐƯT) có giảm dần
        double dutGoc = calculateBaseUT(ts);
        double dutThucTe;
        
        if (baseScore >= AppConstants.THRESHOLD_UUTIEN) {
            // Áp dụng công thức giảm dần
            dutThucTe = ((AppConstants.MAX_SCORE - baseScore) / 7.5) * dutGoc;
        } else {
            dutThucTe = dutGoc;
        }

        // Bước 6: Chốt Điểm Xét Tuyển cuối cùng (ĐXT)
        double finalScore = Math.min(baseScore + dutThucTe, AppConstants.MAX_SCORE);

        // Trả về kết quả ĐXT đã làm tròn 2 chữ số thập phân
        return Math.round(finalScore * 100.0) / 100.0;
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

        // Quy về thang đo
        return (tongDiem / tongHeSo) * 3;
    }

    /**
     * Hàm mock nội suy điểm V-SAT / ĐGNL tạm thời do dev khác sẽ điền công thức sau.
     */
    private Double mockConvertScore(Double rawScore, String type) {
        if (rawScore == null) return 0.0;
        if ("VSAT".equalsIgnoreCase(type)) {
            // Tạm thời return (rawScore / 150) * 10
            return (rawScore / 150.0) * 10.0;
        } else if ("DGNL".equalsIgnoreCase(type)) {
            // Tạm thời return (rawScore / 1200) * 10 
            return (rawScore / 1200.0) * 10.0;
        }
        return rawScore;
    }

    /**
     * Hàm hỗ trợ rẽ nhánh tìm điểm môn dựa trên chuỗi cấu hình.
     */
    private double getScoreBySubject(DiemThi dt, String subject) {
        if (subject == null) return 0.0;
        Double sc = null;
        switch (subject.toUpperCase()) {
            case "TOAN": sc = dt.getToan(); break;
            case "VAN": sc = dt.getVan(); break;
            case "LY": sc = dt.getLy(); break;
            case "HOA": sc = dt.getHoa(); break;
            case "SINH": sc = dt.getSinh(); break;
            case "SU": sc = dt.getSu(); break;
            case "DIA": sc = dt.getDia(); break;
            case "ANH": sc = dt.getAnh(); break;
            case "NK1": sc = dt.getNk1(); break;
            case "NK2": sc = dt.getNk2(); break;
        }
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
            if (dt.equals("01") || dt.equals("04")) ut += 2.0;
            else if (dt.equals("02")) ut += 1.5;
            else if (dt.equals("03")) ut += 1.0;
            else if (dt.equals("05") || dt.equals("06a")) ut += 0.5;
            else if (dt.equals("07")) ut += 0.25;
        }
        
        // Mức điểm ưu tiên theo Khu vực
        if (ts.getKhuVucUt() != null) {
            String kv = ts.getKhuVucUt().trim().toUpperCase();
            if (kv.equals("KV1")) ut += 0.75;
            else if (kv.equals("KV2NT")) ut += 0.5;
            else if (kv.equals("KV2")) ut += 0.25;
            // KV3 là 0
        }
        
        return ut;
    }
}
