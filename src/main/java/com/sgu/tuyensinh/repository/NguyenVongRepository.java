package com.sgu.tuyensinh.repository;

import com.sgu.tuyensinh.entity.NguyenVong;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Repository thao tác bảng nguyện vọng {@code xt_nguyenvongxettuyen}.
 */
public interface NguyenVongRepository extends JpaRepository<NguyenVong, Integer> {
    boolean existsByNvManganh(String nvManganh);

    boolean existsByTtThm(String nvMaToHop);
    
    // BỔ SUNG: Truy vấn phân trang cho UI Read-only
    Page<NguyenVong> findByNnCccdContainingIgnoreCaseOrNvManganhContainingIgnoreCase(String nnCccd, String nvManganh,
            Pageable pageable);
}
