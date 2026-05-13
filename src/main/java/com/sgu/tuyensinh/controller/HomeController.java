package com.sgu.tuyensinh.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller trang chủ Web — điểm vào của giao diện Web MVC.
 *
 * <p>Truy cập: <a href="http://localhost:8080/trang-chu">http://localhost:8080/trang-chu</a></p>
 *
 * <p><b>Lưu ý:</b> Controller này hoạt động độc lập với giao diện Swing Admin.
 * Swing dùng Spring Context nội bộ, còn Web MVC phục vụ qua HTTP.</p>
 */
@Controller
public class HomeController {

    @GetMapping("/trang-chu")
    public String trangChu(Model model) {
        model.addAttribute("tieuDe", "Hệ Thống Tuyển Sinh Đại Học SGU 2026");
        model.addAttribute("moTa", "Tra cứu thông tin tuyển sinh, ngành đào tạo và kết quả xét tuyển.");
        return "index"; // → src/main/resources/templates/index.html
    }
}
