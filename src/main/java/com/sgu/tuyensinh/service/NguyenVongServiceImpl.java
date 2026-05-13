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
    private final com.sgu.tuyensinh.repository.ThiSinhRepository thiSinhRepository;

    // ── Import ───────────────────────────────────────────────
    @Override
    public ImportResultDTO importFromExcel(InputStream inputStream, ProgressCallback callback) {
        ImportResultDTO result = new ImportResultDTO();

        if (inputStream == null) {
            result.addError("InputStream null");
            return result;
        }

        if (callback != null) callback.onProgress(0, 0);

        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            int totalSheets = workbook.getNumberOfSheets();
            int totalRowsAllSheets = 0;
            for (int s = 0; s < totalSheets; s++) {
                Sheet sheet = workbook.getSheetAt(s);
                if (sheet != null && !sheet.getSheetName().equalsIgnoreCase("TKchung")) {
                    totalRowsAllSheets += sheet.getLastRowNum();
                }
            }

            List<NguyenVong> batchList = new java.util.ArrayList<>();
            int processedCount = 0;

            for (int s = 0; s < totalSheets; s++) {
                Sheet sheet = workbook.getSheetAt(s);
                String sheetName = sheet.getSheetName().trim();
                
                // 1. Bỏ qua sheet thống kê chung hoặc sheet không liên quan
                if (sheetName.equalsIgnoreCase("TKchung") || sheetName.contains("ThongKe")) {
                    log.info("⏭️ Bỏ qua sheet thống kê: {}", sheetName);
                    continue;
                }

                log.info("🔍 Đang xử lý sheet: {}", sheetName);
                int totalRows = sheet.getLastRowNum();
                boolean isWishListSheet = false;
                
                for (int i = 0; i <= totalRows; i++) {
                    Row row = sheet.getRow(i);
                    if (row == null) continue;

                    String cccdRaw = ExcelReaderUtil.getSafeString(row.getCell(1));
                    if (cccdRaw == null || cccdRaw.isBlank()) continue;
                    
                    // 2. Kiểm tra dòng tiêu đề để xác định cấu trúc sheet
                    if (cccdRaw.equalsIgnoreCase("CCCD") || cccdRaw.contains("Số CCCD")) {
                        isWishListSheet = true;
                        continue;
                    }

                    // 3. Nếu là sheet TKchung mà bị sót qua filter name, hoặc dòng rác của sheet TK
                    if (cccdRaw.contains("Mã xét tuyển") || cccdRaw.contains("TT")) {
                        continue; 
                    }

                    processedCount++;

                    // ── Parse DTO ───────────────────────────────
                    NguyenVongImportDTO dto = new NguyenVongImportDTO();
                    String cccd = cccdRaw.trim();
                    
                    if (isWishListSheet) {
                        // Cấu trúc Sheet1/Sheet2 (CCCD ở cột 1, ThuTu ở cột 2, MaNganh ở cột 5)
                        dto.setCccd(cccd);
                        dto.setThuTu(ExcelReaderUtil.getSafeInteger(row.getCell(2)));
                        dto.setMaNganh(ExcelReaderUtil.getSafeString(row.getCell(5)));
                    } else {
                        // Cấu trúc mặc định (Phòng hờ nếu file khác)
                        dto.setCccd(cccd);
                        dto.setMaNganh(ExcelReaderUtil.getSafeString(row.getCell(4)));
                        dto.setThuTu(ExcelReaderUtil.getSafeInteger(row.getCell(7)));
                        dto.setPhuongThuc(ExcelReaderUtil.getSafeString(row.getCell(6)));
                        dto.setToHopMon(ExcelReaderUtil.getSafeString(row.getCell(5)));
                    }

                    // ── Validate & Add to Batch ────────────────
                    try {
                        String error = validate(dto, i + 1);
                        if (error != null) {
                            if (dto.getThuTu() == null && (cccd.length() < 5)) continue;
                            result.addError(i + 1, dto.getCccd(), "INVALID_DATA", "Sheet [" + sheetName + "] " + error);
                            result.incrementSkip();
                        } else if (!thiSinhRepository.existsById(dto.getCccd())) {
                            result.addError(i + 1, dto.getCccd(), "MISSING_THISINH", 
                                "Sheet [" + sheetName + "] Dòng " + (i + 1) + ": Thí sinh " + dto.getCccd() + " chưa có.");
                            result.incrementSkip();
                        } else {
                            batchList.add(toEntity(dto));
                            
                            // Thực hiện lưu theo batch (ví dụ 1000 dòng/lần) để tăng tốc độ
                            if (batchList.size() >= 1000) {
                                repository.saveAll(batchList);
                                result.addSuccessCount(batchList.size());
                                batchList.clear();
                                log.info("✅ Đã import thành công {} nguyện vọng...", processedCount);
                            }
                        }
                    } catch (Exception e) {
                        result.addError(i + 1, dto.getCccd(), "DB_ERROR", "Lỗi: " + e.getMessage());
                        result.incrementSkip();
                    }

                    if (callback != null && processedCount % 500 == 0) {
                        callback.onProgress(processedCount, totalRowsAllSheets);
                    }
                }
            }

            // Lưu phần dư còn lại trong batch
            if (!batchList.isEmpty()) {
                repository.saveAll(batchList);
                result.addSuccessCount(batchList.size());
                batchList.clear();
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

    public Page<NguyenVong> layDanhSachPhanTrangVoiStatus(int page, int size, String keyword, String status) {
        Pageable pageable = PageRequest.of(page, size);
        return repository.searchWithFilter(keyword, status, pageable);
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

    @Transactional
    public void deleteAll() {
        repository.deleteAll();
    }
}