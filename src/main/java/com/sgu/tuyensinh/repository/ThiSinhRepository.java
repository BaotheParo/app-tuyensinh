package com.sgu.tuyensinh.repository;

import com.sgu.tuyensinh.entity.ThiSinh;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository cho bảng thí sinh.
 */
public interface ThiSinhRepository extends JpaRepository<ThiSinh, String> {

        // 1. Hàm của team (Tối ưu N+1 Query bằng EntityGraph thay vì JOIN FETCH thủ công)
        @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"diemThi"})
        @Query(value = "SELECT t FROM ThiSinh t", countQuery = "SELECT count(t) FROM ThiSinh t")
        Page<ThiSinh> findAllWithDiemThi(Pageable pageable);

        // 2. Hàm của team (Tìm kiếm gom 1 tham số keyword)
        @Query(value = "SELECT t FROM ThiSinh t " +
                        "WHERE LOWER(t.id) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                        "   OR LOWER(t.hoTen) LIKE LOWER(CONCAT('%', :keyword, '%'))", countQuery = "SELECT COUNT(t) FROM ThiSinh t "
                                        +
                                        "WHERE LOWER(t.id) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                                        "   OR LOWER(t.hoTen) LIKE LOWER(CONCAT('%', :keyword, '%'))")
        Page<ThiSinh> searchThiSinh(@Param("keyword") String keyword, Pageable pageable);

        // 3. BỔ SUNG: Hàm tìm kiếm 2 tham số (Phục vụ cho ThiSinhPanel của Boa)
    Page<ThiSinh> findByIdContainingIgnoreCaseOrHoTenContainingIgnoreCase(String id, String hoTen,
                    Pageable pageable);

    @Query("SELECT COUNT(t) FROM ThiSinh t")
    long countTotal();

    @Query("SELECT t.doiTuongUt, COUNT(t) FROM ThiSinh t GROUP BY t.doiTuongUt")
    java.util.List<Object[]> countByDoiTuong();

    @Query("SELECT t.khuVucUt, COUNT(t) FROM ThiSinh t GROUP BY t.khuVucUt")
    java.util.List<Object[]> countByKhuVuc();
}