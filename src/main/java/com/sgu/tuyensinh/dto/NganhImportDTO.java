package com.sgu.tuyensinh.dto;
import java.math.BigDecimal;

import lombok.Data;

@Data
public class NganhImportDTO {
    private String maNganh, tenNganh, toHopGoc;
    private BigDecimal  diemSan;    
    private Integer chiTieu;
}
