package com.sgu.tuyensinh.service;

import com.sgu.tuyensinh.entity.User;
import com.sgu.tuyensinh.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void testCreateUser_ThrowsExceptionWhenUsernameExists() {
        // Giả lập admin tạo lại tk
        User existingUserAttempt = new User();
        existingUserAttempt.setUsername("admin_super");

        // Báo DB rằng tài khoản trên đã nằm trong CSDL
        Mockito.when(userRepository.existsByUsername("admin_super")).thenReturn(true);

        // Hành động mong muốn: Báo lỗi và chặn lại, không gọi save()
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(existingUserAttempt);
        });

        assertEquals("Tên đăng nhập đã tồn tại", exception.getMessage());
        Mockito.verify(userRepository, Mockito.never()).save(Mockito.any());
    }
}
