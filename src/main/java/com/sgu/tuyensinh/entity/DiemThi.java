package com.sgu.tuyensinh.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Điểm thi của thí sinh.
 *
 * Yêu cầu đặc biệt:
 * - Phải có các cột `nk1` đến `nk8` (Double) để map đúng bảng SQL gốc.
 *
 * Gợi ý cho nhóm:
 * - Các môn thi có thể thay đổi theo năm/khối, nên hiện tại mình tạo một tập môn phổ biến.
 * - Nếu SQL gốc có thêm cột môn khác, nhóm chỉ cần bổ sung field + @Column tương ứng.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "diem_thi")
public class DiemThi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * CCCD của thí sinh (để join với bảng thí sinh).
     * Lưu ý: field này không đặt @Id vì bảng điểm thi thường có id tự tăng.
     */
    @Column(name = "cccd", nullable = false, length = 20)
    private String cccd;

    @jakarta.persistence.ManyToOne(fetch = jakarta.persistence.FetchType.EAGER)
    @jakarta.persistence.JoinColumn(name = "cccd", referencedColumnName = "cccd", insertable = false, updatable = false)
    @lombok.ToString.Exclude
    private ThiSinh thiSinh;

    // ===== Điểm các môn thi (có thể bổ sung thêm tùy dữ liệu thực tế) =====
    @Column(name = "toan")
    private Double toan;

    @Column(name = "van")
    private Double van;

    @Column(name = "ly")
    private Double ly;

    @Column(name = "hoa")
    private Double hoa;

    @Column(name = "sinh")
    private Double sinh;

    @Column(name = "su")
    private Double su;

    @Column(name = "dia")
    private Double dia;

    @Column(name = "anh")
    private Double anh;

    // ===== Các cột năng khiếu theo bảng SQL gốc =====
    @Column(name = "nk1")
    private Double nk1;

    @Column(name = "nk2")
    private Double nk2;

    @Column(name = "nk3")
    private Double nk3;

    @Column(name = "nk4")
    private Double nk4;

    @Column(name = "nk5")
    private Double nk5;

    @Column(name = "nk6")
    private Double nk6;

    @Column(name = "nk7")
    private Double nk7;

    @Column(name = "nk8")
    private Double nk8;
}

