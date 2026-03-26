package com.sgu.tuyensinh.service;

import com.sgu.tuyensinh.dto.ThiSinhImportDTO;
import com.sgu.tuyensinh.entity.ThiSinh;
import com.sgu.tuyensinh.repository.ThiSinhRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ThiSinhImportService {

    private final ThiSinhRepository repository;

    public ThiSinhImportService(ThiSinhRepository repository) {
        this.repository = repository;
    }

    // ====== VALIDATE ======
    public List<String> validate(ThiSinhImportDTO dto) {
        List<String> errors = new ArrayList<>();

        if (dto.getCccd() == null || dto.getCccd().isEmpty()) {
            errors.add("CCCD bị trống");
        }

        if (dto.getHoTen() == null || dto.getHoTen().isEmpty()) {
            errors.add("Họ tên bị trống");
        }

        return errors;
    }   

    // ====== MAP DTO -> ENTITY ======
    public ThiSinh toEntity(ThiSinhImportDTO dto) {
        // return ThiSinh.builder()
        //         .id(dto.getCccd())
        //         .hoTen(dto.getHoTen())
        //         .gioiTinh(dto.getGioiTinh())
        //         .maTinh(dto.getMaTinh())
        //         .maTruong(dto.getMaTruong())
        //         .doiTuongUt(dto.getDoiTuongUt())
        //         .khuVucUt(dto.getKhuVucUt())
        //         .ngaySinh(parseDate(dto.getNgaySinh()))
        //         .build();
        ThiSinh ts = new ThiSinh();
        ts.setId(dto.getCccd());
        ts.setHoTen(dto.getHoTen());
        ts.setGioiTinh(dto.getGioiTinh());
        ts.setMaTinh(dto.getMaTinh());
        ts.setMaTruong(dto.getMaTruong());
        ts.setDoiTuongUt(dto.getDoiTuongUt());
        ts.setKhuVucUt(dto.getKhuVucUt());
        ts.setNgaySinh(parseDate(dto.getNgaySinh()));
        return ts;
    }

    private LocalDate parseDate(String date) {
        try {
            return LocalDate.parse(date, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (Exception e) {
            return null;
        }
    }

    // ====== SAVE LIST ======
    public void saveAll(List<ThiSinh> list) {
        repository.saveAll(list);
    }
}