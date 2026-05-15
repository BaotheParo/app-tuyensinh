package com.sgu.tuyensinh.repository;

import com.sgu.tuyensinh.entity.NguyenVong;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface NguyenVongRepository extends JpaRepository<NguyenVong, Integer> {

    boolean existsByNvManganh(String nvManganh);

    boolean existsByTtThm(String nvMaToHop);

    Page<NguyenVong> findByNnCccdContainingIgnoreCaseOrNvManganhContainingIgnoreCase(
            String nnCccd, String nvManganh, Pageable pageable);

    List<NguyenVong> findByNvManganhAndNvKetQua(String nvManganh, String nvKetQua);
    
    long countByNvKetQua(String nvKetQua);

    @Query("SELECT n.nvManganh, COUNT(n) FROM NguyenVong n GROUP BY n.nvManganh")
    List<Object[]> countByMaNganh();

    java.util.Optional<NguyenVong> findByNnCccdAndNvManganh(String nnCccd, String nvManganh);

    @Query("SELECT n FROM NguyenVong n WHERE (:status IS NULL OR :status = '' OR n.nvKetQua = :status) AND (:keyword IS NULL OR :keyword = '' OR n.nnCccd LIKE CONCAT('%', :keyword, '%') OR n.nvManganh LIKE CONCAT('%', :keyword, '%'))")
    Page<NguyenVong> searchWithFilter(@Param("keyword") String keyword, @Param("status") String status, Pageable pageable);

    @Query("SELECT COUNT(n) FROM NguyenVong n WHERE n.nvManganh = :maNganh AND n.nvKetQua = :ketQua")
    long countByMaNganhAndKetQua(@Param("maNganh") String maNganh, @Param("ketQua") String ketQua);

    @Query("SELECT COUNT(n) FROM NguyenVong n WHERE n.nvManganh = :maNganh AND n.ttPhuongthuc = :phuongThuc")
    long countByMaNganhAndPhuongThuc(@Param("maNganh") String maNganh, @Param("phuongThuc") String phuongThuc);

    List<NguyenVong> findTop100ByNvKetQuaOrderByDiemXetTuyenDesc(String nvKetQua);

    List<NguyenVong> findTop100ByNvManganhAndNvKetQuaOrderByDiemXetTuyenDesc(String nvManganh, String nvKetQua);

    @Query("SELECT n.nvManganh, n.ttPhuongthuc, COUNT(n) FROM NguyenVong n WHERE n.nvKetQua = :ketQua GROUP BY n.nvManganh, n.ttPhuongthuc")
    List<Object[]> countPhuongThucByMaNganhAndKetQua(@Param("ketQua") String ketQua);

    @Query("SELECT n.ttPhuongthuc, COUNT(n) FROM NguyenVong n WHERE n.nvManganh = :maNganh AND n.nvKetQua = 'TRUNG_TUYEN' GROUP BY n.ttPhuongthuc")
    List<Object[]> countByPhuongThucForNganh(@Param("maNganh") String maNganh);
}