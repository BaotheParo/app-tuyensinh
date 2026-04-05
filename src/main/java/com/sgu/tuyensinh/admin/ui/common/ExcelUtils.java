package com.sgu.tuyensinh.admin.ui.common;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.usermodel.Sheet;
import java.io.File;
import java.io.FileInputStream;

// Để đọc tổng số dòng 
public class ExcelUtils {
    public static int countRows(String path) {
        try (FileInputStream fis = new FileInputStream(new File(path));
             Workbook workbook = WorkbookFactory.create(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            return sheet.getLastRowNum() + 1;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}