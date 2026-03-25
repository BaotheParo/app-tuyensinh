package com.sgu.tuyensinh.repository;

import com.sgu.tuyensinh.entity.Nganh;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Set;

/**
 * Repository cho bảng ngành.
 */
public interface NganhRepository extends JpaRepository<Nganh, String> {

    @Query("SELECT n.maNganh FROM Nganh n WHERE n.maNganh IN :maNganhList")
    Set<String> findExistingMaNganh(@Param("maNganhList") List<String> maNganhList);
}