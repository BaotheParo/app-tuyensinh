package com.sgu.tuyensinh.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity người dùng (User) phục vụ đăng nhập.
 *
 * Luồng cơ bản:
 * - Khi đăng nhập, hệ thống sẽ tìm User theo `username`.
 * - Nếu có User thì so sánh `password` (hiện tại so sánh chuỗi trực tiếp theo yêu cầu đồ án).
 * - `role` dùng để phân quyền đơn giản (ADMIN/USER).
 *
 * Lưu ý:
 * - Đây là auth "lõi" tối giản cho sinh viên năm 3, chưa dùng BCrypt/JWT/Spring Security.
 * - Khi nâng cấp bảo mật, nhóm có thể đổi cách lưu/so sánh password sau.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "username", nullable = false, unique = true, length = 100)
    private String username;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    /**
     * Vai trò người dùng: ví dụ "ADMIN" hoặc "USER".
     * Để đơn giản, mình dùng String đúng theo yêu cầu.
     */
    @Column(name = "role", nullable = false, length = 20)
    private String role;
}

