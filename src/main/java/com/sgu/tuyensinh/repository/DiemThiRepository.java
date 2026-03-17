package com.sgu.tuyensinh.repository;

import com.sgu.tuyensinh.entity.DiemThi;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository cho bảng điểm thi.
 */
public interface DiemThiRepository extends JpaRepository<DiemThi, Long> {
}

