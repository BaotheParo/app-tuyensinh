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

        @Query(value = "SELECT t FROM ThiSinh t LEFT JOIN FETCH t.diemThi", countQuery = "SELECT count(t) FROM ThiSinh t")
        Page<ThiSinh> findAllWithDiemThi(Pageable pageable);

        // không dùng ContainingIgnoreCase  vì chỉ cần truyền 1 tham số keyword
        @Query(value = "SELECT t FROM ThiSinh t " +
                        "WHERE LOWER(t.id) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                        "   OR LOWER(t.hoTen) LIKE LOWER(CONCAT('%', :keyword, '%'))", countQuery = "SELECT COUNT(t) FROM ThiSinh t "
                                        +
                                        "WHERE LOWER(t.id) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                                        "   OR LOWER(t.hoTen) LIKE LOWER(CONCAT('%', :keyword, '%'))")
        Page<ThiSinh> searchThiSinh(@Param("keyword") String keyword,
                        Pageable pageable);


        // dùng ContainingIgnoreCase , truỳen 2 tham số để tìm kiếm theo cả CCCD và Họ tên
        Page<ThiSinh> findByIdContainingIgnoreCaseOrHoTenContainingIgnoreCase(
        String id,
        String hoTen,
        Pageable pageable
);
}
