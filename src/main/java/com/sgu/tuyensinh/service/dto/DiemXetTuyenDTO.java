package com.sgu.tuyensinh.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiemXetTuyenDTO {
    private Double diemThxt;
    private Double diemCong;
    private Double diemUtqd;
    private Double diemXetTuyen;
    private String ttThm;
    private String phuongThuc;
}
