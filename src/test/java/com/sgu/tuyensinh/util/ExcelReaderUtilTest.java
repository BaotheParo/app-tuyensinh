package com.sgu.tuyensinh.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExcelReaderUtilTest {

    @Test
    void testGetSafeString_FixScientificNotation() {
        // Giả lập thư viện POI đọc được giá trị khoa học (E mũ)
        Cell mockCell = Mockito.mock(Cell.class);
        Mockito.when(mockCell.getCellType()).thenReturn(CellType.NUMERIC);
        // Nhập CCCD dài bằng số trên Excel sẽ bị biến thành ví dụ: 7.9123456789E10
        Mockito.when(mockCell.getNumericCellValue()).thenReturn(79123456789.0);

        // Action
        String result = ExcelReaderUtil.getSafeString(mockCell);

        // Kiểm định: Chuỗi không được dính E+10 hoặc .0 ở đuôi
        assertEquals("79123456789", result, "Phải ép kiểu số thập phân thành mã CCCD chuẩn xác, không dính lỗi số khoa học.");
    }
}
