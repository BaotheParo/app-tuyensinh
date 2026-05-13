package com.sgu.tuyensinh.controller;

import com.sgu.tuyensinh.entity.DiemThi;
import com.sgu.tuyensinh.entity.Nganh;
import com.sgu.tuyensinh.entity.NganhToHop;
import com.sgu.tuyensinh.entity.ThiSinh;
import com.sgu.tuyensinh.repository.NganhRepository;
import com.sgu.tuyensinh.repository.NganhToHopRepository;
import com.sgu.tuyensinh.service.ScoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Controller giả lập tính điểm xét tuyển.
 *
 * <p>Cho phép thí sinh tự nhập điểm để xem mình đạt ngưỡng hay không,
 * mà KHÔNG lưu bất kỳ dữ liệu nào vào DB.</p>
 *
 * <p>Truy cập: <a href="http://localhost:8080/gia-lap">http://localhost:8080/gia-lap</a></p>
 */
@Controller
@RequiredArgsConstructor
public class TinhDiemController {

    private final NganhRepository nganhRepository;
    private final NganhToHopRepository nganhToHopRepository;
    private final ScoringService scoringService;

    // ═══════════════════════════════════════════════════════════
    //  GET /gia-lap — Hiển thị form nhập điểm
    // ═══════════════════════════════════════════════════════════

    @GetMapping("/gia-lap")
    public String showForm(Model model) {
        List<Nganh> danhSachNganh = nganhRepository.findAll();
        model.addAttribute("danhSachNganh", danhSachNganh);
        return "gia-lap"; // → templates/gia-lap.html
    }

    // ═══════════════════════════════════════════════════════════
    //  AJAX: Lấy danh sách tổ hợp theo mã ngành
    // ═══════════════════════════════════════════════════════════

    @GetMapping("/api/to-hop-theo-nganh")
    @ResponseBody
    public List<NganhToHop> getToHopTheoNganh(@RequestParam("maNganh") String maNganh) {
        return nganhToHopRepository.findAll().stream()
                .filter(nth -> maNganh.equals(nth.getMaNganh()))
                .toList();
    }

    // ═══════════════════════════════════════════════════════════
    //  POST /gia-lap — Tính điểm và trả kết quả
    // ═══════════════════════════════════════════════════════════

    @PostMapping("/gia-lap")
    public String tinhDiem(
            @RequestParam("maNganh") String maNganh,
            @RequestParam("toHopId") Integer toHopId,
            @RequestParam("phuongThuc") String phuongThuc,
            @RequestParam("diem1") Double diem1,
            @RequestParam("diem2") Double diem2,
            @RequestParam("diem3") Double diem3,
            @RequestParam(value = "doiTuongUt", required = false) String doiTuongUt,
            @RequestParam(value = "khuVucUt", required = false) String khuVucUt,
            Model model) {

        // ── 1. Load ngành & tổ hợp từ DB ──
        Optional<Nganh> optNganh = nganhRepository.findById(maNganh);
        Optional<NganhToHop> optNth = nganhToHopRepository.findById(toHopId);

        if (optNganh.isEmpty() || optNth.isEmpty()) {
            model.addAttribute("error", "Không tìm thấy ngành hoặc tổ hợp. Vui lòng thử lại.");
            return showForm(model);
        }

        Nganh nganh = optNganh.get();
        NganhToHop nth = optNth.get();

        // ── 2. Tạo ThiSinh ảo + DiemThi ảo (KHÔNG lưu DB) ──
        ThiSinh tsAo = ThiSinh.builder()
                .id("GIA_LAP_" + System.currentTimeMillis()) // id ảo
                .hoTen("Thí sinh giả lập")
                .doiTuongUt(doiTuongUt)
                .khuVucUt(khuVucUt)
                .build();

        DiemThi diemThiAo = buildDiemThiAo(nth, diem1, diem2, diem3, phuongThuc);
        tsAo.setDiemThi(diemThiAo);

        // ── 3. Gọi ScoringService để tính điểm ──
        Double tongDiem = scoringService.calculateFinalScore(tsAo, nth);

        // ── 4. So sánh với điểm sàn ──
        BigDecimal diemSan = nganh.getDiemSan() != null ? nganh.getDiemSan() : BigDecimal.ZERO;
        boolean datNguong = tongDiem >= diemSan.doubleValue();

        // ── 5. Tính điểm thành phần để hiển thị ──
        double diemQuyDoi = calculateComboRaw(diem1, diem2, diem3, nth, phuongThuc);
        double diemUuTien = calculateUuTien(doiTuongUt, khuVucUt);
        double diemCong = 0.0; // Giả lập không có điểm cộng HSG/CC

        // ── 6. Bind dữ liệu vào Model ──
        model.addAttribute("danhSachNganh", nganhRepository.findAll());
        model.addAttribute("coKetQua", true);
        model.addAttribute("nganh", nganh);
        model.addAttribute("toHop", nth);
        model.addAttribute("phuongThuc", phuongThuc);
        model.addAttribute("diem1", diem1);
        model.addAttribute("diem2", diem2);
        model.addAttribute("diem3", diem3);
        model.addAttribute("doiTuongUt", doiTuongUt);
        model.addAttribute("khuVucUt", khuVucUt);

        model.addAttribute("diemQuyDoi", Math.round(diemQuyDoi * 100.0) / 100.0);
        model.addAttribute("diemCong", diemCong);
        model.addAttribute("diemUuTien", Math.round(diemUuTien * 100.0) / 100.0);
        model.addAttribute("tongDiem", tongDiem);
        model.addAttribute("diemSan", diemSan);
        model.addAttribute("datNguong", datNguong);

        return "gia-lap";
    }

    // ═══════════════════════════════════════════════════════════
    //  Helpers (tính toán nội bộ, không lưu DB)
    // ═══════════════════════════════════════════════════════════

    /**
     * Tạo DiemThi ảo bằng cách gán điểm vào đúng slot môn theo tổ hợp.
     */
    private DiemThi buildDiemThiAo(NganhToHop nth, Double d1, Double d2, Double d3, String phuongThuc) {
        DiemThi dt = new DiemThi();
        setScoreBySubject(dt, nth.getThMon1(), d1);
        setScoreBySubject(dt, nth.getThMon2(), d2);
        setScoreBySubject(dt, nth.getThMon3(), d3);
        return dt;
    }

    private void setScoreBySubject(DiemThi dt, String subject, Double score) {
        if (subject == null || score == null) return;
        switch (subject.toUpperCase()) {
            case "TOAN" -> dt.setToan(score);
            case "VAN"  -> dt.setVan(score);
            case "LY"   -> dt.setLy(score);
            case "HOA"  -> dt.setHoa(score);
            case "SINH" -> dt.setSinh(score);
            case "SU"   -> dt.setSu(score);
            case "DIA"  -> dt.setDia(score);
            case "ANH"  -> dt.setAnh(score);
            case "NK1"  -> dt.setNk1(score);
            case "NK2"  -> dt.setNk2(score);
            case "NK3"  -> dt.setNk3(score);
            case "NK4"  -> dt.setNk4(score);
        }
    }

    /**
     * Tính điểm tổ hợp thô (quy đổi về thang 30) để hiển thị cho thí sinh.
     */
    private double calculateComboRaw(Double d1, Double d2, Double d3, NganhToHop nth, String phuongThuc) {
        double s1 = d1 != null ? d1 : 0;
        double s2 = d2 != null ? d2 : 0;
        double s3 = d3 != null ? d3 : 0;

        // Quy đổi nếu không phải THPT
        if ("DGNL".equalsIgnoreCase(phuongThuc)) {
            s1 = (s1 / 1200.0) * 10.0;
            s2 = (s2 / 1200.0) * 10.0;
            s3 = (s3 / 1200.0) * 10.0;
        } else if ("VSAT".equalsIgnoreCase(phuongThuc)) {
            s1 = (s1 / 150.0) * 10.0;
            s2 = (s2 / 150.0) * 10.0;
            s3 = (s3 / 150.0) * 10.0;
        }

        double w1 = nth.getHsMon1() != null ? nth.getHsMon1() : 1.0;
        double w2 = nth.getHsMon2() != null ? nth.getHsMon2() : 1.0;
        double w3 = nth.getHsMon3() != null ? nth.getHsMon3() : 1.0;
        double tongHeSo = w1 + w2 + w3;

        return ((s1 * w1) + (s2 * w2) + (s3 * w3)) / tongHeSo * 3;
    }

    /**
     * Tính điểm ưu tiên (giống ScoringService.calculateBaseUT).
     */
    private double calculateUuTien(String doiTuong, String khuVuc) {
        double ut = 0.0;
        if (doiTuong != null && !doiTuong.isEmpty()) {
            switch (doiTuong) {
                case "01", "04" -> ut += 2.0;
                case "02"       -> ut += 1.5;
                case "03"       -> ut += 1.0;
                case "05", "06a"-> ut += 0.5;
                case "07"       -> ut += 0.25;
            }
        }
        if (khuVuc != null && !khuVuc.isEmpty()) {
            switch (khuVuc.toUpperCase()) {
                case "KV1"   -> ut += 0.75;
                case "KV2NT" -> ut += 0.5;
                case "KV2"   -> ut += 0.25;
            }
        }
        return ut;
    }
}
