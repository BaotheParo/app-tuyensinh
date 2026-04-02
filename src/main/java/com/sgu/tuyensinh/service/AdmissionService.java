package com.sgu.tuyensinh.service;

import com.sgu.tuyensinh.entity.Nganh;
import com.sgu.tuyensinh.entity.NguyenVong;
import com.sgu.tuyensinh.repository.NganhRepository;
import com.sgu.tuyensinh.repository.NguyenVongRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service xử lý Thuật toán Xét tuyển Đại học (Lọc ảo).
 * Đảm nhận Task BE-1.5: Thuật toán gọi trúng tuyển, phân bổ nguyện vọng theo chỉ tiêu.
 */
@Service
public class AdmissionService {

    @Autowired
    private NguyenVongRepository nguyenVongRepository;

    @Autowired
    private NganhRepository nganhRepository;

    /**
     * Chạy thuật toán Xét Tuyển và Lọc Ảo đồng loạt.
     * Cực kỳ quan trọng: Hàm này được đặt @Transactional để đảm bảo toàn vẹn dữ liệu
     * khi duyệt qua hàng ngàn nguyện vọng.
     */
    @Transactional
    public void runAdmissionProcess() {
        
        // BƯỚC 1: Reset Trạng thái
        // Lấy toàn bộ NguyenVong từ DB, reset field ketQua về "DANG_XET" để chạy lại từ đầu 
        List<NguyenVong> danhSachTatCaNV = nguyenVongRepository.findAll();
        for (NguyenVong nv : danhSachTatCaNV) {
            nv.setNvKetQua("DANG_XET");
        }

        // BƯỚC 2: Phân nhóm theo Ngành và Sắp xếp (Tie-breaker)
        // Lấy danh sách tất cả các Nganh để truy xuất cấu hình chỉ tiêu
        List<Nganh> danhSachNganh = nganhRepository.findAll();
        
        // Tạo Map giữ số lượng chỉ tiêu của mỗi ngành để tiện cập nhật (trừ dần)
        Map<String, Integer> mapChiTieuNganh = new HashMap<>();
        for (Nganh nganh : danhSachNganh) {
            mapChiTieuNganh.put(nganh.getMaNganh(), nganh.getChiTieu() != null ? nganh.getChiTieu() : 0);
        }

        // BƯỚC 2: Sắp xếp TOÀN CỤC (Global Sort)
        // Không phân nhóm theo Ngành nữa để tự do ưu tiên điểm số tối đa.
        Comparator<NguyenVong> tieBreakerComparator = (nv1, nv2) -> {
            Double diemXT1 = nv1.getDiemXetTuyen() != null ? nv1.getDiemXetTuyen() : 0.0;
            Double diemXT2 = nv2.getDiemXetTuyen() != null ? nv2.getDiemXetTuyen() : 0.0;

            // Ưu tiên 1: diemXetTuyen giảm dần (Từ cao xuống thấp)
            int soSanhDiem = diemXT2.compareTo(diemXT1);
            if (soSanhDiem != 0) {
                return soSanhDiem;
            }

            // Ưu tiên 2 (Nếu điểm bằng nhau): So sánh thutuNV tăng dần (NV1 ưu tiên hơn NV2)
            Integer ttNV1 = nv1.getNvTt() != null ? nv1.getNvTt() : Integer.MAX_VALUE;
            Integer ttNV2 = nv2.getNvTt() != null ? nv2.getNvTt() : Integer.MAX_VALUE;
            
            return ttNV1.compareTo(ttNV2);
        };

        // Nhét tất cả nguyện vọng vào chung một rổ và sắp xếp 1 lần duy nhất (Global Sort)
        danhSachTatCaNV.sort(tieBreakerComparator);

        // BƯỚC 3: Lọc Ảo (Cắt chỉ tiêu)
        // Set chứa CCCD của những thí sinh đã đậu để theo dõi (tránh việc đậu 2 ngành)
        Set<String> admittedCandidates = new HashSet<>();

        // Chỉ cần 1 vòng lặp duy nhất chạy từ trên xuống dưới danh sách đã sort
        for (NguyenVong nv : danhSachTatCaNV) {
            String cccd = nv.getNnCccd();
            String maNganh = nv.getNvManganh();
            int chiTieuConLai = mapChiTieuNganh.getOrDefault(maNganh, 0);

            // Lọc logic cốt lõi:
            if (!admittedCandidates.contains(cccd) && chiTieuConLai > 0) {
                // Nếu thí sinh (CCCD) chưa đậu trường nào VÀ ngành còn chỉ tiêu: Cho đậu
                nv.setNvKetQua("TRUNG_TUYEN");
                admittedCandidates.add(cccd); // Đưa CCCD vào tập đã đậu
                mapChiTieuNganh.put(maNganh, chiTieuConLai - 1); // Cập nhật lại Map để giảm chỉ tiêu
            } else {
                // Nếu ngành đã hết chỉ tiêu, HOẶC thí sinh đã đậu ngành khác: Đánh rớt
                nv.setNvKetQua("TRUOT");
            }
        }

        // BƯỚC 4: Lưu kết quả
        // Gom tất cả các NguyenVong (đã bị thay đổi trạng thái field ketQua) và lưu 1 đợt xuống DB
        nguyenVongRepository.saveAll(danhSachTatCaNV);
    }
}
