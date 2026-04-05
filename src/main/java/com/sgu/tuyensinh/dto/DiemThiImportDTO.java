package com.sgu.tuyensinh.dto;

import lombok.Data;

/**
 * DTO mẫu để đọc điểm thi (THPT, V-SAT, ĐGNL) từ file Excel.
 * - Mọi điểm nên dùng Object Double (viết hoa chữ D) thay vì double nguyên thủy.
 * - Lý do: Nếu giá trị cột là khoảng trống (không thi), biến sẽ nhận kết quả `null` (khác với điểm liệt `0.0`).
 */
@Data
public class DiemThiImportDTO {
    private String cccd;
    
    // Khối tự nhiên / xã hội cơ bản
    private Double toan;
    private Double van;
    private Double ly;
    private Double hoa;
    private Double sinh;
    private Double su;
    private Double dia;
    private Double anh;
    
    // Năng khiếu
    private Double nk1;
    private Double nk2;
    private Double nk3;
    private Double nk4;
    private Double nk5;
    private Double nk6;
    private Double nk7;
    private Double nk8;
}
