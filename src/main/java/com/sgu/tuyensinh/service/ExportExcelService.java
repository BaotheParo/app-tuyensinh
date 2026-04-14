package com.sgu.tuyensinh.service;

import com.sgu.tuyensinh.entity.NguyenVong;
import com.sgu.tuyensinh.entity.ThiSinh;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExportExcelService {

    private final NguyenVongService nguyenVongService;

    @Transactional(readOnly = true)
    public void exportTrungTuyenTheoNganh(String maNganh, String filePath) throws IOException {

        List<NguyenVong> danhSach = nguyenVongService.getDanhSachTrungTuyen(maNganh);
        System.out.println("👉 So luong NV: " + danhSach.size());

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Trúng tuyển");

            CellStyle titleStyle  = buildTitleStyle(workbook);
            CellStyle headerStyle = buildHeaderStyle(workbook);
            CellStyle dataStyle   = buildDataStyle(workbook, false);
            CellStyle dataAlt     = buildDataStyle(workbook, true);

            // ===== TIÊU ĐỀ =====
            Row titleRow = sheet.createRow(0);
            titleRow.setHeightInPoints(28);

            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("DANH SÁCH THÍ SINH TRÚNG TUYỂN - NGÀNH: " + maNganh);
            titleCell.setCellStyle(titleStyle);

            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 9));

            // ===== HEADER =====
            String[] headers = {
                "STT", "Số CCCD", "Họ và tên", "Ngày sinh", "Giới tính",
                "Phương thức", "Tổ hợp", "Điểm THXT", "Điểm cộng", "Điểm xét tuyển"
            };

            Row headerRow = sheet.createRow(1);
            headerRow.setHeightInPoints(22);

            for (int i = 0; i < headers.length; i++) {
                Cell c = headerRow.createCell(i);
                c.setCellValue(headers[i]);
                c.setCellStyle(headerStyle);
            }

            // ===== DATA =====
            int rowNum = 2;
            int stt = 1;

            for (NguyenVong nv : danhSach) {

                // ❗ LỌC DỮ LIỆU RỖNG
                if (nv.getDiemXetTuyen() == null) {
                    continue;
                }

                Row row = sheet.createRow(rowNum);
                CellStyle style = (rowNum % 2 == 0) ? dataStyle : dataAlt;

                ThiSinh ts = nv.getThiSinh();

                setStr(row, 0, String.valueOf(stt++), style);
                setStr(row, 1, nv.getNnCccd(), style);
                setStr(row, 2, ts != null ? ts.getHoTen() : "", style);
                setStr(row, 3, ts != null && ts.getNgaySinh() != null
                        ? ts.getNgaySinh().toString() : "", style);
                setStr(row, 4, ts != null ? ts.getGioiTinh() : "", style);
                setStr(row, 5, nv.getTtPhuongthuc(), style);
                setStr(row, 6, nv.getTtThm(), style);
                setNum(row, 7, nv.getDiemThxt(), style);
                setNum(row, 8, nv.getDiemCong(), style);
                setNum(row, 9, nv.getDiemXetTuyen(), style);

                rowNum++;
            }

            // ===== AUTO SIZE =====
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 800);
            }

            // ===== GHI FILE =====
            try (FileOutputStream fos = new FileOutputStream(new File(filePath))) {
                workbook.write(fos);
            }
        }
    }

    // =========================================================
    // STYLE
    // =========================================================

    private CellStyle buildTitleStyle(Workbook wb) {
        Font f = wb.createFont();
        f.setBold(true);
        f.setFontHeightInPoints((short) 14);
        f.setFontName("Arial");

        CellStyle s = wb.createCellStyle();
        s.setFont(f);
        s.setAlignment(HorizontalAlignment.CENTER);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        return s;
    }

    private CellStyle buildHeaderStyle(Workbook wb) {
        Font f = wb.createFont();
        f.setBold(true);
        f.setFontHeightInPoints((short) 11);
        f.setFontName("Arial");
        f.setColor(IndexedColors.WHITE.getIndex());

        CellStyle s = wb.createCellStyle();
        s.setFont(f);
        s.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setAlignment(HorizontalAlignment.CENTER);
        s.setVerticalAlignment(VerticalAlignment.CENTER);

        setBorder(s);
        return s;
    }

    private CellStyle buildDataStyle(Workbook wb, boolean alternate) {
        Font f = wb.createFont();
        f.setFontName("Arial");
        f.setFontHeightInPoints((short) 11);

        CellStyle s = wb.createCellStyle();
        s.setFont(f);

        if (alternate) {
            s.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
            s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }

        setBorder(s);
        return s;
    }

    private void setBorder(CellStyle s) {
        s.setBorderTop(BorderStyle.THIN);
        s.setBorderBottom(BorderStyle.THIN);
        s.setBorderLeft(BorderStyle.THIN);
        s.setBorderRight(BorderStyle.THIN);
    }

    // =========================================================
    // SET CELL
    // =========================================================

    private void setStr(Row row, int col, String val, CellStyle style) {
        Cell c = row.createCell(col);
        c.setCellValue(val != null ? val : "");
        c.setCellStyle(style);
    }

    private void setNum(Row row, int col, Double val, CellStyle style) {
        Cell c = row.createCell(col);

        if (val != null) {
            c.setCellValue(val);
        } else {
            c.setBlank(); // ❗ KHÔNG ghi 0 giả
        }

        c.setCellStyle(style);
    }
}