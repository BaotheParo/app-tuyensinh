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
 * Entity Tổ hợp môn (map với bảng {@code xt_tohop_monthi}).
 *
 * Tổ hợp được lưu theo dạng 3 môn (mon1, mon2, mon3) + tên hiển thị.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "xt_tohop_monthi")
public class ToHop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idtohop", nullable = false)
    private Integer idtohop;

    /**
     * Mã tổ hợp môn (ví dụ: A00, A01...).
     */
    @Column(name = "matohop", nullable = false, length = 45, unique = true)
    private String maToHop;

    /**
     * Môn thứ 1 của tổ hợp.
     */
    @Column(name = "mon1", nullable = false, length = 10)
    private String mon1;

    /**
     * Môn thứ 2 của tổ hợp.
     */
    @Column(name = "mon2", nullable = false, length = 10)
    private String mon2;

    /**
     * Môn thứ 3 của tổ hợp.
     */
    @Column(name = "mon3", nullable = false, length = 10)
    private String mon3;

    /**
     * Tên tổ hợp hiển thị (có thể null).
     */
    @Column(name = "tentohop", length = 100)
    private String tenToHop;
}

