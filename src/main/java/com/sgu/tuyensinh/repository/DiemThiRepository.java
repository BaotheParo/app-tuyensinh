package com.sgu.tuyensinh.repository;

import com.sgu.tuyensinh.entity.DiemThi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

import java.util.Optional;

/**
 * Repository cho bảng điểm thi.
 */
public interface DiemThiRepository extends JpaRepository<DiemThi, Long> {

    Optional<DiemThi> findByCccd(String cccd);



    // Lấy tất cả điểm Toán
    @Query("SELECT d.toan FROM DiemThi d WHERE d.toan IS NOT NULL")
    List<Double> findAllDiemToan();

    // Lấy tất cả điểm Văn
    @Query("SELECT d.van FROM DiemThi d WHERE d.van IS NOT NULL")
    List<Double> findAllDiemVan();

    // Lấy tất cả điểm Lý
    @Query("SELECT d.ly FROM DiemThi d WHERE d.ly IS NOT NULL")
    List<Double> findAllDiemLy();

    // Lấy tất cả điểm Hóa
    @Query("SELECT d.hoa FROM DiemThi d WHERE d.hoa IS NOT NULL")
    List<Double> findAllDiemHoa();

    // Lấy tất cả điểm Sinh
    @Query("SELECT d.sinh FROM DiemThi d WHERE d.sinh IS NOT NULL")
    List<Double> findAllDiemSinh();

    // Lấy tất cả điểm Sử
    @Query("SELECT d.su FROM DiemThi d WHERE d.su IS NOT NULL")
    List<Double> findAllDiemSu();

    // Lấy tất cả điểm Địa
    @Query("SELECT d.dia FROM DiemThi d WHERE d.dia IS NOT NULL")
    List<Double> findAllDiemDia();

    // Lấy tất cả điểm Anh
    @Query("SELECT d.anh FROM DiemThi d WHERE d.anh IS NOT NULL")
    List<Double> findAllDiemAnh();

    // Các cột năng khiếu
    @Query("SELECT d.nk1 FROM DiemThi d WHERE d.nk1 IS NOT NULL")
    List<Double> findAllDiemNk1();

    @Query("SELECT d.nk2 FROM DiemThi d WHERE d.nk2 IS NOT NULL")
    List<Double> findAllDiemNk2();

    @Query("SELECT d.nk3 FROM DiemThi d WHERE d.nk3 IS NOT NULL")
    List<Double> findAllDiemNk3();

    @Query("SELECT d.nk4 FROM DiemThi d WHERE d.nk4 IS NOT NULL")
    List<Double> findAllDiemNk4();

    @Query("SELECT d.nk5 FROM DiemThi d WHERE d.nk5 IS NOT NULL")
    List<Double> findAllDiemNk5();

    @Query("SELECT d.nk6 FROM DiemThi d WHERE d.nk6 IS NOT NULL")
    List<Double> findAllDiemNk6();

    @Query("SELECT d.nk7 FROM DiemThi d WHERE d.nk7 IS NOT NULL")
    List<Double> findAllDiemNk7();

    @Query("SELECT d.nk8 FROM DiemThi d WHERE d.nk8 IS NOT NULL")
    List<Double> findAllDiemNk8();


}

