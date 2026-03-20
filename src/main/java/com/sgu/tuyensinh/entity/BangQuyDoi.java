package com.sgu.tuyensinh.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
@Table(name = "xt_bangquydoi")
public class BangQuyDoi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idqd", nullable = false)
    private Integer idqd;

    /**
     * Phương thức: ví dụ ĐGNL, VSAT, NGOAINGU.
     */
    @Column(name = "d_phuongthuc", length = 45)
    private String phuongThuc;

    /**
     * Năm học dùng để version bảng quy đổi (PRD có yêu cầu thêm cột này).
     */
    @Column(name = "nam_hoc")
    private Integer namHoc;

    /**
     * Mã tổ hợp (nếu bảng quy đổi có tách theo tổ hợp; có thể null).
     */
    @Column(name = "d_tohop", length = 45)
    private String dToHop;

    /**
     * Môn thi hoặc loại chứng chỉ (ví dụ: IELTS, TOEFL ITP, VSAT..., hoặc mã môn).
     */
    @Column(name = "d_mon", length = 45)
    private String mon;

    /**
     * Cận dưới điểm gốc.
     */
    @Column(name = "d_diema", precision = 6, scale = 2)
    private Double diemGocA;

    /**
     * Cận trên điểm gốc.
     */
    @Column(name = "d_diemb", precision = 6, scale = 2)
    private Double diemGocB;

    /**
     * Cận dưới điểm quy đổi tương ứng.
     */
    @Column(name = "d_diemc", precision = 6, scale = 2)
    private Double diemQuyDoiC;

    /**
     * Cận trên điểm quy đổi tương ứng.
     */
    @Column(name = "d_diemd", precision = 6, scale = 2)
    private Double diemQuyDoiD;

    /**
     * Mã bảng quy đổi (duy nhất trong dữ liệu import).
     */
    @Column(name = "d_maquydoi", length = 45)
    private String maQuyDoi;

    /**
     * Phân vị/nhãn khoảng điểm (phục vụ import/diagnostics, tùy dataset).
     */
    @Column(name = "d_phanvi", length = 45)
    private String phanVi;
}

