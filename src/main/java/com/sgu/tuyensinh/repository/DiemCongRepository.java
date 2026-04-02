package com.sgu.tuyensinh.repository;

import com.sgu.tuyensinh.entity.DiemCong;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository thao tác bảng điểm cộng ưu tiên {@code xt_diemcongxetuyen}.
 */
public interface DiemCongRepository extends JpaRepository<DiemCong, Integer> {
    List<DiemCong> findByTsCccd(String tsCccd);
}

