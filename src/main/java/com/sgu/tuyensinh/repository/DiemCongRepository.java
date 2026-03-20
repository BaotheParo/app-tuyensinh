package com.sgu.tuyensinh.repository;

import com.sgu.tuyensinh.entity.DiemCong;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository thao tác bảng điểm cộng ưu tiên {@code xt_diemcongxetuyen}.
 */
public interface DiemCongRepository extends JpaRepository<DiemCong, Integer> {
}

