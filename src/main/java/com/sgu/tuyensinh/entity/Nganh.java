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
    private String id;

    @Column(name = "tennganh", nullable = false, length = 100)
    private String tenNganh;

    @Column(name = "n_chitieu", nullable = false)
    private Integer chiTieu;
}

