package com.sgu.tuyensinh.dto;

import lombok.Data;

/**
 * DTO chứa dữ liệu Thí sinh đọc từ file Excel.
 * Dùng lombok @Data để lớp tự động sinh Getter/Setter.
 */
@Data
public class ThiSinhImportDTO {
    private String cccd;
    private String hoTen;
    
    // Ngày sinh có thể ưu tiên định dạng là String hoặc parse luôn LocalDate
    private String ngaySinh; 
    
    private String gioiTinh;
    private String maTruong;
    private String maTinh;
    private String doiTuongUt;
    private String khuVucUt;
}
