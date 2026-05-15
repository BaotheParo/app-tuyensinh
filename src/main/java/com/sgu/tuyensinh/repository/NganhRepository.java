package com.sgu.tuyensinh.repository;

import com.sgu.tuyensinh.entity.Nganh;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Set;

public interface NganhRepository extends JpaRepository<Nganh, String> {

    @Query("SELECT n.maNganh FROM Nganh n WHERE n.maNganh IN :maNganhList")
    Set<String> findExistingMaNganh(@Param("maNganhList") List<String> maNganhList);

    // BỔ SUNG: Truy vấn phân trang hỗ trợ tìm kiếm theo Mã hoặc Tên ngành
    Page<Nganh> findByMaNganhContainingOrTenNganhContainingIgnoreCase(String maNganh, String tenNganh, Pageable pageable);

    Nganh findByTenNganh(String tenNganh);
}