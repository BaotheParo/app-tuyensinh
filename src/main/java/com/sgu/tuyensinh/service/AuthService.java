package com.sgu.tuyensinh.service;

import com.sgu.tuyensinh.entity.User;
import com.sgu.tuyensinh.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service xử lý đăng nhập (Auth lõi) cho đồ án.
 *
 * Mục tiêu thiết kế (layered architecture):
 * - Controller (tầng ngoài) nhận request và gọi vào AuthService.
 * - AuthService (tầng nghiệp vụ) chứa logic đăng nhập: tìm user và kiểm tra mật khẩu.
 * - Repository (tầng dữ liệu) chỉ lo truy vấn DB.
 *
 * Lưu ý quan trọng:
 * - Theo yêu cầu đồ án, mật khẩu đang so sánh trực tiếp bằng chuỗi (chưa mã hóa BCrypt).
 * - Cách này KHÔNG an toàn cho sản phẩm thật, nhưng phù hợp để nhóm hiểu luồng trước.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Đăng nhập theo username + mật khẩu (raw).
     *
     * Luồng xử lý:
     * - Bước 1: Tìm user theo username.
     * - Bước 2: Nếu không có user => trả về null.
     * - Bước 3: Nếu có user => so sánh trực tiếp `rawPassword` với `user.getPassword()`.
     * - Bước 4: Nếu trùng => đăng nhập thành công, trả về User.
     * - Bước 5: Nếu không trùng => đăng nhập thất bại, trả về null.
     *
     * @param username tên đăng nhập
     * @param rawPassword mật khẩu nhập từ form (chưa mã hóa)
     * @return User nếu đăng nhập đúng; ngược lại trả về null
     */
    public User login(String username, String rawPassword) {
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()) {
            return null;
        }

        User user = optionalUser.get();

        // So sánh chuỗi cơ bản theo yêu cầu (chưa dùng BCrypt).
        if (rawPassword == null) {
            return null;
        }

        if (rawPassword.equals(user.getPassword())) {
            return user;
        }

        return null;
    }
}

