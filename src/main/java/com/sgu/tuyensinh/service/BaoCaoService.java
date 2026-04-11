package com.sgu.tuyensinh.service;

import com.sgu.tuyensinh.entity.Nganh;
import com.sgu.tuyensinh.entity.NguyenVong;
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
}