package com.sgu.tuyensinh.service.impl;

import com.sgu.tuyensinh.dto.ToHopMonImportDTO;
import com.sgu.tuyensinh.entity.ToHop;
import com.sgu.tuyensinh.repository.NganhToHopRepository;
import com.sgu.tuyensinh.repository.NguyenVongRepository;
import com.sgu.tuyensinh.repository.ToHopRepository;
import com.sgu.tuyensinh.service.dto.ImportResultDTO;
import com.sgu.tuyensinh.service.interfaces.IImportService;
import com.sgu.tuyensinh.util.ExcelReaderUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sgu.tuyensinh.service.interfaces.ProgressCallback;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class ToHopServiceImpl implements IImportService {

    private final ToHopRepository toHopRepository;
    private final NganhToHopRepository nganhToHopRepository;
    private final NguyenVongRepository nguyenVongRepository;
    @Override
    @Transactional
    public ImportResultDTO importFromExcel(InputStream inputStream, ProgressCallback callback) {
        ImportResultDTO result = new ImportResultDTO();

        if (inputStream == null) {
            result.addError("InputStream không được để null");
            return result;
        }

        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            // Báo đang đọc file
            if (callback != null)
                callback.onProgress(0, 0);

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null || sheet.getLastRowNum() < 1) {
                result.addError("File không có dữ liệu");
                return result;
            }

            int total = sheet.getLastRowNum();
            int current = 0;
            Set<String> maToHopSet = new HashSet<>();

            for (int i = 1; i <= total; i++) {
                Row row = sheet.getRow(i);
                if (row == null)
                    continue;

                current++;
                ToHopMonImportDTO dto = readRowToDto(row, i + 1);

                // ── Validate ────────────────────────────────
                String error = validateToHop(dto, i + 1);
                if (error != null) {
                    result.addError(i + 1, dto.getMaToHop(), "INVALID_DATA", error);
                    result.incrementSkip();

                } else if (!maToHopSet.add(dto.getMaToHop())) {
                    result.addError(i + 1, dto.getMaToHop(), "DUPLICATE_IN_FILE",
                            "Mã tổ hợp bị trùng trong file");
                    result.incrementSkip();

                } else if (toHopRepository.existsByMaToHop(dto.getMaToHop())) {
                    result.addError(i + 1, dto.getMaToHop(), "DUPLICATE_IN_DB",
                            "Mã tổ hợp đã tồn tại trong database");
                    result.incrementSkip();

                } else {
                    toHopRepository.save(convertToEntity(dto));
                    result.incrementSuccess();
                }

                // ── Callback progress ────────────────────────
                if (callback != null)
                    callback.onProgress(current, total);
            }

        } catch (Exception e) {
            log.error("Lỗi import tổ hợp", e);
            result.addError("Lỗi hệ thống: " + e.getMessage());
        }

        return result;
    }

    // ── Validate có kiểm tra định dạng ──────────────────────
    private String validateToHop(ToHopMonImportDTO dto, int rowNum) {
        if (dto.getMaToHop() == null || dto.getMaToHop().trim().isEmpty())
            return "Mã tổ hợp không được trống";

        // Mã tổ hợp chỉ cho phép chữ + số, VD: A00, D01, C15
        if (!dto.getMaToHop().trim().matches("[A-Za-z]\\d{2}"))
            return "Mã tổ hợp sai định dạng (VD: A00, D01) — nhận được: " + dto.getMaToHop();

        if (dto.getTenToHop() == null || dto.getTenToHop().trim().isEmpty())
            return "Tên tổ hợp không được trống";

        if (dto.getMon1() == null || dto.getMon1().trim().isEmpty())
            return "Môn 1 không được trống";

        if (dto.getMon2() == null || dto.getMon2().trim().isEmpty())
            return "Môn 2 không được trống";

        if (dto.getMon3() == null || dto.getMon3().trim().isEmpty())
            return "Môn 3 không được trống";

        return null; // hợp lệ
    }

    private ToHopMonImportDTO readRowToDto(Row row, int rowNumber) {
        ToHopMonImportDTO dto = new ToHopMonImportDTO();
        dto.setMaToHop(ExcelReaderUtil.getSafeString(row.getCell(1)));
        dto.setMon1(ExcelReaderUtil.getSafeString(row.getCell(2)));
        dto.setMon2(ExcelReaderUtil.getSafeString(row.getCell(3)));
        dto.setMon3(ExcelReaderUtil.getSafeString(row.getCell(4)));
        dto.setTenToHop(ExcelReaderUtil.getSafeString(row.getCell(5)));
        return dto;
    }

    private ToHop convertToEntity(ToHopMonImportDTO dto) {
        ToHop toHop = new ToHop();
        toHop.setMaToHop(dto.getMaToHop().trim());
        toHop.setTenToHop(dto.getTenToHop().trim());
        toHop.setMon1(dto.getMon1() != null ? dto.getMon1().trim() : null);
        toHop.setMon2(dto.getMon2() != null ? dto.getMon2().trim() : null);
        toHop.setMon3(dto.getMon3() != null ? dto.getMon3().trim() : null);
        return toHop;
    }

    // =========================================================
    // CÁC HÀM CRUD PHỤC VỤ TRỰC TIẾP CHO GIAO DIỆN (JAVA SWING)
    // =========================================================

    /**
     * Lấy danh sách Tổ hợp có phân trang
     */
    public org.springframework.data.domain.Page<ToHop> layDanhSachPhanTrang(int page, int size, String keyword) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        if (keyword != null && !keyword.trim().isEmpty()) {
            return toHopRepository.findByMaToHopContainingIgnoreCaseOrTenToHopContainingIgnoreCase(keyword, keyword,
                    pageable);
        }
        return toHopRepository.findAll(pageable);
    }

    /**
     * Lưu mới hoặc Cập nhật Tổ Hợp
     */
    @Transactional
    public ToHop luuToHop(ToHop toHop) {
        if (toHop.getMaToHop() == null || toHop.getMaToHop().isBlank()) {
            throw new IllegalArgumentException("Mã tổ hợp không được để trống!");
        }
        if (toHop.getMon1() == null || toHop.getMon2() == null || toHop.getMon3() == null) {
            throw new IllegalArgumentException("Phải nhập đủ 3 môn thi!");
        }

        // Kiểm tra trùng lặp nếu là Thêm mới (idtohop == null)
        if (toHop.getIdtohop() == null && toHopRepository.existsByMaToHop(toHop.getMaToHop())) {
            throw new IllegalArgumentException("Mã tổ hợp này đã tồn tại!");
        }

        return toHopRepository.save(toHop);
    }

    /**
     * Xóa Tổ Hợp theo ID
     */
    @Transactional
    public void xoaToHop(Integer id) {
        if (!toHopRepository.existsById(id)) {
            throw new IllegalArgumentException("Không tìm thấy tổ hợp với ID: " + id);
        }
        toHopRepository.deleteById(id);
    }


    public List<ToHop> getAllToHop() {
        return toHopRepository.findAll();
    }

    public ToHop getToHopById(Integer id) {
        return toHopRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tổ hợp với ID: " + id));
    }

    public ToHop createToHop(ToHop toHop) {
        if (toHop.getMaToHop() == null || toHop.getMaToHop().isBlank()) {
            throw new IllegalArgumentException("Mã tổ hợp không được để trống");
        }

        // check duplicate theo business key
        if (toHopRepository.existsByMaToHop(toHop.getMaToHop())) {
            throw new IllegalArgumentException("Mã tổ hợp đã tồn tại");
        }

        return toHopRepository.save(toHop);
    }

    public ToHop updateToHop(Integer id, ToHop newData) {
        ToHop old = toHopRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tổ hợp với ID: " + id));

        // ❌ Không cho đổi mã (tránh vỡ FK)
        if (!old.getMaToHop().equals(newData.getMaToHop())) {
            throw new IllegalStateException("Không được thay đổi mã tổ hợp");
        }

        old.setTenToHop(newData.getTenToHop());
        old.setMon1(newData.getMon1());
        old.setMon2(newData.getMon2());
        old.setMon3(newData.getMon3());

        return toHopRepository.save(old);
    }

    public void deleteToHop(Integer id) {
        ToHop toHop = toHopRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tổ hợp với ID: " + id));

        // check logic trước
        if (nguyenVongRepository.existsByTtThm(toHop.getMaToHop())) {
            throw new IllegalStateException("Không thể xóa vì có nguyện vọng liên quan");
        }

        if (nganhToHopRepository.existsByMaToHop(toHop.getMaToHop())) {
            throw new IllegalStateException("Không thể xóa vì có ngành liên quan");
        }

        try {
            toHopRepository.delete(toHop);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("Không thể xóa do ràng buộc dữ liệu trong DB");
        }
    }
}