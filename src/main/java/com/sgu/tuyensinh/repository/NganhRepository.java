package com.sgu.tuyensinh.repository;

import com.sgu.tuyensinh.entity.Nganh;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository cho bảng ngành.
 */
public interface NganhRepository extends JpaRepository<Nganh, String> {
}

