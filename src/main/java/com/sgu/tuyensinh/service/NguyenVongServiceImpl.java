package com.sgu.tuyensinh.service;

import com.sgu.tuyensinh.dto.NguyenVongImportDTO;
import com.sgu.tuyensinh.entity.NguyenVong;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NguyenVongServiceImpl implements IImportService {

    private final NguyenVongRepository repository;

    // ── Import ───────────────────────────────────────────────
    @Override
    public ImportResultDTO importFromExcel(InputStream inputStream, ProgressCallback callback) {
        ImportResultDTO result = new ImportResultDTO();

        if (inputStream == null) {
            result.addError("InputStream null");
            return result;
        }

        if (callback != null) callback.onProgress(0, 0); // báo đang đọc file

        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null || sheet.getLastRowNum() < 1) {
                result.addError("File không có dữ liệu");
                return result;
            }

            int total   = sheet.getLastRowNum();
            int current = 0;

            for (int i = 1; i <= total; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                current++;

                // ── Parse DTO ───────────────────────────────
                NguyenVongImportDTO dto = new NguyenVongImportDTO();
                dto.setCccd(ExcelReaderUtil.getSafeString(row.getCell(0)));
                dto.setMaNganh(ExcelReaderUtil.getSafeString(row.getCell(1)));
                dto.setThuTu(ExcelReaderUtil.getSafeInteger(row.getCell(2)));
                dto.setPhuongThuc(ExcelReaderUtil.getSafeString(row.getCell(3)));
                dto.setToHopMon(ExcelReaderUtil.getSafeString(row.getCell(4)));

                // ── Validate ────────────────────────────────
                String error = validate(dto, i + 1);
                if (error != null) {
                    result.addError(i + 1, dto.getCccd(), "INVALID_DATA", error);
                    result.incrementSkip();
                } else {
                    repository.save(toEntity(dto));
                    result.incrementSuccess();
                }

                if (callback != null) callback.onProgress(current, total);
            }

        } catch (Exception e) {
            log.error("Lỗi import nguyện vọng", e);
            result.addError("Lỗi hệ thống: " + e.getMessage());
        }

        return result;
    }

    // ── Validate ─────────────────────────────────────────────
    private String validate(NguyenVongImportDTO dto, int rowNum) {
        if (dto.getCccd() == null || dto.getCccd().isBlank())
            return "Dòng " + rowNum + ": CCCD không được trống";
        if (dto.getMaNganh() == null || dto.getMaNganh().isBlank())
            return "Dòng " + rowNum + ": Mã ngành không được trống (cccd=" + dto.getCccd() + ")";
        if (dto.getThuTu() == null)
            return "Dòng " + rowNum + ": Thứ tự NV không được trống (cccd=" + dto.getCccd() + ")";
        return null;
    }

    // ── Convert ──────────────────────────────────────────────
    private NguyenVong toEntity(NguyenVongImportDTO dto) {
        NguyenVong nv = new NguyenVong();
        nv.setNnCccd(dto.getCccd());
        nv.setNvManganh(dto.getMaNganh());
        nv.setNvTt(dto.getThuTu());
        nv.setTtPhuongthuc(dto.getPhuongThuc());
        nv.setTtThm(dto.getToHopMon());
        nv.setDiemThxt(dto.getDiemThxt());
        nv.setDiemUtqd(dto.getDiemUtqd());
        nv.setDiemCong(dto.getDiemCong());
        nv.setDiemXetTuyen(dto.getDiemXetTuyen());
        nv.setNvKetQua(null);
        nv.setNvKeys(dto.getCccd() + "_" + dto.getMaNganh() + "_" + dto.getThuTu());
        return nv;
    }

    // ── CRUD ─────────────────────────────────────────────────
    public Page<NguyenVong> layDanhSachPhanTrang(int page, int size, String keyword) {
        Pageable pageable = PageRequest.of(page, size);
        if (keyword != null && !keyword.trim().isEmpty()) {
            return repository.findByNnCccdContainingIgnoreCaseOrNvManganhContainingIgnoreCase(
                keyword, keyword, pageable);
        }
        return repository.findAll(pageable);
    }

    public List<NguyenVong> getByCccd(String cccd) {
        return repository.findAll().stream()
                .filter(nv -> nv.getNnCccd().equals(cccd))
                .toList();
    }

    @Transactional
    public List<NguyenVong> getDanhSachTrungTuyen(String maNganh) {
        return repository.findByNvManganhAndNvKetQua(maNganh, "TRUNG_TUYEN");
    }
}