package com.sgu.tuyensinh.service.dto;

import com.sgu.tuyensinh.entity.DiemCong;
import com.sgu.tuyensinh.entity.DiemThi;
import com.sgu.tuyensinh.entity.ThiSinh;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * 
 * class DTO này dùng để trả về chi tiết thông tin của thí sinh, điểm thi và điểm cộng.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ThiSinhDetailDTO {

    private String cccd;
    private String hoTen;
    private String gioiTinh;
    private String maTruong;
    private String maTinh;
    private String doiTuongUt;
    private String khuVucUt;

    private DiemThiDTO diemThi;
    private List<DiemCongDTO> diemCongs;
}