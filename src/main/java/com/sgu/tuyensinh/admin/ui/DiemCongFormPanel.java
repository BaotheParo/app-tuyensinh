package com.sgu.tuyensinh.admin.ui;

import com.sgu.tuyensinh.admin.ui.common.BaseTablePanel;
import com.sgu.tuyensinh.admin.ui.common.MessageDialog;
import com.sgu.tuyensinh.entity.DiemCong;
import com.sgu.tuyensinh.service.DiemCongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * TUẦN 4 — DiemCongFormPanel (Nhiệm vụ 2 — Yêu cầu 7)
 * Package: com.sgu.tuyensinh.admin.ui
 *
 * Thêm / sửa điểm cộng (HSG hoặc chứng chỉ TA) cho 1 thí sinh bằng tay.
 *
 * Fields khớp DiemCong entity:
 *   tsCccd, manganh, matohop, phuongthuc,
 *   diemCC, diemUtxt, diemTong, ghichu, dc_keys, ngayCap
 *
 * Gắn vào MainFrame: thêm button "Điểm cộng" trong sidebar hoặc nhúng vào DiemUuTienPanel.
 */
@Component
public class DiemCongFormPanel extends JPanel {

    private static final Color BG         = new Color(247, 248, 252);
    private static final Color BLUE       = new Color(41, 128, 185);
    private static final Color GREEN      = new Color(39, 174, 96);
    private static final Color RED        = new Color(192, 57, 43);
    private static final Color BORDER     = new Color(210, 215, 225);
    private static final Font  FONT_LABEL = new Font("Segoe UI", Font.BOLD, 12);
    private static final Font  FONT_INPUT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font  FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);

    private final DiemCongService diemCongService;

    // ── Search ────────────────────────────────────────────────────
    private JTextField txtSearchCccd;

    // ── Table ─────────────────────────────────────────────────────
    private BaseTablePanel tablePanel;
    private JButton btnPrev, btnNext;
    private JLabel  lblPage;
    private int currentPage = 0;
    private final int pageSize = 20;
    private int totalPages = 1;

    // ── Form fields — khớp DiemCong entity ───────────────────────
    private JTextField txtCccd;       // tsCccd
    private JTextField txtMaNganh;    // manganh
    private JTextField txtMaToHop;    // matohop
    private JComboBox<String> cboPhuongThuc; // phuongthuc
    private JTextField txtDiemCC;     // diemCC
    private JTextField txtDiemUtxt;   // diemUtxt
    private JTextField txtDiemTong;   // diemTong (auto-tính)
    private JTextField txtGhiChu;     // ghichu
    private JTextField txtNgayCap;    // ngayCap (dd/MM/yyyy, nullable)
    private JTextField txtDcKeys;     // dc_keys (unique key)

    private Integer selectedId = null;

    @Autowired
    public DiemCongFormPanel(DiemCongService diemCongService) {
        this.diemCongService = diemCongService;
        initUI();
        loadData();
    }

    private void initUI() {
        setBackground(BG);
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(12, 14, 12, 14));

        add(buildTop(),       BorderLayout.NORTH);
        add(buildTable(),     BorderLayout.CENTER);
        add(buildPaging(),    BorderLayout.SOUTH);
    }

    // ── Top: search + form ────────────────────────────────────────
    private JPanel buildTop() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setOpaque(false);
        p.add(buildSearch(), BorderLayout.NORTH);
        p.add(buildForm(),   BorderLayout.CENTER);
        return p;
    }

    // ── Search bar ────────────────────────────────────────────────
    private JPanel buildSearch() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                new TitledBorder(new LineBorder(BORDER, 1, true),
                        "Tìm kiếm theo CCCD",
                        TitledBorder.LEFT, TitledBorder.TOP, FONT_LABEL),
                new EmptyBorder(4, 8, 4, 8)
        ));

        txtSearchCccd = new JTextField(20);
        txtSearchCccd.setFont(FONT_INPUT);
        txtSearchCccd.setPreferredSize(new Dimension(240, 30));

        JButton btn = new JButton("Tìm kiếm");
        btn.setFont(FONT_LABEL);
        btn.setBackground(BLUE);
        btn.setForeground(Color.WHITE);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(100, 30));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> { currentPage = 0; loadData(); });
        txtSearchCccd.addActionListener(e -> { currentPage = 0; loadData(); });

        p.add(new JLabel("CCCD:") {{ setFont(FONT_LABEL); }});
        p.add(txtSearchCccd);
        p.add(btn);
        return p;
    }

    // ── Input form ────────────────────────────────────────────────
    private JPanel buildForm() {
        JPanel card = new JPanel(new BorderLayout(12, 0));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new TitledBorder(new LineBorder(BORDER, 1, true),
                        "Thêm / Cập nhật điểm cộng thủ công",
                        TitledBorder.LEFT, TitledBorder.TOP, FONT_LABEL),
                new EmptyBorder(8, 12, 8, 12)
        ));

        JPanel grid = new JPanel(new GridLayout(3, 6, 8, 8));
        grid.setOpaque(false);

        // Row 1
        txtCccd = input("CCCD thí sinh *");
        txtMaNganh = input("Mã ngành (VD: 7480201)");
        txtMaToHop = input("Mã tổ hợp (VD: A01)");

        String[] pts = {"PT1", "PT2", "PT3", "PT4"};
        cboPhuongThuc = new JComboBox<>(pts);
        cboPhuongThuc.setFont(FONT_INPUT);

        txtDcKeys  = input("dc_keys (CCCD_ngành_tohop) *");
        txtNgayCap = input("Ngày cấp CC (dd/MM/yyyy)");

        grid.add(lbl("CCCD *"));         grid.add(txtCccd);
        grid.add(lbl("Mã ngành"));       grid.add(txtMaNganh);
        grid.add(lbl("Mã tổ hợp"));      grid.add(txtMaToHop);

        // Row 2
        txtDiemCC   = input("Điểm chứng chỉ");
        txtDiemUtxt = input("Điểm ưu tiên");
        txtDiemTong = input("Điểm tổng (auto)");
        txtDiemTong.setEditable(false);
        txtDiemTong.setBackground(new Color(245, 246, 250));

        grid.add(lbl("Phương thức"));    grid.add(cboPhuongThuc);
        grid.add(lbl("dc_keys *"));      grid.add(txtDcKeys);
        grid.add(lbl("Ngày cấp CC"));    grid.add(txtNgayCap);

        // Row 3
        txtGhiChu = input("Mô tả (VD: HSG Quốc gia Toán)");

        grid.add(lbl("Điểm CC"));        grid.add(txtDiemCC);
        grid.add(lbl("Điểm ưu tiên"));   grid.add(txtDiemUtxt);
        grid.add(lbl("Điểm tổng"));      grid.add(txtDiemTong);

        // Auto-tính diemTong khi nhập
        java.awt.event.KeyAdapter autoCalc = new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) { autoCalcTong(); }
        };
        txtDiemCC.addKeyListener(autoCalc);
        txtDiemUtxt.addKeyListener(autoCalc);

        // Buttons
        JPanel btnPanel = new JPanel(new GridLayout(5, 1, 5, 5));
        btnPanel.setOpaque(false);
        btnPanel.setPreferredSize(new Dimension(120, 0));
        btnPanel.add(actionBtn("Thêm mới", BLUE,  e -> executeSave(true)));
        btnPanel.add(actionBtn("Cập nhật", GREEN, e -> executeSave(false)));
        btnPanel.add(actionBtn("Xóa",      RED,   e -> executeDelete()));
        btnPanel.add(actionBtn("Làm mới",  new Color(100, 110, 130), e -> clearForm()));

        JPanel bottom = new JPanel(new BorderLayout(0, 4));
        bottom.setOpaque(false);
        JPanel ghiChuRow = new JPanel(new BorderLayout(8, 0));
        ghiChuRow.setOpaque(false);
        ghiChuRow.add(lbl("Ghi chú"), BorderLayout.WEST);
        ghiChuRow.add(txtGhiChu,      BorderLayout.CENTER);

        bottom.add(grid,      BorderLayout.NORTH);
        bottom.add(ghiChuRow, BorderLayout.CENTER);

        card.add(bottom,   BorderLayout.CENTER);
        card.add(btnPanel, BorderLayout.EAST);
        return card;
    }

    // ── Table ─────────────────────────────────────────────────────
    private JPanel buildTable() {
        String[] cols = {"ID", "CCCD", "Mã ngành", "Tổ hợp", "PT", "Điểm CC", "Điểm UT", "Tổng", "Ghi chú", "Ngày cấp"};
        tablePanel = new BaseTablePanel(cols);
        tablePanel.getTable().setRowHeight(28);
        tablePanel.getTable().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) fillFormFromTable();
        });
        return tablePanel;
    }

    private JPanel buildPaging() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 6));
        p.setBackground(BG);
        btnPrev = new JButton("<< Trước");
        btnNext = new JButton("Sau >>");
        lblPage = new JLabel("Trang: 1/1");
        lblPage.setFont(FONT_SMALL);
        btnPrev.addActionListener(e -> { if (currentPage > 0) { currentPage--; loadData(); }});
        btnNext.addActionListener(e -> { if (currentPage < totalPages-1) { currentPage++; loadData(); }});
        p.add(btnPrev); p.add(lblPage); p.add(btnNext);
        return p;
    }

    // ─────────────────────────────────────────────────────────────
    //  Data operations
    // ─────────────────────────────────────────────────────────────
    public void loadData() {
        String keyword = txtSearchCccd.getText().trim();
        SwingWorker<Page<DiemCong>, Void> worker = new SwingWorker<>() {
            @Override
            protected Page<DiemCong> doInBackground() {
                return diemCongService.layDanhSachPhanTrang(currentPage, pageSize, keyword);
            }
            @Override
            protected void done() {
                try {
                    Page<DiemCong> page = get();
                    totalPages = page.getTotalPages() == 0 ? 1 : page.getTotalPages();
                    lblPage.setText("Trang: " + (currentPage+1) + "/" + totalPages);

                    DefaultTableModel model = (DefaultTableModel) tablePanel.getTable().getModel();
                    model.setRowCount(0);
                    for (DiemCong d : page.getContent()) {
                        tablePanel.addRow(new Object[]{
                                d.getIddiemcong(), d.getTsCccd(), d.getManganh(),
                                d.getMatohop(), d.getPhuongthuc(),
                                d.getDiemCC(), d.getDiemUtxt(), d.getDiemTong(),
                                d.getGhichu(), d.getNgayCap()
                        });
                    }
                    btnPrev.setEnabled(currentPage > 0);
                    btnNext.setEnabled(currentPage < totalPages - 1);
                } catch (Exception ex) {
                    MessageDialog.showError("Lỗi tải dữ liệu: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void fillFormFromTable() {
        int row = tablePanel.getTable().getSelectedRow();
        if (row < 0) return;
        JTable t = tablePanel.getTable();

        selectedId = t.getValueAt(row, 0) != null
                ? Integer.parseInt(t.getValueAt(row, 0).toString()) : null;

        txtCccd.setText(strVal(t.getValueAt(row, 1)));
        txtMaNganh.setText(strVal(t.getValueAt(row, 2)));
        txtMaToHop.setText(strVal(t.getValueAt(row, 3)));
        setCombo(cboPhuongThuc, strVal(t.getValueAt(row, 4)));
        txtDiemCC.setText(strVal(t.getValueAt(row, 5)));
        txtDiemUtxt.setText(strVal(t.getValueAt(row, 6)));
        txtDiemTong.setText(strVal(t.getValueAt(row, 7)));
        txtGhiChu.setText(strVal(t.getValueAt(row, 8)));
        txtNgayCap.setText(strVal(t.getValueAt(row, 9)));
        txtCccd.setEditable(false);
    }

    private void executeSave(boolean isNew) {
        try {
            DiemCong d = new DiemCong();
            if (!isNew && selectedId != null) d.setIddiemcong(selectedId);

            d.setTsCccd(require(txtCccd, "CCCD"));
            d.setManganh(txtMaNganh.getText().trim());
            d.setMatohop(txtMaToHop.getText().trim());
            d.setPhuongthuc((String) cboPhuongThuc.getSelectedItem());
            d.setGhichu(txtGhiChu.getText().trim());
            d.setNgayCap(txtNgayCap.getText().trim().isEmpty() ? null : txtNgayCap.getText().trim());

            String keysVal = txtDcKeys.getText().trim();
            if (keysVal.isEmpty()) {
                keysVal = d.getTsCccd() + "_"
                        + (d.getManganh().isEmpty() ? "ALL" : d.getManganh()) + "_"
                        + (d.getMatohop().isEmpty()  ? "ALL" : d.getMatohop());
            }
            d.setDcKeys(keysVal);

            d.setDiemCC(parseDouble(txtDiemCC, "Điểm CC"));
            d.setDiemUtxt(parseDouble(txtDiemUtxt, "Điểm ưu tiên"));
            double cc  = d.getDiemCC()   != null ? d.getDiemCC()   : 0.0;
            double ut  = d.getDiemUtxt() != null ? d.getDiemUtxt() : 0.0;
            d.setDiemTong(cc + ut);

            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            DiemCong finalD = d;
            SwingWorker<DiemCong, Void> worker = new SwingWorker<>() {
                @Override
                protected DiemCong doInBackground() {
                    // ĐÃ SỬA: Gọi đúng hàm addDiemCong hoặc updateDiemCong
                    if (isNew) {
                        return diemCongService.addDiemCong(finalD.getTsCccd(), finalD);
                    } else {
                        return diemCongService.updateDiemCong(selectedId, finalD);
                    }
                }
                @Override
                protected void done() {
                    setCursor(Cursor.getDefaultCursor());
                    try {
                        get();
                        MessageDialog.showInfo((isNew ? "Thêm" : "Cập nhật") + " điểm cộng thành công!");
                        clearForm();
                        loadData();
                    } catch (Exception ex) {
                        MessageDialog.showError("Lỗi lưu: " + ex.getMessage());
                    }
                }
            };
            worker.execute();
        } catch (IllegalArgumentException ex) {
            MessageDialog.showError(ex.getMessage());
        }
    }

    private void executeDelete() {
        if (selectedId == null) {
            MessageDialog.showWarning("Chọn 1 dòng để xóa.");
            return;
        }
        int c = JOptionPane.showConfirmDialog(this,
                "Xác nhận xóa điểm cộng ID = " + selectedId + "?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (c != JOptionPane.YES_OPTION) return;

        final Integer idDel = selectedId;
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() {
                diemCongService.deleteDiemCong(idDel);
                return null;
            }
            @Override protected void done() {
                setCursor(Cursor.getDefaultCursor());
                try {
                    get();
                    MessageDialog.showInfo("Xóa thành công!");
                    clearForm(); loadData();
                } catch (Exception ex) {
                    MessageDialog.showError("Lỗi xóa: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void autoCalcTong() {
        try {
            double cc = txtDiemCC.getText().trim().isEmpty()   ? 0 : Double.parseDouble(txtDiemCC.getText().trim());
            double ut = txtDiemUtxt.getText().trim().isEmpty() ? 0 : Double.parseDouble(txtDiemUtxt.getText().trim());
            txtDiemTong.setText(String.format("%.2f", cc + ut));
        } catch (NumberFormatException ignored) {
            txtDiemTong.setText("?");
        }
    }

    private void clearForm() {
        selectedId = null;
        txtCccd.setText(""); txtCccd.setEditable(true);
        txtMaNganh.setText(""); txtMaToHop.setText("");
        cboPhuongThuc.setSelectedIndex(0);
        txtDiemCC.setText(""); txtDiemUtxt.setText("");
        txtDiemTong.setText(""); txtGhiChu.setText("");
        txtNgayCap.setText(""); txtDcKeys.setText("");
        tablePanel.getTable().clearSelection();
    }

    // ── Helpers ───────────────────────────────────────────────────
    private JLabel lbl(String text) {
        JLabel l = new JLabel(text + ":"); l.setFont(FONT_LABEL);
        l.setForeground(new Color(60, 70, 90));
        return l;
    }

    private JTextField input(String placeholder) {
        JTextField f = new JTextField();
        f.setFont(FONT_INPUT);
        f.setToolTipText(placeholder);
        f.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1), new EmptyBorder(3, 6, 3, 6)));
        return f;
    }

    private JButton actionBtn(String text, Color bg, java.awt.event.ActionListener al) {
        JButton b = new JButton(text);
        b.setFont(FONT_LABEL); b.setBackground(bg); b.setForeground(Color.WHITE);
        b.setBorderPainted(false); b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(al);
        return b;
    }

    private String strVal(Object v) { return v != null ? v.toString() : ""; }

    private void setCombo(JComboBox<String> cb, String val) {
        for (int i = 0; i < cb.getItemCount(); i++)
            if (val.equals(cb.getItemAt(i))) { cb.setSelectedIndex(i); return; }
    }

    private String require(JTextField f, String name) {
        String v = f.getText().trim();
        if (v.isEmpty()) throw new IllegalArgumentException(name + " không được để trống.");
        return v;
    }

    private Double parseDouble(JTextField f, String name) {
        String v = f.getText().trim();
        if (v.isEmpty()) return null;
        try { return Double.parseDouble(v.replace(",", ".")); }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException(name + " = '" + v + "' không phải số hợp lệ.");
        }
    }
}