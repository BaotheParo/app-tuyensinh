package com.sgu.tuyensinh.service;

import com.sgu.tuyensinh.entity.DiemThi;
import com.sgu.tuyensinh.entity.ThiSinh;
import com.sgu.tuyensinh.repository.DiemThiRepository;
import com.sgu.tuyensinh.repository.ThiSinhRepository;
import com.sgu.tuyensinh.service.dto.DiemCongDTO;
import com.sgu.tuyensinh.service.dto.DiemThiDTO;
import com.sgu.tuyensinh.service.dto.ThiSinhDetailDTO;
import com.sgu.tuyensinh.repository.custom.ThiSinhCustomRepository;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Pageable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service xử lý logic và cung cấp API nội bộ cho UI (Desktop Admin: Java
 * Swing).
 * Không sử dụng Controller REST vì giao diện gọi trực tiếp hàm (Mô hình
 * Monolith).
 */
@Service
public class ThiSinhService {

    @Autowired
    private ThiSinhRepository thiSinhRepository;

    @Autowired
    private DiemThiRepository diemThiRepository;

    @Autowired
    private ThiSinhCustomRepository repo;

    /**
     * 1. Lấy danh sách phân trang và ngăn chặn N+1 query.
     * Sử dụng PageRequest để lấy đúng lượng dữ liệu (VD: 20 dòng/trang).
     */
    public Page<ThiSinh> getDanhSachThiSinhPhanTrang(int pageNumber, int pageSize) {
        return thiSinhRepository.findAllWithDiemThi(PageRequest.of(pageNumber, pageSize));
    }

    /**
     * 2. Cập nhật đè điểm số cho một thí sinh.
     * Dùng Annotation @Transactional để bảo đảm tính toàn vẹn,
     * nếu có lỗi văng ra hệ thống sẽ rollback dữ liệu.
     */
    @Transactional
    public DiemThi capNhatDiemThi(String cccd, DiemThi diemMoi) {
        // Kiểm tra thi sinh có thực sự tồn tại trong DB không
        thiSinhRepository.findById(cccd)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thí sinh với CCCD: " + cccd));

        // Lấy DiemThi hiện tại, nếu chưa nhập bao giờ (null) thì tạo record trắng
        DiemThi diemHienTai = diemThiRepository.findByCccd(cccd)
                .orElse(new DiemThi());

        if (diemHienTai.getCccd() == null) {
            diemHienTai.setCccd(cccd);
        }

        // Cập nhật các điểm môn văn hóa cơ bản
        diemHienTai.setToan(diemMoi.getToan());
        diemHienTai.setVan(diemMoi.getVan());
        diemHienTai.setLy(diemMoi.getLy());
        diemHienTai.setHoa(diemMoi.getHoa());
        diemHienTai.setSinh(diemMoi.getSinh());
        diemHienTai.setSu(diemMoi.getSu());
        diemHienTai.setDia(diemMoi.getDia());
        diemHienTai.setAnh(diemMoi.getAnh());

        // Cập nhật các điểm năng khiếu (nếu môn trống, null vẫn được lưu hợp lệ theo
        // PRD)
        diemHienTai.setNk1(diemMoi.getNk1());
        diemHienTai.setNk2(diemMoi.getNk2());
        diemHienTai.setNk3(diemMoi.getNk3());
        diemHienTai.setNk4(diemMoi.getNk4());
        diemHienTai.setNk5(diemMoi.getNk5());
        diemHienTai.setNk6(diemMoi.getNk6());
        diemHienTai.setNk7(diemMoi.getNk7());
        diemHienTai.setNk8(diemMoi.getNk8());

        // Lưu vào JPA repository
        return diemThiRepository.save(diemHienTai);
    }

    // 3. Lấy chi tiết thí sinh kèm điểm thi & điểm cộng

    public List<ThiSinhDetailDTO> getThiSinhDetailsForScoring() {

        List<Object[]> tsRows = repo.fetchThiSinhWithDiemThi();
        List<Object[]> dcRows = repo.fetchDiemCong();

        // ===== Map DiemCong theo CCCD =====
        Map<String, List<DiemCongDTO>> diemCongMap = new HashMap<>();

        for (Object[] row : dcRows) {
            DiemCongDTO dc = new DiemCongDTO(
                    (String) row[0],
                    (String) row[1],
                    (String) row[2],
                    (String) row[3],
                    (Double) row[4],
                    (Double) row[5],
                    (Double) row[6],
                    (String) row[7]);

            diemCongMap
                    .computeIfAbsent(dc.getTsCccd(), k -> new ArrayList<>())
                    .add(dc);
        }

        // ===== Build result =====
        List<ThiSinhDetailDTO> result = new ArrayList<>();

        for (Object[] row : tsRows) {

            String cccd = (String) row[0];

            // map DiemThi
            DiemThiDTO diemThi = new DiemThiDTO(
                    cccd,
                    (Double) row[7], (Double) row[8], (Double) row[9],
                    (Double) row[10], (Double) row[11],
                    (Double) row[12], (Double) row[13], (Double) row[14],
                    (Double) row[15], (Double) row[16], (Double) row[17],
                    (Double) row[18], (Double) row[19], (Double) row[20],
                    (Double) row[21], (Double) row[22]);

            ThiSinhDetailDTO dto = new ThiSinhDetailDTO(
                    cccd,
                    (String) row[1],
                    (String) row[2],
                    (String) row[3],
                    (String) row[4],
                    (String) row[5],
                    (String) row[6],
                    diemThi,
                    diemCongMap.getOrDefault(cccd, new ArrayList<>()));

            result.add(dto);
        }

        return result;
    }

    public Page<ThiSinh> searchThiSinh(String keyword, Pageable pageable) {

        if (keyword == null || keyword.trim().isEmpty()) {
            return thiSinhRepository.findAll(pageable);
        }

        String keywordTrim = keyword.trim();

        return thiSinhRepository.searchThiSinh(keywordTrim, pageable);
    }
    //4. Tìm kiếm thí sinh theo CCCD hoặc Họ tên (có phân trang)
    public Page<ThiSinh> findByIdContainingIgnoreCaseOrHoTenContainingIgnoreCase (String keyword, Pageable pageable) {

        if (keyword == null || keyword.trim().isEmpty()) {
            return thiSinhRepository.findAll(pageable);
        }

        String keywordTrim = keyword.trim();

        return thiSinhRepository.findByIdContainingIgnoreCaseOrHoTenContainingIgnoreCase(
                keywordTrim, keywordTrim, pageable);
    }
}
