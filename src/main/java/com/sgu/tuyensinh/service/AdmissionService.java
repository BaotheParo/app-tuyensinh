package com.sgu.tuyensinh.service;

import com.sgu.tuyensinh.entity.Nganh;
import com.sgu.tuyensinh.entity.NguyenVong;
import com.sgu.tuyensinh.repository.NganhRepository;
import com.sgu.tuyensinh.repository.NguyenVongRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sgu.tuyensinh.repository.DiemThiRepository;
import com.sgu.tuyensinh.repository.DiemCongRepository;
import com.sgu.tuyensinh.repository.NganhToHopRepository;
import com.sgu.tuyensinh.entity.DiemCong;
import com.sgu.tuyensinh.entity.DiemThi;
import com.sgu.tuyensinh.entity.NganhToHop;
import com.sgu.tuyensinh.entity.ThiSinh;
import com.sgu.tuyensinh.service.dto.DiemXetTuyenDTO;
import com.sgu.tuyensinh.util.AppConstants;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service xử lý Thuật toán Xét tuyển Đại học (Lọc ảo).
 * Đảm nhận Task BE-1.5: Thuật toán gọi trúng tuyển, phân bổ nguyện vọng theo chỉ tiêu.
 */
@Service
public class AdmissionService {

    @Autowired
    private NguyenVongRepository nguyenVongRepository;

    @Autowired
    private NganhRepository nganhRepository;

    @Autowired
    private NganhToHopRepository nganhToHopRepository;

    @Autowired
    private BonusPointService bonusPointService;

    @Autowired
    private DiemCongRepository diemCongRepository;

    @Autowired
    private DiemThiRepository diemThiRepository;

    @Autowired
    private ScoringService scoringService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Chạy thuật toán Xét Tuyển và Lọc Ảo đồng loạt.
     */
    @Transactional
    public void runAdmissionProcess() {
        
        // BƯỚC 1: Reset Trạng thái và Tính điểm
        List<NguyenVong> danhSachTatCaNV = nguyenVongRepository.findAll();
        for (NguyenVong nv : danhSachTatCaNV) {
            nv.setNvKetQua("DANG_XET");
        }

        List<NganhToHop> allNth = nganhToHopRepository.findAll();
        Map<String, List<NganhToHop>> mapNthByNganh = allNth.stream()
                .collect(Collectors.groupingBy(NganhToHop::getMaNganh));

        List<DiemThi> allDiemThi = diemThiRepository.findAll();
        Map<String, DiemThi> mapDiemThiByCccd = allDiemThi.stream()
                .collect(Collectors.toMap(DiemThi::getCccd, dt -> dt, (existing, replacement) -> existing));

        List<DiemCong> allDiemCong = diemCongRepository.findAll();
        Map<String, List<DiemCong>> mapDiemCongByCccd = allDiemCong.stream()
                .collect(Collectors.groupingBy(DiemCong::getTsCccd));

        Map<String, Double> mapDcRawCache = new HashMap<>();

        for (NguyenVong nv : danhSachTatCaNV) {
            ThiSinh ts = nv.getThiSinh();
            if (ts == null) continue;
            
            String cccd = ts.getId();
            if (ts.getDiemThi() == null) {
                ts.setDiemThi(mapDiemThiByCccd.get(cccd));
            }

            Double dcRaw = mapDcRawCache.get(cccd);
            if (dcRaw == null) {
                dcRaw = calculateRawBonusPointInMemory(mapDiemCongByCccd.getOrDefault(cccd, Collections.emptyList()), cccd);
                mapDcRawCache.put(cccd, dcRaw);
            }

            List<NganhToHop> dsToHop = mapNthByNganh.getOrDefault(nv.getNvManganh(), Collections.emptyList());
            DiemXetTuyenDTO bestScore = null;
            
            for (NganhToHop nth : dsToHop) {
                String pt = "THPT";
                if (nth.getMaToHop() != null) {
                    if (nth.getMaToHop().toUpperCase().contains("DGNL")) pt = "DGNL";
                    else if (nth.getMaToHop().toUpperCase().contains("VSAT")) pt = "VSAT";
                }
                DiemXetTuyenDTO score = scoringService.calculateDetailedScore(ts, nth, pt, dcRaw);
                if (bestScore == null || score.getDiemXetTuyen() > bestScore.getDiemXetTuyen()) {
                    bestScore = score;
                }
            }

            if (bestScore != null) {
                nv.setDiemThxt(bestScore.getDiemThxt());
                nv.setDiemCong(bestScore.getDiemCong());
                nv.setDiemUtqd(bestScore.getDiemUtqd());
                nv.setDiemXetTuyen(bestScore.getDiemXetTuyen());
                nv.setTtThm(bestScore.getTtThm());
                nv.setTtPhuongthuc(bestScore.getPhuongThuc());
            } else {
                nv.setDiemXetTuyen(0.0);
                nv.setDiemThxt(0.0);
            }
        }

        // BƯỚC 2: Sắp xếp TOÀN CỤC (Global Sort)
        Comparator<NguyenVong> tieBreakerComparator = (nv1, nv2) -> {
            Double diemXT1 = nv1.getDiemXetTuyen() != null ? nv1.getDiemXetTuyen() : 0.0;
            Double diemXT2 = nv2.getDiemXetTuyen() != null ? nv2.getDiemXetTuyen() : 0.0;

            int soSanhDiem = diemXT2.compareTo(diemXT1);
            if (soSanhDiem != 0) return soSanhDiem;

            Double dthgxt1 = nv1.getDiemThxt() != null ? nv1.getDiemThxt() : 0.0;
            Double dthgxt2 = nv2.getDiemThxt() != null ? nv2.getDiemThxt() : 0.0;
            int soSanhDthgxt = dthgxt2.compareTo(dthgxt1);
            if (soSanhDthgxt != 0) return soSanhDthgxt;

            Integer ttNV1 = nv1.getNvTt() != null ? nv1.getNvTt() : Integer.MAX_VALUE;
            Integer ttNV2 = nv2.getNvTt() != null ? nv2.getNvTt() : Integer.MAX_VALUE;
            return ttNV1.compareTo(ttNV2);
        };

        danhSachTatCaNV.sort(tieBreakerComparator);

        Set<String> admittedCandidates = new HashSet<>();

        // BƯỚC 2.5: Giai đoạn 1 - Tuyển Thẳng (PT1)
        List<NguyenVong> dsTuyenThang = danhSachTatCaNV.stream()
                .filter(nv -> nv.getIsTuyenThang() != null && nv.getIsTuyenThang())
                .collect(Collectors.toList());

        for (NguyenVong nv : dsTuyenThang) {
            String cccd = nv.getNnCccd();
            if (!admittedCandidates.contains(cccd)) {
                nv.setNvKetQua("TRUNG_TUYEN");
                nv.setTtPhuongthuc("PT1");
                admittedCandidates.add(cccd);
            } else {
                nv.setNvKetQua("TRUOT");
            }
        }

        // BƯỚC 3: Lọc Ảo (Cắt chỉ tiêu)
        List<Nganh> danhSachNganh = nganhRepository.findAll();
        Map<String, Integer> mapChiTieuNganh = new HashMap<>();
        Map<String, Double> mapDiemSanNganh = new HashMap<>();
        for (Nganh nganh : danhSachNganh) {
            mapChiTieuNganh.put(nganh.getMaNganh(), nganh.getChiTieu() != null ? nganh.getChiTieu() : 0);
            mapDiemSanNganh.put(nganh.getMaNganh(), nganh.getDiemSan() != null ? nganh.getDiemSan().doubleValue() : 0.0);
        }

        for (NguyenVong nv : danhSachTatCaNV) {
            // Bỏ qua nếu đã được xử lý (trúng tuyển PT1 hoặc đã bị đánh rớt do đứng sau NV PT1)
            if (nv.getNvKetQua() != null && !nv.getNvKetQua().equals("DANG_XET")) continue;
            
            String cccd = nv.getNnCccd();
            String maNganh = nv.getNvManganh();
            int chiTieuConLai = mapChiTieuNganh.getOrDefault(maNganh, 0);
            double diemSan = mapDiemSanNganh.getOrDefault(maNganh, 0.0);
            double diemXT = nv.getDiemXetTuyen() != null ? nv.getDiemXetTuyen() : 0.0;
            double diemGoc = nv.getDiemThxt() != null ? nv.getDiemThxt() : 0.0;

            // ĐIỀU KIỆN TRÚNG TUYỂN: Không bị liệt (>0) và Đạt điểm sàn (>=diemSan)
            if (!admittedCandidates.contains(cccd) && chiTieuConLai > 0 && diemGoc > 0.0 && diemXT >= diemSan) {
                nv.setNvKetQua("TRUNG_TUYEN");
                admittedCandidates.add(cccd);
                mapChiTieuNganh.put(maNganh, chiTieuConLai - 1);
            } else {
                nv.setNvKetQua("TRUOT");
            }
        }

        nguyenVongRepository.saveAll(danhSachTatCaNV);
    }

    @Transactional
    public void resetResults() {
        List<NguyenVong> ds = nguyenVongRepository.findAll();
        for (NguyenVong nv : ds) {
            nv.setNvKetQua("DANG_XET");
            nv.setTtThm(null);
            nv.setTtPhuongthuc(null);
        }
        nguyenVongRepository.saveAll(ds);
    }

    private double calculateRawBonusPointInMemory(List<DiemCong> allDiemCong, String cccd) {
        double diemHSG = 0.0;
        double maxDiemNgoaiNgu = 0.0;

        for (DiemCong dc : allDiemCong) {
            String phuongThuc = (dc.getPhuongthuc() != null) ? dc.getPhuongthuc().toUpperCase() : "";
            if (phuongThuc.contains("NGOAINGU")) {
                String ngayCapStr = dc.getNgayCap();
                if (ngayCapStr != null && !ngayCapStr.isEmpty()) {
                    LocalDate ngayCap = parseLocalDateSafe(ngayCapStr, cccd);
                    if (ngayCap != null && !ngayCap.isBefore(AppConstants.EXPIRY_DATE)) {
                        double diemCC = (dc.getDiemCC() != null) ? dc.getDiemCC() : 0.0;
                        if (diemCC > maxDiemNgoaiNgu) maxDiemNgoaiNgu = diemCC;
                    }
                }
            } else {
                double diemUT = (dc.getDiemUtxt() != null) ? dc.getDiemUtxt() : 0.0;
                diemHSG += diemUT;
            }
        }
        return diemHSG + maxDiemNgoaiNgu;
    }

    private LocalDate parseLocalDateSafe(String dateStr, String cccd) {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;
        try {
            return LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (Exception e1) {
            try {
                return LocalDate.parse(dateStr);
            } catch (Exception e2) {
                return null;
            }
        }
    }
}
