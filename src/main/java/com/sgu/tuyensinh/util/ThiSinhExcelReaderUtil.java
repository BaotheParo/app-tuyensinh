// package com.sgu.tuyensinh.util;

// import com.sgu.tuyensinh.dto.ThiSinhImportDTO;
// import org.apache.poi.ss.usermodel.*;

// import java.io.File;
// import java.io.FileInputStream;
// import java.util.ArrayList;
// import java.util.List;

// public class ThiSinhExcelReaderUtil {

//     public static List<ThiSinhImportDTO> read(File file) {
//         List<ThiSinhImportDTO> list = new ArrayList<>();

//         try (FileInputStream fis = new FileInputStream(file);
//                 Workbook workbook = WorkbookFactory.create(fis)) {

//             Sheet sheet = workbook.getSheetAt(0);

//             for (int i = 1; i <= sheet.getLastRowNum(); i++) {
//                 Row row = sheet.getRow(i);
//                 if (row == null)
//                     continue;

//                 ThiSinhImportDTO dto = new ThiSinhImportDTO();

//                 dto.setCccd(ExcelReaderUtil.getSafeString(row.getCell(1)));
//                 dto.setHoTen(ExcelReaderUtil.getSafeString(row.getCell(2)));
//                 dto.setNgaySinh(ExcelReaderUtil.getSafeString(row.getCell(3)));
//                 dto.setGioiTinh(ExcelReaderUtil.getSafeString(row.getCell(4)));
//                 // Mặc định 2 cột matruong và matinh là null vì file excel ko có
//                 //dto.setMaTruong(ExcelReaderUtil.getSafeString(row.getCell()));
//                 //dto.setMaTinh(ExcelReaderUtil.getSafeString(row.getCell(5)));
//                 dto.setDoiTuongUt(ExcelReaderUtil.getSafeString(row.getCell(5)));
//                 dto.setKhuVucUt(ExcelReaderUtil.getSafeString(row.getCell(6)));
//                 list.add(dto);
//             }

//         } catch (Exception e) {
//             e.printStackTrace();
//         }

//         return list;
//     }
// }
package com.sgu.tuyensinh.util;

import com.sgu.tuyensinh.dto.ThiSinhImportDTO;
import org.apache.poi.ss.usermodel.*;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ThiSinhExcelReaderUtil {

    public static List<ThiSinhImportDTO> read(InputStream is) {
        List<ThiSinhImportDTO> list = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null)
                    continue;

                ThiSinhImportDTO dto = new ThiSinhImportDTO();

                dto.setCccd(ExcelReaderUtil.getSafeString(row.getCell(1)));
                dto.setHoTen(ExcelReaderUtil.getSafeString(row.getCell(2)));
                dto.setNgaySinh(ExcelReaderUtil.getSafeString(row.getCell(3)));
                dto.setGioiTinh(ExcelReaderUtil.getSafeString(row.getCell(4)));

                // Mặc định null vì file không có
                // dto.setMaTruong(...)
                // dto.setMaTinh(...)
                dto.setDoiTuongUt(ExcelReaderUtil.getSafeString(row.getCell(5)));
                dto.setKhuVucUt(ExcelReaderUtil.getSafeString(row.getCell(6)));

                list.add(dto);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
}