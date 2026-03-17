package com.sgu.tuyensinh.util;

/**
 * Tiện ích xử lý chuỗi đơn giản.
 *
 * Mục tiêu:
 * - Tránh lặp lại các đoạn kiểm tra null/blank trong nhiều nơi.
 * - Viết theo kiểu dễ hiểu: if-else rõ ràng.
 */
public final class StringUtils {

    private StringUtils() {
        // Không cho tạo object từ class util này.
    }

    public static boolean isBlank(String value) {
        if (value == null) {
            return true;
        }

        // trim() để loại bỏ khoảng trắng đầu/cuối, sau đó kiểm tra độ dài.
        return value.trim().isEmpty();
    }
}
