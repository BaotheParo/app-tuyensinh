package com.sgu.tuyensinh.repository.custom;

import java.util.List;

public interface ThiSinhCustomRepository {

    /**
     * Lấy danh sách thí sinh + điểm thi (JOIN 2 bảng)
     */
    List<Object[]> fetchThiSinhWithDiemThi();

    /**
     * Lấy toàn bộ điểm cộng
     */
    List<Object[]> fetchDiemCong();
}