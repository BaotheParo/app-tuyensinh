package com.sgu.tuyensinh.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;

import java.math.BigDecimal;

/**
 * Tiện ích đọc dữ liệu an toàn từ các ô (Cell) của file Excel.
 * Dành cho sinh viên năm 3: Class này giúp các em lấy dữ liệu mà không sợ bị lỗi vặt như:
 * - Số CCCD bị biến thành số khoa học (VD: 079... -> 7.9E10).
 * - Số nguyên bị dính số thập phân (VD: 1 -> 1.0).
 * - Lỗi NullPointerException khi ô trống.
 */
public class ExcelReaderUtil {

    /**
     * Lấy giá trị chuỗi an toàn từ Cell.
     * Đặc biệt quan trọng với các định danh như CCCD (tránh bị lỗi 1.23E11).
     *
     * @param cell Ô excel cần đọc
     * @return Chuỗi đã được parse cẩn thận, trim() 2 đầu. Trả về null nếu ô hoàn toàn trống.
     */
    public static String getSafeString(Cell cell) {
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return null;
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    // Tránh số khoa học và loại bỏ .0 ở cuối
                    double value = cell.getNumericCellValue();
                    long longValue = (long) value;
                    if (value == longValue) {
                        return String.valueOf(longValue);
                    } else {
                        // Tránh format kiểu exponent (khoa học) nếu số thập phân quá dài
                        return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue().trim();
                } catch (IllegalStateException e) {
                    return String.valueOf(cell.getNumericCellValue());
                }
            default:
                return null;
        }
    }

    /**
     * Lấy giá trị Double an toàn từ Cell. Dùng cho điểm thi.
     * TUYỆT ĐỐI KHÔNG trả về 0.0 nếu ô trống (vì điểm 0.0 là rớt).
     *
     * @param cell Ô excel
     * @return Double hoặc null nếu thí sinh không thi môn đó/để trống.
     */
    public static Double getSafeDouble(Cell cell) {
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return null;
        }

        switch (cell.getCellType()) {
            case NUMERIC:
                return cell.getNumericCellValue();
            case STRING:
                String text = cell.getStringCellValue().trim();
                if (text.isEmpty()) {
                    return null;
                }
                try {
                    return Double.parseDouble(text.replace(',', '.')); // Xử lý nếu file Excel xuất kiểu dấu phẩy
                } catch (NumberFormatException e) {
                    return null; // Dữ liệu sai định dạng chữ
                }
            case FORMULA:
                try {
                    return cell.getNumericCellValue();
                } catch (IllegalStateException e) {
                    return null;
                }
            default:
                return null;
        }
    }

    /**
     * Lấy giá trị Integer an toàn từ Cell.
     *
     * @param cell Ô excel
     * @return Integer hoặc null nếu rỗng.
     */
    public static Integer getSafeInteger(Cell cell) {
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return null;
        }

        switch (cell.getCellType()) {
            case NUMERIC:
                return (int) Math.round(cell.getNumericCellValue());
            case STRING:
                String text = cell.getStringCellValue().trim();
                if (text.isEmpty()) {
                    return null;
                }
                try {
                    // Tránh lỗi parse nếu text string bị dính lố .0 (VD: "123.0")
                    if (text.endsWith(".0")) {
                        text = text.substring(0, text.length() - 2);
                    }
                    return Integer.parseInt(text);
                } catch (NumberFormatException e) {
                    return null;
                }
            case FORMULA:
                try {
                    return (int) Math.round(cell.getNumericCellValue());
                } catch (IllegalStateException e) {
                    return null;
                }
            default:
                return null;
        }
    }
}
