package com.sgu.tuyensinh.service.interfaces;

import com.sgu.tuyensinh.service.dto.ImportResultDTO;
import java.io.InputStream;

public interface IImportService {

    /**
     * Import dữ liệu từ file Excel với callback tiến trình.
     *
     * @param inputStream luồng đọc file .xlsx / .xls
     * @param callback    callback cập nhật progress bar (có thể null)
     * @return ImportResultDTO chứa successCount, skipCount, danh sách lỗi
     */
    ImportResultDTO importFromExcel(InputStream inputStream, ProgressCallback callback);
}