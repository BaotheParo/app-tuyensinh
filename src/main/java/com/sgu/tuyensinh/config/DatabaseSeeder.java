package com.sgu.tuyensinh.config;

import com.sgu.tuyensinh.entity.*;
import com.sgu.tuyensinh.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class DatabaseSeeder giúp tự động tạo dữ liệu mẫu (Mock Data) khi ứng dụng khởi chạy.
 * Phù hợp cho đội Frontend test giao diện và Senior Dev test logic xét tuyển/nội suy.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ThiSinhRepository thiSinhRepository;
    private final DiemThiRepository diemThiRepository;
    private final NganhRepository nganhRepository;
    private final ToHopRepository toHopRepository;
    private final NganhToHopRepository nganhToHopRepository;
    private final BangQuyDoiRepository bangQuyDoiRepository;
    private final NguyenVongRepository nguyenVongRepository;
    private final DiemCongRepository diemCongRepository;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("--- Bắt đầu quá trình Seed dữ liệu mẫu toàn hệ thống ---");

        // RESET: Xóa dữ liệu cũ để đảm bảo Seed lại toàn bộ từ đầu (Tránh lỗi partial seed)
        // Chú ý: Thứ tự xóa phải ngược với thứ tự lưu để không vi phạm FK
        diemCongRepository.deleteAll();
        nguyenVongRepository.deleteAll();
        nganhToHopRepository.deleteAll();
        bangQuyDoiRepository.deleteAll();
        diemThiRepository.deleteAll();
        thiSinhRepository.deleteAll();
        nganhRepository.deleteAll();
        toHopRepository.deleteAll();

        seedAdminUser();
        seedDataCategories(); // Ngành, Tổ hợp, Quy đổi
        seedMockThiSinhAndNguyenVong();
        seedDiemCong();

        log.info("--- Quá trình Seed dữ liệu hoàn tất ---");
    }

    private void seedAdminUser() {
        if (userRepository.count() == 0) {
            User admin = User.builder()
                    .username("admin")
                    .password("123")
                    .role("ADMIN")
                    .isActive(true)
                    .build();
            userRepository.save(admin);
            log.info("[SEED] Đã tạo tài khoản mặc định: admin / 123");
        }
    }

    private void seedDataCategories() {
        // 1. Seed Nganh (4 Nganh chinh)
        if (nganhRepository.count() == 0) {
            List<Nganh> nganhs = Arrays.asList(
                Nganh.builder().maNganh("7480201").tenNganh("Cong nghe thong tin").chiTieu(200).diemSan(new BigDecimal("18.0")).thpt("1").vsat("1").dgnl("1").build(),
                Nganh.builder().maNganh("7340101").tenNganh("Quan tri kinh doanh").chiTieu(150).diemSan(new BigDecimal("17.0")).thpt("1").vsat("1").dgnl("1").build(),
                Nganh.builder().maNganh("7340301").tenNganh("Ke toan").chiTieu(100).diemSan(new BigDecimal("16.5")).thpt("1").vsat("0").dgnl("0").build(),
                Nganh.builder().maNganh("7220201").tenNganh("Ngon ngu Anh").chiTieu(120).diemSan(new BigDecimal("19.0")).thpt("1").vsat("0").dgnl("1").build()
            );
            nganhRepository.saveAll(nganhs);
            log.info("[SEED] Da tao 4 nganh tuyen sinh.");
        }

        // 2. Seed To hop (Day du cac to hop pho bien)
        if (toHopRepository.count() == 0) {
            List<ToHop> toHops = Arrays.asList(
                new ToHop(null, "A00", "TO", "LI", "HO", "Toan, Ly, Hoa"),
                new ToHop(null, "A01", "TO", "LI", "N1", "Toan, Ly, Anh"),
                new ToHop(null, "D01", "TO", "VA", "N1", "Toan, Van, Anh"),
                new ToHop(null, "C00", "VA", "SU", "DI", "Van, Su, Dia"),
                new ToHop(null, "D07", "TO", "HO", "N1", "Toan, Hoa, Anh")
            );
            toHopRepository.saveAll(toHops);
            log.info("[SEED] Da tao 5 to hop mon pho bien.");
        }

        // 3. Seed Nganh - To hop (Dam bao tat ca 4 nganh deu co mapping)
        if (nganhToHopRepository.count() == 0) {
            List<NganhToHop> nts = Arrays.asList(
                // CNTT: A00, A01, D07
                new NganhToHop(null, "7480201", "A00", "TO", 1.0, "LI", 1.0, "HO", 1.0, "7480201_A00", 0.0, 1.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, null, null),
                new NganhToHop(null, "7480201", "A01", "TO", 1.0, "LI", 1.0, "N1", 1.0, "7480201_A01", 1.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, null, null),
                new NganhToHop(null, "7480201", "D07", "TO", 1.0, "HO", 1.0, "N1", 1.0, "7480201_D07", 1.0, 1.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, null, null),
                // QTKD: A01, D01
                new NganhToHop(null, "7340101", "A01", "TO", 1.0, "LI", 1.0, "N1", 1.0, "7340101_A01", 1.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, null, null),
                new NganhToHop(null, "7340101", "D01", "TO", 1.0, "VA", 1.0, "N1", 1.0, "7340101_D01", 1.0, 1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, null, null),
                // Ke toan: A00, D01
                new NganhToHop(null, "7340301", "A00", "TO", 1.0, "LI", 1.0, "HO", 1.0, "7340301_A00", 0.0, 1.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, null, null),
                new NganhToHop(null, "7340301", "D01", "TO", 1.0, "VA", 1.0, "N1", 1.0, "7340301_D01", 1.0, 1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, null, null),
                // Ngon ngu Anh: D01 (Mon Anh he so 2)
                new NganhToHop(null, "7220201", "D01", "TO", 1.0, "VA", 1.0, "N1", 2.0, "7220201_D01", 1.0, 1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, null, null)
            );
            nganhToHopRepository.saveAll(nts);
            log.info("[SEED] Da gan to hop chi tiet cho toan bo cac nganh.");
        }

        // 4. Seed Bang quy doi (Mo rong DGNL va VSAT cac mon khac)
        if (bangQuyDoiRepository.count() == 0) {
            List<BangQuyDoi> qds = Arrays.asList(
                // IELTS
                new BangQuyDoi(null, "NGOAINGU", 2026, null, "IELTS", 4.0, 5.0, 8.0, 8.0, "IELTS_8", "Bac 1"),
                new BangQuyDoi(null, "NGOAINGU", 2026, null, "IELTS", 5.5, 6.5, 9.0, 9.0, "IELTS_9", "Bac 2"),
                new BangQuyDoi(null, "NGOAINGU", 2026, null, "IELTS", 7.0, 9.0, 10.0, 10.0, "IELTS_10", "Bac 3"),
                // VSAT Toan
                new BangQuyDoi(null, "VSAT", 2026, "A00", "TO", 0.0, 400.0, 0.0, 6.0, "VSAT_TO_1", "K1"),
                new BangQuyDoi(null, "VSAT", 2026, "A00", "TO", 400.0, 800.0, 6.0, 9.0, "VSAT_TO_2", "K2"),
                new BangQuyDoi(null, "VSAT", 2026, "A00", "TO", 800.0, 1000.0, 9.0, 10.0, "VSAT_TO_3", "K3"),
                // DGNL (V-SAT SGU 2026)
                new BangQuyDoi(null, "DGNL", 2026, "A00", null, 0.0, 600.0, 0.0, 15.0, "DGNL_1", "Muc 1"),
                new BangQuyDoi(null, "DGNL", 2026, "A00", null, 600.0, 1200.0, 15.0, 30.0, "DGNL_2", "Muc 2")
            );
            bangQuyDoiRepository.saveAll(qds);
            log.info("[SEED] Da nap bang quy doi (IELTS, VSAT, DGNL).");
        }
    }

    private void seedMockThiSinhAndNguyenVong() {
        if (thiSinhRepository.count() == 0) {
            List<ThiSinh> thiSinhList = new ArrayList<>();
            List<DiemThi> diemThiList = new ArrayList<>();
            List<NguyenVong> nvList = new ArrayList<>();

            for (int i = 1; i <= 25; i++) {
                String cccd = String.format("079000000%03d", i);
                
                ThiSinh ts = ThiSinh.builder()
                        .id(cccd)
                        .hoTen("Thi sinh Mock " + i)
                        .gioiTinh(i % 2 == 0 ? "Nam" : "Nu")
                        .doiTuongUt(i % 5 == 0 ? "01" : "00")
                        .khuVucUt(i % 3 == 0 ? "KV1" : "KV2")
                        .build();
                
                DiemThi dt = DiemThi.builder()
                        .cccd(cccd)
                        .toan(randomScore())
                        .van(randomScore())
                        .anh(randomScore())
                        .ly(randomScore())
                        .hoa(randomScore())
                        .sinh(randomScore())
                        .su(randomScore())
                        .dia(randomScore())
                        .nk1(randomScore())
                        .nk2(randomScore())
                        .nk3(randomScore())
                        .nk4(randomScore())
                        .nk5(randomScore())
                        .nk6(randomScore())
                        .nk7(randomScore())
                        .nk8(randomScore())
                        .build();

                // Tao 3 nguyen vong cho moi thi sinh de test logic tie-break
                nvList.add(new NguyenVong(null, cccd, "7480201", 1, null, cccd + "_1", "THPT", "A01", null, null, null, null, null));
                nvList.add(new NguyenVong(null, cccd, "7340101", 2, null, cccd + "_2", "THPT", "D01", null, null, null, null, null));
                nvList.add(new NguyenVong(null, cccd, "7220201", 3, null, cccd + "_3", "THPT", "D01", null, null, null, null, null));

                thiSinhList.add(ts);
                diemThiList.add(dt);
            }

            thiSinhRepository.saveAll(thiSinhList);
            diemThiRepository.saveAll(diemThiList);
            nguyenVongRepository.saveAll(nvList);

            log.info("[SEED] Da tao 25 thi sinh kem diem thi tat ca cac mon va 75 nguyen vong.");
        }
    }

    private void seedDiemCong() {
        if (diemCongRepository.count() == 0) {
            List<DiemCong> ds = new ArrayList<>();
            for (int i = 1; i <= 25; i++) {
                String cccd = String.format("079000000%03d", i);
                // Mock diem uu tien khu vuc/doi tuong
                ds.add(new DiemCong(null, cccd, null, null, "PT1", 0.0, 0.5, 0.5, "Uu tien khu vuc/doi tuong", null, cccd + "_DC", null));
                // Mock diem cong IELTS cho nhung thi sinh le
                if (i % 2 != 0) {
                    ds.add(new DiemCong(null, cccd, null, "A01", "PT2", 1.0, 0.0, 1.0, "Cong diem IELTS 6.5", "10/10/2025", cccd + "_IELTS", null));
                }
            }
            diemCongRepository.saveAll(ds);
            log.info("[SEED] Da tao du lieu Diem uu tien (Khu vuc & Chung chi).");
        }
    }

    private double randomScore() {
        double score = 4.0 + (Math.random() * 5.5); // Score từ 4.0 đến 9.5
        return Math.round(score * 10.0) / 10.0;
    }
}
