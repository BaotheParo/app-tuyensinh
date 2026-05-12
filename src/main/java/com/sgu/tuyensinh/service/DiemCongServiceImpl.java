package com.sgu.tuyensinh.service;

import com.sgu.tuyensinh.entity.DiemCong;
import com.sgu.tuyensinh.repository.DiemCongRepository;
import com.sgu.tuyensinh.repository.ThiSinhRepository;
import com.sgu.tuyensinh.service.dto.ImportResultDTO;
import com.sgu.tuyensinh.service.interfaces.IImportService;
import com.sgu.tuyensinh.service.interfaces.ProgressCallback;
import com.sgu.tuyensinh.util.ExcelReaderUtil;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import org.apache.poi.ss.usermodel.Sheet;

import java.io.InputStream;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service xử lý CRUD cho các loại Điểm Ưu Tiên (Chứng chỉ, HSG).
 * Dành cho Admin sửa tay dữ liệu thí sinh.
 */
@Service
@RequiredArgsConstructor
public class DiemCongServiceImpl implements IImportService{

    private final DiemCongRepository diemCongRepository;

    private final ThiSinhRepository thiSinhRepository;

@Override
@Transactional
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

        int total = sheet.getLastRowNum();
        int current = 0;

        for (int i = 1; i <= total; i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            current++;

            try {
                DiemCong dc = new DiemCong();

                String cccd = ExcelReaderUtil.getSafeString(row.getCell(1));

                if (cccd == null || cccd.isBlank()) {
                    throw new IllegalArgumentException("CCCD trống");
                }

                // check tồn tại thí sinh
                if (!thiSinhRepository.existsById(cccd)) {
                    throw new EntityNotFoundException("Không tồn tại CCCD: " + cccd);
                }

                dc.setTsCccd(cccd);
                dc.setManganh(ExcelReaderUtil.getSafeString(row.getCell(2)));
                dc.setMatohop(ExcelReaderUtil.getSafeString(row.getCell(3)));
                dc.setPhuongthuc(ExcelReaderUtil.getSafeString(row.getCell(4)));

                dc.setDiemCC(ExcelReaderUtil.getSafeDouble(row.getCell(5)));
                dc.setDiemUtxt(ExcelReaderUtil.getSafeDouble(row.getCell(6)));
                dc.setDiemTong(ExcelReaderUtil.getSafeDouble(row.getCell(7)));

                dc.setGhichu(ExcelReaderUtil.getSafeString(row.getCell(8)));

                // generate key chống trùng
                dc.setDcKeys("IMPORT_" + System.currentTimeMillis() + "_" + i);

                diemCongRepository.save(dc);

                result.incrementSuccess();

            } catch (Exception e) {
                result.addError("Dòng " + (i + 1) + ": " + e.getMessage());
                result.incrementSkip();
            }

            if (callback != null) {
                callback.onProgress(current, total);
            }
        }

    } catch (Exception e) {
        result.addError("Lỗi hệ thống: " + e.getMessage());
    }

    return result;
}

    
    /**
     * Thêm một chứng chỉ hoặc giải thưởng mới cho thí sinh.
     * Kiểm tra sự tồn tại của thí sinh trước khi lưu.
     */
    @Transactional
    public DiemCong addDiemCong(String cccd, DiemCong diemCong) {
        if (!thiSinhRepository.existsById(cccd)) {
            throw new EntityNotFoundException("Không tìm thấy thí sinh với CCCD: " + cccd);
        }
        diemCong.setTsCccd(cccd);
        // dcKeys là bắt buộc trong schema để chống trùng khi import, 
        // khi add tay ta có thể generate random hoặc dùng timestamp.
        if (diemCong.getDcKeys() == null) {
            diemCong.setDcKeys("MANUAL_" + System.currentTimeMillis());
        }
        return diemCongRepository.save(diemCong);
    }

    /**
     * Cập nhật thông tin điểm cộng hiện có dựa trên ID.
     */
    @Transactional
    public DiemCong updateDiemCong(Integer id, DiemCong diemCongMoi) {
        DiemCong existing = diemCongRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy bản ghi điểm cộng ID: " + id));

        // Senior: Chỉ cập nhật các field nghiệp vụ, không đổi CCCD hay ID
        existing.setManganh(diemCongMoi.getManganh());
        existing.setMatohop(diemCongMoi.getMatohop());
        existing.setPhuongthuc(diemCongMoi.getPhuongthuc());
        existing.setDiemCC(diemCongMoi.getDiemCC());
        existing.setDiemUtxt(diemCongMoi.getDiemUtxt());
        existing.setDiemTong(diemCongMoi.getDiemTong());
        existing.setGhichu(diemCongMoi.getGhichu());
        existing.setNgayCap(diemCongMoi.getNgayCap());

        return diemCongRepository.save(existing);
    }

    /**
     * Xóa một bản ghi điểm cộng.
     */
    @Transactional
    public void deleteDiemCong(Integer id) {
        if (!diemCongRepository.existsById(id)) {
            throw new EntityNotFoundException("Không tìm thấy bản ghi điểm cộng ID: " + id);
        }
        diemCongRepository.deleteById(id);
    }

    /**
     * Lấy danh sách điểm cộng có phân trang (Read-only UI)
     */
    public Page<DiemCong> layDanhSachPhanTrang(int page, int size, String keyword) {
        Pageable pageable = PageRequest.of(page, size);
        if (keyword != null && !keyword.trim().isEmpty()) {
            return diemCongRepository.findByTsCccdContainingIgnoreCase(keyword, pageable);
        }
        return diemCongRepository.findAll(pageable);
    }
}
