package com.sgu.tuyensinh.repository;

import com.sgu.tuyensinh.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository thao tác với bảng `users`.
 *
 * Luồng sử dụng phổ biến:
 * - AuthService gọi `findByUsername(username)` để lấy thông tin user khi đăng nhập.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
}

