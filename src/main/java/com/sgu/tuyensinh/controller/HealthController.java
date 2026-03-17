package com.sgu.tuyensinh.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller kiểm tra nhanh hệ thống có chạy hay chưa.
 *
 * Mục tiêu:
 * - Khi mới dựng project, nhóm chỉ cần chạy app và mở /api/health là biết OK.
 * - Sau này có thể mở rộng thêm endpoint khác theo nhu cầu.
 */
@RestController
public class HealthController {

    @GetMapping("/api/health")
    public String health() {
        // Trả về chuỗi đơn giản để test nhanh.
        return "OK";
    }
}
