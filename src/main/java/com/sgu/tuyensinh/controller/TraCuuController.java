package com.sgu.tuyensinh.controller;

import com.sgu.tuyensinh.entity.NguyenVong;
import com.sgu.tuyensinh.entity.ThiSinh;
import com.sgu.tuyensinh.entity.Nganh;
import com.sgu.tuyensinh.entity.NganhToHop;
import com.sgu.tuyensinh.repository.NguyenVongRepository;
import com.sgu.tuyensinh.repository.ThiSinhRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Controller tra cứu kết quả xét tuyển dành cho thí sinh.
 *
 * <p>Luồng hoạt động:</p>
 * <ol>
 *   <li>Thí sinh truy cập {@code /tra-cuu} → hiển thị form đăng nhập (CCCD + mật khẩu)</li>
 *   <li>POST {@code /tra-cuu} → xác thực bằng CCCD + ngày sinh (DDMMYYYY)</li>
 *   <li>Thành công → redirect tới {@code /tra-cuu/ket-qua?cccd=...} hiển thị danh sách nguyện vọng</li>
 * </ol>
 *
 * <p><b>Mật khẩu:</b> 8 chữ số theo định dạng {@code DDMMYYYY} trích từ trường
 * {@code ngay_sinh} của thí sinh trong DB.</p>
 */
@Controller
@RequiredArgsConstructor
public class TraCuuController {

    private final ThiSinhRepository thiSinhRepository;
    private final NguyenVongRepository nguyenVongRepository;

    // ── Formatter chuẩn để chuyển LocalDate → "ddMMyyyy" ──
    private static final DateTimeFormatter FMT_DDMMYYYY = DateTimeFormatter.ofPattern("ddMMyyyy");

    /**
     * GET /tra-cuu — Hiển thị trang đăng nhập.
     */
    @GetMapping("/tra-cuu")
    public String showLoginPage() {
        return "login"; // → templates/login.html
    }

    /**
     * POST /tra-cuu — Xử lý đăng nhập.
     *
     * <p>So sánh mật khẩu nhập vào với ngày sinh (DDMMYYYY) của thí sinh.</p>
     */
    @PostMapping("/tra-cuu")
    public String handleLogin(@RequestParam("cccd") String cccd,
                              @RequestParam("matKhau") String matKhau,
                              RedirectAttributes redirectAttributes) {

        // 1. Tìm thí sinh theo CCCD
        Optional<ThiSinh> optThiSinh = thiSinhRepository.findById(cccd.trim());

        if (optThiSinh.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy thí sinh với số CCCD: " + cccd);
            return "redirect:/tra-cuu";
        }

        ThiSinh thiSinh = optThiSinh.get();

        // 2. Lấy ngày sinh → tạo mật khẩu kỳ vọng (DDMMYYYY)
        String expectedPassword = buildPasswordFromNgaySinh(thiSinh.getNgaySinh());

        if (expectedPassword == null) {
            redirectAttributes.addFlashAttribute("error", "Thí sinh chưa có ngày sinh trong hệ thống. Vui lòng liên hệ ban tuyển sinh.");
            return "redirect:/tra-cuu";
        }

        // 3. So khớp mật khẩu
        if (!expectedPassword.equals(matKhau.trim())) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu không đúng. Mật khẩu là 8 chữ số ngày sinh (DDMMYYYY).");
            redirectAttributes.addFlashAttribute("cccd", cccd); // giữ lại giá trị CCCD
            return "redirect:/tra-cuu";
        }

        // 4. Đăng nhập thành công → redirect kèm CCCD
        return "redirect:/tra-cuu/ket-qua?cccd=" + cccd.trim();
    }

    /**
     * GET /tra-cuu/ket-qua — Hiển thị kết quả xét tuyển.
     */
    @GetMapping("/tra-cuu/ket-qua")
    public String showKetQua(@RequestParam("cccd") String cccd, Model model) {
        // Tìm thí sinh
        Optional<ThiSinh> optThiSinh = thiSinhRepository.findById(cccd.trim());
        if (optThiSinh.isEmpty()) {
            return "redirect:/tra-cuu";
        }

        ThiSinh thiSinh = optThiSinh.get();

        // Lấy danh sách nguyện vọng (sắp xếp theo thứ tự NV)
        List<NguyenVong> danhSachNV = nguyenVongRepository.findAll().stream()
                .filter(nv -> cccd.trim().equals(nv.getNnCccd()))
                .sorted((a, b) -> Integer.compare(
                        a.getNvTt() != null ? a.getNvTt() : 999,
                        b.getNvTt() != null ? b.getNvTt() : 999))
                .toList();

        // Kiểm tra có ít nhất 1 NV trúng tuyển không
        boolean coTrungTuyen = danhSachNV.stream()
                .anyMatch(nv -> "TRUNG_TUYEN".equals(nv.getNvKetQua()));

        // Bind dữ liệu vào Model
        model.addAttribute("thiSinh", thiSinh);
        model.addAttribute("danhSachNV", danhSachNV);
        model.addAttribute("coTrungTuyen", coTrungTuyen);

        return "ketqua"; // → templates/ketqua.html
    }

    private final com.sgu.tuyensinh.repository.NganhRepository nganhRepository;
    private final com.sgu.tuyensinh.repository.NganhToHopRepository nganhToHopRepository;
    private final com.sgu.tuyensinh.service.ScoringService scoringService;

    /**
     * GET /tra-cuu/chi-tiet — Xem chi tiết cách tính điểm của từng nguyện vọng.
     * Nếu không có cccd, hiển thị form nhập.
     */
    @GetMapping("/tra-cuu/chi-tiet")
    public String showDetailedKetQua(@RequestParam(value = "cccd", required = false) String cccd, Model model) {
        if (cccd == null || cccd.trim().isEmpty()) {
            return "tra-cuu-chi-tiet-search"; // Trang nhập CCCD để xem chi tiết
        }
        
        Optional<ThiSinh> optThiSinh = thiSinhRepository.findById(cccd.trim());
        if (optThiSinh.isEmpty()) {
            model.addAttribute("error", "Không tìm thấy thí sinh với mã: " + cccd);
            return "tra-cuu-chi-tiet-search";
        }

        ThiSinh thiSinh = optThiSinh.get();
        List<NguyenVong> danhSachNV = nguyenVongRepository.findAll().stream()
                .filter(nv -> cccd.trim().equals(nv.getNnCccd()))
                .sorted((a, b) -> Integer.compare(a.getNvTt() != null ? a.getNvTt() : 999, b.getNvTt() != null ? b.getNvTt() : 999))
                .toList();

        // Tạo cấu trúc dữ liệu để chứa chi tiết điểm của từng nguyện vọng
        // Map<NV_TT, List<DiemXetTuyenDTO>>
        java.util.Map<Integer, List<com.sgu.tuyensinh.service.dto.DiemXetTuyenDTO>> chiTietDiemMap = new java.util.LinkedHashMap<>();
        java.util.Map<Integer, String> tenNganhMap = new java.util.HashMap<>();

        for (NguyenVong nv : danhSachNV) {
            String maNganh = nv.getNvManganh();
            Optional<Nganh> optNganh = nganhRepository.findById(maNganh);
            if (optNganh.isPresent()) {
                Nganh nganh = optNganh.get();
                tenNganhMap.put(nv.getNvTt(), nganh.getTenNganh());
                
                List<NganhToHop> dsToHop = nganhToHopRepository.findByMaNganh(maNganh);

                List<com.sgu.tuyensinh.service.dto.DiemXetTuyenDTO> detailedScores = new java.util.ArrayList<>();
                java.util.Set<String> processedKeys = new java.util.HashSet<>(); // Để tránh trùng lặp tổ hợp+phương thức
                
                // Các phương thức được phép của ngành
                String[] phuongThucs = {"THPT", "DGNL", "VSAT"};
                for (String pt : phuongThucs) {
                    // Kiểm tra flag phương thức trong ngành (mặc định là Y nếu null để test)
                    boolean isAllowed = switch (pt) {
                        case "THPT" -> nganh.getThpt() == null || "Y".equalsIgnoreCase(nganh.getThpt());
                        case "DGNL" -> "Y".equalsIgnoreCase(nganh.getDgnl());
                        case "VSAT" -> "Y".equalsIgnoreCase(nganh.getVsat());
                        default -> false;
                    };

                    if (isAllowed) {
                        for (NganhToHop nth : dsToHop) {
                            String key = pt + "_" + nth.getMaToHop();
                            if (processedKeys.contains(key)) continue;
                            
                            com.sgu.tuyensinh.service.dto.DiemXetTuyenDTO dto = scoringService.calculateDetailedScore(thiSinh, nth, pt);
                            // Chỉ thêm nếu có điểm thô > 0 (không bị liệt môn)
                            if (dto.getDiemThxt() != null && dto.getDiemThxt() > 0) {
                                detailedScores.add(dto);
                                processedKeys.add(key);
                            }
                        }
                    }
                }
                // Sắp xếp điểm giảm dần
                detailedScores.sort((a, b) -> Double.compare(b.getDiemXetTuyen(), a.getDiemXetTuyen()));
                chiTietDiemMap.put(nv.getNvTt(), detailedScores);
            }
        }

        model.addAttribute("thiSinh", thiSinh);
        model.addAttribute("danhSachNV", danhSachNV);
        model.addAttribute("chiTietDiemMap", chiTietDiemMap);
        model.addAttribute("tenNganhMap", tenNganhMap);

        return "tra-cuu-chi-tiet";
    }

    // ── Helper ───────────────────────────────────────────────────

    /**
     * Chuyển {@link LocalDate} (hoặc String ngày sinh) → chuỗi "ddMMyyyy".
     * Hỗ trợ cả trường hợp ngày sinh là String có dấu / hoặc -.
     */
    private String buildPasswordFromNgaySinh(Object ngaySinh) {
        if (ngaySinh == null) return null;

        if (ngaySinh instanceof LocalDate localDate) {
            return localDate.format(FMT_DDMMYYYY);
        }

        // Fallback: nếu ngày sinh được lưu dưới dạng String
        String raw = ngaySinh.toString().trim();
        if (raw.isEmpty()) return null;

        // Xóa dấu "/" và "-" → kỳ vọng kết quả 8 ký tự (DDMMYYYY)
        return raw.replaceAll("[/\\-]", "");
    }
}
