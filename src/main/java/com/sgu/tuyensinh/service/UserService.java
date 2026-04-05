package com.sgu.tuyensinh.service;

import com.sgu.tuyensinh.entity.User;
import com.sgu.tuyensinh.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service xử lý các file liên quan đến Người dùng (User) dành cho Admin.
 */
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Lấy danh sách tất cả tài khoản.
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Thêm tài khoản mới sau khi kiểm tra tên đăng nhập.
     * Ném lỗi nếu username đã tồn tại.
     */
    @Transactional
    public User createUser(User newUser) {
        if (userRepository.existsByUsername(newUser.getUsername())) {
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại");
        }

        // Đồ án yêu cầu đơn giản, lưu mật khẩu thuần (plaintext).
        // Nếu trường isActive null, set là true để tài khoản dùng được ngay.
        if (newUser.getIsActive() == null) {
            newUser.setIsActive(true);
        }

        return userRepository.save(newUser);
    }

    /**
     * Đổi mật khẩu.
     * Phải check mật khẩu cũ có khớp DB không trước khi cho đổi.
     */
    @Transactional
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản với ID: " + userId));

        // Kiểm tra mật khẩu cũ. Ở đồ án đang dùng plaintext nên so sánh trực tiếp.
        if (!user.getPassword().equals(oldPassword)) {
            return false;
        }

        user.setPassword(newPassword);
        userRepository.save(user);
        return true;
    }

    /**
     * Hàm chuyển đổi quyền (Từ ADMIN -> USER và ngược lại).
     */
    @Transactional
    public User toggleRole(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản với ID: " + userId));

        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            user.setRole("USER");
        } else {
            user.setRole("ADMIN");
        }
        
        return userRepository.save(user);
    }

    /**
     * Hàm khóa/mở khóa tài khoản.
     * Đổi giá trị isActive từ true <-> false.
     */
    @Transactional
    public User toggleStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản với ID: " + userId));

        // Nếu null, ngầm định trạng thái trước đó là true
        boolean currentStatus = (user.getIsActive() != null) ? user.getIsActive() : true;
        user.setIsActive(!currentStatus);

        return userRepository.save(user);
    }
}
