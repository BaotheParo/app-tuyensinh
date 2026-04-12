package com.sgu.tuyensinh.service;

import com.sgu.tuyensinh.entity.DiemCong;
import com.sgu.tuyensinh.repository.DiemCongRepository;
import com.sgu.tuyensinh.repository.ThiSinhRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service xử lý CRUD cho các loại Điểm cộng (Chứng chỉ, HSG).
 * Dành cho Admin sửa tay dữ liệu thí sinh.
 */
@Service
public class DiemCongService {

    @Autowired
    private DiemCongRepository diemCongRepository;

    @Autowired
    private ThiSinhRepository thiSinhRepository;

    /**
     * Thêm một chứng chỉ hoặc giải thưởng mới cho thí sinh.
     * Kiểm tra sự tồn tại của thí sinh trước khi lưu.
     */
    @Transactional
    public DiemCong addDiemCong(String cccd, DiemCong diemCong) {
        if (!thiSinhRepository.existsById(cccd)) {
            throw new EntityNotFoundException("Không tìm thấy thí sinh với CCCD: " + cccd);
        }
        diemCong.setTsCccd(cccd);
        // dcKeys là bắt buộc trong schema để chống trùng khi import, 
        // khi add tay ta có thể generate random hoặc dùng timestamp.
        if (diemCong.getDcKeys() == null) {
            diemCong.setDcKeys("MANUAL_" + System.currentTimeMillis());
        }
        return diemCongRepository.save(diemCong);
    }

    /**
     * Cập nhật thông tin điểm cộng hiện có dựa trên ID.
     */
    @Transactional
    public DiemCong updateDiemCong(Integer id, DiemCong diemCongMoi) {
        DiemCong existing = diemCongRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy bản ghi điểm cộng ID: " + id));

        // Senior: Chỉ cập nhật các field nghiệp vụ, không đổi CCCD hay ID
        existing.setManganh(diemCongMoi.getManganh());
        existing.setMatohop(diemCongMoi.getMatohop());
        existing.setPhuongthuc(diemCongMoi.getPhuongthuc());
        existing.setDiemCC(diemCongMoi.getDiemCC());
        existing.setDiemUtxt(diemCongMoi.getDiemUtxt());
        existing.setDiemTong(diemCongMoi.getDiemTong());
        existing.setGhichu(diemCongMoi.getGhichu());
        existing.setNgayCap(diemCongMoi.getNgayCap());

        return diemCongRepository.save(existing);
    }

    /**
     * Xóa một bản ghi điểm cộng.
     */
    @Transactional
    public void deleteDiemCong(Integer id) {
        if (!diemCongRepository.existsById(id)) {
            throw new EntityNotFoundException("Không tìm thấy bản ghi điểm cộng ID: " + id);
        }
        diemCongRepository.deleteById(id);
    }

    /**
     * Lấy danh sách điểm cộng có phân trang (Read-only UI)
     */
    public Page<DiemCong> layDanhSachPhanTrang(int page, int size, String keyword) {
        Pageable pageable = PageRequest.of(page, size);
        if (keyword != null && !keyword.trim().isEmpty()) {
            return diemCongRepository.findByTsCccdContainingIgnoreCase(keyword, pageable);
        }
        return diemCongRepository.findAll(pageable);
    }
}
