
package com.sgu.tuyensinh.service.dto;

public class RowErrorDTO {
    private final int    rowNumber;   // dòng bị lỗi trong file Excel
    private final String identifier;  // CCCD hoặc mã định danh
    private final String errorCode;   // VD: CCCD_DUPLICATE, SCORE_OUT_OF_RANGE
    private final String detail;      // Mô tả chi tiết lỗi

    public RowErrorDTO(int rowNumber, String identifier, String errorCode, String detail) {
        this.rowNumber  = rowNumber;
        this.identifier = identifier;
        this.errorCode  = errorCode;
        this.detail     = detail;
    }

    public int    getRowNumber()  { return rowNumber;  }
    public String getIdentifier() { return identifier; }
    public String getErrorCode()  { return errorCode;  }
    public String getDetail()     { return detail;     }
}