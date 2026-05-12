package com.sgu.tuyensinh.service;

import com.sgu.tuyensinh.dto.NganhToHopImportDTO;
import com.sgu.tuyensinh.entity.NganhToHop;
import com.sgu.tuyensinh.repository.NganhToHopRepository;
import com.sgu.tuyensinh.service.dto.ImportResultDTO;
import com.sgu.tuyensinh.service.interfaces.IImportService;
import com.sgu.tuyensinh.service.interfaces.ProgressCallback;

import com.sgu.tuyensinh.util.ExcelReaderUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class NganhToHopImportService implements IImportService {

    private final NganhToHopRepository nganhToHopRepository;

    // Regex parse: B03(TO-3,VA-3,SI-1)
    private static final Pattern MA_TO_HOP_PATTERN = Pattern.compile(
            "([A-Z0-9]+)\\s*\\(\\s*([A-Z0-9]+)-(\\d+)\\s*,\\s*([A-Z0-9]+)-(\\d+)\\s*,\\s*([A-Z0-9]+)-(\\d+)\\s*\\)",
            Pattern.CASE_INSENSITIVE);

    // @Override
    // @Transactional
    // public ImportResultDTO importFromExcel(InputStream inputStream) {
    //     ImportResultDTO result = new ImportResultDTO();

    //     try (Workbook workbook = WorkbookFactory.create(inputStream)) {
    //         Sheet sheet = workbook.getSheetAt(0);
    //         if (sheet == null || sheet.getLastRowNum() < 1) return result;

    //         List<NganhToHop> entitiesToSave = new ArrayList<>();

    //         for (int i = 1; i <= sheet.getLastRowNum(); i++) {
    //             Row row = sheet.getRow(i);
    //             if (row == null) continue;

    //             // Sử dụng DTO để đọc data
    //             NganhToHopImportDTO dto = readRowToDto(row);

    //             // Filter: Chỉ lấy dòng có chữ "Gốc"
    //             if (dto.getGoc() == null || !dto.getGoc().equalsIgnoreCase("Gốc")) continue;
    //             if (dto.getMaNganh() == null || dto.getMaToHop() == null) continue;

    //             entitiesToSave.add(convertToEntity(dto));
    //         }

    //         if (!entitiesToSave.isEmpty()) {
    //             nganhToHopRepository.saveAll(entitiesToSave);
    //             entitiesToSave.forEach(e -> result.incrementSuccess());
    //             log.info("Import thành công {} bản ghi.", entitiesToSave.size());
    //         }
    //     } catch (Exception e) {
    //         log.error("Lỗi import Excel: ", e);
    //         result.addError("Lỗi hệ thống khi đọc file Excel: " + e.getMessage());
    //     }

    //     return result;
    // }
@Transactional
public ImportResultDTO importFromExcel(InputStream inputStream, ProgressCallback callback) {
    ImportResultDTO result = new ImportResultDTO();
    try (Workbook workbook = WorkbookFactory.create(inputStream)) {
        Sheet sheet = workbook.getSheetAt(0);
        if (sheet == null || sheet.getLastRowNum() < 1) return result;

        List<NganhToHop> entitiesToSave = new ArrayList<>();

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            NganhToHopImportDTO dto = readRowToDto(row);

            if (dto.getGoc() == null || !dto.getGoc().equalsIgnoreCase("Gốc")) continue;
            if (dto.getMaNganh() == null || dto.getMaToHop() == null) continue;

            entitiesToSave.add(convertToEntity(dto));
        }

        if (!entitiesToSave.isEmpty()) {
            // Thay vì saveAll, ta save từng cái trong try-catch để xem cái nào chết
            for (NganhToHop entity : entitiesToSave) {
                try {
                    nganhToHopRepository.saveAndFlush(entity); 
                    result.incrementSuccess();
                } catch (Exception e) {
                    log.error("💥 LỖI TẠI DÒNG CÓ MÃ NGÀNH: {} - MÃ TỔ HỢP: {}", 
                              entity.getMaNganh(), entity.getMaToHop());
                    throw e; // Ném ra để thấy log chi tiết trong Console
                }
            }
        }
    } catch (Exception e) {
        log.error("Lỗi import Excel: ", e);
        throw new RuntimeException(e); // Quan trọng: throw để JUnit bắt được
    }
    return result;
}
    private NganhToHopImportDTO readRowToDto(Row row) {
        return NganhToHopImportDTO.builder()
                .maNganh(ExcelReaderUtil.getSafeString(row.getCell(1)))
                .tenNganh(ExcelReaderUtil.getSafeString(row.getCell(2)))
                .maToHop(ExcelReaderUtil.getSafeString(row.getCell(3)))
                .tbKeys(ExcelReaderUtil.getSafeString(row.getCell(4)))
                .tenToHop(ExcelReaderUtil.getSafeString(row.getCell(5)))
                .goc(ExcelReaderUtil.getSafeString(row.getCell(6)))
                .doLech(ExcelReaderUtil.getSafeBigDecimal(row.getCell(7)))
                .build();
    }

    private NganhToHop convertToEntity(NganhToHopImportDTO dto) {
        NganhToHop entity = new NganhToHop();
        entity.setMaNganh(dto.getMaNganh().trim());
        entity.setTbKeys(dto.getTbKeys());

        Matcher matcher = MA_TO_HOP_PATTERN.matcher(dto.getMaToHop().trim());
        if (matcher.matches()) {
            entity.setMaToHop(matcher.group(1).trim());

            // Set thông tin 3 môn chính
            entity.setThMon1(matcher.group(2).trim());
            entity.setHsMon1(Double.parseDouble(matcher.group(3)));

            entity.setThMon2(matcher.group(4).trim());
            entity.setHsMon2(Double.parseDouble(matcher.group(5)));

            entity.setThMon3(matcher.group(6).trim());
            entity.setHsMon3(Double.parseDouble(matcher.group(7)));

            // Gán giá trị 1.0 cho các field môn học tương ứng (Dùng helper để tránh if-else)
            List<String> monHocList = Arrays.asList(entity.getThMon1(), entity.getThMon2(), entity.getThMon3());
            assignSubjectValues(entity, monHocList);
        } else {
            entity.setMaToHop(dto.getMaToHop());
        }
        return entity;
    }

    private void assignSubjectValues(NganhToHop entity, List<String> monHocs) {
        // Duyệt qua danh sách môn học đã parse được, nếu trùng mã nào thì set field đó = 1.0
        monHocs.forEach(mon -> {
            switch (mon.toUpperCase()) {
                case "TO": entity.setTo(1.0); break;
                case "VA": entity.setVa(1.0); break;
                case "LI": entity.setLi(1.0); break;
                case "HO": entity.setHo(1.0); break;
                case "SI": entity.setSi(1.0); break;
                case "SU": entity.setSu(1.0); break;
                case "DI": entity.setDi(1.0); break;
                case "TI": entity.setTi(1.0); break;
                case "N1": entity.setN1(1.0); break;
                case "KTPL": entity.setKtpl(1.0); break;
                case "KHAC": entity.setKhac(1.0); break;
            }
        });
    }
}