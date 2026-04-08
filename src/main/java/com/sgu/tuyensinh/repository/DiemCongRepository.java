package com.sgu.tuyensinh.repository;

import com.sgu.tuyensinh.entity.DiemCong;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Repository thao tác bảng điểm cộng ưu tiên {@code xt_diemcongxetuyen}.
 */
public interface DiemCongRepository extends JpaRepository<DiemCong, Integer> {
    List<DiemCong> findByTsCccd(String tsCccd);

    // BỔ SUNG: Truy vấn phân trang cho UI Read-only
    Page<DiemCong> findByTsCccdContainingIgnoreCase(String tsCccd, Pageable pageable);
}

