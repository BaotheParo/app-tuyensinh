package com.sgu.tuyensinh;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Điểm khởi chạy của ứng dụng Spring Boot.
 *
 * Lưu ý cho nhóm:
 * - Đây là class "main", chạy lên là Spring sẽ tự quét các component trong package `com.sgu.tuyensinh` trở xuống.
 * - Vì vậy các controller/service/repository/config/util/... nên nằm dưới package này để Spring nhận diện tự động.
 */
@SpringBootApplication
public class AppTuyenSinhApplication {

    public static void main(String[] args) {
        SpringApplication.run(AppTuyenSinhApplication.class, args);
    }
}
