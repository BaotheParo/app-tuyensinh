package com.sgu.tuyensinh.repository;

import com.sgu.tuyensinh.entity.ThiSinh;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * Repository cho bảng thí sinh.
 */
public interface ThiSinhRepository extends JpaRepository<ThiSinh, String> {

    @Query(value = "SELECT t FROM ThiSinh t LEFT JOIN FETCH t.diemThi",
           countQuery = "SELECT count(t) FROM ThiSinh t")
    Page<ThiSinh> findAllWithDiemThi(Pageable pageable);
}

