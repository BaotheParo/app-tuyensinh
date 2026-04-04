package com.sgu.tuyensinh.service;

import com.sgu.tuyensinh.entity.ToHop;
import com.sgu.tuyensinh.repository.NganhToHopRepository;
import com.sgu.tuyensinh.repository.NguyenVongRepository;
import com.sgu.tuyensinh.repository.ToHopRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Test cho Module Quản lý Tổ hợp (TH-02)")
class ToHopServiceTest {

    @Mock private ToHopRepository toHopRepository;
    @Mock private NganhToHopRepository nganhToHopRepository;
    @Mock private NguyenVongRepository nguyenVongRepository;

    @InjectMocks private ToHopService toHopService;

    private ToHop sampleToHop;

    @BeforeEach
    void setUp() {
        sampleToHop = new ToHop();
        sampleToHop.setIdtohop(1);
        sampleToHop.setMaToHop("A01");
        sampleToHop.setTenToHop("Toán, Lý, Anh");
        sampleToHop.setMon1("TO");
        sampleToHop.setMon2("LI");
        sampleToHop.setMon3("N1");
    }

    // --- TEST CREATE ---
    @Test
    @DisplayName("Thêm tổ hợp mới thành công")
    void createToHop_Success() {
        when(toHopRepository.existsByMaToHop("A01")).thenReturn(false);
        when(toHopRepository.save(any(ToHop.class))).thenReturn(sampleToHop);

        ToHop result = toHopService.createToHop(sampleToHop);

        assertNotNull(result);
        assertEquals("A01", result.getMaToHop());
        verify(toHopRepository).save(sampleToHop);
    }

    @Test
    @DisplayName("Thêm tổ hợp thất bại do trùng mã tổ hợp (A01)")
    void createToHop_DuplicateMa() {
        when(toHopRepository.existsByMaToHop("A01")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> toHopService.createToHop(sampleToHop));
    }

    // --- TEST UPDATE ---
    @Test
    @DisplayName("Cập nhật tổ hợp thất bại khi cố tình sửa mã tổ hợp")
    void updateToHop_Fail_ChangeMa() {
        ToHop newData = new ToHop();
        newData.setMaToHop("D01"); // Mã khác với A01 ban đầu

        when(toHopRepository.findById(1)).thenReturn(Optional.of(sampleToHop));

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            toHopService.updateToHop(1, newData);
        });

        assertEquals("Không được thay đổi mã tổ hợp", exception.getMessage());
    }

    // --- TEST DELETE ---
    @Test
    @DisplayName("Chặn xóa tổ hợp khi đã được gán cho Ngành (Ràng buộc FK)")
    void deleteToHop_Fail_LinkedToNganh() {
        when(toHopRepository.findById(1)).thenReturn(Optional.of(sampleToHop));
        // Giả lập tổ hợp A01 đã được gán cho một ngành nào đó
        when(nganhToHopRepository.existsByMaToHop("A01")).thenReturn(true);

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            toHopService.deleteToHop(1);
        });

        assertTrue(exception.getMessage().contains("có ngành liên quan"));
        verify(toHopRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Xóa tổ hợp thành công khi dữ liệu sạch")
    void deleteToHop_Success() {
        when(toHopRepository.findById(1)).thenReturn(Optional.of(sampleToHop));
        when(nguyenVongRepository.existsByTtThm("A01")).thenReturn(false);
        when(nganhToHopRepository.existsByMaToHop("A01")).thenReturn(false);

        assertDoesNotThrow(() -> toHopService.deleteToHop(1));
        verify(toHopRepository).delete(sampleToHop);
    }
}
