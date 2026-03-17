package com.sgu.tuyensinh.util;

import java.time.LocalDate;

/**
 * Nơi tập trung các hằng số dùng chung trong dự án.
 *
 * Vì nhóm mình là sinh viên, để code dễ đọc và dễ sửa:
 * - Hạn chế "magic number" (số xuất hiện trực tiếp trong code mà không rõ ý nghĩa).
 * - Đưa các ngưỡng/giá trị cấu hình quan trọng về một chỗ.
 */
public final class AppConstants {

    private AppConstants() {
        // Không cho tạo object từ class hằng số này.
    }

    /**
     * Điểm tối đa (thang 30) thường dùng trong xét tuyển.
     */
    public static final double MAX_SCORE = 30.0;

    /**
     * Ngưỡng điểm ưu tiên (ví dụ: từ mức điểm này trở lên thì xét thêm ưu tiên theo quy định).
     * Giá trị cụ thể tùy nghiệp vụ nhóm mình định nghĩa.
     */
    public static final double THRESHOLD_UUTIEN = 22.5;

    /**
     * Ngày hết hạn (hạn cuối) cho một mốc nghiệp vụ trong hệ thống.
     *
     * User yêu cầu giá trị dạng '2024-06-30' => mình lưu dạng LocalDate để code an toàn kiểu dữ liệu.
     */
    public static final LocalDate EXPIRY_DATE = LocalDate.parse("2024-06-30");
}
