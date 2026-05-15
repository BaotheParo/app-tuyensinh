package com.sgu.tuyensinh.service;

import com.sgu.tuyensinh.entity.Nganh;
import com.sgu.tuyensinh.entity.NguyenVong;
import com.sgu.tuyensinh.repository.DiemThiRepository;
import com.sgu.tuyensinh.repository.NganhRepository;
import com.sgu.tuyensinh.repository.NguyenVongRepository;
import com.sgu.tuyensinh.service.interfaces.IImportService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BaoCaoServiceImpl  {

        private final NguyenVongRepository nguyenVongRepository;
        private final NganhRepository nganhRepository;
        private final DiemThiRepository diemThiRepository;

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Map<String, Long> thongKeDangKyTheoNganh() {
        List<Object[]> counts = nguyenVongRepository.countByMaNganh();
        Map<String, String> mapNganh = nganhRepository.findAll().stream()
                .collect(Collectors.toMap(Nganh::getMaNganh, Nganh::getTenNganh));

        return counts.stream()
                .collect(Collectors.toMap(
                        row -> mapNganh.getOrDefault(row[0].toString(), row[0].toString()),
                        row -> (Long) row[1]
                ));
    }

    public List<NganhThongKeDTO> thongKeChiTietTheoNganh() {
        Map<String, Long> thongKe = thongKeDangKyTheoNganh();
        return nganhRepository.findAll().stream()
                .map(nganh -> new NganhThongKeDTO(
                        nganh.getMaNganh(),
                        nganh.getTenNganh(),
                        thongKe.getOrDefault(nganh.getTenNganh(), 0L)
                ))
                .collect(Collectors.toList());
    }

    public record NganhThongKeDTO(String maNganh, String tenNganh, Long soLuongDangKy) {
    }

    public double[] getDiemMonHoc(String monHoc) {
        if (monHoc == null) return new double[0];
        String key = monHoc.trim().toUpperCase();
        List<Double> dsDiem;
        switch (key) {
            case "TOÁN", "TOAN" -> dsDiem = diemThiRepository.findAllDiemToan();
            case "VĂN", "VAN" -> dsDiem = diemThiRepository.findAllDiemVan();
            case "LÝ", "LY" -> dsDiem = diemThiRepository.findAllDiemLy();
            case "HÓA", "HOA" -> dsDiem = diemThiRepository.findAllDiemHoa();
            case "SINH" -> dsDiem = diemThiRepository.findAllDiemSinh();
            case "SỬ", "SU" -> dsDiem = diemThiRepository.findAllDiemSu();
            case "ĐỊA", "DIA" -> dsDiem = diemThiRepository.findAllDiemDia();
            case "ANH", "NGOAINGU" -> dsDiem = diemThiRepository.findAllDiemAnh();
            case "NK1", "VSAT", "V-SAT" -> dsDiem = diemThiRepository.findAllDiemNk1();
            case "NK2", "DGNL", "ĐGNL" -> dsDiem = diemThiRepository.findAllDiemNk2();
            case "NK3" -> dsDiem = diemThiRepository.findAllDiemNk3();
            case "NK4" -> dsDiem = diemThiRepository.findAllDiemNk4();
            case "NK5" -> dsDiem = diemThiRepository.findAllDiemNk5();
            case "NK6" -> dsDiem = diemThiRepository.findAllDiemNk6();
            case "NK7" -> dsDiem = diemThiRepository.findAllDiemNk7();
            case "NK8" -> dsDiem = diemThiRepository.findAllDiemNk8();
            default -> dsDiem = List.of();
        }
        return dsDiem.stream().filter(d -> d != null).mapToDouble(Double::doubleValue).toArray();
    }

    public List<String> getDanhSachMonHoc() {
        return List.of("Toán", "Văn", "Lý", "Hóa", "Sinh", "Sử", "Địa", "Anh", "NK1", "NK2");
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Map<String, KetQuaTheoNganhDTO> thongKeKetQuaTheoNganh() {
        // Tối ưu: Lấy danh sách trúng tuyển và rớt theo mã ngành bằng 1 câu query duy nhất
        List<Nganh> dsNganh = nganhRepository.findAll();
        Map<String, String> mapNganh = dsNganh.stream().collect(Collectors.toMap(Nganh::getMaNganh, Nganh::getTenNganh));
        
        Map<String, KetQuaTheoNganhDTO> resultMap = new java.util.HashMap<>();
        
        // Mocking grouping logic via counts to avoid loading 50k objects
        for (Nganh n : dsNganh) {
            long soDau = nguyenVongRepository.countByMaNganhAndKetQua(n.getMaNganh(), "TRUNG_TUYEN");
            long soRot = nguyenVongRepository.countByMaNganhAndKetQua(n.getMaNganh(), "TRUOT");
            resultMap.put(n.getTenNganh(), new KetQuaTheoNganhDTO(n.getMaNganh(), n.getTenNganh(), soDau, soRot));
        }
        return resultMap;
    }

    public record KetQuaTheoNganhDTO(String maNganh, String tenNganh, long soDau, long soRot) {
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Object[][] getDanhSachTrungTuyen(String filterNganh) {
        // Tối ưu: Chỉ lấy TOP 100 trúng tuyển để hiển thị trên bảng (Tránh treo UI)
        // Nếu user muốn xem hết thì sẽ dùng chức năng xuất Excel
        List<NguyenVong> dsTrungTuyen;
        if (filterNganh == null || filterNganh.isEmpty() || "Tất cả".equals(filterNganh)) {
            dsTrungTuyen = nguyenVongRepository.findTop100ByNvKetQuaOrderByDiemXetTuyenDesc("TRUNG_TUYEN");
        } else {
            dsTrungTuyen = nguyenVongRepository.findTop100ByNvManganhAndNvKetQuaOrderByDiemXetTuyenDesc(filterNganh, "TRUNG_TUYEN");
            if (dsTrungTuyen.isEmpty()) {
                // Thử tìm theo tên ngành nếu mã ngành không khớp
                Nganh n = nganhRepository.findByTenNganh(filterNganh);
                if (n != null) {
                    dsTrungTuyen = nguyenVongRepository.findTop100ByNvManganhAndNvKetQuaOrderByDiemXetTuyenDesc(n.getMaNganh(), "TRUNG_TUYEN");
                }
            }
        }

        Object[][] data = new Object[dsTrungTuyen.size()][6];
        for (int i = 0; i < dsTrungTuyen.size(); i++) {
            NguyenVong nv = dsTrungTuyen.get(i);
            data[i][0] = nv.getNnCccd();
            data[i][1] = nv.getThiSinh() != null ? nv.getThiSinh().getHoTen() : "N/A";
            data[i][2] = nv.getNvManganh();
            data[i][3] = nv.getTtPhuongthuc() != null ? nv.getTtPhuongthuc() : "";
            data[i][4] = nv.getDiemXetTuyen() != null ? String.format("%.2f", nv.getDiemXetTuyen()) : "0.00";
            data[i][5] = nv.getNvKetQua();
        }
        return data;
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Map<String, Map<String, Long>> thongKePhuongThucTheoNganh() {
        // Lấy toàn bộ danh sách trúng tuyển để thống kê (Top 1000 để an toàn hiệu năng nhưng vẫn đủ bao quát)
        // Hoặc tốt hơn: Dùng Native Query để group by maNganh and phuongThuc
        List<Object[]> stats = nguyenVongRepository.countPhuongThucByMaNganhAndKetQua("TRUNG_TUYEN");
        
        Map<String, String> mapNganh = nganhRepository.findAll().stream()
                .collect(Collectors.toMap(Nganh::getMaNganh, Nganh::getTenNganh));
        
        Map<String, Map<String, Long>> result = new java.util.HashMap<>();
        for (Object[] row : stats) {
            String maNganh = (String) row[0];
            String pt = (String) row[1];
            Long count = (Long) row[2];
            
            String tenNganh = mapNganh.getOrDefault(maNganh, maNganh);
            if (pt == null || pt.isEmpty()) pt = "Chưa rõ";
            
            result.computeIfAbsent(tenNganh, k -> new java.util.HashMap<>()).put(pt, count);
        }
        return result;
    }


}