package com.sgu.tuyensinh.entity;

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
 * - `id` là mã ngành, dùng làm khóa chính.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "nganh")
public class Nganh {

    @Id
    @Column(name = "ma_nganh", nullable = false, length = 50)
    private String id;

    @Column(name = "ten_nganh", nullable = false, length = 255)
    private String tenNganh;

    @Column(name = "chi_tieu")
    private Integer chiTieu;
}

