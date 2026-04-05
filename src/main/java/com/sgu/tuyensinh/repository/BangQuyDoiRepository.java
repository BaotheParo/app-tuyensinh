package com.sgu.tuyensinh.repository;

import com.sgu.tuyensinh.entity.BangQuyDoi;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

/**
 * Repository thao tác bảng {@code xt_bangquydoi}.
 */
public interface BangQuyDoiRepository extends JpaRepository<BangQuyDoi, Integer> {
    Optional<BangQuyDoi> findByPhuongThucAndToHopAndMonAndMaQuyDoi(
            String phuongThuc,
            String toHop,
            String mon,
            String maQuyDoi);

    // 🔍 NGOAINGU → tìm theo môn/chứng chỉ
    List<BangQuyDoi> findByPhuongThucAndMonContainingIgnoreCase(
            String phuongThuc,
            String mon);

    // 🔍 VSAT, DGNL → tìm theo tổ hợp
    List<BangQuyDoi> findByPhuongThucAndToHopContainingIgnoreCase(
            String phuongThuc,
            String toHop);
    // Tìm 
    List<BangQuyDoi> findByPhuongThuc(String phuongThuc);
}
