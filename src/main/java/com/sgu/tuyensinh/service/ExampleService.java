package com.sgu.tuyensinh.service;

import org.springframework.stereotype.Service;

/**
 * Service mẫu.
 *
 * Gợi ý cho nhóm:
 * - Service là nơi đặt nghiệp vụ (business logic).
 * - Controller chỉ nên nhận request/validate đơn giản rồi gọi service.
 */
@Service
public class ExampleService {

    public String getWelcomeMessage() {
        // Ví dụ logic đơn giản để nhóm hình dung luồng gọi.
        return "Chào mừng đến với hệ thống tuyển sinh SGU 2026";
    }
}
