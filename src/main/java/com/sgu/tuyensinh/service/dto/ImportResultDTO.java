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

    private int successCount;   // Số dòng import thành công
    private int skipCount;      // Số dòng bỏ qua (trùng, không hợp lệ)
    private List<String> errors; // Danh sách mô tả lỗi từng dòng

    public ImportResultDTO() {
        this.errors = new ArrayList<>();
    }

    // ── Mutators dùng trong vòng lặp import ──────────────────────────

    public void incrementSuccess() {
        this.successCount++;
    }

    public void incrementSkip() {
        this.skipCount++;
    }

    public void addError(String errorMessage) {
        this.errors.add(errorMessage);
    }

    // // ── Getters ───────────────────────────────────────────────────────

    // public int getSuccessCount() { return successCount; }
    // public int getSkipCount()    { return skipCount; }
    // public List<String> getErrors() { return errors; }
    // public boolean hasErrors()   { return !errors.isEmpty(); }

    @Override
    public String toString() {
        return String.format("Import xong — Thành công: %d | Bỏ qua: %d | Lỗi: %d dòng",
                successCount, skipCount, errors.size());
    }
}