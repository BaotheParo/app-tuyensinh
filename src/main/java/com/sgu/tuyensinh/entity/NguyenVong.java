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
 * Entity Nguyện vọng (map với bảng {@code xt_nguyenvongxettuyen}).
 *
 * Ghi chú cho nhóm:
 * - {@code nv_tt} là "thứ tự nguyện vọng" của thí sinh (1,2,3,...).
 * - {@code nv_manganh} là "mã ngành" mà nguyện vọng đăng ký.
 * - {@code nv_ketqua} là "kết quả" (ví dụ: TRUNG_TUYEN / TRUOT / ERROR).
 * - Có {@code @ManyToOne} sang {@link ThiSinh} thông qua khóa {@code nn_cccd} (cccd của thí sinh).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "xt_nguyenvongxettuyen")
public class NguyenVong {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idnv", nullable = false)
    private Integer idnv;

    /**
     * CCCD của thí sinh (dùng để join với {@link ThiSinh}).
     */
    @Column(name = "nn_cccd", nullable = false, length = 45)
    private String nnCccd;

    /**
     * Mã ngành thí sinh đăng ký ở nguyện vọng này.
     */
    @Column(name = "nv_manganh", nullable = false, length = 45)
    private String nvManganh;

    /**
     * Thứ tự nguyện vọng của thí sinh (càng nhỏ càng ưu tiên hơn khi tie-break).
     */
    @Column(name = "nv_tt", nullable = false)
    private Integer nvTt;

    /**
     * Kết quả nguyện vọng.
     * - TRUNG_TUYEN: đậu
     * - TRUOT: rớt
     * - ERROR: lỗi dữ liệu/thuật toán ở bước tính điểm
     */
    @Column(name = "nv_ketqua", length = 45)
    private String nvKetQua;

    /**
     * Khóa tổ hợp duy nhất (do import tạo).
     */
    @Column(name = "nv_keys", length = 45)
    private String nvKeys;

    /**
     * Phương thức xét tuyển được chọn khi trúng (PT1/PT2/PT3/PT4 hoặc giá trị khác theo file import).
     */
    @Column(name = "tt_phuongthuc", length = 45)
    private String ttPhuongthuc;

    /**
     * Tổ hợp môn (tohop) được chọn khi trúng (trường hợp cần tie-break cấp 2/3).
     */
    @Column(name = "tt_thm", length = 45)
    private String ttThm;

    // ====== Các cột điểm trong bảng (để lưu đầy đủ dữ liệu theo DB schema) ======
    @Column(name = "diem_thxt", precision = 10, scale = 5)
    private Double diemThxt;

    @Column(name = "diem_utqd", precision = 10, scale = 5)
    private Double diemUtqd;

    @Column(name = "diem_cong", precision = 6, scale = 2)
    private Double diemCong;

    @Column(name = "diem_xettuyen", precision = 10, scale = 5)
    private Double diemXetTuyen;

    /**
     * Quan hệ tới thí sinh.
     *
     * Chú ý:
     * - Cột nối là {@code nn_cccd} (đã có trong entity bằng {@code nnCccd}).
     * - Vì vậy {@code insertable=false, updatable=false} để tránh ghi 2 nơi cùng 1 cột.
     */
    @lombok.ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "nn_cccd", referencedColumnName = "cccd", insertable = false, updatable = false)
    private ThiSinh thiSinh;
}

