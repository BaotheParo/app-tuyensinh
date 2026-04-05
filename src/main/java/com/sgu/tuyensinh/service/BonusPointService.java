package com.sgu.tuyensinh.service;

import com.sgu.tuyensinh.entity.DiemCong;
import com.sgu.tuyensinh.repository.DiemCongRepository;
import com.sgu.tuyensinh.util.AppConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service tính toán điểm cộng (Bonus Points) cho Thí sinh.
 * Master Logic QA-3.1 / BE-2.5
 */
@Service
public class BonusPointService {

    @Autowired
    private DiemCongRepository diemCongRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Tính tổng điểm cộng thô (Raw Bonus Score) cho một thí sinh.
     * Logic: Tổng điểm HSG + Điểm cao nhất của Chứng chỉ Ngoại ngữ còn hạn.
     * 
     * @param cccd CCCD của thí sinh
     * @return Tổng điểm cộng thô (chưa chặn trần 3.0)
     */
    public Double calculateRawBonusPoint(String cccd) {
        // 1. Lấy toàn bộ danh sách điểm cộng của thí sinh
        List<DiemCong> allDiemCong = diemCongRepository.findByTsCccd(cccd);

        double diemHSG = 0.0;
        double maxDiemNgoaiNgu = 0.0;

        // 2. Duyệt qua từng bản ghi điểm cộng
        for (DiemCong dc : allDiemCong) {
            try {
                String phuongThuc = (dc.getPhuongthuc() != null) ? dc.getPhuongthuc().toUpperCase() : "";

                // Trường hợp: Chứng chỉ Ngoại ngữ (NGOAINGU)
                if (phuongThuc.contains("NGOAINGU")) {
                    String ngayCapStr = dc.getNgayCap();
                    if (ngayCapStr != null && !ngayCapStr.isEmpty()) {
                        LocalDate ngayCap = parseLocalDateSafe(ngayCapStr, cccd);

                        // Nếu parse thành công, tiến hành kiểm tra hạn
                        if (ngayCap != null) {
                            if (ngayCap.isBefore(AppConstants.EXPIRY_DATE)) {
                                System.out.println("Chứng chỉ hết hạn (CCCD " + cccd + "): " + ngayCapStr);
                                continue;
                            }

                            double diemCC = (dc.getDiemCC() != null) ? dc.getDiemCC() : 0.0;
                            if (diemCC > maxDiemNgoaiNgu) {
                                maxDiemNgoaiNgu = diemCC;
                            }
                        } else {
                            System.err.println("Bỏ qua chứng chỉ do lỗi ngày cấp (CCCD " + cccd + "): " + ngayCapStr);
                        }
                    }
                } 
                // Trường hợp khác (HSG, Ưu tiên...)
                else {
                    double diemUT = (dc.getDiemUtxt() != null) ? dc.getDiemUtxt() : 0.0;
                    diemHSG += diemUT;
                }
            } catch (Exception e) {
                System.err.println("Lỗi xử lý dòng điểm cộng cho thí sinh " + cccd + ": " + e.getMessage());
            }
        }

        // 3. Kết quả là tổng điểm HSG và chứng chỉ ngoại ngữ tốt nhất
        return diemHSG + maxDiemNgoaiNgu;
    }

    /**
     * Hỗ trợ parse ngày tháng linh hoạt từ dd/MM/yyyy hoặc yyyy-MM-dd.
     */
    private LocalDate parseLocalDateSafe(String dateStr, String cccd) {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;
        
        // 1. Thử dd/MM/yyyy
        try {
            return LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (Exception e1) {
            // 2. Thử yyyy-MM-dd
            try {
                return LocalDate.parse(dateStr);
            } catch (Exception e2) {
                System.err.println("Không thể parse ngày (CCCD " + cccd + "): " + dateStr);
                return null;
            }
        }
    }
}
