package com.sgu.tuyensinh.service;

import com.sgu.tuyensinh.entity.DiemThi;
import org.springframework.data.domain.Page;

import java.util.Map;

/**
 * Interface nghiệp vụ quản lý Điểm Thi.
 */
public interface DiemThiService {

    /**
     * Lấy danh sách điểm thi phân trang (dùng cho DiemThiPanel).
     * @param keyword Chuỗi tìm kiếm (theo CCCD hoặc Tên thí sinh)
     * @param pageNumber Số trang (0-indexed)
     * @param pageSize Kích thước trang
     * @return Danh sách DiemThi phân trang
     */
    Page<DiemThi> getDanhSachDiemThi(String keyword, int pageNumber, int pageSize);

    /**
     * Sửa điểm thi của một thí sinh.
     * Cập nhật tất cả các môn dựa trên Cccd của thí sinh.
     * @param cccd CCCD của thí sinh
     * @param diemMoi Đối tượng chứa điểm mới
     * @return DiemThi sau khi cập nhật
     */
    DiemThi updateDiemThi(String cccd, DiemThi diemMoi);

    /**
     * Làm rỗng toàn bộ điểm thi của thí sinh (Set tất cả về Null).
     * @param cccd CCCD của thí sinh
     */
    void clearDiemThi(String cccd);

    /**
     * Thống kê phổ điểm của 1 môn học chuyên biệt.
     * In-memory aggregation để trả về Dashboard.
     * @param monHoc Tên cột môn học (toan, van, ly,...)
     * @return Map chứa các khoảng điểm (VD: "<5.0") và số lượng thí sinh
     */
    Map<String, Long> thongKePhoDiem(String monHoc);
}
