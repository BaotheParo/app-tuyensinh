package com.sgu.tuyensinh.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
 * Entity ánh xạ Ngành - Tổ hợp (map với bảng {@code xt_nganh_tohop}).
 *
 * Bảng này chứa:
 * - Tổ hợp môn của ngành (môn1/môn2/môn3 và hệ số tương ứng hsmon1/2/3).
 * - Các cờ (flags) môn thi như TO, LI, HO, SI, VA, DI, N1... để phục vụ logic "skip optimization"
 *   (bỏ qua tổ hợp khi thí sinh không có điểm môn bắt buộc).
 * - `dolech`: độ lệch khi quy đổi điểm tổ hợp về tổ hợp gốc của ngành.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "xt_nganh_tohop")
public class NganhToHop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    /**
     * Mã ngành.
     * Dùng để join tới {@link Nganh}.
     */
    @Column(name = "manganh", nullable = false, length = 45)
    private String maNganh;

    /**
     * Mã tổ hợp môn.
     * Dùng để join tới {@link ToHop}.
     */
    @Column(name = "matohop", nullable = false, length = 45)
    private String maToHop;

    // ===== Môn thành phần + hệ số môn (w1, w2, w3 trong PRD) =====

    /**
     * Môn thứ 1 của tổ hợp theo cấu hình ngành.
     */
    @Column(name = "th_mon1", length = 10)
    private String thMon1;

    /**
     * Hệ số môn thứ 1 (thường 1, môn nhân đôi thì = 2).
     * Dùng trong công thức ĐTHXT: w1 = hsmon1.
     */
    @Column(name = "hsmon1")
    private Double hsMon1;

    @Column(name = "th_mon2", length = 10)
    private String thMon2;

    /**
     * Hệ số môn thứ 2 (w2).
     */
    @Column(name = "hsmon2")
    private Double hsMon2;

    @Column(name = "th_mon3", length = 10)
    private String thMon3;

    /**
     * Hệ số môn thứ 3 (w3).
     */
    @Column(name = "hsmon3")
    private Double hsMon3;

    /**
     * Khóa duy nhất do import tạo ra (manganh_matohop).
     */
    @Column(name = "tb_keys", length = 45)
    private String tbKeys;

    // ===== CÁC CỜ MÔN THI (flags) =====
    /**
     * Ý nghĩa chung (theo PRD phần skip optimization):
     * - Flag = 1 nghĩa là tổ hợp yêu cầu môn tương ứng phải có điểm (điểm môn != 0).
     * - Flag = 0 nghĩa là môn đó không bắt buộc trong tổ hợp này.
     *
     * Các flag này được kiểm tra trong `isComboValid(...)`.
     *
     * Ngoại lệ quan trọng:
     * - Flag {@code N1} = 1 nghĩa là tổ hợp có môn Tiếng Anh, khi tính điểm sẽ dùng {@code N1_CC}
     *   thay cho điểm Tiếng Anh gốc (xem PRD phần 2.4).
     *
     * Ghi chú kiểu dữ liệu:
     * - Trong DB là tinyint (0/1). Mình vẫn map kiểu {@code Double} để phù hợp yêu cầu của bài và
     *   thuận tiện nếu sau này nhóm dùng giá trị không chỉ 0/1.
     */
    @Column(name = "`N1`")
    private Double n1 = 0.0;   // Mặc định 0.0 (không bắt buộc), nếu = 1.0 thì bắt buộc phải có điểm môn Tiếng Anh (xem PRD phần skip optimization)

    /** Flag môn TO (Toán). */
    @Column(name = "`TO`")
    private Double to = 0.0;   // Mặc định 0.0 (không bắt buộc), nếu = 1.0 thì bắt buộc phải có điểm môn Toán (xem PRD phần skip optimization) 

    /** Flag môn LI (Lý). */
    @Column(name = "`LI`")
    private Double li = 0.0;   // Mặc định 0.0 (không bắt buộc), nếu = 1.0 thì bắt buộc phải có điểm môn Lý (xem PRD phần skip optimization)

    /** Flag môn HO (Hóa). */
    @Column(name = "`HO`")
    private Double ho = 0.0;   // Mặc định 0.0 (không bắt buộc), nếu = 1.0 thì bắt buộc phải có điểm môn Hóa (xem PRD phần skip optimization)

    /** Flag môn SI (Sinh). */
    @Column(name = "`SI`")
    private Double si = 0.0;   // Mặc định 0.0 (không bắt buộc), nếu = 1.0 thì bắt buộc phải có điểm môn Sinh (xem PRD phần skip optimization)

    /** Flag môn VA (Văn). */
    @Column(name = "`VA`")
    private Double va = 0.0;   // Mặc định 0.0 (không bắt buộc), nếu = 1.0 thì bắt buộc phải có điểm môn Văn (xem PRD phần skip optimization)

    /** Flag môn SU (Sử). */
    @Column(name = "`SU`")
    private Double su = 0.0;   // Mặc định 0.0 (không bắt buộc), nếu = 1.0 thì bắt buộc phải có điểm môn Sử (xem PRD phần skip optimization)

    /** Flag môn DI (Địa). */
    @Column(name = "`DI`")
    private Double di = 0.0;   // Mặc định 0.0 (không bắt buộc), nếu = 1.0 thì bắt buộc phải có điểm môn Địa (xem PRD phần skip optimization)

    /** Flag môn TI (Tin). */
    @Column(name = "`TI`")
    private Double ti = 0.0;   // Mặc định 0.0 (không bắt buộc), nếu = 1.0 thì bắt buộc phải có điểm môn Tin (xem PRD phần skip optimization)

    /** Flag môn KHAC (môn khác theo cấu hình). */
    @Column(name = "`KHAC`")
    private Double khac = 0.0;   // Mặc định 0.0 (không bắt buộc), nếu = 1.0 thì bắt buộc phải có điểm môn khác (xem PRD phần skip optimization)

    /** Flag môn KTPL (môn khác/nhóm môn theo cấu hình). */
    @Column(name = "`KTPL`")
    private Double ktpl = 0.0;   // Mặc định 0.0 (không bắt buộc), nếu = 1.0 thì bắt buộc phải có điểm môn khác/nhóm môn (xem PRD phần skip optimization)

    /**
     * Độ lệch để quy đổi điểm tổ hợp đang xét về tổ hợp gốc của ngành.
     * PRD quy ước dấu:
     * - `dolech` âm -> trừ (âm) = cộng bù -> tăng điểm khi quy về gốc.
     * - `dolech` dương -> trừ (dương) -> giảm điểm khi quy về gốc.
     */
    @Column(name = "dolech")
    private Double doLech = 0.0;   // Mặc định 0.0 (không lệch), nếu < 0 thì khi quy về tổ hợp gốc sẽ cộng thêm điểm, nếu > 0 thì khi quy về tổ hợp gốc sẽ trừ đi điểm.

    // ===== Quan hệ =====

    /**
     * Quan hệ tới ngành.
     * - fetch LAZY để tối ưu truy vấn.
     * - JsonIgnore tránh vòng lặp infinite recursion khi serialize JSON.
     */
    @JsonIgnore
    @lombok.ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "manganh", referencedColumnName = "manganh", insertable = false, updatable = false)
    private Nganh nganH;

    /**
     * Quan hệ tới tổ hợp môn.
     * - fetch LAZY để tối ưu truy vấn.
     * - JsonIgnore tránh vòng lặp infinite recursion.
     * - Sử dụng ConstraintMode.NO_CONSTRAINT để tránh lỗi MySQL khi không có index trên cột matohop.
     */
    @JsonIgnore
    @lombok.ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "matohop", referencedColumnName = "matohop", insertable = false, updatable = false, 
                foreignKey = @jakarta.persistence.ForeignKey(jakarta.persistence.ConstraintMode.NO_CONSTRAINT))
    private ToHop toHop;
}

