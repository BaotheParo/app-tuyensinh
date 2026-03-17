package com.sgu.tuyensinh.repository;

import com.sgu.tuyensinh.entity.ThiSinh;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository cho bảng thí sinh.
 */
public interface ThiSinhRepository extends JpaRepository<ThiSinh, String> {
}

