package com.sgu.tuyensinh.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * Ngành tuyển sinh.
 *
 * Ghi chú:
 * - Theo schema DB, bảng `xt_nganh` dùng cột `manganh` là mã ngành.
 * - Mình map `id` (String) sang cột `manganh` để các quan hệ kiểu `ManyToOne`
 *   (ví dụ từ `xt_nganh_tohop`) có thể join theo đúng mã ngành.
 */



@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "xt_nganh")
public class Nganh {

    @Id
    @Column(name = "manganh", nullable = false, length = 45)
    private String maNganh;

    @Column(name = "tennganh", nullable = false, length = 100)
    private String tenNganh;

    @Column(name = "n_tohopgoc", length = 45)
    private String toHopGoc;

    @Column(name = "n_chitieu", nullable = false)
    private Integer chiTieu;

    @Column(name = "n_diemsan", precision = 10, scale = 2)
    private BigDecimal diemSan;

    @Column(name = "n_diemtrungtuyen", precision = 10, scale = 2)
    private BigDecimal diemTrungTuyen;

    @Column(name = "n_tuyenthang", length = 1)
    private String tuyenThang;

    @Column(name = "n_dgnl", length = 1)
    private String dgnl;

    @Column(name = "n_thpt", length = 1)
    private String thpt;

    @Column(name = "n_vsat", length = 1)
    private String vsat;

    @Column(name = "sl_xtt")
    private Integer slXtt;

    @Column(name = "sl_dgnl")
    private Integer slDgnl;

    @Column(name = "sl_vsat")
    private Integer slVsat;

    @Column(name = "sl_thpt", length = 45)
    private String slThpt;
}

