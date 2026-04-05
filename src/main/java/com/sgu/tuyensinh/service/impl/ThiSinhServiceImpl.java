package com.sgu.tuyensinh.service.impl;

import com.sgu.tuyensinh.entity.ThiSinh;
import com.sgu.tuyensinh.repository.ThiSinhRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service xử lý nghiệp vụ cho Thí Sinh.
 * Chứa logic CRUD và phân trang gọi trực tiếp từ UI.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ThiSinhServiceImpl {

    private final ThiSinhRepository thiSinhRepository;

    /**
     * Lấy danh sách Thí sinh có phân trang.
     * Hỗ trợ tìm kiếm chéo (cross-cutting) theo CCCD hoặc Họ Tên.
     */
    public Page<ThiSinh> layDanhSachPhanTrang(int page, int size, String keyword) {
        Pageable pageable = PageRequest.of(page, size);

        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = keyword.trim();
            return thiSinhRepository.findByIdContainingOrHoTenContainingIgnoreCase(kw, kw, pageable);
        }

        return thiSinhRepository.findAll(pageable);
    }

    /**
     * Thêm mới hoặc Cập nhật thông tin Thí Sinh.
     * Đảm bảo tính toàn vẹn dữ liệu (Data Integrity).
     */
    @Transactional
    public ThiSinh luuThiSinh(ThiSinh thiSinh) {
        // Hacking reality: Bắt lỗi ngay tại cửa ngõ logic thay vì đợi Database báo lỗi
        if (thiSinh.getId() == null || thiSinh.getId().trim().length() < 9) {
            log.warn("Lưu thất bại: CCCD không hợp lệ - {}", thiSinh.getId());
            throw new IllegalArgumentException("CCCD không hợp lệ (phải từ 9-12 số)!");
        }
        if (thiSinh.getHoTen() == null || thiSinh.getHoTen().trim().isEmpty()) {
            log.warn("Lưu thất bại: Họ tên trống");
            throw new IllegalArgumentException("Họ tên không được để trống!");
        }

        // Chuẩn hóa (Optimize) dữ liệu rác từ UI
        thiSinh.setId(thiSinh.getId().trim());
        thiSinh.setHoTen(thiSinh.getHoTen().trim());
        if (thiSinh.getMaTruong() != null) thiSinh.setMaTruong(thiSinh.getMaTruong().trim());
        if (thiSinh.getMaTinh() != null) thiSinh.setMaTinh(thiSinh.getMaTinh().trim());

        log.info("Thực thi lưu Thí sinh CCCD: {}", thiSinh.getId());
        return thiSinhRepository.save(thiSinh);
    }

    /**
     * Xóa Thí Sinh theo CCCD.
     */
    @Transactional
    public void xoaThiSinh(String cccd) {
        String idClean = cccd.trim();

        // Reset & Rollback ngay nếu dữ liệu không tồn tại
        if (!thiSinhRepository.existsById(idClean)) {
            log.error("Xóa thất bại: Không tìm thấy CCCD {}", idClean);
            throw new IllegalArgumentException("Không tìm thấy thí sinh với CCCD: " + idClean);
        }

        thiSinhRepository.deleteById(idClean);
        log.info("Đã xóa Thí sinh CCCD: {}", idClean);
    }
}