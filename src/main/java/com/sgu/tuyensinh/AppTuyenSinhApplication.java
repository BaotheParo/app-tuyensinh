package com.sgu.tuyensinh;

import com.sgu.tuyensinh.admin.ui.MainFrame;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import javax.swing.*;

@SpringBootApplication

public class AppTuyenSinhApplication {

    public static void main(String[] args) {
        new org.springframework.boot.builder.SpringApplicationBuilder(AppTuyenSinhApplication.class)
                .headless(false)
                .web(WebApplicationType.NONE)
                .run(args);
    }

    @Bean
    @Profile("!test") // Chỉ chạy MainFrame khi không ở profile test

    public CommandLineRunner run(MainFrame mainFrame) {
        return args -> {
            SwingUtilities.invokeLater(() -> {
                mainFrame.setVisible(true);
            });
        };
    }
}