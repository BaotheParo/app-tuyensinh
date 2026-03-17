package com.sgu.tuyensinh.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Thí sinh (map với thông tin định danh và ưu tiên).
 *
 * Ghi chú cho nhóm:
 * - `id` chính là số CCCD (chuỗi), dùng làm khóa chính để đồng bộ theo dữ liệu gốc.
 * - Các field ưu tiên (đối tượng/khu vực) thường là mã (string) theo quy định.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "thi_sinh")
public class ThiSinh {

    /**
     * CCCD - khóa chính.
     */
    @Id
    @Column(name = "cccd", nullable = false, length = 20)
    private String id;

    @Column(name = "ho_ten", nullable = false, length = 255)
    private String hoTen;

    @Column(name = "ngay_sinh")
    private LocalDate ngaySinh;

    @Column(name = "gioi_tinh", length = 20)
    private String gioiTinh;

    @Column(name = "ma_truong", length = 50)
    private String maTruong;

    @Column(name = "ma_tinh", length = 50)
    private String maTinh;

    @Column(name = "doi_tuong_ut", length = 50)
    private String doiTuongUt;

    @Column(name = "khu_vuc_ut", length = 50)
    private String khuVucUt;
}

