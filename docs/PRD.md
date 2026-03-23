# Technical PRD — Phần Mềm Quản Lý Tuyển Sinh Đại Học (SGU 2026)

> **Phiên bản:** 3.0 · **Ngày cập nhật:** 16/03/2026  
> **Tác giả:** Senior Technical PM / Lead Architect  
> **Trạng thái:** FINAL — Đã điều chỉnh độ khó phù hợp năm 3  
> **Thời gian thực hiện:** 5 tuần · **Quy mô team:** 8 người

### Changelog v2.1 → v3.0 — Hạ cấp độ khó

> **Lý do:** Đây là đồ án môn học năm 3. Mục tiêu chính là **chứng minh hiểu kiến trúc phân lớp và luồng nghiệp vụ**, không phải tối ưu hóa hiệu năng production.

| Module | v2.1 (Quá sức năm 3) | v3.0 (Phù hợp năm 3) | Hậu quả chấp nhận |
|--------|---------------------|---------------------|-------------------|
| **Import file** | `StatelessSession`, chunking 500, native upsert | POI đọc file → `saveAll()` từng batch hoặc từng dòng | Import 43k dòng mất 3–5 phút thay vì 30 giây. Chấp nhận được vì dùng nội bộ. |
| **Caching** | `ConcurrentHashMap`, invalidate sau import | `HashMap` load 1 lần lúc khởi động app, dùng cho đến khi tắt | Bảng quy đổi không tự refresh — Admin tắt/bật lại app sau khi import bảng mới. |
| **Concurrency** | Multi-threading, thread-safe collections | Bỏ hoàn toàn đa luồng. Mọi thứ chạy tuần tự (synchronous) | App không dùng chung nhiều người → không ảnh hưởng thực tế. |
| **Ghi kết quả xét tuyển** | 1 transaction lớn cuối, batch write 1000 | Vòng `for`, `repository.save()` từng thí sinh sau khi tính xong | Lưu kết quả chậm hơn. Code dễ debug, dễ đọc. |
| **SwingWorker** | Async nghiêm ngặt, memory management | `SwingWorker` cơ bản: chạy nền để UI không đơ, progress bar đơn giản | Progress bar có thể giật cục. Chấp nhận được. |

### Changelog v2.0 → v2.1

| # | Mục thay đổi | Loại |
|---|-------------|------|
| Q1 | Bảng quy đổi: thêm `nam_hoc`, giữ lịch sử, cache invalidation | DB Schema + Architecture |
| Q2 | Tuyển thẳng PT1: ưu tiên tuyệt đối, không check quota, dùng `sl_xtt` | Business Rule |
| Q3 | Lỗi thuật toán: log + continue + `nv_ketqua='ERROR'`, ghi DB 1 transaction cuối | Error Handling |
| Q4 | Import concurrent: đổi sang upsert `ON DUPLICATE KEY UPDATE` | Architecture |
| Q5 | Cache bảng quy đổi: `ConcurrentHashMap`, invalidate sau import | Architecture |
| Q6 | Export Excel: explicit `Comparator` 4 cấp, label phương thức tiếng Việt | Functional |
| Q7 | Transaction xét tuyển: toàn bộ in-memory, 1 transaction ghi DB cuối | Architecture |
| Q8 | Chứng chỉ TA: lấy max điểm, reject hết hạn (`CERT_EXPIRED`), thêm `ngay_cap` | Business Rule + Validation |

---

## Mục lục

1. [Tổng quan sản phẩm](#1-tổng-quan-sản-phẩm)
2. [Nghiệp vụ cốt lõi — Domain Layer](#2-nghiệp-vụ-cốt-lõi--domain-layer)
3. [Yêu cầu chức năng](#3-yêu-cầu-chức-năng)
4. [Yêu cầu kỹ thuật & Kiến trúc phân lớp](#4-yêu-cầu-kỹ-thuật--kiến-trúc-phân-lớp)
5. [Kế hoạch phân việc 5 tuần](#5-kế-hoạch-phân-việc-5-tuần)
6. [Phụ lục](#phụ-lục)

---

## 1. Tổng quan sản phẩm

### 1.1 Bối cảnh & Mục tiêu

Đại học Sài Gòn (SGU) cần một hệ thống gồm **2 thành phần**:

| Thành phần | Đối tượng | Stack | Ưu tiên |
|-----------|-----------|-------|---------|
| **Desktop Admin App** | Cán bộ phòng tuyển sinh | Java Swing + Hibernate + MySQL | **Làm trước — hoàn thiện toàn bộ** |
| **Web Candidate Portal** | Thí sinh (tra cứu điểm, kết quả) | Spring MVC + Thymeleaf + MySQL (cùng DB) | Làm sau, sau khi admin xong |

> **Nguyên tắc triển khai:** Web portal cho thí sinh chỉ khởi động sau khi toàn bộ chức năng Admin (import, tính điểm, xét tuyển) đã hoàn chỉnh và dữ liệu kết quả đã có trong DB. Web portal chỉ **đọc** dữ liệu, không ghi.

### 1.2 In-Scope (MVP — Admin Desktop)

| # | Module | Mô tả ngắn |
|---|--------|-----------|
| 1 | Auth & User Mgmt | Đăng nhập, phân quyền Admin / User |
| 2 | Quản lý Ngành | CRUD + Import; cấu hình phương thức xét tuyển per ngành |
| 3 | Quản lý Tổ hợp môn | CRUD + Import; ánh xạ ngành–tổ hợp với hệ số + flags môn |
| 4 | Quản lý Thí sinh | Import + CRUD + phân trang + tìm kiếm |
| 5 | Quản lý Điểm thi | Import điểm THPT / V-SAT / ĐGNL + NK1–NK8 |
| 6 | Bảng Quy đổi | Import + CRUD bảng quy đổi chứng chỉ TA + bách phân vị |
| 7 | Điểm cộng & Ưu tiên | Import + CRUD; tính điểm cộng HSG, chứng chỉ, đối tượng, khu vực |
| 8 | Nguyện vọng | Import + xem + sửa nguyện vọng thí sinh |
| 9 | **Xét tuyển Engine** | Tính điểm → xét nguyện vọng → phân loại trúng/trượt |
| 10 | Báo cáo | Thống kê, xuất Excel |

### 1.3 In-Scope (Giai đoạn 2 — Web Portal Thí sinh)

| # | Module | Mô tả ngắn |
|---|--------|-----------|
| W1 | Đăng ký tài khoản | Thí sinh tự đăng ký bằng CCCD; tạo password để tra cứu |
| W2 | Đăng nhập | Xác thực bằng CCCD + password |
| W3 | Xem điểm thi | Hiển thị điểm thi của bản thân theo phương thức |
| W4 | Xem kết quả xét tuyển | Hiển thị trúng/trượt, ngành trúng, phương thức trúng, tổ hợp |

### 1.4 Out-of-Scope

- Cổng nộp hồ sơ trực tuyến (Bộ GD&ĐT quản lý).
- Tích hợp API Bộ GD&ĐT / ĐHQG.
- Chatbot / GenAI.
- Thanh toán lệ phí.
- Tích hợp SMS/Email tự động.
- Multi-tenant.

### 1.5 Người dùng mục tiêu

| Role | Quyền hạn |
|------|-----------|
| `ADMIN` | Toàn quyền: CRUD, Import, chạy thuật toán, quản lý user |
| `USER` | Chỉ xem danh sách, tìm kiếm, xem kết quả |
| `CANDIDATE` (web) | Chỉ xem điểm và kết quả của chính mình |

---

## 2. Nghiệp vụ cốt lõi — Domain Layer

> **Nguyên tắc vàng:** Toàn bộ logic tính điểm nằm trong **Service Layer**. Không rải vào Controller hay Repository. Mỗi rule phải có Unit Test độc lập, có thể chạy không cần DB.

### 2.1 Bốn phương thức xét tuyển

| Mã | Tên | Flag trong DB | Thang gốc | Cần quy đổi? |
|----|-----|--------------|-----------|-------------|
| `PT1` | Tuyển thẳng / Ưu tiên xét tuyển | `n_tuyenthang = 'Y'` | — | Không |
| `PT2` | ĐGNL (ĐHQG HCM) | `n_dgnl = 'Y'` | 1200 điểm | Có → về thang 30 |
| `PT3` | V-SAT | `n_vsat = 'Y'` | 450 điểm | Có → về thang 30 |
| `PT4` | THPT | `n_thpt = 'Y'` | 30 điểm | Không (gốc) |

**Nguyên tắc flags:** Khi Admin cấu hình ngành, bật flag phương thức nào thì ngành đó mới xét phương thức đó. Ví dụ: Giáo dục Mầm non chỉ bật `n_thpt = 'Y'` và `n_tuyenthang = 'Y'` → chỉ xét PT1 + PT4.

---

### 2.2 Quy đổi điểm ĐGNL / V-SAT về thang THPT (Nội suy tuyến tính)

**Công thức:**

```
y = c + ((x − a) / (b − a)) × (d − c)
```

Trong đó:
- `x`: điểm gốc cần quy đổi (VD: 1200 ĐGNL).
- `[a, b]`: khoảng điểm nguồn trong bảng bách phân vị.
- `[c, d]`: khoảng điểm THPT tương ứng trong bảng bách phân vị.
- `y`: điểm sau quy đổi về thang THPT (0–30).

**Ví dụ thực tế:**
> Thí sinh đạt 1200 điểm ĐGNL. Tra bảng bách phân vị: khoảng [1190, 1200] ĐGNL tương ứng với [19, 23] THPT.
> → y = 19 + ((1200 − 1190) / (1200 − 1190)) × (23 − 19) = 19 + 1 × 4 = **23 điểm THPT**

**Lưu ý implement:**
- Bảng bách phân vị lưu trong `xt_bangquydoi` với các interval `[d_diema, d_diemb]` liên tiếp.
- **Mọi query PHẢI truyền `nam_hoc` explicit** (Q1 — versioning): `WHERE d_phuongthuc=? AND nam_hoc=2026 AND d_diema<=? AND d_diemb>=?`.
- Tra cứu bằng binary search trên `d_diema` sau khi đã filter theo `nam_hoc`.
- Điểm ngoài range → clamp về min/max của bảng, log cảnh báo `INTERPOLATION_OUT_OF_TABLE`.
- Kết quả làm tròn 2 chữ số thập phân.
- Bảng V-SAT và ĐGNL là 2 bảng riêng: `d_phuongthuc = 'VSAT'` / `'DGNL'`.
- **Cache strategy (Q5):** `ScoreConversionService` dùng `ConcurrentHashMap<String, List<Interval>>` (key: `"VSAT_2026"`, `"DGNL_2026"`). Load lazy khi first query. Sau khi `ImportService` ghi bảng mới thành công → gọi `conversionService.invalidateCache(namHoc)` ngay lập tức. UI hiển thị indicator "Bảng quy đổi: năm 2026 (cached)".

---

### 2.3 Xử lý điểm môn = 0 — Skip Optimization

**Rule:**
- Nếu thí sinh không có điểm một môn → điểm môn đó = 0.
- Khi tính điểm cho một tổ hợp, nếu bất kỳ môn bắt buộc nào trong tổ hợp = 0 → **bỏ qua toàn bộ tổ hợp đó**, không tính.
- Cách kiểm tra: dùng các flag columns `(TO, LI, HO, SI, VA, SU, DI, TI, N1, CNCN, CNNN, KTPL, NK1..NK8)` trong `xt_nganh_tohop`. Flag = 1 nghĩa là tổ hợp yêu cầu môn đó.

**Pseudo-code:**

```java
boolean isComboValid(NganhTohop combo, CandidateScore scores) {
    if (combo.isTO()   && scores.getTO()   == 0) return false;
    if (combo.isLI()   && scores.getLI()   == 0) return false;
    if (combo.isHO()   && scores.getHO()   == 0) return false;
    if (combo.isSI()   && scores.getSI()   == 0) return false;
    if (combo.isVA()   && scores.getVA()   == 0) return false;
    if (combo.isSU()   && scores.getSU()   == 0) return false;
    if (combo.isDI()   && scores.getDI()   == 0) return false;
    if (combo.isN1()   && scores.getN1_CC()== 0) return false;
    if (combo.isCNCN() && scores.getCNCN()  == 0) return false;
    if (combo.isCNNN() && scores.getCNNN()  == 0) return false;
    if (combo.isKTPL() && scores.getKTPL()  == 0) return false;
    // NK1–NK8
    for (int i = 1; i <= 8; i++) {
        if (combo.isNK(i) && scores.getNK(i) == 0) return false;
    }
    return true;
}
```

**Hiệu quả:** Với ~4.000 thí sinh × 54 tổ hợp, skip sớm giảm tổng số phép tính từ ~216.000 xuống ~20.000–40.000 (tùy phân bố điểm thực tế).

---

### 2.4 Xử lý điểm Tiếng Anh & Chứng chỉ Ngoại ngữ

#### Hai trường hợp tách biệt:

**Trường hợp 1: Tổ hợp CÓ môn Tiếng Anh (flag N1 = 1)**

```
N1_CC = MAX(N1_THI, best_cert_diem_quy_doi)
```

- `N1_THI`: điểm thi thực tế môn Tiếng Anh (đã quy đổi về thang 10 nếu là V-SAT).
- `best_cert_diem_quy_doi`: điểm quy đổi của **chứng chỉ hợp lệ cho điểm cao nhất** (Q8). Nếu thí sinh có nhiều chứng chỉ (IELTS + VSTEP), chọn chứng chỉ nào cho `diem_quy_doi` cao nhất. Nếu không có chứng chỉ hợp lệ = 0.
- **Điều kiện hợp lệ của chứng chỉ (Q8):** `ngay_cap + 2 năm ≥ 30/06/2026`. Chứng chỉ hết hạn bị loại khỏi tính toán (reject tại import, error code `CERT_EXPIRED`).
- `N1_CC` được tính một lần và lưu vào `xt_diemthixettuyen.N1_CC` **trước khi chạy thuật toán**.
- Khi tính điểm tổ hợp → dùng `N1_CC` thay cho `N1_THI`.
- **Không cộng thêm ĐC** từ chứng chỉ trong trường hợp này.

**Trường hợp 2: Tổ hợp KHÔNG có môn Tiếng Anh (flag N1 = 0, VD: Toán–Lý–Hóa)**

- Điểm tổ hợp tính bình thường với 3 môn.
- Nếu thí sinh có chứng chỉ TA → cộng `diem_cong_cc` vào **ĐC**.
- Mức `diem_cong_cc` tra từ bảng `xt_bangquydoi` theo (loại chứng chỉ, điểm/bậc).

**Bảng quy đổi chứng chỉ (thang THPT — PT3 + PT4):**

| Chứng chỉ | Mức thấp → 8đ | Mức trung → 9đ | Mức cao → 10đ |
|-----------|--------------|----------------|--------------|
| IELTS | 4.0–5.0 | 5.5–6.5 | ≥7.0 |
| TOEFL ITP | 450–499 | 500–626 | ≥627 |
| TOEFL iBT | 30–45 | 46–93 | ≥94 |
| TOEIC (4kn) | 275–399 | 400–489 | ≥490 |
| PTE Academic | 43–58 | 59–75 | ≥76 |
| Linguaskill | 140–159 | 160–179 | ≥180 |
| Aptis ESOL | B1 | B2 | C/C1 |
| VSTEP | Bậc 3 | Bậc 4 | Bậc 5 |

**Điểm cộng ĐC khi tổ hợp KHÔNG có môn TA (thang 30):**
- Mức 8đ → +1.0; Mức 9đ → +1.5; Mức 10đ → +2.0.

**Đối với PT2 (ĐGNL, thang 1200) khi tổ hợp không có TA:**
- Mức 8đ → +40; Mức 9đ → +60; Mức 10đ → +80.

---

### 2.5 Công thức tính Điểm Tổ Hợp Xét Tuyển (ĐTHXT)

Áp dụng sau khi đã quy đổi điểm môn về thang 10:

```
ĐTHXT = (d1 × w1 + d2 × w2 + d3 × w3) / W × 10
```

Trong đó:
- `d1, d2, d3`: điểm từng môn (thang 10). Nếu tổ hợp có Tiếng Anh → dùng `N1_CC`.
- `w1, w2, w3`: hệ số môn từ `xt_nganh_tohop.hsmon1/2/3` (thông thường = 1; môn nhân đôi = 2).
- `W = w1 + w2 + w3`.
- **Kết quả ĐTHXT ∈ [0, 30].**

---

### 2.6 Công thức tính Điểm Tổ Hợp Gốc Xét Tuyển (ĐTHGXT)

Mỗi ngành có một **tổ hợp gốc** (`n_tohopgoc`). Tất cả tổ hợp phải quy về tổ hợp gốc để so sánh công bằng.

```
ĐTHGXT = ĐTHXT − dolech(tổ_hợp_đang_xét → tổ_hợp_gốc_ngành)
```

- `dolech` tra trong `xt_nganh_tohop.dolech`.
- Nếu tổ hợp đang xét = tổ hợp gốc → `dolech = 0`.

**Convention quan trọng — dấu của dolech:**
- Giá trị `dolech` trong DB có thể âm hoặc dương.
- **Khi quy về gốc: TRỪ đi `dolech`.**
- `dolech` dương → tổ hợp đó CAO HƠN gốc → sau khi trừ, điểm quy đổi về gốc sẽ thấp hơn.
- `dolech` âm → tổ hợp đó THẤP HƠN gốc → sau khi trừ (trừ số âm = cộng), điểm quy đổi về gốc sẽ cao hơn.

**Ví dụ:** A01 đạt 20 điểm, ngành gốc A00. `dolech(A01→A00) = −0.69`.
→ `ĐTHGXT = 20 − (−0.69) = 20.69` (A01 thấp hơn A00 nên cộng bù).

**Bảng độ lệch chuẩn THPT:**

| Tổ hợp xét ↓ \ Gốc → | A00 | A01 | B00 | C00 | C01 | D01 |
|----------------------|-----|-----|-----|-----|-----|-----|
| A00 | 0 | −0.69 | −1.21 | +2.32 | +0.94 | −0.68 |
| A01 | +0.69 | 0 | −0.52 | +3.01 | +1.63 | +0.01 |
| B00 | +1.21 | +0.52 | 0 | +3.53 | +2.15 | +0.53 |
| C00 | −2.32 | −3.01 | −3.53 | 0 | −1.38 | −3.00 |
| C01 | −0.94 | −1.63 | −2.15 | +1.38 | 0 | −1.62 |
| D01 | +0.68 | −0.01 | −0.53 | +3.00 | +1.62 | 0 |

---

### 2.7 Điểm Cộng (ĐC)

```
ĐC = ĐC_chungchi_TA + ĐC_hsg + ĐC_khac
ĐC ≤ 3.0 điểm (hard cap, thang 30)
```

#### 2.7.1 Điểm cộng HSG Quốc gia (thang 30 THPT)

| Loại giải | Môn đạt giải CÓ trong THXT | Môn đạt giải KHÔNG trong THXT |
|-----------|--------------------------|-------------------------------|
| Giải Nhất | +3.0 | +1.0 |
| Giải Nhì | +2.0 | +0.75 |
| Giải Ba | +1.5 | +0.5 |
| Giải KK / Tư | +1.0 | 0 |

#### 2.7.2 Điểm cộng HSG Tỉnh / Thành phố (thang 30 THPT)

| Loại giải | Môn CÓ trong THXT | Môn KHÔNG trong THXT |
|-----------|------------------|---------------------|
| Giải Nhất | +1.0 | +0.25 |
| Giải Nhì | +0.75 | 0 |
| Giải Ba | +0.5 | 0 |
| Giải KK | 0 | 0 |

> **Lưu ý V-SAT (thang 450):** Nhân hệ số 15. VD: Quốc gia Giải Nhất có môn = +45.
>
> **Xác định "môn có trong THXT":** Kiểm tra flag của môn đạt giải trong `xt_nganh_tohop`. Nếu flag = 1 → có trong tổ hợp đang xét tuyển.

---

### 2.8 Điểm Ưu Tiên (ĐƯT)

```
ĐƯT = ĐƯT_doi_tuong + ĐƯT_khu_vuc
```

**Hai thành phần cộng độc lập** — thí sinh có cả đối tượng lẫn khu vực được cộng cả hai.

#### 2.8.1 Mức điểm ưu tiên theo Đối tượng (MĐƯT_ĐT, thang 30)

| Mã ĐT | Đối tượng tiêu biểu | MĐƯT_ĐT |
|-------|---------------------|---------|
| ĐT 01 | Con liệt sĩ; con thương binh/bệnh binh mất ≥81% sức lao động; Anh hùng LLVT; Anh hùng Lao động | +2.0 |
| ĐT 02 | Con thương binh/bệnh binh mất 21–80%; con người nhiễm chất độc hóa học | +1.5 |
| ĐT 03 | Con của người hoạt động kháng chiến được tặng huân/huy chương kháng chiến | +1.0 |
| ĐT 04 | Người dân tộc thiểu số rất ít người (dưới 10.000 người theo QĐ Chính phủ) | +2.0 |
| ĐT 05 | Con của người hoạt động cách mạng trước 01/01/1945 | +0.5 |
| ĐT 06a | Người dân tộc thiểu số (không thuộc ĐT04) ở vùng KT-XH đặc biệt khó khăn | +0.5 |
| ĐT 07 | Tốt nghiệp THPT tại vùng KT-XH đặc biệt khó khăn; người khuyết tật | +0.25 |

**Ghi chú dân tộc tiêu biểu có ưu tiên ĐT06a:**
Tày, Nùng, Thái, Mường, Khơ Me, Hmông, Gia Rai, Ê Đê, Ba Na, Sán Cháy, Chăm, Thổ, Sán Dìu, Hoa (nếu ở vùng KT-XH đặc biệt khó khăn), và các dân tộc thiểu số khác theo danh sách Chính phủ.
*(Danh sách đầy đủ sẽ cập nhật theo văn bản hướng dẫn Bộ GD&ĐT năm tuyển sinh.)*

#### 2.8.2 Mức điểm ưu tiên theo Khu vực (MĐƯT_KV, thang 30)

| Mã KV | Mô tả | MĐƯT_KV |
|-------|-------|---------|
| KV1 | Xã đặc biệt khó khăn, vùng sâu, vùng xa, hải đảo, biên giới | +0.75 |
| KV2-NT | Nông thôn (không thuộc KV1 và KV3) | +0.5 |
| KV2 | Thành phố, thị xã (không thuộc KV3) | +0.25 |
| KV3 | Các quận nội thành TP trực thuộc TW (HN, HCM, HP, ĐN, Cần Thơ) | 0 |

#### 2.8.3 Công thức ĐƯT giảm dần khi điểm cao

Gọi `MĐƯT = MĐƯT_ĐT + MĐƯT_KV`:

```
Nếu (ĐTHGXT + ĐC) < 22.5:
    ĐƯT = MĐƯT

Nếu (ĐTHGXT + ĐC) ≥ 22.5:
    ĐƯT = MĐƯT × (30 − ĐTHGXT − ĐC) / 7.5
    (Giảm tuyến tính về 0 khi ĐTHGXT + ĐC = 30)
```

---

### 2.9 Điểm Xét Tuyển Cuối Cùng (ĐXT)

**Thứ tự áp cap ĐÚNG — phải follow đúng 6 bước này (Q6):**

```
Bước 1: Tính ĐTHGXT  (từ 2.6)
Bước 2: ĐC_raw = ĐC_HSG + ĐC_CC + ...
Bước 3: ĐC = min(ĐC_raw, 3.0)            ← cap ĐC trước
Bước 4: base = min(ĐTHGXT + ĐC, 30.0)   ← cap tổng trước khi tính ĐƯT
         (tránh ĐTHGXT+ĐC > 30 làm công thức ĐƯT cho kết quả âm)
Bước 5: ĐƯT = tính theo công thức 2.8 với base (thay vì ĐTHGXT+ĐC trực tiếp)
Bước 6: ĐXT = min(base + ĐƯT, 30.0)     ← hard cap cuối cùng
```

**Ví dụ nguy hiểm nếu không có bước 4:**
> ĐTHGXT = 28.5, ĐC = 3.0 → ĐTHGXT + ĐC = 31.5 > 30. Nếu dùng 31.5 vào công thức ĐƯT giảm dần: `MĐƯT × (30 − 31.5) / 7.5` → ĐƯT âm → ĐXT giảm xuống bất hợp lý. Dùng `base = min(31.5, 30.0) = 30.0` → `ĐƯT = MĐƯT × 0 / 7.5 = 0` → `ĐXT = 30.0`. Đúng.

**ĐXT là điểm dùng để sắp xếp (sort) và xét trúng tuyển.**

---

### 2.10 Thuật Toán Xét Tuyển — Chi tiết đầy đủ

#### Giai đoạn 0 — Tiền xử lý (Pre-scoring, chạy trước thuật toán)

1. **Tính N1_CC** từng thí sinh: `N1_CC = max(N1_THI, diem_quy_doi_CC)`. Ghi vào `xt_diemthixettuyen.N1_CC`.
2. **Tính ĐC** từng cặp (thí sinh × tổ hợp): ĐC_chứng chỉ + ĐC_HSG. Ghi vào `xt_diemcongxetuyen`.
3. **Tính ĐXT** từng cặp (thí sinh × nguyện vọng × tổ hợp × phương thức). Ghi vào `xt_nguyenvongxettuyen.diem_xettuyen`.

#### Giai đoạn 1 — Tuyển thẳng (PT1) — Ưu tiên tuyệt đối

**Quyết định kiến trúc (Q2):** Tuyển thẳng được ưu tiên TUYỆT ĐỐI, không kiểm tra chỉ tiêu. Căn cứ: Quy chế SGU mục 2.1.1 — "Hiệu trưởng xem xét từng hồ sơ để quyết định xét tuyển" — hàm ý Hiệu trưởng có quyền vượt chỉ tiêu dự kiến với diện tuyển thẳng giải quốc tế/quốc gia.

```
FOR EACH candidate c WITH wish nv WHERE nv.is_tuyen_thang = TRUE:
    // KHÔNG kiểm tra admittedCount — tuyển thẳng luôn được nhận
    MARK nv AS TRUNG_TUYEN_TUYENTHANG
    SET nv.tt_phuongthuc = 'PT1', nv.tt_thm = NULL
    admittedCount[nv.manganh]++
    nganh.sl_xtt++   // ← ghi vào cột sl_xtt (đã có trong xt_nganh)

// Sau PT1: cập nhật chỉ tiêu còn lại cho PT2/3/4
chitieu_con_lai[manganh] = max(0, n_chitieu - admittedCount[manganh])
// PT2/3/4 chỉ tuyển trong chitieu_con_lai
```

#### Giai đoạn 2 — Hàm tính điểm best cho một nguyện vọng

```
FUNCTION getBestScore(candidate c, wish w, major m):
    scores = []
    allowed_methods = methods_enabled_for_major(m)  // dựa theo flags

    FOR EACH method IN allowed_methods:
        validCombos = [combo FOR combo IN m.combos IF isComboValid(c, combo)]
        FOR EACH combo IN validCombos:
            // Quy đổi điểm môn về thang 10 theo method
            dthxt  = calcDTHXT(c, combo, method)
            dthgxt = calcDTHGXT(dthxt, combo.dolech)
            dc     = getDC(c.cccd, combo.matohop)    // từ xt_diemcongxetuyen
            dut    = calcDUT(dthgxt, dc, c.doituong, c.khuvuc)
            dxt    = MIN(dthgxt + dc + dut, 30.0)
            scores.add({ method, combo, dxt, dthgxt })

    IF scores.isEmpty(): RETURN null   // thí sinh không hợp lệ bất kỳ tổ hợp nào
    RETURN scores.maxBy(s => s.dxt)    // ← LẤY MAX toàn bộ (phương thức × tổ hợp)
```

#### Giai đoạn 3 — Phân bổ trúng tuyển

```
// Sắp xếp toàn bộ (candidate, wish) pairs theo ĐXT DESC
// Tie-break 3 cấp (xem 2.10.1 — đã bỏ tie-break thứ 4 timestamp)
sortedPairs = ALL (c, w) WHERE w.dxt IS NOT NULL
              sorted_by (dxt DESC, dthgxt DESC, diem_mon_chinh DESC, nv_tt ASC)

FOR EACH (c, w) IN sortedPairs:
    IF c đã trúng tuyển 1 NV nào rồi: SKIP
    major = getMajor(w.manganh)
    IF w.dxt < major.n_diemsan: CONTINUE                           // dưới ngưỡng
    IF admittedCount[w.manganh] >= chitieu_con_lai[w.manganh]:     // dùng chỉ tiêu còn lại sau PT1
        CONTINUE

    // Trúng tuyển
    w.nv_ketqua    = 'TRUNG_TUYEN'
    w.tt_phuongthuc = w.best_method
    w.tt_thm        = w.best_tohop
    admittedCount[w.manganh]++

// Đánh dấu trượt
FOR EACH (c, w) WHERE w.nv_ketqua IS NULL AND w.dxt IS NOT NULL:
    w.nv_ketqua = 'TRUOT'

// Đánh dấu lỗi dữ liệu (không tính được điểm)
FOR EACH (c, w) WHERE w.dxt IS NULL:
    w.nv_ketqua = 'KHONG_HOP_LE'
    w.ghichu    = 'Không có tổ hợp hợp lệ cho phương thức được phép'

// Ghi toàn bộ kết quả vào DB — 1 transaction duy nhất, batch 1000 (Q7)
repo.batchUpdateAllWishes(allWishes, chunkSize=1000)
```

#### 2.10.2 Xử lý lỗi giữa chừng thuật toán (Q3)

**Quyết định: Log + Continue, KHÔNG rollback.**

```java
// Trong AdmissionServiceImpl.run():
List<NguyenVong> results = new ArrayList<>();
List<AlgorithmError> errors = new ArrayList<>();

for (WishScore ws : sortedPairs) {
    try {
        BestScore best = getBestScore(ws.candidate, ws.wish, ws.major);
        // ... xét tuyển
        results.add(buildResult(ws, best));
    } catch (BusinessRuleException e) {
        // Log lỗi, đánh dấu NV này là ERROR, tiếp tục
        ws.wish.setNvKetqua("ERROR");
        ws.wish.setGhichu("Lỗi: " + e.getMessage());
        errors.add(new AlgorithmError(ws.candidate.getCccd(), ws.wish.getManganh(), e));
        results.add(ws.wish); // vẫn ghi vào results với status ERROR
    }
}

// Ghi tất cả — 1 transaction (Q7)
repo.batchUpdate(results, 1000);

// Trả về báo cáo lỗi cho UI
return new AdmissionResultDTO(results, errors);
```

**UI sau khi chạy xét tuyển phải hiển thị:**
- Số TRUNG_TUYEN / TRUOT / ERROR / KHONG_HOP_LE.
- Nếu `ERROR > 0` → cảnh báo đỏ, yêu cầu Admin kiểm tra và fix data trước khi phát hành kết quả.
- Nút "Xuất log lỗi" → file Excel danh sách NV có `nv_ketqua = 'ERROR'`.

#### 2.10.1 Tie-break — Cùng ĐXT (3 cấp — Q5 đã bỏ cấp timestamp)

Schema `xt_nguyenvongxettuyen` không có cột `created_at`/`nv_ngaynop`, và file import Nguyenvong.xlsx cũng không có dữ liệu này. Tie-break timestamp không khả thi → bỏ khỏi PRD.

**3 cấp tie-break được áp dụng (stable sort, `Collections.sort`):**

1. **ĐXT cao hơn** — điểm xét tuyển cuối cùng.
2. **ĐTHGXT cao hơn** — điểm thuần không tính ưu tiên (phân biệt thí sinh điểm học tập cao nhưng ít ưu tiên).
3. **Số thứ tự nguyện vọng thấp hơn** (`nv_tt` nhỏ = ưu tiên cao hơn của thí sinh).

> Nếu cả 3 cấp bằng nhau (xác suất cực thấp với dữ liệu thực tế ~15k NV) → thứ tự theo `idnv` (auto-increment khi import), đảm bảo sort deterministic.

---

### 2.11 Chiến lược Performance — Bảng xt_diemcongxetuyen

**Vấn đề:** 4.000 thí sinh × 20 NV × 54 tổ hợp = **~4.3 triệu dòng tiềm năng**.

**Chiến lược 4 tầng:**

**Tầng 1 — Skip optimization (section 2.3):** Chỉ tính ĐC cho tổ hợp hợp lệ. Ước tính ~8 tổ hợp/thí sinh → ~32.000 dòng thực tế.

**Tầng 2 — In-memory computation:**
```java
// 1. Load toàn bộ thí sinh + điểm thi vào Map<cccd, CandidateData>
// 2. Load toàn bộ bonus input (HSG, chứng chỉ) vào Map<cccd, List<BonusInput>>
// 3. Tính ĐC cho mọi cặp hợp lệ — hoàn toàn trong memory
// 4. Batch write kết quả 1 lần: INSERT ... ON DUPLICATE KEY UPDATE, chunk 1000
```

**Tầng 3 — Index DB:**
```sql
CREATE INDEX idx_dc_cccd_tohop ON xt_diemcongxetuyen(ts_cccd, matohop);
CREATE INDEX idx_nt_flags ON xt_nganh_tohop(manganh, TO, LI, HO, SI, VA, DI, N1);
```

**Tầng 4 — Tính ĐC on-the-fly (tuỳ chọn):** Thay vì pre-compute toàn bộ ĐC vào DB, tính ĐC trực tiếp từ HashMap trong vòng lặp thuật toán. Chỉ persist ĐC cho dòng cuối cùng trúng tuyển. Giảm ghi DB xuống ~4.000 dòng.

---

## 3. Yêu cầu chức năng

### 3.1 Module Auth & User Management

| ID | Chức năng | Role |
|----|-----------|------|
| AU-01 | Đăng nhập Desktop (bcrypt) | All |
| AU-02 | Phân quyền ADMIN / USER | ADMIN |
| AU-03 | Đổi mật khẩu | All |
| AU-04 | Enable / Disable tài khoản | ADMIN |
| AU-05 | Xem danh sách, sửa user | ADMIN |

### 3.2 Module Quản lý Ngành (xt_nganh)

| ID | Chức năng | Ghi chú |
|----|-----------|---------|
| NG-01 | Import CSV/Excel ngành | Batch insert, validate mã ngành |
| NG-02 | CRUD ngành | Set/unset 4 flag phương thức |
| NG-03 | Nhập chỉ tiêu + ngưỡng đầu vào | |
| NG-04 | Xem danh sách (phân trang 20/page) | |
| NG-05 | Tìm kiếm theo mã, tên | |

**Validation Import:** `manganh` duy nhất; `n_chitieu` ≥ 0; `n_diemsan` ≥ 0; ít nhất 1 flag = 'Y'.

### 3.3 Module Quản lý Tổ Hợp Môn

| ID | Chức năng | Ghi chú |
|----|-----------|---------|
| TH-01 | Import tổ hợp môn | |
| TH-02 | CRUD tổ hợp môn | |
| TH-03 | Import ánh xạ Ngành–Tổ hợp | Hệ số môn + dolech + flag môn |
| TH-04 | CRUD ánh xạ | |
| TH-05 | Cấu hình flag môn | Đánh dấu môn nào = 1 để dùng cho skip optimization |

### 3.4 Module Quản lý Thí Sinh

| ID | Chức năng | Ghi chú |
|----|-----------|---------|
| TS-01 | Import Excel thí sinh | Batch, validate đầy đủ |
| TS-02 | Xem danh sách (phân trang 20/page) | |
| TS-03 | Tìm kiếm theo CCCD, họ tên | |
| TS-04 | Xem chi tiết | |
| TS-05 | Sửa thông tin | |

**Validation:**

| Field | Rule | Error Code |
|-------|------|-----------|
| CCCD | Duy nhất trong file + DB, 12 chữ số | `CCCD_DUPLICATE` / `CCCD_FORMAT_INVALID` |
| Ngày sinh | dd/MM/yyyy | `DATE_FORMAT_INVALID` |
| ĐTƯT | ∈ {01, 02, 03, 04, 05, 06a, 07, null} | `INVALID_DOITUONG` |
| KVƯT | ∈ {1, 2NT, 2, 3, null} | `INVALID_KHUVUC` |
| Điểm THPT | Decimal(8,2) ∈ [0.00, 10.00] | `SCORE_OUT_OF_RANGE` |

**Import strategy (Q4 — upsert):** Dùng `INSERT ... ON DUPLICATE KEY UPDATE` thay vì INSERT thuần. Nếu CCCD đã tồn tại → UPDATE thông tin mới (không fail batch). Ghi log "updated" vào `ImportResultDTO.updateCount`.

### 3.5 Module Quản lý Điểm Thi

| ID | Chức năng | Ghi chú |
|----|-----------|---------|
| DT-01 | Import điểm THPT | |
| DT-02 | Import điểm V-SAT | |
| DT-03 | Import điểm ĐGNL | |
| DT-04 | Xem, sửa điểm đơn lẻ | |
| DT-05 | Thống kê điểm theo môn | Histogram |

**Schema Update bắt buộc:**

```sql
-- 1. Thêm NK3–NK8
ALTER TABLE `xt_diemthixettuyen`
  ADD COLUMN `NK3` decimal(8,2) DEFAULT NULL COMMENT 'Điểm năng khiếu 3',
  ADD COLUMN `NK4` decimal(8,2) DEFAULT NULL COMMENT 'Điểm năng khiếu 4',
  ADD COLUMN `NK5` decimal(8,2) DEFAULT NULL COMMENT 'Điểm năng khiếu 5',
  ADD COLUMN `NK6` decimal(8,2) DEFAULT NULL COMMENT 'Điểm năng khiếu 6',
  ADD COLUMN `NK7` decimal(8,2) DEFAULT NULL COMMENT 'Điểm năng khiếu 7',
  ADD COLUMN `NK8` decimal(8,2) DEFAULT NULL COMMENT 'Điểm năng khiếu 8';

-- 2. Thêm ngay_cap cho bảng chứng chỉ ngoại ngữ (Q8)
-- (lưu trong xt_diemcongxetuyen hoặc bảng riêng xt_chungchi_ngoaingu)
-- Nếu dùng xt_diemcongxetuyen, thêm:
ALTER TABLE `xt_diemcongxetuyen`
  ADD COLUMN `ngay_cap` date DEFAULT NULL COMMENT 'Ngày cấp chứng chỉ ngoại ngữ';
```

**Rule NK (Q3 — làm rõ):** NK chỉ tham gia tính điểm khi ngành có ít nhất 1 tổ hợp với flag `NK_i = 1`. Nếu ngành Kế toán không có tổ hợp nào dùng NK, điểm NK thí sinh đó không bao giờ được xét — **không cần reject khi import**, skip optimization tự xử lý.

**Rule NK:** Áp dụng cho ngành Sư phạm Âm nhạc, Mỹ thuật, Giáo dục Mầm non. NK_i = 0 → skip tổ hợp có môn NK_i (áp dụng rule section 2.3).

### 3.6 Module Bảng Quy Đổi

| ID | Chức năng | Ghi chú |
|----|-----------|---------|
| QD-01 | Import quy đổi chứng chỉ TA | `d_phuongthuc = 'NGOAINGU'`, UI có field chọn `nam_hoc` |
| QD-02 | Import bách phân vị V-SAT | `d_phuongthuc = 'VSAT'`, UI có field chọn `nam_hoc` |
| QD-03 | Import bách phân vị ĐGNL | `d_phuongthuc = 'DGNL'`, UI có field chọn `nam_hoc` |
| QD-04 | CRUD | |
| QD-05 | Tìm kiếm | Filter theo phương thức + `nam_hoc` |
| QD-06 | Xem lịch sử các năm | So sánh bảng 2025 vs 2026 để audit |

**Quy tắc import (Q1):**
- Khi import bảng mới năm 2026: **INSERT thêm**, không xóa năm 2025.
- Hệ thống giữ cả hai năm để hỗ trợ kiểm tra/audit điểm trúng tuyển năm trước.
- Sau khi import thành công → `ImportService` gọi `conversionService.invalidateCache(2026)`.
- UI hiển thị indicator trạng thái cache: "Bảng quy đổi đang dùng: **năm 2026** ✓".

### 3.7 Module Điểm Cộng & Ưu Tiên

| ID | Chức năng | Ghi chú |
|----|-----------|---------|
| DC-01 | Import ĐC HSG quốc gia + tỉnh/TP | |
| DC-02 | Import ĐC chứng chỉ TA (kèm `ngay_cap`) | Validate hết hạn: `ngay_cap + 2 năm ≥ 30/06/2026` |
| DC-03 | Xem, sửa, xóa | |
| DC-04 | Tính toán ĐC tổng hợp per (thí sinh × tổ hợp) | Trigger từ Xét Tuyển |

**Validation chứng chỉ TA khi import (Q8):**

| Tình huống | Xử lý | Error Code |
|-----------|-------|-----------|
| `ngay_cap + 2 năm < 30/06/2026` | Reject dòng đó | `CERT_EXPIRED` |
| Thí sinh có nhiều chứng chỉ hợp lệ | Hệ thống tự chọn chứng chỉ cho `diem_quy_doi` cao nhất khi tính điểm | — |
| Không có field `ngay_cap` trong file | Reject, yêu cầu nhập | `MISSING_REQUIRED_FIELD` |

### 3.8 Module Nguyện Vọng

| ID | Chức năng | Ghi chú |
|----|-----------|---------|
| NV-01 | Import Excel nguyện vọng | Validate CCCD + manganh tồn tại |
| NV-02 | Xem NV theo thí sinh | |
| NV-03 | Xem NV theo ngành | |
| NV-04 | Sửa NV | Chỉ trước khi chạy xét tuyển |

### 3.9 Module Xét Tuyển (Core Engine)

| ID | Chức năng | Ghi chú |
|----|-----------|---------|
| XT-01 | Tính N1_CC tất cả thí sinh | Pre-requisite — chạy trước XT-02 |
| XT-02 | Tính ĐC tất cả (thí sinh × tổ hợp) | In-memory, batch write cuối |
| XT-03 | Tính ĐXT tất cả nguyện vọng | Ghi vào DB |
| XT-04 | Chạy thuật toán xét tuyển | Toàn bộ in-memory → 1 transaction ghi DB cuối (Q7) |
| XT-05 | Xem kết quả theo ngành | TRUNG_TUYEN / TRUOT / ERROR / KHONG_HOP_LE |
| XT-06 | Xuất Excel trúng tuyển | Explicit Comparator 4 cấp (Q6); label PT tiếng Việt |
| XT-07 | Xuất log lỗi thuật toán | File Excel NV có `nv_ketqua = 'ERROR'` để Admin review |
| XT-08 | Reset kết quả | ADMIN only — xóa nv_ketqua, tt_phuongthuc, tt_thm |

**Transaction strategy (Q7):**
- Thuật toán chạy **hoàn toàn in-memory** (không ghi DB trong vòng lặp).
- Sau khi loop kết thúc → **1 transaction** `batchUpdate` toàn bộ kết quả, chunk 1000.
- Nếu ghi DB thất bại → rollback toàn bộ (DB về trạng thái sạch) → Admin chạy lại.
- Không có race condition trên `admittedCount` vì thuật toán single-threaded.

**Sau khi chạy XT-04, UI hiển thị summary:**
```
✅ TRUNG_TUYEN:      4,231
✅ TRUNG_TUYEN PT1:     18  (tuyển thẳng)
❌ TRUOT:           10,898
⚠️  ERROR:               0   ← nếu > 0, cảnh báo Admin
⚠️  KHONG_HOP_LE:        0
```

### 3.10 Module Báo Cáo

| ID | Chức năng |
|----|-----------|
| BC-01 | Thống kê đăng ký per ngành |
| BC-02 | Histogram điểm theo môn |
| BC-03 | Tỉ lệ trúng/trượt per ngành |
| BC-04 | Xuất danh sách trúng tuyển đầy đủ |

### 3.11 Web Portal Thí sinh — Giai đoạn 2 (Spring MVC)

**Mô hình MVC:**
```
Browser → Spring DispatcherServlet
    → Controller (xử lý request, gọi Service, bind Model)
    → Service Layer (đọc DB qua Repository — cùng Service với Desktop)
    → Thymeleaf View (render HTML)
```

| ID | URL | Method | Chức năng |
|----|-----|--------|-----------|
| W-01 | `/register` | GET | Form đăng ký |
| W-02 | `/register` | POST | Tạo tài khoản: validate CCCD tồn tại trong DB, hash password |
| W-03 | `/login` | GET | Form đăng nhập |
| W-04 | `/login` | POST | Xác thực CCCD + password |
| W-05 | `/diem` | GET | Xem điểm thi (auth required) |
| W-06 | `/ket-qua` | GET | Xem kết quả xét tuyển (auth required) |
| W-07 | `/logout` | POST | Đăng xuất |

**Bảo mật:** Spring Security, session cookie, bcrypt, rate-limit 5 lần/phút/IP.

---

## 4. Yêu cầu kỹ thuật & Kiến trúc phân lớp

### 4.1 Kiến trúc tổng thể

```
┌──────────────────────────────────────────────────────────────────┐
│                     PRESENTATION LAYER                            │
│  ┌─────────────────────────┐  ┌──────────────────────────────┐  │
│  │  Java Swing (Admin)     │  │  Spring MVC (Web Phase 2)    │  │
│  │  JFrame/JPanel          │  │  Controller + Thymeleaf View │  │
│  │  SwingWorker (async)    │  │  Spring Security + Session   │  │
│  └───────────┬─────────────┘  └──────────────┬───────────────┘  │
└──────────────┼──────────────────────────────┼───────────────────┘
               │ Interface (Constructor DI)   │
┌──────────────▼──────────────────────────────▼───────────────────┐
│                     SERVICE LAYER                                 │
│  ScoringService · AdmissionService · ScoreConversionService      │
│  BonusPointService · ImportService · ValidationService           │
│  (Nhận/trả DTO thuần — KHÔNG phụ thuộc DB)                      │
└─────────────────────────────┬───────────────────────────────────┘
                               │ Repository Interface
┌─────────────────────────────▼───────────────────────────────────┐
│                  DATA ACCESS LAYER (Repository)                   │
│                Hibernate 6 + HikariCP + MySQL 8                   │
└─────────────────────────────────────────────────────────────────┘
```

### 4.2 Package Structure

```
vn.edu.sgu.tuyen/
├── admin/ui/                         # Presentation Layer — Swing
│   ├── MainFrame.java
│   ├── common/
│   │   ├── ErrorLogDialog.java       # Bảng hiển thị lỗi import
│   │   └── ProgressDialog.java       # JProgressBar cơ bản
│   ├── nganh/     · thisinh/  · diem/
│   ├── xettuyen/
│   │   ├── XetTuyenPanel.java
│   │   └── KetQuaSummaryDialog.java
│   └── baocao/
│
├── web/                              # Phase 2 — Spring MVC (làm sau)
│   ├── controller/
│   └── view/  (Thymeleaf .html)
│
├── service/
│   ├── interfaces/
│   │   ├── IScoringService.java
│   │   ├── IAdmissionService.java
│   │   ├── IScoreConversionService.java
│   │   ├── IBonusPointService.java
│   │   └── IImportService.java
│   └── impl/
│       ├── ScoringServiceImpl.java        # 6 bước cap — logic không đổi
│       ├── AdmissionServiceImpl.java      # PT1 ưu tiên; tính in-memory; lưu for loop
│       ├── ScoreConversionServiceImpl.java  # HashMap load 1 lần khi khởi động
│       └── BonusPointServiceImpl.java      # max cert; expiry check
│
├── service/dto/
│   ├── CandidateScoreInput.java
│   ├── ScoreResultDTO.java
│   ├── AdmissionResultDTO.java
│   └── ImportResultDTO.java            # successCount + updateCount + errors
│
├── repository/
│   ├── interfaces/  (extends JpaRepository — dùng saveAll đơn giản)
│   └── impl/
│
├── entity/
│   ├── ThiSinh.java
│   ├── DiemThi.java                     # NK1–NK8
│   ├── Nganh.java                       # 4 PT flags + sl_xtt
│   ├── NganhTohop.java                  # flags + dolech + hsmon
│   ├── NguyenVong.java                  # tt_phuongthuc + tt_thm + ghichu
│   ├── BangQuydoi.java                  # + nam_hoc
│   └── DiemCong.java                    # + ngay_cap
│
└── util/
    ├── ExcelParser.java                  # Apache POI wrapper đơn giản
    ├── InterpolationUtil.java            # Nội suy tuyến tính
    └── AppException.java                 # Custom exceptions
```

> **Lưu ý khi bảo vệ đồ án:** Dù code đơn giản hơn, kiến trúc 3 tầng vẫn phải rõ ràng — `ScoringService` không biết DB, `Repository` không chứa business logic. Đây là điểm giám khảo sẽ hỏi.

### 4.3 Decoupling — ScoringService thuần túy

Kiến trúc phân lớp vẫn giữ nguyên — đây là điểm cốt lõi để bảo vệ đồ án. Chỉ đơn giản hóa phần implement bên trong.

```java
// Input DTO: không chứa @Entity — vẫn giữ để đảm bảo decoupling
public class CandidateScoreInput {
    private Map<String, Double> subjectScores; // {"TO":8.5, "N1_CC":10.0...}
    private String doiTuong;
    private String khuVuc;
    private double diemCongRaw;
}

// ScoringServiceImpl — 6 bước cap đúng thứ tự (LOGIC KHÔNG ĐỔI)
public ScoreResultDTO calculate(CandidateScoreInput input, ComboConfig combo) {
    double dthxt  = calcDTHXT(input, combo);
    double dthgxt = calcDTHGXT(dthxt, combo.getDolech());
    double dc     = Math.min(input.getDiemCongRaw(), 3.0);  // bước 3
    double base   = Math.min(dthgxt + dc, 30.0);            // bước 4
    double dut    = calcDUT(base, input.getDoiTuong(), input.getKhuVuc()); // bước 5
    double dxt    = Math.min(base + dut, 30.0);             // bước 6
    return new ScoreResultDTO(dxt, dthgxt, dc, dut, combo.getMethod(), combo.getMaTohop());
}
```

**Cache bảng quy đổi — dùng HashMap đơn giản (v3.0):**

Thay vì `ConcurrentHashMap` + invalidate, dùng `HashMap` load 1 lần khi khởi động. Nếu Admin import bảng mới → **tắt và mở lại app** là đủ với quy mô đồ án.

```java
// ScoreConversionServiceImpl — đơn giản hóa
@Service
public class ScoreConversionServiceImpl implements IScoreConversionService {
    // Load 1 lần khi Spring khởi động bean này
    private final Map<String, List<Interval>> tableCache;

    public ScoreConversionServiceImpl(BangQuydoiRepository repo) {
        // Load toàn bộ bảng quy đổi năm 2026 vào Map một lần
        this.tableCache = repo.findByNamHoc(2026).stream()
            .collect(Collectors.groupingBy(b -> b.getPhuongThuc() + "_" + b.getMon()));
    }

    public double convert(double rawScore, String phuongThuc, String mon) {
        List<Interval> table = tableCache.get(phuongThuc + "_" + mon);
        if (table == null) return rawScore; // không có bảng → trả về nguyên
        return InterpolationUtil.interpolate(rawScore, table);
    }
    // Không cần invalidateCache() — tắt/bật app là đủ
}
```

**Ghi kết quả xét tuyển — vòng for đơn giản (v3.0):**

```java
// AdmissionServiceImpl — sau khi tính xong toàn bộ, lưu tuần tự
public void persistResults(List<NguyenVong> results) {
    for (NguyenVong nv : results) {
        nguyenVongRepository.save(nv);  // Hibernate tự flush theo batch_size config
    }
    // Không cần transaction thủ công — @Transactional trên method là đủ
}
```

### 4.4 Import File — Cách đơn giản (năm 3)

```
File Excel
  ↓ Apache POI đọc toàn bộ → List<DTO>
  ↓ Vòng for validate từng dòng → gom List<RowError>
  ↓ Vòng for lưu từng dòng hợp lệ:
      repository.save(entity)   ← đơn giản, Hibernate tự commit
      hoặc repository.saveAll(validList)  ← nhanh hơn một chút
  ↓ SwingWorker cơ bản: chạy nền để UI không đơ
      worker.publish(i)  →  JProgressBar.setValue(i * 100 / total)
  ↓ ErrorLogDialog.show(errors)
Thời gian chấp nhận: 3–5 phút cho 43k dòng. Hoàn toàn OK với app nội bộ.
```

**Lưu ý quan trọng:** `saveAll()` của Spring Data JPA / Hibernate vẫn gom nhiều INSERT trong 1 batch ngầm (nếu bật `spring.jpa.properties.hibernate.jdbc.batch_size=50`). Team chỉ cần thêm 1 dòng config là có được hiệu năng tốt hơn mà không cần code phức tạp.

```properties
# application.properties — thêm 1 dòng này để saveAll() nhanh hơn
spring.jpa.properties.hibernate.jdbc.batch_size=50
spring.jpa.properties.hibernate.order_inserts=true
```

**SwingWorker cơ bản — đủ để UI không đơ:**

```java
SwingWorker<ImportResultDTO, Integer> worker = new SwingWorker<>() {
    @Override
    protected ImportResultDTO doInBackground() {
        List<RowError> errors = new ArrayList<>();
        for (int i = 0; i < validList.size(); i++) {
            repository.save(mapper.toEntity(validList.get(i)));
            publish(i + 1);  // cập nhật progress
        }
        return new ImportResultDTO(validList.size(), errors);
    }
    @Override
    protected void process(List<Integer> chunks) {
        progressBar.setValue(chunks.get(chunks.size()-1) * 100 / total);
    }
    @Override
    protected void done() {
        errorLogDialog.show(get());
    }
};
worker.execute();
```

### 4.5 Tiêu Chuẩn Error Handling

```java
AppException
├── ValidationException
├── DuplicateKeyException
├── EntityNotFoundException
├── ImportException
├── BusinessRuleException
└── AlgorithmException        // Lỗi trong thuật toán xét tuyển

// ImportResultDTO
public class ImportResultDTO {
    private String importId;        // UUID
    private String fileName;
    private LocalDateTime importAt;
    private int totalRows;
    private int successCount;
    private int updateCount;
    private int failCount;
    private List<RowError> errors;

    public static class RowError {
        private int rowNumber;      // Số dòng Excel (bắt đầu từ 2)
        private String identifier;  // CCCD hoặc mã nhận dạng
        private String fieldName;   // Tên cột bị lỗi
        private String errorCode;   // Mã lỗi chuẩn
        private String rawValue;    // Giá trị gốc trong file
        private String message;     // Thông báo thân thiện tiếng Việt
    }
}
```

**Error Code chuẩn hóa:**

| Error Code | Ý nghĩa | Scope |
|------------|---------|-------|
| `CCCD_DUPLICATE` | CCCD đã tồn tại trong DB | Import TS |
| `CCCD_DUPLICATE_IN_FILE` | CCCD trùng trong cùng file import | Import TS |
| `CCCD_FORMAT_INVALID` | CCCD không phải 12 chữ số | Import TS |
| `SCORE_OUT_OF_RANGE` | Điểm không thuộc [0, 10] | Import Điểm |
| `SCORE_FORMAT_INVALID` | Điểm không phải số hợp lệ | Import Điểm |
| `REFERENCE_NOT_FOUND` | CCCD / mã ngành tham chiếu không tồn tại | Import NV, ĐC |
| `DATE_FORMAT_INVALID` | Ngày sinh sai định dạng dd/MM/yyyy | Import TS |
| `MISSING_REQUIRED_FIELD` | Thiếu trường bắt buộc | Import chung |
| `INVALID_DOITUONG` | Đối tượng ưu tiên không hợp lệ | Import TS |
| `INVALID_KHUVUC` | Khu vực ưu tiên không hợp lệ | Import TS |
| `MAJOR_NO_METHOD_FLAG` | Ngành không bật phương thức nào | Import Ngành |
| `INTERPOLATION_OUT_OF_TABLE` | Điểm ngoài range bảng bách phân vị | Tính điểm |
| `CERT_EXPIRED` | Chứng chỉ hết hạn (`ngay_cap + 2yr < 30/06/2026`) | Import CC (Q8) |
| `CERT_MISSING_DATE` | Thiếu `ngay_cap` khi import chứng chỉ | Import CC (Q8) |
| `ALGORITHM_ERROR` | Lỗi nghiệp vụ trong thuật toán xét tuyển | Xét tuyển (Q3) |

**Hiển thị lỗi Swing:**

```
╔════════════════════════════════════════════════════════════╗
║  KẾT QUẢ IMPORT — Ds_thi_sinh.xlsx                       ║
╠════════════════════════════════════════════════════════════╣
║  ✅ Thành công:  42,514 dòng                               ║
║  ❌ Lỗi:            486 dòng                               ║
╠═════╦══════════════╦══════════════════════╦══════════════╣
║ Dòng║ CCCD         ║ Mã lỗi               ║ Chi tiết     ║
╠═════╬══════════════╬══════════════════════╬══════════════╣
║  15 ║ 058304001234 ║ CCCD_DUPLICATE       ║ Đã tồn tại   ║
║  89 ║ TS_00456     ║ SCORE_OUT_OF_RANGE   ║ TO=11.5 >10  ║
║ 234 ║ TS_00789     ║ INVALID_DOITUONG     ║ "09" không hợp lệ ║
╚═════╩══════════════╩══════════════════════╩══════════════╝
[ Xuất file lỗi .xlsx ]              [ Đóng ]
```

### 4.6 Database — Index Strategy

```sql
-- Thêm vào sau ALTER TABLE NK3-NK8:
CREATE INDEX idx_nv_cccd_tt    ON xt_nguyenvongxettuyen(nn_cccd, nv_tt);
CREATE INDEX idx_nv_nganh      ON xt_nguyenvongxettuyen(nv_manganh);
CREATE INDEX idx_dc_cccd_tohop ON xt_diemcongxetuyen(ts_cccd, matohop);
CREATE INDEX idx_nt_flags      ON xt_nganh_tohop(manganh, TO, LI, HO, SI, VA, DI, N1);
CREATE INDEX idx_qd_pt_mon     ON xt_bangquydoi(d_phuongthuc, d_mon);
```

### 4.7 Tech Stack

| Thành phần | Lựa chọn | Ghi chú |
|-----------|---------|---------|
| Language | Java 17 LTS | |
| Admin UI | Java Swing | |
| Web Phase 2 | Spring Boot 3 + Spring MVC + Thymeleaf | Làm sau |
| ORM | Hibernate 6 / Spring Data JPA | Dùng `JpaRepository.saveAll()` — đủ đơn giản |
| DB | MySQL 8.0 | |
| Excel | Apache POI 5.x | Đọc file + ghi báo cáo |
| Build | Maven | |
| Test | JUnit 5 + Mockito | 4 test class chính |
| Web Security | Spring Security 6 | Phase 2 |
| Pool | HikariCP | Config mặc định của Spring Boot là đủ |

**Config tối thiểu cần thêm vào `application.properties`:**

```properties
# Tăng tốc saveAll() mà không cần code phức tạp
spring.jpa.properties.hibernate.jdbc.batch_size=50
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.order_updates=true
```

---

## 5. Kế hoạch phân việc 5 tuần

### Phân công

| Vai trò | SL | Trách nhiệm chính |
|---------|---|------------------|
| Lead Backend | 1 | Architecture, ScoringService, AdmissionService, Code Review |
| Backend Dev | 3 | Import, Repository, ScoreConversion, BonusPoint |
| Frontend Swing | 3 | UI panels, Import/Progress dialog, Xét tuyển, Báo cáo |
| QA/PM | 1 | Test case, verify nghiệp vụ, bug tracking |

---

### Tuần 1 — Foundation & Schema

| Task | Mô tả | Người | Done When |
|------|-------|-------|-----------|
| W1-BE-01 | Spring Boot, Hibernate config, MySQL connect | Lead BE | App start OK |
| W1-BE-02 | Entity classes từ SQL + ALTER TABLE NK3–NK8 + `nam_hoc` + `ngay_cap` | BE-2 | Schema validate, `@Entity` map đúng |
| W1-BE-03 | `JpaRepository` interfaces cho tất cả entity | BE-3 | Compile + test `findAll`/`save` |
| W1-BE-04 | Auth module: login, bcrypt, session, phân quyền | BE-4 | Login OK |
| W1-FE-01 | MainFrame + menu + layout chuẩn | FE-1 | App hiển thị |
| W1-FE-02 | LoginFrame + phân quyền ẩn/hiện menu | FE-2 | Flow hoàn chỉnh |
| W1-FE-03 | `ErrorLogDialog` + `ProgressDialog` cơ bản | FE-3 | Dialog hiển thị đúng |
| W1-QA-01 | Soạn test case nghiệp vụ: tính điểm, xét NV, tie-break (20+ cases) | QA | Doc hoàn chỉnh |

---

### Tuần 2 — CRUD & Import

| Task | Mô tả | Người | Done When |
|------|-------|-------|-----------|
| W2-BE-01 | `ImportService` + `ExcelParser` (POI) + `ValidationService` + `saveAll()` | Lead BE | Import file test < 5 phút, error log đúng |
| W2-BE-02 | `ValidationService`: CCCD, điểm, ngày, doi_tuong, khu_vuc, `CERT_EXPIRED` | BE-2 | Unit test 15+ cases pass |
| W2-BE-03 | Service + Repo: Nganh, TohopMon, NganhTohop | BE-3 | CRUD hoàn chỉnh |
| W2-BE-04 | Service + Repo: ThiSinh, DiemThi (NK1–NK8), DiemCong, BangQuydoi | BE-4 | CRUD hoàn chỉnh |
| W2-FE-01 | Import Dialog: file picker + `SwingWorker` cơ bản + `JProgressBar` + `ErrorLogDialog` | FE-1 | Test file thật, UI không đơ |
| W2-FE-02 | CRUD Ngành (JTable + form + 4 flag checkboxes) | FE-2 | CRUD hoàn chỉnh |
| W2-FE-03 | CRUD TohopMon + ánh xạ Ngành–Tổ hợp | FE-3 | CRUD hoàn chỉnh |
| W2-QA-01 | Test Import các file mẫu, ghi bug | QA | Bug list Tuần 2 |

---

### Tuần 3 — Scoring Engine & Admission Algorithm

| Task | Mô tả | Người | Done When |
|------|-------|-------|-----------|
| W3-BE-01 | `ScoreConversionService`: nội suy + `HashMap` load khi khởi động + tính N1_CC | Lead BE | Unit test 10+ cases kể cả boundary |
| W3-BE-02 | `BonusPointService`: ĐC_HSG (quốc gia + tỉnh) + ĐC_CC + max cert + expiry + cap 3.0 | BE-2 | Unit test từng rule |
| W3-BE-03 | `ScoringService`: ĐTHXT → ĐTHGXT → 6 bước cap → ĐXT; skip môn=0; MAX phương thức | Lead BE | Unit test 15+ cases, ĐXT không vượt 30 |
| W3-BE-04 | `AdmissionService`: PT1 ưu tiên; tie-break 3 cấp; lưu kết quả `for` loop | BE-3 | Test với data mẫu, kết quả match |
| W3-BE-05 | Repo: NguyenVong (load + `saveAll` kết quả) | BE-4 | Lưu 15k NV xong không crash |
| W3-FE-01 | UI Xét Tuyển: check tiền điều kiện + trigger từng bước + `SwingWorker` progress | FE-1 | Full flow không đơ |
| W3-FE-02 | UI Kết quả: filter TRUNG_TUYEN/TRUOT/ERROR, hiển thị phương thức + tổ hợp, xuất Excel | FE-2 | Export đúng |
| W3-FE-03 | UI Báo cáo: thống kê đăng ký + tỉ lệ trúng/trượt per ngành | FE-3 | Hiển thị đúng số |
| W3-QA-01 | Verify ĐXT 5–10 thí sinh mẫu bằng tay, so sánh kết quả system | QA | Sai số ≤ 0.01 |

**Unit Test — phạm vi thực tế cho năm 3:**
- `ScoringServiceTest`: test 6 bước cap, `base` intermediate, ĐXT không vượt 30, ĐƯT không âm.
- `AdmissionServiceTest`: PT1 không check quota; dưới ngưỡng bị TRUOT; tie-break cấp 3.
- `ScoreConversionTest`: nội suy đúng tại boundary, clamp khi ngoài range.
- `BonusPointTest`: N1_CC = max(thi, cert); cert hết hạn bị loại; cap ĐC=3.

> Không cần test concurrency hay cache invalidation. 4 test class này là đủ để giám khảo thấy nhóm hiểu nghiệp vụ.

---

### Tuần 4 — Integration & System Test

| Task | Mô tả | Người |
|------|-------|-------|
| W4-01 | Fix bugs Tuần 3; performance test end-to-end | Lead BE + BE-2 |
| W4-02 | UI consistency, validation messages | FE-1 + FE-2 |
| W4-03 | End-to-end test: Import → N1_CC → ĐC → ĐXT → Xét → Kết quả | QA |
| W4-04 | Javadoc Service Layer | Lead BE + BE-3 |
| W4-05 | (Optional) Spring MVC skeleton cho web portal | BE-4 |

---

### Tuần 5 — Buffer, Demo & Documentation

| Task | Mô tả | Người |
|------|-------|-------|
| W5-01 | Demo script: full flow từ Import đến xuất kết quả | QA + Lead BE |
| W5-02 | Báo cáo đồ án: kiến trúc, sơ đồ class, flow nghiệp vụ | PM + Lead BE |
| W5-03 | (Optional) Web portal: Login + xem điểm + kết quả | BE-4 + FE-3 |
| W5-04 | Buffer: fix bugs, performance tuning | All |

---

## Phụ lục

### Phụ lục A — SQL Alter & Index (v2.1)

```sql
-- ============================================================
-- 1. Thêm NK3–NK8 vào bảng điểm thi
-- ============================================================
ALTER TABLE `xt_diemthixettuyen`
  ADD COLUMN `NK3` decimal(8,2) DEFAULT NULL COMMENT 'Điểm năng khiếu 3',
  ADD COLUMN `NK4` decimal(8,2) DEFAULT NULL COMMENT 'Điểm năng khiếu 4',
  ADD COLUMN `NK5` decimal(8,2) DEFAULT NULL COMMENT 'Điểm năng khiếu 5',
  ADD COLUMN `NK6` decimal(8,2) DEFAULT NULL COMMENT 'Điểm năng khiếu 6',
  ADD COLUMN `NK7` decimal(8,2) DEFAULT NULL COMMENT 'Điểm năng khiếu 7',
  ADD COLUMN `NK8` decimal(8,2) DEFAULT NULL COMMENT 'Điểm năng khiếu 8';

-- ============================================================
-- 2. Thêm nam_hoc vào bảng quy đổi (Q1 — versioning hàng năm)
-- ============================================================
ALTER TABLE `xt_bangquydoi`
  ADD COLUMN `nam_hoc` SMALLINT NOT NULL DEFAULT 2026
    COMMENT 'Năm học — bảng bách phân vị thay đổi mỗi năm';

-- Xóa unique key cũ (d_maquydoi không đủ để unique khi có nhiều năm)
ALTER TABLE `xt_bangquydoi` DROP INDEX `d_maquydoi_UNIQUE`;

-- Tạo unique key mới bao gồm nam_hoc
ALTER TABLE `xt_bangquydoi`
  ADD UNIQUE KEY `uq_qd_nam` (`d_phuongthuc`, `d_mon`, `d_diema`, `nam_hoc`);

-- ============================================================
-- 3. Thêm ngay_cap vào bảng điểm cộng (Q8 — cert expiry)
-- ============================================================
ALTER TABLE `xt_diemcongxetuyen`
  ADD COLUMN `ngay_cap` date DEFAULT NULL
    COMMENT 'Ngày cấp chứng chỉ ngoại ngữ — phải trong 2 năm tính đến 30/06/2026';

-- ============================================================
-- 4. Index cho performance
-- ============================================================
CREATE INDEX idx_nv_cccd_tt    ON xt_nguyenvongxettuyen(nn_cccd, nv_tt);
CREATE INDEX idx_nv_nganh      ON xt_nguyenvongxettuyen(nv_manganh);
CREATE INDEX idx_dc_cccd_tohop ON xt_diemcongxetuyen(ts_cccd, matohop);
CREATE INDEX idx_nt_flags      ON xt_nganh_tohop(manganh, `TO`, LI, HO, SI, VA, DI, N1);
CREATE INDEX idx_qd_pt_nam     ON xt_bangquydoi(d_phuongthuc, d_mon, nam_hoc);

-- ============================================================
-- 5. Cột chỉ tiêu còn lại sau tuyển thẳng (Q2)
--    sl_xtt đã có trong schema, chỉ cần dùng đúng
--    Verify: SELECT manganh, n_chitieu, sl_xtt FROM xt_nganh;
-- ============================================================
-- Không cần ALTER — sl_xtt đã tồn tại trong xt_nganh
-- Đảm bảo Hibernate Entity Nganh.java map cột sl_xtt đúng type Integer
```

### Phụ lục A.1 — Script kiểm tra toàn vẹn dữ liệu trước khi chạy xét tuyển

```sql
-- Kiểm tra 1: Ngành có chỉ tiêu > 0 và ít nhất 1 phương thức bật
SELECT manganh, tennganh, n_chitieu,
       n_tuyenthang, n_dgnl, n_vsat, n_thpt
FROM xt_nganh
WHERE n_chitieu <= 0
   OR (n_tuyenthang = 'N' AND n_dgnl = 'N' AND n_vsat = 'N' AND n_thpt = 'N');
-- Kết quả phải rỗng trước khi chạy thuật toán

-- Kiểm tra 2: Bảng bách phân vị năm 2026 đã import
SELECT d_phuongthuc, COUNT(*) as so_khoang, nam_hoc
FROM xt_bangquydoi
WHERE d_phuongthuc IN ('VSAT', 'DGNL') AND nam_hoc = 2026
GROUP BY d_phuongthuc, nam_hoc;
-- Phải có rows cho VSAT_2026 và DGNL_2026

-- Kiểm tra 3: Thí sinh có CCCD trong NV nhưng không có điểm thi
SELECT nv.nn_cccd
FROM xt_nguyenvongxettuyen nv
LEFT JOIN xt_diemthixettuyen dt ON nv.nn_cccd = dt.cccd
WHERE dt.cccd IS NULL
LIMIT 20;
-- Kết quả phải rỗng

-- Kiểm tra 4: Nguyện vọng tuyển thẳng hợp lệ
SELECT COUNT(*) as sl_tuyen_thang
FROM xt_nguyenvongxettuyen
WHERE tt_phuongthuc = 'PT1' OR nv_keys LIKE '%TUYEN_THANG%';
```

### Phụ lục B — Mapping File Excel → DB

| File | Bảng DB đích | Ghi chú |
|------|-------------|---------|
| `Ds_thi_sinh.xlsx` | `xt_thisinhxettuyen25` + `xt_diemthixettuyen` | NK1–NK8 đầy đủ |
| `Chi_tieu_2025.xlsx` | `xt_nganh.n_chitieu` | |
| `Nguong_dau_vao_2025.xlsx` | `xt_nganh.n_diemsan` | |
| `tohopmon.xlsx` | `xt_tohop_monthi` + `xt_nganh_tohop` | flags + dolech + hệ số |
| `Ds_quy_doi_tieng_Anh.xlsx` | `xt_bangquydoi` | phuongthuc='NGOAINGU' |
| `Uu_tien_xet_tuyen.xlsx` | `xt_diemcongxetuyen` | HSG quốc gia + tỉnh |
| `Nguyenvong.xlsx` | `xt_nguyenvongxettuyen` | |

### Phụ lục C — Viết tắt

| Ký hiệu | Diễn giải |
|---------|-----------|
| PT1/2/3/4 | Phương thức 1 (Tuyển thẳng) / 2 (ĐGNL) / 3 (V-SAT) / 4 (THPT) |
| ĐTHXT | Điểm Tổ Hợp Xét Tuyển |
| ĐTHGXT | Điểm Tổ Hợp Gốc Xét Tuyển |
| ĐC | Điểm Cộng (cộng từ HSG + chứng chỉ, cap 3.0) |
| ĐƯT | Điểm Ưu Tiên = ĐƯT_ĐT + ĐƯT_KV |
| ĐXT | Điểm Xét Tuyển = min(base + ĐƯT, 30.0) (cap 6 bước) |
| base | min(ĐTHGXT + ĐC, 30.0) — trung gian trước khi tính ĐƯT |
| N1_THI | Điểm Tiếng Anh thi thực tế |
| N1_CC | max(N1_THI, best_cert_diem_quy_doi) |
| NK1–NK8 | Điểm Năng Khiếu 1–8 |
| HSG | Học Sinh Giỏi |
| MĐƯT | Mức Điểm Ưu Tiên = MĐƯT_ĐT + MĐƯT_KV |
| CCCD | Căn Cước Công Dân |
| `sl_xtt` | Số lượng xét tuyển thẳng (tuyển ngoài chỉ tiêu thông thường) |
| `nam_hoc` | Năm học của bảng bách phân vị (VD: 2026) |
| `ngay_cap` | Ngày cấp chứng chỉ ngoại ngữ (validate ≤ 2 năm) |
| TRUNG_TUYEN | Kết quả xét tuyển: trúng tuyển |
| TRUOT | Kết quả xét tuyển: không trúng |
| ERROR | Kết quả xét tuyển: lỗi data, cần Admin kiểm tra |
| KHONG_HOP_LE | Kết quả xét tuyển: không có tổ hợp nào hợp lệ |

---

*— Hết PRD v2.1 —*

