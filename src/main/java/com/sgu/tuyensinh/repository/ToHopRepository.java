package com.sgu.tuyensinh.repository;

import com.sgu.tuyensinh.entity.ToHop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Set;


/**
 * Repository thao tác bảng {@code xt_tohop_monthi}.
 */
public interface ToHopRepository extends JpaRepository<ToHop, Integer> {
    @Query("SELECT t.maToHop FROM ToHop t WHERE t.maToHop IN :maToHopList")
    Set<String> findExistingMaToHop(@Param("maToHopList") List<String> maToHopList);
}

