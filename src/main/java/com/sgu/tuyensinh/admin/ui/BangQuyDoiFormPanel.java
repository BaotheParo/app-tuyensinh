package com.sgu.tuyensinh.admin.ui;

import com.sgu.tuyensinh.admin.ui.common.BaseTablePanel;
import com.sgu.tuyensinh.admin.ui.common.MessageDialog;
import com.sgu.tuyensinh.entity.BangQuyDoi;
import com.sgu.tuyensinh.service.BangQuyDoiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * TUẦN 4 — BangQuyDoiFormPanel (Yêu cầu 9)
 * Package: com.sgu.tuyensinh.admin.ui
 *
 * CRUD bảng quy đổi + tìm kiếm chứng chỉ.
 * Tất cả lời gọi service khớp đúng BangQuyDoiService:
 *   - search(phuongThuc, keyword)   → tìm kiếm
 *   - create(entity)                → thêm mới
 *   - update(id, entity)            → cập nhật
 *   - delete(id)                    → xóa
 *   - layDanhSachPhanTrang(...)     → load bảng
 *
 * Thay thế BangQuyDoiPanel (read-only) trong MainFrame.
 */
@Component
public class BangQuyDoiFormPanel extends JPanel {

    // ── Colors / Fonts ────────────────────────────────────────────
    private static final Color BG         = new Color(247, 248, 252);
    private static final Color BLUE       = new Color(41, 128, 185);
    private static final Color GREEN      = new Color(39, 174, 96);
    private static final Color RED        = new Color(192, 57, 43);
    private static final Color BORDER     = new Color(210, 215, 225);
    private static final Font  FONT_LABEL = new Font("Segoe UI", Font.BOLD, 12);
    private static final Font  FONT_INPUT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font  FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);

    private final BangQuyDoiService bangQuyDoiService;

    // ── Chế độ hiển thị bảng ─────────────────────────────────────
    // true = đang hiển thị kết quả search (List), false = phân trang (Page)
    private boolean isSearchMode = false;

    // ── Search ────────────────────────────────────────────────────
    // search() trong service bắt buộc phuongThuc không được rỗng
    private static final String[] PHUONG_THUC_OPTS = {"NGOAINGU", "VSAT", "DGNL"};
    private JComboBox<String> cboPhuongThucSearch;
    private JTextField txtKeyword;

    // ── Table ─────────────────────────────────────────────────────
    private BaseTablePanel tablePanel;
    private JButton btnPrev, btnNext;
    private JLabel  lblPage;
    private int currentPage = 0;
    private final int pageSize = 20;
    private int totalPages = 1;

    // ── Form nhập liệu ────────────────────────────────────────────
    // Tên field khớp BangQuyDoi entity
    private JTextField     txtMaQuyDoi;      // maQuyDoi   — @Column d_maquydoi
    private JComboBox<String> cboPhuongThucForm; // phuongThuc — @Column d_phuongthuc
    private JTextField     txtToHop;         // toHop      — @Column d_tohop
    private JTextField     txtMon;           // mon        — @Column d_mon
    private JTextField     txtPhanVi;        // phanVi     — @Column d_phanvi
    private JSpinner       spnNamHoc;        // namHoc     — @Column nam_hoc
    private JTextField     txtDiemA;         // diemGocA   — @Column d_diema
    private JTextField     txtDiemB;         // diemGocB   — @Column d_diemb
    private JTextField     txtDiemC;         // diemQuyDoiC— @Column d_diemc
    private JTextField     txtDiemD;         // diemQuyDoiD— @Column d_diemd

    // ID bản ghi đang chọn (null = chưa chọn → nút Cập nhật/Xóa disabled)
    private Integer selectedId = null;

    @Autowired
    public BangQuyDoiFormPanel(BangQuyDoiService bangQuyDoiService) {
        this.bangQuyDoiService = bangQuyDoiService;
        initUI();
        loadPage(); // load trang đầu khi khởi tạo
    }

    // ─────────────────────────────────────────────────────────────
    private void initUI() {
        setBackground(BG);
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(12, 14, 12, 14));

        add(buildSearchBar(), BorderLayout.NORTH);
        add(buildFormAndTable(), BorderLayout.CENTER);
        add(buildPagingRow(), BorderLayout.SOUTH);
    }

    // ── Search bar ────────────────────────────────────────────────
    private JPanel buildSearchBar() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                new TitledBorder(new LineBorder(BORDER, 1, true),
                        "Tìm kiếm chứng chỉ / bảng quy đổi  (Yêu cầu 9)",
                        TitledBorder.LEFT, TitledBorder.TOP, FONT_LABEL),
                new EmptyBorder(4, 8, 6, 8)
        ));

        // Phương thức — bắt buộc khi search (service throw nếu rỗng)
        cboPhuongThucSearch = new JComboBox<>(PHUONG_THUC_OPTS);
        cboPhuongThucSearch.setFont(FONT_INPUT);
        cboPhuongThucSearch.setPreferredSize(new Dimension(130, 30));

        txtKeyword = new JTextField(22);
        txtKeyword.setFont(FONT_INPUT);
        txtKeyword.setPreferredSize(new Dimension(220, 30));
        txtKeyword.setToolTipText(
                "NGOAINGU/VSAT: tìm theo tên môn/chứng chỉ (VD: IELTS)\n" +
                        "DGNL: tìm theo mã tổ hợp (VD: A00)"
        );

        JButton btnSearch = styleBtn("Tìm kiếm", BLUE);
        btnSearch.addActionListener(e -> executeSearch());
        txtKeyword.addActionListener(e -> executeSearch());

        JButton btnShowAll = styleBtn("Hiện tất cả", new Color(100, 110, 130));
        btnShowAll.addActionListener(e -> {
            isSearchMode = false;
            currentPage = 0;
            txtKeyword.setText("");
            loadPage();
        });

        p.add(new JLabel("Phương thức:") {{ setFont(FONT_LABEL); }});
        p.add(cboPhuongThucSearch);
        p.add(new JLabel("Từ khóa:") {{ setFont(FONT_LABEL); }});
        p.add(txtKeyword);
        p.add(btnSearch);
        p.add(btnShowAll);
        return p;
    }

    // ── Form + Table ──────────────────────────────────────────────
    private JPanel buildFormAndTable() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setOpaque(false);
        p.add(buildInputForm(), BorderLayout.NORTH);
        p.add(buildTable(),     BorderLayout.CENTER);
        return p;
    }

    // ── Input form ────────────────────────────────────────────────
    private JPanel buildInputForm() {
        JPanel card = new JPanel(new BorderLayout(12, 0));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new TitledBorder(new LineBorder(BORDER, 1, true),
                        "Thêm mới / Cập nhật thông số quy đổi",
                        TitledBorder.LEFT, TitledBorder.TOP, FONT_LABEL),
                new EmptyBorder(10, 14, 10, 14)
        ));

        // Grid 3 hàng × 6 cột
        JPanel grid = new JPanel(new GridLayout(3, 6, 8, 8));
        grid.setOpaque(false);

        // Khởi tạo widgets
        txtMaQuyDoi = input("VD: IELTS_10  (bắt buộc, unique)");
        cboPhuongThucForm = new JComboBox<>(PHUONG_THUC_OPTS);
        cboPhuongThucForm.setFont(FONT_INPUT);

        txtToHop  = input("VD: A00  (DGNL dùng, có thể trống)");
        txtMon    = input("VD: IELTS  (NGOAINGU/VSAT dùng)");
        txtPhanVi = input("VD: Bac 3  (nhãn phân vị, tùy chọn)");
        spnNamHoc = new JSpinner(new SpinnerNumberModel(2026, 2020, 2030, 1));
        spnNamHoc.setFont(FONT_INPUT);

        txtDiemA = input("Điểm gốc từ (d_diema) *");
        txtDiemB = input("Điểm gốc đến (d_diemb)");
        txtDiemC = input("Điểm quy đổi từ (d_diemc)");
        txtDiemD = input("Điểm quy đổi đến (d_diemd)");

        // Hàng 1: maQuyDoi | phuongThuc | toHop
        grid.add(lbl("Mã quy đổi *"));   grid.add(txtMaQuyDoi);
        grid.add(lbl("Phương thức *"));   grid.add(cboPhuongThucForm);
        grid.add(lbl("Tổ hợp"));          grid.add(txtToHop);

        // Hàng 2: mon | phanVi | namHoc
        grid.add(lbl("Môn / Chứng chỉ")); grid.add(txtMon);
        grid.add(lbl("Phân vị"));          grid.add(txtPhanVi);
        grid.add(lbl("Năm học"));           grid.add(spnNamHoc);

        // Hàng 3: diemA | diemB | diemC
        grid.add(lbl("Điểm A (gốc từ) *"));   grid.add(txtDiemA);
        grid.add(lbl("Điểm B (gốc đến)"));     grid.add(txtDiemB);
        grid.add(lbl("Điểm C (quy đổi từ)")); grid.add(txtDiemC);

        // Hàng phụ: diemD (không vừa grid 3×6)
        JPanel rowD = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        rowD.setOpaque(false);
        rowD.add(lbl("Điểm D (quy đổi đến)"));
        txtDiemD.setPreferredSize(new Dimension(180, 28));
        rowD.add(txtDiemD);

        // Nút hành động
        JPanel btnPanel = new JPanel(new GridLayout(4, 1, 5, 6));
        btnPanel.setOpaque(false);
        btnPanel.setPreferredSize(new Dimension(120, 0));

        JButton btnAdd    = styleBtn("Thêm mới", BLUE);
        JButton btnUpdate = styleBtn("Cập nhật", GREEN);
        JButton btnDelete = styleBtn("Xóa",      RED);
        JButton btnClear  = styleBtn("Làm mới",  new Color(100, 110, 130));

        btnAdd.addActionListener(e    -> executeCreate());
        btnUpdate.addActionListener(e -> executeUpdate());
        btnDelete.addActionListener(e -> executeDelete());
        btnClear.addActionListener(e  -> clearForm());

        btnPanel.add(btnAdd);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        btnPanel.add(btnClear);

        JPanel center = new JPanel(new BorderLayout(0, 6));
        center.setOpaque(false);
        center.add(grid, BorderLayout.NORTH);
        center.add(rowD, BorderLayout.CENTER);

        card.add(center,   BorderLayout.CENTER);
        card.add(btnPanel, BorderLayout.EAST);
        return card;
    }

    // ── Table ─────────────────────────────────────────────────────
    private JPanel buildTable() {
        String[] cols = {
                "ID", "Mã QĐ", "Phương thức", "Tổ hợp", "Môn",
                "Điểm A", "Điểm B", "Điểm C", "Điểm D", "Phân vị", "Năm"
        };
        tablePanel = new BaseTablePanel(cols);
        tablePanel.getTable().setRowHeight(28);
        // Click dòng → đổ lên form
        tablePanel.getTable().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) fillFormFromRow();
        });
        return tablePanel;
    }

    // ── Phân trang ────────────────────────────────────────────────
    private JPanel buildPagingRow() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 6));
        p.setBackground(BG);
        btnPrev = new JButton("<< Trước");
        btnNext = new JButton("Sau >>");
        lblPage = new JLabel("Trang: 1/1");
        lblPage.setFont(FONT_SMALL);
        btnPrev.addActionListener(e -> {
            if (!isSearchMode && currentPage > 0) { currentPage--; loadPage(); }
        });
        btnNext.addActionListener(e -> {
            if (!isSearchMode && currentPage < totalPages - 1) { currentPage++; loadPage(); }
        });
        p.add(btnPrev); p.add(lblPage); p.add(btnNext);
        return p;
    }

    // ─────────────────────────────────────────────────────────────
    //  Data operations — đúng theo BangQuyDoiService
    // ─────────────────────────────────────────────────────────────

    /**
     * Load bảng phân trang — dùng layDanhSachPhanTrang(page, size, keyword).
     * keyword rỗng → findAll với phân trang.
     */
    public void loadPage() {
        isSearchMode = false;
        SwingWorker<Page<BangQuyDoi>, Void> worker = new SwingWorker<>() {
            @Override
            protected Page<BangQuyDoi> doInBackground() {
                // layDanhSachPhanTrang hỗ trợ keyword chung (phuongThuc + mon)
                return bangQuyDoiService.layDanhSachPhanTrang(currentPage, pageSize, "");
            }
            @Override
            protected void done() {
                try {
                    Page<BangQuyDoi> page = get();
                    totalPages = page.getTotalPages() == 0 ? 1 : page.getTotalPages();
                    updateTable(page.getContent());
                    lblPage.setText("Trang: " + (currentPage + 1) + "/" + totalPages);
                    btnPrev.setEnabled(currentPage > 0);
                    btnNext.setEnabled(currentPage < totalPages - 1);
                } catch (Exception ex) {
                    MessageDialog.showError("Lỗi tải dữ liệu: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    /**
     * Tìm kiếm — dùng search(phuongThuc, keyword).
     * Service bắt buộc phuongThuc không rỗng, keyword có thể rỗng.
     */
    private void executeSearch() {
        String phuongThuc = (String) cboPhuongThucSearch.getSelectedItem();
        String keyword    = txtKeyword.getText().trim();

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        SwingWorker<List<BangQuyDoi>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<BangQuyDoi> doInBackground() {
                // search() xử lý đúng theo phuongThuc:
                //   VSAT/NGOAINGU → filter theo mon
                //   DGNL          → filter theo toHop
                return bangQuyDoiService.search(phuongThuc, keyword);
            }
            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                try {
                    List<BangQuyDoi> results = get();
                    isSearchMode = true;
                    updateTable(results);
                    lblPage.setText("Kết quả: " + results.size() + " bản ghi");
                    btnPrev.setEnabled(false);
                    btnNext.setEnabled(false);
                } catch (Exception ex) {
                    // Bao gồm IllegalArgumentException từ service khi phuongThuc lạ
                    MessageDialog.showError("Lỗi tìm kiếm: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    /**
     * Thêm mới — dùng create(entity).
     * Service tự check duplicate (phuongThuc + toHop + mon + maQuyDoi).
     */
    private void executeCreate() {
        BangQuyDoi entity = buildEntityFromForm();
        if (entity == null) return; // validate thất bại

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        SwingWorker<BangQuyDoi, Void> worker = new SwingWorker<>() {
            @Override
            protected BangQuyDoi doInBackground() {
                return bangQuyDoiService.create(entity);
            }
            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                try {
                    get();
                    MessageDialog.showInfo("Thêm mới thành công!");
                    clearForm();
                    loadPage();
                } catch (Exception ex) {
                    // IllegalArgumentException: "Dữ liệu quy đổi đã tồn tại" hoặc validate fail
                    MessageDialog.showError("Thêm mới thất bại: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    /**
     * Cập nhật — dùng update(id, entity).
     * Service tự load bản ghi cũ theo id rồi ghi đè.
     */
    private void executeUpdate() {
        if (selectedId == null) {
            MessageDialog.showWarning("Chọn 1 dòng trong bảng để cập nhật.");
            return;
        }
        BangQuyDoi entity = buildEntityFromForm();
        if (entity == null) return;

        final int id = selectedId;
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        SwingWorker<BangQuyDoi, Void> worker = new SwingWorker<>() {
            @Override
            protected BangQuyDoi doInBackground() {
                return bangQuyDoiService.update(id, entity);
            }
            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                try {
                    get();
                    MessageDialog.showInfo("Cập nhật thành công!");
                    clearForm();
                    if (isSearchMode) executeSearch(); else loadPage();
                } catch (Exception ex) {
                    MessageDialog.showError("Cập nhật thất bại: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    /**
     * Xóa — dùng delete(id).
     * Service tự check tồn tại trước khi xóa.
     */
    private void executeDelete() {
        if (selectedId == null) {
            MessageDialog.showWarning("Chọn 1 dòng trong bảng để xóa.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Xác nhận xóa bản ghi ID = " + selectedId + "?\n" +
                        "Mã quy đổi: " + txtMaQuyDoi.getText(),
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        final int id = selectedId;
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                bangQuyDoiService.delete(id);  // delete() đúng tên trong service
                return null;
            }
            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                try {
                    get();
                    MessageDialog.showInfo("Xóa thành công!");
                    clearForm();
                    if (isSearchMode) executeSearch(); else loadPage();
                } catch (Exception ex) {
                    MessageDialog.showError("Xóa thất bại: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    // ─────────────────────────────────────────────────────────────
    //  Helpers
    // ─────────────────────────────────────────────────────────────

    /** Đọc form → build BangQuyDoi entity. Trả null nếu validate thất bại. */
    private BangQuyDoi buildEntityFromForm() {
        try {
            BangQuyDoi b = new BangQuyDoi();
            // maQuyDoi — bắt buộc (service validate)
            b.setMaQuyDoi(require(txtMaQuyDoi, "Mã quy đổi"));
            // phuongThuc — bắt buộc (service validate)
            b.setPhuongThuc((String) cboPhuongThucForm.getSelectedItem());
            // diemGocA — bắt buộc (service validate)
            b.setDiemGocA(requireDouble(txtDiemA, "Điểm A"));

            // Optional fields
            b.setToHop(txtToHop.getText().trim().isEmpty() ? null : txtToHop.getText().trim());
            b.setMon(txtMon.getText().trim().isEmpty()     ? null : txtMon.getText().trim());
            b.setPhanVi(txtPhanVi.getText().trim().isEmpty()? null : txtPhanVi.getText().trim());
            b.setNamHoc((Integer) spnNamHoc.getValue());
            b.setDiemGocB(parseDouble(txtDiemB));
            b.setDiemQuyDoiC(parseDouble(txtDiemC));
            b.setDiemQuyDoiD(parseDouble(txtDiemD));
            return b;
        } catch (IllegalArgumentException ex) {
            MessageDialog.showError(ex.getMessage());
            return null;
        }
    }

    /** Đổ dòng đang chọn trong bảng lên form */
    private void fillFormFromRow() {
        int row = tablePanel.getTable().getSelectedRow();
        if (row < 0) return;
        JTable t = tablePanel.getTable();

        Object idVal = t.getValueAt(row, 0);
        selectedId = idVal != null ? Integer.parseInt(idVal.toString()) : null;

        txtMaQuyDoi.setText(str(t.getValueAt(row, 1)));
        setCombo(cboPhuongThucForm, str(t.getValueAt(row, 2)));
        txtToHop.setText(str(t.getValueAt(row, 3)));
        txtMon.setText(str(t.getValueAt(row, 4)));
        txtDiemA.setText(str(t.getValueAt(row, 5)));
        txtDiemB.setText(str(t.getValueAt(row, 6)));
        txtDiemC.setText(str(t.getValueAt(row, 7)));
        txtDiemD.setText(str(t.getValueAt(row, 8)));
        txtPhanVi.setText(str(t.getValueAt(row, 9)));
        Object namHoc = t.getValueAt(row, 10);
        if (namHoc != null) {
            try { spnNamHoc.setValue(Integer.parseInt(namHoc.toString())); }
            catch (NumberFormatException ignored) {}
        }
        txtMaQuyDoi.setEditable(false); // khóa PK khi đang update
    }

    /** Điền dữ liệu từ List<BangQuyDoi> vào JTable */
    private void updateTable(List<BangQuyDoi> list) {
        DefaultTableModel model = (DefaultTableModel) tablePanel.getTable().getModel();
        model.setRowCount(0);
        for (BangQuyDoi b : list) {
            tablePanel.addRow(new Object[]{
                    b.getIdqd(),
                    b.getMaQuyDoi(),
                    b.getPhuongThuc(),
                    b.getToHop(),
                    b.getMon(),
                    b.getDiemGocA(),
                    b.getDiemGocB(),
                    b.getDiemQuyDoiC(),
                    b.getDiemQuyDoiD(),
                    b.getPhanVi(),
                    b.getNamHoc()
            });
        }
    }

    private void clearForm() {
        selectedId = null;
        txtMaQuyDoi.setText(""); txtMaQuyDoi.setEditable(true);
        cboPhuongThucForm.setSelectedIndex(0);
        txtToHop.setText(""); txtMon.setText(""); txtPhanVi.setText("");
        txtDiemA.setText(""); txtDiemB.setText("");
        txtDiemC.setText(""); txtDiemD.setText("");
        spnNamHoc.setValue(2026);
        tablePanel.getTable().clearSelection();
    }

    // ── Widget helpers ────────────────────────────────────────────
    private JLabel lbl(String text) {
        JLabel l = new JLabel(text + ":");
        l.setFont(FONT_LABEL);
        l.setForeground(new Color(60, 70, 90));
        return l;
    }

    private JTextField input(String tooltip) {
        JTextField f = new JTextField();
        f.setFont(FONT_INPUT);
        f.setToolTipText(tooltip);
        f.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1),
                new EmptyBorder(3, 6, 3, 6)
        ));
        return f;
    }

    private JButton styleBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFont(FONT_LABEL);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void setCombo(JComboBox<String> cb, String val) {
        for (int i = 0; i < cb.getItemCount(); i++)
            if (val != null && val.equals(cb.getItemAt(i))) { cb.setSelectedIndex(i); return; }
    }

    private String str(Object v)   { return v != null ? v.toString() : ""; }

    private String require(JTextField f, String name) {
        String v = f.getText().trim();
        if (v.isEmpty()) throw new IllegalArgumentException(name + " không được để trống.");
        return v;
    }

    private Double requireDouble(JTextField f, String name) {
        String v = f.getText().trim();
        if (v.isEmpty()) throw new IllegalArgumentException(name + " không được để trống.");
        try { return Double.parseDouble(v.replace(",", ".")); }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException(name + " = '" + v + "' không phải số hợp lệ.");
        }
    }

    private Double parseDouble(JTextField f) {
        String v = f.getText().trim();
        if (v.isEmpty()) return null;
        try { return Double.parseDouble(v.replace(",", ".")); }
        catch (NumberFormatException e) { return null; }
    }
}