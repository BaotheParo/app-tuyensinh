package com.sgu.tuyensinh.repository;

import com.sgu.tuyensinh.entity.DiemThi;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Repository cho bảng điểm thi.
 */
public interface DiemThiRepository extends JpaRepository<DiemThi, Long> {

    Optional<DiemThi> findByCccd(String cccd);
}

