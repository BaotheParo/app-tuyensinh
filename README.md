# SGU Tuyển Sinh 2026 (Spring Boot - Java 17 - Maven)

Dự án đồ án năm 3: backend Spring Boot theo **Layered Architecture** (Controller → Service → Repository → Database).  
Mục tiêu: code **đơn giản, dễ đọc**, phù hợp làm việc nhóm, có Javadoc tiếng Việt để mọi người nắm luồng nhanh.

---

## 1) Công nghệ sử dụng

- **Java**: 17
- **Spring Boot**: 3.x (Web MVC)
- **Build tool**: Maven
- **ORM**: Spring Data JPA (Hibernate)
- **Database**: MySQL
- **Lombok**: giảm boilerplate (getter/setter/constructor/builder)

---

## 2) Yêu cầu môi trường

- Cài **JDK 17**
- Cài **Maven** (hoặc dùng Maven tích hợp trong IDE)
- Cài **MySQL** (khuyến nghị MySQL 8+)

---

## 3) Cấu hình database

### Tạo database

Tạo database tên đúng yêu cầu:

```sql
CREATE DATABASE sgu_tuyensinh_2026 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### Cấu hình kết nối

Sửa file:

- `src/main/resources/application.properties`

Các dòng quan trọng:

- `spring.datasource.url=jdbc:mysql://localhost:3306/sgu_tuyensinh_2026...`
- `spring.datasource.username=...`
- `spring.datasource.password=...`

Ghi chú:
- Hiện tại cấu hình `spring.jpa.hibernate.ddl-auto=update` để tiện dev (tự tạo/cập nhật bảng theo Entity).

---

## 4) Cách chạy dự án

### Hỗ trợ khởi chạy DB bằng Docker (Local Dev)
Nếu máy bạn chưa cài đặt MySQL, bạn có thể chạy file Compose có sẵn để dựng nhanh DB bằng lệnh:
```bash
docker compose up -d
```
Hệ thống sẽ tự động kéo image MySQL 8.0, thiết lập database `sgu_tuyensinh_2026` với user/pass là `root / 123456`.

### Chạy bằng Maven

```bash
mvn spring-boot:run
```

### Build (không chạy test)

```bash
mvn -DskipTests package
```

---

## 5) Endpoint kiểm tra nhanh

- `GET /api/health` → trả về `OK` để biết hệ thống đang chạy.

---

## 6) Cấu trúc thư mục (Layered Architecture)

Mã nguồn nằm trong:

- `src/main/java/com/sgu/tuyensinh`

Các package chính:

- `controller`: Nhận request/response, validate đơn giản, gọi service
- `service`: Nghiệp vụ (business logic)
- `repository`: Tầng truy vấn DB (Spring Data JPA)
- `entity`: JPA Entity map với bảng DB
- `config`: Cấu hình ứng dụng
- `util`: Hằng số / tiện ích dùng chung

---

## 7) Entity & Repository hiện có

### Entity (package `com.sgu.tuyensinh.entity`)

- `ThiSinh` → bảng `thi_sinh`
- `DiemThi` → bảng `diem_thi` (có cột **nk1..nk8** để map SQL gốc)
- `Nganh` → bảng `nganh`
- `User` → bảng `users` (phục vụ đăng nhập)
- `DiemCong` → bảng `diem_cong` (Quản lý điểm cộng chứng chỉ/HSG)
- `NganhToHop` → bảng `nganh_to_hop` (Cấu hình hệ số và mức bù lệch môn Toán/Văn/Anh...)
- `NguyenVong` → bảng `nguyen_vong`
- `BangQuyDoi` → bảng `bang_qui_doi`

### Repository (package `com.sgu.tuyensinh.repository`)

- `ThiSinhRepository`
- `DiemThiRepository`
- `NganhRepository`
- `UserRepository`
  - Có hàm: `Optional<User> findByUsername(String username)`

---

## 8) Auth lõi (Đăng nhập) – mô tả luồng

Hiện tại dự án có **Auth lõi tối giản** để nhóm hiểu luồng trước (chưa Spring Security/JWT).

### Service

- `AuthService` (package `com.sgu.tuyensinh.service`)
  - Hàm: `User login(String username, String rawPassword)`

### Logic đăng nhập (đơn giản theo yêu cầu đồ án)

- Tìm user theo `username`
- Nếu không có user → trả `null`
- Nếu có user → so sánh trực tiếp chuỗi `rawPassword` với `user.getPassword()`
  - Đúng → trả `User`
  - Sai → trả `null`

**Lưu ý bảo mật**: so sánh password dạng chuỗi là **không an toàn** cho sản phẩm thật. Khi dự án ổn định, nhóm có thể nâng cấp sang BCrypt + Spring Security.

---

## 9) Quy ước code cho nhóm (khuyến nghị)

- Đặt tên rõ nghĩa, tránh viết tắt khó hiểu.
- Tách logic theo tầng:
  - Controller không viết nghiệp vụ phức tạp
  - Service chứa nghiệp vụ
  - Repository chỉ truy vấn DB
- Tránh “magic number”: đưa hằng số vào `util/AppConstants.java`.
- Comment/Javadoc tiếng Việt cho luồng quan trọng (đăng nhập, xét tuyển, tính điểm...).

---

## 10) Troubleshooting nhanh

- **Không kết nối được MySQL**:
  - Kiểm tra MySQL đang chạy chưa
  - Kiểm tra username/password trong `application.properties`
  - Kiểm tra database `sgu_tuyensinh_2026` đã tạo chưa
- **Lombok không hoạt động trong IDE**:
  - Bật plugin Lombok (IntelliJ/Eclipse)
  - Enable “Annotation Processing” trong IDE

---

## 11) Các Module Nghiệp Vụ Chính (Core Services)
Dự án được phân rã nghiệp vụ rõ ràng để team dễ dàng nắm bắt:
- **`ScoringService.java`**: Chịu trách nhiệm tính điểm xét tuyển (ĐXT) tuân thủ cực kỳ chuẩn xác 6 bước bộ quy tắc (tính tổ hợp gốc, điểm cộng, ưu tiên giảm dần, v.v.). Đây là Core Engine độc lập.
- **`ThiSinhService.java`**: Xử lý logic Thí sinh (phân trang tối ưu hiệu năng không mắc lỗi N+1 query bằng `LEFT JOIN FETCH`, cập nhật điểm thủ công và đảm bảo giao dịch với `@Transactional`).
- **`UserService.java`**: Nghiệp vụ quản lý user bao gồm chặn tạo trùng tên, thay đổi mật khẩu, phân quyền động (`toggleRole`) và hỗ trợ cấm/khóa hoạt động tài khoản (`toggleStatus`).
- **`ExcelReaderUtil.java`**: Nằm ở `util`, dùng `Apache POI` để duyệt đọc file Excel an toàn (chống lỗi tràn số mũ E+11 của Excel, xử lý kiểu mượt mà cho sinh viên sử dụng).

