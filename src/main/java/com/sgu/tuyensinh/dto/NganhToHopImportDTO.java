package com.sgu.tuyensinh.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NganhToHopImportDTO {
    private String maNganh, tenNganh, maToHop, tbKeys, tenToHop, goc;
    private BigDecimal doLech;
}