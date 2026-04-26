package com.sgu.tuyensinh.service.impl;

import com.sgu.tuyensinh.dto.NganhImportDTO;
import com.sgu.tuyensinh.entity.Nganh;
import com.sgu.tuyensinh.repository.NganhRepository;
import com.sgu.tuyensinh.repository.NganhToHopRepository;
import com.sgu.tuyensinh.repository.NguyenVongRepository;
import com.sgu.tuyensinh.service.dto.ImportResultDTO;
import com.sgu.tuyensinh.service.interfaces.IImportService;
import com.sgu.tuyensinh.service.interfaces.ProgressCallback;
import com.sgu.tuyensinh.util.ExcelReaderUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.io.InputStream;
import java.math.BigDecimal;
// ✅ KHÔNG import ImportWorker — tránh phụ thuộc ngược từ service → UI

/**
 * Import Ngành từ Excel.
 * Đặt tại: service/impl/NganhServiceImpl.java
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NganhServiceImpl implements IImportService {

// Thêm vào phần field
private final NganhRepository nganhRepository;
private final NguyenVongRepository nguyenVongRepository;    // ← thêm
private final NganhToHopRepository nganhToHopRepository;    // ← thêm
    @Override
    public ImportResultDTO importFromExcel(InputStream inputStream, ProgressCallback callback) {

        ImportResultDTO result = new ImportResultDTO();

        if (inputStream == null) {
            result.addError("InputStream null");
            return result;
        }

        try (Workbook workbook = WorkbookFactory.create(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null || sheet.getLastRowNum() < 1) {
                result.addError("File không có dữ liệu");
                return result;
            }

            int total   = sheet.getLastRowNum(); // tổng số dòng dữ liệu (bỏ header)
            int current = 0;

            for (int i = 1; i <= total; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                current++;

                // ── Parse DTO ───────────────────────────────
                NganhImportDTO dto = new NganhImportDTO();
                dto.setMaNganh(ExcelReaderUtil.getSafeString(row.getCell(1)));
                dto.setTenNganh(ExcelReaderUtil.getSafeString(row.getCell(2)));
                dto.setToHopGoc(ExcelReaderUtil.getSafeString(row.getCell(3)));
                dto.setChiTieu(ExcelReaderUtil.getSafeInteger(row.getCell(4)));
                dto.setDiemSan(ExcelReaderUtil.getSafeBigDecimal(row.getCell(5)));

                // ── Validate ────────────────────────────────
                String error = validate(dto, i + 1);
                if (error != null) {
                    result.addError(error);
                    result.incrementSkip();
                } else {
                    // ✅ Lưu ngay từng dòng hợp lệ thay vì batch cuối
                    // -> progress bar phản ánh đúng tiến trình DB
                    nganhRepository.save(convertToEntity(dto));
                    result.incrementSuccess();
                }

                // ✅ Callback sau mỗi dòng để UI cập nhật real-time
                if (callback != null) {
                    callback.onProgress(current, total);
                }
            }

        } catch (Exception e) {
            log.error("Lỗi import ngành", e);
            result.addError("Lỗi hệ thống: " + e.getMessage());
        }

        return result;
    }

    // ── Validate ─────────────────────────────────────────────
    private String validate(NganhImportDTO dto, int rowNum) {
        if (dto.getMaNganh() == null || dto.getMaNganh().isBlank())
            return "Dòng " + rowNum + ": Mã ngành không được trống";
        if (dto.getTenNganh() == null || dto.getTenNganh().isBlank())
            return "Dòng " + rowNum + ": Tên ngành không được trống (maNganh=" + dto.getMaNganh() + ")";
        if (dto.getChiTieu() == null || dto.getChiTieu() <= 0)
            return "Dòng " + rowNum + ": Chỉ tiêu phải > 0 (maNganh=" + dto.getMaNganh() + ")";
        if (dto.getDiemSan() == null || dto.getDiemSan().compareTo(BigDecimal.ZERO) < 0)
            return "Dòng " + rowNum + ": Điểm sàn không được âm (maNganh=" + dto.getMaNganh() + ")";
        return null;
    }

    // ── Convert ──────────────────────────────────────────────
    private Nganh convertToEntity(NganhImportDTO dto) {
        Nganh nganh = new Nganh();
        nganh.setMaNganh(dto.getMaNganh().trim());
        nganh.setTenNganh(dto.getTenNganh().trim());
        nganh.setToHopGoc(dto.getToHopGoc() != null ? dto.getToHopGoc().trim() : null);
        nganh.setChiTieu(dto.getChiTieu());
        nganh.setDiemSan(dto.getDiemSan());
        return nganh;
    }

    // =========================================================
    // CRUD PHỤC VỤ GIAO DIỆN SWING
    // =========================================================

    /**
     * Lấy danh sách ngành có phân trang + tìm kiếm
     */
    public Page<Nganh> layDanhSachPhanTrang(int page, int size, String keyword) {
        Pageable pageable = PageRequest.of(page, size);
        if (keyword != null && !keyword.trim().isEmpty()) {
            return nganhRepository.findByMaNganhContainingOrTenNganhContainingIgnoreCase(
                keyword, keyword, pageable);
        }
        return nganhRepository.findAll(pageable);
    }

    /**
     * Lưu mới hoặc cập nhật Ngành
     */
    @Transactional
    public Nganh luuNganh(Nganh nganh) {
        if (nganh.getMaNganh() == null || nganh.getMaNganh().isBlank())
            throw new IllegalArgumentException("Mã ngành không được để trống!");
        if (nganh.getTenNganh() == null || nganh.getTenNganh().isBlank())
            throw new IllegalArgumentException("Tên ngành không được để trống!");
        return nganhRepository.save(nganh);
    }




        public List<Nganh> getAllNganh() {
        return nganhRepository.findAll();
    }

    public Nganh getNganhById(String maNganh) {
        return nganhRepository.findById(maNganh)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy ngành với mã: " + maNganh));
    }

    public Nganh createNganh(Nganh nganh) {
        if (nganh.getMaNganh() == null || nganh.getMaNganh().isBlank())
            throw new IllegalArgumentException("Mã ngành không được để trống");
        if (nganhRepository.existsById(nganh.getMaNganh()))
            throw new IllegalArgumentException("Mã ngành đã tồn tại");
        return nganhRepository.save(nganh);
    }

    public Nganh updateNganh(String maNganh, Nganh newData) {
        Nganh old = nganhRepository.findById(maNganh)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy ngành với mã: " + maNganh));

        if (!old.getMaNganh().equals(newData.getMaNganh()))
            throw new IllegalStateException("Không được thay đổi mã ngành");

        old.setTenNganh(newData.getTenNganh());
        old.setDiemSan(newData.getDiemSan());
        old.setChiTieu(newData.getChiTieu());
        old.setToHopGoc(newData.getToHopGoc());

        return nganhRepository.save(old);
    }

    public void deleteNganh(String maNganh) {
        Nganh nganh = nganhRepository.findById(maNganh)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy ngành với mã: " + maNganh));

        if (nguyenVongRepository.existsByNvManganh(maNganh))
            throw new IllegalStateException("Không thể xóa vì có nguyện vọng liên quan");

        if (nganhToHopRepository.existsByMaNganh(maNganh))
            throw new IllegalStateException("Không thể xóa vì có tổ hợp liên quan");

        try {
            nganhRepository.delete(nganh);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("Không thể xóa do ràng buộc dữ liệu trong DB");
        }
    }
}
