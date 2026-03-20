package com.sgu.tuyensinh.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity Điểm cộng ưu tiên (map với bảng {@code xt_diemcongxetuyen}).
 *
 * Ghi chú cho nhóm:
 * - {@code matohop} là mã tổ hợp.
 * - {@code diemUtxt} là "điểm cộng ưu tiên" (theo cột trong DB schema).
 * - Có {@code @ManyToOne} sang {@link ThiSinh} thông qua {@code ts_cccd}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "xt_diemcongxetuyen")
public class DiemCong {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "iddiemcong", nullable = false)
    private Integer iddiemcong;

    /**
     * CCCD của thí sinh (dùng để join với {@link ThiSinh}).
     */
    @Column(name = "ts_cccd", nullable = false, length = 45)
    private String tsCccd;

    /**
     * Mã ngành của tổ hợp.
     */
    @Column(name = "manganh", length = 20)
    private String manganh;

    /**
     * Mã tổ hợp môn.
     */
    @Column(name = "matohop", length = 10)
    private String matohop;

    /**
     * Phương thức xét tuyển (PT1/PT2/PT3/PT4 hoặc giá trị theo file import).
     */
    @Column(name = "phuongthuc", length = 45)
    private String phuongthuc;

    /**
     * Điểm cộng từ chứng chỉ/nguồn dữ liệu (cột DB: diemCC).
     */
    @Column(name = "diemCC", precision = 6, scale = 2)
    private Double diemCC;

    /**
     * Điểm cộng ưu tiên (cột DB: diemUtxt).
     */
    @Column(name = "diemUtxt", precision = 6, scale = 2)
    private Double diemUtxt;

    /**
     * Tổng điểm cộng sau khi xử lý theo logic trong PRD (cột DB: diemTong).
     */
    @Column(name = "diemTong", precision = 6, scale = 2)
    private Double diemTong;

    /**
     * Ghi chú (nếu có).
     */
    @Column(name = "ghichu")
    private String ghichu;

    /**
     * Khóa duy nhất khi import tạo (dùng để chống trùng).
     */
    @Column(name = "dc_keys", nullable = false, length = 45)
    private String dcKeys;

    /**
     * Quan hệ tới thí sinh qua CCCD.
     *
     * Chú ý:
     * - Vì {@code tsCccd} đã map trực tiếp tới cột {@code ts_cccd}, ta dùng
     *   {@code insertable=false, updatable=false} để tránh Hibernate ghi 2 nơi.
     */
    @lombok.ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ts_cccd", referencedColumnName = "cccd", insertable = false, updatable = false)
    private ThiSinh thiSinh;
}

