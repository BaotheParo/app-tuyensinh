package com.sgu.tuyensinh;

import com.sgu.tuyensinh.admin.ui.MainFrame;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.swing.*;

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
        new org.springframework.boot.builder.SpringApplicationBuilder(AppTuyenSinhApplication.class)
                .headless(false)
                .web(WebApplicationType.NONE)
                .run(args);
    }

    @Bean
    public CommandLineRunner run() {
        return args -> {
            SwingUtilities.invokeLater(() -> {
                new MainFrame().setVisible(true);
            });
        };
    }
}
