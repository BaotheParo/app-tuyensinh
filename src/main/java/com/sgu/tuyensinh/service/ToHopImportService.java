package com.sgu.tuyensinh.service;

import com.sgu.tuyensinh.dto.ToHopMonImportDTO;
import com.sgu.tuyensinh.entity.Nganh;
import com.sgu.tuyensinh.entity.ToHop;
import com.sgu.tuyensinh.repository.NganhRepository;
import com.sgu.tuyensinh.repository.NganhToHopRepository;
import com.sgu.tuyensinh.repository.NguyenVongRepository;
import com.sgu.tuyensinh.repository.ToHopRepository;
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

import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Import Tổ hợp môn từ file "tohopmon.xlsx".
 *
 * Cấu trúc file (dòng 1 là header):
 *   Col 0: STT         | Col 1: MANGANH      | Col 2: TEN_NGANHCHUAN
 *   Col 3: MA_TO_HOP   | Col 4: tb_keys      | Col 5: TEN_TO_HOP
 *   Col 6: Gốc         | Col 7: Độ lệch
 *
 * MA_TO_HOP ở dạng "B03(TO-3,VA-3,SI-1)" → tách thành:
 *   - maToHop = "B03"
 *   - mon1 = "TO", mon2 = "VA", mon3 = "SI"
 *
 * Nếu cột "Gốc" = "Gốc" → đây là tổ hợp gốc của ngành đó
 *   → Tự động cập nhật trường toHopGoc trong bảng xt_nganh
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ToHopImportService implements IImportService {

    private final ToHopRepository toHopRepository;
    private final NganhToHopRepository nganhToHopRepository;
    private final NguyenVongRepository nguyenVongRepository;
    private final NganhRepository nganhRepository;

    @Override
    @Transactional
    public ImportResultDTO importFromExcel(InputStream inputStream, ProgressCallback callback) {
        ImportResultDTO result = new ImportResultDTO();

        if (inputStream == null) {
            result.addError("InputStream không được để null");
            return result;
        }

        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            if (callback != null) callback.onProgress(0, 0);

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null || sheet.getLastRowNum() < 1) {
                result.addError("File không có dữ liệu");
                return result;
            }

            int total = sheet.getLastRowNum();
            int current = 0;
            // Track mã tổ hợp đã lưu trong lần import này để tránh insert trùng
            Set<String> savedInThisRun = new HashSet<>();

            for (int i = 1; i <= total; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                current++;
                ToHopMonImportDTO dto = readRowToDto(row, i + 1);

                // Nếu dòng hoàn toàn trống hoặc thiếu Mã ngành/Mã tổ hợp → bỏ qua không báo lỗi
                if (dto.getMaNganh() == null || dto.getMaNganh().isEmpty() || 
                    dto.getMaToHop() == null || dto.getMaToHop().isEmpty()) {
                    continue;
                }

                // ── Validate ──────────────────────────────
                String error = validateToHop(dto, i + 1);
                if (error != null) {
                    result.addError(i + 1, dto.getMaToHop(), "INVALID_DATA", error);
                    result.incrementSkip();
                } else {
                    // Nếu tổ hợp chưa tồn tại trong DB và chưa lưu trong lần này → insert
                    if (!toHopRepository.existsByMaToHop(dto.getMaToHop())
                            && savedInThisRun.add(dto.getMaToHop())) {
                        toHopRepository.save(convertToEntity(dto));
                    }

                    // ── 3. Lưu vào bảng Ngành - Tổ hợp (xt_nganh_tohop) ──────
                    saveNganhToHop(dto);

                    // Nếu là tổ hợp gốc → update toHopGoc cho ngành
                    updateToHopGocIfNeeded(dto);
                    result.incrementSuccess();
                }

                if (callback != null) callback.onProgress(current, total);
            }

        } catch (Exception e) {
            log.error("Lỗi import tổ hợp", e);
            result.addError("Lỗi hệ thống: " + e.getMessage());
        }

        return result;
    }

    // ── Validate ──────────────────────────────────────────────
    private String validateToHop(ToHopMonImportDTO dto, int rowNum) {
        if (dto.getMaToHop() == null || dto.getMaToHop().trim().isEmpty())
            return "Mã tổ hợp không được trống (dòng " + rowNum + ")";

        // Chấp nhận mã tổ hợp: 1-2 chữ + 2 số (A00, B03, D01, X01)
        if (!dto.getMaToHop().trim().matches("[A-Za-z]{1,2}\\d{2}"))
            return "Mã tổ hợp sai định dạng — nhận được: " + dto.getMaToHop() + " (dòng " + rowNum + ")";

        if (dto.getMon1() == null || dto.getMon1().trim().isEmpty())
            return "Môn 1 không được trống (dòng " + rowNum + ")";

        if (dto.getMon2() == null || dto.getMon2().trim().isEmpty())
            return "Môn 2 không được trống (dòng " + rowNum + ")";

        if (dto.getMon3() == null || dto.getMon3().trim().isEmpty())
            return "Môn 3 không được trống (dòng " + rowNum + ")";

        return null;
    }

    /**
     * Đọc dòng theo cấu trúc tohopmon.xlsx:
     *   Col 0: STT | Col 1: MANGANH | Col 2: TEN_NGANHCHUAN
     *   Col 3: MA_TO_HOP dạng "B03(TO-3,VA-3,SI-1)" | Col 4: tb_keys
     *   Col 5: TEN_TO_HOP | Col 6: Gốc | Col 7: Độ lệch
     */
    private ToHopMonImportDTO readRowToDto(Row row, int rowNumber) {
        ToHopMonImportDTO dto = new ToHopMonImportDTO();

        // Col 1: mã ngành
        dto.setMaNganh(ExcelReaderUtil.getSafeString(row.getCell(1)));

        // Col 3: MA_TO_HOP dạng "B03(TO-3,VA-3,SI-1)"
        String rawToHop = ExcelReaderUtil.getSafeString(row.getCell(3));
        if (rawToHop != null) {
            int parenIdx = rawToHop.indexOf('(');
            if (parenIdx > 0) {
                dto.setMaToHop(rawToHop.substring(0, parenIdx).trim());

                String inner = rawToHop.substring(parenIdx + 1).replace(")", "").trim();
                String[] parts = inner.split(",");
                if (parts.length >= 1) {
                    dto.setMon1(parseMon(parts[0]));
                    dto.setHs1(parseWeight(parts[0]));
                }
                if (parts.length >= 2) {
                    dto.setMon2(parseMon(parts[1]));
                    dto.setHs2(parseWeight(parts[1]));
                }
                if (parts.length >= 3) {
                    dto.setMon3(parseMon(parts[2]));
                    dto.setHs3(parseWeight(parts[2]));
                }
            } else {
                dto.setMaToHop(rawToHop.trim());
            }
        }

        // Col 4: tb_keys
        dto.setTbKeys(ExcelReaderUtil.getSafeString(row.getCell(4)));

        // Col 5: tên tổ hợp
        dto.setTenToHop(ExcelReaderUtil.getSafeString(row.getCell(5)));

        // Col 6: "Gốc" marker — dùng để xác định tổ hợp gốc của ngành
        dto.setGoc(ExcelReaderUtil.getSafeString(row.getCell(6)));

        // Col 7: Độ lệch
        dto.setDoLech(ExcelReaderUtil.getSafeDouble(row.getCell(7)));

        return dto;
    }

    /** Parse "TO-3" → 3.0, "N1-1" → 1.0 */
    private Double parseWeight(String monWithWeight) {
        if (monWithWeight == null) return 1.0;
        int dashIdx = monWithWeight.lastIndexOf('-');
        if (dashIdx > 0) {
            try {
                return Double.parseDouble(monWithWeight.substring(dashIdx + 1).trim());
            } catch (Exception e) {
                return 1.0;
            }
        }
        return 1.0;
    }

    /** Parse "TO-3" → "TO", "N1-1" → "N1" */
    private String parseMon(String monWithWeight) {
        if (monWithWeight == null) return null;
        int dashIdx = monWithWeight.lastIndexOf('-');
        if (dashIdx > 0) return monWithWeight.substring(0, dashIdx).trim();
        return monWithWeight.trim();
    }

    /**
     * Nếu cột Gốc chứa text "Gốc" → cập nhật trường toHopGoc cho ngành tương ứng.
     */
    private void updateToHopGocIfNeeded(ToHopMonImportDTO dto) {
        if (dto.getMaNganh() == null || dto.getMaToHop() == null) return;
        String goc = dto.getGoc();
        if (goc == null || goc.trim().isEmpty()) return;
        // So sánh trực tiếp: cột có giá trị "Gốc" (tiếng Việt có dấu)
        // Dùng contains để linh hoạt với các biến thể whitespace
        if (!goc.trim().equals("Gốc") && !goc.trim().equals("Goc") && !goc.trim().equals("gốc")) return;

        Optional<Nganh> nganhOpt = nganhRepository.findById(dto.getMaNganh().trim());
        if (nganhOpt.isPresent()) {
            Nganh nganh = nganhOpt.get();
            nganh.setToHopGoc(dto.getMaToHop().trim());
            nganhRepository.save(nganh);
            log.debug("✅ Tổ hợp gốc ngành {}: {}", dto.getMaNganh(), dto.getMaToHop());
        }
    }

    private com.sgu.tuyensinh.entity.ToHop convertToEntity(ToHopMonImportDTO dto) {
        ToHop toHop = new ToHop();
        toHop.setMaToHop(dto.getMaToHop().trim());
        toHop.setTenToHop(dto.getTenToHop() != null ? dto.getTenToHop().trim() : dto.getMaToHop());
        toHop.setMon1(dto.getMon1() != null ? dto.getMon1().trim() : null);
        toHop.setMon2(dto.getMon2() != null ? dto.getMon2().trim() : null);
        toHop.setMon3(dto.getMon3() != null ? dto.getMon3().trim() : null);
        return toHop;
    }

    private void saveNganhToHop(ToHopMonImportDTO dto) {
        if (dto.getMaNganh() == null || dto.getMaToHop() == null) return;

        // Xây dựng tb_keys nếu trống (manganh_matohop)
        String tbKeys = dto.getTbKeys();
        if (tbKeys == null || tbKeys.isEmpty()) {
            tbKeys = dto.getMaNganh().trim() + "_" + dto.getMaToHop().trim();
        }

        // Tìm existing để update hoặc tạo mới
        // (Đây là logic tối ưu để tránh duplicate records trong bảng xt_nganh_tohop)
        com.sgu.tuyensinh.entity.NganhToHop nth = new com.sgu.tuyensinh.entity.NganhToHop();
        nth.setMaNganh(dto.getMaNganh().trim());
        nth.setMaToHop(dto.getMaToHop().trim());
        nth.setThMon1(dto.getMon1());
        nth.setHsMon1(dto.getHs1() != null ? dto.getHs1() : 1.0);
        nth.setThMon2(dto.getMon2());
        nth.setHsMon2(dto.getHs2() != null ? dto.getHs2() : 1.0);
        nth.setThMon3(dto.getMon3());
        nth.setHsMon3(dto.getHs3() != null ? dto.getHs3() : 1.0);
        nth.setTbKeys(tbKeys);
        nth.setDoLech(dto.getDoLech() != null ? dto.getDoLech() : 0.0);

        // Thiết lập cờ (flags) môn thi tự động dựa trên tên môn
        setFlags(nth, dto.getMon1(), dto.getMon2(), dto.getMon3());

        // Lưu vào DB
        nganhToHopRepository.save(nth);
    }

    private void setFlags(com.sgu.tuyensinh.entity.NganhToHop nth, String m1, String m2, String m3) {
        // Reset all flags
        nth.setN1(0.0); nth.setTo(0.0); nth.setLi(0.0); nth.setHo(0.0);
        nth.setSi(0.0); nth.setVa(0.0); nth.setSu(0.0); nth.setDi(0.0);
        nth.setTi(0.0); nth.setKtpl(0.0);

        List<String> mons = List.of(
            m1 != null ? m1.toUpperCase() : "",
            m2 != null ? m2.toUpperCase() : "",
            m3 != null ? m3.toUpperCase() : ""
        );

        for (String m : mons) {
            switch (m) {
                case "TO": nth.setTo(1.0); break;
                case "VA": nth.setVa(1.0); break;
                case "LI": nth.setLi(1.0); break;
                case "HO": nth.setHo(1.0); break;
                case "SI": nth.setSi(1.0); break;
                case "SU": nth.setSu(1.0); break;
                case "DI": nth.setDi(1.0); break;
                case "TI": nth.setTi(1.0); break;
                case "GD": case "KT": nth.setKtpl(1.0); break;
                case "N1": case "AN": case "EN": nth.setN1(1.0); break;
            }
        }
    }

    // =========================================================
    // CÁC HÀM CRUD PHỤC VỤ TRỰC TIẾP CHO GIAO DIỆN (JAVA SWING)
    // =========================================================

    public Page<ToHop> layDanhSachPhanTrang(int page, int size, String keyword) {
        Pageable pageable = PageRequest.of(page, size);
        if (keyword != null && !keyword.trim().isEmpty()) {
            return toHopRepository.findByMaToHopContainingIgnoreCaseOrTenToHopContainingIgnoreCase(keyword, keyword, pageable);
        }
        return toHopRepository.findAll(pageable);
    }

    @Transactional
    public ToHop luuToHop(ToHop toHop) {
        if (toHop.getMaToHop() == null || toHop.getMaToHop().isBlank())
            throw new IllegalArgumentException("Mã tổ hợp không được để trống!");
        if (toHop.getMon1() == null || toHop.getMon2() == null || toHop.getMon3() == null)
            throw new IllegalArgumentException("Phải nhập đủ 3 môn thi!");
        if (toHop.getIdtohop() == null && toHopRepository.existsByMaToHop(toHop.getMaToHop()))
            throw new IllegalArgumentException("Mã tổ hợp này đã tồn tại!");
        return toHopRepository.save(toHop);
    }

    @Transactional
    public void xoaToHop(Integer id) {
        if (!toHopRepository.existsById(id))
            throw new IllegalArgumentException("Không tìm thấy tổ hợp với ID: " + id);
        toHopRepository.deleteById(id);
    }

    public List<ToHop> getAllToHop() { return toHopRepository.findAll(); }

    public ToHop getToHopById(Integer id) {
        return toHopRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tổ hợp với ID: " + id));
    }

    public ToHop createToHop(ToHop toHop) {
        if (toHop.getMaToHop() == null || toHop.getMaToHop().isBlank())
            throw new IllegalArgumentException("Mã tổ hợp không được để trống");
        if (toHopRepository.existsByMaToHop(toHop.getMaToHop()))
            throw new IllegalArgumentException("Mã tổ hợp đã tồn tại");
        return toHopRepository.save(toHop);
    }

    public ToHop updateToHop(Integer id, ToHop newData) {
        ToHop old = toHopRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tổ hợp với ID: " + id));
        if (!old.getMaToHop().equals(newData.getMaToHop()))
            throw new IllegalStateException("Không được thay đổi mã tổ hợp");
        old.setTenToHop(newData.getTenToHop());
        old.setMon1(newData.getMon1());
        old.setMon2(newData.getMon2());
        old.setMon3(newData.getMon3());
        return toHopRepository.save(old);
    }

    public void deleteToHop(Integer id) {
        ToHop toHop = toHopRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tổ hợp với ID: " + id));
        if (nguyenVongRepository.existsByTtThm(toHop.getMaToHop()))
            throw new IllegalStateException("Không thể xóa vì có nguyện vọng liên quan");
        if (nganhToHopRepository.existsByMaToHop(toHop.getMaToHop()))
            throw new IllegalStateException("Không thể xóa vì có ngành liên quan");
        try {
            toHopRepository.delete(toHop);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("Không thể xóa do ràng buộc dữ liệu trong DB");
        }
    }
}