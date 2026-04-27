package com.sgu.tuyensinh.service.interfaces;

import com.sgu.tuyensinh.service.dto.ImportResultDTO;

import java.io.InputStream;

/**
 * Interface chung cho tất cả các service import Excel.
 *
 * Các class implement:
 *  - NganhImportService          (import Ngành)
 *  - ToHopImportService          (import Tổ hợp môn)
 *  - NganhToHopImportService     (import Bảng ngành–tổ hợp)
 *  - QuyDoiTiengAnhServiceImpl (import Bảng quy đổi ngoại ngữ)
 *
 * Flow chuẩn năm 3 (theo PRD v3.0):
 *   InputStream → Apache POI đọc toàn bộ
 *       → vòng for validate từng dòng, gom lỗi vào ImportResultDTO
 *       → saveAll(validList)
 *       → trả ImportResultDTO về UI → ErrorLogDialog.show(result.getErrors())
 */
public interface IImportService {

    /**
     * Import dữ liệu từ file Excel.
     *
     * @param inputStream luồng đọc file .xlsx / .xls
     * @return ImportResultDTO chứa successCount, skipCount, và danh sách lỗi từng dòng
     */
    ImportResultDTO importFromExcel(InputStream inputStream);
}