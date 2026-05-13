package com.sgu.tuyensinh.dto;
import lombok.Data;

@Data
public class ToHopMonImportDTO {
    private String maToHop, mon1, mon2, mon3, tenToHop;
    private Double hs1, hs2, hs3;
    private String tbKeys;
    private Double doLech;

    /** MANGANH (cột 1 trong tohopmon.xlsx) - để update toHopGoc cho Nganh */
    private String maNganh;
    /** Giá trị cột 'Gốc' (cột 6) - "Gốc" nếu là tổ hợp gốc, null nếu không */
    private String goc;
}
