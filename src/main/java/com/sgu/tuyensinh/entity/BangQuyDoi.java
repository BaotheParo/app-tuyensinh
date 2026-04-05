package com.sgu.tuyensinh.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity bảng quy đổi (map với bảng {@code xt_bangquydoi}).
 *
 * Bảng này phục vụ việc nội suy điểm theo PRD:
 * - {@code d_phuongthuc}: phương thức (ĐGNL/VSAT/NGOAINGU)
 * - {@code nam_hoc}: năm học (ví dụ 2026)
 * - {@code d_mon}: môn thi hoặc loại chứng chỉ
 * - Các cặp {@code d_diema/d_diemb} và {@code d_diemc/d_diemd} là khoảng điểm:
 *   - {@code d_diema/d_diemb}: điểm gốc (thang đầu vào)
 *   - {@code d_diemc/d_diemd}: điểm quy đổi (thang THPT)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "xt_bangquydoi",
    uniqueConstraints = @UniqueConstraint(
        columnNames = {"d_phuongthuc", "d_tohop", "d_mon", "d_maquydoi"}
    )
)
public class BangQuyDoi {

    // HOTFIX: Sử dụng Integer (Object) để Hibernate nhận diện null là record mới, 
    // và dùng @GeneratedValue để MySQL tự động tăng ID (BE-3.2 Vinh fix)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idqd", nullable = false)
    private Integer idqd;

    /**
     * Phương thức: ví dụ ĐGNL, VSAT, NGOAINGU.
     */
    @Column(name = "d_phuongthuc", length = 255)
    private String phuongThuc;

    /**
     * Năm học dùng để version bảng quy đổi (PRD có yêu cầu thêm cột này).
     */
    @Column(name = "nam_hoc")
    private Integer namHoc;

    /**
     * Mã tổ hợp (nếu bảng quy đổi có tách theo tổ hợp; có thể null).
     */
    @Column(name = "d_tohop", length = 255)
    private String toHop;

    /**
     * Môn thi hoặc loại chứng chỉ (ví dụ: IELTS, TOEFL ITP, VSAT..., hoặc mã môn).
     */
    @Column(name = "d_mon", length = 255)
    private String mon;

    /**
     * Cận dưới điểm gốc.
     */
    @Column(name = "d_diema")
    private Double diemGocA;

    /**
     * Cận trên điểm gốc.
     */
    @Column(name = "d_diemb")
    private Double diemGocB;

    /**
     * Cận dưới điểm quy đổi tương ứng.
     */
    @Column(name = "d_diemc")
    private Double diemQuyDoiC;

    /**
     * Cận trên điểm quy đổi tương ứng.
     */
    @Column(name = "d_diemd")
    private Double diemQuyDoiD;

    /**
     * Mã bảng quy đổi (duy nhất trong dữ liệu import).
     */
    @Column(name = "d_maquydoi", length = 255)
    private String maQuyDoi;

    /**
     * Phân vị/nhãn khoảng điểm (phục vụ import/diagnostics, tùy dataset).
     */
    @Column(name = "d_phanvi", length = 255)
    private String phanVi;

    
}

