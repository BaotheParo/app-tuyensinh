package com.sgu.tuyensinh.service;

import com.sgu.tuyensinh.dto.NguyenVongImportDTO;
import com.sgu.tuyensinh.entity.NguyenVong;
import com.sgu.tuyensinh.repository.NguyenVongRepository;

import java.util.ArrayList;
import java.util.List;

public class NguyenVongImportService {

    private final NguyenVongRepository repository;

    public NguyenVongImportService(NguyenVongRepository repository) {
        this.repository = repository;
    }

    // ===== VALIDATE =====
    public List<String> validate(NguyenVongImportDTO dto) {
        List<String> errors = new ArrayList<>();

        if (dto.getCccd() == null || dto.getCccd().isEmpty()) {
            errors.add("CCCD trống");
        }

        if (dto.getMaNganh() == null || dto.getMaNganh().isEmpty()) {
            errors.add("Mã ngành trống");
        }

        if (dto.getThuTu() == null) {
            errors.add("Thứ tự NV trống");
        }

        return errors;
    }

    // ===== MAP DTO -> ENTITY =====
    public NguyenVong toEntity(NguyenVongImportDTO dto) {
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

        nv.setNvKetQua(null); // chưa xét

        // key unique (tùy bạn định nghĩa)
        nv.setNvKeys(dto.getCccd() + "_" + dto.getMaNganh() + "_" + dto.getThuTu());

        return nv;
    }

    // ===== SAVE =====
    public void saveAll(List<NguyenVong> list) {
        repository.saveAll(list);
    }
}