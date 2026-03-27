package com.sgu.tuyensinh.util;

import com.sgu.tuyensinh.dto.NguyenVongImportDTO;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class NguyenVongExcelReaderUtil {

    public static List<NguyenVongImportDTO> read(File file) {
        List<NguyenVongImportDTO> list = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(file);
                Workbook workbook = WorkbookFactory.create(fis)) {

            // Duyệt qua tất cả các sheet trong file
            for (int s = 0; s < workbook.getNumberOfSheets(); s++) {
                Sheet sheet = workbook.getSheetAt(s);

                if (sheet == null)
                    continue;

                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row == null)
                        continue;

                    NguyenVongImportDTO dto = new NguyenVongImportDTO();

                    dto.setCccd(ExcelReaderUtil.getSafeString(row.getCell(1)));
                    dto.setThuTu(ExcelReaderUtil.getSafeInteger(row.getCell(2)));
                    dto.setMaNganh(ExcelReaderUtil.getSafeString(row.getCell(5)));

                    list.add(dto);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
}