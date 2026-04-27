# File Tree: app-tuyensinh

**Generated:** 4/26/2026, 10:56:08 PM
**Root Path:** `d:\app-tuyensinh`

```
├── 📁 .github
│   └── 📁 java-upgrade
│       ├── 📁 hooks
│       │   └── 📁 scripts
│       │       ├── 📄 recordToolUse.ps1
│       │       └── 📄 recordToolUse.sh
│       └── ⚙️ .gitignore
├── 📁 docs
│   ├── 📘 1.ThongTinTuyenSinh_SGU_2025_Thanh 13-6 - cuoi cung.docx
│   ├── 📄 Chi tieu 2025.xlsx
│   ├── 📄 Ds quy doi tieng Anh.xlsx
│   ├── 📄 Ds thi sinh.xlsx
│   ├── 📘 Dự án phần mềm tuyển sinh.docx
│   ├── 📄 Nganh.xlsx
│   ├── 📄 Nguong dau vao 2025.xlsx
│   ├── 📄 Nguyenvong.xlsx
│   ├── 📝 PRD.md
│   ├── 📕 PRD_apptuyensinh.pdf
│   ├── 📘 Quy doi diem thi V-SAT 2025.docx
│   ├── 📘 ThongTinTuyenSinh(TrinhKi)_2025_17-3 - In.doc
│   ├── 📄 Uu tien xet tuyen.xlsx
│   ├── 📘 cac cong thuc tinh.docx
│   ├── 🌐 prototype_basedon_PRD.html
│   ├── 📄 tohopmon.xlsx
│   ├── 📄 xettuyen2026_empty.sql
│   └── 📘 ~$ án phần mềm tuyển sinh.docx
├── 📁 src
│   ├── 📁 main
│   │   ├── 📁 java
│   │   │   └── 📁 com
│   │   │       └── 📁 sgu
│   │   │           └── 📁 tuyensinh
│   │   │               ├── 📁 admin
│   │   │               │   └── 📁 ui
│   │   │               │       ├── 📁 common
│   │   │               │       │   ├── ☕ BaseTablePanel.java
│   │   │               │       │   ├── ☕ ErrorLogDialog.java
│   │   │               │       │   ├── ☕ ExcelUtils.java
│   │   │               │       │   ├── ☕ ExportPanel.java
│   │   │               │       │   ├── ☕ ImportPanel.java
│   │   │               │       │   ├── ☕ ImportWorker.java
│   │   │               │       │   ├── ☕ MessageDialog.java
│   │   │               │       │   └── ☕ ProgressPanel.java
│   │   │               │       ├── ☕ BangQuyDoiPanel.java
│   │   │               │       ├── ☕ BaoCaoPanel.java
│   │   │               │       ├── ☕ DiemThiPanel.java
│   │   │               │       ├── ☕ DiemUuTienPanel.java
│   │   │               │       ├── 📄 MainFrame.form
│   │   │               │       ├── ☕ MainFrame.java
│   │   │               │       ├── ☕ NganhPanel.java
│   │   │               │       ├── ☕ NguyenVongPanel.java
│   │   │               │       ├── ☕ QuanLyUserPanel.java
│   │   │               │       ├── ☕ ThiSinhPanel.java
│   │   │               │       ├── ☕ ToHopPanel.java
│   │   │               │       └── ☕ TrangChuPanel.java
│   │   │               ├── 📁 client
│   │   │               │   └── 📁 ui
│   │   │               │       └── ☕ QuanLyNganhFrame.java
│   │   │               ├── 📁 config
│   │   │               │   ├── ☕ AppConfig.java
│   │   │               │   └── ☕ DatabaseSeeder.java
│   │   │               ├── 📁 dto
│   │   │               │   ├── ☕ DiemThiImportDTO.java
│   │   │               │   ├── ☕ NganhImportDTO.java
│   │   │               │   ├── ☕ NganhToHopImportDTO.java
│   │   │               │   ├── ☕ NguyenVongImportDTO.java
│   │   │               │   ├── ☕ QuyDoiNNImportDTO.java
│   │   │               │   ├── ☕ ThiSinhImportDTO.java
│   │   │               │   └── ☕ ToHopMonImportDTO.java
│   │   │               ├── 📁 entity
│   │   │               │   ├── ☕ BangQuyDoi.java
│   │   │               │   ├── ☕ DiemCong.java
│   │   │               │   ├── ☕ DiemThi.java
│   │   │               │   ├── ☕ Nganh.java
│   │   │               │   ├── ☕ NganhToHop.java
│   │   │               │   ├── ☕ NguyenVong.java
│   │   │               │   ├── ☕ ThiSinh.java
│   │   │               │   ├── ☕ ToHop.java
│   │   │               │   └── ☕ User.java
│   │   │               ├── 📁 repository
│   │   │               │   ├── 📁 custom
│   │   │               │   │   ├── ☕ ThiSinhCustomRepository.java
│   │   │               │   │   └── ☕ ThiSinhCustomRepositoryImpl.java
│   │   │               │   ├── ☕ BangQuyDoiRepository.java
│   │   │               │   ├── ☕ DiemCongRepository.java
│   │   │               │   ├── ☕ DiemThiRepository.java
│   │   │               │   ├── ☕ NganhRepository.java
│   │   │               │   ├── ☕ NganhToHopRepository.java
│   │   │               │   ├── ☕ NguyenVongRepository.java
│   │   │               │   ├── ☕ ThiSinhRepository.java
│   │   │               │   ├── ☕ ToHopRepository.java
│   │   │               │   └── ☕ UserRepository.java
│   │   │               ├── 📁 service
│   │   │               │   ├── 📁 dto
│   │   │               │   │   ├── ☕ DiemCongDTO.java
│   │   │               │   │   ├── ☕ DiemThiDTO.java
│   │   │               │   │   ├── ☕ ImportResultDTO.java
│   │   │               │   │   ├── ☕ NguyenVongResultDTO.java
│   │   │               │   │   ├── ☕ RowErrorDTO.java
│   │   │               │   │   └── ☕ ThiSinhDetailDTO.java
│   │   │               │   ├── 📁 impl
│   │   │               │   │   ├── ☕ BangQuyDoiServiceImpl.java
│   │   │               │   │   ├── ☕ BaoCaoServiceImpl.java
│   │   │               │   │   ├── ☕ DiemCongServiceImpl.java
│   │   │               │   │   ├── ☕ DiemThiServiceImpl.java
│   │   │               │   │   ├── ☕ NganhServiceImpl.java
│   │   │               │   │   ├── ☕ NganhToHopServiceImpl.java
│   │   │               │   │   ├── ☕ NguyenVongServiceImpl.java
│   │   │               │   │   ├── ☕ ThiSinhServiceImpl.java
│   │   │               │   │   └── ☕ ToHopServiceImpl.java
│   │   │               │   ├── 📁 interfaces
│   │   │               │   │   ├── ☕ IImportService.java
│   │   │               │   │   └── ☕ ProgressCallback.java
│   │   │               │   ├── ☕ AdmissionService.java
│   │   │               │   ├── ☕ AuthService.java
│   │   │               │   ├── ☕ BonusPointService.java
│   │   │               │   ├── ☕ ScoringService.java
│   │   │               │   └── ☕ UserService.java
│   │   │               ├── 📁 util
│   │   │               │   ├── ☕ AppConstants.java
│   │   │               │   ├── ☕ ExcelReaderUtil.java
│   │   │               │   └── ☕ StringUtils.java
│   │   │               └── ☕ AppTuyenSinhApplication.java
│   │   └── 📁 resources
│   │       ├── 📁 icon
│   │       │   ├── 🖼️ account.svg
│   │       │   ├── 🖼️ account_32px.svg
│   │       │   ├── 🖼️ add.svg
│   │       │   ├── 🖼️ area.svg
│   │       │   ├── 🖼️ area_32px.svg
│   │       │   ├── 🖼️ brand.svg
│   │       │   ├── 🖼️ brand_100px.svg
│   │       │   ├── 🖼️ brand_32px.svg
│   │       │   ├── 🖼️ cancel.svg
│   │       │   ├── 🖼️ close.png
│   │       │   ├── 🖼️ color_100px.svg
│   │       │   ├── 🖼️ customer.svg
│   │       │   ├── 🖼️ customer_32px.svg
│   │       │   ├── 🖼️ customerr.svg
│   │       │   ├── 🖼️ delete.svg
│   │       │   ├── 🖼️ detail.svg
│   │       │   ├── 🖼️ edit.svg
│   │       │   ├── 🖼️ edit_25px.png
│   │       │   ├── 🖼️ export.svg
│   │       │   ├── 🖼️ export_32px.svg
│   │       │   ├── 🖼️ export_excel.svg
│   │       │   ├── 🖼️ factory_100px.svg
│   │       │   ├── 🖼️ find.png
│   │       │   ├── 🖼️ ghinhangopy.svg
│   │       │   ├── 🖼️ glass_12861007.png
│   │       │   ├── 🖼️ gopy.svg
│   │       │   ├── 🖼️ home.svg
│   │       │   ├── 🖼️ home_32px.svg
│   │       │   ├── 🖼️ import.svg
│   │       │   ├── 🖼️ import_32px.svg
│   │       │   ├── 🖼️ import_excel.svg
│   │       │   ├── 🖼️ info.png
│   │       │   ├── 🖼️ inventory_32px.svg
│   │       │   ├── 🖼️ log_out.svg
│   │       │   ├── 🖼️ log_out_32px.svg
│   │       │   ├── 🖼️ man_50px.svg
│   │       │   ├── 🖼️ menu_home.svg
│   │       │   ├── 🖼️ nhomquyen.svg
│   │       │   ├── 🖼️ permission.svg
│   │       │   ├── 🖼️ permission_32px.svg
│   │       │   ├── 🖼️ phone.svg
│   │       │   ├── 🖼️ product.svg
│   │       │   ├── 🖼️ product_32px.svg
│   │       │   ├── 🖼️ productt.svg
│   │       │   ├── 🖼️ ram_100px.svg
│   │       │   ├── 🖼️ refresh.svg
│   │       │   ├── 🖼️ rom_100px.svg
│   │       │   ├── 🖼️ search.svg
│   │       │   ├── 🖼️ shoe_product.svg
│   │       │   ├── 🖼️ staff.svg
│   │       │   ├── 🖼️ staff_32px.svg
│   │       │   ├── 🖼️ stafff.svg
│   │       │   ├── 🖼️ statistical.svg
│   │       │   ├── 🖼️ sucess.png
│   │       │   ├── 🖼️ supplier.svg
│   │       │   ├── 🖼️ supplier_32px.svg
│   │       │   ├── 🖼️ tinhbaomat_128px.svg
│   │       │   ├── 🖼️ tinhchinhxac_128px.svg
│   │       │   ├── 🖼️ tinhchinhxac_64px.svg
│   │       │   ├── 🖼️ tinhhieuqua_128px.svg
│   │       │   ├── 🖼️ warning.png
│   │       │   └── 🖼️ women_50px.svg
│   │       ├── 📁 img
│   │       │   ├── 🖼️ imgTrangChu.jpg
│   │       │   └── 🖼️ logoLogin.png
│   │       └── 📄 application.properties
│   └── 📁 test
│       ├── 📁 java
│       │   └── 📁 com
│       │       └── 📁 sgu
│       │           └── 📁 tuyensinh
│       │               ├── 📁 service
│       │               │   ├── ☕ AdmissionIntegrationTest.java
│       │               │   ├── ☕ BangQuyDoiServiceTest.java
│       │               │   ├── ☕ BonusPointIntegrationTest.java
│       │               │   ├── ☕ DanhMucImportIntegrationTest.java
│       │               │   ├── ☕ DiemThiIntegrationTest.java
│       │               │   ├── ☕ NganhServiceTest.java
│       │               │   ├── ☕ NguyenVongServiceTest.java
│       │               │   ├── ☕ ScoringServiceTest.java
│       │               │   ├── ☕ ThiSinhImportIntegrationTest.java
│       │               │   ├── ☕ ThiSinhServiceTest.java
│       │               │   ├── ☕ ToHopServiceTest.java
│       │               │   └── ☕ UserServiceTest.java
│       │               ├── 📁 util
│       │               │   └── ☕ ExcelReaderUtilTest.java
│       │               ├── ☕ BangQuyDoiServiceImplTest.java
│       │               ├── ☕ NganhServiceImpl.java
│       │               ├── ☕ NganhToHopServiceImplTest.java
│       │               └── ☕ ToHopServiceImplTest.java
│       └── 📁 resources
│           ├── 📁 test_data
│           │   ├── 📄 Ds_quy_doi_tieng_Anh_test.xlsx
│           │   └── 📄 Ds_thi_sinh_test.xlsx
│           ├── 📄 application.properties
│           ├── 📄 importBangQuyDoi.xlsx
│           ├── 📄 importNganh.xlsx
│           ├── 📄 importNganhToHop.xlsx
│           └── 📄 importToHop.xlsx
├── ⚙️ .gitignore
├── 📝 README.md
├── ⚙️ docker-compose.yml
└── ⚙️ pom.xml
```

---
*Generated by FileTree Pro Extension*