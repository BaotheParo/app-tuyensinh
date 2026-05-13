package com.sgu.tuyensinh.service.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

/**
 * DTO chứa kết quả sau khi import Excel.
 * Dùng chung cho tất cả các loại import (Ngành, TổHợp, NganhToHop, BảngQuyĐổi).
 * Hiển thị lên ErrorLogDialog sau khi import xong.
 */
@Data
public class ImportResultDTO {

    private int successCount;
    private int skipCount;
    private List<RowErrorDTO> errors;  // đổi từ List<String>

    public ImportResultDTO() {
        this.errors = new ArrayList<>();
    }

    public void incrementSuccess() { this.successCount++; }
    public void addSuccessCount(int count) { this.successCount += count; }
    public void incrementSkip()    { this.skipCount++;    }

    // Thêm lỗi có đầy đủ thông tin
    public void addError(int rowNumber, String identifier, String errorCode, String detail) {
        this.errors.add(new RowErrorDTO(rowNumber, identifier, errorCode, detail));
    }

    // Giữ overload String cho chỗ nào chưa có đủ thông tin
    public void addError(String message) {
        this.errors.add(new RowErrorDTO(0, "", "ERROR", message));
    }

    @Override
    public String toString() {
        return String.format("Import xong — Thành công: %d | Bỏ qua: %d | Lỗi: %d dòng",
                successCount, skipCount, errors.size());
    }
}