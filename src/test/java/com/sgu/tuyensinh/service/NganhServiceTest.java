package com.sgu.tuyensinh.service;

import com.sgu.tuyensinh.entity.Nganh;
import com.sgu.tuyensinh.repository.NganhRepository;
import com.sgu.tuyensinh.repository.NganhToHopRepository;
import com.sgu.tuyensinh.repository.NguyenVongRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Test cho Module Quản lý Ngành (NG-02/03)")
class NganhServiceTest {

    @Mock private NganhRepository nganhRepository;
    @Mock private NguyenVongRepository nguyenVongRepository;
    @Mock private NganhToHopRepository nganhToHopRepository;

    @InjectMocks private NganhService nganhService;

    private Nganh sampleNganh;

    @BeforeEach
    void setUp() {
        sampleNganh = new Nganh();
        sampleNganh.setMaNganh("7480201");
        sampleNganh.setTenNganh("Công nghệ thông tin");
        sampleNganh.setChiTieu(100);
        sampleNganh.setDiemSan(BigDecimal.valueOf(18.00));
    }

    // --- TEST CREATE ---
    @Test
    @DisplayName("Thêm ngành mới thành công")
    void createNganh_Success() {
        when(nganhRepository.existsById(sampleNganh.getMaNganh())).thenReturn(false);
        when(nganhRepository.save(any(Nganh.class))).thenReturn(sampleNganh);

        Nganh result = nganhService.createNganh(sampleNganh);

        assertNotNull(result);
        assertEquals("7480201", result.getMaNganh());
        verify(nganhRepository, times(1)).save(sampleNganh);
    }

    @Test
    @DisplayName("Thêm ngành thất bại do trùng mã ngành")
    void createNganh_DuplicateId() {
        when(nganhRepository.existsById(sampleNganh.getMaNganh())).thenReturn(true);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            nganhService.createNganh(sampleNganh);
        });

        assertEquals("Mã ngành đã tồn tại", exception.getMessage());
    }

    // --- TEST UPDATE ---
    @Test
    @DisplayName("Cập nhật thông tin ngành thành công")
    void updateNganh_Success() {
        Nganh newData = new Nganh();
        newData.setMaNganh("7480201");
        newData.setTenNganh("Kỹ thuật phần mềm"); // Đổi tên

        when(nganhRepository.findById("7480201")).thenReturn(Optional.of(sampleNganh));
        when(nganhRepository.save(any(Nganh.class))).thenReturn(newData);

        Nganh result = nganhService.updateNganh("7480201", newData);

        assertEquals("Kỹ thuật phần mềm", result.getTenNganh());
        verify(nganhRepository).save(any(Nganh.class));
    }

    @Test
    @DisplayName("Cập nhật thất bại khi cố tình thay đổi mã ngành (Ràng buộc Q2/Q6)")
    void updateNganh_Fail_ChangeId() {
        Nganh newData = new Nganh();
        newData.setMaNganh("9999999"); // Mã khác với sample

        when(nganhRepository.findById("7480201")).thenReturn(Optional.of(sampleNganh));

        assertThrows(IllegalStateException.class, () -> {
            nganhService.updateNganh("7480201", newData);
        });
    }

    // --- TEST DELETE ---
    @Test
    @DisplayName("Xóa ngành thành công khi không có ràng buộc")
    void deleteNganh_Success() {
        when(nganhRepository.findById("7480201")).thenReturn(Optional.of(sampleNganh));
        when(nguyenVongRepository.existsByNvManganh("7480201")).thenReturn(false);
        when(nganhToHopRepository.existsByMaNganh("7480201")).thenReturn(false);

        assertDoesNotThrow(() -> nganhService.deleteNganh("7480201"));
        verify(nganhRepository).delete(sampleNganh);
    }

    // @Test
    // @DisplayName("Chặn xóa ngành khi đã có nguyện vọng đăng ký (Data Integrity)")
    // void deleteNganh_Fail_HasNguyenVong() {
    //     when(nganhRepository.findById("7480201")).thenReturn(Optional.of(sampleNganh));
    //     when(nguyenVongRepository.existsByNvManganh("7480201")).thenReturn(true); // Giả lập có NV

    //     Exception exception = assertThrows(IllegalStateException.class, () -> {
    //         nganhService.deleteNganh("7480201");
    //     });

    //     assertTrue(exception.getMessage().contains("có nguyện vọng liên quan"));
    //     verify(nganhRepository, never()).delete(any());
    // }
}
