# Hướng dẫn Chạy Module Web MVC — SGU Tuyển Sinh 2026

Dự án hiện đã tích hợp thêm module **Web MVC** chạy song song với giao diện **Swing Admin**. Thí sinh và thành viên nhóm có thể truy cập các tính năng tra cứu và giả lập thông qua trình duyệt web.

---

## 1. Yêu cầu hệ thống
- **Java:** JDK 17+.
- **Database:** MySQL (Chạy qua Docker Compose như cũ).
- **Maven:** Để build và chạy project.

---

## 2. Cách khởi động
Bạn khởi động project bằng lệnh Maven quen thuộc. Server Web sẽ tự động khởi chạy trên cổng `8080` cùng lúc với cửa sổ Swing:

```bash
mvn spring-boot:run
```

*Lưu ý: Nếu bạn thấy giao diện Swing hiện lên thì có nghĩa là Server Web cũng đã sẵn sàng.*

---

## 3. Các đường dẫn (URLs) truy cập

Mở trình duyệt và truy cập các địa chỉ sau:

| Tính năng | Đường dẫn | Đối tượng |
| :--- | :--- | :--- |
| **Trang chủ** | [http://localhost:8080/trang-chu](http://localhost:8080/trang-chu) | Mọi người |
| **Tra cứu kết quả** | [http://localhost:8080/tra-cuu](http://localhost:8080/tra-cuu) | Thí sinh |
| **Giả lập tính điểm** | [http://localhost:8080/gia-lap](http://localhost:8080/gia-lap) | Thí sinh / Khách |

---

## 4. Hướng dẫn sử dụng các tính năng mới

### 4.1. Tra cứu kết quả xét tuyển
- **Tài khoản (CCCD):** Sử dụng số CCCD của thí sinh trong Database (Ví dụ: `079204001234`).
- **Mật khẩu:** Định dạng `DDMMYYYY` (8 chữ số ngày sinh).
  - *Ví dụ:* Thí sinh sinh ngày `02/07/2004` thì mật khẩu là `02072004`.
- **Kết quả:** Hệ thống hiển thị bảng nguyện vọng, điểm xét tuyển và trạng thái (Trúng tuyển/Không trúng tuyển) kèm Banner chúc mừng nếu đỗ.

### 4.2. Giả lập tính điểm (Gia-lap)
- Cho phép thí sinh tự nhập điểm để "ướm" xem mình có đạt ngưỡng điểm sàn của ngành hay không.
- **Tính năng thông minh:**
  - Chọn ngành → Tự động load danh sách tổ hợp môn tương ứng (AJAX).
  - Chọn phương thức (THPT/ĐGNL/VSAT) → Tự động thay đổi thang điểm tối đa cho các ô nhập liệu.
  - Tự động tính điểm ưu tiên dựa trên Đối tượng và Khu vực.
- **Lưu ý:** Dữ liệu nhập ở đây chỉ để giả lập, **KHÔNG** lưu vào Database.

---

## 5. Thông tin kỹ thuật (Dành cho Dev)
- **Công nghệ:** Spring Boot Web MVC + Thymeleaf Template Engine.
- **Frontend:** Bootstrap 5 + Bootstrap Icons (dùng CDN để nhẹ project).
- **Cấu trúc thư mục:**
  - `com.sgu.tuyensinh.controller`: Chứa các file xử lý logic Web.
  - `src/main/resources/templates`: Chứa các file giao diện HTML (.html).
- **Cấu hình:** Enable Web được thiết lập trong `AppTuyenSinhApplication.java` thông qua `.web(WebApplicationType.SERVLET)`.

---
*Mọi thắc mắc về module Web vui lòng liên hệ Senior Dev trong nhóm.*
