package com.sgu.tuyensinh.service.impl;

import com.sgu.tuyensinh.entity.DiemThi;
import com.sgu.tuyensinh.repository.DiemThiRepository;
import com.sgu.tuyensinh.service.DiemThiService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Service implementation cho các nghiệp vụ quản lý và thống kê Điểm Thi.
 */
@Service
public class DiemThiServiceImpl implements DiemThiService {

    @Autowired
    private DiemThiRepository diemThiRepository;

    @Override
    public Page<DiemThi> getDanhSachDiemThi(String keyword, int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        // Nếu không có keyword, lấy toàn bộ
        if (keyword == null || keyword.trim().isEmpty()) {
            return diemThiRepository.findAll(pageable);
        }
        // Có keyword -> Gọi custom Query tìm qua CCCD / Tên
        return diemThiRepository.findByThiSinh_CccdContainingOrThiSinh_HoTenContainingIgnoreCase(keyword, keyword, pageable);
    }

    @Override
    @Transactional
    public DiemThi updateDiemThi(String cccd, DiemThi diemMoi) {
        DiemThi existing = diemThiRepository.findByCccd(cccd)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy điểm thi của thí sinh: " + cccd));
        
        // Copy giá trị điểm từ form nhập sang Entity cũ
        existing.setToan(diemMoi.getToan());
        existing.setVan(diemMoi.getVan());
        existing.setLy(diemMoi.getLy());
        existing.setHoa(diemMoi.getHoa());
        existing.setSinh(diemMoi.getSinh());
        existing.setSu(diemMoi.getSu());
        existing.setDia(diemMoi.getDia());
        existing.setAnh(diemMoi.getAnh());
        // Cập nhật điểm năng khiếu (nếu có)
        existing.setNk1(diemMoi.getNk1());
        existing.setNk2(diemMoi.getNk2());
        existing.setNk3(diemMoi.getNk3());
        existing.setNk4(diemMoi.getNk4());
        existing.setNk5(diemMoi.getNk5());
        existing.setNk6(diemMoi.getNk6());
        existing.setNk7(diemMoi.getNk7());
        existing.setNk8(diemMoi.getNk8());
        
        return diemThiRepository.save(existing);
    }

    @Override
    @Transactional
    public void clearDiemThi(String cccd) {
        DiemThi existing = diemThiRepository.findByCccd(cccd)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy điểm thi của thí sinh: " + cccd));
        
        // Reset tất cả các môn về null
        existing.setToan(null);
        existing.setVan(null);
        existing.setLy(null);
        existing.setHoa(null);
        existing.setSinh(null);
        existing.setSu(null);
        existing.setDia(null);
        existing.setAnh(null);
        existing.setNk1(null);
        existing.setNk2(null);
        existing.setNk3(null);
        existing.setNk4(null);
        existing.setNk5(null);
        existing.setNk6(null);
        existing.setNk7(null);
        existing.setNk8(null);
        
        diemThiRepository.save(existing);
    }

    @Override
    public Map<String, Long> thongKePhoDiem(String monHoc) {
        List<Double> diemList;
        
        // Router để lấy List điểm từ DB dựa theo input môn học
        switch (monHoc.toLowerCase()) {
            case "toan": diemList = diemThiRepository.findAllDiemToan(); break;
            case "van": diemList = diemThiRepository.findAllDiemVan(); break;
            case "ly": diemList = diemThiRepository.findAllDiemLy(); break;
            case "hoa": diemList = diemThiRepository.findAllDiemHoa(); break;
            case "sinh": diemList = diemThiRepository.findAllDiemSinh(); break;
            case "su": diemList = diemThiRepository.findAllDiemSu(); break;
            case "dia": diemList = diemThiRepository.findAllDiemDia(); break;
            case "anh": diemList = diemThiRepository.findAllDiemAnh(); break;
            case "nk1": diemList = diemThiRepository.findAllDiemNk1(); break;
            case "nk2": diemList = diemThiRepository.findAllDiemNk2(); break;
            case "nk3": diemList = diemThiRepository.findAllDiemNk3(); break;
            case "nk4": diemList = diemThiRepository.findAllDiemNk4(); break;
            case "nk5": diemList = diemThiRepository.findAllDiemNk5(); break;
            case "nk6": diemList = diemThiRepository.findAllDiemNk6(); break;
            case "nk7": diemList = diemThiRepository.findAllDiemNk7(); break;
            case "nk8": diemList = diemThiRepository.findAllDiemNk8(); break;
            default: throw new IllegalArgumentException("Môn học không hỗ trợ thống kê: " + monHoc);
        }

        long kem = 0, trungBinh = 0, kha = 0, gioi = 0;

        // Phân nhóm phổ điểm
        for (Double diem : diemList) {
            if (diem == null) continue;
            
            if (diem < 5.0) {
                kem++;
            } else if (diem >= 5.0 && diem < 6.5) {
                trungBinh++;
            } else if (diem >= 6.5 && diem < 8.0) {
                kha++;
            } else {
                gioi++; // >= 8.0
            }
        }

        // Sử dụng LinkedHashMap để duy trì thứ tự chèn, tiện cho việc hiển thị Dashboard đúng thứ tự từ Thấp đến Cao
        Map<String, Long> ketQua = new LinkedHashMap<>();
        ketQua.put("Kém (< 5.0)", kem);
        ketQua.put("Trung bình (5.0 - 6.5)", trungBinh);
        ketQua.put("Khá (6.5 - 8.0)", kha);
        ketQua.put("Giỏi (> 8.0)", gioi);

        return ketQua;
    }
}
