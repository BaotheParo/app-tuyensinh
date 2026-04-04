package com.sgu.tuyensinh.repository;

import com.sgu.tuyensinh.entity.NguyenVong;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository thao tác bảng nguyện vọng {@code xt_nguyenvongxettuyen}.
 */
public interface NguyenVongRepository extends JpaRepository<NguyenVong, Integer> {
    boolean existsByNvManganh(String nvManganh);
    boolean existsByTtThm(String nvMaToHop);
}

