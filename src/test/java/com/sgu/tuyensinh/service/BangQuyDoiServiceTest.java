package com.sgu.tuyensinh.service;

import com.sgu.tuyensinh.entity.BangQuyDoi;
import com.sgu.tuyensinh.repository.BangQuyDoiRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Test Bảng Quy Đổi & Thuật toán Nội suy (QD-01/02/03)")
class BangQuyDoiServiceTest {

    @Mock
    private BangQuyDoiRepository repository;

    @InjectMocks
    private BangQuyDoiService service;

    private List<BangQuyDoi> mockIntervals;

    @BeforeEach
    void setUp() {
        // Giả lập bảng bách phân vị cho DGNL 2026 (PRD trang 5)
        // Khoảng 1: [1190, 1200] -> [19, 23]
        BangQuyDoi kv1 = new BangQuyDoi();
        kv1.setPhuongThuc("DGNL");
        kv1.setNamHoc(2026);
        kv1.setToHop("A00");
        kv1.setDiemGocA(1190.0);
        kv1.setDiemGocB(1200.0);
        kv1.setDiemQuyDoiC(19.0);
        kv1.setDiemQuyDoiD(23.0);

        // Khoảng 2: [1000, 1100] -> [15, 18]
        BangQuyDoi kv2 = new BangQuyDoi();
        kv2.setPhuongThuc("DGNL");
        kv2.setNamHoc(2026);
        kv2.setToHop("A00");
        kv2.setDiemGocA(1000.0);
        kv2.setDiemGocB(1100.0);
        kv2.setDiemQuyDoiC(15.0);
        kv2.setDiemQuyDoiD(18.0);

        mockIntervals = Arrays.asList(kv1, kv2);
    }

    @Test
    @DisplayName("Nội suy đúng điểm trong khoảng (Ví dụ PRD: 1200 -> 23)")
    void calculateInterpolation_CorrectValue() {
        when(repository.findByPhuongThucAndToHopContainingIgnoreCase("DGNL", "A00"))
                .thenReturn(mockIntervals);

        // Test điểm 1200 (Biên trên)
        Double result1 = service.calculateInterpolation("DGNL", "A00", 1200.0);
        assertEquals(23.0, result1);

        // Test điểm 1195 (Chính giữa khoảng 1190-1200 -> 19-23 => 21.0)
        Double result2 = service.calculateInterpolation("DGNL", "A00", 1195.0);
        assertEquals(21.0, result2);
    }

    @Test
    @DisplayName("Nội suy và làm tròn 2 chữ số thập phân (Yêu cầu trang 5)")
    void calculateInterpolation_Rounding() {
        when(repository.findByPhuongThucAndToHopContainingIgnoreCase("DGNL", "A00"))
                .thenReturn(mockIntervals);

        // x = 1192.34 -> y = 19 + ((1192.34-1190)/(1200-1190))*(23-19) = 19.936 -> 19.94
        Double result = service.calculateInterpolation("DGNL", "A00", 1192.34);
        assertEquals(19.94, result);
    }

    @Test
    @DisplayName("Xử lý Clamp (Bo biên) khi điểm ngoài bảng (INTERPOLATION_OUT_OF_TABLE)")
    void calculateInterpolation_Clamp() {
        when(repository.findByPhuongThucAndToHopContainingIgnoreCase("DGNL", "A00"))
                .thenReturn(mockIntervals);

        // Thấp hơn min (900 < 1000) -> trả về điểm C của khoảng thấp nhất (15.0)
        assertEquals(15.0, service.calculateInterpolation("DGNL", "A00", 900.0));

        // Cao hơn max (1300 > 1200) -> trả về điểm D của khoảng cao nhất (23.0)
        assertEquals(23.0, service.calculateInterpolation("DGNL", "A00", 1300.0));
    }

    @Test
    @DisplayName("Chặn nội suy khi không có dữ liệu năm 2026 (Q1 Versioning)")
    void calculateInterpolation_NoData2026() {
        // Giả lập chỉ có dữ liệu năm 2025
        BangQuyDoi oldData = new BangQuyDoi();
        oldData.setNamHoc(2025);
        oldData.setPhuongThuc("VSAT");

        when(repository.findByPhuongThucAndMonContainingIgnoreCase("VSAT", "TOAN"))
                .thenReturn(List.of(oldData));

        assertThrows(IllegalStateException.class, () -> {
            service.calculateInterpolation("VSAT", "TOAN", 100.0);
        });
    }

    @Test
    @DisplayName("Tìm kiếm đúng phương thức (Case-insensitive)")
    void search_CorrectLogic() {
        service.search("vsat", "TO");
        verify(repository).findByPhuongThucAndMonContainingIgnoreCase("VSAT", "TO");

        service.search("DGNL", "A01");
        verify(repository).findByPhuongThucAndToHopContainingIgnoreCase("DGNL", "A01");
    }
}
