package com.sgu.tuyensinh.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Cấu hình Web MVC cho module giao diện Web.
 *
 * <p>Thymeleaf tự động đăng ký ViewResolver với prefix "classpath:/templates/"
 * và suffix ".html", nên không cần cấu hình thủ công.</p>
 *
 * <p>Class này dùng để:
 * <ul>
 *   <li>Đăng ký View Controller đơn giản (không cần viết Controller class)</li>
 *   <li>Cấu hình static resources, CORS, interceptors nếu cần sau này</li>
 * </ul>
 *
 * <p><b>Lưu ý:</b> Module Web MVC hoạt động song song với Swing UI hiện tại.
 * Các class trong package {@code com.sgu.tuyensinh.admin.ui} không bị ảnh hưởng.</p>
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * Đăng ký redirect mặc định: truy cập "/" sẽ chuyển đến trang chủ "/trang-chu".
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addRedirectViewController("/", "/trang-chu");
    }
}
