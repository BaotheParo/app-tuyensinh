package com.sgu.tuyensinh.service.impl;

import com.sgu.tuyensinh.dto.ToHopMonImportDTO;
import com.sgu.tuyensinh.entity.ToHop;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Override
    @Transactional
    public ImportResultDTO importFromExcel(InputStream inputStream) {
        ImportResultDTO result = new ImportResultDTO();

        if (inputStream == null) {
            throw new IllegalArgumentException("InputStream không được để null");
        }

        List<ToHop> toHopToSave = new ArrayList<>();
        Set<String> maToHopSet = new HashSet<>();

        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                throw new RuntimeException("File Excel không có sheet nào");
            }

            int lastRowNum = sheet.getLastRowNum();
            if (lastRowNum < 1) {
                log.warn("File Excel không có dữ liệu (chỉ có header hoặc rỗng)");
                return result;
            }

            // Đọc dữ liệu từ Excel
            for (int i = 1; i <= lastRowNum; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                ToHopMonImportDTO dto = readRowToDto(row, i + 1);

                if (!isValidToHopDto(dto)) {
                    log.warn("Dòng {} bỏ qua do dữ liệu không hợp lệ: maToHop={}", i + 1, dto.getMaToHop());
                    result.incrementSkip();
                    continue;
                }

                if (!maToHopSet.add(dto.getMaToHop())) {
                    log.warn("Mã tổ hợp {} bị trùng trong file Excel, bỏ qua dòng {}", dto.getMaToHop(), i + 1);
                    result.incrementSkip();
                    continue;
                }

                toHopToSave.add(convertToEntity(dto));
            }

            if (toHopToSave.isEmpty()) {
                log.info("Không có dữ liệu hợp lệ nào để import");
                return result;
            }

            // Lọc những mã chưa tồn tại trong DB
            Set<String> existingMaToHop = toHopRepository.findExistingMaToHop(
                    toHopToSave.stream().map(ToHop::getMaToHop).toList());

            toHopToSave.removeIf(toHop -> existingMaToHop.contains(toHop.getMaToHop()));

            if (toHopToSave.isEmpty()) {
                log.info("Tất cả mã tổ hợp đã tồn tại trong database");
                return result;
            }

            // Save batch
            toHopRepository.saveAll(toHopToSave);
            toHopToSave.forEach(t -> result.incrementSuccess());
            log.info("Import thành công {} tổ hợp mới từ Excel", toHopToSave.size());

        } catch (Exception e) {
            log.error("Lỗi khi import tổ hợp từ Excel", e);
            throw new RuntimeException("Lỗi import tổ hợp từ Excel", e);
        }

        return result;
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

    private boolean isValidToHopDto(ToHopMonImportDTO dto) {
        return dto.getMaToHop() != null && !dto.getMaToHop().trim().isEmpty()
                && dto.getTenToHop() != null && !dto.getTenToHop().trim().isEmpty()
                && dto.getMon1() != null && !dto.getMon1().trim().isEmpty()
                && dto.getMon2() != null && !dto.getMon2().trim().isEmpty()
                && dto.getMon3() != null && !dto.getMon3().trim().isEmpty();
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
}