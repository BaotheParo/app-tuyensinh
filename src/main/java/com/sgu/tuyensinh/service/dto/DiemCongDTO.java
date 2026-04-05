package com.sgu.tuyensinh.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DiemCongDTO {

    private String tsCccd;
    private String manganh;
    private String matohop;
    private String phuongthuc;
    private Double diemCC;
    private Double diemUtxt;
    private Double diemTong;
    private String ngayCap;
}
