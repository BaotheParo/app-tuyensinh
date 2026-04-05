package com.sgu.tuyensinh.repository;

import com.sgu.tuyensinh.entity.ToHop;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Set;

public interface ToHopRepository extends JpaRepository<ToHop, Integer> {

    @Query("SELECT t.maToHop FROM ToHop t WHERE t.maToHop IN :maToHopList")
    Set<String> findExistingMaToHop(@Param("maToHopList") List<String> maToHopList);

    // BỔ SUNG: Truy vấn phân trang
    Page<ToHop> findByMaToHopContainingIgnoreCaseOrTenToHopContainingIgnoreCase(String maToHop, String tenToHop, Pageable pageable);

    // BỔ SUNG: Kiểm tra trùng mã tổ hợp (dùng cho hàm Thêm Mới)
    boolean existsByMaToHop(String maToHop);
}