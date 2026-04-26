package com.sgu.tuyensinh.service.impl;

import com.sgu.tuyensinh.entity.DiemThi;
import com.sgu.tuyensinh.repository.DiemThiRepository;
import com.sgu.tuyensinh.service.dto.ImportResultDTO;
import com.sgu.tuyensinh.service.interfaces.IImportService;
import com.sgu.tuyensinh.service.interfaces.ProgressCallback;
import com.sgu.tuyensinh.util.ExcelReaderUtil;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.apache.poi.ss.usermodel.Sheet;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Service implementation cho các nghiệp vụ quản lý và thống kê Điểm Thi.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DiemThiServiceImpl implements IImportService {

    private final DiemThiRepository diemThiRepository;

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
                if (row == null)
                    continue;

                current++;

                try {
                    DiemThi diem = new DiemThi();

                    // ⚠️ bạn phải map đúng cột Excel
                    diem.setToan(ExcelReaderUtil.getSafeDouble(row.getCell(1)));
                    diem.setVan(ExcelReaderUtil.getSafeDouble(row.getCell(2)));
                    diem.setLy(ExcelReaderUtil.getSafeDouble(row.getCell(3)));
                    diem.setHoa(ExcelReaderUtil.getSafeDouble(row.getCell(4)));
                    diem.setSinh(ExcelReaderUtil.getSafeDouble(row.getCell(5)));
                    diem.setAnh(ExcelReaderUtil.getSafeDouble(row.getCell(6)));

                    // TODO: set CCCD / liên kết thí sinh
                    // diem.setThiSinh(...)

                    diemThiRepository.save(diem);
                    result.incrementSuccess();

                } catch (Exception e) {
                    result.addError("Dòng " + (i + 1) + ": " + e.getMessage());
                    result.incrementSkip();
                }

                // cập nhật progress
                if (callback != null) {
                    callback.onProgress(current, total);
                }
            }

        } catch (Exception e) {
            log.error("Lỗi import điểm thi", e);
            result.addError("Lỗi hệ thống: " + e.getMessage());
        }

        return result;
    }

    public Page<DiemThi> getDanhSachDiemThi(String keyword, int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        if (keyword == null || keyword.trim().isEmpty()) {
            return diemThiRepository.findAll(pageable);
        }

        return diemThiRepository
                .findByThiSinh_CccdContainingOrThiSinh_HoTenContainingIgnoreCase(
                        keyword, keyword, pageable);
    }





@Transactional
public DiemThi updateDiemThi(String cccd, DiemThi diemMoi) {

    DiemThi existing = diemThiRepository.findByCccd(cccd)
            .orElseThrow(() -> new EntityNotFoundException(
                    "Không tìm thấy điểm thi của thí sinh: " + cccd));

    existing.setToan(diemMoi.getToan());
    existing.setVan(diemMoi.getVan());
    existing.setLy(diemMoi.getLy());
    existing.setHoa(diemMoi.getHoa());
    existing.setSinh(diemMoi.getSinh());
    existing.setSu(diemMoi.getSu());
    existing.setDia(diemMoi.getDia());
    existing.setAnh(diemMoi.getAnh());

    // năng khiếu
    existing.setNk1(diemMoi.getNk1());
    existing.setNk2(diemMoi.getNk2());
    existing.setNk3(diemMoi.getNk3());
    existing.setNk4(diemMoi.getNk4());
    existing.setNk5(diemMoi.getNk5());
    existing.setNk6(diemMoi.getNk6());
    existing.setNk7(diemMoi.getNk7());
    existing.setNk8(diemMoi.getNk8());

    return diemThiRepository.save(existing);
}


    @Transactional
    public void clearDiemThi(String cccd) {
        DiemThi existing = diemThiRepository.findByCccd(cccd)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Không tìm thấy điểm thi của thí sinh: " + cccd));

        existing.setToan(null);
        existing.setVan(null);
        existing.setLy(null);
        existing.setHoa(null);
        existing.setSinh(null);
        existing.setSu(null);
        existing.setDia(null);
        existing.setAnh(null);

        // năng khiếu
        existing.setNk1(null);
        existing.setNk2(null);
        existing.setNk3(null);
        existing.setNk4(null);
        existing.setNk5(null);
        existing.setNk6(null);
        existing.setNk7(null);
        existing.setNk8(null);

        diemThiRepository.save(existing);
}
}
