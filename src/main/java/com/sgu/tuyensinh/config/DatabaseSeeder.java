package com.sgu.tuyensinh.config;

import com.sgu.tuyensinh.entity.DiemThi;
import com.sgu.tuyensinh.entity.ThiSinh;
import com.sgu.tuyensinh.entity.User;
import com.sgu.tuyensinh.repository.DiemThiRepository;
import com.sgu.tuyensinh.repository.ThiSinhRepository;
import com.sgu.tuyensinh.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Class DatabaseSeeder giúp tự động tạo dữ liệu mẫu (Mock Data) khi ứng dụng khởi chạy.
 * Phù hợp cho đội Frontend test giao diện (đăng nhập, phân trang JTable).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ThiSinhRepository thiSinhRepository;
    private final DiemThiRepository diemThiRepository;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("--- Bắt đầu quá trình Seed dữ liệu mẫu ---");

        seedAdminUser();
        seedMockThiSinh();

        log.info("--- Quá trình Seed dữ liệu hoàn tất ---");
    }

    /**
     * Tạo tài khoản Admin mặc định nếu chưa có.
     */
    private void seedAdminUser() {
        if (userRepository.count() == 0) {
            User admin = User.builder()
                    .username("admin")
                    .password("123") // Lưu ý: Đồ án đang so sánh chuỗi trực tiếp
                    .role("ADMIN")
                    .isActive(true)
                    .build();
            
            userRepository.save(admin);
            log.info("[SEED] Đã tạo tài khoản mặc định: admin / 123");
        }
    }

    /**
     * Tạo 25 thí sinh kèm điểm thi để test phân trang (Pagination 20 dòng/trang).
     */
    private void seedMockThiSinh() {
        if (thiSinhRepository.count() == 0) {
            List<ThiSinh> thiSinhList = new ArrayList<>();
            List<DiemThi> diemThiList = new ArrayList<>();

            for (int i = 1; i <= 25; i++) {
                // Tạo số CCCD giả lập (đảm bảo độ dài 12 số)
                String cccd = String.format("079000000%03d", i);
                
                // 1. Tạo thực thể Thí sinh
                ThiSinh ts = ThiSinh.builder()
                        .id(cccd)
                        .hoTen("Thí sinh Mock " + i)
                        .gioiTinh(i % 2 == 0 ? "Nam" : "Nữ")
                        .doiTuongUt("01")
                        .khuVucUt("KV1")
                        .build();
                
                // 2. Tạo thực thể Điểm thi (Liên kết qua CCCD)
                DiemThi dt = DiemThi.builder()
                        .cccd(cccd)
                        .toan(randomScore())
                        .van(randomScore())
                        .anh(randomScore())
                        .build();

                thiSinhList.add(ts);
                diemThiList.add(dt);
            }

            // Lưu hàng loạt (Batch save) để tối ưu hiệu năng
            thiSinhRepository.saveAll(thiSinhList);
            diemThiRepository.saveAll(diemThiList);

            log.info("[SEED] Đã tạo 25 thí sinh mẫu kèm điểm thi TOAN-VAN-ANH.");
        }
    }

    /**
     * Hàm hỗ trợ tạo điểm ngẫu nhiên từ 5.0 đến 9.5 (làm tròn 1 chữ số).
     */
    private double randomScore() {
        double score = 5.0 + (Math.random() * 4.5);
        return Math.round(score * 10.0) / 10.0;
    }
}
