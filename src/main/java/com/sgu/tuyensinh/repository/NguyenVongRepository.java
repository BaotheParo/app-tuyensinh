package com.sgu.tuyensinh.repository;

import com.sgu.tuyensinh.entity.NguyenVong;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

/**
 * Repository thao tác bảng nguyện vọng {@code xt_nguyenvongxettuyen}.
 */
public interface NguyenVongRepository extends JpaRepository<NguyenVong, Integer> {

    boolean existsByNvManganh(String nvManganh);

    boolean existsByTtThm(String nvMaToHop);

    Page<NguyenVong> findByNnCccdContainingIgnoreCaseOrNvManganhContainingIgnoreCase(
            String nnCccd, String nvManganh, Pageable pageable);

    List<NguyenVong> findByNvManganhAndNvKetQua(String nvManganh, String nvKetQua);
}