package com.sgu.tuyensinh.admin.ui;

import com.sgu.tuyensinh.admin.ui.common.MessageDialog;
import com.sgu.tuyensinh.entity.DiemThi;
import com.sgu.tuyensinh.service.DiemThiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;

/**
 * TUẦN 4 — DiemThiFormPanel (Nhiệm vụ 2 — Yêu cầu 6)
 * Package: com.sgu.tuyensinh.admin.ui
 *
 * Form nhập tay / cập nhật điểm thi 1 thí sinh.
 * Dùng độc lập hoặc nhúng vào DiemThiPanel hiện có.
 *
 * Cách dùng trong DiemThiPanel:
 *   // Thêm nút "Nhập tay điểm" → mở dialog chứa DiemThiFormPanel
 *   JButton btnNhapTay = new JButton("Nhập tay điểm");
 *   btnNhapTay.addActionListener(e -> DiemThiFormPanel.showDialog(this, diemThiService, cccd));
 */
@Component
public class DiemThiFormPanel extends JPanel {

    // ── Colors / Fonts ────────────────────────────────────────────
    private static final Color BG          = new Color(247, 248, 252);
    private static final Color BLUE        = new Color(41, 128, 185);
    private static final Color GREEN       = new Color(39, 174, 96);
    private static final Color RED         = new Color(192, 57, 43);
    private static final Color BORDER      = new Color(210, 215, 225);
    private static final Font  FONT_LABEL  = new Font("Segoe UI", Font.BOLD, 12);
    private static final Font  FONT_INPUT  = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font  FONT_TITLE  = new Font("Segoe UI", Font.BOLD, 15);
    private static final Font  FONT_SMALL  = new Font("Segoe UI", Font.PLAIN, 11);

    private final DiemThiService diemThiService;

    // ── Input fields — khớp DiemThiImportDTO ─────────────────────
    private JTextField txtCccd;
    private JTextField txtToan, txtVan,  txtLy,  txtHoa;
    private JTextField txtSinh, txtSu,   txtDia, txtAnh;
    private JTextField txtNk1,  txtNk2,  txtNk3, txtNk4;
    private JTextField txtNk5,  txtNk6,  txtNk7, txtNk8;

    private JLabel lblHint;

    @Autowired
    public DiemThiFormPanel(DiemThiService diemThiService) {
        this.diemThiService = diemThiService;
        initUI();
    }

    // ─────────────────────────────────────────────────────────────
    private void initUI() {
        setBackground(BG);
        setLayout(new BorderLayout(0, 10));
        setBorder(new EmptyBorder(16, 20, 16, 20));

        add(buildSearchRow(),  BorderLayout.NORTH);
        add(buildScoreForm(),  BorderLayout.CENTER);
        add(buildButtonRow(),  BorderLayout.SOUTH);
    }

    // ── Tìm CCCD để load điểm hiện tại ───────────────────────────
    private JPanel buildSearchRow() {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.setBackground(BG);

        JLabel lbl = new JLabel("CCCD thí sinh:");
        lbl.setFont(FONT_LABEL);

        txtCccd = new JTextField();
        txtCccd.setFont(new Font("Consolas", Font.BOLD, 14));
        txtCccd.setPreferredSize(new Dimension(0, 36));

        JButton btnLoad = new JButton("Tải điểm hiện tại");
        btnLoad.setFont(FONT_LABEL);
        btnLoad.setBackground(BLUE);
        btnLoad.setForeground(Color.WHITE);
        btnLoad.setBorderPainted(false);
        btnLoad.setFocusPainted(false);
        btnLoad.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLoad.setPreferredSize(new Dimension(155, 36));
        btnLoad.addActionListener(e -> loadCurrentScores());

        lblHint = new JLabel("Nhập CCCD và nhấn 'Tải' để điền điểm hiện tại, hoặc nhập mới từ đầu.");
        lblHint.setFont(FONT_SMALL);
        lblHint.setForeground(Color.GRAY);

        JPanel inputRow = new JPanel(new BorderLayout(8, 0));
        inputRow.setOpaque(false);
        inputRow.add(new JLabel("CCCD:") {{ setFont(FONT_LABEL); }}, BorderLayout.WEST);
        inputRow.add(txtCccd,   BorderLayout.CENTER);
        inputRow.add(btnLoad,   BorderLayout.EAST);

        p.add(inputRow, BorderLayout.CENTER);
        p.add(lblHint,  BorderLayout.SOUTH);
        return p;
    }

    // ── Form điểm ─────────────────────────────────────────────────
    private JPanel buildScoreForm() {
        JPanel wrapper = new JPanel(new GridLayout(1, 2, 16, 0));
        wrapper.setOpaque(false);

        // Cột trái: 8 môn chính (khớp DiemThiImportDTO)
        JPanel left = createGroup("Điểm 8 môn chính");
        txtToan = addScoreRow(left, "Toán (toan)");
        txtVan  = addScoreRow(left, "Văn  (van)");
        txtLy   = addScoreRow(left, "Lý   (ly)");
        txtHoa  = addScoreRow(left, "Hóa  (hoa)");
        txtSinh = addScoreRow(left, "Sinh (sinh)");
        txtSu   = addScoreRow(left, "Sử   (su)");
        txtDia  = addScoreRow(left, "Địa  (dia)");
        txtAnh  = addScoreRow(left, "TA   (anh)");

        // Cột phải: NK1–NK8
        JPanel right = createGroup("Điểm năng khiếu NK1–NK8");
        txtNk1 = addScoreRow(right, "NK1 (nk1)");
        txtNk2 = addScoreRow(right, "NK2 (nk2)");
        txtNk3 = addScoreRow(right, "NK3 (nk3)");
        txtNk4 = addScoreRow(right, "NK4 (nk4)");
        txtNk5 = addScoreRow(right, "NK5 (nk5)");
        txtNk6 = addScoreRow(right, "NK6 (nk6)");
        txtNk7 = addScoreRow(right, "NK7 (nk7)");
        txtNk8 = addScoreRow(right, "NK8 (nk8)");

        wrapper.add(left);
        wrapper.add(right);
        return wrapper;
    }

    private JPanel createGroup(String title) {
        JPanel p = new JPanel(new GridLayout(9, 2, 6, 6));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                new TitledBorder(new LineBorder(BORDER, 1, true), title,
                        TitledBorder.LEFT, TitledBorder.TOP,
                        FONT_LABEL, new Color(60, 80, 130)),
                new EmptyBorder(8, 12, 8, 12)
        ));
        return p;
    }

    private JTextField addScoreRow(JPanel parent, String labelText) {
        JLabel lbl = new JLabel(labelText + ":");
        lbl.setFont(FONT_LABEL);
        lbl.setForeground(new Color(70, 80, 100));

        JTextField txt = new JTextField();
        txt.setFont(FONT_INPUT);
        txt.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1),
                new EmptyBorder(3, 6, 3, 6)
        ));
        txt.setToolTipText("Nhập 0.00–10.00 hoặc để trống (null = không thi)");

        parent.add(lbl);
        parent.add(txt);
        return txt;
    }

    // ── Button row ────────────────────────────────────────────────
    private JPanel buildButtonRow() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        p.setBackground(BG);

        JButton btnClear = new JButton("Xóa trắng");
        btnClear.setFont(FONT_LABEL);
        btnClear.setPreferredSize(new Dimension(110, 34));
        btnClear.setBackground(Color.WHITE);
        btnClear.setForeground(new Color(80, 90, 110));
        btnClear.setBorder(new LineBorder(BORDER, 1));
        btnClear.setFocusPainted(false);
        btnClear.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnClear.addActionListener(e -> clearAllFields());

        JButton btnSave = new SaveButton();
        btnSave.addActionListener(e -> saveScores());

        p.add(btnClear);
        p.add(btnSave);
        return p;
    }

    // ─────────────────────────────────────────────────────────────
    //  Logic
    // ─────────────────────────────────────────────────────────────

    /** Tải điểm hiện tại từ DB vào form */
    private void loadCurrentScores() {
        String cccd = txtCccd.getText().trim();
        if (cccd.isEmpty()) {
            MessageDialog.showWarning("Vui lòng nhập CCCD trước khi tải điểm.");
            return;
        }

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        SwingWorker<DiemThi, Void> worker = new SwingWorker<>() {
            @Override
            protected DiemThi doInBackground() {
                // DiemThiService cần có hàm findByCccd(String cccd)
                return diemThiService.findByCccd(cccd);
            }

            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                try {
                    DiemThi dt = get();
                    if (dt == null) {
                        lblHint.setText("⚠ Không tìm thấy CCCD — form trống, sẽ tạo mới khi lưu.");
                        lblHint.setForeground(new Color(180, 80, 0));
                        return;
                    }
                    fillForm(dt);
                    lblHint.setText("✔ Đã tải điểm của " + cccd + ". Chỉnh sửa rồi nhấn 'Lưu'.");
                    lblHint.setForeground(GREEN);
                } catch (Exception ex) {
                    MessageDialog.showError("Lỗi tải điểm: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    /** Điền dữ liệu từ entity vào form */
    private void fillForm(DiemThi dt) {
        setField(txtToan, dt.getToan());
        setField(txtVan,  dt.getVan());
        setField(txtLy,   dt.getLy());
        setField(txtHoa,  dt.getHoa());
        setField(txtSinh, dt.getSinh());
        setField(txtSu,   dt.getSu());
        setField(txtDia,  dt.getDia());
        setField(txtAnh,  dt.getAnh());
        setField(txtNk1,  dt.getNk1());
        setField(txtNk2,  dt.getNk2());
        setField(txtNk3,  dt.getNk3());
        setField(txtNk4,  dt.getNk4());
        setField(txtNk5,  dt.getNk5());
        setField(txtNk6,  dt.getNk6());
        setField(txtNk7,  dt.getNk7());
        setField(txtNk8,  dt.getNk8());
    }

    private void setField(JTextField field, Double val) {
        field.setText(val != null ? String.format("%.2f", val) : "");
    }

    /** Lưu điểm — validate trước, sau đó gọi DiemThiService */
    private void saveScores() {
        String cccd = txtCccd.getText().trim();
        if (cccd.isEmpty()) {
            MessageDialog.showWarning("Vui lòng nhập CCCD.");
            return;
        }

        DiemThi dt = new DiemThi();
        dt.setCccd(cccd);

        try {
            // Parse và validate — null nếu ô trống (DiemThiImportDTO dùng Double nullable)
            dt.setToan(parseScore(txtToan, "Toán"));
            dt.setVan (parseScore(txtVan,  "Văn"));
            dt.setLy  (parseScore(txtLy,   "Lý"));
            dt.setHoa (parseScore(txtHoa,  "Hóa"));
            dt.setSinh(parseScore(txtSinh, "Sinh"));
            dt.setSu  (parseScore(txtSu,   "Sử"));
            dt.setDia (parseScore(txtDia,  "Địa"));
            dt.setAnh (parseScore(txtAnh,  "TA"));
            dt.setNk1 (parseScore(txtNk1,  "NK1"));
            dt.setNk2 (parseScore(txtNk2,  "NK2"));
            dt.setNk3 (parseScore(txtNk3,  "NK3"));
            dt.setNk4 (parseScore(txtNk4,  "NK4"));
            dt.setNk5 (parseScore(txtNk5,  "NK5"));
            dt.setNk6 (parseScore(txtNk6,  "NK6"));
            dt.setNk7 (parseScore(txtNk7,  "NK7"));
            dt.setNk8 (parseScore(txtNk8,  "NK8"));
        } catch (IllegalArgumentException ex) {
            MessageDialog.showError(ex.getMessage());
            return;
        }

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        SwingWorker<DiemThi, Void> worker = new SwingWorker<>() {
            @Override
            protected DiemThi doInBackground() {
                return diemThiService.updateDiemThi(cccd, dt);
            }

            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                try {
                    get();
                    MessageDialog.showInfo("Cập nhật điểm thành công cho CCCD: " + cccd);
                    lblHint.setText("✔ Đã lưu điểm cho " + cccd);
                    lblHint.setForeground(GREEN);
                } catch (Exception ex) {
                    MessageDialog.showError("Lưu thất bại: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    /**
     * Parse ô nhập điểm:
     *   - Ô trống → null (không thi — DiemThiImportDTO dùng Double nullable)
     *   - Có giá trị → validate [0, 10]
     */
    private Double parseScore(JTextField field, String fieldName) throws IllegalArgumentException {
        String txt = field.getText().trim();
        if (txt.isEmpty()) return null;
        try {
            double v = Double.parseDouble(txt.replace(",", "."));
            if (v < 0 || v > 10) {
                throw new IllegalArgumentException(
                        "Điểm " + fieldName + " = " + v + " nằm ngoài [0.00, 10.00]");
            }
            return v;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Điểm " + fieldName + " = '" + txt + "' không phải số hợp lệ");
        }
    }

    private void clearAllFields() {
        JTextField[] all = {txtToan,txtVan,txtLy,txtHoa,txtSinh,txtSu,txtDia,txtAnh,
                txtNk1,txtNk2,txtNk3,txtNk4,txtNk5,txtNk6,txtNk7,txtNk8};
        for (JTextField f : all) f.setText("");
        lblHint.setText("Nhập CCCD và nhấn 'Tải' để điền điểm hiện tại, hoặc nhập mới từ đầu.");
        lblHint.setForeground(Color.GRAY);
    }

    /** Pre-fill CCCD từ bên ngoài (VD: từ DiemThiPanel khi chọn dòng) */
    public void setCccd(String cccd) {
        txtCccd.setText(cccd);
        loadCurrentScores();
    }

    // ─────────────────────────────────────────────────────────────
    //  Static helper: mở dialog từ DiemThiPanel
    // ─────────────────────────────────────────────────────────────
    /**
     * Mở dialog chứa form nhập điểm tay.
     * Gọi từ DiemThiPanel:
     *   DiemThiFormPanel.showDialog(this, diemThiService, selectedCccd);
     */
    public static void showDialog(Component parent, DiemThiService service, String cccd) {
        DiemThiFormPanel form = new DiemThiFormPanel(service);
        if (cccd != null && !cccd.isEmpty()) {
            form.txtCccd.setText(cccd);
            form.loadCurrentScores();
        }

        JDialog dialog = new JDialog(
                SwingUtilities.getWindowAncestor(parent) instanceof Frame
                        ? (Frame) SwingUtilities.getWindowAncestor(parent) : null,
                "Nhập / Cập nhật Điểm Thi Thủ Công",
                true
        );
        dialog.setContentPane(form);
        dialog.setSize(720, 580);
        dialog.setMinimumSize(new Dimension(640, 500));
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    // ── Custom Save Button (tự vẽ để màu không bị L&F override) ──
    private static class SaveButton extends JButton {
        SaveButton() {
            super("Lưu điểm");
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setPreferredSize(new Dimension(110, 34));
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            Color bg;
            if (!isEnabled())            bg = new Color(180, 190, 200);
            else if (getModel().isPressed()) bg = new Color(30, 130, 70);
            else if (getModel().isRollover()) bg = new Color(46, 200, 110);
            else                          bg = new Color(39, 174, 96);

            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);

            g2.setColor(Color.WHITE);
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(getText())) / 2;
            int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(getText(), x, y);
            g2.dispose();
        }
    }
}