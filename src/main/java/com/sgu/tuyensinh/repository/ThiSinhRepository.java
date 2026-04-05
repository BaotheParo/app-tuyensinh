package com.sgu.tuyensinh.repository;

import com.sgu.tuyensinh.entity.ThiSinh;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * Repository cho bảng thí sinh.
 */
public interface ThiSinhRepository extends JpaRepository<ThiSinh, String> {

    // 1. Hàm của team (Dùng cho logic tính điểm/xét tuyển để tối ưu N+1 Query)
    @Query(value = "SELECT t FROM ThiSinh t LEFT JOIN FETCH t.diemThi",
            countQuery = "SELECT count(t) FROM ThiSinh t")
    Page<ThiSinh> findAllWithDiemThi(Pageable pageable);

    // 2. BỔ SUNG: Hàm dành riêng cho giao diện Quản lý Thí sinh (Tìm kiếm + Phân trang)
    Page<ThiSinh> findByIdContainingOrHoTenContainingIgnoreCase(String id, String hoTen, Pageable pageable);
}