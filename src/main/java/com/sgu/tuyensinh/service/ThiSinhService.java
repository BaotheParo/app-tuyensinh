package com.sgu.tuyensinh.service;

import com.sgu.tuyensinh.entity.DiemThi;
import com.sgu.tuyensinh.entity.ThiSinh;
import com.sgu.tuyensinh.repository.DiemThiRepository;
import com.sgu.tuyensinh.repository.ThiSinhRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service xử lý logic và cung cấp API nội bộ cho UI (Desktop Admin: Java Swing).
 * Không sử dụng Controller REST vì giao diện gọi trực tiếp hàm (Mô hình Monolith).
 */
@Service
public class ThiSinhService {

    @Autowired
    private ThiSinhRepository thiSinhRepository;

    @Autowired
    private DiemThiRepository diemThiRepository;

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

        // Cập nhật các điểm năng khiếu (nếu môn trống, null vẫn được lưu hợp lệ theo PRD)
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
}
