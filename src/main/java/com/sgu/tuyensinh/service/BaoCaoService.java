package com.sgu.tuyensinh.service;

import com.sgu.tuyensinh.entity.Nganh;
import com.sgu.tuyensinh.entity.NguyenVong;
import com.sgu.tuyensinh.repository.DiemThiRepository;
import com.sgu.tuyensinh.repository.NganhRepository;
import com.sgu.tuyensinh.repository.NguyenVongRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BaoCaoService {

        private final NguyenVongRepository nguyenVongRepository;
        private final NganhRepository nganhRepository;
        private final DiemThiRepository diemThiRepository;

        /**
         * Thống kê số lượng nguyện vọng đăng ký theo ngành (key = tên ngành)
         */
        public Map<String, Long> thongKeDangKyTheoNganh() {
                List<NguyenVong> dsNguyenVong = nguyenVongRepository.findAll();

                Map<String, Long> thongKeTheoMaNganh = dsNguyenVong.stream()
                                .collect(Collectors.groupingBy(
                                                NguyenVong::getNvManganh,
                                                Collectors.counting()));

                // chuyển từ mã ngành sang tên ngành
                return nganhRepository.findAll().stream()
                                .collect(Collectors.toMap(
                                                Nganh::getTenNganh,
                                                nganh -> thongKeTheoMaNganh.getOrDefault(nganh.getMaNganh(), 0L)));
        }

        /**
         * Trả về danh sách ngành kèm số lượng đăng ký
         */
        public List<NganhThongKeDTO> thongKeChiTietTheoNganh() {
                Map<String, Long> thongKe = thongKeDangKyTheoNganh();
                List<Nganh> dsNganh = nganhRepository.findAll();

                return dsNganh.stream()
                                .map(nganh -> new NganhThongKeDTO(
                                                nganh.getMaNganh(), // vẫn giữ mã ngành để tham chiếu
                                                nganh.getTenNganh(), // tên ngành để hiển thị
                                                thongKe.getOrDefault(nganh.getTenNganh(), 0L) // tra theo tên ngành
                                ))
                                .collect(Collectors.toList());
        }

        // DTO phụ trợ cho báo cáo
        public record NganhThongKeDTO(String maNganh, String tenNganh, Long soLuongDangKy) {
        }

        public double[] getDiemMonHoc(String monHoc) {
                List<Double> dsDiem;
                switch (monHoc) {
                        case "Toán" -> dsDiem = diemThiRepository.findAllDiemToan();
                        case "Văn" -> dsDiem = diemThiRepository.findAllDiemVan();
                        case "Lý" -> dsDiem = diemThiRepository.findAllDiemLy();
                        case "Hóa" -> dsDiem = diemThiRepository.findAllDiemHoa();
                        case "Sinh" -> dsDiem = diemThiRepository.findAllDiemSinh();
                        case "Sử" -> dsDiem = diemThiRepository.findAllDiemSu();
                        case "Địa" -> dsDiem = diemThiRepository.findAllDiemDia();
                        case "Anh" -> dsDiem = diemThiRepository.findAllDiemAnh();
                        case "NK1" -> dsDiem = diemThiRepository.findAllDiemNk1();
                        case "NK2" -> dsDiem = diemThiRepository.findAllDiemNk2();
                        case "NK3" -> dsDiem = diemThiRepository.findAllDiemNk3();
                        case "NK4" -> dsDiem = diemThiRepository.findAllDiemNk4();
                        case "NK5" -> dsDiem = diemThiRepository.findAllDiemNk5();
                        case "NK6" -> dsDiem = diemThiRepository.findAllDiemNk6();
                        case "NK7" -> dsDiem = diemThiRepository.findAllDiemNk7();
                        case "NK8" -> dsDiem = diemThiRepository.findAllDiemNk8();
                        default -> dsDiem = List.of();
                }
                return dsDiem.stream().mapToDouble(Double::doubleValue).toArray();
        }


        public List<String> getDanhSachMonHoc() {
    return List.of("Toán", "Văn", "Lý", "Hóa", "Sinh", "Sử", "Địa", "Anh",
                   "NK1","NK2","NK3","NK4","NK5","NK6","NK7","NK8");
}




/**
 * Thống kê tỷ lệ Đậu/Rớt theo ngành
 * Trả về Map: key = tên ngành, value = DTO chứa số lượng Đậu/Rớt
 */
public Map<String, KetQuaTheoNganhDTO> thongKeKetQuaTheoNganh() {
    List<NguyenVong> dsNguyenVong = nguyenVongRepository.findAll();

    // Gom nhóm theo mã ngành
    Map<String, List<NguyenVong>> theoMaNganh = dsNguyenVong.stream()
            .collect(Collectors.groupingBy(NguyenVong::getNvManganh));

    // Chuyển sang map theo tên ngành
    return nganhRepository.findAll().stream()
            .collect(Collectors.toMap(
                    Nganh::getTenNganh,
                    nganh -> {
                        List<NguyenVong> nvTheoNganh = theoMaNganh.getOrDefault(nganh.getMaNganh(), List.of());
                        long soDau = nvTheoNganh.stream()
                                .filter(nv -> "TRUNG_TUYEN".equalsIgnoreCase(nv.getNvKetQua()))
                                .count();
                        long soRot = nvTheoNganh.stream()
                                .filter(nv -> "TRUOT".equalsIgnoreCase(nv.getNvKetQua()))
                                .count();
                        return new KetQuaTheoNganhDTO(nganh.getMaNganh(), nganh.getTenNganh(), soDau, soRot);
                    }
            ));
}

/**
 * DTO kết quả theo ngành
 */
public record KetQuaTheoNganhDTO(String maNganh, String tenNganh, long soDau, long soRot) {}





/**
 * Lấy danh sách thí sinh trúng tuyển để hiển thị trong bảng BC-04
 * Trả về mảng 2 chiều Object[][] cho JTable
 */
public Object[][] getDanhSachTrungTuyen() {
    List<NguyenVong> dsTrungTuyen = nguyenVongRepository.findAll().stream()
            .filter(nv -> "TRUNG_TUYEN".equalsIgnoreCase(nv.getNvKetQua()))
            .toList();

    Object[][] data = new Object[dsTrungTuyen.size()][5];
    for (int i = 0; i < dsTrungTuyen.size(); i++) {
        NguyenVong nv = dsTrungTuyen.get(i);
        data[i][0] = nv.getThiSinh().getId();      // Mã thí sinh
        data[i][1] = nv.getThiSinh().getHoTen();     // Họ tên
        data[i][2] = nv.getNvManganh();              // Mã ngành (hoặc join sang tên ngành nếu muốn)
        data[i][3] = nv.getDiemXetTuyen();           // Điểm xét tuyển
        data[i][4] = nv.getNvKetQua();               // Trạng thái (TRUNG_TUYEN)
    }
    return data;
}

}